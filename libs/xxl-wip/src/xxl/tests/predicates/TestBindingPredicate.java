package xxl.tests.predicates;

import java.util.Arrays;

import xxl.core.comparators.ComparableComparator;
import xxl.core.predicates.BindingPredicate;
import xxl.core.predicates.Less;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BindingPredicate.
 */
public class TestBindingPredicate {

	/**
	 * The main method contains some examples of how to use a BindingPredicate.
	 * It can also be used to test the functionality of a BindingPredicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){
		// create a BindingPredicate that implements the Less-Predicate
		// 42 < x
		BindingPredicate<Integer> p = new BindingPredicate<Integer>(
			new Less<Integer>(
				ComparableComparator.INTEGER_COMPARATOR
			),
			Arrays.asList(0),
			Arrays.asList(42)
		);
		
		System.out.println(p.invoke(2));
		System.out.println(p.invoke(Arrays.asList(44)));

		p.restoreBinds();
		// x < 42
		p.setBind(1, 42);
		
		System.out.println(p.invoke(2));

		// 44 < 42
		p.setBind(0, 44);

		System.out.println(p.invoke());

		System.out.println();
	}

}
