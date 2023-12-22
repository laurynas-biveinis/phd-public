package xxl.tests.util;

import java.util.Arrays;
import java.util.Iterator;

import xxl.core.util.DoubleArrays;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DoubleArrays.
 */
public class TestDoubleArrays {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of the associated class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main ( String [] args) {
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		System.out.println("real-valued grid from ( -1.0, 2.0)' to (4.0, 3.0) exclusivly using ( 6, 4)' points!");
		Iterator it = DoubleArrays.realGrid ( new double [] { -1.0, 2.0}, new double [] { 4.0, 3.0}, new int [] { 6, 4});
		while ( it.hasNext()){
			System.out.println( Arrays.toString ( (double []) it.next()));
		}
		System.out.println("- end -");
		// ---
		System.out.println("\n\ncounter real valued grid from ( -1.0, 2.0)' to (4.0, 3.0) exclusivly using 4 points!");
		it = DoubleArrays.realGrid ( new double [] { -1.0, 2.0}, new double [] { 4.0, 3.0}, 4);
		while ( it.hasNext()){
			System.out.println( Arrays.toString ( (double []) it.next()));
		}
		System.out.println("- end -");
		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		double [] g2 = DoubleArrays.equiGrid( -4.0, 4.0, 10);
		System.out.println("\n\nequi-grid of [-4.0, 4.0] wtih 10 grid points:\n" + Arrays.toString(g2));
	}

}
