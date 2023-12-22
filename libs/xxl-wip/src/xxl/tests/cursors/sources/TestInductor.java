package xxl.tests.cursors.sources;

import xxl.core.cursors.sources.Inductor;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Inductor.
 */
public class TestInductor {

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
		
		Inductor<Integer> fibonacci = new Inductor<Integer>(
			new AbstractFunction<Integer, Integer>() {
				@Override
				public Integer invoke(Integer fib_1, Integer fib_2) {
					return fib_1 + fib_2;
				}
			},
			1,
			1
		);
		
		fibonacci.open();
		
		System.out.println(xxl.core.cursors.Cursors.nth(fibonacci, 7));
		
		fibonacci.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		Inductor<Integer> factorial = new Inductor<Integer>(
			new AbstractFunction<Integer, Integer>() {
				int factor = 1;
				
				@Override
				public Integer invoke(Integer n) {
					return n * factor++;
				}
			},
			1
		);
		
		factorial.open();
		
		System.out.println(xxl.core.cursors.Cursors.nth(factorial, 3));
		
		factorial.close();
	}

}
