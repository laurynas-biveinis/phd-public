package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.CharacterConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class CharacterConverter.
 */
public class TestCharacterConverter {

	/**
	 * The main method contains some examples how to use a charcter converter.
	 * It can also be used to test the functionality of a character converter.
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
		// write a Character and a char value to the output stream
		CharacterConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), 'C');
		CharacterConverter.DEFAULT_INSTANCE.writeChar(new java.io.DataOutputStream(output), 'p');
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a char value and a Character from the input stream
		char c1 = CharacterConverter.DEFAULT_INSTANCE.readChar(new java.io.DataInputStream(input));
		Character c2 = CharacterConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(c1);
		System.out.println(c2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
