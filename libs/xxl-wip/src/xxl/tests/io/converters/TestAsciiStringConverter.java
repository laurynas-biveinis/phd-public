package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.AsciiStringConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AsciiStringConverter.
 */
public class TestAsciiStringConverter {

	/**
	 * The main method contains some examples how to use an ASCII string
	 * converter. It can also be used to test the functionality of an ASCII
	 * string converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new AsciiStringConverter that uses "\r\n" (cariage
		// return, line feed), "\t" (horizontal tab) and " " (white
		// space) as delimiters
		AsciiStringConverter converter = new AsciiStringConverter(new String[] {"\r\n", "\t", " "});
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write some strings (delimited by "\r\n" per default) to the
		// output stream
		converter.write(new java.io.DataOutputStream(output), "Far out in the uncharted backwaters of the unfashionable end of the western spiral arm of the Galaxy lies a small unregarded yellow sun.");
		converter.write(new java.io.DataOutputStream(output), "Orbiting this at a distance of roughly ninety-two million miles is an utterly insignificant little blue green planet whose ape-descended life forms are so amazingly primitive that they still think digital watches are a pretty neat idea.");
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read strings from the input stream and print them as long
		// as the stream contains data
		while (input.available() > 0)
			System.out.println(converter.read(new java.io.DataInputStream(input)));
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
