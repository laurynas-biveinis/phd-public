/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.errors;

import java.io.IOException;

/**
 * This class is the top exception of this project. It indicates if there
 * was an exception which belongs to the DIR, FAT, BPB, FSI, and classes
 * which use them.
 */
public class DirectoryException extends IOException
{
	/**
	 * Create an instance of this object.
	 * @param str error message.
	 */
	public DirectoryException(String str)
	{
		super(str);
	}	//end constructor.

}	//end class DirectoryException
