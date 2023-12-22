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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node table node} in a directed acyclic graph. Beside
 * these methods, it contains constants for identifying local metadata
 * fragments inside an expression node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Tables {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a table in
	 * the directed acyclic graph.
	 */
	public static final String EXPRESSION_TYPE = "TABLE";
	
	/**
	 * This constant is used to identify the table's schema name inside its
	 * global metadata.
	 */
	public static final String SCHEMA_NAME = "TABLE->SCHEMA_NAME";
	
	/**
	 * This constant is used to identify the column's name inside its global
	 * metadata.
	 */
	public static final String TABLE_NAME = "TABLE->TABLE_NAME";
	
	// static 'constructors'
	
	/**
	 * Creates a new table expression representing the table named
	 * <code>schemaName</code>.<code>tableName</code>.
	 * 
	 * @param schemaName the name of the schema providing the table's table.
	 * @param tableName the name of the table.
	 * @return a new column expression representing the table named
	 *         <code>schemaName</code>.<code>tableName</code>.
	 */
	public static final Node newTable(String schemaName, String tableName) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		if (schemaName != null)
			globalMetaData.add(SCHEMA_NAME, schemaName);
		if (tableName != null)
			globalMetaData.add(TABLE_NAME, tableName);
		
		signature.put(SCHEMA_NAME, String.class);
		signature.put(TABLE_NAME, String.class);
		
		expression.updateMetaData();
		
		return expression;
	}
	
	/**
	 * Creates a new table expression representing the table named
	 * <code>tableName</code>.
	 * 
	 * @param tableName the name of the table.
	 * @return a new column expression representing the table named
	 *         <code>tableName</code>.
	 */
	public static final Node newTable(String tableName) {
		return newTable(null, tableName);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the schema name of the given table expression.
	 *  
	 * @param expression the table expression whose schema name should be
	 *        returned.
	 * @return the schema name of the given table expression.
	 */
	public static final String getSchemaName(Node expression) {
		return (String)expression.getMetaData().get(SCHEMA_NAME);
	}
	
	/**
	 * Returns the table name of the given table expression.
	 *  
	 * @param expression the table expression whose table name should be
	 *        returned.
	 * @return the table name of the given table expression.
	 */
	public static final String getTableName(Node expression) {
		return (String)expression.getMetaData().get(TABLE_NAME);
	}
	
	/**
	 * Returns the canonical name of the given table expression, i.e, the name
	 * given by the table expression's schema name and table name.
	 *  
	 * @param expression the table expression whose canonical name should be
	 *        returned.
	 * @return the canonical name of the given table expression.
	 */
	public static final String getCanonicalName(Node expression) {
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		return globalMetaData.contains(SCHEMA_NAME) ?
			(String)globalMetaData.get(SCHEMA_NAME) + '.' + (String)globalMetaData.get(TABLE_NAME) :
			(String)globalMetaData.get(TABLE_NAME);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a table expression into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element);
			
			if (children.hasNext())
				throw new MetaDataException("a table expression must not have any sub-expressions");
			
			return element.hasAttribute(QueryConverter.SCHEMA_NAME_ATTRIBUTE) ?
				newTable(
					element.getAttribute(QueryConverter.SCHEMA_NAME_ATTRIBUTE),
					element.getAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE)
				) :
				newTable(
					element.getAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE)
				);
		}
	};

	/**
	 * A factory method that can be used to transform a table expression into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(3);
			Node expression = (Node)args.get(4);
			
			if (expression.getMetaData().contains(SCHEMA_NAME)) {
				element.setAttribute(QueryConverter.SCHEMA_NAME_ATTRIBUTE, getSchemaName(expression));
				element.setAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE, getTableName(expression));
			}
			else
				element.setAttribute(QueryConverter.TABLE_NAME_ATTRIBUTE, getTableName(expression));

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a table expression into
	 * an appropriate implementation.
	 */
	public static final Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			throw new IllegalStateException("table expressions are internally used for identifying relational source, hence the occurence of thi exception is a signal of a missuse of a table expression");
		}
	};

	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Tables() {
		// private access in order to ensure non-instantiability
	}
	
}
