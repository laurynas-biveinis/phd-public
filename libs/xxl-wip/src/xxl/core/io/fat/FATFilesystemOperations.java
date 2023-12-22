/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat;

import java.io.IOException;
import java.io.RandomAccessFile;

import xxl.core.io.FilesystemOperations;
import xxl.core.util.WrappingRuntimeException;

/**
 * This class implements the file system operations for the FAT.
 */
public class FATFilesystemOperations implements FilesystemOperations {
	
	/**
	 * FATDevice used for the content of the files.
	 */
	protected FATDevice fd;
	
	/**
	 * Creates a FilesystemOperations object which forwards all calls to the
	 * methods of a FATDevice.
	 * @param fd FATDevice that is used.
	 */
	public FATFilesystemOperations(FATDevice fd) {
		this.fd = fd;
	}
	
	/**
	 * Opens a file and returns a RandomAccessFile.
	 * @param fileName the name of the file.
	 * @param mode the access mode ("r" or "rw").
	 * @return the new RandomAccessFile or null, if the operation was not successful.
	 */
	public RandomAccessFile openFile(String fileName, String mode) {
		RandomAccessFile ra;
		try {
			ra = fd.getRandomAccessFile(fileName,mode);
		}
		catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
			
		return ra;
	}

	/**
	 * Determines if a file exists or not.
	 * @param fileName the name of the file to be checked.
	 * @return true iff the file exists.
	 */
	public boolean fileExists(String fileName) {
		return fd.fileExists(fileName);
	}

	/**
	 * Renames the name of a file to a new name.
	 * @param oldName the old name of the file.
	 * @param newName the new name of the file.
	 * @return true iff the operation completed successfully.
	 */
	public boolean renameFile(String oldName, String newName) {
		return fd.renameTo(oldName,newName);
	}

	/**
	 * Deletes a file.
	 * @param fileName the name of the file.
	 * @return true iff the operation completed successfully.
	 */
	public boolean deleteFile(String fileName) {
		return fd.delete(fileName);
	}
}
