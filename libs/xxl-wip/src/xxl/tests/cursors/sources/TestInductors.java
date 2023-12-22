package xxl.tests.cursors.sources;

import java.util.Arrays;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.Inductors;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Inductors.
 */
public class TestInductors {

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
		
		System.out.println("counter lexiographic order with int[] {6, 7, 2} {nDimCounter]");
		
		Cursor<int[]> counter = Inductors.nDimCounter(new int[] {6, 7, 2});
		
		counter.open();
		
		while (counter.hasNext())
			System.out.println(Arrays.toString(counter.next()));
		System.out.println("- end -");
		
		counter.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		int f = 3;
		int t = 78;
		
		System.out.println("natural numbers from " + f + " to " + t);
		
		Cursor<Long> naturalNumbers = Inductors.naturalNumbers(f, t);
		
		naturalNumbers.open();
		
		while (naturalNumbers.hasNext())
			System.out.println(naturalNumbers.next());
		System.out.println("- end -");
		
		naturalNumbers.close();

	}
}
