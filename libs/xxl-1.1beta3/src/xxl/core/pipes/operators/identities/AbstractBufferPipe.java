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

import java.util.ArrayList;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.pipes.memoryManager.MemoryManageable;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.scheduler.Controllable;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.util.XXLSystem;

/**
 * @param <I> 
 * @param <O> 
 *
 */
public abstract class AbstractBufferPipe<I,O> extends AbstractPipe<I,O> implements Controllable, MemoryManageable {
	
	protected Queue<I> buffer;
	
	protected int batchSize = 1;
		
	protected int objectSize;
	
	protected int assignedMemSize = 0;
	
	protected boolean checkObjectSize = false;
	
	public AbstractBufferPipe(Queue<I> buffer, int batchSize, int objectSize) {
		this.buffer = buffer;
		this.batchSize = batchSize;
		this.objectSize = objectSize;		
	}
	
	public AbstractBufferPipe(Source<? extends I>[] sources, int[] sourceIDs, Queue<I> buffer, int batchSize, int objectSize) {
		this(buffer, batchSize, objectSize);
		for (int i = 0; i < sources.length; i++)
			if (!Pipes.connect(sources[i], this, sourceIDs[i]))
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	@SuppressWarnings("unchecked")
	public AbstractBufferPipe(Source<? extends I> source, int sourceID, Queue<I> buffer, int batchSize, int objectSize) {
		this(new Source[]{source}, new int[]{sourceID}, buffer, batchSize, objectSize);
	}
	
	public AbstractBufferPipe(Source<? extends I> source, int batchSize, int objectSize) {
		this(source, DEFAULT_ID, new ListQueue<I>(new ArrayList<I>(batchSize)), batchSize, objectSize);
	}
	
	public AbstractBufferPipe(Source<? extends I> source, int objectSize) {
		this(source, 150, objectSize);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#open()
	 */
	@Override
	public void open() throws SourceIsClosedException {
		super.open();
		synchronized(buffer) {
			buffer.open();
		}
	}
	
	public AbstractBufferPipe(Source<? extends I> source) {
		this(source, MemoryMonitorable.SIZE_UNKNOWN);
		this.checkObjectSize = true;
	}	
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		if (checkObjectSize) {
			computeObjectSize(o);
		}
		synchronized(buffer) {
			buffer.enqueue(o);
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#close()
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed)
			synchronized(buffer) {
				buffer.close();
			}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#done(int)
	 */
	@Override
	public void done(int sourceID) {
		if (isClosed || isDone) return;
		processingWLock.lock();
		try {
			updateDoneStatus(sourceID);
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/**
	 * @return
	 */
	public int getBufferSize() {
	    synchronized(buffer) {
	        return buffer.size();
	    }
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.scheduler.Controllable#execute()
	 */
	public abstract void execute();
	
	/**
	 * @param batchSize
	 */
	public void setBatchSize(int batchSize) {
		processingWLock.lock();
		try {
			this.batchSize = batchSize;
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/**
	 * @return
	 */
	public int getBatchSize() {
		processingRLock.lock();
		try {
			return batchSize;
		}
		finally {
			processingRLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.scheduler.Controllable#isFinished()
	 */
	public boolean isFinished() {
		processingRLock.lock();
		try {
			return isClosed || (isDone && buffer.size() == 0);
		}
		finally {
			processingRLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#getObjectSize()
	 */
	public int getObjectSize() {
		return objectSize;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#getPreferredMemSize()
	 */
	public int getPreferredMemSize() {
		return MemoryManageable.MAXIMUM;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#getAssignedMemSize()
	 */
	public int getAssignedMemSize() {
		return assignedMemSize;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		if ( objectSize != SIZE_UNKNOWN )
			return getBufferSize()*objectSize;
		return SIZE_UNKNOWN;		
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryManageable#assignMemSize(int)
	 */
	public void assignMemSize(int newMemSize) {
		this.assignedMemSize = newMemSize;
		if (buffer instanceof MemoryManageable) 
		    ((MemoryManageable)buffer).assignMemSize(newMemSize);
	}
	
	/**
	 * @param o
	 */
	protected void computeObjectSize(I o) {		
		try {
			this.objectSize = XXLSystem.getObjectSize(o);
		} catch (IllegalAccessException e) {
			this.objectSize = MemoryMonitorable.SIZE_UNKNOWN;
		}
		checkObjectSize = false;		
	}

}
