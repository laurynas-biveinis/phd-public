package xxl.tests.cursors.sources;

import java.security.SecureRandom;

import xxl.core.cursors.sources.Permutator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Permutator.
 */
public class TestPermutator {

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
		
		Permutator permutator = new Permutator(10);
		
		permutator.open();
		
		while(permutator.hasNext())
			System.out.print(permutator.next() + "; ");
		System.out.flush();
		System.out.println();
		
		permutator.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		permutator = new Permutator(new int[10], new SecureRandom());
		
		permutator.open();
		
		while(permutator.hasNext())
			System.out.print(permutator.next() +"; ");
		System.out.flush();
		System.out.println();
		
		permutator.close();
	}

}
