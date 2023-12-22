package xxl.tests.io.converters;

import java.io.IOException;
import java.math.BigInteger;

import xxl.core.io.converters.BigIntegerConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BigIntegerConverter.
 */
public class TestBigIntegerConverter {

	/**
	 * The main method contains some examples how to use a big decimal
	 * converter. It can also be used to test the functionality of a big
	 * integer converter.
	 *
	 * @param args array of <code>String</tcodet> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write two values to the output stream
		BigIntegerConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), new BigInteger("1"));
		BigIntegerConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), new BigInteger("2"));
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read the values from the input stream
		BigInteger d1 = BigIntegerConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		BigInteger d2 = BigIntegerConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// print the objects
		System.out.println(d1);
		System.out.println(d2);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
