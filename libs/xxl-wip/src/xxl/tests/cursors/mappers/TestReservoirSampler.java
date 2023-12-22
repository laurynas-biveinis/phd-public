package xxl.tests.cursors.mappers;

import xxl.core.cursors.mappers.ReservoirSampler;
import xxl.core.math.statistics.parametric.aggregates.ReservoirSample;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ReservoirSampler.
 */
public class TestReservoirSampler {

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

		ReservoirSampler sample = new ReservoirSampler(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 50),
			new ReservoirSample(
				10,
				new ReservoirSample.XType(10)
			)
		);
		
		sample.open();
		
		int c = 0;
		while (sample.hasNext()) {
			Object[] o = (Object [])sample.next();
			System.out.print(c++);
			if (o != null) {
				for (int i = 0; i < o.length; i++)
					System.out.print(": " + o[i]);
				System.out.println();
			}
			else
				System.out.println(": reservoir not yet initialized!");
		}
		System.out.println("---------------------------------");
	}

}
