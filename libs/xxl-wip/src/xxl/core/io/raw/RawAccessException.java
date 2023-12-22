/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.raw;

/**
 * Error which will be thrown if a raw access operation fails.
 */
public class RawAccessException extends RuntimeException {
	/**
	 * Create an instance of this object.
	 */
	public RawAccessException() {
		super();
	}

	/**
	 * Create an instance of this object.
	 *
	 * @param type special name for the error
	 */
	public RawAccessException(String type) {
		super(type);
	}
}
