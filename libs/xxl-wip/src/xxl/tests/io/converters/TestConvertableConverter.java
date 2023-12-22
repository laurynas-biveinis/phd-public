package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.io.converters.ConvertableConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ConvertableConverter.
 */
public class TestConvertableConverter {

	/**
	 * The main method contains some examples how to use a convertable
	 * converter. It can also be used to test the functionality of a
	 * convertable converter.
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
		// create two convertable objects (bit sets)
		xxl.core.util.BitSet b1 = new xxl.core.util.BitSet(13572l);
		xxl.core.util.BitSet b2 = new xxl.core.util.BitSet(-1l);
		// create a factory method for bit sets
		Function<Object, xxl.core.util.BitSet> factory = new AbstractFunction<Object, xxl.core.util.BitSet>() {
			@Override
			public xxl.core.util.BitSet invoke() {
				return new xxl.core.util.BitSet();
			}
		};
		// create a new convertable converter that converts bit sets
		ConvertableConverter<xxl.core.util.BitSet> converter = new ConvertableConverter<xxl.core.util.BitSet>(factory);
		// write the bit sets to the output stream
		converter.write(new java.io.DataOutputStream(output), b1);
		converter.write(new java.io.DataOutputStream(output), b2);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read the bit sets from the input stream and compare it to
		// the original bit sets
		System.out.println(b1.compareTo(converter.read(new java.io.DataInputStream(input))));
		System.out.println(b2.compareTo(converter.read(new java.io.DataInputStream(input))));
		// close the streams after use
		input.close();
		output.close();

		System.out.println();
	}

}
