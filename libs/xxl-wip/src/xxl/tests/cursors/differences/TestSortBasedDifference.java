package xxl.tests.cursors.differences;

import xxl.core.cursors.differences.SortBasedDifference;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SortBasedDifference.
 */
public class TestSortBasedDifference {

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
		
		SortBasedDifference<Integer> difference = new SortBasedDifference<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			//new xxl.core.cursors.sources.EmptyCursor<Integer>(),
			new xxl.core.cursors.filters.Filter<Integer>(
				new xxl.core.cursors.sources.Enumerator(21),
				new xxl.core.predicates.AbstractPredicate<Integer>() {
					public boolean invoke(Integer next) {
						return next % 2 == 0;
					}
				}
			),
			xxl.core.comparators.ComparableComparator.INTEGER_COMPARATOR,
			true,
			true
		);
		
		difference.open();

		while (difference.hasNext())
			System.out.println(difference.next());
		
		difference.close();

		System.out.println();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		difference = new SortBasedDifference<Integer>(
			new xxl.core.cursors.sources.ArrayCursor<Integer>(1, 2, 2, 2, 3),
			new xxl.core.cursors.sources.ArrayCursor<Integer>(1, 2, 2, 3),
			xxl.core.comparators.ComparableComparator.INTEGER_COMPARATOR,
			false,
			true
		);
		
		difference.open();
		
		while (difference.hasNext())
			System.out.println(difference.next());
		
		difference.close();
	}

}
