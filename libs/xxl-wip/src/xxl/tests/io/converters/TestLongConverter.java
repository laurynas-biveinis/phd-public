package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.LongConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class LongConverter.
 */
public class TestLongConverter {

	/**
	 * The main method contains some examples how to use a long converter. It
	 * can also be used to test the functionality of a long converter.
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
		// write a Long and a long value to the output stream
		LongConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), 1234567890l);
		LongConverter.DEFAULT_INSTANCE.writeLong(new java.io.DataOutputStream(output), 123456789012345l);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a long value and a Long from the input stream
		long l1 = LongConverter.DEFAULT_INSTANCE.readLong(new java.io.DataInputStream(input));
		Long l2 = LongConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(l1);
		System.out.println(l2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
