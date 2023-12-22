package xxl.tests.functions;

import java.util.ArrayList;
import java.util.Arrays;

import xxl.core.functions.Function;
import xxl.core.functions.Functions;
import xxl.core.functions.MultiPrint;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Print.
 */
public class TestMultiPrint {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		MultiPrint<Double> print = new MultiPrint<Double>();
		
		System.out.println("Composing print function:");
		Function<Number, Double> tan = Functions.compose(Functions.composeMulti(Functions.div(), print), Functions.sin(), Functions.cos());
		System.out.println("tan(0.5)=" + tan.invoke(0.5));
		//
		System.out.println("<\n----------------------------\nJust printing an array:");
		print.invoke(Arrays.asList(1.0, 2.0, 3.0));
		//
		System.out.println("<\n----------------------------\nJust printing array of length 0:");
		print.invoke(new ArrayList<Double>(0));
	}

}
