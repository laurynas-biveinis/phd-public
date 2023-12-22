package xxl.tests.cursors.sources;

import xxl.core.cursors.sources.DiscreteRandomNumber;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DiscreteRandomNumber.
 */
public class TestDiscreteRandomNumber {

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
		
		DiscreteRandomNumber drn = new DiscreteRandomNumber(
			new xxl.core.util.random.JavaDiscreteRandomWrapper(),
			200
		);
		
		drn.open();
		
		long i = 0;
		while (drn.hasNext())
			System.out.println((i++) + "\t:\t" + drn.next());
		System.out.println();
		
		drn.close();
	}

}
