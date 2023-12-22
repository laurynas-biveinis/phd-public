package xxl.tests.predicates;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Subquery;
import xxl.core.predicates.AnyPredicate;
import xxl.core.predicates.Predicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AnyPredicate.
 */
public class TestAnyPredicate {

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
