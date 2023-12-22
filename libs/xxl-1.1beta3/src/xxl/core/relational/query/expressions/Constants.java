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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.functions.Switch;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.Types;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node constant node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an expression node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Constants {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a constant in
	 * the directed acyclic graph.
	 */
	public static final String EXPRESSION_TYPE = "CONSTANT";
	
	/**
	 * This constant is used to identify a constant node's value inside its
	 * global metadata.
	 */
	public static final String VALUE = "CONSTANT->VALUE";
	
	/**
	 * This constant is used internally to denote that a constant represents a
	 * <code>null</code> value.
	 */
	protected static final Object NULL = new Object() {
		@Override
		public String toString() {
			return "null";
		}
	};
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the column.
	 */
	public static final Function<Object, ColumnMetaData> COLUMN_METADATA_FACTORY = new Function<Object, ColumnMetaData>() {
		@Override
		public ColumnMetaData invoke(Object identifier, Object expression) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)expression).getMetaData();
			
			Object value = globalMetaData.get(VALUE);
			return value == NULL ?
				new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 11, "NULL column", "NULL_COLUMN", "", 0, 0, "", "", java.sql.Types.NULL, true, false, false) :
				Types.getColumnMetaData(Types.getJavaTypeCode(value.getClass().getName()), value);
		}
	};

	// static 'constructors'
	
	/**
	 * Creates a new constant expression representing the given object
	 * <code>value</code>.
	 * 
	 * @param value the object represented by this constant expression.
	 * @return a new constant expression representing the given object
	 *         <code>value</code>.
	 */
	public static final Node newConstant(Object value) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		globalMetaData.add(VALUE, value == null ? NULL : value);
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(VALUE, Object.class);
		
		expression.updateMetaData();
		
		return expression;
	}
	
	// metadata fragment accessors
	
	/**
	 * Creates a new constant expression representing <code>NULL</code>.
	 * 
	 * @return a new constant expression representing <code>NULL</code>.
	 */
	public static final Node newNull() {
		return newConstant(NULL);
	}
	
	/**
	 * Returns the value of the given constant expression.
	 *  
	 * @param expression the constant expression whose value should be
	 *        returned.
	 * @return the value of the given constant expression.
	 */
	public static final Object getValue(Node expression) {
		Object value = expression.getMetaData().get(VALUE);
		return value == NULL ? null : value;
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a constant expression into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element);
			
			if (children.hasNext())
				throw new MetaDataException("a constant expression must not have any sub-expressions");
			
			String type = element.getAttribute(QueryConverter.TYPE_ATTRIBUTE);
			
			if (type.equals(QueryConverter.NULL_VALUE))
				return newNull();
			
			String stringRepresentation = element.getAttribute(QueryConverter.VALUE_ATTRIBUTE);
			Object value;
			
			switch (Types.getJavaTypeCode(type)) {
				case Types.BOOLEAN:
					value = Boolean.valueOf(stringRepresentation);
					break;
				case Types.BYTE:
					value = Byte.valueOf(stringRepresentation);
					break;
				case Types.SHORT:
					value = Short.valueOf(stringRepresentation);
					break;
				case Types.INTEGER:
					value = Integer.valueOf(stringRepresentation);
					break;
				case Types.LONG:
					value = Long.valueOf(stringRepresentation);
					break;
				case Types.FLOAT:
					value = Float.valueOf(stringRepresentation);
					break;
				case Types.DOUBLE:
					value = Double.valueOf(stringRepresentation);
					break;
				case Types.BIG_DECIMAL:
					value = new BigDecimal(stringRepresentation);
					break;
				case Types.CHARACTER:
					if (stringRepresentation.length() != 1)
						throw new IllegalArgumentException("the given string \"" + stringRepresentation + "\" does not represent a character");
					value = stringRepresentation.charAt(0);
					break;
				case Types.STRING:
					value = stringRepresentation;
					break;
				case Types.DATE:
					value = Date.valueOf(stringRepresentation);
					break;
				case Types.TIME:
					value = Time.valueOf(stringRepresentation);
					break;
				case Types.TIMESTAMP:
					value = Timestamp.valueOf(stringRepresentation);
					break;
				case Types.BYTE_ARRAY:
					try {
						value = stringRepresentation.getBytes("ISO-LATIN-1");
					}
					catch (UnsupportedEncodingException uee) {
						throw new WrappingRuntimeException(uee);
					}
					break;
				case Types.UNKNOWN:
				case Types.CLOB :
				case Types.BLOB :
				case Types.ARRAY :
				case Types.STRUCT :
				case Types.REF :
					try {
						ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(stringRepresentation.getBytes("ISO-LATIN-1")));
						value = ois.readObject();
						ois.close();
					}
					catch (ClassNotFoundException cnfe) {
						throw new WrappingRuntimeException(cnfe);
					}
					catch (IOException ioe) {
						throw new WrappingRuntimeException(ioe);
					}
					break;
				default:
					throw new MetaDataException("unknown java type code");
			}

			return newConstant(value);
		}
	};

	/**
	 * A factory method that can be used to transform a constant expression
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@SuppressWarnings("fallthrough")
		@Override
		public Node invoke(List<? extends Object> args) {
			Element element = (Element)args.get(3);
			Node expression = (Node)args.get(4);
			
			try {
				ColumnMetaData columnMetaData = ColumnMetaDatas.getColumnMetaData(expression);
				
				if (columnMetaData.getColumnType() == java.sql.Types.NULL)
					element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, QueryConverter.NULL_VALUE);
				else {
					String type = columnMetaData.getColumnClassName();
					Object value = getValue(expression);
					String stringRepresentation;
					
					switch (Types.getJavaTypeCode(type)) {
						case Types.BOOLEAN :
						case Types.BYTE : 
						case Types.SHORT :
						case Types.INTEGER :
						case Types.LONG :
						case Types.FLOAT :
						case Types.DOUBLE :
						case Types.BIG_DECIMAL :
						case Types.CHARACTER :
						case Types.STRING :
						case Types.DATE :
						case Types.TIME :
						case Types.TIMESTAMP :
							stringRepresentation = value.toString();
							break;
						case Types.UNKNOWN :
						case Types.CLOB :
						case Types.BLOB :
						case Types.ARRAY :
						case Types.STRUCT :
						case Types.REF :
							try {
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								ObjectOutputStream oos = new ObjectOutputStream(baos);
								oos.writeObject(value);
								value = baos.toByteArray();
								oos.close();
							}
							catch (IOException ioe) {
								throw new WrappingRuntimeException(ioe);
							}
						case Types.BYTE_ARRAY :
							try {
								stringRepresentation = new String((byte[])value, "ISO-LATIN-1");
							}
							catch (UnsupportedEncodingException uee) {
								throw new WrappingRuntimeException(uee);
							}
							break;
						default :
							throw new MetaDataException("unknown java type code");
					}
					
					element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, type);
					element.setAttribute(QueryConverter.VALUE_ATTRIBUTE, stringRepresentation);
				}
			}
			catch (SQLException sqle) {
				throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
			}

			return null;
		}
	};
	
	// query translation

	/**
	 * A factory method that can be used to translate a constant expression
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (children.hasNext())
				throw new MetaDataException("a constant expression must not have any sub-expressions");
			
			// return a metadata function providing the node's metadata and
			// returning the constant value
			return new MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>>(
					new Function<Tuple, Object>() {
						protected Object value = getValue(node);
						
						@Override
						public Object invoke(List<? extends Tuple> tuples) {
							return value;
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
	private Constants() {
		// private access in order to ensure non-instantiability
	}
	
}
