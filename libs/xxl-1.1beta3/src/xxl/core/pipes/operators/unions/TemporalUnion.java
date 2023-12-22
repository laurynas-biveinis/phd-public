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

package xxl.core.pipes.operators.unions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;

/**
 * The temporal union operator.
 *
 * @since 1.1
 */
public class TemporalUnion<E> extends AbstractTemporalPipe<E,E> implements MemoryMonitorable{

    public static class TemporalUnionSA<E> extends AbstractSweepArea<TemporalObject<E>> {

        protected Predicate<? super TemporalObject<E>>[] removePredicates;
    	protected Heap<TemporalObject<E>> heap;
    
    	public TemporalUnionSA(Predicate<? super TemporalObject<E>>[] removePredicates, Heap<TemporalObject<E>> heap, int objectSize) {
    		super(objectSize);
    		this.removePredicates = new Predicate[removePredicates.length];
    	    for (int i = 0; i < removePredicates.length; i++) 
    			this.removePredicates[i] = removePredicates[i];
    		this.heap = heap;
    	}

    	public TemporalUnionSA(Predicate<? super TemporalObject<E>>[] removePredicates, Heap<TemporalObject<E>> heap) {
    		this(removePredicates, heap, SIZE_UNKNOWN);
    	}
   	
    	public TemporalUnionSA(Predicate<? super TemporalObject<E>> removePredicate, int dim, Heap<TemporalObject<E>> heap, int objectSize) {
    	    super(objectSize);
    		this.removePredicates = new Predicate[dim];
    	    for (int i = 0; i < dim; i++) 
    			this.removePredicates[i] = removePredicate;	
    		this.heap = heap;
    	}

    	public TemporalUnionSA(Predicate<? super TemporalObject<E>> removePredicate, int dim, Heap<TemporalObject<E>> heap) {
    		this(removePredicate, dim, heap, SIZE_UNKNOWN);
    	}
    	

    	public TemporalUnionSA(int dim, Heap<TemporalObject<E>> heap, int objectSize) {
    		this(TemporalObject.getStartTSReorganizePredicate(true), dim, heap, objectSize);
    	}
    	
    	public TemporalUnionSA(Predicate<? super TemporalObject<E>>[] removePredicates) {
    		this(removePredicates, new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR));
    	}
    	
    	public TemporalUnionSA(Predicate<? super TemporalObject<E>> removePredicate, int dim) {
    		this(removePredicate, dim, new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR));
    	}
    	
    	public TemporalUnionSA(int dim, int objectSize) {
    		this(dim, new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR), objectSize);
    	}
    	
    	public TemporalUnionSA(int dim) {
    		this(dim, SIZE_UNKNOWN);
    	}
    	
    	@Override
		public void insert(TemporalObject<E> o) throws IllegalArgumentException {
    		super.insert(o);
    		heap.enqueue(o);    		
    	}
    	
    	@Override
		public Iterator<TemporalObject<E>> expire(final TemporalObject<E> currentStatus, final int ID) {
  			return new AbstractCursor<TemporalObject<E>>() {
 
    			@Override
				public boolean hasNextObject() {
    				if (heap.isEmpty())
    				    return false;
    				return removePredicates[ID].invoke(heap.peek(), currentStatus);
    			}
    			
    			@Override
				public TemporalObject<E> nextObject() {
    				return heap.dequeue();
    			}
    		};
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
    	
    	// is destructive
    	@Override
		public Iterator<TemporalObject<E>> iterator() throws UnsupportedOperationException {
    		return new QueueCursor<TemporalObject<E>>(heap);
    	}
    	
    	@Override
		public Iterator<TemporalObject<E>> query(TemporalObject<E> o, int ID) throws IllegalArgumentException {
    		return new EmptyCursor<TemporalObject<E>>();
    	}

    	@Override
		public Iterator<TemporalObject<E>> query(TemporalObject<E> [] os, int [] IDs, int valid) throws IllegalArgumentException {
    		return new EmptyCursor<TemporalObject<E>>();
    	}

    	@Override
		public Iterator<TemporalObject<E>> query(TemporalObject<E> [] os, int [] IDs) throws IllegalArgumentException {
    		return new EmptyCursor<TemporalObject<E>>();
    	}
    	
    	@Override
		public void reorganize(TemporalObject<E> currentStatus, int ID) throws IllegalStateException {
    		throw new UnsupportedOperationException("Results are produced during reorganization; use method expire instead.");	
    	}	
    }
    
	protected TemporalUnionSA<E> sweepArea;
	
	public TemporalUnion(TemporalUnionSA<E> sweepArea) {
		this.sweepArea = sweepArea;
	}
	
	/** 
	 * Creates a new Union as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately.
	 *
	 * @param sources This pipe gets subscribed to the specified sources.
	 * @param sourceIDs This pipe uses the given IDs for subscription.
	 * @param sweepArea TemporalUnionSweepArea managing the elements delivered by the source.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != IDs.length</CODE>.
	 */ 
	public TemporalUnion(Source<? extends TemporalObject<E>>[] sources, int[] sourceIDs, TemporalUnionSA<E> sweepArea) {
		this(sweepArea);
		for (int i = 0; i < sources.length; i++)
			if (!Pipes.connect(sources[i], this, sourceIDs[i]))
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
	}
	
	public TemporalUnion(Source<? extends TemporalObject<E>>[] sources, TemporalUnionSA<E> sweepArea) {
		this(sweepArea);
		for (int i = 0; i < sources.length; i++)
			if (!Pipes.connect(sources[i], this, i))
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
	}
	
	@SuppressWarnings("unchecked")
	public TemporalUnion(Source<? extends TemporalObject<E>> source1, Source<? extends TemporalObject<E>> source2, int sourceID_0, int sourceID_1, TemporalUnionSA<E> sweepArea) {
		this(new Source[]{source1, source2}, new int[]{sourceID_0, sourceID_1}, sweepArea);
	}
	
	@SuppressWarnings("unchecked")
	public TemporalUnion(Source<? extends TemporalObject<E>> source1, Source<? extends TemporalObject<E>> source2, TemporalUnionSA<E> sweepArea) {
		this(new Source[]{source1, source2}, sweepArea);
	}
	
	public TemporalUnion(Source<? extends TemporalObject<E>>[] sources) {
		this(sources, new TemporalUnionSA<E>(sources.length));
	}
	
	@SuppressWarnings("unchecked")
	public TemporalUnion(Source<? extends TemporalObject<E>> source1, Source<? extends TemporalObject<E>> source2) {
		this(new Source[]{source1, source2});
	}

	/** 
	 * The given element is transferred
	 * to all of this pipe's subscribed sinks by calling <CODE>super.transfer(o)</CODE>. 
	 *
	 * @param o The element streaming in.
	 * @param ID One of the IDs this pipe specified during its subscription by the underlying sources. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(TemporalObject<E> o, int ID) throws IllegalArgumentException {
		transferIterator(sweepArea.expire(new TemporalObject<E>(o.getObject(), new TimeInterval(minTimeStamp, o.getEnd())), ID));
		sweepArea.insert(o);
	}
	
	protected boolean transferIterator(Iterator<? extends TemporalObject<E>> it) {
		boolean ret = it.hasNext();
		while(it.hasNext())
			transfer(it.next());
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#close()
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed()) 
			sweepArea.close();
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
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		return sweepArea.getCurrentMemUsage();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#processHeartbeat(long, int)
	 */
	@Override
	protected long processHeartbeat(long minTS, int sourceID) {		
		// works as long as all remove predicates in temporalunion are the same 
		return transferIterator(sweepArea.expire(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), sourceID<0 ? 0: sourceID)) ? NO_HEARTBEAT : minTS;
	}
			
	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		final int noOfElements1 = 10000;
		final int noOfElements2 = 5000;
		final int buckets = 10;
		final int startInc = 25;
		final int intervalSize = 50;
		final List<TemporalObject<Integer>> in = Collections.synchronizedList(new ArrayList<TemporalObject<Integer>>());
		
		final Integer[] hashCodes = new Integer[buckets];
		for (int i = 0; i < buckets; i++)
			hashCodes[i] = new Integer(i);
			
		RandomNumber<Integer> r1 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements1, 0
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, 0
		);		
		
		final Function<Object,Function<Integer,TemporalObject<Integer>>> decorateWithRandomIntervals = new Function<Object,Function<Integer,TemporalObject<Integer>>>() {
			@Override
			public Function<Integer,TemporalObject<Integer>> invoke() {
				return new Function<Integer,TemporalObject<Integer>>() {
					Random random = new Random();
					long start;
					long newStart, newEnd;
					
					@Override
					public TemporalObject<Integer> invoke(Integer o) {
						newStart = start+random.nextInt(startInc);
						newEnd   = newStart+1+random.nextInt(intervalSize-1);
						TemporalObject<Integer> object = new TemporalObject<Integer>(o%buckets, 
							new TimeInterval(newStart, newEnd)
						);
						start = newStart;
						in.add(object);
						return object;
					}
				};
			}
		};
				
		Source<TemporalObject<Integer>> s1 = new Mapper<Integer, TemporalObject<Integer>>(r1, decorateWithRandomIntervals.invoke());
		Source<TemporalObject<Integer>> s2 = new Mapper<Integer, TemporalObject<Integer>>(r2, decorateWithRandomIntervals.invoke());
	
		TemporalUnion<Integer> u = new TemporalUnion<Integer>(s1, s2);
		
		Pipes.verifyStartTimeStampOrdering(u);
		
		AbstractSink sink = new AbstractSink<TemporalObject<Integer>>(u) {
			List<TemporalObject<Integer>> out = new LinkedList<TemporalObject<Integer>>();
			
			@Override
			public void processObject(TemporalObject<Integer> o, int ID) {				
				out.add(o);
			}
			
			@Override
			public void done(int sourceID) {
				super.done(sourceID);
				if (isDone()) {
					Iterator<TemporalObject<Integer>> it = in.iterator();
					it = in.iterator();
					while (it.hasNext()) {
						TemporalObject<Integer> o1 = it.next();
						if (!out.contains(o1))
							System.err.println("ERROR: element lost: "+o1);
					}
					System.out.println("CHECK 1 finished.");
					
					it = out.iterator();
					while (it.hasNext()) {
						TemporalObject<Integer> o1 = it.next();
						if (!in.contains(o1))
							System.err.println("ERROR: more elements than expected: "+o1);
					}
					System.out.println("CHECK 2 finished.");
				}
			}
		};
		Printer printer = new Printer<TemporalObject<Integer>>(u);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(printer);
		exec.startAllQueries();
	}
	
}