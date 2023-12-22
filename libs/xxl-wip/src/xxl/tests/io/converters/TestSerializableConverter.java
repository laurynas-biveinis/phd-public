package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.SerializableConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SerializableConverter.
 */
public class TestSerializableConverter {

	/**
	 * The main method contains some examples how to use a serializable
	 * converter. It can also be used to test the functionality of a
	 * serializable converter.
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
		// write a Double, an Integer and a String to the output stream
		SerializableConverter.DEFAULT_INSTANCE.write(new java.io.ObjectOutputStream(output), Math.E);
		SerializableConverter.DEFAULT_INSTANCE.write(new java.io.ObjectOutputStream(output), 42);
		SerializableConverter.DEFAULT_INSTANCE.write(new java.io.ObjectOutputStream(output), "Universum");
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read a Double, an Integer and a String from the input stream
		Double d = (Double)SerializableConverter.DEFAULT_INSTANCE.read(new java.io.ObjectInputStream(input));
		Integer i = (Integer)SerializableConverter.DEFAULT_INSTANCE.read(new java.io.ObjectInputStream(input));
		String s = (String)SerializableConverter.DEFAULT_INSTANCE.read(new java.io.ObjectInputStream(input));
		// print the objects
		System.out.println(d);
		System.out.println(i);
		System.out.println(s);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
