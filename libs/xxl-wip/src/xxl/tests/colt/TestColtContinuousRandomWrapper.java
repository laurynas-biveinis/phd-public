package xxl.tests.colt;

import xxl.connectivity.colt.ColtContinuousRandomWrapper;
import xxl.core.util.random.ContinuousRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ColtContinuousRandomWrapper.
 */
public class TestColtContinuousRandomWrapper {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		ContinuousRandomWrapper drw = new ColtContinuousRandomWrapper();
		for (int i = 0; i < 50; i++)
			System.out.println(i + "-th continuous random number=" + drw.nextDouble());
		System.out.println("----------------------------------");
	}

}
