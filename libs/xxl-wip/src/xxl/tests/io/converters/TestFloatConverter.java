package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.FloatConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FloatConverter.
 */
public class TestFloatConverter {

	/**
	 * The main method contains some examples how to use a float converter. It
	 * can also be used to test the functionality of a float converter.
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
		// write a Float and a float value to the output stream
		FloatConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), 2.7236512f);
		FloatConverter.DEFAULT_INSTANCE.writeFloat(new java.io.DataOutputStream(output), 6.123853f);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a float value and a Float from the input stream
		float f1 = FloatConverter.DEFAULT_INSTANCE.readFloat(new java.io.DataInputStream(input));
		Float f2 = FloatConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the value and the object
		System.out.println(f1);
		System.out.println(f2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
