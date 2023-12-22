/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.io.converters;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import xxl.core.io.DataInputInputStream;
import xxl.core.io.DataOutputOutputStream;

/**
 * This class provides a converter that compresses/decompresses everything
 * what a different converter writes/reads. Only big Objects (>256 Bytes) are
 * worth being compressed!
 *
 * @param <T> the type to be converted.
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class ZipConverter<T> extends Converter<T> {

	/**
	 * The converter which is decorated.
	 */
	protected Converter<T> converter;

	/**
	 * Constructs a new zip converter.
	 * 
	 * @param converter a converter which converts the uncompressed byte arrays
	 *        into objects of a certain type and vice versa.
	 */
	public ZipConverter(Converter<T> converter) {
		this.converter = converter;
	}

	/**
	 * Reads in a zip compressed object for the specified from the given data
	 * input and returns the decompressed object.
	 * 
	 * <p>This implementation ignores the specified object and returns a new
	 * byte array. So it does not matter when the specified object is
	 * <code>null</code>.</p>
	 *
	 * @param dataInput the stream to read a string from in order to return an
	 *        decompressed byte array.
	 * @param object the object to be decompressed. In this implementation it
	 *        is ignored.
	 * @return the read decompressed byte array.
	 * @throws IOException if I/O errors occur.
	 */
	public T read(final DataInput dataInput, T object) throws IOException {
		ZipInputStream zis = new ZipInputStream(new DataInputInputStream(dataInput));
		// ZipEntry entry
		zis.getNextEntry();
		return converter.read(new DataInputStream(zis), object);
	}

	/**
	 * Writes the specified object compressed to the specified data output.
	 *
	 * @param dataOutput the stream to write the string representation of the
	 *        specified object to.
	 * @param object the object that should be written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public void write(final DataOutput dataOutput, T object) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new DataOutputOutputStream(dataOutput));
		zos.setMethod(ZipOutputStream.DEFLATED);
		zos.putNextEntry(new ZipEntry("a"));
		DataOutputStream dos = new DataOutputStream(zos);
		
		converter.write(dos, object);
		dos.flush();
		zos.finish();
		dos.close();
	}

	/**
	 * The main method contains some examples how to use a zip converter. It
	 * can also be used to test the functionality of a zip converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit a String to compress/decompress.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a string to be converted and zipped
		String s;
		if (args.length == 0) {
			s =	"This is a really long message text for test purposes."+
				"For shorter strings than 100 characters, this compression "+
	 			"does not make much sense because the zip-header ist too long. ";
	 		// make the string longer
	 		s = s+s+s;
	 	}
	 	else
	 		s = args[0];
 		// create a new byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// write the string to the output stream
		StringConverter.DEFAULT_INSTANCE.write(new java.io.DataOutputStream(output), s);
		// get the data generated by the output stream
		byte[] buf = output.toByteArray();
		// close the output stream
		output.close();
		// write out the uncompressed length
		System.out.println("Uncompressed length: " + buf.length);
		// create a zip converter for strings
		Converter<String> converter = new ZipConverter<String>(StringConverter.DEFAULT_INSTANCE);
		// create a new byte array output stream
		output = new java.io.ByteArrayOutputStream();
		// write the string to the output stream
		converter.write(new java.io.DataOutputStream(output), s);
		// get the zipped data generated by the output stream
		buf = output.toByteArray();
		// close the output stream
		output.close();
		// write out the compressed length
		System.out.println("Compressed length: " + buf.length);
		// create a new byte array input buffer based on the stored zipped data
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(buf);
		// read a string from the input stream
		String s2 = converter.read(new java.io.DataInputStream(input), null);
		// close the input stream
		input.close();
		// write out the uncompressed text
		System.out.println("The decompressed text: " + s2);
		// compare the original and the uncompressed text
		if (!s.equals(s2))
			throw new RuntimeException("The original and the compressed/decompressed strings are not equal.");
	}
}
