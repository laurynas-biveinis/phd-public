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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ListSA;
import xxl.core.collections.sweepAreas.ListSAImplementor;
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
import xxl.core.predicates.Predicate;

/**
 * @param <E> 
 * 
 */
/**
 * @author heinzch
 *
 * @param <E>
 */
public class Coalesce<E> extends AbstractTemporalPipe<E,E> implements MemoryMonitorable {

    public static class CoalesceSA<E> extends ListSA<TemporalObject<E>> {
    	
    	public static Predicate<TemporalObject<?>> ADJACENT_INTERVALS = new Predicate<TemporalObject<?>>() {
    		@Override
			public boolean invoke(TemporalObject<?> object1, TemporalObject<?> newElement) {
    			return object1.getEnd() == newElement.getStart();
    		}
    	};
    	
    	public static Predicate<TemporalObject<?>> getReorganizationPredicate(final long epsilon) {
    		return new Predicate<TemporalObject<?>>() {
    			@Override
				public boolean invoke(TemporalObject<?> object1, TemporalObject<?> newElement) {
    				return  newElement.getStart() > object1.getEnd() ||
    						object1.getEnd() - object1.getStart() > epsilon;
    			}
    		};
    	}
    	

    	public CoalesceSA(SweepAreaImplementor<TemporalObject<E>> impl, Predicate<? super TemporalObject<E>> queryPredicate, Predicate<? super TemporalObject<E>> removePredicate, List<TemporalObject<E>> list, int objectSize) {
    		super(impl, DEFAULT_ID, true, ReorganizeModes.LAZY_REORGANIZE, new And<TemporalObject<E>>(queryPredicate, ADJACENT_INTERVALS), removePredicate, 1, list, objectSize);
    	}

    	public CoalesceSA(SweepAreaImplementor<TemporalObject<E>> impl, Predicate<? super TemporalObject<E>> queryPredicate, List<TemporalObject<E>> list, long epsilon, int objectSize) {
    		super(impl, DEFAULT_ID, true, ReorganizeModes.LAZY_REORGANIZE, new And<TemporalObject<E>>(queryPredicate, ADJACENT_INTERVALS), getReorganizationPredicate(epsilon), 1, list, objectSize);
    	}
    	
    	public CoalesceSA(SweepAreaImplementor<TemporalObject<E>> impl, List<TemporalObject<E>> list, long epsilon) {
    		this(impl, TemporalObject.VALUE_EQUIVALENCE_PREDICATE, list, epsilon, SIZE_UNKNOWN);
    	}

    	public CoalesceSA(SweepAreaImplementor<TemporalObject<E>> impl, Predicate<? super TemporalObject<E>> queryPredicate, Predicate<? super TemporalObject<E>> removePredicate) {
    		super(impl, DEFAULT_ID, true,  new And<TemporalObject<E>>(queryPredicate, ADJACENT_INTERVALS), removePredicate, 1);
    	}

    	public CoalesceSA(SweepAreaImplementor<TemporalObject<E>> impl, Predicate<? super TemporalObject<E>> queryPredicate, long epsilon) {
    		super(impl, DEFAULT_ID, true, new And<TemporalObject<E>>(queryPredicate, ADJACENT_INTERVALS), getReorganizationPredicate(epsilon), 1);
    	}

    	public CoalesceSA(SweepAreaImplementor<TemporalObject<E>> impl, long epsilon) {
    		this(impl, TemporalObject.VALUE_EQUIVALENCE_PREDICATE, epsilon);
    	}
    	
    	public CoalesceSA(Function<? super TemporalObject<E>,Integer> hashFunction, long epsilon) {
    		this(new HashSAImplementor<TemporalObject<E>>(hashFunction, 1), epsilon);
    	}
    	    	
    	public void update(TemporalObject<E> oldObject, TemporalObject<E> coalescedObject) {
    		// update object in SweepArea 
    		int index = Collections.binarySearch(list, oldObject, TemporalObject.START_TIMESTAMP_COMPARATOR); 
    		TemporalObject<E> current = list.get(index);
    		boolean updated = false;
			if (current.equals(oldObject)) {
				list.set(index, coalescedObject);
				updated = true;
			}
			if (!updated) {
				TemporalObject<E> suc;
				for (int j = index+1, size = list.size(); j < size; j++) {
					suc = list.get(j);
					if (oldObject.getStart() < suc.getStart())
						break;
					if (suc.equals(oldObject)) {
						index = j;
						list.set(index, coalescedObject);
						updated = true;
						break;
					}
				}
			}
			if (!updated) {
				TemporalObject<E> pre;
				for (int j = index-1; j >= 0; j--) {
					pre = list.get(j);
					if (oldObject.getStart() > pre.getStart()) 
						break;
					if (pre.equals(oldObject)) {
						index = j;
						list.set(index, coalescedObject);
						updated = true;
						break;
					}
				}
			}
			for (TemporalObject<E> cur, next; index+1 < list.size()
				&& (cur = list.get(index)).getStart() == (next = list.get(index+1)).getStart()
				&& cur.getEnd() > next.getEnd(); index++) {
				list.set(index, next);
				list.set(index+1, cur);
			}
			try {
				impl.update(oldObject, coalescedObject);
			}
			catch (UnsupportedOperationException uoe) {
				impl.remove(oldObject);
				impl.insert(coalescedObject);
			}
			return;
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.ListSA#reorganize(java.lang.Object, int)
    	 */
    	@Override
		public void reorganize(TemporalObject<E> currentStatus, int ID) throws IllegalStateException {
    		throw new UnsupportedOperationException("Results are produced during reorganization; use method expire instead.");	
    	}
    	
    	public long getMinTimeStamp() {
    		return list.size() > 0 ? list.get(0).getStart() : 0;
    	}
    	
    }
    
    
	protected CoalesceSA<E> sweepArea;

	public Coalesce(CoalesceSA<E> sweepArea) {
		this.sweepArea = sweepArea;
	}
	
	public Coalesce(Source<? extends TemporalObject<E>> source, int sourceID, CoalesceSA<E> sweepArea) {
		this(sweepArea);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
	
	public Coalesce(Source<? extends TemporalObject<E>> source, CoalesceSA<E> sweepArea) {
		this(source, DEFAULT_ID, sweepArea);
	}
	
	public Coalesce(Source<? extends TemporalObject<E>> source, Function<? super TemporalObject<E>,Integer> hashFunction, long epsilon) {
		this(source, DEFAULT_ID, new CoalesceSA<E>(hashFunction, epsilon));
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(TemporalObject<E> o, int sourceID) throws IllegalArgumentException {
		// expiration
		Iterator<? extends TemporalObject<E>> results = sweepArea.expire(o, 0);
		while(results.hasNext())
			transfer(results.next());
		
		// query for qualifying elements
		Iterator<? extends TemporalObject<E>> it = sweepArea.query(o, 0);
		if (it.hasNext()) { // match found -> merge intervals
			TemporalObject<E> oldObject = it.next();
			TemporalObject<E> newObject = new TemporalObject<E>(
				o.getObject(),
				new TimeInterval(oldObject.getStart(), o.getEnd())
			);
			sweepArea.update(oldObject, newObject);
		}
		else // no match found
			sweepArea.insert(o);
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
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#processHeartbeat(long, int)
	 */
	@Override
	protected long processHeartbeat(long minTS, int sourceID) {
		if (transferIterator(sweepArea.expire(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), sourceID)))
			return NO_HEARTBEAT;
		return sweepArea.size() >0 ?  Math.min(sweepArea.getMinTimeStamp(), minTS) : minTS;
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
		final long epsilon = 5000;
		Coalesce<Integer> c = new Coalesce<Integer>(s, 0, new CoalesceSA<Integer>(new ListSAImplementor<TemporalObject<Integer>>(), epsilon));
		// check sort order
		Pipes.verifyStartTimeStampOrdering(c);

		AbstractSink sink = new AbstractSink<TemporalObject<Integer>>(c) {
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
		Printer printer = new Printer<TemporalObject<Integer>>(c);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(printer);
		exec.startAllQueries();
	}

}
