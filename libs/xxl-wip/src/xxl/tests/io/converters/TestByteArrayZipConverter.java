package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.ByteArrayZipConverter;
import xxl.core.io.converters.StringConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ByteArrayZipConverter.
 */
public class TestByteArrayZipConverter {

	/**
	 * The main method contains some examples how to use a byte array zip
	 * converter. It can also be used to test the functionality of a byte array
	 * zip converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit a String to compress/decompress.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a string
		String s;
		if (args.length == 0) {
			s =	"This is a really long message text for test purposes."+
				"For shorter strings than 100 characters, this compression "+
	 			"does not make much sense because the zip-header ist too long. ";
	 		// make the String longer
	 		s = s+s+s;
	 	}
	 	else
	 		s = args[0];
 		// create a new byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write the string to the output stream
		StringConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output),s);
		// get the uncompressed data of the output stream
		byte[] buf = output.toByteArray();
		// close the output stream
		output.close();
		// write out the uncompressed length of the string
		System.out.println("Uncompressed length: " + buf.length);
		// create a new byte array output stream
		output = new java.io.ByteArrayOutputStream();
		// use the byte array zip converter the write the byte array to the output stream
		ByteArrayZipConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), buf);
		// get the compressed data of the output stream
		buf = output.toByteArray();
		// close the output stream
		output.close();
		// write the compressed length of the string
		System.out.println("Compressed length: " + buf.length);
		// create a new byte array input stream based of the compressed data
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(buf);
		// read the byte array from the input stream
		buf = ByteArrayZipConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// close the input stream
		input.close();
		// create a new byte array input stream based on the read (decompressed) byte array
		input = new java.io.ByteArrayInputStream(buf);
		// read the string from the input stream
		String s2 = StringConverter.DEFAULT_INSTANCE.read(new java.io.DataInputStream(input));
		// close the input stream
		input.close();
		// write out the decompressed text
		System.out.println("The decompressed text: "+s2);
		// compare the original and the decompressed text
		if (!s.equals(s2))
			throw new RuntimeException("The original and the compressed/decompressed Strings are not equal.");
		
		System.out.println();
	}

}
