package xxl.tests.functions;

import xxl.core.functions.For;
import xxl.core.functions.Identity;
import xxl.core.functions.Print;
import xxl.core.predicates.Less;
import xxl.core.predicates.RightBind;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class For.
 */
public class TestFor {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){
		//example: counting from 1 to 42
		For<Integer, Integer> f = new For<Integer, Integer>(
			new RightBind<Integer>(				//predicate
				new Less<Integer>(
					new xxl.core.comparators.ComparableComparator<Integer>()
				),
				42
			),
			new Print<Integer>(),				//f1: Output
			new Identity<Integer>(),			//f2: do nothing
			For.IntegerIncrement.DEFAULT_INSTANCE	//newState: increment int
		);

		System.out.println("Return-value:\t" + f.invoke(1));
	}

}
