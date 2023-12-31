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
 * This class provides a converter that is able to read and write
 * <code>Character</code> objects as <tt>ASCII</tt> characters (the
 * <code>byte</code> values representing the <tt>ASCII</tt> character). In
 * addition to the read and write methods that read or write
 * <code>Character</code> objects this class contains <code>readChar</code> and
 * <code>writeChar</code> methods that convert the <code>Character</code>
 * object after reading or before writing it to its primitive <code>char</code>
 * type.
 * 
 * <p>Example usage (1).
 * <code><pre>
 *     // catch IOExceptions
 *
 *     try {
 *
 *         // create a byte array output stream
 *
 *         ByteArrayOutputStream output = new ByteArrayOutputStream();
 *
 *         // write a Character and a char value to the output stream
 *
 *         AsciiConverter.DEFAULT_INSTANCE.write(new DataOutputStream(output), new Character('C'));
 *         AsciiConverter.DEFAULT_INSTANCE.writeChar(new DataOutputStream(output), 'b');
 *
 *         // create a byte array input stream on the output stream
 *
 *         ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
 *
 *         // read a char value and a Character from the input stream
 *
 *         char c1 = AsciiConverter.DEFAULT_INSTANCE.readByte(new DataInputStream(input));
 *         Character c2 = (Character)AsciiConverter.DEFAULT_INSTANCE.read(new DataInputStream(input));
 *
 *         // print the value and the object
 *
 *         System.out.println(c1);
 *         System.out.println(c2);
 *
 *         // close the streams after use
 *
 *         input.close();
 *         output.close();
 *     }
 *     catch (IOException ioe) {
 *         System.out.println("An I/O error occured.");
 *     }
 * </pre></code></p>
 *
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class AsciiConverter extends CharacterConverter {

	/**
	 * This instance can be used for getting a default instance of an ASCII
	 * converter. It is similar to the <i>Singleton Design Pattern</i> (for
	 * further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of
	 * an ASCII converter.
	 */
	public static final AsciiConverter DEFAULT_INSTANCE = new AsciiConverter();

	/**
	 * This field contains the number of bytes needed to serialize the
	 * <code>byte</code> value representing the <tt>ASCII</tt> character of a
	 * <code>Character</code> object. Because this size is predefined it must
	 * not be measured each time.
	 */
	public static final int SIZE = 1;

	/**
	 * Reads the <code>byte</code> value representing the <tt>ASCII</tt>
	 * character for the specified (<code>Character</code>) object from the
	 * specified data input and returns the restored object.
	 * 
	 * <p>This implementation ignores the specified object and returns a new
	 * <code>Character</code> object. So it does not matter when the specified
	 * object is <code>null</code>.</p>
	 *
	 * @param dataInput the stream to read the <code>byte</code> value
	 *        representing the <tt>ASCII</tt> character from in order to return
	 *        a <code>Character</code> object.
	 * @param object the (<code>Character</code>) object to be restored. In
	 *        this implementation it is ignored.
	 * @return the read <code>Character</code> object.
	 * @throws IOException if I/O errors occur.
	 */
	@Override
	public Character read(DataInput dataInput, Character object) throws IOException {
		return (char)dataInput.readByte();
	}

	/**
	 * Writes the <code>byte</code> value representing the <tt>ASCII</tt>
	 * character of the specified <code>Character</code> object to the
	 * specified data output.
	 *
	 * @param dataOutput the stream to write the <code>byte</code> value
	 *        representing the <tt>ASCII</tt> character of the specified
	 *        <code>Character</code> object to.
	 * @param object the <code>Character</code> object that <code>byte</code>
	 *        value representing the <tt>ASCII</tt> character should be
	 *        written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@Override
	public void write(DataOutput dataOutput, Character object) throws IOException {
		dataOutput.writeByte(object);
	}
}
