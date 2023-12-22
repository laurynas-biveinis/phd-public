package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.IntegerArrayConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class IntegerArrayConverter.
 */
public class TestIntegerArrayConverter {

	/**
	 * The main method contains some examples how to use an integer array
	 * converter. It can also be used to test the functionality of an integer
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

		// create an int array
		int [] array = {42, 4711, 666, 190, 77};
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write array to the output stream
		IntegerArrayConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), array);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// reset the array
		array = null;
		// read array from the input stream
		array = IntegerArrayConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the array
		for (int i : array)
			System.out.println(i);
		// close the streams after use
		input.close();
		output.close();
		
		System.out.println();
	}

}
