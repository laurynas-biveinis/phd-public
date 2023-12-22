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
import java.util.Map;
import java.util.NoSuchElementException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.cursors.filters.Filter;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.Enumerator;
import xxl.core.cursors.sources.SingleObjectCursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.And;
import xxl.core.predicates.FeaturePredicate;
import xxl.core.predicates.MetaDataPredicate;
import xxl.core.predicates.Predicate;
import xxl.core.relational.JoinUtils;
import xxl.core.relational.QueryConverter;
import xxl.core.relational.QueryTranslator;
import xxl.core.relational.cursors.NestedLoopsJoin;
import xxl.core.relational.metaData.AppendedResultSetMetaData;
import xxl.core.relational.metaData.MergedResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.metaData.UnifiedResultSetMetaData;
import xxl.core.relational.query.Node;
import xxl.core.relational.query.Nodes;
import xxl.core.relational.query.expressions.Columns;
import xxl.core.relational.query.expressions.Expressions;
import xxl.core.relational.query.predicates.Comparisons;
import xxl.core.relational.query.predicates.Predicates;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node join node} in a directed acyclic graph. Beside
 * this methods, it contains constants for identifying local metadata fragments
 * inside an operator node's global metadata, methods for accessing them and
 * local metadata factories for updating them.
 * 
 * @see Node
 */
public class Joins {
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant can be used to denote that a node represents a join
	 * operator in the directed acyclic graph.
	 */
	public static final String OPERATOR_TYPE = "JOIN";
	
	/**
	 * This constant is used to identify a join operator's type inside its
	 * global metadata.
	 */
	public static final String TYPE = "JOIN->TYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a join
	 * operator's global metadata for identifing the join operator's type.
	 */
	public static enum Type {
		
		/**
		 * This constant can be used to denote that a node represents a cross
		 * join (cartesian product) operator in the directed acyclic graph.
		 */
		CROSS_JOIN ,
		
		/**
		 * This constant can be used to denote that a node represents a natural
		 * join operator in the directed acyclic graph.
		 */
		NATURAL_JOIN,
		
		/**
		 * This constant can be used to denote that a node represents an
		 * equi-join operator in the directed acyclic graph.
		 */
		EQUI_JOIN,
		
		/**
		 * This constant can be used to denote that a node represents a theta
		 * join operator in the directed acyclic graph.
		 */
		THETA_JOIN
	}
	
	/**
	 * This constant is used to identify a join operator's subtype inside its
	 * global metadata.
	 */
	public static final String SUBTYPE = "JOIN->SUBTYPE";
	
	/**
	 * An enumeration providing the local metadata fragments used in a join
	 * operator's global metadata for identifing the join operator's subtype.
	 */
	public static enum Subtype {
		
		/**
		 * This constant can be used to denote that a node represents an inner
		 * join operator in the directed acyclic graph.
		 */
		INNER_JOIN,
		
		/**
		 * This constant can be used to denote that a node represents an outer
		 * join operator in the directed acyclic graph.
		 */
		OUTER_JOIN,
		
		/**
		 * This constant can be used to denote that a node represents a
		 * semi-join operator in the directed acyclic graph.
		 */
		SEMI_JOIN
	}
	
	/**
	 * This constant can be used as a prefix to identify the first
	 * equi-column's local metadata inside the equi-join's global metadata.
	 */
	public static final String FIRST_EQUI_COLUMN_PREFIX = "JOIN->FIRST_EQUI_COLUMN";
	
	/**
	 * This constant can be used as a prefix to identify the second
	 * equi-column's local metadata inside the equi-join's global metadata.
	 */
	public static final String SECOND_EQUI_COLUMN_PREFIX = "JOIN->SECOND_EQUI_COLUMN";
	
	/**
	 * This constant can be used as a prefix to identify an outer join's or a
	 * semi-join's affected child nodes inside its global metadata.
	 */
	public static final String AFFECTED_CHILD_INDEX_PREFIX = "JOIN->AFFECTED_CHILD_INDEX_";
	
	/**
	 * This constant is used to identify the join predicate inside the join
	 * operator's global metadata.
	 */
	public static final String PREDICATE = "JOIN->PREDICATE";
	
	// metadata fragment factories
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of cross join, equi-join and
	 * theta join operators.
	 */
	public static final Function<Object, AppendedResultSetMetaData> APPENDED_RESULTSET_METADATA_FACTORY = new Function<Object, AppendedResultSetMetaData>() {
		@Override
		public AppendedResultSetMetaData invoke(Object identifier, Object operator) {
			final Node node = (Node)operator;
			return new AppendedResultSetMetaData(
				new Mapper<Node, ResultSetMetaData>(
					new Function<Node, ResultSetMetaData>() {
						@Override
						public ResultSetMetaData invoke(Node node) {
							return ResultSetMetaDatas.getResultSetMetaData(node);
						}
					},
					getSubtype(node) == Subtype.SEMI_JOIN ?
						new Filter<Node>(
							node.getChildren(),
							new Predicate<Node>() {
								protected Cursor<Integer> indices = getAffectedChildIndices(node);
								protected int index = 0;
								
								@Override
								public boolean invoke(Node node) {
									if (indices.hasNext() && index++ == indices.peek()) {
										indices.next();
										return true;
									}
									return false; 
								}
							}
						) :
						node.getChildren()
				)
			);
		}
	};
	
	/**
	 * A local metadata factory that can be used to create/update the metadata
	 * fragment describing the relational data of a natural join operator.
	 */
	public static final Function<Object, UnifiedResultSetMetaData> UNIFIED_RESULTSET_METADATA_FACTORY = new Function<Object, UnifiedResultSetMetaData>() {
		@Override
		public UnifiedResultSetMetaData invoke(Object identifier, Object operator) {
			final Node node = (Node)operator;
			return new UnifiedResultSetMetaData(
				new Mapper<Node, ResultSetMetaData>(
					new Function<Node, ResultSetMetaData>() {
						@Override
						public ResultSetMetaData invoke(Node node) {
							return ResultSetMetaDatas.getResultSetMetaData(node);
						}
					},
					getSubtype(node) == Subtype.SEMI_JOIN ?
						new Filter<Node>(
							node.getChildren(),
							new Predicate<Node>() {
								protected Cursor<Integer> indices = getAffectedChildIndices(node);
								protected int index = 0;
								
								@Override
								public boolean invoke(Node node) {
									if (indices.hasNext() && index++ == indices.peek()) {
										indices.next();
										return true;
									}
									return false; 
								}
							}
						) :
						node.getChildren()
				)
			);
		}
	};
	
	// static 'constructors'
	
	/**
	 * Creates a new join operator of the given type and subtype. The join
	 * operator gets its relational data from the operator nodes given by the
	 * specified iteration.
	 * 
	 * @param type the type of the join operator. This class provides constants
	 *        for cross joins, natural joins, equi-joins and theta joins. 
	 * @param subtype the subtype of the join operator. This class provides
	 *        constants for inner joins, outer joins and semi joins. 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the join operator.
	 * @return a new join operator of the given type and subtype.
	 */
	public static final Node newJoin(Type type, Subtype subtype, Iterator<? extends Node> inputs) {
		Node operator = Operators.newOperator(OPERATOR_TYPE, Nodes.VARIABLE, Nodes.VARIABLE);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.add(TYPE, type);
		globalMetaData.add(SUBTYPE, subtype);
		globalMetaDataFactory.put(Operators.MODE, Operators.MODE_METADATA_FACTORY);
		globalMetaDataFactory.put(ResultSetMetaDatas.RESULTSET_METADATA_TYPE, type.equals(Type.NATURAL_JOIN) ? UNIFIED_RESULTSET_METADATA_FACTORY : APPENDED_RESULTSET_METADATA_FACTORY);
		
		signature.put(TYPE, Type.class);
		signature.put(SUBTYPE, Subtype.class);

		Node input;
		while (inputs.hasNext()) {
			if (Nodes.getType(input = inputs.next()) != Operators.NODE_TYPE)
				throw new IllegalArgumentException("only operators can be used as child nodes of a join operator");
			
			operator.addChild(input);
		}
				
		return operator;
	}
	
	/**
	 * Creates a new join operator of the given type and subtype. The join
	 * operator gets its relational data from the operator nodes stored in the
	 * specified array.
	 * 
	 * @param type the type of the join operator. This class provides constants
	 *        for cross joins, natural joins, equi-joins and theta joins. 
	 * @param subtype the subtype of the join operator. This class provides
	 *        constants for inner joins, outer joins and semi joins. 
	 * @param inputs an array storing the operator nodes providing the
	 *        relational data for the join operator.
	 * @return a new join operator of the given type and subtype.
	 */
	public static final Node newJoin(Type type, Subtype subtype, Node... inputs) {
		return newJoin(type, subtype, new ArrayCursor<Node>(inputs));
	}
	
	/**
	 * Creates a new cross join operator that produces the Cartesian product
	 * of the given operator nodes' relational data. The cross join operator
	 * gets its relational data from the operator nodes given by the specified
	 * iteration.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the cross join operator.
	 * @return a new cross join operator that produces the Cartesian product of
	 *         the given operator nodes' relational data.
	 */
	public static final Node newCrossJoin(Iterator<? extends Node> inputs) {
		return newJoin(Type.CROSS_JOIN, Subtype.INNER_JOIN, inputs);
	}
	
	/**
	 * Creates a new cross join operator that produces the Cartesian product
	 * of the given operator nodes' relational data.
	 * 
	 * @param inputs the operator nodes providing the relational data for the
	 *        cross join operator.
	 * @return a new cross join operator that produces the Cartesian product of
	 *         the given operator nodes' relational data.
	 */
	public static final Node newCrossJoin(Node... inputs) {
		return newCrossJoin(new ArrayCursor<Node>(inputs));
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the join of the given operator nodes' relational data. The
	 * natural join operator gets its relational data from the operator nodes
	 * given by the specified iteration.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the natural join operator.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the join of the given operator nodes' relational
	 *         data.
	 */
	public static final Node newNaturalJoin(Iterator<? extends Node> inputs) {
		return newJoin(Type.NATURAL_JOIN, Subtype.INNER_JOIN, inputs);
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the join of the given operator nodes' relational data.
	 * 
	 * @param inputs the operator nodes providing the relational data for the
	 *        natural join operator.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the join of the given operator nodes' relational
	 *         data.
	 */
	public static final Node newNaturalJoin(Node... inputs) {
		return newNaturalJoin(new ArrayCursor<Node>(inputs));
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the outer join of the given operator nodes' relational data.
	 * The natural join operator gets its relational data from the operator
	 * nodes given by the specified iteration and provides all of the
	 * relational data from the specified affected operator nodes, even if
	 * there are no matching values for relational data from the remaining
	 * operator nodes.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the natural join operator.
	 * @param affectedChildIndices an iteration holding the operator node
	 *        indices affected by this outer join, i.e., the natural join
	 *        operator provides all of the relational data from the specified
	 *        affected operator nodes, even if there are no matching values for
	 *        relational data from the remaining operator nodes.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newOuterNaturalJoin(Iterator<? extends Node> inputs, Iterator<Integer> affectedChildIndices) {
		Node operator = newJoin(Type.NATURAL_JOIN, Subtype.OUTER_JOIN, inputs);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		for (int i = 0; affectedChildIndices.hasNext(); i++) {
			globalMetaData.add(AFFECTED_CHILD_INDEX_PREFIX + i, affectedChildIndices.next());
			
			signature.put(AFFECTED_CHILD_INDEX_PREFIX + i, Integer.class);
		}
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the left outer join of the given operator nodes' relational
	 * data. The natural join operator provides all of the relational data from
	 * the first (left) operator node, even if there are no matching values for
	 * relational data from the second (right) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the natural join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the natural join operator.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the left outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newLeftOuterNaturalJoin(Node input0, Node input1) {
		return newOuterNaturalJoin(new ArrayCursor<Node>(input0, input1), new SingleObjectCursor<Integer>(0));
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the right outer join of the given operator nodes' relational
	 * data. The natural join operator provides all of the relational data from
	 * the second (right) operator node, even if there are no matching values
	 * for relational data from the first (left) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the natural join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the natural join operator.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the right outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newRightOuterNaturalJoin(Node input0, Node input1) {
		return newOuterNaturalJoin(new ArrayCursor<Node>(input0, input1), new SingleObjectCursor<Integer>(1));
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the full outer join of the given operator nodes' relational
	 * data. The natural join operator provides all of the relational data from
	 * both operator nodes, even if there are no matching values for relational
	 * data from the other operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the natural join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the natural join operator.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the full outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newFullOuterNaturalJoin(Node input0, Node input1) {
		return newOuterNaturalJoin(new ArrayCursor<Node>(input0, input1), new Enumerator(2));
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the semi-join of the given operator nodes' relational data.
	 * The natural join operator gets its relational data from the operator
	 * nodes given by the specified iteration and provides only the relational
	 * data from the specified affected operator nodes if there are matching
	 * values for relational data from the remaining operator nodes.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the natural join operator.
	 * @param affectedChildIndices an iteration holding the operator node
	 *        indices affected by this semi-join, i.e., the natural join
	 *        operator provides only the relational data from the specified
	 *        affected operator nodes if there are matching values for
	 *        relational data from the remaining operator nodes.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the semi-join of the given operator nodes' relational
	 *         data.
	 */
	public static final Node newSemiNaturalJoin(Iterator<? extends Node> inputs, Iterator<Integer> affectedChildIndices) {
		Node operator = newJoin(Type.NATURAL_JOIN, Subtype.SEMI_JOIN, inputs);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		for (int i = 0; affectedChildIndices.hasNext(); i++) {
			globalMetaData.add(AFFECTED_CHILD_INDEX_PREFIX + i, affectedChildIndices.next());
			
			signature.put(AFFECTED_CHILD_INDEX_PREFIX + i, Integer.class);
		}
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the left semi-join of the given operator nodes' relational
	 * data. The natural join provides only the relational data from the first
	 * (left) operator node if there are matching values for relational data
	 * from the second (right) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the natural join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the natural join operator.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the left semi-join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newLeftSemiNaturalJoin(Node input0, Node input1) {
		return newSemiNaturalJoin(new ArrayCursor<Node>(input0, input1), new SingleObjectCursor<Integer>(0));
	}
	
	/**
	 * Creates a new natural join operator that uses the commonly named columns
	 * to perform the right semi-join of the given operator nodes' relational
	 * data. The natural join provides only the relational data from the second
	 * (right) operator node if there are matching values for relational data
	 * from the first (left) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the natural join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the natural join operator.
	 * @return a new natural join operator that uses the commonly named columns
	 *         to perform the left semi-join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newRightSemiNaturalJoin(Node input0, Node input1) {
		return newSemiNaturalJoin(new ArrayCursor<Node>(input0, input1), new SingleObjectCursor<Integer>(1));
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the join of the given operator nodes' relational data. The
	 * equi-join operator gets its relational data from the operator nodes
	 * given by the specified iteration.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the join of the given operator nodes' relational data.
	 */
	public static final Node newEquiJoin(Iterator<? extends Node> inputs, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns) {
		Node operator = newJoin(Type.EQUI_JOIN, Subtype.INNER_JOIN, inputs);
		
		Nodes.putNodes(
			operator,
			FIRST_EQUI_COLUMN_PREFIX,
			firstEquiColumns,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == Columns.EXPRESSION_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		Nodes.putNodes(
			operator,
			SECOND_EQUI_COLUMN_PREFIX,
			secondEquiColumns,
			new Predicate<Node>() {
				@Override
				public boolean invoke(Node node) {
					return Nodes.getType(node) == Expressions.NODE_TYPE && Expressions.getType(node) == Columns.EXPRESSION_TYPE;
				}
			},
			Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY,
			true
		);
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the join of the given operator nodes' relational data.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the join of the given operator nodes' relational data.
	 */
	public static final Node newEquiJoin(Node input0, Node input1, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns) {
		return newEquiJoin(new ArrayCursor<Node>(input0, input1), firstEquiColumns, secondEquiColumns);
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the outer join of the given operator nodes' relational data. The
	 * equi-join operator gets its relational data from the operator nodes
	 * given by the specified iteration and provides all of the relational data
	 * from the specified affected operator nodes, even if there are no
	 * matching values for relational data from the remaining operator nodes.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @param affectedChildIndices an iteration holding the operator node
	 *        indices affected by this outer join, i.e., the equi-join operator
	 *        provides all of the relational data from the specified affected
	 *        operator nodes, even if there are no matching values for
	 *        relational data from the remaining operator nodes.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the outer join of the given operator nodes' relational
	 *         data.
	 */
	public static final Node newOuterEquiJoin(Iterator<? extends Node> inputs, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns, Iterator<Integer> affectedChildIndices) {
		Node operator = newEquiJoin(inputs, firstEquiColumns, secondEquiColumns);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.replace(SUBTYPE, Subtype.OUTER_JOIN);
		for (int i = 0; affectedChildIndices.hasNext(); i++) {
			globalMetaData.add(AFFECTED_CHILD_INDEX_PREFIX + i, affectedChildIndices.next());
			
			signature.put(AFFECTED_CHILD_INDEX_PREFIX + i, Integer.class);
		}
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the left outer join of the given operator nodes' relational
	 * data. The equi-join operator provides all of the relational data from
	 * the first (left) operator node, even if there are no matching values for
	 * relational data from the second (right) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the legft outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newLeftOuterEquiJoin(Node input0, Node input1, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns) {
		return newOuterEquiJoin(new ArrayCursor<Node>(input0, input1), firstEquiColumns, secondEquiColumns, new SingleObjectCursor<Integer>(0));
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the right outer join of the given operator nodes' relational
	 * data. The equi-join operator provides all of the relational data from
	 * the second (right) operator node, even if there are no matching values
	 * for relational data from the first (left) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the rigth outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newRightOuterEquiJoin(Node input0, Node input1, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns) {
		return newOuterEquiJoin(new ArrayCursor<Node>(input0, input1), firstEquiColumns, secondEquiColumns, new SingleObjectCursor<Integer>(1));
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the full outer join of the given operator nodes' relational
	 * data. The equi-join operator provides all of the relational data from
	 * both operator nodes, even if there are no matching values for relational
	 * data from the other operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the full outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newFullOuterEquiJoin(Node input0, Node input1, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns) {
		return newOuterEquiJoin(new ArrayCursor<Node>(input0, input1), firstEquiColumns, secondEquiColumns, new Enumerator(2));
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the semi-join of the given operator nodes' relational data. The
	 * equi-join operator gets its relational data from the operator
	 * nodes given by the specified iteration and provides only the relational
	 * data from the specified affected operator nodes if there are matching
	 * values for relational data from the remaining operator nodes.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @param affectedChildIndices an iteration holding the operator node
	 *        indices affected by this semi-join, i.e., the equi-join operator
	 *        provides only the relational data from the specified affected
	 *        operator nodes if there are matching values for relational data
	 *        from the remaining operator nodes.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the semi-join of the given operator nodes' relational
	 *         data.
	 */
	public static final Node newSemiEquiJoin(Iterator<? extends Node> inputs, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns, Iterator<Integer> affectedChildIndices) {
		Node operator = newEquiJoin(inputs, firstEquiColumns, secondEquiColumns);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.replace(SUBTYPE, Subtype.SEMI_JOIN);
		for (int i = 0; affectedChildIndices.hasNext(); i++) {
			globalMetaData.add(AFFECTED_CHILD_INDEX_PREFIX + i, affectedChildIndices.next());
			
			signature.put(AFFECTED_CHILD_INDEX_PREFIX + i, Integer.class);
		}
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the left semi-join of the given operator nodes' relational data.
	 * The equi-join operator provides only the relational data from the first
	 * (left) operator node if there are matching values for relational data
	 * from the second (right) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the left semi-join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newLeftSemiEquiJoin(Node input0, Node input1, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns) {
		return newSemiEquiJoin(new ArrayCursor<Node>(input0, input1), firstEquiColumns, secondEquiColumns, new SingleObjectCursor<Integer>(0));
	}
	
	/**
	 * Creates a new equi-join operator that uses the specified columns to
	 * perform the right semi-join of the given operator nodes' relational
	 * data. The equi-join operator provides only the relational data from the
	 * second (right) operator node if there are matching values for relational
	 * data from the first (left) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the equi-join operator.
	 * @param firstEquiColumns an iteration holding the first columns tested
	 *        for equivalence.
	 * @param secondEquiColumns an iteration holding the second columns tested
	 *        for equivalence.
	 * @return a new equi-join operator that uses the specified columns to
	 *         perform the right semi-join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newRightSemiEquiJoin(Node input0, Node input1, Iterator<? extends Node> firstEquiColumns, Iterator<? extends Node> secondEquiColumns) {
		return newSemiEquiJoin(new ArrayCursor<Node>(input0, input1), firstEquiColumns, secondEquiColumns, new SingleObjectCursor<Integer>(1));
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the join of the given operator nodes' relational data. The theta
	 * join operator gets its relational data from the operator nodes given by
	 * the specified iteration.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the join of the given operator nodes' relational data.
	 */
	public static final Node newThetaJoin(Iterator<? extends Node> inputs, Node predicate) {
		Node operator = newJoin(Type.THETA_JOIN, Subtype.INNER_JOIN, inputs);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = operator.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		if (Nodes.getType(predicate) != Predicates.NODE_TYPE)
			throw new IllegalArgumentException("only predicates can be used as condition of a theta join operator");
		
		globalMetaData.add(PREDICATE, predicate);
		globalMetaDataFactory.put(PREDICATE, Nodes.NODE_WITH_ANCHOR_METADATA_FACTORY);
		
		signature.put(PREDICATE, Node.class);
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the join of the given operator nodes' relational data.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the theta join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the join of the given operator nodes' relational data.
	 */
	public static final Node newThetaJoin(Node input0, Node input1, Node predicate) {
		return newThetaJoin(new ArrayCursor<Node>(input0, input1), predicate);
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the outer join of the given operator nodes' relational data. The
	 * theta join operator gets its relational data from the operator nodes
	 * given by the specified iteration and provides all of the relational data
	 * from the specified affected operator nodes, even if there are no
	 * matching values for relational data from the remaining operator nodes.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @param affectedChildIndices an iteration holding the operator node
	 *        indices affected by this outer join, i.e., the theta join
	 *        operator provides all of the relational data from the specified
	 *        affected operator nodes, even if there are no matching values for
	 *        relational data from the remaining operator nodes.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the outer join of the given operator nodes' relational
	 *         data.
	 */
	public static final Node newOuterThetaJoin(Iterator<? extends Node> inputs, Node predicate, Iterator<Integer> affectedChildIndices) {
		Node operator = newThetaJoin(inputs, predicate);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.replace(SUBTYPE, Subtype.OUTER_JOIN);
		for (int i = 0; affectedChildIndices.hasNext(); i++) {
			globalMetaData.add(AFFECTED_CHILD_INDEX_PREFIX + i, affectedChildIndices.next());
			
			signature.put(AFFECTED_CHILD_INDEX_PREFIX + i, Integer.class);
		}
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the left outer join of the given operator nodes' relational
	 * data. The theta join operator provides all of the relational data from
	 * the first (left) operator node, even if there are no matching values for
	 * relational data from the second (right) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the theta join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the left outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newLeftOuterThetaJoin(Node input0, Node input1, Node predicate) {
		return newOuterThetaJoin(new ArrayCursor<Node>(input0, input1), predicate, new SingleObjectCursor<Integer>(0));
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the right outer join of the given operator nodes' relational
	 * data. The theta join operator provides all of the relational data from
	 * the second (right) operator node, even if there are no matching values
	 * for relational data from the first (left) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the theta join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the right outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newRightOuterThetaJoin(Node input0, Node input1, Node predicate) {
		return newOuterThetaJoin(new ArrayCursor<Node>(input0, input1), predicate, new SingleObjectCursor<Integer>(1));
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the full outer join of the given operator nodes' relational
	 * data. The theta join operator provides all of the relational data from
	 * the both operator nodes, even if there are no matching values for
	 * relational data from the other operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the theta join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the full outer join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newFullOuterThetaJoin(Node input0, Node input1, Node predicate) {
		return newOuterThetaJoin(new ArrayCursor<Node>(input0, input1), predicate, new Enumerator(2));
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the semi-join of the given operator nodes' relational data. The
	 * theta join operator gets its relational data from the operator nodes
	 * given by the specified iteration and provides only the relational data
	 * from the specified affected operator nodes if there are matching values
	 * for relational data from the remaining operator nodes.
	 * 
	 * @param inputs an iteration holding the operator nodes providing the
	 *        relational data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @param affectedChildIndices an iteration holding the operator node
	 *        indices affected by this outer join, i.e., the theta join
	 *        operator provides all of the relational data from the specified
	 *        affected operator nodes, even if there are no matching values for
	 *        relational data from the remaining operator nodes.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the semi-join of the given operator nodes' relational
	 *         data.
	 */
	public static final Node newSemiThetaJoin(Iterator<? extends Node> inputs, Node predicate, Iterator<Integer> affectedChildIndices) {
		Node operator = newThetaJoin(inputs, predicate);
		CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		Map<Object, Class<?>> signature = operator.getSignature();
		
		globalMetaData.replace(SUBTYPE, Subtype.SEMI_JOIN);
		for (int i = 0; affectedChildIndices.hasNext(); i++) {
			globalMetaData.add(AFFECTED_CHILD_INDEX_PREFIX + i, affectedChildIndices.next());
			
			signature.put(AFFECTED_CHILD_INDEX_PREFIX + i, Integer.class);
		}
		
		operator.updateMetaData();
		
		return operator;
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the left semi-join of the given operator nodes' relational data.
	 * The theta join operator provides only the relational data from the first
	 * (left) operator node if there are matching values for relational data
	 * from the second (right) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the theta join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the left semi-join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newLeftSemiThetaJoin(Node input0, Node input1, Node predicate) {
		return newSemiThetaJoin(new ArrayCursor<Node>(input0, input1), predicate, new SingleObjectCursor<Integer>(0));
	}
	
	/**
	 * Creates a new theta join operator that uses the specified predicate to
	 * perform the right semi-join of the given operator nodes' relational
	 * data. The theta join operator provides only the relational data from the
	 * second (right) operator node if there are matching values for relational
	 * data from the first (left) operator node.
	 * 
	 * @param input0 the first (left) operator node providing the relational
	 *        data for the theta join operator.
	 * @param input1 the second (right) operator node providing the relational
	 *        data for the theta join operator.
	 * @param predicate the predicate that is used to determine whether the
	 *        relational data from the operator nodes should be joined to a
	 *        result of the theta join operator or not.
	 * @return a new theta join operator that uses the specified predicate to
	 *         perform the right semi-join of the given operator nodes'
	 *         relational data.
	 */
	public static final Node newRightSemiThetaJoin(Node input0, Node input1, Node predicate) {
		return newSemiThetaJoin(new ArrayCursor<Node>(input0, input1), predicate, new SingleObjectCursor<Integer>(1));
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given join operator node.
	 * 
	 * @param operator the join operator node whose type should be returned.
	 * @return the type of the given join operator node.
	 */
	public static final Type getType(Node operator) {
		return (Type)operator.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the subtype of the given join operator node.
	 * 
	 * @param operator the join operator node whose subtype should be returned.
	 * @return the subtype of the given join operator node.
	 */
	public static final Subtype getSubtype(Node operator) {
		return (Subtype)operator.getMetaData().get(SUBTYPE);
	}
	
	/**
	 * Returns the index of the affected child of the given join operator node
	 * using the specified suffix.
	 * 
	 * @param operator the outer join operator node whose affected child index
	 *        should be returned using the specified suffix.
	 * @param suffix the suffix that is used to create an unique identifier for
	 *        the desired affected child index.
	 * @return the index of the affected child of the given join operator node
	 *         usinf the specified suffix.
	 */
	public static final int getAffectedChildIndex(Node operator, String suffix) {
		return (Integer)operator.getMetaData().get(AFFECTED_CHILD_INDEX_PREFIX + suffix);
	}
	
	/**
	 * Returns an iteation over the indices of the affected children of the
	 * given join operator node.
	 * 
	 * @param operator the outer join operator node whose affected child
	 *        indices should be returned.
	 * @return an iteation over the indices of the affected children of the
	 *         given join operator node.
	 */
	public static final Cursor<Integer> getAffectedChildIndices(Node operator) {
		final CompositeMetaData<Object, Object> globalMetaData = operator.getMetaData();
		return new AbstractCursor<Integer>() {
			protected int i = -1;
			
			@Override
			public boolean hasNextObject() {
				return globalMetaData.contains(AFFECTED_CHILD_INDEX_PREFIX + ++i);
			}
			
			@Override
			public Integer nextObject() {
				return (Integer)globalMetaData.get(AFFECTED_CHILD_INDEX_PREFIX + i);
			}
			
			@Override
			public void reset() {
				super.reset();
				i = -1;
			}
			
			@Override
			public boolean supportsReset() {
				return true;
			}
		};
	}
	
	// XML I/O
	
	/**
	 * A factory method that can be used to transform the XML representation of
	 * a join operator into the node itself.
	 */
	public static final Function<Object, Node> XML_READER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			final QueryConverter queryConverter = (QueryConverter)args.get(1);
			Element element = (Element)args.get(2);
			
			Cursor<Node> children = queryConverter.readChildren(element, QueryConverter.OPERATOR_ELEMENT);
			Type type = Enum.valueOf(Type.class, element.getAttribute(QueryConverter.TYPE_ATTRIBUTE));
			Subtype subtype = Enum.valueOf(Subtype.class, element.getAttribute(QueryConverter.SUBTYPE_ATTRIBUTE));
			
			switch (type) {
				case CROSS_JOIN:
					return newCrossJoin(children);
				case NATURAL_JOIN:
					switch (subtype) {
						case INNER_JOIN:
							return newNaturalJoin(children);
						case OUTER_JOIN:
							return newOuterNaturalJoin(
								children,
								new Mapper<Element, Integer>(
									new Function<Element, Integer>() {
										@Override
										public Integer invoke(Element element) {
											return Integer.valueOf(element.getAttribute(QueryConverter.NAME_ATTRIBUTE));
										}
									},
									QueryConverter.getChildren(element, QueryConverter.AFFECTED_CHILD_ELEMENT)
								)
							);
						case SEMI_JOIN:
							return newSemiNaturalJoin(
								children,
								new Mapper<Element, Integer>(
									new Function<Element, Integer>() {
										@Override
										public Integer invoke(Element element) {
											return Integer.valueOf(element.getAttribute(QueryConverter.NAME_ATTRIBUTE));
										}
									},
									QueryConverter.getChildren(element, QueryConverter.AFFECTED_CHILD_ELEMENT)
								)
							);
					}
					throw new MetaDataException("unknown subtype " + subtype + " for a natural join");
				case EQUI_JOIN:
					ArrayList<Node> firstEquiColumns = new ArrayList<Node>();
					ArrayList<Node> secondEquiColumns = new ArrayList<Node>();
					Cursor<Element> equivalenceElements = QueryConverter.getChildren(element, QueryConverter.EQUIVALENCE_ELEMENT);
					Cursor<Element> equiColumns;
					while (equivalenceElements.hasNext()) {
						equiColumns = QueryConverter.getChildren(equivalenceElements.next(), QueryConverter.EXPRESSION_ELEMENT);
						if (!equiColumns.hasNext())
							throw new MetaDataException("an equivalence can only be tested for exactly two columns");
						firstEquiColumns.add(queryConverter.read(equiColumns.next()));
						if (!equiColumns.hasNext())
							throw new MetaDataException("an equivalence can only be tested for exactly two columns");
						secondEquiColumns.add(queryConverter.read(equiColumns.next()));
						if (equiColumns.hasNext())
							throw new MetaDataException("an equivalence can only be tested for exactly two columns");
					}
					switch (subtype) {
						case INNER_JOIN:
							return newEquiJoin(children, firstEquiColumns.iterator(), secondEquiColumns.iterator());
						case OUTER_JOIN:
							return newOuterEquiJoin(
								children,
								firstEquiColumns.iterator(),
								secondEquiColumns.iterator(),
								new Mapper<Element, Integer>(
									new Function<Element, Integer>() {
										@Override
										public Integer invoke(Element element) {
											return Integer.valueOf(element.getAttribute(QueryConverter.NAME_ATTRIBUTE));
										}
									},
									QueryConverter.getChildren(element, QueryConverter.AFFECTED_CHILD_ELEMENT)
								)
							);
						case SEMI_JOIN:
							return newSemiEquiJoin(
								children,
								firstEquiColumns.iterator(),
								secondEquiColumns.iterator(),
								new Mapper<Element, Integer>(
									new Function<Element, Integer>() {
										@Override
										public Integer invoke(Element element) {
											return Integer.valueOf(element.getAttribute(QueryConverter.NAME_ATTRIBUTE));
										}
									},
									QueryConverter.getChildren(element, QueryConverter.AFFECTED_CHILD_ELEMENT)
								)
							);
					}
					throw new MetaDataException("unknown subtype " + subtype + " for a natural join");
				case THETA_JOIN:
					Cursor<Element> predicateElements = QueryConverter.getChildren(element, QueryConverter.PREDICATE_ELEMENT);
					
					if (!predicateElements.hasNext())
						throw new MetaDataException("a theta join operator must have exactly one join predicate");
					Element predicateElement = predicateElements.next();
					if (predicateElements.hasNext())
						throw new MetaDataException("a theta join operator must not have more than one join predicate");
					
					Node predicate = queryConverter.read(predicateElement);
					
					switch (subtype) {
						case INNER_JOIN:
							return newThetaJoin(children, predicate);
						case OUTER_JOIN:
							return newOuterThetaJoin(
								children,
								predicate,
								new Mapper<Element, Integer>(
									new Function<Element, Integer>() {
										@Override
										public Integer invoke(Element element) {
											return Integer.valueOf(element.getAttribute(QueryConverter.NAME_ATTRIBUTE));
										}
									},
									QueryConverter.getChildren(element, QueryConverter.AFFECTED_CHILD_ELEMENT)
								)
							);
						case SEMI_JOIN:
							return newSemiThetaJoin(
								children,
								predicate,
								new Mapper<Element, Integer>(
									new Function<Element, Integer>() {
										@Override
										public Integer invoke(Element element) {
											return Integer.valueOf(element.getAttribute(QueryConverter.NAME_ATTRIBUTE));
										}
									},
									QueryConverter.getChildren(element, QueryConverter.AFFECTED_CHILD_ELEMENT)
								)
							);
					}
					throw new MetaDataException("unknown subtype " + subtype + " for a theta join");
			}
			throw new MetaDataException("unknown join type " + type);
		}
	};

	/**
	 * A factory method that can be used to transform a join operator into its
	 * XML representation.
	 */
	public static final Function<Object, Node> XML_WRITER = new Function<Object, Node>() {
		@Override
		public Node invoke(List<? extends Object> args) {
			QueryConverter queryConverter = (QueryConverter)args.get(1);
			Document document = (Document)args.get(2);
			Element element = (Element)args.get(3);
			Node operator = (Node)args.get(4);
			
			Type type = getType(operator);
			
			element.setAttribute(QueryConverter.TYPE_ATTRIBUTE, type.toString());
			
			switch (type) {
				case EQUI_JOIN:
					try {
						for (Cursor<Node> firstEquiColumns = Nodes.getNodes(operator, FIRST_EQUI_COLUMN_PREFIX), secondEquiColumns = Nodes.getNodes(operator, SECOND_EQUI_COLUMN_PREFIX); firstEquiColumns.hasNext() || secondEquiColumns.hasNext();) {
							Element childElement = document.createElement(QueryConverter.EQUIVALENCE_ELEMENT);
							queryConverter.write(firstEquiColumns.next(), document, childElement);
							queryConverter.write(secondEquiColumns.next(), document, childElement);
							element.appendChild(childElement);
						}
					}
					catch (NoSuchElementException nsee) {
						throw new MetaDataException("the number of given first and second equi columns does not fit");
					}
					break;
				case THETA_JOIN:
					queryConverter.write(Nodes.getNode(operator, PREDICATE), document, element);
					break;
				case CROSS_JOIN:
				case NATURAL_JOIN:
			}
			
			Subtype subtype = getSubtype(operator);
			
			element.setAttribute(QueryConverter.SUBTYPE_ATTRIBUTE, subtype.toString());
			
			switch (subtype) {
				case OUTER_JOIN:
				case SEMI_JOIN:
					for (Cursor<Integer> children = getAffectedChildIndices(operator); children.hasNext();) {
						Element childElement = document.createElement(QueryConverter.AFFECTED_CHILD_ELEMENT);
						childElement.setAttribute(QueryConverter.NAME_ATTRIBUTE, children.next().toString());
						element.appendChild(childElement);
					}
					break;
				case INNER_JOIN:
			}
			
			queryConverter.writeChildren(operator.getChildren(), document, element);
			
			return null;
		}
	};
	
	// query translation

	/**
	 * A factory method that can be used to translate a logical join operator
	 * into an appropriate implementation.
	 */
	public static final Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>> TRANSLATION_FUNCTION = new Function<Object, MetaDataProvider<CompositeMetaData<Object, Object>>>() {
		@Override
		public MetaDataProvider<CompositeMetaData<Object, Object>> invoke(List<? extends Object> args) {
			final QueryTranslator queryTranslator = (QueryTranslator)args.get(0);
			final Node node = (Node)args.get(1);
			
			Cursor<Node> children = node.getChildren();
			
			// test for children
			if (!children.hasNext())
				throw new MetaDataException("a join operator must have at least two input operators");
			MetaDataProvider<CompositeMetaData<Object, Object>> firstChild = queryTranslator.translate(children.next());
			if (!children.hasNext())
				throw new MetaDataException("a join operator must have at least two input operators");
			MetaDataProvider<CompositeMetaData<Object, Object>> secondChild = queryTranslator.translate(children.next());
			if (children.hasNext())
				throw new MetaDataException("implementations of multiway join are still unsupported yet, i.e., they must be transformed to join plans of binary joins using the query optimizer");
			
			// get the join's type and mode
			Type type = getType(node);
			Operators.Mode mode = Operators.getMode(node); 
			
			switch (mode) {
				case ACTIVE: {
					// create an active implementation for the logical operator
					Source<TemporalObject<Tuple>> source = null;
					
					switch (type) {
						case CROSS_JOIN: {
							// cross join aka. cartesian product
							source = xxl.core.pipes.operators.joins.Joins.TemporalCartesianProduct(
								(Source<TemporalObject<Tuple>>)firstChild,
								(Source<TemporalObject<Tuple>>)secondChild,
								0,
								1,
								JoinUtils.genericJoinTupleFactory(
									ArrayTuple.FACTORY_METHOD,
									(MergedResultSetMetaData)ResultSetMetaDatas.getResultSetMetaData(node)
								)
							);
							break;
						} // CROSS_JOIN
						case NATURAL_JOIN: {
							// natural join
							source = xxl.core.pipes.operators.joins.Joins.TemporalSNJ(
								(Source<TemporalObject<Tuple>>)firstChild,
								(Source<TemporalObject<Tuple>>)secondChild,
								0,
								1,
								new FeaturePredicate<TemporalObject<Tuple>, Tuple>(
									JoinUtils.naturalJoinPredicate(
										new UnifiedResultSetMetaData(
											new Mapper<Node, ResultSetMetaData>(
												new Function<Node, ResultSetMetaData>() {
													@Override
													public ResultSetMetaData invoke(Node node) {
														return ResultSetMetaDatas.getResultSetMetaData(node);
													}
												},
												node.getChildren()
											)
										)
									),
									new Function<TemporalObject<Tuple>, Tuple>() {
										@Override
										public Tuple invoke(TemporalObject<Tuple> object) {
											return object.getObject();
										}
									}
								),
								JoinUtils.genericJoinTupleFactory(
									ArrayTuple.FACTORY_METHOD,
									(MergedResultSetMetaData)ResultSetMetaDatas.getResultSetMetaData(node)
								)
							);
							break;
						} // NATURAL_JOIN
						case EQUI_JOIN: {
							// equi-join
							
							// because of the lack of a separate equi-join
							// implementation a theta join predicate is
							// constructed
							Cursor<Predicate<Tuple>> predicates = new Mapper<Node, Predicate<Tuple>>(
								new Function<Node, Predicate<Tuple>>() {
									@Override
									public Predicate<Tuple> invoke(Node firstEquiColumn, Node secondEquiColumn) {
										return (Predicate<Tuple>)queryTranslator.translate(Comparisons.newEqualComparison(firstEquiColumn, secondEquiColumn));
									}
								},
								Nodes.getNodes(node, FIRST_EQUI_COLUMN_PREFIX),
								Nodes.getNodes(node, SECOND_EQUI_COLUMN_PREFIX)
							);
							Predicate<? super Tuple> predicate;
							if (predicates.hasNext()) {
								predicate = predicates.next();
								while (predicates.hasNext())
									predicate = new And<Tuple>(predicate, predicates.next());
							}
							else
								predicate = xxl.core.predicates.Predicates.TRUE;
							
							source = xxl.core.pipes.operators.joins.Joins.TemporalSNJ(
								(Source<TemporalObject<Tuple>>)firstChild,
								(Source<TemporalObject<Tuple>>)secondChild,
								0,
								1,
								new FeaturePredicate<TemporalObject<Tuple>, Tuple>(
									predicate,
									new Function<TemporalObject<Tuple>, Tuple>() {
										@Override
										public Tuple invoke(TemporalObject<Tuple> object) {
											return object.getObject();
										}
									}
								),
								JoinUtils.genericJoinTupleFactory(
									ArrayTuple.FACTORY_METHOD,
									(MergedResultSetMetaData)ResultSetMetaDatas.getResultSetMetaData(node)
								)
							);
							break;
						} // EQUI_JOIN
						case THETA_JOIN: {
							// theta join
							source = xxl.core.pipes.operators.joins.Joins.TemporalSNJ(
								(Source<TemporalObject<Tuple>>)firstChild,
								(Source<TemporalObject<Tuple>>)secondChild,
								0,
								1,
								new FeaturePredicate<TemporalObject<Tuple>, Tuple>(
									(Predicate<Tuple>)queryTranslator.translate(Nodes.getNode(node, PREDICATE)),
									new Function<TemporalObject<Tuple>, Tuple>() {
										@Override
										public Tuple invoke(TemporalObject<Tuple> object) {
											return object.getObject();
										}
									}
								),
								JoinUtils.genericJoinTupleFactory(
									ArrayTuple.FACTORY_METHOD,
									(MergedResultSetMetaData)ResultSetMetaDatas.getResultSetMetaData(node)
								)
							);
						} // THETA_JOIN
					} // switch (type)
					if (source == null)
						throw new MetaDataException("unknown join type " + type);
					
					// set the implementations metadata information to the
					// metadata information provided by the logical node
					((AbstractMetaDataManagement<Object, Object>)source.getMetaDataManagement()).initialize(node.getMetaData());
					
					return source;
				} // ACTIVE
				case PASSIVE: {
					// create a passive implementation for the logical operator
					// cross join aka. cartesian product
					switch (type) {
						case CROSS_JOIN: {
							// cross join aka. cartesian product
							return new NestedLoopsJoin(
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)firstChild,
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)secondChild,
								null,
								ArrayTuple.FACTORY_METHOD,
								NestedLoopsJoin.Type.CARTESIAN_PRODUCT
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
						} // CROSS_JOIN
						case NATURAL_JOIN: {
							// natural join
							return new NestedLoopsJoin(
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)firstChild,
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)secondChild,
								null,
								new MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>(
									JoinUtils.naturalJoinPredicate(
										new UnifiedResultSetMetaData(
											new Mapper<Node, ResultSetMetaData>(
												new Function<Node, ResultSetMetaData>() {
													@Override
													public ResultSetMetaData invoke(Node node) {
														return ResultSetMetaDatas.getResultSetMetaData(node);
													}
												},
												node.getChildren()
											)
										)
									)
								) {
									@Override
									public CompositeMetaData<Object, Object> getMetaData() {
										return node.getMetaData();
									}
								},
								ArrayTuple.FACTORY_METHOD,
								NestedLoopsJoin.Type.NATURAL_JOIN
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
						} // NATURAL_JOIN
						case EQUI_JOIN: {
							// equi-join

							// because of the lack of a separate equi-join
							// implementation a theta join predicate is
							// constructed
							Cursor<Predicate<Tuple>> predicates = new Mapper<Node, Predicate<Tuple>>(
								new Function<Node, Predicate<Tuple>>() {
									@Override
									public Predicate<Tuple> invoke(Node firstEquiColumn, Node secondEquiColumn) {
										return (Predicate<Tuple>)queryTranslator.translate(Comparisons.newEqualComparison(firstEquiColumn, secondEquiColumn));
									}
								},
								Nodes.getNodes(node, FIRST_EQUI_COLUMN_PREFIX),
								Nodes.getNodes(node, SECOND_EQUI_COLUMN_PREFIX)
							);
							Predicate<? super Tuple> predicate;
							if (predicates.hasNext()) {
								predicate = predicates.next();
								while (predicates.hasNext())
									predicate = new And<Tuple>(predicate, predicates.next());
							}
							else
								predicate = xxl.core.predicates.Predicates.TRUE;
							
							return new NestedLoopsJoin(
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)firstChild,
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)secondChild,
								null,
								new MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>(predicate) {
									@Override
									public CompositeMetaData<Object, Object> getMetaData() {
										return node.getMetaData();
									}
								},
								ArrayTuple.FACTORY_METHOD,
								NestedLoopsJoin.Type.THETA_JOIN
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
						} // EQUI_JOIN
						case THETA_JOIN: {
							// theta join
							return new NestedLoopsJoin(
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)firstChild,
								(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>>)secondChild,
								null,
								new MetaDataPredicate<Tuple, CompositeMetaData<Object, Object>>((Predicate<Tuple>)queryTranslator.translate(Nodes.getNode(node, PREDICATE))) {
									@Override
									public CompositeMetaData<Object, Object> getMetaData() {
										return node.getMetaData();
									}
								},
								ArrayTuple.FACTORY_METHOD,
								NestedLoopsJoin.Type.THETA_JOIN
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
						} // THETA_JOIN
					} // switch (type)
				} // PASSIVE
			} // switch (mode)
			throw new MetaDataException("unknown join mode " + mode);
		}
	};
	
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Joins() {
		// private access in order to ensure non-instantiability
	}
	
}
