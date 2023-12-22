package xxl.tests.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.math.statistics.parametric.aggregates.StatefulVarianceEstimator;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class StatefulVarianceEstimator.
 */
public class TestVarianceEstimator {

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
		Aggregator aggregator1 = 
			new Aggregator(
				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(100), 50), // the input-Cursor
				new StatefulVarianceEstimator() // aggregate functions
			);
		Object l1 = aggregator1.last();
		aggregator1.close();
		System.out.println(
			"The result of the sample variance aggregation of 100 randomly distributed integers is: " + l1);

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		Aggregator aggregator2 =
			new Aggregator(
				xxl.core.cursors.sources.Inductors.naturalNumbers(1, 100), // the input-Cursor
				new StatefulVarianceEstimator() // aggregate function
			);
		System.out.println(
			"\nThe result of the sample variance aggregation of the natural numbers from 1 to 100 is: "
				+ aggregator2.last());
		aggregator2.close();
	}

}
