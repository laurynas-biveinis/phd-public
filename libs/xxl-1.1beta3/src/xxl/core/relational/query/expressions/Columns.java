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

package xxl.core.relational.query.expressions;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.functions.Switch;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.ResultSetMetaDataColumnMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node column node} in a directed acyclic graph. Beside
 * these methods, it contains constants for identifying local metadata
 * fragments inside an expression node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Columns {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a column in
	 * the directed acyclic graph.
	 */
	public static final String EXPRESSION_TYPE = "COLUMN";
	
	/**
	 * This constant is used to identify the column's schema name inside its
	 * global metadata.
	 */
	public static final String SCHEMA_NAME = "COLUMN->SCHEMA_NAME";
	
	/**
	 * This constant is used to identify the column's table name inside its
	 * global metadata.
	 */
	public static final String TABLE_NAME = "COLUMN->TABLE_NAME";
	
	/**
	 * This constant is used to identify the column's name inside its global
	 * metadata.
	 */
	public static final String COLUMN_NAME = "COLUMN->COLUMN_NAME";
	
	/**
	 * This constant is used to identify the index of the anchor's child that
	 * contains the column inside the its global metadata.
	 */
	public static final String CHILD_INDEX = "COLUMN->ANCHOR->CHILD_INDEX";
	
	/**
	 * This constant is used to identify the index of the anchor's column
	 * inside its global metadata.
	 */
	public static final String COLUMN_INDEX = "COLUMN->ANCHOR->COLUMN_INDEX";
	
	/**
	 * An anchor placement strategy for column expressions. The strategy simply
	 * addes the anchor as a local metadata fragment to the column expression's
	 * global metadata.
	 */
	public static final Function<Node, ?> ANCHOR_PLACEMENT_STRATEGY = new Function<Node, Object>() {
		@Override
		public Object invoke(Node column, Node anchor) {
			column.getMetaData().put(Nodes.ANCHOR, anchor);
			column.updateMetaData();
			return null;
		}
	};
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to update the metadata
	 * fragment holding a child index. It returns the metadata fragment stored
	 * in the node's global metadata when it exists or creates a new metadata
	 * fragment based on the available child nodes and the column name
	 * otherwise.
	 */
	public static final Function<Object, Integer> COLUMN_INDEX_METADATA_FACTORY = new Function<Object, Integer>() {
		@Override
		public Integer invoke(Object identifier, Object expression) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)expression).getMetaData();
			Iterator<Node> children = ((Node)globalMetaData.get(Nodes.ANCHOR)).getChildren();
			Function<ResultSetMetaData, Integer> columnIndexFactory = getColumnIndexFactory((Node)expression);
			
			for (int childIndex = 0, columnIndex; children.hasNext(); childIndex++)
				if ((columnIndex = columnIndexFactory.invoke(ResultSetMetaDatas.getResultSetMetaData(children.next()))) > 0) {
					globalMetaData.put(CHILD_INDEX, childIndex);
					return columnIndex;
				}
			
			throw new MetaDataException("local metadata fragment identified by " + identifier + " cannot be created because no available column fits suggested column name");
		}
	};
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the column.
	 */
	public static final Function<Object, ResultSetMetaDataColumnMetaData> COLUMN_METADATA_FACTORY = new Function<Object, ResultSetMetaDataColumnMetaData>() {
		@Override
		public ResultSetMetaDataColumnMetaData invoke(Object identifier, Object expression) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)expression).getMetaData();
			return new ResultSetMetaDataColumnMetaData(
				ResultSetMetaDatas.getResultSetMetaData(
					((Node)globalMetaData.get(Nodes.ANCHOR)).getChild(
						(Integer)globalMetaData.get(CHILD_INDEX)
					)
				),
				(Integer)globalMetaData.get(COLUMN_INDEX)
			);
		}
	};

	// static 'constructors'
	
	/**
	 * Creates a new column expression representing the
	 * <code>columnIndex</code>-th column of the <code>childIndex</code>-th
	 * child of the expression's anchor node.
	 * 
	 * @param childIndex the index of the expression's anchor node's child
	 *        containing the column.
	 * @param columnIndex the index of the column.
	 * @return a new column expression representing the
	 *         <code>columnIndex</code>-th column of the
	 *         <code>childIndex</code>-th child of the expression's anchor
	 *         node.
	 */
	public static final Node newColumn(int childIndex, int columnIndex) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		globalMetaData.add(CHILD_INDEX, childIndex);
		globalMetaData.add(COLUMN_INDEX, columnIndex);
		globalMetaData.add(Nodes.ANCHOR_PLACEMENT_STRATEGY, ANCHOR_PLACEMENT_STRATEGY);
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(CHILD_INDEX, Integer.class);
		signature.put(COLUMN_INDEX, Integer.class);
		
		expression.updateMetaData();
		
		return expression;
	}
	
	/**
	 * Creates a new column expression representing the column named
	 * <code>schemaName</code>.<code>tableName</code>.<code>columnName</code>
	 * of the expression's anchor node's children.
	 * 
	 * @param schemaName the name of the schema providing the column's table.
	 * @param tableName the name of the table containing the column.
	 * @param columnName the name of the column.
	 * @return a new column expression representing the column named
	 *         <code>schemaName</code>.<code>tableName</code>.<code>columnName</code>
	 *         of the expression's anchor node's children.
	 */
	public static final Node newColumn(String schemaName, String tableName, String columnName) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		if (schemaName != null)
			globalMetaData.add(SCHEMA_NAME, schemaName);
		if (tableName != null)
			globalMetaData.add(TABLE_NAME, tableName);
		if (columnName != null)
			globalMetaData.add(COLUMN_NAME, columnName);
		globalMetaData.add(Nodes.ANCHOR_PLACEMENT_STRATEGY, ANCHOR_PLACEMENT_STRATEGY);
		globalMetaDataFactory.put(COLUMN_INDEX, COLUMN_INDEX_METADATA_FACTORY);
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(CHILD_INDEX, Integer.class);
		signature.put(COLUMN_INDEX, Integer.class);
		
		expression.updateMetaData();
		
		return expression;
	}
	
	/**
	 * Creates a new column expression representing the column named
	 * <code>tableName</code>.<code>columnName</code> of the expression's
	 * anchor node's children.
	 * 
	 * @param tableName the name of the table containing the column.
	 * @param columnName the name of the column.
	 * @return a new column expression representing the column named
	 *         <code>tableName</code>.<code>columnName</code> of the
	 *         expression's anchor node's children.
	 */
	public static final Node newColumn(String tableName, String columnName) {
		return newColumn(null, tableName, columnName);
	}
	
	/**
	 * Creates a new column expression representing the column named
	 * <code>columnName</code> of the expression's anchor node's children.
	 * 
	 * @param columnName the name of the column.
	 * @return a new column expression representing the column named
	 *         <code>columnName</code> of the expression's anchor node's
	 *         children.
	 */
	public static final Node newColumn(String columnName) {
		return newColumn(null, null, columnName);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the schema name of the given column expression.
	 *  
	 * @param expression the column expression whose schema name should be
	 *        returned.
	 * @return the schema name of the given column expression.
	 */
	public static final String getSchemaName(Node expression) {
		return (String)expression.getMetaData().get(SCHEMA_NAME);
	}
	
	/**
	 * Returns the table name of the given column expression.
	 *  
	 * @param expression the column expression whose table name should be
	 *        returned.
	 * @return the table name of the given column expression.
	 */
	public static final String getTableName(Node expression) {
		return (String)expression.getMetaData().get(TABLE_NAME);
	}
	
	/**
	 * Returns the column name of the given column expression.
	 *  
	 * @param expression the column expression whose column name should be
	 *        returned.
	 * @return the column name of the given column expression.
	 */
	public static final String getColumnName(Node expression) {
		return (String)expression.getMetaData().get(COLUMN_NAME);
	}
	
	/**
	 * Returns the index of the anchor node' child containing the column
	 * represented by the given column expression.
	 *  
	 * @param expression the column expression whose child index should be
	 *        returned.
	 * @return the index of the anchor node' child containing the column
	 *         represented by the given column expression.
	 */
	public static final int getChildIndex(Node expression) {
		return (Integer)expression.getMetaData().get(CHILD_INDEX);
	}
	
	/**
	 * Returns the index of the column represented by the given column
	 * expression inside the relational metadata of the anchor node's child.
	 *  
	 * @param expression the column expression whose column index should be
	 *        returned.
	 * @return the index of the column represented by the given column
	 *         expression inside the relational metadata of the anchor node's
	 *         child.
	 */
	public static final int getColumnIndex(Node expression) {
		return (Integer)expression.getMetaData().get(COLUMN_INDEX);
	}
	
	/**
	 * Returns a factory method for coumn indices using the schema, table and
	 * column name of the column represented by the given column expression
	 * inside the relational metadata of the anchor node's child.
	 *  
	 * @param expression the column expression whose column index should be
	 *        resolved by the return factory method.
	 * @return a factory method for coumn indices using the schema, table and
	 *         column name of the column represented by the given column
	 *         expression inside the relational metadata of the anchor node's
	 *         child.
	 */
	public static final Function<ResultSetMetaData, Integer> getColumnIndexFactory(Node expression) {
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		
		if (globalMetaData.contains(SCHEMA_NAME)) {
			final String schemaName = (String)globalMetaData.get(SCHEMA_NAME);
			final String tableName = (String)globalMetaData.get(TABLE_NAME);
			final String columnName = (String)globalMetaData.get(COLUMN_NAME);
			
			return new Function<ResultSetMetaData, Integer>() {
				@Override
				public Integer invoke(ResultSetMetaData resultSetMetaData) {
					try {
						return ResultSetMetaDatas.getColumnIndex(resultSetMetaData, schemaName, tableName, columnName);
					}
					catch (SQLException sqle) {
						throw new MetaDataException("sql exception occured during meta data construction: \'" + sqle.getMessage() + "\'");
					}
				}
			};
		}
		
		if (globalMetaData.contains(TABLE_NAME)) {
			final String tableName = (String)globalMetaData.get(TABLE_NAME);
			final String columnName = (String)globalMetaData.get(COLUMN_NAME);
			
			return new Function<ResultSetMetaData, Integer>() {
				@Override
				public Integer invoke(ResultSetMetaData resultSetMetaData) {
					try {
						return ResultSetMetaDatas.getColumnIndex(resultSetMetaData, tableName, columnName);
					}
					catch (SQLException sqle) {
						throw new MetaDataException("sql exception occured during meta data construction: \'" + sqle.getMessage() + "\'");
					}
				}
			};
		}
		
		final String columnName = (String)globalMetaData.get(COLUMN_NAME);
		
		return new Function<ResultSetMetaData, Integer>() {
			@Override
			public Integer invoke(ResultSetMetaData resultSetMetaData) {
				try {
					return ResultSetMetaDatas.getColumnIndex(resultSetMetaData, columnName);
				}
				catch (SQLException sqle) {
					throw new MetaDataException("sql exception occured during meta data construction: \'" + sqle.getMessage() + "\'");
				}
			}
		};
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a column expression into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element);
			
			if (children.hasNext())
				throw new MetaDataException("a column expression must not have any sub-expressions");
			
			return element.hasAttribute(QueryConverter.SCHEMA_NAME_ATTRIBUTE) ?
				newColumn(
					element.getAttribute(QueryConverter.SCHEMA_NAME_ATTRIBUTE),
					element.getAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE),
					element.getAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE)
				) :
				element.hasAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE) ?
					newColumn(
						element.getAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE),
						element.getAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE)
					) :
					newColumn(
						element.getAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE)
					);
		}
	};

	/**
	 * A factory method that can be used to transform a column expression into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(3);
			Node expression = (Node)args.get(4);
			
			CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
			
			if (globalMetaData.contains(SCHEMA_NAME)) {
				element.setAttribute(QueryConverter.SCHEMA_NAME_ATTRIBUTE, getSchemaName(expression));
				element.setAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE, getTableName(expression));
				element.setAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE, getColumnName(expression));
			}
			else
				if (globalMetaData.contains(TABLE_NAME)) {
					element.setAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE, getTableName(expression));
					element.setAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE, getColumnName(expression));
				}
				else
					try {
						element.setAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE, ColumnMetaDatas.getColumnMetaData(expression).getColumnName());
					}
					catch (SQLException sqle) {
						throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
					}

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a column expression into
	 * an appropriate implementation.
	 */
	public static final Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (children.hasNext())
				throw new MetaDataException("a column expression must not have any sub-expressions");
			
			// return a metadata function providing the node's metadata and
			// returning the columnIndex'th column of the childIndex'th tuple
			return new MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>>(
				new Function<Tuple, Object>() {
					protected int childIndex = getChildIndex(node);
					protected int columnIndex = getColumnIndex(node);
					
					@Override
					public Object invoke(List<? extends Tuple> tuples) {
						return tuples.get(childIndex).getObject(columnIndex);
					}
				}
			) {
				@Override
				public CompositeMetaData<Object, Object> getMetaData() {
					return node.getMetaData();
				}
			};
		}
	};

	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Columns() {
		// private access in order to ensure non-instantiability
	}
	
}
