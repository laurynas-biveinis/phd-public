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
 * Error which will be thrown if something has a wrong length.
 */
public class WrongLength extends Exception
{
	/**
	 * The length that was used.
	 */
	long length;

	
	/**
	 * Create an instance of this object.
	 * @param str error message.
	 * @param length the used length.
	 */
	public WrongLength(String str, long length)
	{
		super(str+" Wrong length. The length was "+length);
		this.length = length;
	}	//end constructor

}	//end class WrongLength
