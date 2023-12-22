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
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.identities.IdentityPipe;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.cursors.Renaming;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.RenamedResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Columns;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.query.expressions.RenamedColumns;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node renaming node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an operator node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Renamings {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a renaming
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "RENAMING";
	
	/**
	 * This constant is used to identify the renamed table name inside the
	 * renaming operator's global metadata.
	 */
	public static final String TABLE_NAME = "RENAMING->TABLE_NAME";
	
	/**
	 * This constant is used to generate unique identifiers for the renamed
	 * columns inside the renaming operator's global metadata.
	 */
	public static final String RENAMED_COLUMN_PREFIX = "RENAMING->RENAMED_COLUMN_";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of a renaming operator.
	 */
	public static final Function<Object, RenamedResultSetMetaData> RESULTSET_METADATA_FACTORY = new Function<Object, RenamedResultSetMetaData>() {
		@Override
		public RenamedResultSetMetaData invoke(Object identifier, final Object operator) {
			int[] columnIndices = new int[Nodes.countNodes((Node)operator, RENAMED_COLUMN_PREFIX)];
			String[] columnNames = new String[columnIndices.length];
			
			Iterator<Node> renamedColumns = Nodes.getNodes((Node)operator, RENAMED_COLUMN_PREFIX);
			Node renamedColumn;
			for (int i = 0; renamedColumns.hasNext(); i++) {
				renamedColumn = renamedColumns.next();
				columnIndices[i] = Columns.getColumnIndex(renamedColumn.getChild(0));
				columnNames[i] = RenamedColumns.getColumnName(renamedColumn);
			}
			
			return new RenamedResultSetMetaData(ResultSetMetaDatas.getResultSetMetaData(((Node)operator).getChild(0)), columnIndices, columnNames);
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new renaming operator using the new table name and the
	 * renamings given by the specified iteration. The renaming operator gets
	 * its relational data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        renaming operator.
	 * @param tableName the new table name of the returned renaming operator.
	 * @param renamings an iteration over the renamings applied by the returned
	 *        renaming operator.
	 * @return a new renaming operator using the new table name and the
	 *         renamings given by the specified iteration.
	 */
	public static final Node newRenaming(Node input, String tableName, Iterator<? extends Node> renamings) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, 1, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();

		if (tableName != null) {
			globalMetaData.add(TABLE_NAME, tableName);
			
			signature.put(TABLE_NAME, String.class);
		}
		
		Nodes.putNodes(
			operator,
			RENAMED_COLUMN_PREFIX,
			renamings,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == RenamedColumns.EXPRESSION_TYPE && Nodes.getType(node = node.getChild(0)) == Expressions.NODE_TYPE && Expressions.getType(node) == Columns.EXPRESSION_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, RESULTSET_METADATA_FACTORY);
		
		if (Nodes.getType(input) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as child nodes of a renaming operator");
		
		operator.addChild(input);
		
		return operator;
	}
	
	/**
	 * Creates a new renaming operator using the renamings given by the
	 * specified iteration. The renaming operator gets its relational data from
	 * the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        renaming operator.
	 * @param renamings an iteration over the renamings applied by the returned
	 *        renaming operator.
	 * @return a new renaming operator using the renamings given by the
	 *         specified iteration.
	 */
	public static final Node newRenaming(Node input, Iterator<? extends Node> renamings) {
		return newRenaming(input, null, renamings);
	}
	
	/**
	 * Creates a new renaming operator using the new table name and the
	 * specified renamings. The renaming operator gets its relational data from
	 * the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        renaming operator.
	 * @param tableName the new table name of the returned renaming operator.
	 * @param renamings an iteration over the renamings applied by the returned
	 *        renaming operator.
	 * @return a new renaming operator using the new table name and the
	 *         renamings given by the specified iteration.
	 */
	public static final Node newRenaming(Node input, String tableName, Node... renamings) {
		return newRenaming(input, tableName, new ArrayCursor<Node>(renamings));
	}
	
	/**
	 * Creates a new renaming operator using the specified renamings. The
	 * renaming operator gets its relational data from the given operator node.
	 * 
	 * @param input the operator node providing the relational data for the
	 *        renaming operator.
	 * @param renamings the renamings applied by the returned renaming
	 *        operator.
	 * @return a new renaming operator using the specified renamings.
	 */
	public static final Node newRenaming(Node input, Node... renamings) {
		return newRenaming(input, null, renamings);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the renamed table name of given renaming operator.
	 * 
	 * @param operator the  renaming operator whose renamed table name should
	 *        be returned.
	 * @return the renamed table name of given renaming operator.
	 */
	public static final String getTableName(Node operator) {
		return (String)operator.getMetaData().get(TABLE_NAME);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a renaming operator into the node itself.
	 */
	public static Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.OPERATOR_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("a renaming operator must have exactly one input operator");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("a renaming operator must not have more than one input operator");
			
			return element.hasAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE) ?
				newRenaming(
					child,
					element.getAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE),
					queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
				) :
				newRenaming(
					child,
					queryConverter.readChildren(element, QueryConverter.EXPRESSION_ELEMENT)
				);
		}
	};

	/**
	 * A factory method that can be used to transform a renaming operator into
	 * its XML representation.
	 */
	public static Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			if (operator.getMetaData().contains(TABLE_NAME))
				element.setAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE, getTableName(operator));
			queryConverter.writeChildren(Nodes.getNodes(operator, RENAMED_COLUMN_PREFIX), document, element);
			queryConverter.writeChildren(operator.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical renaming
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
				throw new MetaDataException("a renaming operator must have exactly one input operator");
			MetaDataProvider<CompositeMetaData<Object, Object>> child = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("a renaming operator must not have more than one input operators");
			
			// get the columns to be renamed
			Cursor<Node> renamedColumns = Nodes.getNodes(node, RENAMED_COLUMN_PREFIX);
			ArrayList<Integer> columnIndices = new ArrayList<Integer>();
			ArrayList<String> columnNames = new ArrayList<String>();
			try {
				while (renamedColumns.hasNext()) {
					columnIndices.add(RenamedColumns.getColumnIndex(renamedColumns.peek()));
					columnNames.add(ColumnMetaDatas.getColumnMetaData(renamedColumns.next()).getColumnName());
				}
			}
			catch (SQLException sqle) {
				throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
			}
			
			// get the renaming's mode
			Operators.Mode mode = Operators.getMode(node);
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = new IdentityPipe<TemporalObject<Tuple>>(
						(Source<TemporalObject<Tuple>>)child
					);
					
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					// create a passive implementation for the logical operator
					return new Renaming(
						(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)child,
						xxl.core.util.Arrays.newIntArray(columnIndices.size(), columnIndices.iterator()),
						columnNames.toArray(new String[columnNames.size()])
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
			throw new MetaDataException("unknown renaming operator mode " + mode);
		}
	};
		
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Renamings() {
		// private access in order to ensure non-instantiability
	}
	
}
