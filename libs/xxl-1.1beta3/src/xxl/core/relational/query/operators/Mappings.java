/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
						Head of the Database Research Group
						Department of Mathematics and Computer Science
						University of Marburg
						Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.relational.query.operators;

import java.sql.ResultSetMetaData;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.collections.MappedList;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.ResetableCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.operators.mappers.TemporalMapper;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.tuples.ListTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node mapping node} in a directed acyclic graph.
 * Beside the methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Mappings {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a mapping
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "MAPPING";
	
	/**
	 * This constant is used to generate unique identifiers for the mappings
	 * inside the mapping operator's global metadata.
	 */
	public static final String MAPPING_PREFIX = "MAPPING->MAPPING_";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of a mapping operator.
	 */
	public static final Function<Object, ResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, ResultSetMetaData>() {
		@Override
		public ResultSetMetaData invoke(Object identifier, final Object operator) {
			return new ColumnMetaDataResultSetMetaData(
				Cursors.toArray(
					new Mapper<Node, ColumnMetaData>(
						new Function<Node, ColumnMetaData>() {
							@Override
							public ColumnMetaData invoke(Node node) {
								return ColumnMetaDatas.getColumnMetaData(node);
							}
						},
						Nodes.getNodes((Node)operator, MAPPING_PREFIX)
					),
					new ColumnMetaData[Nodes.countNodes((Node)operator, MAPPING_PREFIX)]
				)
			);
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new mapping operator that applies the mappings given by the
	 * specified iteration to its relational data. The mapping operator gets
	 * its relational data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        mapping operator.
	 * @param mappings an iteration over the mappings to be applied.
	 * @return a new mapping operator that applies the mappings given by the
	 *         specified iteration to its relational data.
	 */
	public static final Node newMapping(Node input, Iterator<? extends Node> mappings) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 1, Nodes.VARIABLE);
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		
		Nodes.putNodes(
			operator,
			MAPPING_PREFIX,
			mappings,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of a mapping operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	/**
	 * Creates a new mapping operator that applies the given mappings to its
	 * relational data. The mapping operator gets its relational data from the
	 * given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        mapping operator.
	 * @param mappings the mappings to be applied.
	 * @return a new mapping operator that applies the given mappings to its
	 *         relational data.
	 */
	public static final Node newMapping(Node input, Node... mappings) {
		return newMapping(input, new ArrayCursor<Node>(mappings));
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a mapping operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("a mapping operator must have exactly one input operator");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("a mapping operator must not have more than one input operators");
			
			return newMapping(
				child,
				queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
			);
		}
	};

	/**
	 * A factory method that can be used to transform a mapping operator into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			queryConverter.writeChildren(Nodes.getNodes(operator, MAPPING_PREFIX), document, element);
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical mapping
	 * operator into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("a mapping operator must have exactly one input operator");
			MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("a mapping operator must not have more than one input operators");
			
			// get the mappings
			final Cursor<Function<Tuple, ? extends Object>> mappings = new ResetableCursor<Function<Tuple, ? extends Object>>(
				new Mapper<Node, Function<Tuple, ? extends Object>>(
					new Function<Node, Function<Tuple, ? extends Object>>() {
						@Override
						public Function<Tuple, ? extends Object> invoke(Node node) {
							return (Function<Tuple, ? extends Object>)queryTranslator.translate(node);
						}
					},
					Nodes.getNodes(node, MAPPING_PREFIX)
				)
			);
			
			// get the mapping's mode
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = new TemporalMapper<Tuple, Tuple>(
						(Source<TemporalObject<Tuple>>)child,
						new Function<TemporalObject<Tuple>, TemporalObject<Tuple>>() {
							protected Function<TemporalObject<Tuple>, Tuple> unwrapTuples = new Function<TemporalObject<Tuple>, Tuple>() {
								@Override
								public Tuple invoke(TemporalObject<Tuple> tuple) {
									return tuple.getObject();
								}
							};
							
							protected Function<TemporalObject<?>, TimeInterval> intersectTimeIntervals = new Function<TemporalObject<?>, TimeInterval>() {
								@Override
								public TimeInterval invoke() {
									return new TimeInterval(Long.MIN_VALUE, Long.MAX_VALUE);
								}
								
								@Override
								public TimeInterval invoke(TemporalObject<?> temporalObject) {
									return temporalObject.getTimeInterval();
								}
								
								@Override
								public TimeInterval invoke(TemporalObject<?> firstTemporalObject, TemporalObject<?> secondTemporalObject) {
									return firstTemporalObject.getTimeInterval().intersect(secondTemporalObject.getTimeInterval());
								}
								
								@Override
								public TimeInterval invoke(List<? extends TemporalObject<?>> temporalObjects) {
									long maxstart = Long.MIN_VALUE, minend = Long.MAX_VALUE;
									TimeInterval timeInterval;
									for (TemporalObject<?> temporalObject : temporalObjects) {
										timeInterval = temporalObject.getTimeInterval();
										if (timeInterval.getStart() > maxstart)
											maxstart = timeInterval.getStart();
										if (timeInterval.getEnd() < minend)
											minend = timeInterval.getEnd();
									}
									if (maxstart >= minend) {
										String error = "Disjunct intervals:";
										for (TemporalObject<?> temporalObject : temporalObjects)
											error += "\n" + temporalObject.getTimeInterval();
										throw new IllegalArgumentException(error);
									}
									return new TimeInterval(maxstart, minend);
									
								}
							};
 
							@Override
							public TemporalObject<Tuple> invoke(List<? extends TemporalObject<Tuple>> tuples) {
								MappedList<TemporalObject<Tuple>, Tuple> unwrappedTuples = new MappedList<TemporalObject<Tuple>, Tuple>(
									tuples,
									unwrapTuples
								);
								LinkedList<Object> list = new LinkedList<Object>();
								while (mappings.hasNext())
									list.add(mappings.next().invoke(unwrappedTuples));
								mappings.reset();
								return new TemporalObject<Tuple>(
									new ListTuple(list),
									intersectTimeIntervals.invoke(tuples)
								);
							}
						}
					);
					
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					// create a passive implementation for the logical operator
					return new xxl.core.relational.cursors.Mapper(
						new MetaDataFunction<Tuple, Tuple, CompositeMetaData<Object, Object>>(
							new Function<Tuple, Tuple>() {
								@Override
								public Tuple invoke(List<? extends Tuple> tuples) {
									LinkedList<Object> list = new LinkedList<Object>();
									while (mappings.hasNext())
										list.add(mappings.next().invoke(tuples));
									mappings.reset();
									return new ListTuple(list);
								}
							}
						) {
							@Override
							public CompositeMetaData<Object, Object> getMetaData() {
								return node.getMetaData();
							}
						},
						(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)child
					) {
						{
							if (ResultSetMetaDatas.RESULTSET_METADATA_COMPARATOR.compare((ResultSetMetaData)super.getMetaData().get(ResultSetMetaDatas.RESULTSET_METADATA_TYPE), (ResultSetMetaData)getMetaData().get(ResultSetMetaDatas.RESULTSET_METADATA_TYPE)) != 0)
								throw new MetaDataException("relational metadata of logical operator and appropriate implementation does not fit");
						}
						
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown mapping operator mode " + mode);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Mappings() {
		// private access in order to ensure non-instantiability
	}
	
}
