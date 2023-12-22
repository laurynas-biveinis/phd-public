package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.StringConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class StringConverter.
 */
public class TestStringConverter {

	/**
	 * The main method contains some examples how to use a string converter. It
	 * can also be used to test the functionality of a string converter.
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
		// write two strings to the output stream
		StringConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), "Hello world!");
		StringConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), "That's all, folks!");
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read two strings from the input stream
		String s1 = StringConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		String s2 = StringConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(s1);
		System.out.println(s2);
		// close the streams after use
		input.close();
		output.close();
			
		System.out.println();
	}

}
