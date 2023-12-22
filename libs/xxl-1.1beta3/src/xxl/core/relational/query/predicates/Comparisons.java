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

package xxl.core.relational.query.predicates;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.comparators.ComparableComparator;
import xxl.core.comparators.Comparators;
import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.predicates.FeaturePredicate;
import xxl.core.predicates.MetaDataPredicate;
import xxl.core.predicates.MultiFeaturePredicate;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.Types;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node comparison predicate node} in a directed acyclic
 * graph. Beside this methods, it contains constants for identifying local
 * metadata fragments inside a predicate node's global metadata, methods for
 * accessing them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Comparisons {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a comparison
	 * predicate in the directed acyclic graph.
	 */
	public static final String PREDICATE_TYPE = "COMPARISON";
	
	/**
	 * This constant is used to identify a comparison predicate's type inside
	 * its global metadata.
	 */
	public static final String TYPE = "COMPARISON->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a
	 * comparison's global metadata for identifing the comparison's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a comparison predicate
		 * tests if the first argument of the comparison is <b>unequal to</b>
		 * the other one.
		 */
		UNEQUAL("!="),
		
		/**
		 * This constant can be used to denote that a comparison predicate
		 * tests if the first argument of the comparison is <b>less than</b>
		 * the other one.
		 */
		LESS("<"),
	
		/**
		 * This constant can be used to denote that a comparison predicate
		 * tests if the first argument of the comparison is <b>less than or
		 * equal to</b> the other one.
		 */
		LESS_EQUAL("<="),
	
		/**
		 * This constant can be used to denote that a comparison predicate
		 * tests if the first argument of the comparison is <b>equal to</b> the
		 * other one.
		 */
		EQUAL("=="),
	
		/**
		 * This constant can be used to denote that a comparison predicate
		 * tests if the first argument of the comparison is <b>greater than or
		 * equal to</b> the other one.
		 */
		GREATER_EQUAL(">="),
	
		/**
		 * This constant can be used to denote that a comparison predicate
		 * tests if the first argument of the comparison is <b>greater than</b>
		 * the other one.
		 */
		GREATER(">");
		
		/**
		 * A mnemonic for this comparison type.
		 */
		protected String mnemonic;
		
		/**
		 * Creates a new comparison type using the given mnemonic.
		 * 
		 * @param mnemonic the mnemonic used for this comparison type.
		 */
		Type(String mnemonic) {
			this.mnemonic = mnemonic;
		}
		
		/**
		 * Returns the mnemonic representing this comparison type.
		 * 
		 * @return the mnemonic representing this comparison type.
		 */
		public String getMnemonic() {
			return mnemonic;
		}
	}
	
	/**
	 * This constant can be used as a prefix to identify the first argument's
	 * local metadata inside the comparison predicate's global metadata.
	 */
	public static final String FIRST_ARGUMENT = "COMPARISON->FIRST_ARGUMENT";
	
	/**
	 * This constant can be used as a prefix to identify the second argument's
	 * local metadata inside the comparison predicate's global metadata.
	 */
	public static final String SECOND_ARGUMENT = "COMPARISON->SECOND_ARGUMENT";
	
	/**
	 * An anchor placement strategy for comparison predicates. The strategy
	 * simply places the specified anchor to the given comparison predicate's
	 * argument expressions.
	 */
	public static final Function<Node, ?> ANCHOR_PLACEMENT_STRATEGY = new Function<Node, Object>() {
		@Override
		public Object invoke(Node comparison, Node anchor) {
			Nodes.placeAnchor(Nodes.getNode(comparison, FIRST_ARGUMENT), anchor);
			Nodes.placeAnchor(Nodes.getNode(comparison, SECOND_ARGUMENT), anchor);
			return null;
		}
	};
	
	// static 'constructors'
	
	/**
	 * Creates a new comparison predicate of the specified type that compares
	 * the two given expressions.
	 * 
	 * @param type the type of the comparison predicate that should be
	 *        returned.
	 * @param firstArgument the first argument to be compared.
	 * @param secondArgument the second argument to be compared.
	 * @return a new comparison predicate of the specified type that compares
	 *         the two given expressions.
	 */
	public static final Node newComparison(Type type, Node firstArgument, Node secondArgument) {
		Node predicate = Predicates.newPredicate(PREDICATE_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = predicate.getMetaData();
		Map<Object, Class<?>> signature = predicate.getSignature();
		
		if (Nodes.getType(firstArgument) != Expressions.NODE_TYPE || Nodes.getType(secondArgument) != Expressions.NODE_TYPE)
			throw new IllegalArgumentException("only expressions can be used as arguments of a comparison predicate");
		
		globalMetaData.add(TYPE, type);
		globalMetaData.add(FIRST_ARGUMENT, firstArgument);
		globalMetaData.add(SECOND_ARGUMENT, secondArgument);
		globalMetaData.add(Nodes.ANCHOR_PLACEMENT_STRATEGY, ANCHOR_PLACEMENT_STRATEGY);
		
		signature.put(TYPE, Type.class);
		signature.put(FIRST_ARGUMENT, Node.class);
		signature.put(SECOND_ARGUMENT, Node.class);
		
		predicate.updateMetaData();
		
		return predicate;
	}
	
	/**
	 * Returns a new comparison predicate that tests the first given expression
	 * for being unequal to the second one.
	 * 
	 * @param firstArgument the first argument to be compared.
	 * @param secondArgument the second argument to be compared.
	 * @return a new comparison predicate that tests the first given expression
	 *         for being unequal to the second one.
	 */
	public static final Node newUnequalComparison(Node firstArgument, Node secondArgument) {
		return newComparison(Type.UNEQUAL, firstArgument, secondArgument);
	}
	
	/**
	 * Returns a new comparison predicate that tests the first given expression
	 * for being less than the second one.
	 * 
	 * @param firstArgument the first argument to be compared.
	 * @param secondArgument the second argument to be compared.
	 * @return a new comparison predicate that tests the first given expression
	 *         for being less than the second one.
	 */
	public static final Node newLessComparison(Node firstArgument, Node secondArgument) {
		return newComparison(Type.LESS, firstArgument, secondArgument);
	}
	
	/**
	 * Returns a new comparison predicate that tests the first given expression
	 * for being less than or equal to the second one.
	 * 
	 * @param firstArgument the first argument to be compared.
	 * @param secondArgument the second argument to be compared.
	 * @return a new comparison predicate that tests the first given expression
	 *         for being less than or equal to the second one.
	 */
	public static final Node newLessEqualComparison(Node firstArgument, Node secondArgument) {
		return newComparison(Type.LESS_EQUAL, firstArgument, secondArgument);
	}
	
	/**
	 * Returns a new comparison predicate that tests the first given expression
	 * for being equal to the second one.
	 * 
	 * @param firstArgument the first argument to be compared.
	 * @param secondArgument the second argument to be compared.
	 * @return a new comparison predicate that tests the first given expression
	 *         for being equal to the second one.
	 */
	public static final Node newEqualComparison(Node firstArgument, Node secondArgument) {
		return newComparison(Type.EQUAL, firstArgument, secondArgument);
	}
	
	/**
	 * Returns a new comparison predicate that tests the first given expression
	 * for being greater than or equal to the second one.
	 * 
	 * @param firstArgument the first argument to be compared.
	 * @param secondArgument the second argument to be compared.
	 * @return a new comparison predicate that tests the first given expression
	 *         for being greater than or equal to the second one.
	 */
	public static final Node newGreaterEqualComparison(Node firstArgument, Node secondArgument) {
		return newComparison(Type.GREATER_EQUAL, firstArgument, secondArgument);
	}
	
	/**
	 * Returns a new comparison predicate that tests the first given expression
	 * for being greater than the second one.
	 * 
	 * @param firstArgument the first argument to be compared.
	 * @param secondArgument the second argument to be compared.
	 * @return a new comparison predicate that tests the first given expression
	 *         for being greater than the second one.
	 */
	public static final Node newGreaterComparison(Node firstArgument, Node secondArgument) {
		return newComparison(Type.GREATER, firstArgument, secondArgument);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given comparison predicate.
	 * 
	 * @param predicate the comparison predicate whose type should be returned.
	 * @return the type of the given comparison predicate.
	 */
	public static final Type getType(Node predicate) {
		return (Type)predicate.getMetaData().get(TYPE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a comparison predicate into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.PREDICATE_ELEMENT);
			
			if (children.hasNext())
				throw new MetaDataException("a comparison predicate must not have any sub-predicates");
			
			Cursor<Element> expressions = QueryConverter.getChildren(element, QueryConverter.EXPRESSION_ELEMENT);
			
			if (!expressions.hasNext())
				throw new MetaDataException("a comparison predicate must have exactly two argument expressions");
			Node firstArgument = queryConverter.read(expressions.next());
			if (!expressions.hasNext())
				throw new MetaDataException("a comparison predicate must have exactly two argument expressions");
			Node secondArgument = queryConverter.read(expressions.next());
			if (expressions.hasNext())
				throw new MetaDataException("a selection operator must not have more than two argument expressions");
			
			return newComparison(Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE)), firstArgument, secondArgument);
		}
	};

	/**
	 * A factory method that can be used to transform a comparison predicate
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node predicate = (Node)args.get(4);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, getType(predicate).toString());
			queryConverter.write(Nodes.getNode(predicate, FIRST_ARGUMENT), document, element);
			queryConverter.write(Nodes.getNode(predicate, SECOND_ARGUMENT), document, element);

			return null;
		}
	};

	// query translation
	
	/**
	 * A factory method that can be used to translate a logical comparison
	 * predicate into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (children.hasNext())
				throw new MetaDataException("a comparison predicate must not have any sub-predicates");
			
			// get the type of the comparisons and its arguments
			Type type = getType(node);
			MetaDataProvider<CompositeMetaData<Object, Object>> firstArgument = queryTranslator.translate(Nodes.getNode(node, FIRST_ARGUMENT));
			ColumnMetaData firstArgumentMetaData = (ColumnMetaData)firstArgument.getMetaData().get(ColumnMetaDatas.COLUMN_METADATA_TYPE);
			MetaDataProvider<CompositeMetaData<Object, Object>> secondArgument = queryTranslator.translate(Nodes.getNode(node, SECOND_ARGUMENT));
			ColumnMetaData secondArgumentMetaData = (ColumnMetaData)secondArgument.getMetaData().get(ColumnMetaDatas.COLUMN_METADATA_TYPE);
			
			try {
				// test if the argument expressions are comparable
				if (firstArgumentMetaData.getColumnType() == java.sql.Types.NULL || secondArgumentMetaData.getColumnType() == java.sql.Types.NULL) {
					if (type != Type.EQUAL && type != Type.UNEQUAL)
						throw new MetaDataException("an argument can only be compared for being equal or unequal to NULL");
				}
				else
					if (!Types.areComparable(Types.getJavaTypeCode(firstArgumentMetaData.getColumnClassName()), Types.getJavaTypeCode(secondArgumentMetaData.getColumnClassName())))
						throw new MetaDataException("the comparison predicate's arguments are not comparable");
				
				// create the internally used comparison predicate
				Predicate<Tuple> comparisonPredicate;
				switch (Types.getJavaTypeCode(firstArgumentMetaData.getColumnClassName())) {
					case Types.BYTE:
					case Types.SHORT:
					case Types.INTEGER:
					case Types.LONG:
					case Types.FLOAT:
					case Types.BIG_DECIMAL: {
						comparisonPredicate = new MultiFeaturePredicate<Tuple, Number>(
							xxl.core.predicates.Predicates.predicate(
								type.getMnemonic(),
								Comparators.newNullSensitiveComparator(
									new Comparator<Number>() {
										public int compare(Number n1, Number n2) {
											return Double.compare(n1.doubleValue(), n2.doubleValue());
										}
									},
									true
								)
							),
							(MetaDataFunction<Tuple, ? extends Number, CompositeMetaData<Object, Object>>)firstArgument,
							(MetaDataFunction<Tuple, ? extends Number, CompositeMetaData<Object, Object>>)secondArgument
						);
						break;
					} // Types.BYTE, Types.SHORT, Types.INTEGER, Types.LONG, Types.FLOAT, Types.BIG_DECIMAL
					case Types.CHARACTER:
					case Types.STRING: {
						comparisonPredicate = new MultiFeaturePredicate<Tuple, Object>(
							new FeaturePredicate<Object, String>(
								xxl.core.predicates.Predicates.predicate(
									type.getMnemonic(),
									Comparators.newNullSensitiveComparator(
										ComparableComparator.CASE_INSENSITIVE_STRING_COMPARATOR
									)
								),
								new Function<Object, String>() {
									public String invoke(Object object) {
										return String.valueOf(object);
									}
								}
							),
							(MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>>)firstArgument,
							(MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>>)secondArgument
						);
						break;
					} // TYpes.CHARACTER, Types.STRING
					case Types.BOOLEAN: {
						comparisonPredicate = new MultiFeaturePredicate<Tuple, Boolean>(
							xxl.core.predicates.Predicates.predicate(
								type.getMnemonic(),
								Comparators.newNullSensitiveComparator(
									ComparableComparator.BOOLEAN_COMPARATOR
								)
							),
							(MetaDataFunction<Tuple, Boolean, CompositeMetaData<Object, Object>>)firstArgument,
							(MetaDataFunction<Tuple, Boolean, CompositeMetaData<Object, Object>>)secondArgument
						);
						break;
					} // Types.BOOLEAN
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP: {
						comparisonPredicate = new MultiFeaturePredicate<Tuple, Date>(
							xxl.core.predicates.Predicates.predicate(
								type.getMnemonic(),
								Comparators.newNullSensitiveComparator(
									ComparableComparator.DATE_COMPARATOR
								)
							),
							(MetaDataFunction<Tuple, Date, CompositeMetaData<Object, Object>>)firstArgument,
							(MetaDataFunction<Tuple, Date, CompositeMetaData<Object, Object>>)secondArgument
						);
						break;
					} // Types.DATE, Types.TIME, Types.TIMESTAMP
					case Types.BYTE_ARRAY: {
						comparisonPredicate = new MultiFeaturePredicate<Tuple, byte[]>(
							xxl.core.predicates.Predicates.predicate(
								type.getMnemonic(),
								Comparators.newNullSensitiveComparator(
									new Comparator<byte[]>() {
										public int compare(byte[] bytes1, byte[] bytes2) {
											int result = bytes1.length - bytes2.length;
											for (int i = 0; result == 0 && i < bytes1.length; i++)
												result = bytes1[i] - bytes2[i];
											return result;
										}
									}
								)
							),
							(MetaDataFunction<Tuple, byte[], CompositeMetaData<Object, Object>>)firstArgument,
							(MetaDataFunction<Tuple, byte[], CompositeMetaData<Object, Object>>)secondArgument
						);
						break;
					} // Types.BYTE_ARRAY
					default: {
						throw new MetaDataException("incomparable Java type " + firstArgumentMetaData.getColumnClassName());
					} // default
				} // switch (Types.getJavaTypeCode(firstArgumentMetaData.getColumnClassName()))
				
				// create a metadata predicate doing the comparison
				return new MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>(comparisonPredicate) {
					@Override
					public CompositeMetaData<Object, Object> getMetaData() {
						return node.getMetaData();
					}
				};
			}
			catch (SQLException sqle) {
				throw new MetaDataException("the metadata cannot be accessed properly because of the following SQL exception: " + sqle.getMessage());
			}
		}
	};

	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Comparisons() {
		// private access in order to ensure non-instantiability
	}
	
}
