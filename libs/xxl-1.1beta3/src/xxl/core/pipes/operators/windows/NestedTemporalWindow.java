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


import java.util.ArrayList;
import java.util.Iterator;

import xxl.core.collections.MapEntry;
import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.queues.Heap;
import xxl.core.collections.sweepAreas.AbstractSweepArea;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.wrappers.QueueCursor;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.operators.mappers.TimeGranularity;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;


/**
 * An operator for continuous queries with time-based windows, 
 * for instance fixed and sliding windows. For use in nested queries, when two or more
 * window operators occur on a path from a source to a sink.
 * 
 * assumption: elements arrive at this operator with the specified time granularity
 * @param <E> 
 */
public class NestedTemporalWindow<E> extends AbstractTemporalPipe<E, E> implements MemoryMonitorable {
    
	public static final Function<Long,Function<Long,Long>> SLIDING_WINDOW_FACTORY () {
		return new Function<Long,Function<Long,Long>>() {
			
			@Override
			public Function<Long,Long> invoke(final Long currentWindowSize, final Long currentTimeGranularity) {
				return new Function<Long,Long>() {
	
					@Override
					public Long invoke(Long start) {
						long end = start + currentWindowSize;
						// adjust end to current time granularity
						long mod = end % currentTimeGranularity;
						if (mod > 0)
							end -= mod;
						return end;
					}
				};
			}
		};
	}
	
	public static final Function<Long,Function<Long,Long>> FIXED_WINDOW_FACTORY (final long offset) {
		return new Function<Long,Function<Long,Long>>() {
			
			@Override
			public Function<Long,Long> invoke(final Long currentWindowSize, final Long currentTimeGranularity) {
				return new Function<Long,Long>() {
					
					protected long currentStart = offset;
					protected long currentEnd;
	
					@Override
					public Long invoke(Long start) {
						currentEnd = currentStart + currentWindowSize;
						while (start >= currentEnd) {
							currentStart = currentEnd;
							currentEnd = currentStart + currentWindowSize;
						}
	
						// adjust end to current time granularity
						long mod = currentEnd % currentTimeGranularity;
						if (mod > 0)
							currentEnd -= mod;
						return currentEnd;
					}
				};
			}
		};
	}
	
	/**
	 *
	 * @param <E>
	 */
	public static class TemporalWindowSA<E> extends AbstractSweepArea<TemporalObject<E>> {

		protected Predicate<? super TemporalObject<E>> removePredicate;
        protected Heap<TemporalObject<E>> heap;
		
		public TemporalWindowSA(Predicate<? super TemporalObject<E>> removePredicate, Heap<TemporalObject<E>> heap, int objectSize) {
    	    super(objectSize);
    		this.removePredicate = removePredicate;
    		this.heap = heap;
    	}
		
		public TemporalWindowSA(Predicate<? super TemporalObject<E>> removePredicate, int objectSize) {
    	    this(removePredicate, new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR), objectSize);
    	}
		
		public TemporalWindowSA() {
    	    super(SIZE_UNKNOWN);
    	    this.removePredicate = TemporalObject.getStartTSReorganizePredicate(true);
    		this.heap = new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR);
    	}
		
		@Override
		public void insert(TemporalObject<E> o) throws IllegalArgumentException {
    		super.insert(o);
    	    heap.enqueue(o);    		
    	}
		
		@Override
		public void clear() {
			heap.clear();
		}

		@Override
		public void close() {
			heap.close();
		}

		@Override
		public int size() {
			return heap.size();
		}

		@Override
		public Iterator<TemporalObject<E>> iterator() {
			// is destructive
			return new QueueCursor<TemporalObject<E>>(heap);
		}

		@Override
		public Iterator<TemporalObject<E>> query(TemporalObject<E> o, int ID) throws IllegalArgumentException {
			return new EmptyCursor<TemporalObject<E>>();
		}

		@Override
		public Iterator<TemporalObject<E>> query(TemporalObject<E>[] os, int[] IDs, int valid) throws IllegalArgumentException {
			return new EmptyCursor<TemporalObject<E>>();
		}

		@Override
		public Iterator<TemporalObject<E>> query(TemporalObject<E>[] os, int[] IDs) throws IllegalArgumentException {
			return new EmptyCursor<TemporalObject<E>>();
		}

		@Override
		public Iterator<TemporalObject<E>> expire(final TemporalObject<E> currentStatus, int ID) throws UnsupportedOperationException, IllegalStateException {
			return new AbstractCursor<TemporalObject<E>>() {
				@Override
				public boolean hasNextObject() {
    				if (heap.isEmpty())
    				    return false;
    				return removePredicate.invoke(heap.peek(), currentStatus);
    			}
    			
    			@Override
				public TemporalObject<E> nextObject() {
    				return heap.dequeue();
    			}
			};
		}

		@Override
		public void reorganize(TemporalObject<E> currentStatus, int ID) throws UnsupportedOperationException, IllegalStateException {
			throw new UnsupportedOperationException("Results are produced during reorganization; use method expire instead.");	
    	}
	
	}
	
	
	protected TemporalWindowSA<E> sweepArea;
	
	// MapEntry -> time instant x window size / time granularity
	// Lists have to be ordered by time
	protected ArrayList<MapEntry<Long,Long>> windowSizesList = new ArrayList<MapEntry<Long,Long>>();
	protected ArrayList<MapEntry<Long,Long>> timeGranularitiesList = new ArrayList<MapEntry<Long,Long>>();
	   
    protected long lastStart = Long.MIN_VALUE;
    protected Function<Long,Long> windowFunction;
    protected Function<Long,Function<Long,Long>> windowFactory;
    protected long timeGranularity;
    protected long windowSize;
    
    public NestedTemporalWindow(TemporalWindowSA<E> sweepArea, Function<Long,Function<Long,Long>> windowFactory, long initialWindowSize, long initialTimeGranularity) {
		this.windowFactory = windowFactory;
		addChangeOfWindowSize(0, initialWindowSize);
		addChangeOfTimeGranularity(0, initialTimeGranularity);
		this.sweepArea = sweepArea;
	}
    
	public NestedTemporalWindow(Source<? extends TemporalObject<E>> source, int ID, TemporalWindowSA<E> sweepArea, Function<Long,Function<Long,Long>> windowFactory, long initialWindowSize, long initialTimeGranularity) {
		this(sweepArea, windowFactory, initialWindowSize, initialTimeGranularity);
		if (!Pipes.connect(source, this, ID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
	
	public NestedTemporalWindow(Source<? extends TemporalObject<E>> source, TemporalWindowSA<E> sweepArea, Function<Long,Function<Long,Long>> windowFactory, long initialWindowSize, long initialTimeGranularity) {
		this(source, DEFAULT_ID, sweepArea, windowFactory, initialWindowSize, initialTimeGranularity);
	}
	
	// default: sliding window 
	public NestedTemporalWindow(Source<? extends TemporalObject<E>> source, long initialSlidingWindowSize, long initialTimeGranularity) {
		this(source, new TemporalWindowSA<E>(), SLIDING_WINDOW_FACTORY(), initialSlidingWindowSize, initialTimeGranularity);
	}
	
	// default: sliding window, time granularity = 1 ms
	public NestedTemporalWindow(Source<? extends TemporalObject<E>> source, long initialSlidingWindowSize) {
		this(source, initialSlidingWindowSize, 1);
	}
	
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(TemporalObject<E> tso, int sourceID) {
		//System.out.println("input: "+tso);
		E object = tso.getObject();
		long start = tso.getStart();
		long end = tso.getEnd();
		lastStart = start;
		boolean changeDetected = false;
		
		// detect change of time granularity
		if (timeGranularitiesList.size() > 0 && start >= timeGranularitiesList.get(0).getKey()) {
			timeGranularity = timeGranularitiesList.get(0).getValue();
			timeGranularitiesList.remove(0);
			// TODO: Wechsel in Metadaten erfassen (optional)
			changeDetected = true;
		}
		// detect change of window size
		if (windowSizesList.size() > 0 && start >= windowSizesList.get(0).getKey()) {
			windowSize = windowSizesList.get(0).getValue();
			windowSizesList.remove(0);
			// TODO: Wechsel in Metadaten erfassen (Pflicht)
			changeDetected = true;
		}
		
		if (changeDetected) { // adjust window size to new time granularity
			long mod = windowSize%timeGranularity;
			if (mod > 0) windowSize -= mod;
			windowFunction = windowFactory.invoke(windowSize, timeGranularity);
			changeDetected = false;
		}
		
		if (start%timeGranularity > 0)
			throw new IllegalArgumentException("Start timestamps are not compatible with the specified time granularity.");
		
		for (long newEnd; start < end; start += timeGranularity) {
			newEnd = windowFunction.invoke(start); // compute new end timestamp
			if (start < newEnd) // valid time interval
				sweepArea.insert(new TemporalObject<E>(object, new TimeInterval(start, newEnd)));			
		}
		Iterator<? extends TemporalObject<E>> results = sweepArea.expire(tso, 0);
		while (results.hasNext())
			transfer(results.next());
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
				Iterator<? extends TemporalObject<E>> remainder = sweepArea.iterator();
				while(!isClosed() && remainder.hasNext())
					transfer(remainder.next());
				signalDone();
			}
	    }
	    finally {
	    	processingWLock.unlock();
	    }
	}
	
	/**
	 * @param pointInTime
	 * @param newWindowSize
	 */
	public void addChangeOfWindowSize(long pointInTime, long newWindowSize) {
		if (pointInTime <= lastStart)
			throw new IllegalArgumentException("It is not possible to change the window size for the past.");
		if (windowSizesList.size() > 0 && pointInTime <= windowSizesList.get(windowSizesList.size()-1).getKey())
			throw new IllegalArgumentException("Another window size corresponding to the given point in time has been specified already.");
		windowSizesList.add(new MapEntry<Long,Long>(pointInTime, newWindowSize));
	}
	
	/**
	 * @param pointInTime
	 * @param newTimeGranularity
	 */
	public void addChangeOfTimeGranularity(long pointInTime, long newTimeGranularity) {
		if (pointInTime <= lastStart)
			throw new IllegalArgumentException("It is not possible to change the time granularity for the past.");
		if (timeGranularitiesList.size() > 0 && pointInTime <= timeGranularitiesList.get(timeGranularitiesList.size()-1).getKey())
			throw new IllegalArgumentException("Another time granularity corresponding to the given point in time has been specified already.");
		timeGranularitiesList.add(new MapEntry<Long,Long>(pointInTime, newTimeGranularity));
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		return sweepArea.getCurrentMemUsage();
	}
	
    /**
     * @param args
     */
    public static void main(String[] args) {
        
    	Enumerator e = new Enumerator(500, 0);
        Mapper<Integer,TemporalObject<Integer>> m = new Mapper<Integer,TemporalObject<Integer>>(e, 
        	new Function<Integer,TemporalObject<Integer>>() {
	            protected long count = 0;
	            
	            @Override
				public TemporalObject<Integer> invoke(Integer o) {
	                return new TemporalObject<Integer>(o, new TimeInterval(count+=5, count+10));
	            }
        	}
        );
        
        long timeGranularity = 5;
        long windowSize = 150;
        
        TimeGranularity<Integer> g = new TimeGranularity<Integer>(m, timeGranularity);
        
        NestedTemporalWindow<Integer> w = new NestedTemporalWindow<Integer>(g, windowSize, timeGranularity); // sliding window (100 ms)
        //TemporalWindow<Integer> w = new TemporalWindow<Integer>(m, FIXED_WINDOW_FACTORY(0), 100, 1); // fixed window (100 ms)
        
//        w.addChangeOfTimeGranularity(5, 10);
//        w.addChangeOfTimeGranularity(10, 5);
//        w.addChangeOfTimeGranularity(15, 3);
//        w.addChangeOfWindowSize(5, 50);
//        w.addChangeOfWindowSize(10, 25);
//        w.addChangeOfWindowSize(15, 10);
        
        Pipes.verifyStartTimeStampOrdering(w);
        Printer printer = new Printer<TemporalObject<Integer>>(w);
        
        QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);
    }
}
