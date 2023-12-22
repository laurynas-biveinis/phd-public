/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.metaData;

/**
 * A metadata exception is thrown to indicate that an application has attempted
 * to access a metadata object in an inappropriate way. For example, trying to
 * access a non-existent metadata fragment of a
 * {@link CompositeMetaData composite metadata} object will yield such an
 * exception.
 * 
 * @since 1.1
 */
public class MetaDataException extends RuntimeException {
	
	/**
	 * Constructs a metadata exception with the specified detail message.
	 * 
	 * @param message a detail message explaining what's the cause of this
	 *        exception.
	 */
	public MetaDataException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a metadata exception with no detail message.
	 */
	public MetaDataException() {
		this("");
	}
	
}
