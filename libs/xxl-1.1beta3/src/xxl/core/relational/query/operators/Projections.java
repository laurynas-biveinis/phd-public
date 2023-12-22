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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.mappers.TemporalMapper;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.cursors.Projection;
import xxl.core.relational.metaData.ProjectedResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Columns;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.Arrays;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node projection node} in a directed acyclic graph.
 * Beside the methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Projections {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a projection
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "PROJECTION";
	
	/**
	 * This constant is used to generate unique identifiers for the projected
	 * columns inside the projection operator's global metadata.
	 */
	public static final String PROJECTED_COLUMN_PREFIX = "PROJECTION->PROJECTED_COLUMN_";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of a projection operator.
	 */
	public static final Function<Object, ProjectedResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, ProjectedResultSetMetaData>() {
		@Override
		public ProjectedResultSetMetaData invoke(Object identifier, final Object operator) {
			int[] columnIndices = new int[Nodes.countNodes((Node)operator, PROJECTED_COLUMN_PREFIX)];
			
			Iterator<Node> projectedColumns = Nodes.getNodes((Node)operator, PROJECTED_COLUMN_PREFIX);
			for (int i = 0; projectedColumns.hasNext(); i++)
				columnIndices[i] = Columns.getColumnIndex(projectedColumns.next());
			
			return new ProjectedResultSetMetaData(ResultSetMetaDatas.getResultSetMetaData(((Node)operator).getChild(0)), columnIndices);
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new projection operator that projects the columns given by the
	 * specified iteration. The projection operator gets its relational data
	 * from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        projection operator.
	 * @param columns an iteration over the columns to be projected.
	 * @return a new projection operator that projects the columns given by the
	 *         specified iteration.
	 */
	public static final Node newProjection(Node input, Iterator<? extends Node> columns) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 1, Nodes.VARIABLE);
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		
		Nodes.putNodes(
			operator,
			PROJECTED_COLUMN_PREFIX,
			columns,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == Columns.EXPRESSION_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of a projection operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	/**
	 * Creates a new projection operator that projects the specified columns.
	 * The projection operator gets its relational data from the given operator
	 * node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        projection operator.
	 * @param columns the columns to be projected.
	 * @return a new projection operator that projects the specified columns.
	 */
	public static final Node newProjection(Node input, Node... columns) {
		return newProjection(input, new ArrayCursor<Node>(columns));
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a projection operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("a projection operator must have exactly one input operator");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("a projection operator must not have more than one input operators");
			
			return newProjection(
				child,
				queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
			);
		}
	};

	/**
	 * A factory method that can be used to transform a projection operator
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			queryConverter.writeChildren(Nodes.getNodes(operator, PROJECTED_COLUMN_PREFIX), document, element);
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical projection
	 * operator into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("a projection operator must have exactly one input operator");
			MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("a projection operator must not have more than one input operators");
			
			// get the columns to be projected
			Cursor<Node> projectedColumns = Nodes.getNodes(node, PROJECTED_COLUMN_PREFIX);
			final ArrayList<Integer> columnIndices = new ArrayList<Integer>();
			while (projectedColumns.hasNext())
				columnIndices.add(Columns.getColumnIndex(projectedColumns.next()));
			
			// get the projection's mode
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = new TemporalMapper<Tuple, Tuple>(
						(Source<TemporalObject<Tuple>>)child,
						new Function<TemporalObject<Tuple>, TemporalObject<Tuple>>() {
							protected xxl.core.functions.Projection<Object> projection = new xxl.core.functions.Projection<Object>(Arrays.decrementIntArray(Arrays.newIntArray(columnIndices.size(), columnIndices.iterator()), 1));
							
							@Override
							public TemporalObject<Tuple> invoke(TemporalObject<Tuple> temporalTuple) {
								return new TemporalObject<Tuple>(
									new ArrayTuple(projection.invoke(temporalTuple.getObject().toArray())),
									temporalTuple.getTimeInterval()
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
					return new Projection(
						(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)child,
						ArrayTuple.FACTORY_METHOD,
						Arrays.newIntArray(columnIndices.size(), columnIndices.iterator())
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
			throw new MetaDataException("unknown projection operator mode " + mode);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Projections() {
		// private access in order to ensure non-instantiability
	}
	
}
