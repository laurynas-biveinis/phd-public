package xxl.tests.cursors.groupers;

import xxl.core.collections.bags.Bag;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.groupers.NestedLoopsGrouper;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NestedLoopsGrouper.
 */
public class TestNestedLoopsGrouper {

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
		
		NestedLoopsGrouper<Integer> grouper = new NestedLoopsGrouper<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			new AbstractFunction<Integer, Integer>() {
				public Integer invoke(Integer next) {
					return next % 5;
				}
			},
			new java.util.TreeMap<Object, Bag<Integer>>(),
			32,
			4,
			8
		);

		grouper.open();
		
		while (grouper.hasNext()) {
			Cursor<Integer> nextGroup = grouper.next();
			System.out.print("Next group: ");
			while (nextGroup.hasNext())
				System.out.print(nextGroup.next() + " ");
			System.out.println();
		}
		
		grouper.close();
	}

}
