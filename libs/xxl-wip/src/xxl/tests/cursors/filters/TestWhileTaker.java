package xxl.tests.cursors.filters;

import xxl.core.cursors.filters.WhileTaker;
import xxl.core.predicates.AbstractPredicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class WhileTaker.
 */
public class TestWhileTaker {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <codecode>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		WhileTaker<Integer> whileTaker = new WhileTaker<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			new AbstractPredicate<Integer>() {
				public boolean invoke(Integer object) {
					return object < 10;
				}
			},
			true
		);
		
		whileTaker.open();
		
		while (whileTaker.hasNext())
			System.out.print(whileTaker.next() + "; ");
		System.out.flush();
		
		whileTaker.close();
	}

}
