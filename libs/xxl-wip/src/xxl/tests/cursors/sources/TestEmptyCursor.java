package xxl.tests.cursors.sources;

import xxl.core.cursors.sources.EmptyCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class EmptyCursor.
 */
public class TestEmptyCursor {
	
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
		
		EmptyCursor<Integer> emptyCursor = new EmptyCursor<Integer>();
		
		emptyCursor.open();
		
		System.out.println("Is a next element available? " + emptyCursor.hasNext());
		
		emptyCursor.close();
		
		System.out.println("Is a next element available? " + EmptyCursor.DEFAULT_INSTANCE.hasNext());
	}

}
