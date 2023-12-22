package xxl.tests.cursors.distincts;

import xxl.core.cursors.distincts.NestedLoopsDistinct;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NestedLoopsDistinct.
 */
public class TestNestedLoopsDistinct {

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
		
		NestedLoopsDistinct<Integer> distinct = new NestedLoopsDistinct<Integer>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(21), 30),
			32,
			4
		);
		
		distinct.open();
		
		while(distinct.hasNext())
			System.out.println(distinct.next());
		
		distinct.close();
	}

}
