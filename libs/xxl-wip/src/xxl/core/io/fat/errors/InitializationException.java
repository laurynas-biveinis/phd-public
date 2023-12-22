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
 * Error which will be thrown if something couldn't be initialized.
 */
public class InitializationException extends DirectoryException
{
	/**
	 * Create an instance of this object.
	 * @param str error message.
	 */
	public InitializationException(String str)
	{
		super(str);
	}	//end constructor

}	//end class InitializationException
