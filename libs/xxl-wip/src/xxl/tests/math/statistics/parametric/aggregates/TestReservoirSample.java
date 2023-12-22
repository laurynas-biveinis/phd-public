package xxl.tests.math.statistics.parametric.aggregates;

import java.util.Iterator;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.math.statistics.parametric.aggregates.ReservoirSample;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ReservoirSample.
 */
public class TestReservoirSample {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example                                */
		/*********************************************************************/
		Iterator it = new Aggregator(new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(100), 50), new ReservoirSample(10, new ReservoirSample.XType(10)));
		int c = 0;
		while (it.hasNext()) {
			Object[] o = (Object[]) it.next();
			if (o == null)
				continue;
			System.out.print(c++);
			for (int i = 0; i < o.length; i++)
				System.out.print(": " + o[i]);
			System.out.println();
		}
		System.out.println("---------------------------------");
	}

}
