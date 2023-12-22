/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.converters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This class provides a converter that is able to read and write arrays of
 * <code>String</code> objects. First the converter reads or writes the length
 * of the <code>String</code> array. Thereafter the <code>String</code> objects
 * are read or written.
 * 
 * <p>Example usage (1).
 * <code><pre>
 *   // create a string array
 *   
 *   String[] array = {"This", "is", "a", "String", "array"};
 *   
 *   // create a byte array output stream
 *   
 *   ByteArrayOutputStream output = new ByteArrayOutputStream();
 *   
 *   // write array to the output stream
 *   
 *   StringArrayConverter.DEFAULT_INSTANCE.write(new DataOutputStream(output), array);
 *   
 *   // create a byte array input stream on the output stream
 *   
 *   ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
 *   
 *   // reset the array
 *   
 *   array = null;
 *   
 *   // read array from the input stream
 *   
 *   array = StringArrayConverter.DEFAULT_INSTANCE.read(new DataInputStream(input));
 *   
 *   // print the array
 *   
 *   for (String string : array)
 *       System.out.println(string);
 *       
 *   // close the streams after use
 *   
 *   input.close();
 *   output.close();
 * </pre></code></p>
 *
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class StringArrayConverter extends Converter<String[]> {

	/**
	 * This instance can be used for getting a default instance of a string
	 * array converter. It is similar to the <i>Singleton Design Pattern</i>
	 * (for further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of a
	 * string array converter.
	 */
	public static final StringArrayConverter DEFAULT_INSTANCE = new StringArrayConverter();

	/**
	 * Reads an array of <code>String</code> objects from the specified data
	 * input and returns the restored <code>String</code> array.
	 * 
	 * <p>When the specified <code>string</code> array is <code>null</code>
	 * this implementation returns a new array of <code>String</code> object,
	 * otherwise the size of the specified string array has to be sufficient.
	 * For reading the single <code>String</code> objects from the specified
	 * data input, a string converter is used.</p>
	 *
	 * @param dataInput the stream to read the <code>String</code> array from.
	 * @param object the <code>String</code> array to be restored.
	 * @return the read array of <code>String</code> objects.
	 * @throws IOException if I/O errors occur.
	 */
	@Override
	public String[] read(DataInput dataInput, String[] object) throws IOException{
		int length = dataInput.readInt();
		if (object == null)
			object = new String[length];

		for (int i = 0; i < object.length; i++)
			object[i] = StringConverter.DEFAULT_INSTANCE.read(dataInput);
		return object;
	}

	/**
	 * Writes the specified array of <code>String</code> objects to the
	 * specified data output.
	 * 
	 * <p>This implementation first writes the length of the array to the data
	 * output. Thereafter the <code>String</code> objects are written by
	 * calling the <code>write</code> method of a string converter.</p>
	 *
	 * @param dataOutput the stream to write the <code>String</code> array to.
	 * @param object the <code>String</code> array that should be written to
	 *        the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@Override
	public void write(DataOutput dataOutput, String[] object) throws IOException{
		dataOutput.writeInt(object.length);
		for (String string : object)
			StringConverter.DEFAULT_INSTANCE.write(dataOutput, string);
	}
}
