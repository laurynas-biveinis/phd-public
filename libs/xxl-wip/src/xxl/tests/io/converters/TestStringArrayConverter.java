package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.StringArrayConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class StringArrayConverter.
 */
public class TestStringArrayConverter {

	/**
	 * The main method contains some examples how to use a string array
	 * converter. It can also be used to test the functionality of a string
	 * array converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a string array
		String[] array = {"This", "is", "a", "String", "array"};
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write array to the output stream
		StringArrayConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), array);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// reset the array
		array = null;
		// read array from the input stream
		array = StringArrayConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the array
		for (String string : array)
			System.out.println(string);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
