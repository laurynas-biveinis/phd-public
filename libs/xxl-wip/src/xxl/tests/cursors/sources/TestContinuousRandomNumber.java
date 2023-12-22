package xxl.tests.cursors.sources;

import xxl.core.cursors.sources.ContinuousRandomNumber;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ContinuousRandomNumber.
 */
public class TestContinuousRandomNumber {

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
		
		ContinuousRandomNumber crn = new ContinuousRandomNumber(
			new xxl.core.util.random.JavaContinuousRandomWrapper(),
			200
		);
		
		crn.open();
		
		for (int i = 0; crn.hasNext(); i++)
			System.out.println(i + "\t:\t" + crn.next());
		System.out.println();
		
		crn.close();
	}

}
