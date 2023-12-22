package xxl.tests.colt;

import xxl.connectivity.colt.ColtDiscreteRandomWrapper;
import xxl.core.util.random.DiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ColtDiscreteRandomWrapper.
 */
public class TestColtDiscreteRandomWrapper {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */

	public static void main(String[] args) {
		DiscreteRandomWrapper drw = new ColtDiscreteRandomWrapper();
		for (int i = 0; i < 50; i++)
			System.out.println(i+"-th discrete random number=" + drw.nextInt());
		System.out.println("----------------------------------");
	}

}
