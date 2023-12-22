package xxl.tests.cursors.groupers;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.groupers.HashGrouper;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class HashGrouper.
 */
public class TestHashGrouper {

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
		
		HashGrouper<Integer> hashGrouper = new HashGrouper<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			new AbstractFunction<Integer, Integer>() {
				public Integer invoke(Integer next) {
					return next % 5;
				}
			}
		);
		
		hashGrouper.open();
		
		while (hashGrouper.hasNext()) {
			Cursor<Integer> bucket = hashGrouper.next();
			// a cursor pointing to next group
			while (bucket.hasNext())
				System.out.print(bucket.next() + "; ");
			System.out.flush();
			System.out.println();
		}
		
		hashGrouper.close();
	}

}
