package xxl.tests.math.statistics.nonparametric.kernels;

import java.util.Arrays;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.functions.RealFunctions;
import xxl.core.math.statistics.nonparametric.kernels.BiweightKernel;
import xxl.core.math.statistics.nonparametric.kernels.HybridKernelDensityEstimator;
import xxl.core.util.DoubleArrays;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.random.InversionDistributionBasedPRNG;
import xxl.core.util.random.JavaContinuousRandomWrapper;
import xxl.core.util.random.RejectionDistributionBasedPRNG;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class HybridKernelDensityEstimator.
 */
public class TestHybridKernelDensityEstimator {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		/*********************************************************************/
		/*                            Testing split                          */
		/*********************************************************************/
		Object[] data =
			new Object[] {
				new Double(2.0),
				new Double(4),
				new Double(5.5),
				new Double(-2.0),
				new Double(6),
				new Double(2),
				new Double(-0.2),
				new Double(8),
				new Double(20)
			};
		System.out.println("data:");
		System.out.println(Arrays.toString(data));
		Object[] borders = new Object[] { new Double(-10), new Double(2), new Double(0.0), new Double(10)};
		System.out.println("borders:");
		System.out.println(Arrays.toString(borders));
		System.out.println("-------------------------");
		Object[] splits = HybridKernelDensityEstimator.split(data, borders);
		for (int i = 0; i < splits.length; i++)
			System.out.println("split " + i + ":" + Arrays.toString((Object[]) splits[i]));

		/*********************************************************************/
		/*                            Testing Initialization of dke          */
		/*********************************************************************/

		RealFunction f = RealFunctions.pdfCP01();
		double c = 1.8;
		int n = 200;

		ContinuousRandomWrapper jcrw = new JavaContinuousRandomWrapper();
		ContinuousRandomWrapper g = new InversionDistributionBasedPRNG(jcrw, RealFunctions.invDistCont01());
		RejectionDistributionBasedPRNG rb =
			new RejectionDistributionBasedPRNG(jcrw, f, RealFunctions.pdfCont01(), c, g);
		java.util.Iterator iterator = new ContinuousRandomNumber(rb, n);

		Object[] sample = new Object[n];
		Object[] changePoints = new Object[] { new Double(0.29999)};
		double[] hs = null;
		double min = 0.0;
		double max = 1.0;
		double alpha = -1.0;
		Cursors.toArray(iterator, sample);
		HybridKernelDensityEstimator hkde =
			new HybridKernelDensityEstimator(new BiweightKernel(), sample, changePoints, hs, min, max, alpha);

		java.util.Iterator x = DoubleArrays.realGrid(new double[] { 0.3 }, new double[] { max }, 1000);
		System.out.println("# x ->\t kde_h(x))");
		while (x.hasNext()) {
			double t = ((double[]) x.next())[0];
			System.out.println(t + "\t" + hkde.eval(t));
		}
	}

}
