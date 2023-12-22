package xxl.tests.cursors.sorters;

import java.util.Random;

import xxl.core.cursors.sorters.ShuffleCursor;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ShuffleCursor.
 */
public class TestShuffleCursor {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		ShuffleCursor<Integer> shuffler = new ShuffleCursor<Integer>(
			new xxl.core.cursors.sources.Enumerator(11),
			new DiscreteRandomNumber(
				new JavaDiscreteRandomWrapper(
					new Random()
				)
			)
		);
		
		shuffler.open();
		
		while (shuffler.hasNext())
			System.out.println(shuffler.next());
		
		shuffler.close();
	}

}
