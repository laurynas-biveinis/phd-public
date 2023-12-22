/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.converters;

import java.io.DataOutputStream;
import java.io.IOException;

import xxl.core.io.CounterOutputStream;
import xxl.core.io.NullOutputStream;
import xxl.core.util.WrappingRuntimeException;

/**
 * This abstract class represents a special kind of converter which provides a
 * method which returns the size of an object in bytes before doing the
 * serialization.
 *
 * @param <T> the type of the object that can be converted by using this
 *        converter.
 * @see Converter
 */
public abstract class SizeConverter<T> extends Converter<T> {

	/**
	 * Returns the number of bytes used for serialization/deserialization of a
	 * special object. This method should be overwritten to become performant.
	 * 
	 * @param object the object of which the size is returned.
	 * @return the number of bytes.
	 */
	public int getSerializedSize(T object) {
		try {
			CounterOutputStream dOut = new CounterOutputStream(NullOutputStream.NULL);
			write(new DataOutputStream(dOut), object);
			return (int)dOut.getCounter();
		}
		catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}
}
