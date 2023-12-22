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
package xxl.core.pipes.operators.identities;

import xxl.core.collections.queues.Queue;
import xxl.core.pipes.sources.Source;

/**
 * A pipe containing an internal buffer to decouple pipes in a query graph.
 * The elements recieved from sources by <code>process()</code> are stored in a buffer
 * realized by <code>xxl.collections.Queue</code>. The <code>transfer()</code> method
 * takes a specified number of objects from the buffer and passes them to the subscribed sinks.
 * @param <I> 
 *
 * @see xxl.core.pipes.operators.AbstractPipe
 * @see xxl.core.pipes.scheduler.Strategy
 * @see xxl.core.collections.queues.Queue
 * @since 1.1
 */
public class BufferPipe<I> extends AbstractBufferPipe<I,I> {
	
	protected boolean signalledDone = false;
	
	public BufferPipe(Queue<I> buffer, int batchSize, int objectSize) {
		super(buffer, batchSize, objectSize);
	}
	
	public BufferPipe(Source<? extends I>[] sources, int[] sourceIDs, Queue<I> buffer, int batchSize, int objectSize) {
		super(sources, sourceIDs, buffer, batchSize, objectSize);
	}
	
	public BufferPipe(Source<? extends I> source, int sourceID, Queue<I> buffer, int batchSize, int objectSize) {
		super(source, sourceID, buffer, batchSize, objectSize);
	}
	
	public BufferPipe(Source<? extends I> source, int batchSize, int objectSize) {
		super(source, batchSize, objectSize);
	}
	
	public BufferPipe(Source<? extends I> source, int objectSize) {
		super(source, objectSize);
	}
	
	public BufferPipe(Source<? extends I> source) {
		super(source);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.identities.AbstractBufferPipe#execute()
	 */
	@Override
	public void execute() {
		if (isClosed || signalledDone) return;
		synchronized(buffer) {
			int size  = buffer.size() >= batchSize ? batchSize : buffer.size();
			for (int i = 0; i < size; i++) 
				transfer(buffer.dequeue());
			if (isDone && buffer.size() == 0) {
				signalDone();
				signalledDone = true;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.identities.AbstractBufferPipe#isFinished()
	 */
	@Override
	public boolean isFinished() {
		processingRLock.lock();
		try {
			return isClosed || signalledDone;
		}
		finally {
			processingRLock.unlock();
		}
	}
	
}
