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


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.queues.Heap;
import xxl.core.collections.sweepAreas.AbstractSweepArea;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;

/**
 * @param <E> 
 * 
 */
public class Split<E> extends AbstractTemporalPipe<E, E> implements MemoryMonitorable {
  
    public static class SplitSA<E> extends AbstractSweepArea<TemporalObject<E>> {
                
        protected Predicate<? super TemporalObject<E>> removePredicate;
        protected Heap<TemporalObject<E>> heap;
        protected long l;
        
        public SplitSA(Predicate<? super TemporalObject<E>> removePredicate, Heap<TemporalObject<E>> heap, long l, int objectSize) {
    	    super(objectSize);
        	this.removePredicate = removePredicate;	
    		this.heap = heap;
    		this.l = l;
    	}
        
        public SplitSA(Predicate<? super TemporalObject<E>> removePredicate, long l) {
    	   this(removePredicate, new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR), l, SIZE_UNKNOWN);
    	}
        
        public SplitSA(long l) {
            super(SIZE_UNKNOWN);
        	this.removePredicate = TemporalObject.getStartTSReorganizePredicate(true);
            this.heap = new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR);
            this.l = l;
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#insert(java.lang.Object)
         */
        @Override
		public void insert(TemporalObject<E> o) throws IllegalArgumentException {
            super.insert(o);
    		E object = o.getObject();
    		TimeInterval interval = o.getTimeInterval();
    		long start = interval.getStart();
    		long end = interval.getEnd();
    		while (start + l < end) {
    		    heap.enqueue(new TemporalObject<E>(object, new TimeInterval(start, start = start + l)));
    		}
    		if (start < end)
    		    heap.enqueue(new TemporalObject<E>(object, new TimeInterval(start, end)));
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#clear()
         */
        @Override
		public void clear() {
            heap.clear();            
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#close()
         */
        @Override
		public void close() {
           heap.close();            
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#size()
         */
        @Override
		public int size() {
            return heap.size();
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#iterator()
         */
        @Override
		public Iterator<TemporalObject<E>> iterator() throws UnsupportedOperationException {
            return heap.cursor();
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#query(java.lang.Object, int)
         */
        @Override
		public Iterator<TemporalObject<E>>  query(TemporalObject<E> o, int ID) throws IllegalArgumentException {
            return new EmptyCursor<TemporalObject<E>>();
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#query(E[], int[], int)
         */
        @Override
		public Iterator<TemporalObject<E>>  query(TemporalObject<E>[] os, int[] IDs, int valid) throws IllegalArgumentException {
            return new EmptyCursor<TemporalObject<E>>();
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#query(E[], int[])
         */
        @Override
		public Iterator<TemporalObject<E>>  query(TemporalObject<E>[] os, int[] IDs) throws IllegalArgumentException {
            return new EmptyCursor<TemporalObject<E>>();
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#expire(java.lang.Object, int)
         */
        @Override
		public Iterator<TemporalObject<E>>  expire(final TemporalObject<E> currentStatus, int ID)
                throws UnsupportedOperationException, IllegalStateException {
            return new AbstractCursor<TemporalObject<E>>() {
                @Override
				public boolean hasNextObject() {
                   return !heap.isEmpty() && removePredicate.invoke(heap.peek(), currentStatus);
                }
                
                @Override
				public TemporalObject<E> nextObject() {
                    return heap.dequeue();
                }
            };
        }

        /* (non-Javadoc)
         * @see xxl.core.collections.sweepAreas.AbstractSweepArea#reorganize(java.lang.Object, int)
         */
        @Override
		public void reorganize(TemporalObject<E> currentStatus, int ID) throws IllegalStateException {
    		throw new UnsupportedOperationException("Results are produced during reorganization; use method expire instead.");	
        }
        
    	/**
    	 * @return
    	 */
    	public long getMinTimeStamp() {
    		return heap.size() > 0 ? heap.peek().getStart() : 0;
    	}
    
    }
    
    protected SplitSA<E> sweepArea;
    
    
    public Split(SplitSA<E> sweepArea) {
    	this.sweepArea = sweepArea;
    }
    
    public Split(Source<? extends TemporalObject<E>> source, int sourceID, SplitSA<E> sweepArea) {
        this(sweepArea);
        if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
    }
    
    public Split(Source<? extends TemporalObject<E>> source, long l) {
        this(source, DEFAULT_ID, new SplitSA<E>(l));
    }
    
    /* (non-Javadoc)
     * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
     */
    @Override
	public void processObject(TemporalObject<E> o, int sourceID) throws IllegalArgumentException {	
		sweepArea.insert(o);
		transferIterator(sweepArea.expire(o, 0));
	}
    
    /**
     * @param it
     * @return
     */
    protected boolean transferIterator(Iterator<? extends TemporalObject<E>> it) {
		boolean ret = it.hasNext();
		while(it.hasNext())
			transfer(it.next());
		return ret;
	}
    
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#processHeartbeat(long, int)
	 */
	@Override
	protected long processHeartbeat(long minTS, int sourceID) {
		if (transferIterator(sweepArea.expire(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), sourceID)))
			return NO_HEARTBEAT;
		return sweepArea.size() >0 ?  Math.min(sweepArea.getMinTimeStamp(), minTS) : minTS;
	}
    
    /* (non-Javadoc)
     * @see xxl.core.pipes.operators.AbstractPipe#done(int)
     */
    @Override
	public void done(int sourceID) {
		if (isClosed) return;
		
		processingWLock.lock();
		updateDoneStatus(sourceID);
	    try {
			if (isDone()) {
				transferIterator(sweepArea.iterator());
				signalDone();
			}
		}
	    finally {
	    	processingWLock.unlock();
	    }
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#close()
	 */
	@Override
	public void close() {
		super.close();
		sweepArea.close();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		return sweepArea.getCurrentMemUsage();
	}
    
    public static void main(String[] args) {
    
        /*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		final int noOfElements = 1000;
		final int buckets = 10;
		final int startInc = 50;
		final int intervalSize = 20;
		final long seed = 42;
		final HashMap<Integer,List<Long>> input = new HashMap<Integer,List<Long>>();
		final Integer[] hashCodes = new Integer[buckets];
		for (int i = 0; i < buckets; i++)
			hashCodes[i] = new Integer(i);
	
		RandomNumber<Integer> r = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements, 0
		);			
		Source<TemporalObject<Integer>> s = Pipes.decorateWithRandomTimeIntervals(r, input, hashCodes, startInc, intervalSize, seed);
		Split<Integer> split = new Split<Integer>(s, 12);
		// check sort order
		Pipes.verifyLexicographicalOrdering(split);

		AbstractSink sink = new AbstractSink<TemporalObject<Integer>>(split) {
			LinkedList<TemporalObject<Integer>> list = new LinkedList<TemporalObject<Integer>>();
			
			@Override
			public void processObject(TemporalObject<Integer> o, int ID) {
				list.add(o);
			}
			
			@Override
			public void done(int sourceID) {
				super.done(sourceID);
				if (isDone()) {
					Iterator<TemporalObject<Integer>> listIt = list.iterator();
					HashMap<Integer,List<Long>> result = new HashMap<Integer,List<Long>>();
					while (listIt.hasNext()) {
						TemporalObject<Integer> t = listIt.next();
						int key = t.getObject().intValue();
						List<Long> l = result.get(hashCodes[key]);
						if (l == null) l = new LinkedList<Long>();
						l.addAll(TemporalObject.getAllSnapshots(t));					
						result.put(hashCodes[key], l);
					}
					// comparison 
					for (int i = 0; i < buckets; i++) {
						List<Long> in = input.get(hashCodes[i]);
						List<Long> out = result.get(hashCodes[i]);
						if (in != null && out != null) {
							Iterator<Long> it = in.iterator();					
							while (it.hasNext()) {
								Long next = it.next();
								if (!out.remove(next)) 
									System.err.println("ERROR: element lost: value "+i+" at time "+next);						
							}
							System.out.println("CHECK 1 finished for bucket "+i+".");
							
							it = in.iterator();
							while(it.hasNext()) {
								Long next = it.next();
								if (out.contains(next)) 
									System.err.println("ERROR: more elements than expected: value "+i+" at time "+next);
							}
							System.out.println("CHECK 2 finished for bucket "+i+".");
						}
						else {
							if (in != null && out == null)
								System.err.println("ERROR: Input, but no output. (i = "+i+")");
							if (in == null && out != null)
								System.err.println("ERROR: Output, but no input. (i = "+i+")");
						}
					}
				}
			}
		};
		Printer printer = new Printer<TemporalObject<Integer>>(split);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(printer);
		exec.startAllQueries();
		
    }

}
