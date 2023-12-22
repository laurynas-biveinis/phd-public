package xxl.tests.math.functions;

import xxl.core.math.functions.RealFunction;
import xxl.core.math.functions.RealFunctions;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RealFunctions.
 */
public class TestRealFunctions {

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
		RealFunction rf1 = RealFunctions.pdfCP00();
		double[] xgrid1 = xxl.core.util.DoubleArrays.equiGrid(0.0, 1.0, 100);
		double[] ygrid1 = xxl.core.math.Statistics.evalRealFunction(xgrid1, rf1);
		System.out.println("# x\t f(x)=x^4 + I[0.3,1.0](x)");
		for (int i = 0; i < xgrid1.length; i++)
			System.out.println(xgrid1[i] + "\t" + ygrid1[i]);
		// -----
	}

}
