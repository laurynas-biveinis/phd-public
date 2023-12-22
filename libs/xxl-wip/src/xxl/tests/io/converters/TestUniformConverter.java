package xxl.tests.io.converters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.StringConverter;
import xxl.core.io.converters.UniformConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UniformConverter.
 */
public class TestUniformConverter {

	/**
	 * The main method contains some examples how to use an uniform converter.
	 * It can also be used to test the functionality of an uniform converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create two map entries
		xxl.core.collections.MapEntry<Integer, String> me1 = new xxl.core.collections.MapEntry<Integer, String>(42, "Hello world.");
		xxl.core.collections.MapEntry<Integer, String> me2 = new xxl.core.collections.MapEntry<Integer, String>(4711, "That's all, folks!");
		// create a converter that only stores the value of a map
		// entry
		Converter<xxl.core.collections.MapEntry<Integer, String>> converter = new Converter<xxl.core.collections.MapEntry<Integer, String>>() {
			// how to write a map entry
			@Override
			public void write(DataOutput dataOutput, xxl.core.collections.MapEntry<Integer, String> object) throws IOException {
				// write the value of the map entry
				StringConverter.DEFAULT_INSTANCE.write(dataOutput, object.getValue());
			}
			// how to read a map entry
			@Override
			public xxl.core.collections.MapEntry<Integer, String> read (DataInput dataInput, xxl.core.collections.MapEntry<Integer, String> object) throws IOException {
				// read the value of the map entry
				object.setValue(StringConverter.DEFAULT_INSTANCE.read(dataInput));
				return object;
			}
		};
		// create a factory method that produces map entries with keys
		// of increasing integer objects
		Function<Object, xxl.core.collections.MapEntry<Integer, String>> factory = new AbstractFunction<Object, xxl.core.collections.MapEntry<Integer, String>>() {
			// a count for the returned keys
			int i = 0;
			// how to create a map entry
			@Override
			public xxl.core.collections.MapEntry<Integer, String> invoke() {
				// return a map entry with an integer wrapping the
				// counter as key and null as value
				return new xxl.core.collections.MapEntry<Integer, String>(i++, null);
			}
		};
		// create an uniform converter with ...
		UniformConverter<xxl.core.collections.MapEntry<Integer, String>> uniformConverter = new UniformConverter<xxl.core.collections.MapEntry<Integer, String>>(
			// the created converter
			converter,
			// the created factory method
			factory
		);
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// create an data output stream
		java.io.DataOutputStream dataOutput = new java.io.DataOutputStream(output);
		// write two strings to the output stream
		uniformConverter.write(dataOutput, me1);
		uniformConverter.write(dataOutput, me2);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// create an data input stream
		java.io.DataInputStream dataInput = new java.io.DataInputStream(input);
		// read two strings from the input stream
		me1 = uniformConverter.read(dataInput);
		me2 = uniformConverter.read(dataInput);
		// print the value and the object
		System.out.println(me1);
		System.out.println(me2);
		// close the streams after use
		dataInput.close();
		dataOutput.close();

		System.out.println();
	}

}
