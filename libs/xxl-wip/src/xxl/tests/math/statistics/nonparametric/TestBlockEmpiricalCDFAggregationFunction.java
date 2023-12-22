package xxl.tests.math.statistics.nonparametric;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.statistics.nonparametric.BlockEmpiricalCDFAggregationFunction;
import xxl.core.math.statistics.nonparametric.EmpiricalCDF;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BlockEmpiricalCDFAggregationFunction.
 */
public class TestBlockEmpiricalCDFAggregationFunction {
	
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
		int N = 20000;
		xxl.core.cursors.Cursor data = new ContinuousRandomNumber(new JavaContinuousRandomWrapper(), N);
		data.open();
		Aggregator aggregator =
			new Aggregator<Double[],EmpiricalCDF>(
					BlockEmpiricalCDFAggregationFunction.inputCursor(data, blockSize), 
				new BlockEmpiricalCDFAggregationFunction()
			);
		RealFunction ecdf=(RealFunction)aggregator.last();
		data.close();
		
		double[] grid = xxl.core.util.DoubleArrays.equiGrid(0.0, 1.0, 100);
		double[] values=xxl.core.math.Statistics.evalRealFunction(grid, ecdf);
		System.out.print(
			"Evaluating empirical cdf based on the last sample block"
			+"\n"+"\n"+"x:"+"\t"+"f(x):"+"\n"
			);
		for(int i=0;i<grid.length;i++) {
			System.out.println(grid[i]+"\t"+values[i]);
		}
	}

}
