/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.errors;

/**
 * Error which will be thrown if there is already a file with this name in the same
 * directory.
 */
public class NameAllreadyExists extends DirectoryException
{
	/**
	 * Create an instance of this class.
	 * @param name the name of the file that already exist.
	 */
	public NameAllreadyExists(String name)
	{
		super("There exists already a directory/file with the name: "+name);
	}	//end constructor.

}	//end class NameAllreadyExists
