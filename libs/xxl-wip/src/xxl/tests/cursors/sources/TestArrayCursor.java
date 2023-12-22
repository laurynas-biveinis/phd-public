package xxl.tests.cursors.sources;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.Enumerator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ArrayCursor.
 */
public class TestArrayCursor {

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
		
		Object[] numbers = Cursors.toArray(new Enumerator(5));
		
		ArrayCursor<Object> arrayCursor1 = new ArrayCursor<Object>(numbers);
		
		arrayCursor1.open();
		
		while (arrayCursor1.hasNext())
			System.out.print(arrayCursor1.next() + "; ");
		System.out.flush();
		System.out.println();
		
		arrayCursor1.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		ArrayCursor<Object> arrayCursor2 = new ArrayCursor<Object>(
				new Enumerator(4,-1),
				numbers
		); // using an indices iterator
		
		arrayCursor2.open();
		
		while (arrayCursor2.hasNext())
			System.out.print(arrayCursor2.next() + "; ");
		System.out.flush();
		System.out.println();
		
		arrayCursor2.close();
	}

}
