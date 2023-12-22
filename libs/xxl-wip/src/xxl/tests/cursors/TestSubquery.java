package xxl.tests.cursors;

import java.util.Arrays;

import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.Subquery;
import xxl.core.cursors.filters.Filter;
import xxl.core.cursors.sources.Enumerator;
import xxl.core.predicates.And;
import xxl.core.predicates.AnyPredicate;
import xxl.core.predicates.BindingPredicate;
import xxl.core.predicates.Equal;
import xxl.core.predicates.ExistPredicate;
import xxl.core.predicates.GreaterEqual;
import xxl.core.predicates.Predicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Subquery.
 */
public class TestSubquery {

	/**
	 * The main method contains some examples of how to use an
	 * {@link xxl.core.predicates.ExistPredicate exist} predicate. It can also
	 * be used to test the functionality of the exist predicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		System.out.println("Cursor 1: integers 8 to 15");
		Enumerator cursor1 = new Enumerator(8, 16);

		System.out.println("Cursor 2: integers 9 to 19");
		Enumerator cursor2 = new Enumerator(9, 20);

		System.out.println("Cursor 3: integers 1 to 11");
		Enumerator cursor3 = new Enumerator(1, 12);
		
		// SELECT Integer3
		// FROM Cursor3
		// WHERE EXIST(
		//     SELECT Integer2
		//     FROM Cursor 2
		//     WHERE ANY Integer3 = (
		//         SELECT Integer1
		//         FROM Cursor 1
		//         WHERE Integer2=Integer1 AND Integer1>=Integer3
		//     )
		// )

		Predicate<Integer> equal = new Equal<Integer>();
		Predicate<Integer> greaterEqual = new GreaterEqual<Integer>(new ComparableComparator<Integer>());
		// bind Integer2 in Equal
		BindingPredicate<Integer> bindEqual = new BindingPredicate<Integer>(equal, Arrays.asList(0));
		// bind Integer3 in GreaterEqual
		BindingPredicate<Integer> bindGrEqual = new BindingPredicate<Integer>(greaterEqual, Arrays.asList(1));
		// and bindings
		Predicate<Integer> and = new And<Integer>(bindEqual, bindGrEqual);
		Filter<Integer> sel = new Filter<Integer>(cursor1, and);
		// inner subquery with two bindings
		Subquery<Integer> sub = new Subquery<Integer>(
			sel,
			Arrays.asList(
				bindEqual,
				bindGrEqual
			),
			new int[][] {
				new int[] {0, -1},
				new int[] {-1, 1}
			}
		);

		// any condition
		Predicate<Integer> equal2 = new Equal<Integer>();
		// integer3 has to bind in any condition
		BindingPredicate<Integer> bindEqual2 = new BindingPredicate<Integer>(equal2, Arrays.asList(0));
		// any predicate will called as invoke(Integer2), so nothing has to bind in the any condition (value -1)
		Predicate<Integer> anyPred = new AnyPredicate<Integer>(sub, bindEqual2, Arrays.asList(-1));
		// filter in subquery of cursor2
		Filter<Integer> sel2 = new Filter<Integer>(cursor2, anyPred);

		// subquery where Integer3 has to bind in inner subquery and any condition
		Subquery<Integer> sub2 = new Subquery<Integer>(
			sel2,
			Arrays.asList(
				sub,
				bindEqual2
			),
			new int[][] {
				new int[] {1},
				new int[] {0}
			}
		);

		// exist predicate of the outer subquery
		Predicate<Integer> exist0 = new ExistPredicate<Integer>(sub2);
		// filter of cursor3
		Filter<Integer> cursor = new Filter<Integer>(cursor3, exist0);

		// cursor output
		System.out.println("Cursor: result");
		Cursors.println(cursor);
	}

}
