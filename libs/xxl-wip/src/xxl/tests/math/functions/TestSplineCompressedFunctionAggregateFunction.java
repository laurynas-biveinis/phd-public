package xxl.tests.math.functions;

import java.util.Iterator;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.functions.Functions;
import xxl.core.math.Maths;
import xxl.core.math.Statistics;
import xxl.core.math.functions.AdaptiveAggregationFunction;
import xxl.core.math.functions.AdaptiveWeightFunctions;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.functions.SplineCompressedFunctionAggregateFunction;
import xxl.core.math.statistics.nonparametric.aggregates.NKDEAggregateFunction;
import xxl.core.math.statistics.nonparametric.kernels.EpanechnikowKernel;
import xxl.core.math.statistics.parametric.aggregates.Maximum;
import xxl.core.math.statistics.parametric.aggregates.Minimum;
import xxl.core.math.statistics.parametric.aggregates.ReservoirSample;
import xxl.core.math.statistics.parametric.aggregates.StatefulVariance;
import xxl.core.util.DoubleArrays;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SplineCompressedFunctionAggregateFunction.
 */
public class TestSplineCompressedFunctionAggregateFunction {

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
		// estimate the density function of a uniformly distributed entirety of
		// double values between 0 and 20

		Iterator it = 
			new Mapper(
				new AbstractFunction() {
					protected double span = 1000.0;
					public Object invoke(Object o) {
						return new Double(((Number) o).doubleValue() / span);
					}
				},new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(20000), 100000)
			);
		int sampleSize = 1000;
		final double left = 0.0;
		final double right = 20.0;
		final int nr = 10;
		Aggregator agg =
			new Aggregator(
				new Aggregator(
					it,
					Maths.multiDimAggregateFunction(new AggregationFunction[] {
						new ReservoirSample(sampleSize, new ReservoirSample.XType(sampleSize)),
						new StatefulVariance()
					}
				)),
				new SplineCompressedFunctionAggregateFunction(
					new NKDEAggregateFunction(
						new EpanechnikowKernel()
					),
					left, right, nr, false
				)
			);

		Function f = null;
		double[] grid = DoubleArrays.equiGrid(left, right, (int) (2.3 * nr));
		// 2.3 * n => eval. points don't fall on Bezier coefficients
		int c = 0;
		while (agg.hasNext()) {
			f = (Function) agg.next();
			if (f != null) {
				if (((++c) % 100) == 0)
					System.out.println(
						"step " + c + " = " + java.util.Arrays.toString(Statistics.evalReal1DFunction(grid, f)));
			}
		}
		System.out.println("step " + c + " (last) = " + java.util.Arrays.toString(Statistics.evalReal1DFunction(grid, f)));

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		// combination of an AdaptiveAggregationFunction and SplineCompressedFunctionAggregateFunction
		final int n = 1000;
		final double a = -1.0;
		final double b = 1.0;
		final int blockSize = 100;
		it = new java.util.Iterator() {
			int c = 0;
			public boolean hasNext() {
				return c <= n ? true : false;
			}
			public Object next() {
				if (hasNext())
					return new Double(a + (c++) * (b - a) / (n - 1));
				else
					throw new java.util.NoSuchElementException("No further numbers available!");
			}
			public void remove() {
				throw new UnsupportedOperationException("Not supported!");
			}
		};
		AggregationFunction af = new AggregationFunction() {
			int c = 0;
			int block = 0;
			Function maxF = Functions.aggregateUnaryFunction(new Maximum());
			Function minF = Functions.aggregateUnaryFunction(new Minimum());
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			public Object invoke(Object old, Object next) {
				c++;
				min = ((Double) minF.invoke(next)).doubleValue();
				max = ((Double) maxF.invoke(next)).doubleValue();
				if (c % blockSize == 0) {
					maxF = Functions.aggregateUnaryFunction(new Maximum());
					minF = Functions.aggregateUnaryFunction(new Minimum());
					block++;
					return new RealFunction() {
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
				} else
					return old;
			}
		};
		xxl.core.cursors.mappers.Aggregator ag =
			new xxl.core.cursors.mappers.Aggregator(
				it, 
				new SplineCompressedFunctionAggregateFunction(
					new AdaptiveAggregationFunction(
						af, 
						new AdaptiveWeightFunctions.GeometricWeights(2.0), 
						true
					),
					-1.0, 1.0, 10, false
				)
			);
		RealFunction r = null;
		while (ag.hasNext()) {
			r = (RealFunction) ag.next();
		}
		grid = xxl.core.util.DoubleArrays.equiGrid(a, b, n);
		double[] e = xxl.core.math.Statistics.evalRealFunction(grid, r);
		System.err.println("Last function : " + r);
		for (int i = 0; i < e.length; i++)
			System.out.println(grid[i] + "\t" + e[i]);

	}

}
