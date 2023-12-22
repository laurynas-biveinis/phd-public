package xxl.tests.io;

import java.io.IOException;

import xxl.core.io.IteratorInputStream;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class IteratorInputStream.
 */
public class TestIteratorInputStream {

	/**
	 * The main method contains some examples how to use an
	 * IteratorInputStream. It can also be used to test the functionality
	 * of an IteratorInputStream.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new list
		java.util.List list = new java.util.ArrayList();
		// add some integers to that list
		list.add(new Integer(1));
		list.add(new Integer(10));
		list.add(new Integer(100));
		list.add(new Integer(1000));
		list.add(new Integer(10000));
		list.add(new Integer(100000));
		list.add(new Integer(1000000));
		list.add(new Integer(10000000));
		// create a new iterator input stream
		IteratorInputStream input = new IteratorInputStream(list.iterator());
		// print all data of the iterator input stream
		try {
			int i = 0;
			while ((i = input.read()) != -1)
				System.out.println(i);
		}
		catch (IOException ioe) {
			System.out.println("An I/O error occured.");
		}
		System.out.println();
	}

}
