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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.ResetableCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.functions.MetaDataFunction;
import xxl.core.functions.Switch;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.metaData.ColumnMetaData;
import xxl.core.relational.metaData.ColumnMetaDatas;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.predicates.Predicates;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node function node} in a directed acyclic graph.
 * Beside these methods, it contains constants for identifying local metadata
 * fragments inside an expression node's global metadata, methods for accessing
 * them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class Functions {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a function
	 * in the directed acyclic graph.
	 */
	public static final String EXPRESSION_TYPE = "FUNCTION";
	
	/**
	 * This constant is used to identify a function's type inside its global
	 * metadata.
	 */
	public static final String TYPE = "FUNCTION->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a
	 * function's global metadata for identifing the function's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents a CASE
		 * function in the directed acyclic graph.
		 */
		CASE
		
	}
	
	/**
	 * This constant can be used as a prefix to identify a CASE function's
	 * condition nodes inside its global metadata.
	 */
	public static final String CONDITION_PREFIX = "FUNCTION->CONDITION_";
	
	/**
	 * This constant can be used as a prefix to identify a CASE function's
	 * result nodes inside its global metadata.
	 */
	public static final String RESULT_PREFIX = "FUNCTION->RESULT_";
	
	/**
	 * An anchor placement strategy for CASE functions. The strategy simply
	 * places the specified anchor to the given CASE function's condition
	 * predicates and result expressions.
	 */
	public static final Function<Node, ?> ANCHOR_PLACEMENT_STRATEGY = new Function<Node, Object>() {
		@Override
		public Object invoke(Node operation, Node anchor) {
			Iterator<Node> nodes = Nodes.getNodes(operation, CONDITION_PREFIX);
			while (nodes.hasNext())
				Nodes.placeAnchor(nodes.next(), anchor);
			nodes = Nodes.getNodes(operation, RESULT_PREFIX);
			while (nodes.hasNext())
				Nodes.placeAnchor(nodes.next(), anchor);
			return null;
		}
	};
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the function's result.
	 */
	public static final Function<Object, ColumnMetaData> COLUMN_METADATA_FACTORY = new Function<Object, ColumnMetaData>() {
		@Override
		public ColumnMetaData invoke(Object identifier, Object expression) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)expression).getMetaData();
			Type type = (Type)globalMetaData.get(TYPE);
			switch (type) {
				case CASE: {
					return ColumnMetaDatas.getColumnMetaData(Nodes.getNode((Node)expression, RESULT_PREFIX + 0));
				} // CASE
			} // switch (type)
			throw new MetaDataException("unknown function type " + type);
		}
	};
	
	// static 'constructors'

	/**
	 * Creates a new function of the given type that accepts the specified
	 * number of arguments and processes the arguments contained by the
	 * specified iteration.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfArguments the number of arguments the returned expression
	 *        node should be able to accept.
	 * @param arguments an iteration over the expression nodes that should
	 *        be processed by the returned expression node.
	 * @return a new function of the given type that accepts the specified
	 *         number of arguments and processes the arguments contained by the
	 *         specified iteration.
	 */
	public static final Node newFunction(Type type, int numberOfArguments, Iterator<Node> arguments) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, numberOfArguments, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(TYPE, Type.class);
		
		Node argument;
		while (arguments.hasNext()) {
			if (Nodes.getType(argument = arguments.next()) != Expressions.NODE_TYPE)
				throw new IllegalArgumentException("only expressions can be used as child nodes of a function expression");
			
			expression.addChild(argument);
		}
		
		return expression;
	}
	
	/**
	 * Creates a new function of the given type that accepts the specified
	 * number of arguments and processes the given arguments.
	 * 
	 * @param type the type of the expression node that should be returned.
	 * @param numberOfArguments the number of arguments the returned expression
	 *        node should be able to accept.
	 * @param arguments the expression nodes that should be processed by the
	 *        returned expression node.
	 * @return a new function of the given type that accepts the specified
	 *         number of arguments and processes the given arguments.
	 */
	public static final Node newFunction(Type type, int numberOfArguments, Node... arguments) {
		return newFunction(type, numberOfArguments, new ArrayCursor<Node>(arguments));
	}
	
	/**
	 * Creates a new CASE function with the conditions contained by the given
	 * iteration and the according results contained by the second given
	 * iteration.
	 * 
	 * @param conditions an iteration over the conditions of the returned CASE
	 *        function node's cases.
	 * @param results an iteration over the results of the returned CASE
	 *        function node's cases.
	 * @return a new CASE function with the conditions contained by the given
	 *         iteration and the according results contained by the second
	 *         given iteration.
	 */
	public static final Node newCase(Iterator<Node> conditions, Iterator<Node> results) {
		Node expression = Expressions.newExpression(EXPRESSION_TYPE, 0, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = expression.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = expression.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = expression.getSignature();
		
		globalMetaData.add(TYPE, Type.CASE);
		globalMetaData.add(Nodes.ANCHOR_PLACEMENT_STRATEGY, ANCHOR_PLACEMENT_STRATEGY);
		
		Nodes.putNodes(
			expression,
			CONDITION_PREFIX,
			conditions,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Predicates.NODE_TYPE;
				}
			},
			true
		);
		Nodes.putNodes(
			expression,
			RESULT_PREFIX,
			results,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE;
				}
			},
			true
		);
		
		globalMetaDataFactory.put(ColumnMetaDatas.COLUMN_METADATA_TYPE, COLUMN_METADATA_FACTORY);
		
		signature.put(TYPE, Type.class);
		
		expression.updateMetaData();
		
		return expression;
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given function.
	 * 
	 * @param expression the function whose type should be returned.
	 * @return the type of the given function.
	 */
	public static final Type getType(Node expression) {
		return (Type)expression.getMetaData().get(TYPE);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a function expression into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Element> children = QueryConverter.getChildren(element, QueryConverter.EXPRESSION_ELEMENT);
			
			Type type = Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE));
			
			switch (type) {
				case CASE :
					if (children.hasNext())
						throw new MetaDataException("a CASE function must not have any sub-expressions");
					
					ArrayList<Node> conditions = new ArrayList<Node>();
					ArrayList<Node> results = new ArrayList<Node>();
					Cursor<Element> caseElements = QueryConverter.getChildren(element, QueryConverter.CASE_ELEMENT);
					Cursor<Element> elements;
					while (caseElements.hasNext()) {
						elements = QueryConverter.getChildren(caseElements.next());
						if (!elements.hasNext())
							throw new MetaDataException("a case of a CASE function must consist of a condition (predicate) and a result (expression)");
						conditions.add(queryConverter.read(elements.next()));
						if (!elements.hasNext())
							throw new MetaDataException("a case of a CASE function must consist of a condition (predicate) and a result (expression)");
						results.add(queryConverter.read(elements.next()));
						if (elements.hasNext())
							throw new MetaDataException("a case of a CASE function must consist of a condition (predicate) and a result (expression)");
					}

					return newCase(conditions.iterator(), results.iterator());
			}
			throw new MetaDataException("unknown function type " + type);
		}
	};

	/**
	 * A factory method that can be used to transform a function expression
	 * into its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node expression = (Node)args.get(4);
			
			Type type = getType(expression);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, type.toString());
			
			switch (type) {
				case CASE:
					for (Cursor<Node> conditions = Nodes.getNodes(expression, CONDITION_PREFIX), results = Nodes.getNodes(expression, RESULT_PREFIX); conditions.hasNext() || results.hasNext();) {
						Element caseElement = document.createElement(QueryConverter.CASE_ELEMENT);
						queryConverter.write(conditions.next(), document, caseElement);
						queryConverter.write(results.next(), document, caseElement);
						element.appendChild(caseElement);
					}
			}
			
			queryConverter.writeChildren(expression.getChildren(), document, element);
			
			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a function expression
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataFunction<Tuple, ? extends Object, CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			Type type = getType(node);
			switch (type) {
				case CASE: {
					// test for children
					if (children.hasNext())
						throw new MetaDataException("a " + type + " function must not have any sub-expressions");
					
					return new MetaDataFunction<Tuple, Object, CompositeMetaData<Object, Object>>(
						new Function<Tuple, Object>() {
							protected Cursor<Predicate<Tuple>> conditions = new ResetableCursor<Predicate<Tuple>>(
								new Mapper<Node, Predicate<Tuple>>(
									new Function<Node, Predicate<Tuple>>() {
										public Predicate<Tuple> invoke(Node node) {
											return (Predicate<Tuple>)queryTranslator.translate(node);
										}
									},
									Nodes.getNodes(node, CONDITION_PREFIX)
								)
							);
							protected Cursor<Function<Tuple, ? extends Object>> results = new ResetableCursor<Function<Tuple, ? extends Object>>(
								new Mapper<Node, Function<Tuple, ? extends Object>>(
									new Function<Node, Function<Tuple, ? extends Object>>() {
										public Function<Tuple, ? extends Object> invoke(Node node) {
											return (Function<Tuple, ? extends Object>)queryTranslator.translate(node);
										}
									},
									Nodes.getNodes(node, RESULT_PREFIX)
								)
							);
														
							@Override
							public Object invoke(List<? extends Tuple> tuples) {
								while (conditions.hasNext() && results.hasNext()) {
									if (conditions.next().invoke(tuples)) {
										Object result = results.next().invoke(tuples);
										conditions.reset();
										results.reset();
										return result;
									}
									results.next();
								}
								conditions.reset();
								results.reset();
								throw new IllegalStateException("undefined case");
							}
						}
					) {
						@Override
						public CompositeMetaData<Object, Object> getMetaData() {
							return node.getMetaData();
						}
					};
				} // CASE
			} // switch (type)
			throw new MetaDataException("unknown function expression type " + type);
		}
	};

	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Functions() {
		// private access in order to ensure non-instantiability
	}
	
}
