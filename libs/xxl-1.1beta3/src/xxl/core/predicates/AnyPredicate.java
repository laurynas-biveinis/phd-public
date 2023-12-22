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

package xxl.core.predicates;

import java.util.List;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Subquery;

/**
 * This class provides a prototypical implementation of the any-predicate. When
 * the <code>invoke</code> method of this class is called, the given arguments
 * will be applied to the subquery and it will be checked if any of the objects
 * the subquery delivers satisfy the check condition.
 * 
 * <p>For example consider the implementation of the query
 * <pre>
 *   SELECT (Integer2)
 *   FROM Cursor 2
 *   WHERE ANY Integer2 = (SELECT Integer1
 *                         FROM Cursor 1)
 * </pre>
 * by using an <code>AnyPredicate</code> instance
 * <code><pre>
 *   System.out.println("Cursor 1: integers 11 to 19");
 *   Cursor&lt;Integer&gt; cursor1 = Cursors.wrap(new Enumerator(11,20));
 *   
 *   System.out.println("Cursor 2: integers 9 to 14");
 *   Cursor&lt;Integer&gt; cursor2 = Cursors.wrap(new Enumerator(9,15));
 *   
 *   Predicate&lt;Integer&gt; pred = new Equal&lt;Integer&gt;();
 *   
 *   Subquery&lt;Integer&gt; sub = new Subquery&lt;Integer&gt;(cursor1, null, null);
 *   
 *   Predicate&lt;Integer&gt; any0 = new AnyPredicate&lt;Integer&gt;(sub, pred, Arrays.asList(1));
 *   
 *   Filter&lt;Integer&gt; cursor = new Filter&lt;Integer&gt;(cursor2, any0);
 *   
 *   System.out.println("Cursor: result");
 *   
 *   Cursors.println(cursor);
 * </code></pre></p>
 *
 * @param <P> the type of the predicate's parameters.
 */
public class AnyPredicate<P> extends Predicate<P> {

	/**
	 * The subquery used in the any-predicate.
	 */
	protected Subquery<P> subquery;

	/**
	 * The check condition of the any-predicate.
	 */
	protected BindingPredicate<P> checkCondition;

	/**
	 * The indices to the positions where the free variable emerges in the
	 * check condition.
	 */
	protected List<Integer> checkBindIndices;

	/**
	 * Creates a new any-predicate. When the <code>invoke</code> method is
	 * called, the given arguments will be applied to the subquery and it will
	 * be checked if any of the objects the subquery delivers satisfy the check
	 * condition.
	 *
	 * @param subquery the subquery used in the any-predicate.
	 * @param checkCondition the check condition of the any-predicate.
	 * @param checkBindIndices the Indices to the positions where the free
	 *        variable emerges in the check condition.
	 */
	public AnyPredicate(Subquery<P> subquery, Predicate<? super P> checkCondition, List<Integer> checkBindIndices) {
		this.subquery = subquery;
		this.checkCondition = new BindingPredicate<P>(checkCondition);
		this.checkBindIndices = checkBindIndices;
	}

	/**
	 * Creates a new any-predicate. When the <code>invoke</code> method is
	 * called, the given arguments will be applied to the subquery and it will
	 * be checked if any of the objects the subquery delivers satisfy the check
	 * condition.
	 */
	public AnyPredicate() {
	}

	/**
	 * Set the subquery of the any-predicate.
	 *
	 * @param subquery the subquery used in the all-predicate.
	 */
	public void setSubquery(Subquery<P> subquery) {
		this.subquery = subquery;
	}

	/**
	 * Set the check condition of the any-predicate.
	 *
	 * @param checkCondition the check condition of the any-predicate.
	 * @param checkBindIndices the Indices to the positions where the free
	 *        variable emerges in the check condition.
	 */
	public void setCheckCondition(Predicate<? super P> checkCondition, List<Integer> checkBindIndices) {
		this.checkCondition = new BindingPredicate<P>(checkCondition);
		this.checkBindIndices = checkBindIndices;
	}

	/**
	 * When the <tt>invoke</tt> method is called, the given arguments will be
	 * applied to the subquery and it will be checked if any of the objects the
	 * subquery delivers satisfy the check condition.
	 *
	 * @param arguments the arguments to be applied to the underlying
	 *        predicate.
	 * @return the result of the underlying predicate's <code>invoke</code>
	 *         method that is called with applied arguments.
	 */
	public boolean invoke(List<? extends P> arguments) {
		boolean result = false;
		if (arguments == null)
			return invoke((P)null);
		subquery.bind(arguments);
		subquery.reset();
		checkCondition.setBinds(checkBindIndices, arguments);
		while (subquery.hasNext() && !result)
			if (checkCondition.invoke(subquery.next()))
				result = true;
		while (subquery.hasNext())
			subquery.next();
		return result;
	}

	/**
	 * The main method contains some examples of how to use an AnyPredicate.
	 * It can also be used to test the functionality of the AnyPredicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		System.out.println("Cursor 1: integers 11 to 19");
		Cursor<Integer> cursor1 = xxl.core.cursors.Cursors.wrap(
			new xxl.core.cursors.sources.Enumerator(11,20)
		);
		
		System.out.println("Cursor 2: integers 9 to 14");
		Cursor<Integer> cursor2 = xxl.core.cursors.Cursors.wrap(
			new xxl.core.cursors.sources.Enumerator(9,15)
		);

		//SELECT (Integer2)
		//FROM Cursor 2
		//WHERE ANY Integer2 = (SELECT Integer1
		//		                FROM Cursor 1)

		Predicate<Integer> pred = new xxl.core.predicates.Equal<Integer>();

		Subquery<Integer> sub = new Subquery<Integer>(cursor1, null, null);
		
		Predicate<Integer> any0 = new AnyPredicate<Integer>(sub, pred, java.util.Arrays.asList(1));

		xxl.core.cursors.filters.Filter<Integer> cursor = new xxl.core.cursors.filters.Filter<Integer>(cursor2, any0);

		System.out.println("Cursor: result");

		xxl.core.cursors.Cursors.println(cursor);

	}
}
