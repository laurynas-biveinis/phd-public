package xxl.tests.cursors.groupers;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.groupers.SortBasedGrouper;
import xxl.core.predicates.AbstractPredicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SortBasedGrouper.
 */
public class TestSortBasedGrouper {

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
		
		SortBasedGrouper<Integer> sortBasedGrouper = new SortBasedGrouper<Integer>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 50),
			new AbstractPredicate<Integer>() {// the predicate used for comparison
				public boolean invoke(Integer previous, Integer next) {
					return previous > next;
				}
			}
		);

		sortBasedGrouper.open();
		
		Cursor<Integer> sequence = null;
		while (sortBasedGrouper.hasNext()) {
			sequence = sortBasedGrouper.next();
			// a cursor pointing to the next group
			while (sequence.hasNext())
				System.out.print(sequence.next() + "; ");
			System.out.flush();
			System.out.println();
		}
		
		sortBasedGrouper.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		sortBasedGrouper = new SortBasedGrouper<Integer>(
			new xxl.core.cursors.sources.Enumerator(0, 100),
			new AbstractPredicate<Integer>() {
				public boolean invoke(Integer previous, Integer next) {
					return previous/10 != next/10;
				}
			}
		);

		sortBasedGrouper.open();

		sequence = null; 
		while (sortBasedGrouper.hasNext()) {
			sequence = sortBasedGrouper.next();
			// a cursor pointing to the next group
			while (sequence.hasNext()) 
				System.out.print(sequence.next() + "; ");
			System.out.flush();
			System.out.println();
		}
		
		sortBasedGrouper.close();
	}

}
