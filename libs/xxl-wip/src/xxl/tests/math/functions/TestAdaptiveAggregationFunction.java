package xxl.tests.math.functions;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.functions.Function;
import xxl.core.functions.Functions;
import xxl.core.math.functions.AbstractRealFunctionFunction;
import xxl.core.math.functions.AdaptiveAggregationFunction;
import xxl.core.math.functions.AdaptiveWeightFunctions;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.functions.RealFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AdaptiveAggregationFunction.
 */
public class TestAdaptiveAggregationFunction {

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

		/* This example shows how an adaptive online aggregation function could be used:
		   1) Iterator <TT>it</TT> delivers n real-valued numbers from a to b
		   2) Every <TT>blockSize</TT> numbers a NEW estimator for the interval seen so far will be established
		      by the aggregation Function af
		   3) The AdaptiveAggregationFunction will be constructed and performed
		   4) The LAST combined function will be evaluated to standard our in GNUPLOT like format
		      Catch the output ( e.g. by java ..... >out.dat) and display it in GNUPLOT with
		      'plot out.dat using 1:2 with lines'!
		      Some debug informations will be given too!
		*/

		final int n = 1000;
		final double a = -1.0;
		final double b = 1.0;
		final int blockSize = 100;
		java.util.Iterator it = new java.util.Iterator() {
			int c = 0;
			public boolean hasNext() {
				return c <= n ? true : false;
			}
			public Object next() {
				if (hasNext())
					return new Double(a + (c++) * (b - a) / (n - 1));
				else
					throw new java.util.NoSuchElementException("no further numbers available!");
			}
			public void remove() {
				throw new UnsupportedOperationException("not supported!");
			}
		};
		AggregationFunction af = new AggregationFunction<Object,Object>() {
			int c = 0;
			int block = 0;
			Function maxF =
				Functions.aggregateUnaryFunction(new xxl.core.math.statistics.parametric.aggregates.Maximum());
			Function minF =
				Functions.aggregateUnaryFunction(new xxl.core.math.statistics.parametric.aggregates.Minimum());
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			public Object invoke(Object old, Object next) {
				c++;
				min = ((Double) minF.invoke(next)).doubleValue();
				max = ((Double) maxF.invoke(next)).doubleValue();
				if (c % blockSize == 0) {
					// new Block? -> building up a new function
					maxF =
						Functions.aggregateUnaryFunction(new xxl.core.math.statistics.parametric.aggregates.Maximum());
					minF =
						Functions.aggregateUnaryFunction(new xxl.core.math.statistics.parametric.aggregates.Minimum());
					block++;
					return new AbstractRealFunctionFunction() { // object of type Function AND RealFunction -> switch realMode could be true OR false
						final int b = block;
						final double mi = min;
						final double ma = max;
						public double eval(double x) {
							return ((x >= mi) & (x <= ma)) ? xxl.core.math.Statistics.cosineArch(x) : 0.0;
						}
						public String toString() {
							return ("estimator for block #" + b + " supporting [" + mi + "," + ma + "]");
						}
					};
				}
				else
					return old;
			}
		};

		/* Here the AdaptiveAggregationFunction will be constructed. */

		xxl.core.cursors.mappers.Aggregator ag = 
			new Aggregator(
				it, // input-iterator
				new AdaptiveAggregationFunction(af, // wrapped aggregation function
				new AdaptiveWeightFunctions.GeometricWeights(2.0), // weight-function
				true // enable real mode
			));
		// That's all ... now evaluate the last returned adaptive estimation function
		RealFunction r = (RealFunction) ag.last();
		// evaluating on this grid
		double[] grid = xxl.core.util.DoubleArrays.equiGrid(a, b, n);
		double[] e = xxl.core.math.Statistics.evalRealFunction(grid, r);
		System.err.println("# Last function : " + r);
		for (int i = 0; i < e.length; i++)
			System.out.println(grid[i] + "\t" + e[i]);
	}

}
