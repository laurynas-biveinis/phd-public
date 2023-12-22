package xxl.tests.cursors.unions;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.unions.Sequentializer;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Sequentializer.
 */
public class TestSequentializer {

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
		
		Sequentializer<Integer> sequentializer = new Sequentializer<Integer>(
			new xxl.core.cursors.sources.Enumerator(11),
			new xxl.core.cursors.sources.Enumerator(11, 21)
		);
		
		sequentializer.open();
		
		while (sequentializer.hasNext())
			System.out.print(sequentializer.next() + "; ");
		System.out.flush();
		System.out.println();
		
		sequentializer.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		sequentializer = new Sequentializer<Integer>(
			new xxl.core.cursors.sources.Enumerator(1, 4),
			new AbstractFunction<Object, Cursor<Integer>>() {
				public Cursor<Integer> invoke() {
					return new xxl.core.cursors.sources.Enumerator(4, 7);
				}
			}
		);
		
		sequentializer.open();
		
		while (sequentializer.hasNext())
			System.out.print(sequentializer.next() + "; ");
		System.out.flush();
		System.out.println();
		
		sequentializer.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		
		sequentializer = new Sequentializer<Integer>(
			new xxl.core.cursors.groupers.HashGrouper<Integer>(
				new xxl.core.cursors.sources.Enumerator(21),
				new AbstractFunction<Integer, Integer>() {
					public Integer invoke(Integer next) {
						return next % 5;
					}
				}
			)
		);
		
		sequentializer.open();
		
		while (sequentializer.hasNext())
			System.out.print(sequentializer.next() + "; ");
		System.out.flush();
		System.out.println();
		
		sequentializer.close();
	}

}
