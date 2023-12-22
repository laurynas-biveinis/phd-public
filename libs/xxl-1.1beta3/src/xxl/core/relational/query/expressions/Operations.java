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
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.functions.Switch;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.metaData.DecoratorColumnMetaData;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of an {@link Node operation node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an expression node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Operations {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents an operation
	 * in the directed acyclic graph.
	 */
	public static final String EXPRESSION_TYPE = "OPERATION";
	
	/**
	 * This constant is used to identify an operation's type inside its global
	 * metadata.
	 */
	public static final String TYPE = "OPERATION->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in an
	 * operation's global metadata for identifing the operation's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents an unary
		 * minus in the directed acyclic graph.
		 */
		MINUS("-"),
		
		/**
		 * This constant can be used to denote that a node represents an
		 * addition in the directed acyclic graph.
		 */
		ADDITION("+"),
		
		/**
		 * This constant can be used to denote that a node represents an
		 * subtraction in the directed acyclic graph.
		 */
		SUBTRACTION("-"),
		
		/**
		 * This constant can be used to denote that a node represents an
		 * multiplication in the directed acyclic graph.
		 */
		MULTIPLICATION("*"),
		
		/**
		 * This constant can be used to denote that a node represents an
		 * division in the directed acyclic graph.
		 */
		DIVISION("/"),
		
		/**
		 * This constant can be used to denote that a node represents an
		 * exponentiation in the directed acyclic graph.
		 */
		EXPONENTIATION("^"),
		
		/**
		 * This constant can be used to denote that a node represents an
		 * concatenation in the directed acyclic graph.
		 */
		CONCATENATION("||");
		
		/**
		 * A mnemonic for this operation type.
		 */
		protected String mnemonic;
		
		/**
		 * Creates a new operation type using the given mnemonic.
		 * 
		 * @param mnemonic the mnemonic used for this operation type.
		 */
		Type(String mnemonic) {
			this.mnemonic = mnemonic;
		}
		
		/**
		 * Returns the mnemonic representing this operation type.
		 * 
		 * @return the mnemonic representing this operation type.
		 */
		public String getMnemonic() {
			return mnemonic;
		}

	}
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the operation's result.
	 */
	public static final Function<Object, ColumnMetaData> COLUMN_METADATA_FACTORY = new Function<Object, ColumnMetaData>() {
		@Override
		public ColumnMetaData invoke(Object identifier, Object expression) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)expression).getMetaData();
			try {
				Type type = (Type)globalMetaData.get(TYPE);
				final String mnemonic = type.getMnemonic();
				switch (type) {
					case MINUS:
						ColumnMetaData columnMetaData = ColumnMetaDatas.getColumnMetaData(((Node)expression).getChild(0));
						switch (columnMetaData.getColumnType()) {
							case Types.BIT:
							case Types.TINYINT:
							case Types.SMALLINT:
							case Types.INTEGER:
							case Types.BIGINT:
							case Types.FLOAT:
							case Types.REAL:
							case Types.DOUBLE:
							case Types.NUMERIC:
							case Types.DECIMAL:
								return new DecoratorColumnMetaData(columnMetaData) {
									@Override
									public boolean isSigned() throws SQLException {
										return true;
									}
									
									@Override
									public String getColumnLabel() throws SQLException {
										return mnemonic + super.getColumnLabel();
									}
									
									@Override
									public String getColumnName() throws SQLException {
										return mnemonic + super.getColumnName();
									}
								};
							default:
								throw new MetaDataException("only numeric values can be negated");
						}
					case ADDITION:
					case SUBTRACTION:
					case MULTIPLICATION:
					case DIVISION:
					case EXPONENTIATION:
						columnMetaData = ColumnMetaDatas.getColumnMetaData(((Node)expression).getChild(0));
						ColumnMetaData secondColumnMetaData = ColumnMetaDatas.getColumnMetaData(((Node)expression).getChild(1));
						switch (columnMetaData.getColumnType()) {
							case Types.BIT:
							case Types.TINYINT:
							case Types.SMALLINT:
							case Types.INTEGER:
							case Types.BIGINT:
							case Types.FLOAT:
							case Types.REAL:
							case Types.DOUBLE:
							case Types.NUMERIC:
							case Types.DECIMAL:
								switch (secondColumnMetaData.getColumnType()) {
									case Types.BIT:
									case Types.TINYINT:
									case Types.SMALLINT:
									case Types.INTEGER:
									case Types.BIGINT:
									case Types.FLOAT:
									case Types.REAL:
									case Types.DOUBLE:
									case Types.NUMERIC:
									case Types.DECIMAL:
										return new StoredColumnMetaData(
											false,
											false,
											true,
											columnMetaData.isCurrency() || secondColumnMetaData.isCurrency(),
											columnMetaData.isNullable() == ResultSetMetaData.columnNullable || secondColumnMetaData.isNullable() == ResultSetMetaData.columnNullable ?
												ResultSetMetaData.columnNullable :
												columnMetaData.isNullable() == ResultSetMetaData.columnNoNulls && secondColumnMetaData.isNullable() == ResultSetMetaData.columnNoNulls ?
													ResultSetMetaData.columnNoNulls :
													ResultSetMetaData.columnNullableUnknown,
											true,
											columnMetaData.getColumnDisplaySize() + secondColumnMetaData.getColumnDisplaySize() + mnemonic.length(),
											columnMetaData.getColumnLabel() + mnemonic + secondColumnMetaData.getColumnLabel(),
											columnMetaData.getColumnName() + mnemonic + secondColumnMetaData.getColumnName(),
											"",
											Math.max(columnMetaData.getPrecision(), secondColumnMetaData.getPrecision()),
											Math.max(columnMetaData.getScale(), secondColumnMetaData.getScale()),
											"",
											"",
											Types.DECIMAL,
											true,
											false,
											false
										);
									default:
										throw new MetaDataException("only numeric values can be mathematically subsumed");
								}
							default:
								throw new MetaDataException("only numeric values can be mathematically subsumed");
						}
					case CONCATENATION:
						columnMetaData = ColumnMetaDatas.getColumnMetaData(((Node)expression).getChild(0));
						secondColumnMetaData = ColumnMetaDatas.getColumnMetaData(((Node)expression).getChild(1));
						if (columnMetaData.getColumnType() == Types.VARCHAR && secondColumnMetaData.getColumnType() == Types.VARCHAR)
							return new StoredColumnMetaData(
								false,
								columnMetaData.isCaseSensitive() || secondColumnMetaData.isCaseSensitive(),
								true,
								false,
								columnMetaData.isNullable() == ResultSetMetaData.columnNullable || secondColumnMetaData.isNullable() == ResultSetMetaData.columnNullable ?
									ResultSetMetaData.columnNullable :
									columnMetaData.isNullable() == ResultSetMetaData.columnNoNulls && secondColumnMetaData.isNullable() == ResultSetMetaData.columnNoNulls ?
										ResultSetMetaData.columnNoNulls :
										ResultSetMetaData.columnNullableUnknown,
								false,
								columnMetaData.getColumnDisplaySize() + secondColumnMetaData.getColumnDisplaySize() + mnemonic.length(),
								columnMetaData.getColumnLabel() + mnemonic + secondColumnMetaData.getColumnLabel(),
								columnMetaData.getColumnName() + mnemonic + secondColumnMetaData.getColumnName(),
								"",
								columnMetaData.getPrecision() + secondColumnMetaData.getPrecision(),
								0,
								"",
								"",
								Types.VARCHAR,
								true,
								false,
								false
							);
						throw new MetaDataException("only textual values can be concatenated");
					default:
						throw new MetaDataException("unknown operation type " + type);
				}
			}
			catch (SQLException sqle) {
				throw new MetaDataException("meta data cannot be constructed due to the following sql exception: " + sqle.getMessage());
			}
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new operation of the given type that accepts the specified
	 * number of subexpressions and subsumes the subexpressions contained by
	 * the specified iteration.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfSubexpressions the number of subexpressions the returned
	 *        expression node should be able to accept.
	 * @param subexpressions an iteration over the expression nodes that should
	 *        be subsumed by the returned expression node.
	 * @return a new operation of the given type that accepts the specified
	 *         number of subexpressions and subsumes the subexpressions
	 *         contained by the specified iteration.
	 */
	public static final Node newOperation(Type type, int numberOfSubexpressions, Iterator<Node> subexpressions) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, numberOfSubexpressions, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(TYPE, Type.class);
		
		Node subexpression;
		while (subexpressions.hasNext()) {
			if (Nodes.getType(subexpression = subexpressions.next()) != Expressions.NODE_TYPE)
				throw new IllegalArgumentException("only expressions can be used as child nodes of an operation expression");
			
			expression.addChild(subexpression);
		}
		
		return expression;
	}
	
	/**
	 * Creates a new operation of the given type that accepts the specified
	 * number of subexpressions and subsumes the given subexpressions.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfSubexpressions the number of subexpressions the returned
	 *        expression node should be able to accept.
	 * @param subexpressions the expression nodes that should be subsumed by
	 *        the returned expression node.
	 * @return a new operation of the given type that accepts the specified
	 *         number of subexpressions and subsumes the given subexpressions.
	 */
	public static final Node newOperation(Type type, int numberOfSubexpressions, Node... subexpressions) {
		return newOperation(type, numberOfSubexpressions, new ArrayCursor<Node>(subexpressions));
	}
	
	/**
	 * Creates a new unary minus that negates the given subexpression.
	 * 
	 * @param subexpression the expression that should be negated by the
	 *        returned expression.
	 * @return a new unary minus that negates the given subexpression.
	 */
	public static final Node newMinus(Node subexpression) {
		return newOperation(Type.MINUS, 1, subexpression);
	}
	
	/**
	 * Creates a new addition of the given subexpressions.
	 * 
	 * @param firstAddend the first subexpression to be added.
	 * @param secondAddend the second subexpression to be added.
	 * @return a new addition of the given subexpressions.
	 */
	public static final Node newAddition(Node firstAddend, Node secondAddend) {
		return newOperation(Type.ADDITION, Nodes.VARIABLE, firstAddend, secondAddend);
	}
	
	/**
	 * Creates a new subtraction of the given subexpressions.
	 * 
	 * @param minuend the minuend of the subtraction.
	 * @param subtrahend the subtrahend of the subtraction.
	 * @return a new subtraction of the given subexpressions.
	 */
	public static final Node newSubtraction(Node minuend, Node subtrahend) {
		return newOperation(Type.SUBTRACTION, Nodes.VARIABLE, minuend, subtrahend);
	}
	
	/**
	 * Creates a new multiplication of the given subexpressions.
	 * 
	 * @param multiplicand the multiplicand of the multiplication.
	 * @param multiplier the multiplier of the multiplication.
	 * @return a new multiplication of the given subexpressions.
	 */
	public static final Node newMultiplication(Node multiplicand, Node multiplier) {
		return newOperation(Type.SUBTRACTION, Nodes.VARIABLE, multiplicand, multiplier);
	}
	
	/**
	 * Creates a new division of the given subexpressions.
	 * 
	 * @param dividend the dividend of the division.
	 * @param divisor the divisor of the division.
	 * @return a new division of the given subexpressions.
	 */
	public static final Node newDivision(Node dividend, Node divisor) {
		return newOperation(Type.DIVISION, Nodes.VARIABLE, dividend, divisor);
	}
	
	/**
	 * Creates a new exponentiation of the given subexpressions.
	 * 
	 * @param base the base of the exponentiation.
	 * @param exponent the exponent of the exponentiation.
	 * @return a new exponentiation of the given subexpressions.
	 */
	public static final Node newExponentiation(Node base, Node exponent) {
		return newOperation(Type.EXPONENTIATION, Nodes.VARIABLE, base, exponent);
	}
	
	/**
	 * Creates a new concatenation of the given subexpressions.
	 * 
	 * @param firstSubexpression the first subexpression of the concatenation.
	 * @param secondSubexpression the second subexpression of the
	 *        concatenation.
	 * @return a new concatenation of the given subexpressions.
	 */
	public static final Node newConcatenation(Node firstSubexpression, Node secondSubexpression) {
		return newOperation(Type.CONCATENATION, Nodes.VARIABLE, firstSubexpression, secondSubexpression);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given operation.
	 * 
	 * @param expression the operation whose type should be returned.
	 * @return the type of the given operation.
	 */
	public static final Type getType(Node expression) {
		return (Type)expression.getMetaData().get(TYPE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * an operation expression into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element);
			
			Type type = Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE));
			
			switch (type) {
				case MINUS:
					if (!children.hasNext())
						throw new MetaDataException("an operation of type " + type + " must have exactly one argument expression");
					Node child = queryConverter.read(children.next());
					if (children.hasNext())
						throw new MetaDataException("an operation of type " + type + " must not have more than one argument expression");
					
					return newOperation(type, 1, child);
				case ADDITION:
				case SUBTRACTION:
				case MULTIPLICATION:
				case DIVISION:
				case EXPONENTIATION:
				case CONCATENATION:
					if (!children.hasNext())
						throw new MetaDataException("an operation of type " + type + " must have exactly two argument expressions");
					Node firstChild = queryConverter.read(children.next());
					if (!children.hasNext())
						throw new MetaDataException("an operation of type " + type + " must have exactly two argument expressions");
					Node secondChild = queryConverter.read(children.next());
					if (children.hasNext())
						throw new MetaDataException("an operation of type " + type + " must not have more than two argument expressions");
					
					return newOperation(type, 2, firstChild, secondChild);
			}
			throw new MetaDataException("unknown operation type " + type);
		}
	};

	/**
	 * A factory method that can be used to transform an operation expression
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node expression = (Node)args.get(4);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, getType(expression).toString());
			queryConverter.writeChildren(expression.getChildren(), document, element);
			
			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate an operation expression
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>>() {
		public MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			Type type = getType(node);
			switch (type) {
				case MINUS: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly one sub-expression");
					Function<Tuple, ? extends Number> child = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " operation must not have more than one sub-expression");
					
					return new MetaDataFunction<Tuple, Double, CompositeMetaData<Object, Object>>(
						xxl.core.functions.Functions.newNullSensitiveFunction(xxl.core.functions.Functions.minus(), null).<Tuple>compose(child)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // MINUS
				case ADDITION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> firstChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> secondChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " operation must not have more than two sub-expressions");
					
					return new MetaDataFunction<Tuple, Double, CompositeMetaData<Object, Object>>(
						xxl.core.functions.Functions.newNullSensitiveFunction(xxl.core.functions.Functions.add(), null).<Tuple>compose(firstChild, secondChild)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // ADDITION
				case SUBTRACTION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> firstChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> secondChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " operation must not have more than two sub-expressions");
					
					return new MetaDataFunction<Tuple, Double, CompositeMetaData<Object, Object>>(
						xxl.core.functions.Functions.newNullSensitiveFunction(xxl.core.functions.Functions.sub(), null).<Tuple>compose(firstChild, secondChild)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // SUBTRACTION
				case MULTIPLICATION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> firstChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> secondChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " operation must not have more than two sub-expressions");
					
					return new MetaDataFunction<Tuple, Double, CompositeMetaData<Object, Object>>(
						xxl.core.functions.Functions.newNullSensitiveFunction(xxl.core.functions.Functions.mult(), null).<Tuple>compose(firstChild, secondChild)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // MULTIPLICATION
				case DIVISION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> firstChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> secondChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " operation must not have more than two sub-expressions");
					
					return new MetaDataFunction<Tuple, Double, CompositeMetaData<Object, Object>>(
						xxl.core.functions.Functions.newNullSensitiveFunction(xxl.core.functions.Functions.div(), null).<Tuple>compose(firstChild, secondChild)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // DIVISION
				case EXPONENTIATION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> firstChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Number> secondChild = (Function<Tuple, ? extends Number>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " operation must not have more than two sub-expressions");
					
					return new MetaDataFunction<Tuple, Double, CompositeMetaData<Object, Object>>(
						xxl.core.functions.Functions.newNullSensitiveFunction(xxl.core.functions.Functions.exp(), null).<Tuple>compose(firstChild, secondChild)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // EXPONENTIATION
				case CONCATENATION: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Object> firstChild = (Function<Tuple, ? extends Object>)queryTranslator.translate(children.next());
					if (!children.hasNext())
						throw new MetaDataException("a " + type + " operation must have exactly two sub-expressions");
					Function<Tuple, ? extends Object> secondChild = (Function<Tuple, ? extends Object>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a " + type + " operation must not have more than two sub-expressions");
					
					return new MetaDataFunction<Tuple, String, CompositeMetaData<Object, Object>>(
						xxl.core.functions.Functions.newNullSensitiveFunction(xxl.core.functions.Functions.concat(), null).<Tuple>compose(firstChild, secondChild)
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // CONCATENATION
			} // switch (type)
			throw new MetaDataException("unknown operation expression type " + type);
		}
	};

	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Operations() {
		// private access in order to ensure non-instantiability
	}
	
}
