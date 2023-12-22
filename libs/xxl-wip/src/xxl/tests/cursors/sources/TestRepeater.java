package xxl.tests.cursors.sources;

import xxl.core.cursors.sources.Repeater;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Repeater.
 */
public class TestRepeater {

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
		
		Repeater<Integer> repeater = new Repeater<Integer>(new Integer(1));
		
		repeater.open();
		
		for (int i = 0; i < 10; i++)
			System.out.print(repeater.next() + "; ");
		System.out.flush();
		System.out.println();
		
		repeater.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		repeater = new Repeater<Integer>(new Integer(1), 10);
		
		repeater.open();
		
		while (repeater.hasNext())
			System.out.print(repeater.next() + "; ");
		System.out.flush();
		System.out.println();
		
		repeater.close();
	}

}
