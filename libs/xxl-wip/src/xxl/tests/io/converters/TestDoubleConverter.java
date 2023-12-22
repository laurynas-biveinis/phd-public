package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.DoubleConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DoubleConverter.
 */
public class TestDoubleConverter {

	/**
	 * The main method contains some examples how to use a double converter. It
	 * can also be used to test the functionality of a double converter.
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
		// write a Double and a double value to the output stream
		DoubleConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), 2.7236512);
		DoubleConverter.DEFAULT_INSTANCE.writeDouble(new java.io.DataOutputStream(output), 6.123853);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a double value and a Double from the input stream
		double d1 = DoubleConverter.DEFAULT_INSTANCE.readDouble(new java.io.DataInputStream(input));
		Double d2 = DoubleConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(d1);
		System.out.println(d2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
