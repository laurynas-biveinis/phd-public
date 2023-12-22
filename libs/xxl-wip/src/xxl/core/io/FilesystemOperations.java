/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io;

import java.io.RandomAccessFile;

/**
 * This interface encapsulates the operations usually needed on a filesystem.
 * By switching such an object, other classes store their information on
 * a certain filesystem.
 *
 * @see xxl.core.collections.containers.io.BlockFileContainer
 */
public interface FilesystemOperations {
	/**
	 * Opens a file and returns a RandomAccessFile.
	 * @param fileName the name of the file.
	 * @param mode the access mode ("r" or "rw").
	 * @return the new RandomAccessFile or null, if the operation was not successful.
	 */
	public RandomAccessFile openFile(String fileName, String mode);

	/**
	 * Determines if a file exists or not.
	 * @param fileName the name of the file to be checked.
	 * @return true iff the file exists.
	 */
	public boolean fileExists(String fileName);

	/**
	 * Renames the name of a file to a new name.
	 * @param oldName the old name of the file.
	 * @param newName the new name of the file.
	 * @return true iff the operation completed successfully.
	 */
	public boolean renameFile(String oldName, String newName);

	/**
	 * Deletes a file.
	 * @param fileName the name of the file.
	 * @return true iff the operation completed successfully.
	 */
	public boolean deleteFile(String fileName);
}
