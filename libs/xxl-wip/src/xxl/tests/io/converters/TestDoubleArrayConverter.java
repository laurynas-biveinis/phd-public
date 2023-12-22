package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.DoubleArrayConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DoubleArrayConverter.
 */
public class TestDoubleArrayConverter {

	/**
	 * The main method contains some examples how to use a double array
	 * converter. It can also be used to test the functionality of a double
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

		// create an double array
		double[] array = {1.945d, 4.09725d, 0.000005d, 3.23456d, 9d};
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write array to the output stream
		DoubleArrayConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), array);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// reset the array
		array = null;
		// read array from the input stream
		array = DoubleArrayConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the array
		for (double d : array)
			System.out.println(d);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
