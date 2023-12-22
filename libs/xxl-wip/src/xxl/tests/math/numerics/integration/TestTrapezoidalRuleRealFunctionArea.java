package xxl.tests.math.numerics.integration;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.math.numerics.integration.TrapezoidalRuleRealFunctionArea;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class TrapezoidalRuleRealFunctionArea.
 */
public class TestTrapezoidalRuleRealFunctionArea {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		// integration of the logarithm over [1,2] using the Trapezoidal Rule
		final int n = 100;
		final double epsilon = 0.005;
		Function f = new AbstractFunction() {
			public Object invoke(Object x) {
				return new Double(Math.log(((Double) x).doubleValue()));
			}
		};
		System.out.println(
			"Integration of the logarithm over [1,2] using the Trapezoidal Rule with n="
				+ n
				+ " steps:"
				+ '\n'
				+ TrapezoidalRuleRealFunctionArea.trapez(1.0, 2.0, f, n));
		System.out.println();
		System.out.println(
			"Integration of the logarithm over [1,2] using the Trapezoidal Rule with epsilon="
				+ epsilon
				+ '\n'
				+ TrapezoidalRuleRealFunctionArea.trapezx(1.0, 2.0, f, epsilon));
	}

}
