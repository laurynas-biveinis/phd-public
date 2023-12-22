package xxl.tests.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sorters.ShuffleCursor;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.cursors.sources.Inductors;
import xxl.core.math.statistics.parametric.aggregates.FourthCentralMomentEstimator;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FourthCentralMomentEstimator.
 */
public class TestFourthCentralMomentEstimator {

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
				new FourthCentralMomentEstimator() // aggregate function
			);
		System.out.println(
			"The result of the 4th central moment aggregation of 100 randomly distributed integers is: "
				+ aggregator1.last());
		aggregator1.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		Aggregator aggregator2 = 
			new Aggregator(
				xxl.core.cursors.sources.Inductors.naturalNumbers(1, 100), // the input-Cursor
			    new FourthCentralMomentEstimator() // aggregate function
			 );
		System.out.println(
			"\nThe result of the 4th central moment aggregation of the natural numbers from 1 to 100 is: "
				+ aggregator2.last());
		aggregator2.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		Aggregator aggregator3 =
			new Aggregator(
				new ShuffleCursor(Inductors.naturalNumbers(1, 100), // the input-Cursor
				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper())),
				new FourthCentralMomentEstimator() // aggregate function
			);

		System.out.println(
			"\nThe result of the 4th central moment aggregation of the natural numbers (shuffled) from 1 to 100 is: "
				+ aggregator3.last());
		aggregator3.close();
	}

}
