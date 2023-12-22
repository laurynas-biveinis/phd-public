package xxl.tests.cursors.sorters;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.sorters.MergeSorter;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MergeSorter.
 */
public class TestMergeSorter {

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
		
		MergeSorter<Integer> sorter = new MergeSorter<Integer>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100000), 300*1000),
			ComparableComparator.INTEGER_COMPARATOR,
			12,
			12*4096,
			0.0,
			0.0,
			0.0,
			4*4096,
			0.0,
			new AbstractFunction<Function<?, Integer>, Queue<Integer>>() {
				public Queue<Integer> invoke(Function<?, Integer> function1, Function<?, Integer> function2) {
					return new ListQueue<Integer>();
				}
			},
			true
		);
		
		sorter.open();
		
		int count = 0;
		for (Integer old = null; sorter.hasNext(); count++) {
			if (old != null && old.compareTo(sorter.peek()) > 0)
				throw new RuntimeException("Fehler: Wert " + sorter.peek() + " ist groesser!");
			old = sorter.next();
		}
		System.out.println("Objects: " + count);
		
		sorter.close();
	}

}
