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

package xxl.core.pipes.operators.windows;

import xxl.core.collections.queues.FIFOQueue;
import xxl.core.collections.queues.ListQueue;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.util.XXLSystem;

/**
 * Implementation of a count-based window. The end timestamp of an incoming element is
 * set to the start timestamp of the 'windowSize'-th future incoming element. 
 * This implementation assumes the start timestamps of incoming elements to be unique.
 * @param <E> 
 */
public class CountBasedWindow<E> extends AbstractTemporalPipe<E, E> implements MemoryMonitorable {

	protected FIFOQueue<TemporalObject<E>> queue; // size is at most windowSize-1
	protected boolean filled = false;
	protected int windowSize;
	protected TemporalObject<E> last;
	protected boolean checkedQueueObjectSize = false;
	protected int queueObjectSize = SIZE_UNKNOWN;
	
	public CountBasedWindow(FIFOQueue<TemporalObject<E>> queue, int windowSize) {
		this.queue = queue;
		this.windowSize = windowSize;
		if (windowSize == 0)
			throw new IllegalArgumentException("Window size 0 is not allowed.");
	}
	
	public CountBasedWindow(Source<? extends TemporalObject<E>> source, int sourceID, FIFOQueue<TemporalObject<E>> queue, int windowSize) {
		this(queue, windowSize);	
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
	
	public CountBasedWindow(Source<? extends TemporalObject<E>> source, int windowSize) {
		this(source, DEFAULT_ID, new ListQueue<TemporalObject<E>>(), windowSize);
	}
	
	// update stream -> windowSize is 1
	public CountBasedWindow(Source<? extends TemporalObject<E>> source) {
		this(source, DEFAULT_ID, null, 1);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(TemporalObject<E> tso, int sourceID) {
		if (windowSize == 1) {
			if (last != null)
				transfer(
					new TemporalObject<E>(last.getObject(),		
							new TimeInterval(last.getStart(), tso.getStart())
						) // end timestamp is the start timestamp of the next element
				);
		}
		else {
			if (!filled && queue.size() == windowSize) 
				filled = true;
			if (filled) {
				TemporalObject<E> next = queue.dequeue();
				transfer(
					new TemporalObject<E>(next.getObject(),		
						new TimeInterval(next.getStart(), tso.getStart())
					) // end timestamp is the start timestamp of the windowSize-th future element
				);
			}
			queue.enqueue(tso);
		}
		last = tso;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#close()
	 */
	@Override
	public void close() {
		super.close();
		queue.close();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#done(int)
	 */
	@Override
	public void done(int sourceID) {
		if (isClosed || isDone) return;
		
		processingWLock.lock();
		updateDoneStatus(sourceID);
	    try {
			if (isDone()) {
				while(!queue.isEmpty()) {
					TemporalObject<E> next = queue.dequeue();
					if (next != last)
					transfer(
						new TemporalObject<E>(next.getObject(),		
							new TimeInterval(next.getStart(), last.getStart()+1)
						) // end timestamp is set to the last start timestamp + 1 
					);
				}
				signalDone();
			}
	    }
	    finally {
	    	processingWLock.unlock();
	    }
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		if (!checkedQueueObjectSize && last != null) {
			try {	
				queueObjectSize = XXLSystem.getObjectSize(last);
			} catch (IllegalAccessException e) {
				queueObjectSize = MemoryMonitorable.SIZE_UNKNOWN;
			}
			checkedQueueObjectSize = true;
		}
		if (checkedQueueObjectSize) {
			if (windowSize == 1) 
				return queueObjectSize;
			if (windowSize > 1)
				return queue.size()* queueObjectSize;
		} 
		return SIZE_UNKNOWN;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Enumerator e = new Enumerator(500, 0);
		Mapper<Integer, TemporalObject<Integer>> m = new Mapper<Integer, TemporalObject<Integer>>(
			e, 
			new Function<Integer, TemporalObject<Integer>>() {
				protected long count = 0;

				@Override
				public TemporalObject<Integer> invoke(Integer o) {
					return new TemporalObject<Integer>(o,
							new TimeInterval(count += 5, count + 10));
				}
			}
		);

		int windowSize = 3;
		CountBasedWindow<Integer> w = new CountBasedWindow<Integer>(m, windowSize); // update semantics

		Pipes.verifyStartTimeStampOrdering(w);
		Printer printer = new Printer<TemporalObject<Integer>>(w);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);
	}

}
