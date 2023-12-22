package xxl.tests.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.math.statistics.parametric.aggregates.StatefulAverage;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class StatefulAverage.
 */
public class TestAverage {

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
		Aggregator aggregator =	new Aggregator<Number,Number>(
			new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(100), 50), // input-Cursor
			new StatefulAverage() // aggregate function
		);
		System.out.print(
			"The result of the average aggregation is: " + aggregator.last());
		aggregator.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		Aggregator aggregator2 = new Aggregator<Number,Number>(
			xxl.core.cursors.sources.Inductors.naturalNumbers(1, 100), // input-Cursor
			new StatefulAverage() // aggregate function
		);
		System.out.println("\nThe result of the average aggregation of the natural numbers from 1 to 100 is: "
				+ aggregator2.last());
		aggregator2.close();
	}

}
