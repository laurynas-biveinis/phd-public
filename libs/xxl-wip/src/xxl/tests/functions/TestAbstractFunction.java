package xxl.tests.functions;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.functions.Functions;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AbstractFunction.
 */
public class TestAbstractFunction {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		Function<Double, Double> sin = new AbstractFunction<Double, Double>() {
			@Override
			public Double invoke(Double argument) {
				return Math.sin(argument);
			}
		};
		Function<Double, Double> cos = new AbstractFunction<Double, Double>() {
			@Override
			public Double invoke(Double argument) {
				return Math.cos(argument);
			}
		};
		Function<Double, Double> tan = new AbstractFunction<Double, Double>() {
			@Override
			public Double invoke(Double argument) {
				return Math.tan(argument);
			}
		};
		Function<Double, Double> div = new AbstractFunction<Double, Double>() {
			@Override
			public Double invoke(Double dividend, Double divisor) {
				return dividend / divisor;
			}
		};
		Double dv = 0.5;
		if (args.length == 1)
			dv = new Double(args[0]);
			
		System.out.println("parameter value: " + dv);
		
		System.out.println("sin: " + sin.invoke(dv));
		System.out.println("cos: " + cos.invoke(dv));

		double resTan = tan.invoke(dv);
		double resComposition = Functions.compose(div, sin, cos).invoke(dv);
		
		System.out.println("tan: " + resTan);
		System.out.println("div.compose(sin,cos): " + resComposition);
		
		if (Math.abs(resTan - resComposition) > 1E-12)
			throw new RuntimeException("Something is wrong with function composition");
	}

}
