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

import xxl.core.pipes.elements.TimeInterval;

/**
 * This class provides a converter that is able to read and write time
 * intervals.
 *
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class TimeIntervalConverter extends FixedSizeConverter<TimeInterval> {

	/**
	 * This instance can be used for getting a default instance of a time
	 * interval converter. It is similar to the <i>Singleton Design Pattern</i>
	 * (for further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of a
	 * time interval converter.
	 */
	public static final TimeIntervalConverter DEFAULT_INSTANCE = new TimeIntervalConverter();

	/**
	 * This field contains the number of bytes needed to serialize the two
	 * <code>long</code> values (start and end timestamp) of a time interval.
	 * Because this size is predefined it must not be measured each time.
	 */
	public static final int SIZE = 16;

	/**
	 * Sole constructor. (For invocation by subclass constructors, typically
	 * implicit.)
	 */
	public TimeIntervalConverter() {
		super(SIZE);
	}

	/**
	 * Reads the two <code>long</code> values (start and end timestamp) for the
	 * specified time interval from the specified data input and returns the
	 * restored object.
	 *
	 * <p>This implementation ignores the specified object and returns a new
	 * time interval. So it does not matter when the specified object is
	 * <code>null</code>.</p>
	 * 
	 * @param dataInput the stream to read the two <code>long</code> values
	 *        (start and end timestamp) from in order to return a time
	 *        interval.
	 * @param object the time interval to be restored. In this implementation
	 *        it is ignored.
	 * @return the read time interval.
	 * @throws IOException if I/O errors occur.
	 */
	@Override
	public TimeInterval read(DataInput dataInput, TimeInterval object) throws IOException {
		long start = dataInput.readLong();
		long end = dataInput.readLong();
		return new TimeInterval(start, end);
	}

	/**
	 * Writes the two <code>long</code> values (start and end timestamp) of the
	 * time interval to the specified data output.
	 * 
	 * @param dataOutput the stream to write the two <code>long</code> values
	 *        (start and end timestamp) of the specified time interval to.
	 * @param object the time interval whose two <code>long</code> values
	 *        (start and end timestamp) should be written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@Override
	public void write(DataOutput dataOutput, TimeInterval object) throws IOException{
		dataOutput.writeLong(object.getStart());
		dataOutput.writeLong(object.getEnd());
	}

}
