package xxl.tests.cursors.groupers;

import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.groupers.ReplacementSelection;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ReplacementSelection.
 */
public class TestReplacementSelection {

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
		
		ReplacementSelection<Integer> cursor = new ReplacementSelection<Integer>(
			new xxl.core.cursors.sources.Enumerator(11),
			3,
			ComparableComparator.INTEGER_COMPARATOR
		);
		
		cursor.open();
		
		while (cursor.hasNext())
			System.out.print(cursor.next() + "; ");
		System.out.flush();
		System.out.println();
		
		cursor.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		cursor = new ReplacementSelection<Integer>(
			new xxl.core.cursors.sources.Permutator(20),
			3,
			ComparableComparator.INTEGER_COMPARATOR
		);
		
		cursor.open();
		
		int last = 0;
		boolean first = true;
		while (cursor.hasNext())
			if (last > cursor.peek() || first) {
				System.out.println();
				System.out.print(" Run: ");
				last = cursor.next();
				System.out.print(last + "; ");
				first = false;
			}
			else {
				last = cursor.next();
				System.out.print(last + "; ");
			}
		System.out.flush();
		
		cursor.close();
	}

}
