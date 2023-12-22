package xxl.tests.cursors.unions;

import java.util.Iterator;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.unions.Merger;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Merger.
 */
public class TestMerger {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	@SuppressWarnings("unchecked") // the array is internally initialized in a correct way
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		xxl.core.cursors.groupers.HashGrouper<Integer> hashGrouper = new xxl.core.cursors.groupers.HashGrouper<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			new AbstractFunction<Integer, Integer>() {
				public Integer invoke(Integer next) {
					return next % 5;
				}
			}
		);
		
		hashGrouper.open();
		
		Cursor<Integer>[] cursors = new Cursor[5];
		for (int i = 0; hashGrouper.hasNext(); i++)
			cursors[i] = hashGrouper.next();

		Merger<Integer> merger = new Merger<Integer>(
			xxl.core.comparators.ComparableComparator.INTEGER_COMPARATOR,
			(Iterator<Integer>[])cursors
		);

		merger.open();
		
		while (merger.hasNext())
			System.out.print(merger.next() + "; ");
		System.out.flush();
		
		merger.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		System.out.println();
		
		merger = new Merger<Integer>(
			new xxl.core.collections.queues.StackQueue<Cursor<Integer>>(),
			new xxl.core.cursors.sources.Enumerator(11),
			new xxl.core.cursors.sources.Enumerator(11, 21)
		);
		
		merger.open();
		
		while (merger.hasNext())
			System.out.print(merger.next() + "; ");
		System.out.flush();
		
		merger.close();
	}

}
