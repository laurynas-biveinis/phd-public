package xxl.tests.cursors.unions;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.unions.Flatten;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Flatten.
 */
public class TestFlatten {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		Cursor cursor = new ArrayCursor(new Object[] {
			new Integer(1),
			new xxl.core.cursors.sources.Enumerator(2,4),
			new Integer(4),
			EmptyCursor.DEFAULT_INSTANCE,
			new ArrayCursor(new Object[] {
				EmptyCursor.DEFAULT_INSTANCE,
				new xxl.core.cursors.sources.Enumerator(5,8),
				new Integer(8),
				new xxl.core.cursors.sources.Enumerator(9,10),
				EmptyCursor.DEFAULT_INSTANCE
			}),
			new Integer(10)
		});
		
		Flatten flatten = new Flatten(
			cursor,
			Flatten.ITERATOR_FLATTEN_FUNCTION
		);
		
		flatten.open();
		
		while (flatten.hasNext())
			System.out.print(flatten.next() + "; ");
		System.out.flush();
		System.out.println();
		
		flatten.close();
	}

}
