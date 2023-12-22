package xxl.tests.cursors;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.ResetableCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ResetableCursor.
 */
public class TestResetableCursor {

	/**
	 * The main method contains some examples how to use a resetable cursor. It
	 * can also be used to test the functionality of a resetable cursor.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		xxl.core.cursors.sources.DiscreteRandomNumber randomNumbers = new xxl.core.cursors.sources.DiscreteRandomNumber(
			new xxl.core.util.random.JavaDiscreteRandomWrapper(),
			10
		);
		
		ResetableCursor<Integer> bufferedCursor = new ResetableCursor<Integer>(randomNumbers);
		
		bufferedCursor.open();
		System.out.println("get 5 elements:");
		for (int i = 1; i < 5 && bufferedCursor.hasNext(); i++)
			System.out.println(bufferedCursor.next());
		
		System.out.println("reset buffered cursor, and get all elements:");
		bufferedCursor.reset();
		Cursors.println(bufferedCursor);
		
		System.out.println("reset buffered cursor, and get all elements:");
		bufferedCursor.reset();
		Cursors.println(bufferedCursor);
		
		bufferedCursor.close();
	}

}
