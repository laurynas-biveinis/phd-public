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

package xxl.core.cursors.joins;

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.sweepAreas.SweepArea;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.Function;

/**
 * A sort-merge implementation of the join operator. This class provides a
 * generic, untyped sort-merge join algorithm. The resulting tuples of any join
 * operation are generated by a user defined function realizing a kind of
 * factory method. The binary function <code>newResult</code> can be used to
 * map the result tuples to an arbitrary user defined type. The sweep-line
 * status structure, here called sweep-area, consists of a bag with an
 * additional method for reorganisation. The way the elements of the input
 * iterations are inserted into the according sweep-area is determined by a
 * given comparator. Depending on the result of the comparison of the two next
 * input elements of the input iterations, the left (<code>sortedInput0</code>)
 * or right (<code>sortedInput1</code>) input is processed. If the left input
 * is processed, the sweep-area's of <code>sortedInput0</code> and
 * <code>sortedInput1</code> are reorganized and the next element of
 * <code>sortedInput0</code> is inserted in <code>sweepArea0</code>. After that
 * <code>sweepArea1</code> is queried with this element, i.e., the specified
 * predicate is applied on the elements contained in <code>sweepArea1</code>
 * and the last inserted element of <code>sweepArea0</code>. In order to
 * perform an effective search in the sweep-area, the query method of the bags
 * should be overriden. If the binary predicate returns <code>true</code> the
 * evaluated tuple gets an result of the join operation. After that query the
 * mapping function to create the result-tuples is applied on the results
 * detected by the predicate and after that they are returned to the user. The
 * right input is processed analogous. The implementation is a bit more complex
 * due to addional checks of join-types and the generation of result-tuples
 * where the evaluated join predicate returned <code>false</code>.
 * 
 * <p><b>Note:</b> When the given input iteration only implements the interface
 * {@link Iterator} it is wrapped to a cursor by a call to the static
 * method {@link Cursors#wrap(Iterator) wrap}.</p>
 * 
 * <p><b>Example usage (1):</b>
 * <code><pre>
 *     LinkedList l1 = new LinkedList();
 *     final LinkedList l2 = new LinkedList();
 *     for (int i = 0; i &le; 10; i++) {
 *         // left: odd numbers or can be divided by 4
 *         if (i%2 != 0 || i%4 == 0)
 *             l1.add(new Integer(i));
 *         //right: even numbers
 *         if (i%2 == 0)
 *             l2.add(new Integer(i));
 *     }
 * 
 *     SortMergeJoin join = new SortMergeJoin(
 *         l1.listIterator(),
 *         l2.listIterator(),
 *         new SortMergeEquiJoinSA(
 *             new ListSAImplementor(),
 *             0,
 *             2
 *         ),
 *         new SortMergeEquiJoinSA(
 *             new ListSAImplementor(),
 *             1,
 *             2
 *         ),
 *         ComparableComparator.DEFAULT_INSTANCE,
 *         Function.IDENTITY
 *     );
 * 
 *     join.open();
 * 
 *     while (join.hasNext()) {
 *         Object[] result = (Object[])join.next();
 *         System.out.println("Tuple: (" + result[0] + ", " + result[1] + ")");
 *     }
 * 
 *     join.close();
 * </pre></code>
 * The input iterations of this simple example are two list iterators. The
 * first one is based on all odd numbers and numbers that can be divided by 4
 * of the interval [0, 10]. The second input iterator contains all even numbers
 * of the same interval. The comparator, a default
 * {@link xxl.core.comparators.ComparableComparator#INTEGER_COMPARATOR comparator}
 * for integers, is used for comparing the elements stored in the sweep-areas
 * based on a {@link java.util.List list}. In this example the specified
 * function to create user defined result-tuples is a rather simple one. The
 * {@link xxl.core.functions.Tuplify tuplify} function delivers the
 * result-tuples in their original representation, namely as an array 
 * containing the matching elements of the input iterations. This can simply be
 * seen when printing the result-tuples to the output stream. But in the
 * package {@link xxl.core.relational} there are different factory methods
 * creating a particular kind of tuple, e.g.,
 * {@link xxl.core.relational.tuples.ArrayTuple array-tuples},
 * {@link xxl.core.relational.tuples.ListTuple list-tuples}, that substitute the
 * tuplify function. So, let us consider the output of this join operation,
 * which looks as follows:
 * <pre>
 *   [0, 0]
 *   [4, 4]
 *   [8, 8]
 * </pre></p>
 * 
 * @param <I> the type of the elements consumed by this iteration.
 * @param <E> the type of the elements returned by this join operation.
 * @see java.util.Iterator
 * @see xxl.core.cursors.Cursor
 * @see xxl.core.cursors.AbstractCursor
 * @see xxl.core.cursors.joins.NestedLoopsJoin
 * @see xxl.core.cursors.joins.SortMergeEquivalenceJoin
 * @see xxl.core.relational.cursors.SortMergeJoin
 * @see xxl.core.spatial.cursors.Orenstein
 * @see xxl.core.spatial.cursors.PlaneSweep
 */
//use switchable bags in those cases where the sweep area does not fit in main memory (external bags)
public class SortMergeJoin<I, E> extends AbstractCursor<E> {

	/**
	 * An enumeration of constants specifing the join types supported by this
	 * class.
	 */
	public static enum Type {
		
		/**
		 * A constant specifying a theta-join. Only the tuples for which the
		 * specified predicate is <code>true</code> will be returned.
		 */
		THETA_JOIN,
		
		/**
		 * A constant specifying a left outer-join. The tuples for which the
		 * specified predicate is <code>true</code> as well as all elements of
		 * <code>input0</code> not qualifying concerning the predicate will be
		 * returned. The function <code>newResult</code> is called with an
		 * element of <code>input0</code> and the <code>null</code> value.
		 */
		LEFT_OUTER_JOIN,
		
		/**
		 * A constant specifying a right outer-join. The tuples for which the
		 * specified predicate is <code>true</code> as well as all elements of
		 * <code>input1</code> not qualifying concerning the predicate will be
		 * returned. The function <code>newResult</code> is called with an
		 * element of <code>input1</code> and the <code>null</code> value.
		 */
		RIGHT_OUTER_JOIN,
		
		/**
		 * A constant specifying a full outer-join. The tuples for which the
		 * specified predicate is <code>true</code> as well as all tuples
		 * additionally returned by the left and right outer-join will be
		 * returned.
		 */
		OUTER_JOIN
	};

	/**
	 * The first (or left) sorted input iteration of the join operator.
	 */
	protected Cursor<? extends I> sortedInput0;
	
	/**
	 * The second (or right) sorted input iteration of the join operator.
	 */
	protected Cursor<? extends I> sortedInput1;
	
	/**
	 * The sweep-area that is used for storing the elements of the first input
	 * iteration (<code>sortedInput0</code>) and that is probed with elements
	 * of the second input iteration (<code>sortedInput1</code>).
	 */
	protected SweepArea<I> sweepArea0;
	
	/**
	 * The sweep-area that is used for storing the elements of the second input
	 * iteration (<code>sortedInput1</code>) and that is probed with elements
	 * of the first input iteration (<code>sortedInput0</code>).
	 */
	protected SweepArea<I> sweepArea1;
	
	/**
	 * An element of one of the input iteration that is actually used for
	 * querying the sweep-area of the other input iteration.
	 */
	protected I queryObject;	//the next Object to be processed by this operator
	
	/**
	 * A boolean flag that determines whether the elements stored in
	 * <code>queryObject</code> belongs to the first input iteration or not.
	 */
	protected boolean first = false;
	
	/**
	 * The comparator used to compare the elements of the two input iterations.
	 */
	protected Comparator<? super I> comparator;

	/**
	 * A function that is invoked on each qualifying tuple before it is
	 * returned to the caller concerning a call to the <code>next</code>
	 * method. This binary function works like a kind of factory method
	 * modelling the resulting object (tuple). Be aware that this function
	 * possibly has to handle <code>null</code> values in cases of outer joins.
	 */
	protected Function<? super I, ? extends E> newResult;

	/**
	 * The type of this sort-merge join operator. Determines whether it
	 * calculates a theta- or an outer-join.
	 */
	protected Type type = Type.THETA_JOIN;
	
	/**
	 * An iterator holding the precomputed results of the join operator.
	 * Because the <code>query</code> method of sweep-area returns an iterator
	 * of elements that match the given query, this iterator must be stored to
	 * return the remaining elements later.
	 */
	protected Iterator<? extends I> results = new EmptyCursor<I>();
	
	/**
	 * Creates a new sort-merge join operator backed on two sorted input
	 * iterations using the given sweep-areas to store the input iterations'
	 * elements and probe for join results. Furthermore a function named
	 * <code>newResult</code> can be specified that is invoked on each
	 * qualifying tuple before it is returned to the caller concerning a call
	 * to the <code>next</code> method. This function is a kind of factory
	 * method to model the resulting object.
	 * 
	 * <p><b>Precondition:</b> The input iterations have to be sorted!</p>
	 * 
	 * @param sortedInput0 the first sorted input iteration to be joined.
	 * @param sortedInput1 the second sorted input iteration to be joined.
	 * @param sweepArea0 the sweep-area used for storing elements of the first
	 *        sorted input iteration (<code>sortedInput0</code>).
	 * @param sweepArea1 the sweep-area used for storing elements of the second
	 *        sorted input iteration (<code>sortedInput1</code>).
	 * @param comparator the comparator that is used for comparing elements of
	 *        the two sorted input iterations.
	 * @param newResult a factory method (function) that takes two parameters
	 *        as argument and is invoked on each tuple where the predicate's
	 *        evaluation result is <code>true</code>, i.e., on each qualifying
	 *        tuple before it is returned to the caller concerning a call to
	 *        the <code>next</code> method.
	 */
	public SortMergeJoin(Iterator<? extends I> sortedInput0, Iterator<? extends I> sortedInput1, SweepArea<I> sweepArea0, SweepArea<I> sweepArea1, Comparator<? super I> comparator, Function<? super I, ? extends E> newResult) {
		this.sortedInput0 = Cursors.wrap(sortedInput0);
		this.sortedInput1 = Cursors.wrap(sortedInput1);
		this.comparator = comparator;
		this.newResult = newResult;
		this.sweepArea0 = sweepArea0;
		this.sweepArea1 = sweepArea1;
		
		//check input arguments (assert):
		if (comparator == null || newResult == null)
			throw new IllegalArgumentException("one of the input arguments was null!");
	}

	/**
	 * Creates a new sort-merge join operator backed on two input iterations
	 * using the given sweep-areas to store the input iterations' elements and
	 * probe for join results. The constructor does not require the two input
	 * iterations to be sorted. The two specified, unary functions
	 * <code>newSorter0</code> and <code>newSorter1</code> will be invoked on
	 * the corresponding input iteration in order to get a sorted input.
	 * Furthermore a function named <code>newResult</code> can be specified
	 * that is invoked on each qualifying tuple before it is returned to the
	 * caller concerning a call to the <code>next</code> method. This function
	 * is a kind of factory method to model the resulting object.
	 * 
	 * @param input0 the first input iteration to be joined.
	 * @param input1 the second input iteration to be joined.
	 * @param newSorter0 an unary function that sorts the first input iteration
	 *        <code>input0</code>.
	 * @param newSorter1 an unary function that sorts the second input
	 *        iteration <code>input1</code>.
	 * @param sweepArea0 the sweep-area used for storing elements of the first
	 *        input iteration (<code>input0</code>).
	 * @param sweepArea1 the sweep-area used for storing elements of the second
	 *        input iteration (<code>input1</code>).
	 * @param comparator the comparator that is used for comparing elements of
	 *        the two input iterations.
	 * @param newResult a factory method (function) that takes two parameters
	 *        as argument and is invoked on each tuple where the predicate's
	 *        evaluation result is <code>true</code>, i.e., on each qualifying
	 *        tuple before it is returned to the caller concerning a call to
	 *        the <code>next</code> method.
	 */
	public SortMergeJoin(Iterator<? extends I> input0, Iterator<? extends I> input1, Function<? super Iterator<? extends I>, ? extends Iterator<? extends I>> newSorter0, Function<? super Iterator<? extends I>, ? extends Iterator<? extends I>> newSorter1, SweepArea<I> sweepArea0, SweepArea<I> sweepArea1, Comparator<? super I> comparator, Function<? super I, ? extends E> newResult) {
		this(newSorter0.invoke(input0), newSorter1.invoke(input1), sweepArea0, sweepArea1, comparator, newResult);
	}

	/**
	 * Creates a new sort-merge join operator backed on two sorted input
	 * iterations using a parameterless function to create the required 
	 * sweep-areas that are used to store the input iterations' elements and
	 * probe for join results. Furthermore a function named
	 * <code>newResult</code> can be specified that is invoked on each
	 * qualifying tuple before it is returned to the caller concerning a call
	 * to the <code>next</code> method. This function is a kind of factory
	 * method to model the resulting object.
	 * 
	 * <p><b>Precondition:</b> The input iterations have to be sorted!</p>
	 * 
	 * @param sortedInput0 the first sorted input iteration to be joined.
	 * @param sortedInput1 the second sorted input iteration to be joined.
	 * @param newSweepArea a parameterless function creating a new sweep-area
	 *        that is used for storing elements of the sorted input iterations.
	 * @param comparator the comparator that is used for comparing elements of
	 *        the two sorted input iterations.
	 * @param newResult a factory method (function) that takes two parameters
	 *        as argument and is invoked on each tuple where the predicate's
	 *        evaluation result is <code>true</code>, i.e., on each qualifying
	 *        tuple before it is returned to the caller concerning a call to
	 *        the <code>next</code> method.
	 */
	public SortMergeJoin(Iterator<? extends I> sortedInput0, Iterator<? extends I> sortedInput1, Function<?, ? extends SweepArea<I>> newSweepArea, Comparator<? super I> comparator, Function<? super I, ? extends E> newResult) {
		this(sortedInput0, sortedInput1, newSweepArea.invoke(), newSweepArea.invoke(), comparator, newResult);
	}

	/**
	 * Returns whether an element of the first input iteration is responsible
	 * for the actual element of the sort-based join operator, i.e., if an
	 * element of the first input iteration has been probed against the second
	 * sweep-area to produce actual element. The implementation simply returns
	 * the field <code>first</code>.
	 *  
	 * @return the value of the filed <code>first</code>.
	 */
	protected boolean getFirst() {
		return first;
	}
	
	/**
	 * Opens the join operator, i.e., signals the cursor to reserve resources,
	 * open the input iteration, etc. Before a cursor has been opened calls to
	 * methods like <code>next</code> or <code>peek</code> are not guaranteed
	 * to yield proper results. Therefore <code>open</code> must be called
	 * before a cursor's data can be processed. Multiple calls to
	 * <code>open</code> do not have any effect, i.e., if <code>open</code> was
	 * called the cursor remains in the state <i>opened</i> until its
	 * <code>close</code> method is called.
	 * 
	 * <p>Note, that a call to the <code>open</code> method of a closed cursor
	 * usually does not open it again because of the fact that its state
	 * generally cannot be restored when resources are released respectively
	 * files are closed.</p>
	 */
	public void open() {
		if (isOpened)
			return;
		super.open();
		sortedInput0.open();
		sortedInput1.open();
	}
	
	/**
	 * Closes the cursor, i.e., signals the cursor to clean up resources, close
	 * input iterations and sweep-areas, etc. When a cursor has been closed
	 * calls to methods like <code>next</code> or <code>peek</code> are not
	 * guaranteed to yield proper results. Multiple calls to <code>close</code>
	 * do not have any effect, i.e., if <code>close</code> was called the
	 * cursor remains in the state <i>closed</i>.
	 * 
	 * <p>Note, that a closed cursor usually cannot be opened again because of
	 * the fact that its state generally cannot be restored when resources are
	 * released respectively files are closed.</p>
	 */
	public void close () {
		if (isClosed)
			return;
		super.close();
		sortedInput0.close();
		sweepArea0.close();
		sortedInput1.close();
		sweepArea1.close();
	}

	/**
	 * Returns <code>true</code> if the iteration has more elements. (In other
	 * words, returns <code>true</code> if <code>next</code> or
	 * <code>peek</code> would return an element rather than throwing an
	 * exception.)
	 * 
	 * @return <code>true</code> if the cursor has more elements.
	 */
	protected boolean hasNextObject() {
		if (!results.hasNext()) {
			queryObject = null;
			//while one of the inputs is not empty
			while (sortedInput0.hasNext() || sortedInput1.hasNext()) {
				// if one of the inputs is empty: process non-empty input
				// else compare
				boolean inputEmpty = !(sortedInput0.hasNext() && sortedInput1.hasNext());
	
				if (first = (inputEmpty ? sortedInput0.hasNext() : comparator.compare(sortedInput0.peek(), sortedInput1.peek()) <= 0)) {
					//process LEFT input:
					//get the next object to be considered form the corresponding input
					queryObject = sortedInput0.next();
					//pass queryObject to SweepArea to be queried (the SweepArea can then eliminate elements that are "out-of-date")
					sweepArea1.reorganize(queryObject, 0);
					//pass queryObject to SweepArea where nextObject will be inserted ( " , this operation is of HIGH IMPORTANCE for non-equi joins!)
					sweepArea0.reorganize(queryObject, 0);
					sweepArea0.insert(queryObject);
	
					//if iterator of results is empty:
					if (!(results = sweepArea1.query(queryObject, 0)).hasNext()) {
					//if element of the right input contains an element that equals queryObject
						if ((type == Type.LEFT_OUTER_JOIN || type == Type.OUTER_JOIN) && !(sortedInput1.hasNext() && comparator.compare(queryObject, sortedInput1.peek()) == 0)) {
							next = newResult.invoke(queryObject, null);
							return true; 
						}
					}
					else
						return true;
				}
				else {
					//process RIGHT input:
					//get the next Object to be considered from the corresponding input
					queryObject = sortedInput1.next();
					//pass queryObject to SweepArea to be queried (the SweepArea can then eliminate elements that are "out-of-date")
					sweepArea0.reorganize(queryObject, 1);
					//pass queryObject to SweepArea where nextObject will be inserted ( " , this operation is of HIGH IMPORTANCE for non-equi joins!)
					sweepArea1.reorganize(queryObject, 1);
					sweepArea1.insert(queryObject);
	
					//if iterator of results is empty
					if (!(results = sweepArea0.query(queryObject, 1)).hasNext()) {
						//if element of the left input contains an element that equals queryObject
						if ((type == Type.RIGHT_OUTER_JOIN || type == Type.OUTER_JOIN) && !(sortedInput0.hasNext() && comparator.compare(queryObject, sortedInput0.peek()) == 0)) {
							next =  newResult.invoke(null, queryObject);
							return true;
						}
					}
					else
						return true;
				}
			}
			return false;
		}
		return true;
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
	protected E nextObject() {
		if (results.hasNext()) 
			return first ? 
				(E)newResult.invoke(queryObject, results.next()) :
				newResult.invoke(results.next(), queryObject);
		return next;
	}

	/**
	 * Resets the sort-merge join operator to its initial state such that the
	 * caller is able to traverse the join result again without constructing a
	 * new join operator (optional operation).
	 * 
	 * <p>Note, that this operation is optional and might not work for all
	 * cursors.</p>
	 *
	 * @throws UnsupportedOperationException if the <code>reset</code>
	 *         operation is not supported by the cursor.
	 */
	public void reset () throws UnsupportedOperationException {
		super.reset();
		sortedInput0.reset();
		sweepArea0.clear();
		sortedInput1.reset();
		sweepArea1.clear();
		results = new EmptyCursor<I>();
	}
	
	/**
	 * Returns <code>true</code> if the <code>reset</code> operation is
	 * supported by the sort-merge join operator. Otherwise it returns
	 * <code>false</code>.
	 *
	 * @return <code>true</code> if the <code>reset</code> operation is
	 *         supported by the sort-merge join operator, otherwise
	 *         <code>false</code>.
	 */
	public boolean supportsReset() {
		return true;
	}
	
	/**
	 * Returns a string representation of the object. In general, the
	 * <code>toString</code> method returns a string that
	 * "textually represents" this object. The result should be a concise but
	 * informative representation that is easy for a person to read. It is
	 * recommended that all subclasses override this method.
	 * 
	 * @return string representation of the object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator sweepAreaContent = sweepArea0.iterator();
		sb.append("sweep area 0:\n");
		while (sweepAreaContent.hasNext())
			sb.append(sweepAreaContent.next() + "\n");
		sweepAreaContent = sweepArea1.iterator();
		sb.append("sweep area 1:\n");
		while (sweepAreaContent.hasNext())
			sb.append(sweepAreaContent.next() + "\n");
		return sb.toString();
	}
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		SortMergeJoin<Integer, Object[]> join = new SortMergeJoin<Integer, Object[]>(
			java.util.Arrays.asList(0, 1, 3, 4, 5, 7, 8, 9).iterator(),
			java.util.Arrays.asList(0, 2, 4, 6, 8, 10).iterator(),
			new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA<Integer>(
				new xxl.core.collections.sweepAreas.ListSAImplementor<Integer>(),
				0,
				2
			),
			new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA<Integer>(
				new xxl.core.collections.sweepAreas.ListSAImplementor<Integer>(),
				1,
				2
			),
			xxl.core.comparators.ComparableComparator.INTEGER_COMPARATOR,
			new xxl.core.functions.Tuplify()
		);

		join.open();
		
		while (join.hasNext())
			System.out.println(java.util.Arrays.toString(join.next()));
		
		join.close();
	}

}
