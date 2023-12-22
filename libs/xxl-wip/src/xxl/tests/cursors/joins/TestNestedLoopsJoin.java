package xxl.tests.cursors.joins;

import java.util.Iterator;

import xxl.core.cursors.joins.NestedLoopsJoin;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NestedLoopsJoin.
 */
public class TestNestedLoopsJoin {

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
		
		NestedLoopsJoin<Integer, Object[]> join = new NestedLoopsJoin<Integer, Object[]>(
			java.util.Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).iterator(),
			new AbstractFunction<Object, Iterator<Integer>>() {
				public Iterator<Integer> invoke() {
					return java.util.Arrays.asList(2, 4, 6, 8, 10).iterator();
				}
			},
			new xxl.core.predicates.ComparatorBasedEqual<Integer>(xxl.core.comparators.ComparableComparator.INTEGER_COMPARATOR),
			new xxl.core.functions.Tuplify(),
			NestedLoopsJoin.Type.OUTER_JOIN
		);

		join.open();
		while (join.hasNext())
			System.out.println(java.util.Arrays.toString(join.next()));
		join.close();
	}

}
