package xxl.tests.math.statistics.nonparametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.mappers.ReservoirSampler;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.statistics.nonparametric.aggregates.EmpiricalCDFAggregateFunction;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class EmpiricalCDFAggregateFunction.
 */
public class TestEmpiricalCDFAggregateFunction {

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
		System.out.println("Computing empirical cdf iteratively over a Cursor with random number based on reservoir sampling:");
		int N = 2000;
		xxl.core.cursors.Cursor cursor = new ContinuousRandomNumber(new JavaContinuousRandomWrapper(), N);
		Aggregator aggregator =
			new Aggregator(
				new Aggregator(
					cursor,
					xxl.core.math.statistics.nonparametric.aggregates.Aggregators.mapSamplingStrategy(
						100,
						ReservoirSampler.RTYPE
					)
				),
				new EmpiricalCDFAggregateFunction()
			);
		double[] grid = xxl.core.util.DoubleArrays.equiGrid(0.0, 1.0, 100);
		RealFunction last = (RealFunction) aggregator.last();
		// --- output ---
		System.out.println("");
		System.out.println("Evaluating resulting empirical cdf:");
		for (int i = 0; i < grid.length; i++) {
			System.out.print(grid[i] + "\t" + " " + last.eval(grid[i]));
			System.out.println();
		}
	}

}
