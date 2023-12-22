package xxl.tests.predicates;

import java.util.Arrays;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Subquery;
import xxl.core.predicates.AllPredicate;
import xxl.core.predicates.Predicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AllPredicate.
 */
public class TestAllPredicate {

	/**
	 * The main method contains some examples of how to use an AllPredicate.
	 * It can also be used to test the functionality of the AllPredicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		System.out.println("Cursor 1: integers 11 to 15");
		Cursor<Integer> cursor1 = xxl.core.cursors.Cursors.wrap(
			new xxl.core.cursors.sources.Enumerator(11,16)
		);
		
		System.out.println("Cursor 2: integers 9 to 19");
		Cursor<Integer> cursor2 = xxl.core.cursors.Cursors.wrap(
			new xxl.core.cursors.sources.Enumerator(9,20)
		);

		//SELECT (Integer2)
		//FROM Cursor 2
		//WHERE ALL Integer2 > (SELECT Integer1
		//		FROM Cursor 1)

		Predicate<Integer> pred = new xxl.core.predicates.Less<Integer>(new xxl.core.comparators.ComparableComparator<Integer>());

		Subquery<Integer> sub = new Subquery<Integer>(cursor1,null,null);
		
		Predicate<Integer> all0 = new AllPredicate<Integer>(sub, pred, Arrays.asList(1));

		xxl.core.cursors.filters.Filter<Integer> cursor = new xxl.core.cursors.filters.Filter<Integer>(cursor2, all0);

		System.out.println("Cursor: result");

		xxl.core.cursors.Cursors.println(cursor);

	}

}
