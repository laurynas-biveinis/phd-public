package xxl.tests.predicates;

import java.util.ArrayList;

import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AbstractPredicate.
 */
public class TestAbstractPredicate {

	/**
	 * The main method contains some examples how to use a Predicate. It can
	 * also be used to test the functionality of a Predicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args){
		// create a predicate that always returns true
		Predicate<Object> p1 = Predicates.TRUE;
		System.out.println(p1.invoke());
		System.out.println(p1.invoke(new Object()));
		System.out.println(p1.invoke(new Object(), new Object()));
		System.out.println(p1.invoke(new ArrayList<Object>(25)));
		System.out.println();

		// create a predicate that determines whether a given Integer
		// object is even or not
		Predicate<Integer> p2 = new AbstractPredicate<Integer>() {
			@Override
			public boolean invoke(Integer argument) {
				return argument % 2 == 0;
			}
		};
		for (int i = 0; i < 10; i++)
			System.out.println(i + " : " + p2.invoke(i));
		System.out.println();
	}

}
