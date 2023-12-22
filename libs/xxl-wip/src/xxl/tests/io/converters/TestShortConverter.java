package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.ShortConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ShortConverter.
 */
public class TestShortConverter {

	/**
	 * The main method contains some examples how to use a short converter. It
	 * can also be used to test the functionality of a short converter.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write a Short and a short value to the output stream
		ShortConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), (short)42);
		ShortConverter.DEFAULT_INSTANCE.writeShort(new java.io.DataOutputStream(output), (short)4711);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a long value and a Long from the input stream
		short s1 = ShortConverter.DEFAULT_INSTANCE.readShort(new java.io.DataInputStream(input));
		Short s2 = ShortConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(s1);
		System.out.println(s2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
