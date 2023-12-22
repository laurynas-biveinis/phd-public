package xxl.tests.cursors.filters;

import xxl.core.cursors.filters.WhileDropper;
import xxl.core.predicates.AbstractPredicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class WhileDropper.
 */
public class TestWhileDropper {
	
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
		
		WhileDropper<Integer> whileDropper = new WhileDropper<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			new AbstractPredicate<Integer>() {
				public boolean invoke(Integer object) {
					return object.intValue() < 10;
				}
			},
			true
		);
		
		whileDropper.open();
		
		while (whileDropper.hasNext())
			System.out.print(whileDropper.next() + "; ");
		System.out.flush();
		
		whileDropper.close();
	}

}
