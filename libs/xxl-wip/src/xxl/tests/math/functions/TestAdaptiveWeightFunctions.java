package xxl.tests.math.functions;

import xxl.core.math.functions.AdaptiveWeightFunctions;
import xxl.core.math.functions.RealFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AdaptiveWeightFunctions.
 */
public class TestAdaptiveWeightFunctions {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		double c = 2.0;
		double alpha1 = 2.0;
		double alpha2 = 0.5;

		RealFunction wa = new AdaptiveWeightFunctions.ArithmeticWeights();
		RealFunction wc = new AdaptiveWeightFunctions.GeometricWeights(c);
		RealFunction wl = new AdaptiveWeightFunctions.LogarithmicWeights();
		RealFunction wp = new AdaptiveWeightFunctions.ProgressiveDegressiveWeights(alpha2);
		RealFunction wd = new AdaptiveWeightFunctions.ProgressiveDegressiveWeights(alpha1);

		System.out.println("j \tarithm\tgeom.(" + c + ")\tlog.\tprog.(" + alpha2 + ")\tdegr.(" + alpha1 + ")");
		for (int j = 1; j <= 100; j++) {
			System.out.print(j + "\t");
			System.out.print(wa.eval(j) + "\t");
			System.out.print(wc.eval(j) + "\t");
			System.out.print(wl.eval(j) + "\t");
			System.out.print(wp.eval(j) + "\t");
			System.out.print(wd.eval(j) + "\n");
		}
	}

}
