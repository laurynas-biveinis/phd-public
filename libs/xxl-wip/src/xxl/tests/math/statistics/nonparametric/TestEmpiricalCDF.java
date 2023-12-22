package xxl.tests.math.statistics.nonparametric;

import xxl.core.cursors.mappers.ReservoirSampler;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.statistics.nonparametric.EmpiricalCDF;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class EmpiricalCDF.
 */
public class TestEmpiricalCDF {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		RealFunction rf =
			new EmpiricalCDF(
				(Object[]) (new ReservoirSampler(new ContinuousRandomNumber(new JavaContinuousRandomWrapper(), 1000),
					100,
					xxl.core.cursors.mappers.ReservoirSampler.XTYPE)
					.last())
				);
		double[] grid = xxl.core.util.DoubleArrays.equiGrid(0.0, 1.0, 100);
		for (int i = 0; i < grid.length; i++)
			System.out.println(grid[i] + "\t" + rf.eval(grid[i]));
	}

}
