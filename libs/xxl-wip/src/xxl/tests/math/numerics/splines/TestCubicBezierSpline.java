package xxl.tests.math.numerics.splines;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.math.Maths;
import xxl.core.math.functions.FunctionRealFunction;
import xxl.core.math.numerics.splines.CubicBezierSpline;
import xxl.core.math.numerics.splines.RB1CubicBezierSpline;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class CubicBezierSpline.
 */
public class TestCubicBezierSpline {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String args[]) {
		Function f = new AbstractFunction() {
			public Object invoke(Object x) {
				double x1 = ((Number) x).doubleValue();
				return new Double(Math.sin(x1) / x1);
			}
		};
		System.out.println(
			"Approx. f(x) = sin (x) / x on [-10,60] in 100 steps with three cubic Bezier-Splines with the first boundary condition:\n#");
		int[] s = { 10, 20, 30, 60 };
		CubicBezierSpline[] splines = new CubicBezierSpline[s.length];
		Object[] results = new Object[s.length];
		double[] evalGrid = xxl.core.util.DoubleArrays.equiGrid(-10.0, 60.0, 2000);
		for (int i = 0; i < s.length; i++) {
			System.out.println("spline number " + i + " uses " + s[i] + " steps");
			splines[i] =
				new RB1CubicBezierSpline(
					-10.0,
					60.0,
					s[i],
					xxl.core.math.Statistics.evalReal1DFunction(-10.0, 60.0, s[i], f));
			System.out.println("Evaluating the built spline number " + i + " on [-10,60] in 2000 function points:\n#");
			results[i] = xxl.core.math.Statistics.evalReal1DFunction(evalGrid, splines[i]);
		}
		System.out.println();
		double[] errors;
		int gridPoints=500;
		for(int i=0;i<s.length;i++) {
			errors=Maths.errorEstimation(-10.0,60.0,gridPoints,splines[i],new FunctionRealFunction(f));
			System.out.println(
				"Local Errors for spline"+i+" on [-10,60] with "+gridPoints+" knots: "+'\n'
				+" L1 "+errors[0]+'\n'
				+" MSE "+errors[1]+'\n'
				+" MXDV "+errors[2]+'\n'
				+" RMSE "+errors[3]+'\n'
			);
		}
	}

}
