package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.BooleanArrayConverter;
import xxl.core.io.converters.Converter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BooleanArrayConverter.
 */
public class TestBooleanArrayConverter {

	/**
	 * Prints the array and tests it against the reconstructed array. This
	 * method is used by <code>makeTest</code> (for main method).
	 * 
	 * @param array the array to be tested.
	 * @param reconstructedArray the reconstructed array to be tested.
	 * @param len the number of elements from the first array that should be
	 *        tested.
	 */
	private static void test(boolean[] array, boolean[] reconstructedArray, int len) {
		// print the array
		if (len == -1) {
			if (reconstructedArray.length != array.length)
				throw new RuntimeException("Error in BooleanArrayConverter (length is not correct)");
			len = array.length;
		}
		
		for (int i = 0; i < len; i++) {
			System.out.print(reconstructedArray[i] + " ");
			if (array[i] != reconstructedArray[i])
				throw new RuntimeException("Error in BooleanArrayConverter");
		}
		System.out.println();
	}

	/**
	 * Test which is called from the main method.
	 * 
	 * @param array the array to be tested.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	private static void makeTest(boolean[] array) throws IOException {
		boolean[] reconstructedArray;
		
		System.out.println("Converting an array with " + array.length + " components");
		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write array to the output stream
		BooleanArrayConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), array);
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		// read array from the input stream
		reconstructedArray = BooleanArrayConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));

		test(array, reconstructedArray, -1);

		// close the streams after use
		input.close();
		output.close();

		System.out.println("Take the array and convert only 3 components");
		// create a byte array output stream
		output = new java.io.ByteArrayOutputStream();
		// write array to the output stream
		Converter<boolean[]> c = new BooleanArrayConverter(3); // only transforms 3 booleans
		c.write(new java.io.DataOutputStream(output), array);
		// create a byte array input stream on the output stream
		input = new java.io.ByteArrayInputStream(output.toByteArray());

		// read array from the input stream
		reconstructedArray = new boolean[4]; // Tricky. Only 3 fields are used.
		c.read(new java.io.DataInputStream(input), reconstructedArray);

		test(array, reconstructedArray, 3);
		
		// close the streams after use
		input.close();
		output.close();
	}
	
	/**
	 * The main method contains some examples how to use a
	 * BooleanArrayConverter. It can also be used to test the
	 * functionality of a BooleanArrayConverter.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main (String [] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create an boolean array
		System.out.println("Test 1:");
		makeTest(new boolean[]{false, true, false, true});
		System.out.println("Test 2:");
		makeTest(new boolean[]{true, false, false, true});
		System.out.println("Test 3:");
		makeTest(new boolean[]{true, false, false, true, true, false, false});
		System.out.println("Test 4:");
		makeTest(new boolean[]{true, false, false, true, true, false, false, true});
		System.out.println("Test 5:");
		makeTest(new boolean[]{false, true, false, false, true, true, false, false, true});
		System.out.println("Test 6:");
		makeTest(new boolean[]{true, false, false, true, false, false, false, true, true, false});
		System.out.println("Test 7:");
		makeTest(new boolean[]{
			true,  false, false, true,  true,  false, false, true,
			false, false, true,  false, false, false, true,  true
		});
		System.out.println("Test 8:");
		makeTest(
			new boolean[] {
				true, false, false, true, false, false, false, true, true, false,
				false,false, true,  false,false, false, true,  true, false,true,
				true, false, false, true, false, false, false, true, true, false,
				true, false, false, true, false, false, false, true, true, false,
				false,false, true,  false,false, false, true,  true, false,true,
				true, false, false, true, false, false, false, true, true, false,
				true, false, false, true, false, false, false, true, true, false,
				true, false, false, true, false, false, false, true, true, false,
				true, false, false, true, false, false, false, true, true, false
			}
		);
	}

}
