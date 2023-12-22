package xxl.tests.math.statistics.nonparametric.kernels;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.math.functions.RealFunctions;
import xxl.core.math.statistics.nonparametric.kernels.BiweightKernel;
import xxl.core.math.statistics.nonparametric.kernels.HybridKernelCDF;
import xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths;
import xxl.core.math.statistics.nonparametric.kernels.ReflectionKernelCDF;
import xxl.core.util.DoubleArrays;
import xxl.core.util.random.InversionDistributionBasedPRNG;
import xxl.core.util.random.JavaContinuousRandomWrapper;
import xxl.core.util.random.RejectionDistributionBasedPRNG;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class HybridKernelCDF.
 */
public class TestHybridKernelCDF {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		/*********************************************************************/
		/*                            Testing                                */
		/*********************************************************************/

		int n = 1000;
		java.util.Iterator iterator =
			new ContinuousRandomNumber(
				new RejectionDistributionBasedPRNG(
					new JavaContinuousRandomWrapper(),
					RealFunctions.pdfCP01(),
					RealFunctions.pdfCont02(),
					2.4,
					new InversionDistributionBasedPRNG(
						new JavaContinuousRandomWrapper(),
						RealFunctions.invDistCont02())
				),n
			);

		Object[] sample = new Object[n];
		Object[] changePoints = new Object[] { new Double(0.33333)};
		double min = 0.0;
		double max = 1.0;
		int h = KernelBandwidths.DPI2_RULE_1D;
		Cursors.toArray(iterator, sample);
		HybridKernelCDF hcdf =
			new HybridKernelCDF(new BiweightKernel(), sample, h, min, max, changePoints, ReflectionKernelCDF.FACTORY);

		java.util.Iterator x = DoubleArrays.realGrid(new double[] { min }, new double[] { max }, 1000);
		System.out.println("# hType = " + h);
		System.out.println("# x ->\t hcdf_h(x))\tq(x-01,x)");
		while (x.hasNext()) {
			double t = ((double[]) x.next())[0];
			System.out.println(t + "\t" + hcdf.eval(t) + "\t" + hcdf.windowQuery(new Double(t - 0.1), new Double(t)));
		}
	}

}
