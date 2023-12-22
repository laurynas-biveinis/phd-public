/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.util;

import java.io.FilenameFilter;

import xxl.core.io.fat.ExtendedFile;

/**
 * Instances of classes that implement this interface are used to filter filenames.
 * These instances are used to filter directory listings in
 * the list method of class ExtendedFile.
 */
public interface ExtendedFilenameFilter extends FilenameFilter
{
	/**
	 * Tests if a specified file should be included in a file list.
	 * @param dir directory in which the file was found.
	 * @param name name of the file.
	 * @return true if and only if the name should be included in the file list; false otherwise.
	 */
	public boolean accept(ExtendedFile dir, String name);
}	//end ExtendedFilenameFilter
