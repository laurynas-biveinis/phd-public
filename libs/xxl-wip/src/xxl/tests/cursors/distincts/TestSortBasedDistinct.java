package xxl.tests.cursors.distincts;

import xxl.core.cursors.distincts.SortBasedDistinct;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SortBasedDistinct.
 */
public class TestSortBasedDistinct {

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
		
		SortBasedDistinct<Integer> distinct = new SortBasedDistinct<Integer>(
			new xxl.core.cursors.sources.ArrayCursor<Integer>(
				new Integer[] {1, 1, 2, 2, 3, 4, 4, 4, 5, 6 }
			)
		);

		distinct.open();
		
		while (distinct.hasNext())
			System.out.println(distinct.next());
			
		distinct.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		System.out.println("\nEarly duplicate elimination: ");
		
		distinct = new SortBasedDistinct<Integer>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(10), 20),
			xxl.core.comparators.ComparableComparator.INTEGER_COMPARATOR,
			8,
			12*4096,
			4*4096
		);
		
		distinct.open();
		
		while (distinct.hasNext())
			System.out.println(distinct.next());
			
		distinct.close();
	}

}
