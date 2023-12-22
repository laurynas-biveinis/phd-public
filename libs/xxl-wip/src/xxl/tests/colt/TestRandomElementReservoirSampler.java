package xxl.tests.colt;

import java.util.Iterator;

import xxl.connectivity.colt.RandomElementReservoirSampler;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RandomElementReservoirSampler.
 */
public class TestRandomElementReservoirSampler {

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

		Iterator it = 
			new RandomElementReservoirSampler(
				new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 50),
				10,
				1
			);
		int c = 0;
		while (it.hasNext()) {
			Object[] o = (Object[]) it.next();
			if(o==null) continue;
			System.out.print(c++);
			for (int i = 0; i < o.length; i++)
				System.out.print(": " + o[i]);
			System.out.println();
		}
		System.out.println("---------------------------------");
	}

}
