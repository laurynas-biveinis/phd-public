package xxl.tests.cursors.filters;

import xxl.core.cursors.filters.Dropper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Dropper.
 */
public class TestDropper {
	
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
		
		Dropper<Integer> dropper = new Dropper<Integer>(new xxl.core.cursors.sources.Enumerator(11), 5);
		
		dropper.open();
		
		while(dropper.hasNext())
			System.out.println(dropper.next());
		
		dropper.close();
	}

}
