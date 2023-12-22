/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.io.raw;

/**
 * Decorates a RawAccess.
 */
public class DecoratorRawAccess implements RawAccess {

	/**
	 * The RawAccess to be decorated.
	 */
	protected RawAccess ra;
	
	/**
	 * Constructs a new decorator for a RawAccess.
	 * @param ra The RawAccess to be decorated.
	 */
	public DecoratorRawAccess(RawAccess ra) {
		this.ra = ra;
	}

	/**
	 * Opens a device/file.
	 *
	 * @param filename name of file or device
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void open(String filename) throws RawAccessException {
		ra.open(filename);
	}

	/**
	 * Closes a device/file.
	 *
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void close() throws RawAccessException {
		ra.close();
	}

	/**
	 * Writes a sector of a characteristic length to the file/device.
	 *
	 * @param block byte array which will be written to the sector
	 * @param sector number of the sector in the file/device where the block will be written
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void write(byte[] block, long sector) throws RawAccessException {
		ra.write(block, sector);
	}

	/**
	 * Reads a sector of characteristic Bytes length from the file/device.
	 *
	 * @param block byte array of which will be written to the sector
	 * @param sector number of the sector in the file/device from where the block will be read
	 * @exception RawAccessException a specialized RuntimeException
	 */
	public void read(byte[] block, long sector) throws RawAccessException {
		ra.read(block, sector);
	}

	/**
	 * Returns the amount of sectors in the file/device.
	 *
	 * @return amount of sectors
	 */
	public long getNumSectors() {
		return ra.getNumSectors();
	}

	/**
	 * Returns the size of a sector of the file/device.
	 *
	 * @return size of sectors
	 */
	public int getSectorSize() {
		return ra.getSectorSize();
	}

	/**
	 * Outputs a String representation of the raw device.
	 * @return A String representation.
	 */
	public String toString()  {
		return "Decorator raw access of: "+ra;
	}

	/**
	 * Returns the internally used RawAccess.
	 * @return Returns the internally used RawAccess.
	 */
	public RawAccess getRawAccess() {
		return ra;
	}
}
