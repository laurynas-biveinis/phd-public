package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.ByteConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ByteConverter.
 */
public class TestByteConverter {

	/**
	 * The main method contains some examples how to use a byte converter. It
	 * can also be used to test the functionality of a byte converter.
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
		// write a Byte and a byte value to the output stream
		ByteConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), (byte)-1);
		ByteConverter.DEFAULT_INSTANCE.writeByte(new java.io.DataOutputStream(output), (byte)27);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a byte value and a Byte from the input stream
		byte b1 = ByteConverter.DEFAULT_INSTANCE.readByte(new java.io.DataInputStream(input));
		Byte b2 = ByteConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(b1);
		System.out.println(b2);
		// close the streams after use
		input.close();
		output.close();
		
		System.out.println();
	}

}
