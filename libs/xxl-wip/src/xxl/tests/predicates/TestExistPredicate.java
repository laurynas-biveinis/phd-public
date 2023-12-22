package xxl.tests.predicates;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Subquery;
import xxl.core.predicates.BindingPredicate;
import xxl.core.predicates.ExistPredicate;
import xxl.core.predicates.Predicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ExistPredicate.
 */
public class TestExistPredicate {

	/**
	 * The main method contains some examples of how to use an ExistPredicate.
	 * It can also be used to test the functionality of the ExistPredicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		System.out.println("Cursor 1: integers 8 to 15");
		Cursor<Integer> cursor1 = xxl.core.cursors.Cursors.wrap(
			new xxl.core.cursors.sources.Enumerator(8,16)
		);
		
		System.out.println("Cursor 2: integers 9 to 19");
		Cursor<Integer> cursor2 = xxl.core.cursors.Cursors.wrap(
			new xxl.core.cursors.sources.Enumerator(9,20)
		);

		//SELECT (Integer2)
		//FROM Cursor 2
		//WHERE EXIST (SELECT Integer1
		//		FROM Cursor 1
		//		WHERE Integer1=Integer2)

		Predicate<Integer> pred = new xxl.core.predicates.Equal<Integer>();
		BindingPredicate<Integer> bindPred = new BindingPredicate<Integer>(pred, java.util.Arrays.asList(1));

		xxl.core.cursors.filters.Filter<Integer> sel = new xxl.core.cursors.filters.Filter<Integer>(cursor1, bindPred);

		Subquery<Integer> sub = new Subquery<Integer>(
			sel,
			java.util.Arrays.asList(bindPred),
			new int[][] {
				new int[] {1}
			}
		);
		
		Predicate<Integer> exist0 = new ExistPredicate<Integer>(sub);

		xxl.core.cursors.filters.Filter<Integer> cursor = new xxl.core.cursors.filters.Filter<Integer>(cursor2, exist0);

		System.out.println("Cursor: result");

		xxl.core.cursors.Cursors.println(cursor);
	}	

}
