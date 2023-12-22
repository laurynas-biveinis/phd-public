package xxl.tests.util;

import xxl.core.cursors.Cursor;
import xxl.core.util.Arrays;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Arrays.
 */
public class TestArrays {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		boolean[] b = Arrays.newBooleanArray(7,false);
		System.out.print("The boolean array: ");
		Arrays.print(b,System.out);
		System.out.println();

		System.out.println("The boolean array copied with copy(b,2,4): ");
		Arrays.print(Arrays.copy(b,2,4),System.out);
		System.out.println();

		int[] i = Arrays.newIntArray(7,42);
		System.out.print("The int array: ");
		Arrays.print(i,System.out);
		System.out.println();

		System.out.println("The int array copied with copy(i,2,4): ");
		Arrays.print(Arrays.copy(i,2,4),System.out);
		System.out.println();

		System.out.println("Constructing an Array of 15 integers containing the values 1..15");
		Arrays.print(Arrays.newIntArray(15,new xxl.core.cursors.sources.Enumerator(1,100)),System.out);
		System.out.println();

		System.out.println("Output of an Object array");
		Arrays.print(new Object[] {"Hello","World","this","is","a","String","array","with","number",new Integer(1)},System.out);
		System.out.println();

		System.out.println("Subset of an array: ");
		Integer[] array = new Integer[10];
		for (int j = 0; j < 10; j++)
			array[j] = new Integer(j);
		Cursor cursor = Arrays.arrayToCursor(array, 5, 8);
		xxl.core.cursors.Cursors.println(cursor);

		System.out.println("Iterate over a primitive int array");
		xxl.core.cursors.Cursors.println(Arrays.intArrayIterator(Arrays.newIntArray(7,42)));
		
		System.out.println("In-/Decrement a primitive int array");
		System.out.println(java.util.Arrays.toString(Arrays.incrementIntArray(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1)));
		System.out.println(java.util.Arrays.toString(Arrays.decrementIntArray(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1)));
	}

}
