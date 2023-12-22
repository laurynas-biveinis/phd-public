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
 * Error which will be thrown if this sector is not a FSI-sector.
 * @see xxl.core.io.fat.FSI
 */
public class NotFSISector extends Exception
{
	/**
	 * The sector which is not a FSI-sector.
	 */
	byte[] sector;
	
	/**
	 * The sector number of the sector. If the
	 * sector number is negativ the sector number
	 * is unknown.
	 */
	long sectorNumber;
	
	
	/**
	 * Create an instance of this object.
	 * @param sector which is not a FSI-sector.
	 */
	public NotFSISector(byte[] sector)
	{
		this(sector, -1);
	}	//end constructor
	
	
	/**
	 * Create an instance of this object.
	 * @param sector which is not a FSI-sector.
	 * @param sectorNumber the sector number of the sector.
	 */
	public NotFSISector(byte[] sector, long sectorNumber)
	{
		super("This sector is no valid FSInfo sector");
		this.sector  = sector;
		this.sectorNumber = sectorNumber;
	}	//end constructor
}	//end class NotFSISector
