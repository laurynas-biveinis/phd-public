/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.util;

import java.io.FileFilter;

import xxl.core.io.fat.ExtendedFile;

/**
 * A filter for pathnames. 
 * Instances of this interface may be passed to the listFiles(ExtendedFileFilter)
 * method of the ExtendedFileFilter class. 
 */
public interface ExtendedFileFilter extends FileFilter
{
	/**
	 * Tests whether or not the specified pathname should be included in a pathname list.
     * @param pathname the pathname to be tested.
     * @return true if and only if pathname should be included.
	 */
	public boolean accept(ExtendedFile pathname);
}	//end class ExtendedFileFilter
