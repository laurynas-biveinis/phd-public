package xxl.tests.util;

import xxl.core.util.Strings;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Strings.
 */
public class TestStrings {
	
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
		String p = "Dies\tist\fein Test\n!";
		String[] s = Strings.parse(p, " ");
		System.out.println("-----------------------");
		System.out.println(p);
		System.out.println("-----------------------");
		for ( int i=0; i< s.length ; i++) System.out.print(s[i]+":");
		System.out.println("-----------------------");	
	}	

}
