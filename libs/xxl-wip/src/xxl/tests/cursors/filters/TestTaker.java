package xxl.tests.cursors.filters;

import xxl.core.cursors.filters.Taker;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Taker.
 */
public class TestTaker {
	
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
		
		Taker<Integer> taker = new Taker<Integer>(new xxl.core.cursors.sources.Enumerator(11), 5);
		
		taker.open();
		
		while (taker.hasNext())
			System.out.print(taker.next() +"; ");
		System.out.flush();
		
		taker.close();
	}

}
