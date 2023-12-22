package xxl.tests.predicates;

import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Predicates.
 */
public class TestPredicates {
	
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
		// example to newNullSensitveEqual(boolean)
		Predicate<Object> p1 = Predicates.newNullSensitiveEqual(true);
		Object[] oa1 = new Object[] {"a1", "a2", "A3", null};
		Object[] oa2 = new Object[] {"a3", "A2", "a1", null};

		for (Object o1 : oa1)
			for (Object o2 : oa2)
				System.out.println("equal(" + o1 +", " + o2 + ")=" + p1.invoke(o1, o2));
	}

}
