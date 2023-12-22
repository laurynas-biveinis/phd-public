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

import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;

/**
 * This class provides a converter that is able to read and write time stamped
 * objects. A second converter is needed that is internally used for reading
 * and writing the wrapped object of a time stamped object.
 *
 * @param <T> the type of the objects wrapped by the time stamped objects that
 *        can be read and written by this converter.
 * @see DataInput
 * @see DataOutput
 * @see IOException
 */
public class TemporalObjectConverter<T> extends Converter<TemporalObject<T>> {

	/**
	 * The converter that is internally used for reading and writing the
	 * wrapped object of a time stamped object.
	 */
	protected Converter<T> objectConverter;
	
	/**
	 * Constructs a new converter that is able to read an write time stamped
	 * objects by using the given comparator for reading and writing the
	 * wrapped object of a time stamped object.
	 * 
	 * @param objectConverter a converter that is internally used for reading and
	 *        writing the wrapped object of a time stamped object.
	 */
	public TemporalObjectConverter(Converter<T> objectConverter) {
		this.objectConverter = objectConverter;
	}
	
	/**
	 * Reads the time interval and the wrapped object for the specified time
	 * stamped object from the specified data input and returns the restored
	 * time stamped object.
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
	public TemporalObject<T> read(DataInput dataInput, TemporalObject<T> object) throws IOException {
		TimeInterval interval = TimeIntervalConverter.DEFAULT_INSTANCE.read(dataInput);
		T newObject = objectConverter.read(dataInput);
		
		if (object == null)
			return new TemporalObject<T>(newObject, interval);
		
		object.setObject(newObject);
		object.setTimeInterval(interval);
		return object;
	}

	/**
	 * Writes the time interval and the wrapped object of the time stamped
	 * object to the specified data output.
	 * 
	 * @param dataOutput the stream to write the time interval and the wrapped
	 *        object of the time stamped object to.
	 * @param object the time stamped object whose time interval and wrapped
	 *        object should be written to the data output.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	@Override
	public void write(DataOutput dataOutput, TemporalObject<T> object) throws IOException {
		TimeIntervalConverter.DEFAULT_INSTANCE.write(dataOutput, object.getTimeInterval());
		objectConverter.write(dataOutput, object.getObject());
	}
}
