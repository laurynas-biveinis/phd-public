package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.CharacterArrayConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class CharacterArrayConverter.
 */
public class TestCharacterArrayConverter {

	/**
	 * The main method contains some examples how to use a character array
	 * converter. It can also be used to test the functionality of a character
	 * array converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main (String [] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create an char array
		char[] array = {'C', 'h', 'a', 'r', '-', 'A', 'r', 'r', 'a', 'y'};
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write array to the output stream
		CharacterArrayConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), array);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// reset the array
		array = null;
		// read array from the input stream
		array = CharacterArrayConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the array
		for (char c : array)
			System.out.println(c);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
