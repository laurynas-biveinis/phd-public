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

package xxl.core.relational.cursors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.bags.ArrayBag;
import xxl.core.collections.bags.Bag;
import xxl.core.collections.bags.ListBag;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.functions.Function;
import xxl.core.predicates.Equal;
import xxl.core.predicates.Predicate;
import xxl.core.relational.cursors.SortMergeJoin.PredicateBasedSA;
import xxl.core.relational.metaData.MergedResultSetMetaData;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.relational.tuples.Tuples;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides the division operator of the relational algebra.
 * 
 * <p>Let us consider the division of <tt>R</tt> and <tt>S</tt> with
 * <tt>R[A1,...,Ai,B1,...,Bj]</tt> and <tt>S[B1,...,Bj]</tt>. Then the result
 * <tt>Res</tt> has schema <tt>Res[A1,...,Ai]</tt>. For every result tuple
 * <tt>res</tt> and every tuple <tt>s</tt> from <tt>S</tt>, the tuple
 * <tt>(res,s)</tt> is contained in <tt>R</tt>.</p>
 * 
 * <p>In other words, the division computes all elements of <tt>R</tt>
 * projected to the <tt>A</tt>-attributes, that are contained in <tt>R</tt>
 * with <b>all</b> tuples of <tt>S</tt>.</p>
 * 
 * <p>The division is an operation that can deliver interesting information
 * from databases!</p>
 * 
 * <p>This class performs a sort-merge join (<tt>R natural join S</tt>). Then,
 * it outputs all elements projected to <tt>[A1,...,Ai]</tt> that occur exactly
 * <tt>|S|</tt> times in the join result.</p>
 */
public class SortBasedDivision extends AbstractCursor<Tuple> implements MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> {

	/**
	 * A counter saving the number of tuples of the second input.
	 */
	protected int counter = 0;
	
	/**
	 * A metadata cursor representing the result of the division.
	 */
	protected MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> result;

	/**
	 * Constructs an instance of the sort-based division operator.
	 *
	 * @param sortedDistinctCursor1 the sorted metadata cursor (by the all
	 *        attributes: <tt>order by A1, ..., Ai, B1, ..., Bj</tt>)
	 *        containing elements of the first input relation (no duplicates
	 *        allowed).
	 * @param sortedDistinctCursor2 the sorted metadata cursor (by the quotient
	 *        attributes) containing elements of the second input relation (no
	 *        duplicates allowed).
	 * @param bag a bag that is used for the sweep area of the internal
	 *        {@link SortMergeJoin} operator.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 */
	public SortBasedDivision(MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>> sortedDistinctCursor1, MetaDataCursor<? extends Tuple, CompositeMetaData<Object, Object>> sortedDistinctCursor2, Bag<Tuple> bag, Function<Object, ? extends Tuple> tupleFactory) {
		try {
			SortMergeJoin join = new SortMergeJoin(
				sortedDistinctCursor1,
				sortedDistinctCursor2,
				// SweepArea0 would always contain exactly one element.
				// But no solution can be generated by this SweepArea.
				// The use of an EmptyBag would be possible (without exceptions)!
				new PredicateBasedSA<Tuple>(
					new ArrayBag<Tuple>(1),
					SortMergeJoin.computeMetaDataPredicate(
						sortedDistinctCursor1,
						sortedDistinctCursor2,
						SortMergeJoin.Type.NATURAL_JOIN
					),
					0
				) {
					public void reorganize(Tuple currentStatus, int ID) throws IllegalStateException {
						clear();
					}
				},
				new PredicateBasedSA<Tuple>(
					bag,
					SortMergeJoin.computeMetaDataPredicate(
						sortedDistinctCursor1,
						sortedDistinctCursor2,
						SortMergeJoin.Type.NATURAL_JOIN
					),
					1
				) {
					public void insert(Tuple tuple) {
						super.insert(tuple);
						counter++;
					}
					
					public void reorganize(Tuple currentStatus, int ID) throws IllegalStateException {}
				},
				new Comparator<Tuple>() {
					public int compare(Tuple tuple1, Tuple tuple2) {
						return 1;
					}
				},
				tupleFactory,
				SortMergeJoin.Type.NATURAL_JOIN
			);
					
			MergedResultSetMetaData joinMetaData = (MergedResultSetMetaData)ResultSetMetaDatas.getResultSetMetaData(join);
			
			ArrayList<Integer> indices = new ArrayList<Integer>();
			columns: for (int column = 1; column <= joinMetaData.getColumnCount(); column++) {
				Iterator<Integer> metadatas = joinMetaData.originalMetaDataIndices(column);
				while (metadatas.hasNext())
					if (metadatas.next() == 1)
						continue columns;
				indices.add(column);
			}
			int[] projectedColumns = new int[indices.size()];
			for (int i = 0; i < projectedColumns.length; projectedColumns[i] = indices.get(i++));
			
			final Comparator<Tuple> tupleComparator = Tuples.getTupleComparator(projectedColumns);
			
			this.result = new Selection(
				new Projection(
					join,
					tupleFactory,
					projectedColumns
				),
				new Predicate<Tuple>() {
					protected Tuple last = null;
					protected int noOfResults = 0;
					
					public boolean invoke(Tuple tuple) {
						if (last == null || tupleComparator.compare(last, tuple) != 0)
							noOfResults = 0;
						noOfResults++;
						last = tuple;
						return noOfResults == counter;
					}
				}
			);
		}
		catch (SQLException sqle) {
			throw new MetaDataException("sql exception occured during meta data construction: \'" + sqle.getMessage() + "\'");
		}
	}

	/**
	 * Constructs an instance of the sort-based division operator. An
	 * {@link ListBag list-bag} is used for the sweep area of the internal
	 * {@link SortMergeJoin} operator.
	 *
	 * @param sortedCursor1 the sorted metadata cursor (by the all
	 *        attributes: <tt>order by A1, ..., Ai, B1, ..., Bj</tt>)
	 *        containing elements of the first input relation.
	 * @param sortedCursor2 the sorted metadata cursor (by the quotient
	 *        attributes) containing elements of the second input relation.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 */
	public SortBasedDivision(MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> sortedCursor1, MetaDataCursor<Tuple, CompositeMetaData<Object, Object>> sortedCursor2, Function<Object, ? extends Tuple> tupleFactory) {
		this(
			new SortBasedDistinct(sortedCursor1, Equal.DEFAULT_INSTANCE),
			new SortBasedDistinct(sortedCursor2, Equal.DEFAULT_INSTANCE),
			new ListBag<Tuple>(),
			tupleFactory
		);
	}

	/**
	 * Constructs an instance of the sort-based division operator.
	 *
	 * @param sortedDistinctResultSet1 the sorted result set (by the all
	 *        attributes: <tt>order by A1, ..., Ai, B1, ..., Bj</tt>)
	 *        containing elements of the first input relation (no duplicates
	 *        allowed).
	 * @param sortedDistinctResultSet2 the sorted result set (by the quotient
	 *        attributes) containing elements of the second input relation (no
	 *        duplicates allowed).
	 * @param bag a bag that is used for the sweep area of the internal
	 *        {@link SortMergeJoin} operator.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 */
	public SortBasedDivision(ResultSet sortedDistinctResultSet1, ResultSet sortedDistinctResultSet2, Bag<Tuple> bag, Function<Object, ? extends Tuple> tupleFactory) {
		this(
			new ResultSetMetaDataCursor(sortedDistinctResultSet1),
			new ResultSetMetaDataCursor(sortedDistinctResultSet2),
			bag,
			tupleFactory
		);
	}

	/**
	 * Constructs an instance of the sort-based division operator. An
	 * {@link ListBag list} is used for the sweep area of the internal
	 * {@link SortMergeJoin} operator.
	 *
	 * @param sortedResultSet1 the sorted result set (by the all attributes:
	 *        <tt>order by A1, ..., Ai, B1, ..., Bj</tt>) containing elements
	 *        of the first input relation.
	 * @param sortedResultSet2 the sorted result set (by the quotient
	 *        attributes) containing elements of the second input relation.
	 * @param tupleFactory a function that maps a list of objects (column
	 *        values) to a new tuple. Classes implementing the Tuple interface
	 *        should provide factory methods for this task. If
	 *        <code>null</code> is passed, a factory method producing
	 *        {@link ArrayTuple array-tuples} is used.
	 */
	public SortBasedDivision(ResultSet sortedResultSet1, ResultSet sortedResultSet2, Function<Object, ? extends Tuple> tupleFactory) {
		this(
			new ResultSetMetaDataCursor(sortedResultSet1),
			new ResultSetMetaDataCursor(sortedResultSet2),
			tupleFactory
		);
	}
	
	/**
	 * Returns <code>true</code> if the iteration has more elements. (In other
	 * words, returns <code>true</code> if <code>next</code> or
	 * <code>peek</code> would return an element rather than throwing an
	 * exception.)
	 * 
	 * @return <code>true</code> if the cursor has more elements.
	 */
	public boolean hasNextObject() {
		return result.hasNext();
	}
	
	/**
	 * Returns the next element in the iteration. This element will be
	 * accessible by some of the cursor's methods, e.g., <code>update</code> or
	 * <code>remove</code>, until a call to <code>next</code> or
	 * <code>peek</code> occurs. This is calling <code>next</code> or
	 * <code>peek</code> proceeds the iteration and therefore its previous
	 * element will not be accessible any more.
	 * 
	 * @return the next element in the iteration.
	 */
	public Tuple nextObject() {
		return result.next();
	}

	/**
	 * Resets the cursor to its initial state such that the caller is able to
	 * traverse the underlying data structure again without constructing a new
	 * cursor (optional operation). The modifications, removes and updates
	 * concerning the underlying data structure, are still persistent.
	 * 
	 * <p>Note, that this operation is optional and does not work for this
	 * cursor.</p>
	 *
	 * @throws UnsupportedOperationException if the <code>reset</code>
	 *         operation is not supported by the cursor.
	 */
	public void reset() throws UnsupportedOperationException {
		super.reset();
		result.reset();
	}
	
	/**
	 * Returns <code>true</code> if the <code>reset</code> operation is
	 * supported by the cursor. Otherwise it returns <code>false</code>.
	 *
	 * @return <code>true</code> if the <code>reset</code> operation is
	 *         supported by the cursor, otherwise <code>false</code>.
	 */
	public boolean supportsReset() {
		return true;
	}
	
	/**
	 * Closes the cursor, i.e., signals the cursor to clean up resources, close
	 * files, etc. When a cursor has been closed calls to methods like
	 * <code>next</code> or <code>peek</code> are not guaranteed to yield
	 * proper results. Multiple calls to <code>close</code> do not have any
	 * effect, i.e., if <code>close</code> was called the cursor remains in the
	 * state <i>closed</i>.
	 * 
	 * <p>Note, that a closed cursor usually cannot be opened again because of
	 * the fact that its state generally cannot be restored when resources are
	 * released respectively files are closed.</p>
	 */
	public void close() {
		if (isClosed)
			return;
		super.close();
		result.close();
	}

	/**
	 * Returns the metadata information for this metadata-cursor as a composite
	 * metadata ({@link CompositeMetaData}).
	 *
	 * @return the metadata information for this metadata-cursor as a composite
	 *         metadata ({@link CompositeMetaData}).
	 */
	public CompositeMetaData<Object, Object> getMetaData() {
		return result.getMetaData();
	}
}
