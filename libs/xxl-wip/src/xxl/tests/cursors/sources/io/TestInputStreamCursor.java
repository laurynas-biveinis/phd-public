package xxl.tests.cursors.sources.io;

import java.io.IOException;

import xxl.core.cursors.sources.io.InputStreamCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class InputStreamCursor.
 */
public class TestInputStreamCursor {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new data output stream
		
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
		
		// create a new integer converter
		
		xxl.core.io.converters.IntegerConverter converter = xxl.core.io.converters.IntegerConverter.DEFAULT_INSTANCE;
		
		// write the integers from 0 to 10 to the data output stream
		
		for (int i = 0; i < 11; i++)
			try {
				converter.writeInt(dos, i);
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
		
		// create a new data input stream
		
		java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
		java.io.DataInputStream dis = new java.io.DataInputStream(bais);
		
		// create a new input stream cursor that depends on this data output
		// stream
		
		InputStreamCursor<Integer> cursor = new InputStreamCursor<Integer>(dis, converter);
		
		// open the cursor
		
		cursor.open();
		
		// print all elements of the cursor
		
		while (cursor.hasNext())
			System.out.println(cursor.next());
		
		// close the data output stream and the input stream cursor after use
		
		cursor.close();
		try {
			dos.close();
		}
		catch (IOException ioe) {
			System.out.println("An I/O error occured.");
		}
		System.out.println();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// create a new data output stream
		
		baos = new java.io.ByteArrayOutputStream();
		dos = new java.io.DataOutputStream(baos);
		
		// create a new integer converter
		
		converter = xxl.core.io.converters.IntegerConverter.DEFAULT_INSTANCE;
		
		// write the integers from 0 to 10 to the data output stream
		
		for (int i = 0; i < 11; i++)
			try {
				converter.writeInt(dos, i);
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
		
		// create a new data input stream
		
		bais = new java.io.ByteArrayInputStream(baos.toByteArray());
		dis = new java.io.DataInputStream(bais);
		
		// create a new input stream cursor that depends on this data output
		// stream
		
		cursor = new InputStreamCursor<Integer>(dis, converter);
		
		// open the cursor
		
		cursor.open();
		
		// print the elements of the cursor and clear it when 5 has been
		// printed
		
		while (cursor.hasNext()) {
			int i = cursor.next();
			System.out.println(i);
			if (i == 5) {
				cursor.close();
				break;
			}
		}
		
		// close the data output stream after use
		
		try {
			dos.close();
		}
		catch (IOException ioe) {
			System.out.println("An I/O error occured.");
		}
		System.out.println();
	}

}
