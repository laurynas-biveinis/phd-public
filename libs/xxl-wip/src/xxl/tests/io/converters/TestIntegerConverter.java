package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.IntegerConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class IntegerConverter.
 */
public class TestIntegerConverter {

	/**
	 * The main method contains some examples how to use an IntegerConverter.
	 * It can also be used to test the functionality of an IntegerConverter.
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
		// write an Integer and an int value to the output stream
		IntegerConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), 42);
		IntegerConverter.DEFAULT_INSTANCE.writeInt(new java.io.DataOutputStream(output), 666);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read an int value and an Integer from the input stream
		int i1 = IntegerConverter.DEFAULT_INSTANCE.readInt(new java.io.DataInputStream(input));
		Integer i2 = IntegerConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(i1);
		System.out.println(i2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
