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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.AbstractMetaDataCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.ResetableCursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.And;
import xxl.core.predicates.MetaDataPredicate;
import xxl.core.predicates.Not;
import xxl.core.predicates.Or;
import xxl.core.predicates.Predicate;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.query.operators.Operators;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node <i>logical operation</i> node} in a directed
 * acyclic graph. Beside these methods, it contains constants for identifying
 * local metadata fragments inside a predicate node's global metadata, methods
 * for accessing them and local metadata factories for updating them.
 * 
 * @see Node
 */
public class LogicalOperations {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a logical
	 * operation in the directed acyclic graph.
	 */
	public static final String PREDICATE_TYPE = "LOGICAL_OPERATION";
	
	/**
	 * This constant is used to identify a logical operation's type inside its
	 * global metadata.
	 */
	public static final String TYPE = "LOGICAL_OPERATION->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a logical
	 * operation's global metadata for identifing the operation's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents a logical
		 * EXISTS in the directed acyclic graph.
		 */
		EXISTS,
		
		/**
		 * This constant can be used to denote that a node represents a logical
		 * IN in the directed acyclic graph.
		 */
		IN,
		
		/**
		 * This constant can be used to denote that a node represents a logical
		 * NOT in the directed acyclic graph.
		 */
		NOT,
		
		/**
		 * This constant can be used to denote that a node represents a logical
		 * AND in the directed acyclic graph.
		 */
		AND,
		
		/**
		 * This constant can be used to denote that a node represents a logical
		 * OR in the directed acyclic graph.
		 */
		OR
	}
	
	/**
	 * This constant is used to identify an EXISTS or IN operation's set inside
	 * its global metadata.
	 */
	public static final String SET = "LOGICAL_OPERATION->SET";
	
	/**
	 * This constant is used to identify an IN operation's member inside its
	 * global metadata.
	 */
	public static final String MEMBER = "LOGICAL_OPERATION->MEMBER";
	
	/**
	 * An anchor placement strategy for IN predicates. The strategy simply
	 * places the specified anchor to the given IN predicate's member
	 * expression.
	 */
	public static final Function<Node, ?> ANCHOR_PLACEMENT_STRATEGY = new Function<Node, Object>() {
		@Override
		public Object invoke(Node operation, Node anchor) {
			Nodes.placeAnchor(Nodes.getNode(operation, MEMBER), anchor);
			return null;
		}
	};
	
	// static 'constructors'
	
	/**
	 * Creates a new logical operation of the given type that accepts the
	 * specified number of subpredicates and subsumes the subpredicates
	 * contained by the specified iteration.
	 * 
	 * @param type the type of the predicate node that should be returned.
	 * @param numberOfSubpredicates the number of subpredicates the returned
	 *        predicate node should be able to accept.
	 * @param subpredicates an iteration over the predicate nodes that should
	 *        be subsumed by the returned predicate node.
	 * @return a new logical operation of the given type that accepts the
	 *         specified number of subpredicates and subsumes the subpredicates
	 *         contained by the specified iteration.
	 */
	public static final Node newLogicalOperation(Type type, int numberOfSubpredicates, Iterator<? extends Node> subpredicates) {
		Node predicate = Predicates.newPredicate(PREDICATE_TYPE, numberOfSubpredicates, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = predicate.getMetaData();
		Map<Object, Class<?>> signature = predicate.getSignature();
		
		globalMetaData.add(TYPE, type);
		
		signature.put(TYPE, Type.class);
		
		Node subpredicate;
		while (subpredicates.hasNext()) {
			if (Nodes.getType(subpredicate = subpredicates.next()) != Predicates.NODE_TYPE)
				throw new IllegalArgumentException("only predicates can be used as child nodes of a logical operation predicate");
			
			predicate.addChild(subpredicate);
		}
		
		return predicate;
	}
	
	/**
	 * Creates a new logical operation of the given type that accepts the
	 * specified number of subpredicates and subsumes the given subpredicates.
	 * 
	 * @param type the type of the predicate node that should be returned.
	 * @param numberOfSubpredicates the number of subpredicates the returned
	 *        predicate node should be able to accept.
	 * @param subpredicates the predicate nodes that should be subsumed by the
	 *        returned predicate node.
	 * @return a new logical operation of the given type that accepts the
	 *         specified number of subpredicates and subsumes the given
	 *         subpredicates.
	 */
	public static final Node newLogicalOperation(Type type, int numberOfSubpredicates, Node... subpredicates) {
		return newLogicalOperation(type, numberOfSubpredicates, new ArrayCursor<Node>(subpredicates));
	}
	
	/**
	 * Creates a new logical EXISTS that test whether the specified query
	 * contains any rows.
	 * 
	 * @param operator the query that should be tested for having any results.
	 * @return a new logical EXISTS that test whether the specified subquery
	 *         contains any rows.
	 */
	public static final Node newExists(Node operator) {
		Node predicate = newLogicalOperation(Type.EXISTS, 0);
		CompositeMetaData<Object, Object> globalMetaData = predicate.getMetaData();
		Map<Object, Class<?>> signature = predicate.getSignature();
		
		if (Nodes.getType(operator) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as sets of an exists predicate");
		
		globalMetaData.add(SET, operator);
		
		signature.put(SET, Node.class);
		
		predicate.updateMetaData();
		
		return predicate;
	}
	
	/**
	 * Creates a new logical IN that test whether the specified query contains
	 * the given single-column row.
	 * 
	 * @param expression the single-column row the specified subquery should be
	 *        searched for.
	 * @param operator the query that should be tested for containing the given
	 *        single-column row.
	 * @return a new logical IN that test whether the specified query contains
	 *         the given single-column row.
	 */
	public static final Node newIn(Node expression, Node operator) {
		Node predicate = newLogicalOperation(Type.IN, 0);
		CompositeMetaData<Object, Object> globalMetaData = predicate.getMetaData();
		Map<Object, Class<?>> signature = predicate.getSignature();
		
		if (Nodes.getType(expression) != Expressions.NODE_TYPE)
			throw new IllegalArgumentException("only expressions can be used as expression of an in predicate");
		if (Nodes.getType(operator) != Operators.NODE_TYPE)
			throw new IllegalArgumentException("only operators can be used as sets of an in predicate");
		
		globalMetaData.add(SET, operator);
		globalMetaData.add(MEMBER, expression);
		globalMetaData.add(Nodes.ANCHOR_PLACEMENT_STRATEGY, ANCHOR_PLACEMENT_STRATEGY);
		
		signature.put(SET, Node.class);
		signature.put(MEMBER, Node.class);
		
		predicate.updateMetaData();
		
		return predicate;
	}
	
	/**
	 * Creates a new logical NOT that inverts the result of the given
	 * subpredicate.
	 * 
	 * @param subpredicate the predicate which result should be inverted by the
	 *        returned predicate.
	 * @return a new logical NOT that inverts the result of the given
	 *         subpredicate.
	 */
	public static final Node newNot(Node subpredicate) {
		return newLogicalOperation(Type.NOT, 1, subpredicate);
	}
	
	/**
	 * Creates a new logical AND that subsumes the subpredicates contained by
	 * the given iteration, i.e., creates the conjunction of them.
	 * 
	 * @param subpredicates an iteration over the predicate nodes that should
	 *        be subsumed.
	 * @return a new logical AND that subsumes the subpredicates contained by
	 *         the given iteration, i.e., creates the conjunction of them.
	 */
	public static final Node newAnd(Iterator<? extends Node> subpredicates) {
		return newLogicalOperation(Type.AND, Nodes.VARIABLE, subpredicates);
	}
	
	/**
	 * Creates a new logical AND that subsumes the given subpredicates, i.e.,
	 * creates the conjunction of them.
	 * 
	 * @param subpredicates an array storing the predicate nodes that should be
	 *        subsumed.
	 * @return a new logical AND that subsumes the given subpredicates, i.e.,
	 *         creates the conjunction of them.
	 */
	public static final Node newAnd(Node... subpredicates) {
		return newLogicalOperation(Type.AND, Nodes.VARIABLE, subpredicates);
	}
	
	/**
	 * Creates a new logical OR that subsumes the subpredicates contained by
	 * the given iteration, i.e., creates the disjunction of them.
	 * 
	 * @param subpredicates an iteration over the predicate nodes that should
	 *        be subsumed.
	 * @return a new logical OR that subsumes the subpredicates contained by
	 *         the given iteration, i.e., creates the conjunction of them.
	 */
	public static final Node newOr(Iterator<? extends Node> subpredicates) {
		return newLogicalOperation(Type.OR, Nodes.VARIABLE, subpredicates);
	}
	
	/**
	 * Creates a new logical OR that subsumes the given subpredicates, i.e.,
	 * creates the disjunction of them.
	 * 
	 * @param subpredicates an array storing the predicate nodes that should be
	 *        subsumed.
	 * @return a new logical OR that subsumes the given subpredicates, i.e.,
	 *         creates the disjunction of them.
	 */
	public static final Node newOr(Node... subpredicates) {
		return newLogicalOperation(Type.OR, Nodes.VARIABLE, subpredicates);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given logicaloperation.
	 * 
	 * @param predicate the logical operation whose type should be returned.
	 * @return the type of the given logical operation.
	 */
	public static final Type getType(Node predicate) {
		return (Type)predicate.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the set of the given EXISTS or IN operation.
	 * 
	 * @param predicate the EXISTS or IN operation whose set should be
	 *        returned.
	 * @return the set of the given EXISTS or IN operation.
	 */
	public static final Node getSet(Node predicate) {
		return (Node)predicate.getMetaData().get(SET);
	}
	
	/**
	 * Returns the member of the given IN operation.
	 * 
	 * @param predicate the IN operation whose member should be returned.
	 * @return the member of the given IN operation.
	 */
	public static final Node getMember(Node predicate) {
		return (Node)predicate.getMetaData().get(MEMBER);
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a logical operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			return newLogicalOperation(
				Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE)),
				Nodes.VARIABLE,
				queryConverter.readChildren(element, QueryConverter.PREDICATE_ELEMENT)
			);
		}
	};

	/**
	 * A factory method that can be used to transform a logical operator into
	 * its XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node predicate = (Node)args.get(4);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, getType(predicate).toString());
			queryConverter.writeChildren(predicate.getChildren(), document, element);

			return null;
		}
	};
	
	// query translation
	
	/**
	 * A factory method that can be used to translate a logical (boolean)
	 * operator into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("a logical (boolean) operator must have at least one sub-predicate");
			Predicate<? super Tuple> predicate = null;
			
			// get the type of the logical (boolean) operator
			switch (getType(node)) {
				case EXISTS: {
					// test for children
					if (children.hasNext())
						throw new MetaDataException("a logical (boolean) EXISTS operator must not have any sub-predicates");
					
					class ExistsPredicate extends Predicate<Tuple> {
						
						protected MetaDataProvider<CompositeMetaData<Object, Object>> subquery = queryTranslator.translate(getSet(node));
						protected boolean result;
						
						@Override
						public boolean invoke(Tuple tuple) {
							result = false;
							
							switch ((Operators.Mode)subquery.getMetaData().get(Operators.MODE)) {
								case ACTIVE: {
									Source<TemporalObject<Tuple>> source = (Source<TemporalObject<Tuple>>)subquery;
									AbstractSink<TemporalObject<Tuple>> sink = new AbstractSink<TemporalObject<Tuple>>() {
										@Override
										public void processObject(TemporalObject<Tuple> o, int sourceID) throws IllegalArgumentException {
											result = true;
											synchronized (ExistsPredicate.this) {
												ExistsPredicate.this.notify();
											}
										}
										
										@Override
										protected void updateDoneStatus(int sourceID) {
											super.updateDoneStatus(sourceID);
											synchronized (ExistsPredicate.this) {
												ExistsPredicate.this.notify();
											}
										}
									};
									Pipes.connect(source, sink, xxl.core.pipes.queryGraph.Node.DEFAULT_ID);
									if (!((Source<TemporalObject<Tuple>>)subquery).isOpened())
										sink.openAllSources();
									
									try {
										synchronized (ExistsPredicate.this) {
											ExistsPredicate.this.wait();
										}
									}
									catch (InterruptedException ie) {
										// interrupted by another thread
									}
									
									Pipes.disconnect(source, sink, xxl.core.pipes.queryGraph.Node.DEFAULT_ID);
									
									return result;
								}
								case PASSIVE: {
									MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = (MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)subquery;
									if (!cursor.supportsReset())
										subquery = cursor = new AbstractMetaDataCursor<Tuple, CompositeMetaData<Object, Object>>(
											new ResetableCursor<Tuple>(cursor)
										) {
											@Override
											public CompositeMetaData<Object, Object> getMetaData() {
												return null;
											}
										};
									
									result = cursor.hasNext();
									
									cursor.reset();
									
									return result;
								}
							}
							throw new MetaDataException("unknown mode type ");
						}
					}
					
					predicate = new ExistsPredicate();
					break;
				} // EXISTS
				case IN: {
					// test for children
					if (children.hasNext())
						throw new MetaDataException("a logical (boolean) IN operator must not have any sub-predicates");
					
					class InPredicate extends Predicate<Tuple> {
						
						protected Function<Tuple, Object> member = (Function<Tuple, Object>)queryTranslator.translate(getMember(node));
						protected MetaDataProvider<CompositeMetaData<Object, Object>> subquery = queryTranslator.translate(getSet(node));
						protected boolean result;
						
						@Override
						public boolean invoke(Tuple tuple) {
							final Object object = member.invoke(tuple);
							result = false;
							switch ((Operators.Mode)subquery.getMetaData().get(Operators.MODE)) {
								case ACTIVE: {
									Source<TemporalObject<Tuple>> source = (Source<TemporalObject<Tuple>>)subquery;
									AbstractSink<TemporalObject<Tuple>> sink = new AbstractSink<TemporalObject<Tuple>>() {
										@Override
										public void processObject(TemporalObject<Tuple> o, int sourceID) throws IllegalArgumentException {
											if (object.equals(o.getObject().getObject(1))) {
												result = true;
												synchronized (InPredicate.this) {
													InPredicate.this.notify();
												}
											}
										}
										
										@Override
										protected void updateDoneStatus(int sourceID) {
											super.updateDoneStatus(sourceID);
											synchronized (InPredicate.this) {
												InPredicate.this.notify();
											}
										}
									};
									Pipes.connect(source, sink, xxl.core.pipes.queryGraph.Node.DEFAULT_ID);
									if (!((Source<TemporalObject<Tuple>>)subquery).isOpened())
										sink.openAllSources();
									
									try {
										synchronized (InPredicate.this) {
											InPredicate.this.wait();
										}
									}
									catch (InterruptedException ie) {
										// interrupted by another thread
									}
									
									Pipes.disconnect(source, sink, xxl.core.pipes.queryGraph.Node.DEFAULT_ID);
									
									return result;
								} // ACTIVE
								case PASSIVE: {
									MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> cursor = (MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)subquery;
									if (!cursor.supportsReset())
										subquery = cursor = new AbstractMetaDataCursor<Tuple, CompositeMetaData<Object, Object>>(
											new ResetableCursor<Tuple>(cursor)
										) {
											@Override
											public CompositeMetaData<Object, Object> getMetaData() {
												return null;
											}
										};
									
									while (!result && cursor.hasNext())
										if (object.equals(cursor.next().getObject(1)))
											result = true;
									
									cursor.reset();
									
									return result;
								} // PASSIVE
							} // switch ((Operators.Mode)subquery.getMetaData().get(Operators.MODE))
							throw new MetaDataException("unknown mode type ");
						}
					}
					
					predicate = new InPredicate();
					break;
				} // IN
				case NOT: {
					// test for children
					if (!children.hasNext())
						throw new MetaDataException("a logical (boolean) NOT must have exactly one sub-predicate");
					predicate = (Predicate<Tuple>)queryTranslator.translate(children.next());
					if (children.hasNext())
						throw new MetaDataException("a logical (boolean) NOT must not have more than one sub-predicate");
					predicate = new Not<Tuple>(predicate);
					break;
				} // NOT
				case AND: {
					// test for children
					if (children.hasNext())
						predicate = (Predicate<Tuple>)queryTranslator.translate(children.next());
					else
						predicate = xxl.core.predicates.Predicates.TRUE;
					while (children.hasNext())
						predicate = new And<Tuple>(predicate, (Predicate<Tuple>)queryTranslator.translate(children.next()));
					break;
				} // AND
				case OR: {
					// test for children
					if (children.hasNext())
						predicate = (Predicate<Tuple>)queryTranslator.translate(children.next());
					else
						predicate = xxl.core.predicates.Predicates.FALSE;
					while (children.hasNext())
						predicate = new Or<Tuple>(predicate, (Predicate<Tuple>)queryTranslator.translate(children.next()));
				} // OR
			} // switch (getType(node))
			return new MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>(predicate) {
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
	private LogicalOperations() {
		// private access in order to ensure non-instantiability
	}
	
}
