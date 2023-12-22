package xxl.tests.math;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.math.Maths;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.numerics.integration.TrapezoidalRuleRealFunctionArea;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Maths.
 */
public class TestMaths {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		// testing triDiag Gauss
		int n = 4;
		double[] a1 = new double[n - 1];
		double[] a2 = new double[n];
		double[] a3 = new double[n - 1];
		double[] a4 = new double[n];
		//
		a1[0] = 1.0;
		a1[1] = 1.0;
		a1[2] = 1.0; // 1 1           3         2
		a2[0] = 1.0;
		a2[1] = 2.0;
		a2[2] = 2.0;
		a2[3] = 2.0; // 1 2 1   * x = 5 => x =  1
		a3[0] = 1.0;
		a3[1] = 1.0;
		a3[2] = 1.0; //   1 2 1       5         1
		a4[0] = 3.0;
		a4[1] = 5.0;
		a4[2] = 5.0;
		a4[3] = 5.0; //     2 2       5         2
		//
		System.out.println("testing triDiagGauss:\n");
		System.out.println("previous data:");
		System.out.println("a1=" + java.util.Arrays.toString(a1));
		System.out.println("a2=" + java.util.Arrays.toString(a2));
		System.out.println("a3=" + java.util.Arrays.toString(a3));
		System.out.println("a4=" + java.util.Arrays.toString(a4));

		System.out.println("Solved=" + java.util.Arrays.toString(Maths.triDiagonalGaussianLGS(a1, a2, a3, a4)));

		System.out.println("later ...");
		System.out.println("a1=" + java.util.Arrays.toString(a1));
		System.out.println("a2=" + java.util.Arrays.toString(a2));
		System.out.println("a3=" + java.util.Arrays.toString(a3));
		System.out.println("a4=" + java.util.Arrays.toString(a4));

		System.out.println("-----------------------------------------\n");

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		// testing trapez
		final double m = 2.0;
		final double b = 8.0;
		Function<Number, Double> f1 = new AbstractFunction<Number, Double>() {
			public Double invoke(Number o) {
				return m * o.doubleValue() + b;
			}
		};
		int nt = 10;
		System.out.println("trapez integration for y = " + m + "x+" + b);
		System.out.println("n=" + nt + " steps for computation");
		System.out.println("[" + 2 + ", " + 5 + "]=" + TrapezoidalRuleRealFunctionArea.trapez(2.0, 5.0, f1, nt));
		System.out.println("[" + 0 + ", " + 10 + "]=" + TrapezoidalRuleRealFunctionArea.trapez(0.0, 10.0, f1, nt));
		System.out.println("[" + -3 + ", " + 3 + "]=" + TrapezoidalRuleRealFunctionArea.trapez(-3.0, 3.0, f1, nt));
		System.out.println("-----------------------------------------\n");

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		// testing simple functions
		System.out.println("isEven(3) = " + Maths.isEven(3));
		System.out.println("isEven(2) = " + Maths.isEven(2));
		System.out.println("isOdd(0) = " + Maths.isOdd(0));
		System.out.println("isOdd(-1) = " + Maths.isOdd(-1));
		System.out.println("isEven(-3) = " + Maths.isEven(-3));
		System.out.println("-----------------------------------------\n");

		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		// testing binomialCoeff and binomialCoeff2
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j <= i; j++) {
				System.out.print(Maths.binomialCoeff(i, j) + " ");
				if (Maths.binomialCoeff(i, j) != Maths.binomialCoeff2(i, j))
					System.out.println("Error computing binomialCoeff");
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------\n");

		/*********************************************************************/
		/*                            Example 5 (a)                          */
		/*********************************************************************/
		// testing qNewton (modified Newton method) I, with f(x) e^x + x^2 - 2
		System.out.println("\n\ntesting modified newton method (qNewton( double, double, Function)):\n");
		// ca.!! x0 = -1.31597377779629, x1 = 0.5372744491738566
		RealFunction f = new RealFunction() {
			public double eval(double x) {
				return Math.pow(Math.E, x) + x * x - 2.0;
			}
			public String toString() {
				return "f(x) = exp(x) + x^2 -2.0";
			}
		};

		double epsilon = 0.4;
		double start = 0.0;
		double x0 = 0.0;
		System.out.println(f + "\n------------------------------");
		for (int i = 0; i <= 12; i++) {
			start = i * .25 - 2.0;
			System.out.print("start value x=" + start);
			System.out.print("\tfor error bound epsilon=" + epsilon);
			try {
				x0 = Maths.qNewton(start, epsilon, f);
				System.out.println("\tx0 = " + x0);
			} catch (ArithmeticException ae) {
				System.out.println("\tfailed for this parameters!");
			}
		}

		/*********************************************************************/
		/*                            Example 5 (b)                          */
		/*********************************************************************/
		// testing rootFinding
		System.out.println("\n\ntesting root finding:\n");
		double a5 = -3.0;
		double b5 = 3.0;
		double h5 = 0.00001;
		System.out.println("Finding all roots of " + f + " between " + a5 + " and " + b5 + " with h=" + h5 + ":");
		System.out.println(java.util.Arrays.toString(Maths.rootFinding(a5, b5, h5, f)));
		System.out.println("-----------------------------------------\n");

		/*********************************************************************/
		/*                            Example 6                              */
		/*********************************************************************/
		// testing fac, fac2, oddFactorial
		System.out.println("testing factorial methods");
		for (int i = 0; i < 21; i++) {
			System.out.println("computing " + i + "!:");
			System.out.println("\tfac(" + i + ")=" + Maths.fac(i));
			System.out.println("\tfac2(" + i + ")=" + Maths.fac2(i));
			if (Maths.isEven(i))
				System.out.println("\toddFactorial(" + i + ")=" + Maths.oddFactorial(i));
		}
		System.out.println("-----------------------------------------\n");

		/*********************************************************************/
		/*                            Example 7                              */
		/*********************************************************************/
		// testing hermite polynomial
		System.out.println("testing the hermite polynomial");
		double x7 = 0.0;
		for (int i = 0; i < 9; i++) {
			x7 = -3.0;
			System.out.println("H(" + i + ", " + x7 + ")=" + Maths.hermitePolynomial(i, x7));
			x7 = -1.0;
			System.out.println("H(" + i + ", " + x7 + ")=" + Maths.hermitePolynomial(i, x7));
			x7 = 0.0;
			System.out.println("H(" + i + ", " + x7 + ")=" + Maths.hermitePolynomial(i, x7));
			x7 = 1.0;
			System.out.println("H(" + i + ", " + x7 + ")=" + Maths.hermitePolynomial(i, x7));
			x7 = 3.0;
			System.out.println("H(" + i + ", " + x7 + ")=" + Maths.hermitePolynomial(i, x7));
		}
		System.out.println("-----------------------------------------\n");
	}

}
