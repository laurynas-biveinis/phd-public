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

package xxl.core.pipes.operators.distincts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ImplementorBasedSweepArea;
import xxl.core.collections.sweepAreas.SweepAreaImplementor;
import xxl.core.functions.Function;
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
import xxl.core.predicates.And;
import xxl.core.predicates.Or;
import xxl.core.predicates.Predicate;


/**
 * Operator component in a query graph that performs a
 * duplicate elimination (distinct operation) over an input stream
 * based on a sweepline statusstructure.
 *
 * ordering by only start timestamps
 *
 * @see xxl.core.collections.sweepAreas.SweepArea
 * @since 1.1
 */
public class TemporalDistinct<E> extends AbstractTemporalPipe<E,E> implements MemoryMonitorable {
	   
	@SuppressWarnings("hiding")
	public static class TemporalDistinctSA<E> extends ImplementorBasedSweepArea<TemporalObject<E>> {
    	
    	public TemporalDistinctSA(SweepAreaImplementor<TemporalObject<E>> impl, Predicate<? super TemporalObject<E>> queryPredicate, Predicate<? super TemporalObject<E>> removePredicate, int objectSize) {
    		super(impl, DEFAULT_ID, true, new And<TemporalObject<E>>(queryPredicate, TemporalObject.VALUE_EQUIVALENCE_PREDICATE), new Or<TemporalObject<E>>(TemporalObject.INTERVAL_OVERLAP_REORGANIZE, removePredicate), 1, objectSize);
    	}
    	
    	public TemporalDistinctSA(SweepAreaImplementor<TemporalObject<E>> impl) {
    		super(impl, DEFAULT_ID, true, TemporalObject.VALUE_EQUIVALENCE_PREDICATE, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, 1, SIZE_UNKNOWN);
    	}
    	
    	public TemporalDistinctSA() {
    		this(new HashSAImplementor<TemporalObject<E>>(
    			new Function<TemporalObject<E>,Integer>() {
    				@Override
					public Integer invoke(TemporalObject<E> o) {
    					return o.getObject().hashCode();
    				}
    			}, 1)
    		);		
    	}
    	
    	public void update(TemporalObject<E> o1, TemporalObject<E> o2) {
    		impl.update(o1, o2);
    	}
    	    	
    }
    
    
    protected TemporalDistinctSA<E> sweepArea;
    // heap size is at most number of distinct values
    protected DynamicHeap<TemporalObject<E>> heap;
   	
    public TemporalDistinct(TemporalDistinctSA<E> sweepArea) {		
		this.sweepArea = sweepArea;		
		this.heap = new DynamicHeap<TemporalObject<E>>(TemporalObject.START_TIMESTAMP_COMPARATOR); // order by start timestamps
	}
    
	/** 
	 * Creates a new Distinct as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately. 
	 * The duplicate elements of <CODE>source</CODE> are removed from the stream.
	 * 
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription to the source.
	 * @param sweepArea TemporalDistinctSweepArea managing the elements delivered by the source.
	 */
   	public TemporalDistinct(Source<? extends TemporalObject<E>> source, int sourceID, TemporalDistinctSA<E> sweepArea) {		
		this(sweepArea);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
   	
   	public TemporalDistinct(Source<? extends TemporalObject<E>> source, Function<? super TemporalObject<E>,Integer> hashFunction) {		
		this(source, DEFAULT_ID, new TemporalDistinctSA<E>(new HashSAImplementor<TemporalObject<E>>(hashFunction, 1)));	
	}
   	
	public TemporalDistinct(Source<? extends TemporalObject<E>> source) {		
		this(source, DEFAULT_ID, new TemporalDistinctSA<E>());	
	}

	/**
	 * Checks, if the given element is already contained in
	 * the sweep area. If not, the element is inserted into it and transferred
	 * to this pipe's subscribed sinks. The sweep area is reorganized thereafter.
	 *
	 * @param o The element streaming in.
	 * @param ID One of The IDs this pipe specified during its subscription by the underlying sources. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
	 */
	@Override
	public void processObject(TemporalObject<E> o, int sourceID) throws IllegalArgumentException {
		sweepArea.reorganize(o, sourceID);
		transferHeap(o.getStart());
		
		Iterator<? extends TemporalObject<E>> results = sweepArea.query(o, 0);
		if (results.hasNext()) {
			TemporalObject<E> res = results.next();
			if (o.getEnd() < res.getEnd()) // duplicate
    			return;
			if (res.getTimeInterval().overlaps(o.getTimeInterval()) && res.getEnd() < o.getEnd()) {
				TemporalObject<E> remainder = new TemporalObject<E>(o.getObject(), new TimeInterval(res.getEnd(), o.getEnd()));
				sweepArea.update(res, remainder);
				heap.enqueue(remainder);
			}
		}
		else {
			transfer(o);
			sweepArea.insert(o);
		}
	}

	/**
	 * Calls <CODE>super.close()</CODE> and closes
	 * the sweep area.
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed)
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
				while(!heap.isEmpty())
					transfer(heap.dequeue());
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
		return sweepArea.getCurrentMemUsage() + heap.size()*sweepArea.getObjectSize();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#processHeartbeat(long, int)
	 */
	@Override
	protected long processHeartbeat(long minTS, int sourceID) {
		sweepArea.reorganize(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), 0);
		if (transferHeap(minTS))
			return NO_HEARTBEAT;
		if (!heap.isEmpty())
			return Math.min(heap.peek().getStart(), minTS);
		return minTS;
	}
	
	/**
	 * @param timeStamp
	 * @return
	 */
	protected boolean transferHeap(long timeStamp) {
		boolean ret = false;
		while (!heap.isEmpty() && heap.peek().getStart() <= timeStamp) {
			ret = true;
			transfer(heap.dequeue()); 
		}
		return ret;
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
	
		final int noOfElements = 10000;
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
		TemporalDistinct<Integer> d = new TemporalDistinct<Integer>(s, 0, new TemporalDistinctSA<Integer>());
		
		Pipes.verifyStartTimeStampOrdering(d);
		
		AbstractSink sink = new AbstractSink<TemporalObject<Integer>>(d) {
			LinkedList<TemporalObject<Integer>> list = new LinkedList<TemporalObject<Integer>>();
			
			@Override
			public void processObject(TemporalObject<Integer> o, int sourceID) {	
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
					// check for multiplicity 
					for (int i = 0; i < buckets; i++) {
						List<Long> in = input.get(hashCodes[i]);
						List<Long> out = result.get(hashCodes[i]);
						if (in != null && out != null) {
							Iterator<Long> it = in.iterator();					
							while (it.hasNext()) {
								Long next = it.next();
								if (!out.contains(next)) 
									System.err.println("ERROR: element lost: value "+i+" at time "+next);						
							}
							System.out.println("CHECK 1 finished for bucket "+i+".");
							it = out.iterator();
							while (it.hasNext()) {
								Long next = it.next();
								if (!in.contains(next)) 
									System.err.println("ERROR: more elements than expected: value "+i+" at time "+next);
								it.remove();
								if (out.contains(next)) 
									System.err.println("ERROR: duplicates detected: value "+i+" at time "+next);						
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
		
		Printer printer = new Printer<TemporalObject<Integer>>(d);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(printer);
		exec.startAllQueries();
	}
}
