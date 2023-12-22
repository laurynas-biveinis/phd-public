package xxl.tests.colt;

import xxl.connectivity.colt.RandomNumbers;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.cursors.sources.DiscreteRandomNumber;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RandomNumbers.
 */
public class TestRandomNumbers {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		ContinuousRandomNumber randomnumbers1 = RandomNumbers.gaussian(2.0, 1.0, 500);
		long i = 0;
		System.out.println("- gaussian distribution ------------------");
		while (randomnumbers1.hasNext()) {
			System.out.println((i++) + "\t" + randomnumbers1.next());
		}
		System.out.println("---------------------------------------");
		randomnumbers1.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		DiscreteRandomNumber randomnumbers2 = RandomNumbers.binomial(20, 0.4, 500);
		i = 0;
		System.out.println("- binomial distribution ---------------");
		while (randomnumbers2.hasNext()) {
			System.out.println((i++) + "\t" + randomnumbers2.next());
		}
		System.out.println();
		System.out.println("---------------------------------------");
		randomnumbers2.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		DiscreteRandomNumber randomnumbers3 = RandomNumbers.duniform(1, 100, 1000);
		i = 0;
		System.out.println("- discrete uniform distribution -------");
		while (randomnumbers3.hasNext()) {
			System.out.println((i++) + "\t" + randomnumbers3.next());
		}
		System.out.println();
		System.out.println("---------------------------------------");
		randomnumbers3.close();

		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		ContinuousRandomNumber randomnumbers4 = RandomNumbers.cuniform(0.0, 1.0);
		i = 0;
		System.out.println("- continuous uniform distribution -------");
		while (i < 100) {
			System.out.println((i++) + "\t" + randomnumbers4.next());
		}
		System.out.println();
		System.out.println("---------------------------------------");
		randomnumbers4.close();
	}

}
