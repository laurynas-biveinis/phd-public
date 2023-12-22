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
import java.sql.Timestamp;

/**
 * A Converter for object of type {@link java.sql.Timestamp}.
 */
public class TimestampConverter extends FixedSizeConverter<Timestamp> {

	/**
	 * This instance can be used for getting a default instance of a timestamp
	 * converter. It is similar to the <i>Singleton Design Pattern</i> (for
	 * further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of a
	 * timestamp converter.
	 */
    public static final TimestampConverter DEFAULT_INSTANCE = new TimestampConverter();

	/**
	 * This field contains the number of bytes needed to serialize the
	 * <code>long</code> value of a <code>Timestamp</code> object. Because this
	 * size is predefined it must not be measured each time.
	 */
	public static final int SIZE = 8;
	
	/**
	 * Sole constructor. (For invocation by subclass constructors, typically
	 * implicit.)
	 */
	public TimestampConverter() {
		super(SIZE);
	}
	
	/** 
	 * Reads the <code>long</code> value for the specified
	 * (<code>Timestamp</code>) object from the specified data input and
	 * returns the restored object.
	 * 
	 * @param dataInput the stream to read the <code>long</code> value from in
	 *        order to return an <code>Timestamp</code> object.
	 * @param object the (<code>Timestamp</code>) object to be restored.
	 * @return the read <code>Timestamp</code> object.
	 * @throws IOException if I/O errors occur.
	 */
    @Override
	public Timestamp read(DataInput dataInput, Timestamp object) throws IOException {
		long time = LongConverter.DEFAULT_INSTANCE.readLong(dataInput);
		if (object == null)
			object = new Timestamp(time);
		else
			object.setTime(time);
		return object;
    }

	/**
	 * Writes the <code>long</code> value of the specified
	 * <code>Timestamp</code> object to the specified data output.  
	 *
	 * @param dataOutput the stream to write the <code>long</code> value of the
	 *        specified <code>Timestamp</code> object to.
	 * @param object the <code>Timestamp</code> object that <code>long</code>
	 *        value should be written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@Override
	public void write(DataOutput dataOutput, Timestamp object) throws IOException {
	    LongConverter.DEFAULT_INSTANCE.writeLong(dataOutput, object.getTime());
	}
}
