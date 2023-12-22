package xxl.tests.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.math.statistics.parametric.aggregates.Minimum;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Minimum.
 */
public class TestMinimum {

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
		Aggregator aggregator = 
			new Aggregator(
				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(100), 50), //  input-Cursor
				new Minimum() // aggregate function
			);
		while (aggregator.hasNext())
			System.out.println("Current minimum is: " + aggregator.next());
		aggregator.close();
	}

}
