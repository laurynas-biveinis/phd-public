package xxl.tests.math.statistics.nonparametric.kernels;

import java.util.Iterator;

import xxl.core.math.statistics.nonparametric.kernels.KernelFunctionND;
import xxl.core.math.statistics.nonparametric.kernels.ProductKernelFunction;
import xxl.core.util.DoubleArrays;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ProductKernelFunction.
 */
public class TestProductKernelFunction {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		int example = 0;
		if ((args.length == 0))
			example = 1;
		else {
			if (args[0].startsWith("epa"))
				example = 1;
			if (args[0].startsWith("biw"))
				example = 2;
		}
		// ---
		switch (example) {
			case 1 :
				/*********************************************************************/
				/*                            Example 1                              */
				/*********************************************************************/
				// evaluate the epanechnikow product kernel from (-1,-1) to (1,1)
				Iterator realgrid1 =
					DoubleArrays.realGrid(new double[] { -1.0, -1.0 }, new double[] { 1.05, 1.05 }, 40);
				KernelFunctionND kf1 = new ProductKernelFunction.Epanechnikow(2);
				while (realgrid1.hasNext()) {
					double[] next = (double[]) realgrid1.next();
					System.out.println(java.util.Arrays.toString(next) + "\t" + kf1.eval(next));
				}
				break;
			case 2 :
				/*********************************************************************/
				/*                            Example 2                              */
				/*********************************************************************/
				// evaluate the biweight product kernel from (-1,-1) to (1,1)
				Iterator realgrid2 =
					DoubleArrays.realGrid(new double[] { -1.0, -1.0 }, new double[] { 1.05, 1.05 }, 40);
				KernelFunctionND kf2 = new ProductKernelFunction.Biweight(2);
				while (realgrid2.hasNext()) {
					double[] next = (double[]) realgrid2.next();
					System.out.println(java.util.Arrays.toString(next) + "\t" + kf2.eval(next));
				}
				break;
			default :
				throw new IllegalArgumentException("unknown kernel function given");
		}
	}

}
