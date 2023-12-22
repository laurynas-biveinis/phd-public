/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.collections.queues.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import xxl.core.functions.DecoratorFunction;
import xxl.core.functions.Function;
import xxl.core.io.CounterInputStream;
import xxl.core.io.CounterOutputStream;
import xxl.core.io.converters.Converter;

/**
 * This class provides a decorator for a random access file queue that
 * counts the number of bytes that are read from and written to the
 * underlying queue.
 * 
 * @see xxl.core.collections.queues.Queue
 * @see xxl.core.collections.queues.io.RandomAccessFileQueue
 */
public class CounterRandomAccessFileQueue extends RandomAccessFileQueue {

	/**
	 * Stores the number of bytes read from the underlying queue.
	 */
	protected long readCount = 0;

	/**
	 * Stores the number of bytes written to the underlying queue.
	 */
	protected long writeCount = 0;

	/**
	 * Creates a new counter random access file queue that counts the
	 * number of bytes read from and written to the specified random
	 * access file queue.
	 *
	 * @param filename the file that is internally used for storing the
	 *        elements of the queue.
	 * @param converter a converter that is used for serializing and
	 *        de-serializing the elements of the queue
	 * @param inputBufferSize a function that determines the size of the
	 *        input buffer.
	 * @param outputBufferSize a function that determines the size of the
	 *        output buffer.
	 */
	public CounterRandomAccessFileQueue(String filename, Converter converter, Function inputBufferSize, Function outputBufferSize) {
		super(filename, converter, inputBufferSize, outputBufferSize);
		newInputStream = new DecoratorFunction(super.newInputStream){
			public Object invoke() {
				return new CounterInputStream((InputStream)function.invoke()) {
					public void close() throws IOException {
						super.close();
						readCount += getCounter();
					}
				};
			}
		};
		newOutputStream = new DecoratorFunction(super.newOutputStream) {
			public Object invoke() {
				return new CounterOutputStream((OutputStream)function.invoke()) {
					public void close() throws IOException {
						super.close();
						writeCount += getCounter();
					}
				};
			}
		};
	}

	/**
	 * Returns the number of bytes that are read from the underlying
	 * random access file queue.
	 *
	 * @return the number of bytes that are read from the underlying
	 * random access file queue.
	 */
	public long getReadCount() {
		return readCount;
	}

	/**
	 * Resets the counter for read bytes to <tt>0</tt>.
	 */
	public void resetReadCount() {
		readCount = 0;
	}

	/**
	 * Returns the number of bytes that are written to the underlying
	 * random access file queue.
	 *
	 * @return the number of bytes that are written to the underlying
	 * random access file queue.
	 */
	public long getWriteCount( ){
		return writeCount;
	}

	/**
	 * Resets the counter for written bytes to <tt>0</tt>.
	 */
	public void resetWriteCount() {
		writeCount = 0;
	}
	
}
