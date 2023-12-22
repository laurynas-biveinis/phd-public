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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.functions.Switch;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.RenamedColumnMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node renamed column node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an expression node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class RenamedColumns {
	
	// metadata identifierand metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a renamed
	 * column in the directed acyclic graph.
	 */
	public static final String EXPRESSION_TYPE = "RENAMED_COLUMN";
	
	/**
	 * This constant is used to identify the renamed column's name inside its
	 * global metadata.
	 */
	public static final String COLUMN_NAME = "RENAMED_COLUMN->COLUMN_NAME";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the column.
	 */
	public static final Function<Object, RenamedColumnMetaData> COLUMN_METADATA_FACTORY = new Function<Object, RenamedColumnMetaData>() {
		@Override
		public RenamedColumnMetaData invoke(Object identifier, Object expression) {
			return new RenamedColumnMetaData(ColumnMetaDatas.getColumnMetaData(((Node)expression).getChild(0)), getColumnName((Node)expression));
		}
	};
	
	// static constructors

	/**
	 * Creates a new renamed column expression renaming the given column using
	 * the specified name.
	 * 
	 * @param column the column to be renamed.
	 * @param columnName the new name of the specified column.
	 * @return a new renamed column expression renaming the given column using
	 *         the specified name.
	 */
	public static final Node newRenamedColumn(Node column, String columnName) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, 1, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		globalMetaData.add(COLUMN_NAME, columnName);
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(COLUMN_NAME, String.class);
		
		if (Nodes.getType(column) != Expressions.NODE_TYPE)
			throw new IllegalArgumentException("only expressions can be used as child nodes of a renamed column expression");
		
		expression.addChild(column);
		
		return expression;
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the column name of the given renamed column expression.
	 *  
	 * @param expression the renamed column expression whose column name should
	 *        be returned.
	 * @return the column name of the given renamed column expression.
	 */
	public static final String getColumnName(Node expression) {
		return (String)expression.getMetaData().get(COLUMN_NAME);
	}
	
	/**
	 * Returns the child index of the column represented by the given renamed
	 * column expression.
	 *  
	 * @param expression the renamed column expression whose child index should
	 *        be returned.
	 * @return the child index of the column represented by the given renamed
	 *         column expression.
	 */
	public static final int getChildIndex(Node expression) {
		Node column = expression.getChild(0);
		try {
			return Columns.getChildIndex(column);
		}
		catch (MetaDataException mde) {
			return getChildIndex(column);
		}
	}
	
	/**
	 * Returns the column index of the column represented by the given renamed
	 * column expression.
	 *  
	 * @param expression the renamed column expression whose column index
	 *        should be returned.
	 * @return the column index of the column represented by the given renamed
	 *         column expression.
	 */
	public static final int getColumnIndex(Node expression) {
		Node column = expression.getChild(0);
		try {
			return Columns.getColumnIndex(column);
		}
		catch (MetaDataException mde) {
			return getColumnIndex(column);
		}
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a renamed column expression into the node itself.
	 */
	public final static Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.EXPRESSION_ELEMENT);
			
			if (!children.hasNext())
				throw new MetaDataException("a renamed column expression must have exactly one underlying column expression");
			Node child = queryConverter.read(children.next());
			if (children.hasNext())
				throw new MetaDataException("a renamed column expression must not have more than one underlying column expression");
			
			return newRenamedColumn(
				child,
				element.getAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE)
			);
		}
	};

	/**
	 * A factory method that can be used to transform a renamed column
	 * expression into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node expression = (Node)args.get(4);
			
			element.setAttribute(QueryConverter.COLUMN_NAME_ATTRIBUTE, getColumnName(expression));
			queryConverter.writeChildren(expression.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a renamed column
	 * expression into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("a renamed column expression must have exactly one sub-expression");
			Function<Tuple, Object> child = (Function<Tuple, Object>)queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("a renamed column expression must not have more than one sub-expression");
			
			// return a metadata function providing the node's metadata and
			// simply decorating the sub-expressions metadata function
			return new MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>>(child) {
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
	private RenamedColumns() {
		// private access in order to ensure non-instantiability
	}
	
}
