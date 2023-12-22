package xxl.tests.io;

import java.io.PrintStream;

import xxl.core.io.NullOutputStream;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NullOutputStream.
 */
public class TestNullOutputStream {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of the associated class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main( String [] args){
		PrintStream p = new PrintStream(NullOutputStream.NULL);
		for( int i=0; i< 10; i++)
			p.println("i=" + i);
		p.flush();
		p.close();
	}

}
