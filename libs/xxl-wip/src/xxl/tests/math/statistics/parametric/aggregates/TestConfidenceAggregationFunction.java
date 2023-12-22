package xxl.tests.math.statistics.parametric.aggregates;

import java.util.List;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.math.Maths;
import xxl.core.math.statistics.parametric.aggregates.ConfidenceAggregationFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ConfidenceAggregationFunction.
 */
public class TestConfidenceAggregationFunction {

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
		// Building Function-Array containing confidence-supporting aggregation functions
		ConfidenceAggregationFunction[] f =
			new ConfidenceAggregationFunction[] {
				ConfidenceAggregationFunction.largeSampleConfidenceVarianceEstimator(0.05),
				ConfidenceAggregationFunction.largeSampleConfidenceStandardDeviationEstimator(0.05),
				ConfidenceAggregationFunction.largeSampleConfidenceAverage(0.05),
				ConfidenceAggregationFunction.conservativeConfidenceAverage(0.05, 1.0, 100.0),
				ConfidenceAggregationFunction.deterministicConfidenceAverage(1.0, 100.0, 100),
				ConfidenceAggregationFunction.deterministicConfidenceSumEstimator(1.0, 100.0, 100),
				ConfidenceAggregationFunction.deterministicConfidenceVarianceEstimator(1.0, 100.0, 100),
				ConfidenceAggregationFunction.deterministicConfidencestandardDeviationEstimator(
					1.0,
					100.0,
					100),
					ConfidenceAggregationFunction.conservativeConfidenceVarianceEstimator(0.04, 1.0, 100.0, 100),
					ConfidenceAggregationFunction.conservativeConfidenceStandardDeviationEstimator(
					0.04,
					1.0,
					100.0,
					100),
					ConfidenceAggregationFunction.largeSampleConfidenceSumEstimator(0.05, 100)};
		// Building aggregator
		Aggregator aggregator =
			new Aggregator(
				xxl.core.cursors.sources.Inductors.naturalNumbers(1, 100), // the input-Cursor
				Maths.multiDimAggregateFunction(f) // aggregation functions
			);
		System.out.println(
			"Processing sorted natural numbers from 1 to 100 ...\n");
		// getting aggregation results
		List aggResult = (List) aggregator.last();
		// printing aggregation results
		for (int i = 0; i < aggResult.size(); i++) {
			String eps = "";
			try {
				Object[] eps2 = (Object[]) f[i].epsilon();
				for (int j = 0; j < eps2.length; j++)
					eps += eps2[j].toString() + "  ";
			} catch (ClassCastException cce) {
				eps = f[i].epsilon().toString();
			}
			System.out.println(
				f[i] + " --> " + aggResult.get(i) + " with epsilon ---> " + eps);
		}
	}

}
