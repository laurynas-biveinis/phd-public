/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.raw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Implements RawAcces with java.io.RandomAccessFile (->RAF).
 */
public class RAFRawAccess implements RawAccess {

	/**
	 * java.io.RandomAccessFile-Handle for the device/file.
	 */
	private RandomAccessFile myRAF = null;
	
	/** Indicates if there are synchronization calls */
	private boolean useSync;
	/**
	 * Size of a sector
	 */
	private int sectorSize;

	/**
	 * Returns a new instance of a raw access that uses a RandomAccessFile.
	 *
	 * @param filename name of device or file
	 * @param useSync make a synchronization call after each read/write
	 * @param sectorSize size of a sector
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public RAFRawAccess(String filename, boolean useSync, int sectorSize) throws RawAccessException {
		this.useSync = useSync;
		this.sectorSize = sectorSize;
		open(filename);
	}

	/**
	 * Returns a new instance of a raw access that uses a RandomAccessFile
	 * (with sector size 512 bytes).
	 *
	 * @param filename name of device or file
	 * @param useSync make a synchronization call after each read/write
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public RAFRawAccess(String filename, boolean useSync) throws RawAccessException {
		this(filename, useSync, 512);
	}

	/**
	 * Returns a new instance of a raw access that uses a RandomAccessFile.
	 *
	 * @param filename name of device or file
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public RAFRawAccess(String filename) throws RawAccessException {
		this(filename,false);
	}

	/**
	 * Opens a device or file
	 * See super class for detailed description
	 *
	 * @param filename name of device or file
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void open(String filename) throws RawAccessException {
		try {
			if (!(new File(filename)).exists())
				throw new RawAccessException("RAFRawAccess: open() cannot access device");
			myRAF = new RandomAccessFile(filename, "rw");
		}
		catch (FileNotFoundException e) {
			throw new RawAccessException("RAFRawAccess: open() device/file not found");
		}
	}

	/**
	 * Closes device or file
	 * See super class for detailed description
	 *
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void close() throws RawAccessException {
		// Anything there instanciated to close?
		if (myRAF == null)
			throw new RawAccessException("RAFRawAccess: close() no device open");
		try {
			// lets try it
			myRAF.close();
		}
		catch (IOException e) {
			throw new RawAccessException("RAFRawAccess: " + e.toString());
		}
	}

	/**
	 * Writes block to file/device
	 * See super class for detailed description
	 *
	 * @param block array to be written
	 * @param sector number of the sector
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void write(byte[] block, long sector) throws RawAccessException {
		if (block.length != sectorSize)
			throw new RawAccessException("RAFRawAccess: write() wrong block length");
		if (myRAF == null)
			throw new RawAccessException("RAFRawAccess: write() no device/file open");
		if (sector >= getNumSectors())
			throw new RawAccessException("RAFRawAccess: write() sector out of bounds");
		try {
			myRAF.seek(sector * sectorSize);
			myRAF.write(block);
			if(useSync) myRAF.getFD().sync();
		}
		catch (IOException e) {
			throw new RawAccessException("RAFRawAccess: write() " + e.toString());
		}
	}

	/**
	 * Reads block from file/device
	 * See super class for detailed description
	 *
	 * @param block byte array of sectorSize bytes for the sector
	 * @param sector number of the sector
	 */
	public void read(byte[] block, long sector) {
		if (myRAF == null)
			throw new RawAccessException("RAFRawAccess: read() no device/file open");
		try {
			myRAF.seek(sector * sectorSize);
			if (myRAF.read(block) != sectorSize)
				throw new RawAccessException("RAFRawAccess: read() failed");
			if(useSync) myRAF.getFD().sync();
		}
		catch (IOException e) {
			throw new RawAccessException("RAFRawAccess: open() " + e.toString());
		}
	}

	/**
	 * Returns the amount of sectors in the file/device.
	 *
	 * @return amount of sectors
	 */
	public long getNumSectors() {
		if (myRAF == null)
			return -1;
		try {
			return  (myRAF.length() / sectorSize);
		}
		catch (java.io.IOException e) {
		}
		return -1;
	}

	/**
	 * Returns the size of a sector of the file/device.
	 *
	 * @return size of sectors
	 */
	public int getSectorSize() {
		return sectorSize;
	}

	/**
	 * Outputs a String representation of the raw device.
	 * @return A String representation.
	 */
	public String toString()  {
		return 
			"RAF raw access, sectors: "+getNumSectors()+
			", sectorSize: "+getSectorSize();
	}
}
