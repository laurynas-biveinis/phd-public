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
 * Error that is thrown in case the name of the file is illegal, that is, if it
 * contains chars that are not allowed.
 */
public class IllegalName extends DirectoryException
{
	/**
	 * Create an instance of this object.
	 * @param str the name of the file which contains the illegal chars.
	 */
	public IllegalName(String str)
	{
		super("Name contains one or more characters that are not allowed. "+str);
	}	//end constructor.

}	//end class IllegalName
