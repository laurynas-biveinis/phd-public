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
 * Error which will be thrown if something has a wrong value.
 */
public class InvalidValue extends Exception
{
	/**
	 * The value that was used.
	 */
	long value;
	
	/**
	 * Create an instance of this object.
	 * @param str error message.
	 * @param value the used value.
	 */
	public InvalidValue(String str, long value)
	{
		super(str+" Wrong value. The value is "+value);
		this.value = value;
	}	//end constructor

}	//end class InvalidValue
