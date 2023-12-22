package xxl.tests.math.statistics.nonparametric.kernels;

import xxl.core.math.statistics.nonparametric.kernels.GaussianKernel;
import xxl.core.math.statistics.nonparametric.kernels.KernelFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class GaussianKernel.
 */
public class TestGaussianKernel {

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

		KernelFunction k = new GaussianKernel();
		xxl.core.math.functions.RealFunction devk = ((xxl.core.math.functions.Differentiable) k).derivative();

		int steps = 100;
		double min = -4.0;
		double max = 4.0;
		double x = 0.0;
		System.out.println("# x \t gau(x) \t gau'(x)");
		for (int i = 0; i <= steps; i++) {
			x = min + (max - min) * i / steps;
			System.out.println(x + "\t" + k.eval(x) + "\t" + devk.eval(x));
		}
	}

}
