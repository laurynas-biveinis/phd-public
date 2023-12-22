package xxl.tests.math.statistics.nonparametric.kernels;

import xxl.core.math.statistics.nonparametric.kernels.KernelFunction;
import xxl.core.math.statistics.nonparametric.kernels.UniformKernel;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UniformKernel.
 */
public class TestUniformKernel {

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

		KernelFunction u = new UniformKernel();
		xxl.core.math.functions.RealFunction intu = ((xxl.core.math.functions.Integrable) u).primitive();

		int steps = 100;
		double min = -1.0;
		double max = 1.0;
		double x = 0.0;
		System.out.println("# x \t u(x) \t int(u(x))dx");
		for (int i = 0; i <= steps; i++) {
			x = min + (max - min) * i / steps;
			System.out.println(x + "\t" + u.eval(x) + "\t" + intu.eval(x));
		}
	}

}
