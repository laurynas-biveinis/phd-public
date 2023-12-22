/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.converters;


/**
 * This abstract class is a special kind of converter which serializes each
 * object to the same number of bytes. This class has the additional method
 * <code>getSize</code> which has to be implemented by subclasses.
 *
 * @param <T> the type of the object that can be converted by using this
 *        converter.
 * @see Converter
 */
public abstract class FixedSizeConverter<T> extends SizeConverter<T> {

	/**
	 * The (fixed) size of a converted object.
	 */
	private final int size;

	/**
	 * Constructs a converter which always converts its objects to size bytes.
	 * 
	 * @param size The size of the converted objects.
	 */
	public FixedSizeConverter(int size) {
		this.size = size;
	}

	/**
	 * Returns the number of bytes used for serialization/deserialization. Each
	 * object must be serialized to the same number of bytes.
	 * 
	 * @return the number of bytes.
	 */
	public int getSerializedSize() {
		return size;
	}

	/**
	 * Returns the number of bytes used for serialization/deserialization of a
	 * special object.
	 * 
	 * @param o the object.
	 * @return the number of bytes.
	 */
	@Override
	public int getSerializedSize(T o) {
		return size;
	}
}
