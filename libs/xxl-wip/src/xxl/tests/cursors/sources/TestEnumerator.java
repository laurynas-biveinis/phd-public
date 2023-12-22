package xxl.tests.cursors.sources;

import xxl.core.cursors.sources.Enumerator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Enumerator.
 */
public class TestEnumerator {

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
		
		Enumerator enumerator = new Enumerator(0, 11);
		
		enumerator.open();
		
		while (enumerator.hasNext())
			System.out.println(enumerator.next());
		
		enumerator.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		enumerator = new Enumerator(10, -1);
		
		enumerator.open();
		
		while (enumerator.hasNext())
			System.out.println(enumerator.next());
		
		enumerator.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		
		enumerator = new Enumerator(11);
		
		enumerator.open();
		
		while (enumerator.hasNext())
			System.out.println(enumerator.next());
		
		enumerator.close();
	}

}
