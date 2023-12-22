/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.cursors.joins;

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.queues.StackQueue;
import xxl.core.collections.sweepAreas.SweepArea;
import xxl.core.cursors.identities.TeeCursor;
import xxl.core.functions.Function;

/**
 * A sort-merge implementation of the self-join operator for asymmetric join
 * predicates.
 * 
 * @param <I> the type of the elements consumed by this iteration.
 * @param <E> the type of the elements returned by this join operation.
 * @see SortMergeJoin
 * @see SortMergeSymmetricSelfJoin 
 * @since 1.1
 */
public class SortMergeSelfJoin<I, E> extends SortMergeJoin<I, E> {

	/**
	 * Creates a new sort-merge self-join operator backed on a sorted input
	 * iterations using the given sweep-areas to store the input iterations'
	 * elements and probe for join results. Furthermore a function named
	 * <code>newResult</code> can be specified that is invoked on each
	 * qualifying tuple before it is returned to the caller concerning a call
	 * to the <code>next</code> method. This function is a kind of factory
	 * method to model the resulting object.
	 * 
	 * <p><b>Precondition:</b> The input iteration have to be sorted!</p>
	 * 
	 * <p>The result-tuples created by this operator are represented as an
	 * array of the input iterations' elements that are participated in this
	 * join result. If the user wants to specify a different result-type, a
	 * mapping function <code>newResult</code> can be specified. This function
	 * works like a kind of factory method modelling the resulting object
	 * (tuple). The iterator given to this constructor is wrapped to a
	 * cursor.<p>
	 *
	 * @param sortedInput the sorted input iteration to be joined.
	 * @param sweepArea0 the sweep-area used for storing elements of the 
	 *        sorted input iteration (<code>sortedInput</code>) considered as
	 *        left input.
	 * @param sweepArea1 the sweep-area used for storing elements of the sorted
	 *        input iteration (<code>sortedInput</code>) considered as right
	 *        input.
	 * @param newResult a factory method (function) that takes two parameters
	 *        as argument and is invoked on each tuple where the predicate's
	 *        evaluation result is <code>true</code>, i.e., on each qualifying
	 *        tuple before it is returned to the caller concerning a call to
	 *        the <code>next</code> method.
	 */	
	public SortMergeSelfJoin(Iterator<? extends I> sortedInput, SweepArea<I,I> sweepArea0, SweepArea<I,I> sweepArea1, Function<? super I, ? extends E> newResult) {
		super(
			new TeeCursor<I>(sortedInput, new StackQueue<I>()),
			null,
			sweepArea0,
			sweepArea1,
			new Comparator<I>() {
				private int side = 1;
				
				public int compare(I arg0, I arg1) {
					return side = -side;
				}
			},
			newResult
		);
		this.sortedInput1 = ((TeeCursor<I>)sortedInput0).cursor();
	}

}
