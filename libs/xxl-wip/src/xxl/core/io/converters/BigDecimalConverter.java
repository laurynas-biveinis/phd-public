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
import java.math.BigDecimal;

/**
 * This class provides a converter that is able to read and write
 * <code>BigDecimal</code> objects. 
 *
 * <p>Example usage (1).
 * <code><pre>
 *   // create a byte array output stream
 *   
 *   ByteArrayOutputStream output = new ByteArrayOutputStream();
 *   
 *   // write two values to the output stream
 *   
 *   BigDecimalConverter.DEFAULT_INSTANCE.write(new DataOutputStream(output), new BigDecimal("2.7236512"));
 *   BigDecimalConverter.DEFAULT_INSTANCE.write(new DataOutputStream(output), new BigDecimal("6.123853"));
 *   
 *   // create a byte array input stream on the output stream
 *   
 *   ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
 *   
 *   // read the values from the input stream
 *   
 *   BigDecimal d1 = BigDecimalConverter.DEFAULT_INSTANCE.read(new DataInputStream(input));
 *   BigDecimal d2 = BigDecimalConverter.DEFAULT_INSTANCE.read(new DataInputStream(input));
 *   
 *   // print the objects
 *   
 *   System.out.println(d1);
 *   System.out.println(d2);
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
public class BigDecimalConverter extends Converter<BigDecimal> {

	/**
	 * This instance can be used for getting a default instance of a big
	 * decimal converter. It is similar to the <i>Singleton Design Pattern</i>
	 * (for further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of a
	 * big decimal converter.
	 */
	public static final BigDecimalConverter DEFAULT_INSTANCE = new BigDecimalConverter();

	/**
	 * Reads the <code>BigDecimal</code> value for the specified
	 * (<code>BigDecimal</code>) object from the specified data input and
	 * returns the restored object.
	 * 
	 * <p>This implementation ignores the specified object and returns a new
	 * <code>BigDecimal</code> object. So it does not matter when the specified
	 * object is <code>null</code>.</p>
	 *
	 * @param dataInput the stream to read the <code>BigDecimal</code> value
	 *        from in order to return a <code>BigDecimal</code> object.
	 * @param object the (<code>BigDecimal</code>) object to be restored. In
	 *        this implementation it is ignored.
	 * @return the read <code>BigDecimal</code> object.
	 * @throws IOException if I/O errors occur.
	 */
	@Override
	public BigDecimal read(DataInput dataInput, BigDecimal object) throws IOException {
		return new BigDecimal(dataInput.readUTF());
	}

	/**
	 * Writes the <code>BigDecimal</code> value of the specified
	 * <code>BigDecimal</code> object to the specified data output.
	 *
	 * @param dataOutput the stream to write the <code>BigDecimal</code> value of
	 *        the specified <code>BigDecimal</code> object to.
	 * @param object the <code>BigDecimal</code> object that
	 *        <code>BigDecimal</code> value should be written to the data
	 *        output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@Override
	public void write(DataOutput dataOutput, BigDecimal object) throws IOException {
		dataOutput.writeUTF(object.toString());
	}
}
