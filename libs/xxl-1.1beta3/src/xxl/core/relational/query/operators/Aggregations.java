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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.ResetableCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.unions.Sequentializer;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.functions.MetaDataAggregationFunction;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.groupers.TemporalGroupAndAggregator;
import xxl.core.pipes.operators.mappers.TemporalAggregator;
import xxl.core.pipes.operators.mappers.TemporalMapper;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.cursors.Aggregator;
import xxl.core.relational.cursors.GroupAggregator;
import xxl.core.relational.cursors.MergeSorter;
import xxl.core.relational.cursors.SortBasedGrouper;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Aggregates;
import xxl.core.relational.query.expressions.Columns;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.tuples.ListTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.relational.tuples.Tuples;
import xxl.core.util.ArrayResizer;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of an {@link Node aggregation node} in a directed acyclic graph.
 * Beside the methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Aggregations {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents an
	 * aggregation operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "AGGREGATION";
	
	/**
	 * This constant is used to generate unique identifiers for the group
	 * columns inside the aggregation operator's global metadata.
	 */
	public static final String GROUP_COLUMN_PREFIX = "AGGREGATION->GROUP_COLUMN_";
	
	/**
	 * This constant is used to generate unique identifiers for the aggregates
	 * inside the aggregation operator's global metadata.
	 */
	public static final String AGGREGATE_PREFIX = "AGGREGATION->AGGREGATE_";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of an aggregation operator.
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
						new Sequentializer<Node>(
							Nodes.getNodes((Node)operator, GROUP_COLUMN_PREFIX),
							Nodes.getNodes((Node)operator, AGGREGATE_PREFIX)
						)
					),
					new ColumnMetaData[Nodes.countNodes((Node)operator, GROUP_COLUMN_PREFIX) + Nodes.countNodes((Node)operator, AGGREGATE_PREFIX)]
				)
			);
		}
	};

	// static 'constructors'
	
	/**
	 * Creates a new aggregation operator that applies the aggregates given by
	 * the second specified iteration to the groups specified by the group
	 * columns contained by the first specified iteration. The aggregation
	 * operator gets its relational data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        aggregation operator.
	 * @param groupColumns an iteration over the columns specifying the
	 *        aggregation operator's groups.
	 * @param aggregates an iteration over the aggregates to be applied.
	 * @return a new aggregation operator that applies the aggregates given by
	 *         the second specified iteration to the groups specified by the
	 *         group columns contained by the first specified iteration.
	 */
	public static final Node newAggregation(Node input, Iterator<? extends Node> groupColumns, Iterator<? extends Node> aggregates) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 1, Nodes.VARIABLE);
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		
		Nodes.putNodes(
			operator,
			GROUP_COLUMN_PREFIX,
			groupColumns,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == Columns.EXPRESSION_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		Nodes.putNodes(
			operator,
			AGGREGATE_PREFIX,
			aggregates,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == Aggregates.EXPRESSION_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of an aggregation operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * an aggregation operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("an aggregation operator must have exactly one input operator");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("an aggregation operator must not have more than one input operators");
			
			Cursor<Element> groupElements = QueryConverter.getChildren(element, QueryConverter.GROUP_ELEMENT);
			Cursor<Node> groupColumns = groupElements.hasNext() ?
				queryConverter.readChildren(groupElements.next(), QueryConverter.EXPRESSION_ELEMENT) :
				new EmptyCursor<Node>();
			if (groupElements.hasNext())
				throw new MetaDataException("an aggregation operator must not have more than one group definition");
			
			return newAggregation(
				child,
				groupColumns,
				queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
			);
		}
	};

	/**
	 * A factory method that can be used to transform an aggregation operator
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			Cursor<Node> groupColumns = Nodes.getNodes(operator, GROUP_COLUMN_PREFIX);
			if (groupColumns.hasNext()) {
				Element groupElement = document.createElement(QueryConverter.GROUP_ELEMENT);
				queryConverter.writeChildren(groupColumns, document, groupElement);
				element.appendChild(groupElement);
			}
			queryConverter.writeChildren(Nodes.getNodes(operator, AGGREGATE_PREFIX), document, element);
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical aggregation
	 * operator into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		@SuppressWarnings("unchecked")
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("an aggregation operator must have exactly one input operator");
			MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("an aggregation operator must not have more than one input operators");
			
			// get the group columns and aggregates
			final Cursor<Integer> groupColumns = new ResetableCursor<Integer>(
				new Mapper<Node, Integer>(
					new Function<Node, Integer>() {
						@Override
						public Integer invoke(Node node) {
							return Columns.getColumnIndex(node);
						}
					},
					Nodes.getNodes(node, GROUP_COLUMN_PREFIX)
				)
			);
			final Cursor<MetaDataAggregationFunction<Tuple, Object, CompositeMetaData<Object, Object>>> aggregates = new ResetableCursor<MetaDataAggregationFunction<Tuple, Object, CompositeMetaData<Object, Object>>>(
				new Mapper<Node, MetaDataAggregationFunction<Tuple, Object, CompositeMetaData<Object, Object>>>(
					new Function<Node, MetaDataAggregationFunction<Tuple, Object, CompositeMetaData<Object, Object>>>() {
						@Override
						public MetaDataAggregationFunction<Tuple, Object, CompositeMetaData<Object, Object>> invoke(Node node) {
							return (MetaDataAggregationFunction<Tuple, Object, CompositeMetaData<Object, Object>>)queryTranslator.translate(node);
						}
					},
					Nodes.getNodes(node, AGGREGATE_PREFIX)
				)
			);
			
			// get the aggregation's mode
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source;
					
					if (groupColumns.hasNext()) {
						source = new TemporalMapper<Entry<List<Object>, Tuple>, Tuple>(
							new TemporalGroupAndAggregator<Tuple, List<Object>, Tuple>(
								(Source<TemporalObject<Tuple>>)child,
								new Function<TemporalObject<Tuple>, List<Object>>() {
									protected int groups = 0;
									
									@Override
									public List<Object> invoke(TemporalObject<Tuple> tuple) {
										ArrayList<Object> result = new ArrayList<Object>(groups);
										
										while (groupColumns.hasNext())
											result.add(tuple.getObject().getObject(groupColumns.next()));
										groupColumns.reset();
										
										return result;
									}
								},
								new AggregationFunction<Tuple, Tuple>() {
									protected int size;
									protected int groups;
									
									{
										try {
											size = ResultSetMetaDatas.getResultSetMetaData(node).getColumnCount();
										}
										catch (SQLException sqle) {
											throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
										}
									}
									
									@Override
									public Tuple invoke(Tuple aggregate, Tuple tuple) {
										ArrayList<Object> result = new ArrayList<Object>(size);
										
										if (aggregate == null) {
											groups = 0;
											while (groupColumns.hasNext()) {
												result.add(tuple.getObject(groupColumns.next()));
												groups++;
											}
											groupColumns.reset();
											while (aggregates.hasNext())
												result.add(aggregates.next().invoke(null, tuple));
										}
										else {
											int column = 1;
											for (; column <= groups; column++)
												result.add(aggregate.getObject(column));
											while (aggregates.hasNext())
												result.add(aggregates.next().invoke(aggregate.getObject(column++), tuple));
										}
										aggregates.reset();
										
										return new ListTuple(result);
									}
								}
							),
							new Function<TemporalObject<Entry<List<Object>, Tuple>>, TemporalObject<Tuple>>() {
								@Override
								public TemporalObject<Tuple> invoke(TemporalObject<Entry<List<Object>, Tuple>> entry) {
									return new TemporalObject<Tuple>(
										entry.getObject().getValue(),
										entry.getTimeInterval()
									);
								}
							}
						);
					}
					else {
						source = new TemporalAggregator<Tuple, Tuple>(
							(Source<TemporalObject<Tuple>>)child,
							new AggregationFunction<Tuple, Tuple>() {
								protected int size;
								
								{
									try {
										size = ResultSetMetaDatas.getResultSetMetaData(node).getColumnCount();
									}
									catch (SQLException sqle) {
										throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
									}
								}
								
								@Override
								public Tuple invoke(Tuple aggregate, Tuple tuple) {
									ArrayList<Object> result = new ArrayList<Object>(size);
									
									if (aggregate == null)
										while (aggregates.hasNext())
											result.add(aggregates.next().invoke(null, tuple));
									else {
										int column = 1;
										while (aggregates.hasNext())
											result.add(aggregates.next().invoke(aggregate.getObject(column++), tuple));
									}
									aggregates.reset();
									
									return new ListTuple(result);
								}
							}
						);
					}
					
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					// create a passive implementation for the logical operator
					
					int size = 0;
					MetaDataAggregationFunction<Tuple, Object, CompositeMetaData<Object, Object>>[] aggregationFunctions = new MetaDataAggregationFunction[size = 0];
					ArrayResizer resizer = new ArrayResizer(0.5, 0);
					while (aggregates.hasNext())
						(aggregationFunctions = resizer.resize(aggregationFunctions, ++size))[size-1] = aggregates.next();
					ArrayResizer finalResizer = new ArrayResizer(1, 1);
					aggregationFunctions = finalResizer.resize(aggregationFunctions, size);
					
					if (groupColumns.hasNext()) {
						int[] groupColumnIndices = new int[size = 0];
						while (groupColumns.hasNext())
							(groupColumnIndices = (int[])resizer.resize(groupColumnIndices, ++size))[size-1] = groupColumns.next();
						groupColumnIndices = (int[])finalResizer.resize(groupColumnIndices, size);
						
						return new GroupAggregator(
							new SortBasedGrouper( 
								new MergeSorter(
									(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)child,
									Tuples.getTupleComparator(groupColumnIndices)
								),
								groupColumnIndices
							),
							aggregationFunctions,
							groupColumnIndices,
							ListTuple.FACTORY_METHOD
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
					}
					
					return new Aggregator(
						(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)child,
						aggregationFunctions,
						new int[0],
						ListTuple.FACTORY_METHOD
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
	private Aggregations() {
		// private access in order to ensure non-instantiability
	}
	
}
