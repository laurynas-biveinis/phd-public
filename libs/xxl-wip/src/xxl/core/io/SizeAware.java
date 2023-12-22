/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io;

/**
 * Interface which has only one method which returns the
 * size of the Object in main memory.
 */
public interface SizeAware {
	/**
	 * Returns the size in bytes in main memory of the current Object.
	 * @return The number of bytes.
	 */
	public int getMemSize();
}
