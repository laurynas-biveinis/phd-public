/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import xxl.core.util.WrappingRuntimeException;

/**
 * This class wraps a DataInput to an InputStream.
 */
public class DataInputInputStream extends InputStream {
	/**
	 * The wrapped DataInput.
	 */
	protected DataInput di;
	
	/**
	 * Creates a DataInputInputStream which wraps a DataInput 
	 * to an InputStream.
	 * @param di The DataInput to be wrapped.
	 */
	public DataInputInputStream (DataInput di) {
		this.di = di;
	}
	
	/**
	 * Reads a byte from the DataInput.
	 * @return the byte read.
	 */
	public int read() {
		try {
			return di.readUnsignedByte();
		}
		catch (EOFException e) {
			return -1;
		}
		catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}
}
