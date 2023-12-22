/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

import xxl.core.util.WrappingRuntimeException;

/**
 * This class which wraps a DataOutput to an OutputStream.
 */
public class DataOutputOutputStream extends OutputStream {
	/**
	 * DataOuput which is wrapped.
	 */
	protected DataOutput dout;

	/**
	 * Constructs a new Wrapper, which wraps a DataOutput to
	 * an OutputStream.
	 * @param dout DataOutput to be wrapped.
	 */
	public DataOutputOutputStream (DataOutput dout) {
		this.dout = dout;
	}

	/**
	 * Writes a byte to the DataOutput.
	 * @param i byte value to be written.
	 */
	public void write(int i) {
		try {
			dout.write(i);
		}
		catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}
}
