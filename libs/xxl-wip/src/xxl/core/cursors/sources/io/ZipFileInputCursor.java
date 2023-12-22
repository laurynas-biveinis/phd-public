/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.cursors.sources.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import xxl.core.io.converters.Converter;
import xxl.core.util.WrappingRuntimeException;

/**
 * This class provides a cursor that reads the input from an entry of a given
 * zip file.
 *
 * @param <E> the type of the elements read from the underlying zip file.
 * @see java.util.Iterator
 * @see xxl.core.cursors.Cursor
 * @see xxl.core.cursors.sources.io.InputStreamCursor
 * @see java.util.zip.ZipFile
 */
public class ZipFileInputCursor<E> extends InputStreamCursor<E> {

	/**
	 * Constructs a new zip-file-input cursor that depends on the specified zip
	 * file and uses the specified converter in order to read out the
	 * serialized objects from the given zip entry.
	 *
	 * @param converter the converter that is used for reading out the
	 *        serialized objects of this iteration.
	 * @param file the zip file that contains the serialized objects of this
	 *        iteration.
	 * @param zipEntry the name of the entry inside the zip file containing the
	 *        data of the iteration.
	 * @param bufferSize the size of the buffer that is used for the file
	 *        input.
	 */
	public ZipFileInputCursor(Converter<? extends E> converter, File file, String zipEntry, int bufferSize) {
		super(null, converter);
		try {
			ZipFile zip = new ZipFile(file);
			input = new DataInputStream(
				new BufferedInputStream(
					zip.getInputStream(
						zip.getEntry(zipEntry)
					),
					bufferSize
				)
			);
		}
		catch (IOException ie) {
			throw new WrappingRuntimeException(ie);
		}
	}

	/**
	 * Constructs a new zip-file-input cursor that depends on the specified zip
	 * file and uses the specified converter in order to read out the
	 * serialized objects from the given zip entry. The buffer size is set to
	 * 4096 bytes.
	 *
	 * @param converter the converter that is used for reading out the
	 *        serialized objects of this iteration.
	 * @param file the zip file that contains the serialized objects of this
	 *        iteration.
	 * @param zipEntry the name of the entry inside the zip file containing the
	 *        data of the iteration.
	 */
	public ZipFileInputCursor(Converter<? extends E> converter, File file, String zipEntry) {
		this(converter, file, zipEntry, 4096);
	}
	
}
