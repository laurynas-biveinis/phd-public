package xxl.tests.io.converters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.core.io.converters.ArrayConverter;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.io.converters.StringConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ArrayConverter.
 */
public class TestArrayConverter {

	/**
	 * The main method contains some examples how to use an array converter. It
	 * can also be used to test the functionality of an array converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@SuppressWarnings("unchecked") // arrays are internally initialized in a correct way
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create an array of 3 object
		java.util.Vector<String>[] array = new java.util.Vector[3];
		// initialize the objects with vectors and fill them with strings
		array[0] = new java.util.Vector<String>();
		array[0].add("This");
		array[0].add("is");
		array[0].add("a");
		array[0].add("vector.");
		array[1] = new java.util.Vector<String>();
		array[1].add("This");
		array[1].add("also.");
		array[2] = new java.util.Vector<String>();
		array[2].add("No");
		array[2].add("it");
		array[2].add("does");
		array[2].add("not");
		array[2].add("really");
		array[2].add("make");
		array[2].add("any");
		array[2].add("sense.");
		// create a converter for vectors
		Converter<java.util.Vector<String>> converter = new Converter<java.util.Vector<String>>() {
			// how to write a vector
			@Override
			public void write(DataOutput dataOutput, java.util.Vector<String> object) throws IOException {
				// write the size of the vector at first
				IntegerConverter.DEFAULT_INSTANCE.writeInt(dataOutput, object.size());
				// thereafter write the elements of the vector
				for (String string : object)
					StringConverter.DEFAULT_INSTANCE.write(dataOutput, string);
			}
			// how to read a vector
			@Override
			public java.util.Vector<String> read(DataInput dataInput, java.util.Vector<String> object) throws IOException {
				// read the size of the vector at first
				int size = IntegerConverter.DEFAULT_INSTANCE.readInt(dataInput);
				// create a new vector
				java.util.Vector<String> vector = new java.util.Vector<String>();
				// thereafter read the elements of the vector
				for (int i = 0; i < size; i++)
					vector.add(StringConverter.DEFAULT_INSTANCE.read(dataInput));
				// return the restored vector
				return vector;
			}
		};
		// create an array converter that is able to read and write
		// arrays of vectors
		ArrayConverter<java.util.Vector<String>> arrayConverter = new ArrayConverter<java.util.Vector<String>>(converter);
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write array to the output stream
		arrayConverter.write(new java.io.DataOutputStream(output), array);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// reset the array
		array = null;
		// read array from the input stream
		array = arrayConverter.read(new java.io.DataInputStream(input), new java.util.Vector[0]);
		// print the array (the data of the vectors)
		for (java.util.Vector<String> vector : array)
			System.out.println(vector);
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
