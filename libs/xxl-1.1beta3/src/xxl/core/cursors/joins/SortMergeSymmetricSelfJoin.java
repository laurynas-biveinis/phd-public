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
import java.util.Iterator;

import xxl.core.collections.sweepAreas.SweepArea;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.sources.Enumerator;
import xxl.core.functions.Function;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * A sort-merge implementation of the self-join operator for symmetric join predicates.
 * 
 * @see SortMergeJoin
 * @see SortMergeSelfJoin 
 * @since 1.1
 */
public class SortMergeSymmetricSelfJoin extends AbstractCursor {

	/**
	 * The sorted input iteration of the join operator.
	 */
	protected Cursor sortedInput;
	
	/**
	 * The sweep-area that is used for storing the elements of the input
	 * iteration {@link #sortedInput} and that is probed with those elements.
	 */
	protected SweepArea sweepArea;
	
	
	/** Unary predicate determining if a tuple joins with itself.
	 */
	protected Predicate selfJoinPredicate;
		
	/**
	 * An element of one of the input iteration that is actually used for
	 * querying the sweep-area of the other input iteration.
	 */
	protected Object queryObject = null;	//the next Object to be processed by this operator
		
	/**
	 * A function that is invoked on each qualifying tuple before it is returned
	 * to the caller concerning a call to the <tt>next</tt> method. This binary
	 * function works like a kind of factory method modelling the resulting
	 * object (tuple). 
	 */
	protected Function newResult;
	
	/**
	 * An iterator holding the precomputed results of the join operator. Because
	 * the <tt>query</tt> method of sweep-area returns an iterator of elements
	 * that match the given query, this iterator must be stored to return the
	 * remaining elements later.
	 */
	protected Iterator results = EmptyCursor.DEFAULT_INSTANCE;

	/** Last Object obtained from {@link #results} which has to be returned for the 
	 * symmetric case.
	 */
	protected Object lastResult = null;
	
	/** Flag indicating if the last object from the input has joins with itself
	 * and has not yet been returned. 
	 */
	protected boolean self = false;
		
	/**
	 * Creates a new sort-merge self-join operator backed on a sorted input
	 * iterations using the given sweep-area to store the input iterations'
	 * elements and probe for join results. Furthermore a function named
	 * <tt>newResult</tt> can be specified that is invoked on each qualifying
	 * tuple before it is returned to the caller concerning a call to the
	 * <tt>next</tt> method. This function is a kind of factory method to model
	 * the resulting object.
	 * 
	 * <p><b>Precondition:</b> The input iteration have to be sorted!</p>
	 * <p><b>Precondition:</b> The join has to be symmetric</p>
	 * 
	 * <p>The result-tuples created by this operator are represented as an array
	 * of the input iterations' elements that are participated in this join
	 * result. If the user wants to specify a different result-type, a mapping
	 * function <tt>newResult</tt> can be specified. This function works like a
	 * kind of factory method modelling the resulting object (tuple). 
	 * The iterator given to this constructor is wrapped to a cursor.<p>
	 *
	 * @param sortedInput the sorted input iteration to be joined.
	 * @param sweepArea the sweep-area used for storing elements of the 
	 *        sorted input iteration (<tt>sortedInput</tt>).
	 * @param selfJoinPredicate unary predicate determining if a tuple joins with itself
	 * 	 * @param newResult a factory method (function) that takes two parameters as
	 *        argument and is invoked on each tuple where the predicate's
	 *        evaluation result is <tt>true</tt>, i.e., on each qualifying tuple
	 *        before it is returned to the caller concerning a call to the
	 *        <tt>next</tt> method.
	 */
	public SortMergeSymmetricSelfJoin(Iterator sortedInput, SweepArea sweepArea, Predicate selfJoinPredicate, Function newResult) {
		this.sortedInput = Cursors.wrap(sortedInput);
		this.newResult = newResult;
		this.sweepArea = sweepArea;
		this.selfJoinPredicate = selfJoinPredicate;
		
		//check input arguments (assert):
		if (newResult == null)
			throw new IllegalArgumentException("one of the input arguments was null!");
	}

	/**
	 * Creates a new sort-merge self-join operator backed on a sorted input
	 * iterations using the given sweep-area to store the input iterations'
	 * elements and probe for join results. Furthermore a function named
	 * <tt>newResult</tt> can be specified that is invoked on each qualifying
	 * tuple before it is returned to the caller concerning a call to the
	 * <tt>next</tt> method. This function is a kind of factory method to model
	 * the resulting object. The constructor does not require the input
	 * iteration to be sorted. The specified, unary function
	 * <tt>newSorter</tt> will be invoked on the input iteration in order to 
	 * get a sorted input.
	 * 
	 * <p><b>Precondition:</b> The join has to be symmetric</p>
	 * 
	 * <p>The result-tuples created by this operator are represented as an array
	 * of the input iterations' elements that are participated in this join
	 * result. If the user wants to specify a different result-type, a mapping
	 * function <tt>newResult</tt> can be specified. This function works like a
	 * kind of factory method modelling the resulting object (tuple). 
	 * The iterator given to this constructor is wrapped to a cursor.<p>
	 *
	 * @param input the input iteration to be joined.
	 * @param newSorter an unary function that sorts the input iteration
	 * @param sweepArea the sweep-area used for storing elements of the 
	 *        sorted input iteration (<tt>sortedInput</tt>).
 	 * @param selfJoinPredicate unary predicate determining if a tuple joins with itself
	 * @param newResult a factory method (function) that takes two parameters as
	 *        argument and is invoked on each tuple where the predicate's
	 *        evaluation result is <tt>true</tt>, i.e., on each qualifying tuple
	 *        before it is returned to the caller concerning a call to the
	 *        <tt>next</tt> method.
	 */
	public SortMergeSymmetricSelfJoin(Iterator input, Function newSorter, SweepArea sweepArea, Predicate selfJoinPredicate, Function newResult) {
		this((Iterator)newSorter.invoke(input), sweepArea, selfJoinPredicate, newResult);
	}
	
	/**
	 * Opens the join operator, i.e., signals the cursor to reserve resources,
	 * open the input iteration, etc. Before a cursor has been
	 * opened calls to methods like <tt>next</tt> or <tt>peek</tt> are not
	 * guaranteed to yield proper results. Therefore <tt>open</tt> must be called
	 * before a cursor's data can be processed. Multiple calls to <tt>open</tt>
	 * do not have any effect, i.e., if <tt>open</tt> was called the cursor
	 * remains in the state <i>opened</i> until its <tt>close</tt> method is
	 * called.
	 * 
	 * <p>Note, that a call to the <tt>open</tt> method of a closed cursor
	 * usually does not open it again because of the fact that its state
	 * generally cannot be restored when resources are released respectively
	 * files are closed.</p>
	 */
	public void open() {
		if (isOpened) return;
		super.open();
		sortedInput.open();
	}
	
	/**
	 * Closes the cursor, i.e., signals the cursor to clean up resources, close
	 * input iterations and sweep-areas, etc. When a cursor has been closed calls
	 * to methods like <tt>next</tt> or <tt>peek</tt> are not guaranteed to yield
	 * proper results. Multiple calls to <tt>close</tt> do not have any effect,
	 * i.e., if <tt>close</tt> was called the cursor remains in the state
	 * <i>closed</i>.
	 * 
	 * <p>Note, that a closed cursor usually cannot be opened again because of
	 * the fact that its state generally cannot be restored when resources are
	 * released respectively files are closed.</p>
	 */
	public void close () {
		if (isClosed) return;
		super.close();
		sortedInput.close();
		sweepArea.close();
	}

	/**
	 * Returns <tt>true</tt> if the iteration has more elements. (In other
	 * words, returns <tt>true</tt> if <tt>next</tt> or <tt>peek</tt> would
	 * return an element rather than throwing an exception.)
	 * 
	 * @return <tt>true</tt> if the cursor has more elements.
	 */
	protected boolean hasNextObject() {
		if (!self && lastResult == null && !results.hasNext()) {
			//while the input is not empty
			while (sortedInput.hasNext()) {
				if (queryObject != null) {
					sweepArea.insert(queryObject);
				}
				queryObject = sortedInput.next();
				if (selfJoinPredicate.invoke(queryObject)) self = true;
				sweepArea.reorganize(queryObject, 0);
				results = sweepArea.query(queryObject, 0);				
				if (self || results.hasNext()) {
					return true; 
				}
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the next element in the iteration. This element will be
	 * accessible by some of the cursor's methods, e.g., <tt>update</tt> or
	 * <tt>remove</tt>, until a call to <tt>next</tt> or <tt>peek</tt> occurs.
	 * This is calling <tt>next</tt> or <tt>peek</tt> proceeds the iteration and
	 * therefore its previous element will not be accessible any more.
	 * 
	 * @return the next element in the iteration.
	 */
	protected Object nextObject() {		
		if (self) {
			self = false;
			return newResult.invoke(queryObject, queryObject);
		}
		if (lastResult != null) {
			Object res = newResult.invoke(lastResult, queryObject);
			lastResult = null;
			return res;
		}
		if (results.hasNext()) { 
			lastResult = results.next();
			return newResult.invoke(queryObject, lastResult);
		}	
		return null;
	}

	/**
	 * Resets the sort-merge join operator to its initial state such that the
	 * caller is able to traverse the join result again without constructing a
	 * new join operator (optional operation).
	 * 
	 * <p>Note, that this operation is optional and might not work for all
	 * cursors.</p>
	 *
	 * @throws UnsupportedOperationException if the <tt>reset</tt> operation is
	 *         not supported by the cursor.
	 */
	public void reset () throws UnsupportedOperationException {
		super.reset();
		sortedInput.reset();
		sweepArea.clear();
		results = EmptyCursor.DEFAULT_INSTANCE;
		self = false;
		lastResult = null;
	}
	
	/**
	 * Returns <tt>true</tt> if the <tt>reset</tt> operation is supported by
	 * the sort-merge join operator. Otherwise it returns <tt>false</tt>.
	 *
	 * @return <tt>true</tt> if the <tt>reset</tt> operation is supported by
	 *         the sort-merge join operator, otherwise <tt>false</tt>.
	 */
	public boolean supportsReset() {
		return true;
	}
	
	/**
	 * Returns a string representation of the object. In general, the
	 * <tt>toString</tt> method returns a string that "textually represents" this
	 * object. The result should be a concise but informative representation that
	 * is easy for a person to read. It is recommended that all subclasses
	 * override this method.
	 * 
	 * @return string representation of the object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator sweepAreaContent = sweepArea.iterator();
		sb.append("sweep area:\n");
		while (sweepAreaContent.hasNext())
			sb.append(sweepAreaContent.next() + "\n");
		return sb.toString();
	}
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 * @throws Exception some <tt>main</tt> methods of classes extending this
	 *         class throw exceptions.
	 */
	public static void main(String[] args) throws Exception {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		// Input iterator : 0, ... , 9
		// Join Predicate : (a,b) <- Join  <=>  a=b
		
		SortMergeSymmetricSelfJoin join = new SortMergeSymmetricSelfJoin(
			new Enumerator(0,10),
			new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA(
				new xxl.core.collections.sweepAreas.ListSAImplementor(),
				0,
				2
			),
			Predicates.TRUE,
			Function.IDENTITY
		);

		join.open();	
		while (join.hasNext())
			System.out.println(join.next());
		join.close();
		
		System.out.println("---");
		
		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		// Input iterator : 0, ... , 3
		// Join Predicate : (a,b) <- Join  <=>  |a-b| <= 2
		
		join = new SortMergeSymmetricSelfJoin(
			new Enumerator(0,4),
			new xxl.core.collections.sweepAreas.ImplementorBasedSweepArea(
				new xxl.core.collections.sweepAreas.ListSAImplementor(),
				1,
				true,
				xxl.core.predicates.Predicates.TRUE,
				new Predicate() {
					public boolean invoke(Object o1, Object o2) {
						return ((Integer)o2).intValue()-((Integer)o1).intValue()>2;						
					}					
				},
				2
			),
			Predicates.TRUE,
			Function.IDENTITY
		);

		join.open();		
		while (join.hasNext())
			System.out.println(join.next());
		join.close();
	}

}
