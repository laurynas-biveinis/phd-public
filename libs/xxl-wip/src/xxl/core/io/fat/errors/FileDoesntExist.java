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
 * Error class that is be thrown if the specified filename doesn't exist.
 */
public class FileDoesntExist extends DirectoryException
{
	/**
	 * Create an instance of this object.
	 * @param name the name of the file that doesn't exist.
	 */
	public FileDoesntExist(String name)
	{
		super("The specified directory/file with the\n name: "+name+" doesn't exist");
	}	//end constructor

}	//end class FileDoesntExist
