/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.raw;

/**
 * Interface for raw file that contains sectors. This interface is implemented 
 * in NativeRawAccess, -RAF, and -RAM.
 * 
 */
public interface RawAccess {

	/**
	 * Opens a device/file.
	 *
	 * @param filename name of file or device
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void open(String filename) throws RawAccessException;

	/**
	 * Closes a device/file.
	 *
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void close() throws RawAccessException;

	/**
	 * Writes a sector of a characteristic length to the file/device.
	 *
	 * @param block byte array which will be written to the sector
	 * @param sector number of the sector in the file/device where the block will be written
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void write(byte[] block, long sector) throws RawAccessException;

	/**
	 * Reads a sector of characteristic Bytes length from the file/device.
	 *
	 * @param block byte array of which will be written to the sector
	 * @param sector number of the sector in the file/device from where the block will be read
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void read(byte[] block, long sector) throws RawAccessException;

	/**
	 * Returns the amount of sectors in the file/device.
	 *
	 * @return amount of sectors
	 */
	public long getNumSectors();

	/**
	 * Returns the size of a sector of the file/device.
	 *
	 * @return size of sectors
	 */
	public int getSectorSize();
}
