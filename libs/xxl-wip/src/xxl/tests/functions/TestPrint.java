package xxl.tests.functions;

import xxl.core.functions.Function;
import xxl.core.functions.Functions;
import xxl.core.functions.Print;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Print.
 */
public class TestPrint {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		Print<Double> print = new Print<Double>();
		
		System.out.println("Composing print function:");
		Function<Number, Double> tan = Functions.compose(Functions.div(), Functions.compose(print, Functions.sin()), Functions.cos());
		System.out.println("tan(0.5)=" + tan.invoke(0.5));
		//
		System.out.println("<\n----------------------------\nJust printing null:");
		print.invoke((Double)null);
	}

}
