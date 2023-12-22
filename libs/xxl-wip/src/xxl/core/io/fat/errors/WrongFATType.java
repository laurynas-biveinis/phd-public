/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.errors;

import xxl.core.io.fat.FAT;

/**
 * Error which will be thrown if an operation is used that is only valid for an other FAT type.
 */
public class WrongFATType extends Exception
{
	/**
	 * Create an instance of this object.
	 * @param isFatType the actual FAT type.
	 */
	public WrongFATType(byte isFatType)
	{
		super("The actual fat type is "+isFatType+", but needed is one\n of the following types "+FAT.FAT12+", "+FAT.FAT16+", or "+FAT.FAT32);
	}	//end constructor
	
		
	/**
	 * Create an instance of this object.
	 * @param isFatType the actual FAT type.
	 * @param neededFatType the FAT type that is needed.
	 */
	public WrongFATType(byte isFatType, byte neededFatType)
	{
		super("The actual fat type is "+isFatType+", but needed is fat type "+neededFatType);
	}	//end constructor

}	//end class WrongFATType
