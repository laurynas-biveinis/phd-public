package xxl.tests.math.statistics.nonparametric.kernels;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.math.statistics.nonparametric.kernels.KernelBasedBlockEstimatorAggregationFunction;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class KernelBasedBlockEstimatorAggregationFunction.
 */
public class TestKernelBasedBlockEstimatorAggregationFunction {

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
		int blockSize = 100;
		int N = 2000;
		xxl.core.cursors.Cursor cursor = new ContinuousRandomNumber(new JavaContinuousRandomWrapper(), N);
		Aggregator aggregator =
			new Aggregator(
				KernelBasedBlockEstimatorAggregationFunction.inputCursor(cursor, blockSize), 
				new KernelBasedBlockEstimatorAggregationFunction()
			);
		int c = 0;
		java.util.List l = new java.util.ArrayList(N / blockSize);
		double[] grid = xxl.core.util.DoubleArrays.equiGrid(0.0, 1.0, 100);
		Object last = null;
		while (aggregator.hasNext()) {
			Object next = aggregator.next();
			if (!(last == next)) {
				System.err.println("c:" + (++c));
				if (N % blockSize == 0) {
					l.add(xxl.core.math.Statistics.evalRealFunction(grid, (xxl.core.math.functions.RealFunction) next));
				}
				last = next;
			}
		}
		// --- output ---
		for (int i = 0; i < grid.length; i++) {
			System.out.print(grid[i] + "\t");
			for (int j = 0; j < l.size(); j++) {
				System.out.print(((double[]) l.get(j))[i] + "\t");
			}
			System.out.println();
		}
	}

}
