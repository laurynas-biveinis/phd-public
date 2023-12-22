package xxl.tests.cursors.differences;

import java.util.Iterator;

import xxl.core.collections.bags.ListBag;
import xxl.core.cursors.differences.NestedLoopsDifference;
import xxl.core.functions.AbstractFunction;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Equal;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NestedLoopsDifference.
 */
public class TestNestedLoopsDifference {

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
		
		NestedLoopsDifference<Integer> difference = new NestedLoopsDifference<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			//new xxl.core.cursors.sources.EmptyCursor<Integer>(),
			new xxl.core.cursors.filters.Filter<Integer>(
				new xxl.core.cursors.sources.Enumerator(21),
				new AbstractPredicate<Integer>() {
					public boolean invoke(Integer next) {
						return next % 2 == 0;
					}
				}
			),
			32,
			8,
			new AbstractFunction<Object, ListBag<Integer>>() {
				public ListBag<Integer> invoke() {
					return new ListBag<Integer>();
				}
			},
			Equal.DEFAULT_INSTANCE,
			false
		);
		
		difference.open();
		
		while (difference.hasNext())
			System.out.println(difference.next());
		
		difference.close();

		System.out.println();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/

		difference = new NestedLoopsDifference<Integer>(
			java.util.Arrays.asList(1, 2, 3, 4).iterator(),
			java.util.Arrays.asList(1, 2, 3).iterator(),
			32,
			8,
			new AbstractFunction<Object, Iterator<Integer>>() {
				public Iterator<Integer> invoke() {
					return java.util.Arrays.asList(1, 2, 3).iterator();
				}
			},
			false
		);
		
		difference.open();
		
		while (difference.hasNext())
			System.out.println(difference.next());
		
		difference.close();
	}

}
