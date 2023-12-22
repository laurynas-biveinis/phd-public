package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.BooleanConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BooleanConverter.
 */
public class TestBooleanConverter {

	/**
	 * The main method contains some examples how to use a boolean converter.
	 * It can also be used to test the functionality of a boolean converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write a Boolean and a boolean value to the output stream
		BooleanConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), true);
		BooleanConverter.DEFAULT_INSTANCE.writeBoolean(new java.io.DataOutputStream(output), false);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a boolean value and a Boolean from the input stream
		boolean b1 = BooleanConverter.DEFAULT_INSTANCE.readBoolean(new java.io.DataInputStream(input));
		Boolean b2 = BooleanConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(b1);
		System.out.println(b2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
