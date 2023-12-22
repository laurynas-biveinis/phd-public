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
import java.io.IOException;

/**
 * Wraps a DataInput and counts the number of bytes which
 * are read. The methods that return a String are not supported.
 */
public class CounterDataInput implements DataInput {
	/**
	 * Number of bytes read so far.
	 */
	protected int size;
	/**
	 * The DataInput which is wrapped.
 	 */
	protected DataInput di;
	
	/**
	 * Constructs a new DataInput which counts the bytes
	 * which are read from a wrapped DataInput object.
	 * @param di DataInput which is wrapped.
	 */
	public CounterDataInput(DataInput di) {
		this.di = di;
		size = 0;
	}
	/**
	 * Returns the number of bytes read so far.
	 * @return The number of bytes read so far.
	 */
	public int getCounter() {
		return size;
	}
	/**
	 * @see java.io.DataInput#readFully(byte[])
	 */
	public void readFully(byte[] b) throws IOException {
		size += b.length;
		di.readFully(b);
	}
	/**
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	public void readFully(byte[] b, int off, int len) throws IOException {
		size += len;
		di.readFully(b, off, len);
	}
	/**
	 * @see java.io.DataInput#skipBytes(int)
	 */
	public int skipBytes(int n) throws IOException {
		size += n;
		return di.skipBytes(n);
	}
	/**
	 * @see java.io.DataInput#readBoolean()
	 */
	public boolean readBoolean() throws IOException {
		size++;
		return di.readBoolean();
	}
	/**
	 * @see java.io.DataInput#readByte()
	 */
	public byte readByte() throws IOException {
		size++;
		return di.readByte();
	}
	/**
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	public int readUnsignedByte() throws IOException {
		size++;
		return di.readUnsignedByte();
	}
	/**
	 * @see java.io.DataInput#readShort()
	 */
	public short readShort() throws IOException {
		size += 2;
		return di.readShort();
	}
	/**
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	public int readUnsignedShort() throws IOException {
		size += 2;
		return di.readUnsignedShort();
	}
	/**
	 * @see java.io.DataInput#readChar()
	 */
	public char readChar() throws IOException {
		size += 2;
		return di.readChar();
	}
	/**
	 * @see java.io.DataInput#readInt()
	 */
	public int readInt() throws IOException {
		size += 4;
		return di.readInt();
	}
	/**
	 * @see java.io.DataInput#readLong()
	 */
	public long readLong() throws IOException {
		size += 8;
		return di.readLong();
	}
	/**
	 * @see java.io.DataInput#readFloat()
	 */
	public float readFloat() throws IOException {
		size += 4;
		return di.readFloat();
	}
	/**
	 * @see java.io.DataInput#readDouble()
	 */
	public double readDouble() throws IOException {
		size += 8;
		return di.readDouble();
	}
	/**
	 * This method is not supported by the counter.
	 * @return throws an UnsupportedOperationException.
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}
	/**
	 * This method is not supported by the counter.
	 * @return throws an UnsupportedOperationException.
	 * @throws IOException
	 */
	public String readUTF() throws IOException {
		throw new UnsupportedOperationException();
	}
}
