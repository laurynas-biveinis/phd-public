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

package xxl.core.pipes.operators.differences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ListSA;
import xxl.core.collections.sweepAreas.SweepAreaImplementor;
import xxl.core.cursors.sorters.MergeSorter;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.And;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.RightBind;

/**
 * Operator component in a query graph that performs a difference (minus)
 * operation over two input streams based on sweepline statusstructures.
 * The elements of the second stream are removed from the first one. <BR>
 * @param <E> 
 * 
 * @see xxl.core.collections.sweepAreas.SweepArea
 * @since 1.1
 */
public class TemporalDifference<E> extends AbstractTemporalPipe<E, E> implements MemoryMonitorable {

    public static class TemporalDifferenceSA<E> extends ListSA<TemporalObject<E>> {
        
        protected int memSize;
        protected int finalMemSize;
        
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, Predicate<? super TemporalObject<E>>[] queryPredicates, Predicate<? super TemporalObject<E>>[] removePredicates, List<TemporalObject<E>> list, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		super(impl, ID, false, queryPredicates, removePredicates, list, objectSize);
    		if (queryPredicates.length != 2)
    			throw new IllegalArgumentException("Exactly two inputs allowed. Wrong dimensionality.");
    		for (int i = 0; i < queryPredicates.length; i++) {
    			this.queryPredicates[i] = new And<TemporalObject<E>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    			this.removeRightBinds[i] = new RightBind<TemporalObject<E>>(this.removePredicates[i], null);
    		}
    		this.impl.setQueryPredicates(ID, this.queryPredicates);    		
    		this.memSize = memSize;
    		this.finalMemSize = finalMemSize;
    	}
    	
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, Predicate<? super TemporalObject<E>>[] queryPredicates, Predicate<? super TemporalObject<E>> removePredicate, List<TemporalObject<E>> list, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		super(impl, ID, false, queryPredicates, removePredicate, list, objectSize);
    		if (queryPredicates.length != 2)
    			throw new IllegalArgumentException("Exactly two inputs allowed. Wrong dimensionality.");
    		for (int i = 0; i < queryPredicates.length; i++) 
    			this.queryPredicates[i] = new And<TemporalObject<E>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    		this.impl.setQueryPredicates(ID, this.queryPredicates);    		
    		this.memSize = memSize;
    		this.finalMemSize = finalMemSize;
    	}
    	
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, Predicate<? super TemporalObject<E>> queryPredicate, Predicate<? super TemporalObject<E>> removePredicate, List<TemporalObject<E>> list, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		super(impl, ID, false, ReorganizeModes.LAZY_REORGANIZE, queryPredicate, removePredicate, 2, list, objectSize);
    		for (int i = 0; i < queryPredicates.length; i++) {
    			this.queryPredicates[i] = new And<TemporalObject<E>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    			this.removeRightBinds[i] = new RightBind<TemporalObject<E>>(this.removePredicates[i], null);
    		}
    		this.impl.setQueryPredicates(ID, this.queryPredicates);    		
    		this.memSize = memSize;
    		this.finalMemSize = finalMemSize;
    	}
    	
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, Predicate<? super TemporalObject<E>> queryPredicate, List<TemporalObject<E>> list, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		super(impl, ID, false, ReorganizeModes.LAZY_REORGANIZE, queryPredicate, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, 2, list, objectSize);
    		for (int i = 0; i < queryPredicates.length; i++) 
    			this.queryPredicates[i] = new And<TemporalObject<E>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    		this.impl.setQueryPredicates(ID, this.queryPredicates);    		
    		this.memSize = memSize;
    		this.finalMemSize = finalMemSize;
    	}
    	
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, List<TemporalObject<E>> list, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		this(impl, ID, TemporalObject.VALUE_EQUIVALENCE_PREDICATE, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, list, objectSize, memSize, finalMemSize);
    	}
    	
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, Predicate<? super TemporalObject<E>>[] queryPredicates, Predicate<? super TemporalObject<E>>[] removePredicates, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		this(impl, ID, queryPredicates, removePredicates, new ArrayList<TemporalObject<E>>(), objectSize, memSize, finalMemSize);
    	}
    	
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, Predicate<? super TemporalObject<E>> queryPredicate, Predicate<? super TemporalObject<E>> removePredicate, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		this(impl, ID, queryPredicate, removePredicate, new ArrayList<TemporalObject<E>>(), objectSize, memSize, finalMemSize);
    	}
    	
    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, Predicate<? super TemporalObject<E>> queryPredicate, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		this(impl, ID, queryPredicate, new ArrayList<TemporalObject<E>>(), objectSize, memSize, finalMemSize);
    	}

    	public TemporalDifferenceSA(SweepAreaImplementor<TemporalObject<E>> impl, int ID, final int objectSize,
    			final int memSize, final int finalMemSize) {
    		this(impl, ID, TemporalObject.VALUE_EQUIVALENCE_PREDICATE, objectSize, memSize, finalMemSize);
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.ListSA#insert(java.lang.Object)
    	 */
    	@Override
		public void insert(TemporalObject<E> o) throws IllegalArgumentException {
    		if (checkObjectSize)
    			computeObjectSize(o);
    		int index = Collections.binarySearch(list, o, TemporalObject.START_TIMESTAMP_COMPARATOR);
    		if (index < 0)
    			list.add(-index-1, o);
    		else
    			list.add(index, o);
    		impl.insert(o);
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.ImplementorBasedSweepArea#query(java.lang.Object, int)
    	 */
    	@Override
		public Iterator<TemporalObject<E>> query(TemporalObject<E> o, int ID) throws IllegalArgumentException {
    		// sorting according to stream order
    		return new MergeSorter<TemporalObject<E>>(impl.query(o, ID), TemporalObject.START_TIMESTAMP_COMPARATOR, objectSize, memSize, finalMemSize);
    	}

    	public void remove(TemporalObject<E> object) {
    	    impl.remove(object); // remove from implementor
    	    // remove from list
			int index = Collections.binarySearch(list, object, TemporalObject.START_TIMESTAMP_COMPARATOR);
			TemporalObject cur = list.get(index);
			boolean removed = false;
			if (cur.equals(object)) {
				list.remove(index);
				removed = true;
			}
			if (!removed) {
				TemporalObject<E> suc;
				for (int j = index+1, size = list.size(); j < size; j++) {
					suc = list.get(j);
					if (object.getStart() < suc.getStart())
						break;
					if (suc.equals(object)) {
						list.remove(j);
						removed = true;
						break;
					}
				}
			}
			if (!removed) {
				TemporalObject<E> pre;
				for (int j = index-1; j >= 0; j--) {
					pre = list.get(j);
					if (object.getStart() > pre.getStart()) 
						break;
					if (pre.equals(object)) {
						list.remove(j);
						removed = true;
						break;
					}
				}
			}
    	}
    	
    	public long getMinTimeStamp() {
    		return list.get(0).getStart();
    	}    	
    }
    
    
	@SuppressWarnings("unchecked")
	protected TemporalDifferenceSA<E>[] sweepAreas = new TemporalDifferenceSA[2];   	
   	
   	public TemporalDifference(TemporalDifferenceSA<E> sweepArea0, TemporalDifferenceSA<E> sweepArea1) {
		this.sweepAreas[0] = sweepArea0;
        this.sweepAreas[1] = sweepArea1;        
	}
   	
	/** 
	 * Creates a new Difference as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately. 
	 * The elements of <CODE>source1</CODE> are removed from the stream
	 * produced by <CODE>source0</CODE>.
	 *
	 * @param source0 This pipe gets subscribed to the specified source.
	 * @param source1 This pipe gets subscribed to the specified source.
	 * @param ID_0 This pipe uses the given ID for subscription to the first source.
	 * @param ID_1 This pipe uses the given ID for subscription to the second source.
	 * @param sweepArea0 DifferenceSweepArea managing the elements delivered by the first source.
	 * @param sweepArea1 SweepArea managing the elements delivered by the second source.
	 */ 
	public TemporalDifference(Source<? extends TemporalObject<E>> source0, Source<? extends TemporalObject<E>> source1, int sourceID_0, int sourceID_1, TemporalDifferenceSA<E> sweepArea0, TemporalDifferenceSA<E> sweepArea1) {
		this(sweepArea0, sweepArea1);
		if (!(Pipes.connect(source0, this, sourceID_0) && Pipes.connect(source1, this, sourceID_1)))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
	}
	
	public TemporalDifference(Source<? extends TemporalObject<E>> source0, Source<? extends TemporalObject<E>> source1, Function<? super TemporalObject<E>,Integer> hashFunction, int objectSize, int memSize, int finalMemSize) {
		this(source0, source1, 0, 1, new TemporalDifferenceSA<E>(new HashSAImplementor<TemporalObject<E>>(hashFunction, 2), 0, objectSize, memSize, finalMemSize), new TemporalDifferenceSA<E>(new HashSAImplementor<TemporalObject<E>>(hashFunction, 2), 1, objectSize, memSize, finalMemSize));    
	}
		
	/** 
	 * If the given element comes from the first source, the second sweep area
	 * is searched for corresponding elements. If no matching element is found,
	 * the given element is inserted into the first sweep area, which is 
	 * reorganized afterwards. <BR>
	 * If the element is from the second sweep area, it is inserted directly
	 * into this one. Thereafter both sweep areas are reorganized. Note, that
	 * the DifferenceSweepArea's reorganization method removes all corresponding
	 * elements internally.
	 *
	 * @param object The element streaming in.
	 * @param ID One of the IDs this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(TemporalObject<E> object, int sourceID) throws IllegalArgumentException {		
		
		// optimization
		boolean continueWithExpiration = false;
		if (sourceID == sourceIDs[0] && receivedDone(sourceIDs[1]) && sweepAreas[1].size() == 0) { // right input is closed
			sweepAreas[0].insert(object);
			continueWithExpiration = true;
		}
		
		int j = sourceID == sourceIDs[0] ? 0 : 1;
		int k = 1 - j;			
			
		// normal processing
		if (!continueWithExpiration) {
			// determine qualifying elements			
			Iterator<? extends TemporalObject<E>> qualifies = sweepAreas[k].query(object, j);
			if (!qualifies.hasNext()) {
				sweepAreas[j].insert(object);
			}
			else {
				List<TemporalObject<E>> insertIntoSA_j = new LinkedList<TemporalObject<E>>();
				insertIntoSA_j.add(object);
								
				while (insertIntoSA_j.size() > 0 && qualifies.hasNext()) {
					TemporalObject<E> o1 = qualifies.next();
					TimeInterval t1 = o1.getTimeInterval();	
					
					Iterator<TemporalObject<E>> it = insertIntoSA_j.listIterator();
					
					// for each element o1, the overlap condition with an element in the list addSA_j is checked;
					// the overlap is eliminated through generating the inverse intersection;
					// this procedure is performed as long as elements from the list addSA_j overlap with
					// elements in qualifies;
					if (it.hasNext()) {
						TemporalObject<E> o2 = it.next();
						TimeInterval t2 = o2.getTimeInterval();
						if (t2.overlaps(t1)) { // time intervals overlap
							it.remove(); // remove from insertIntoSA_j
							sweepAreas[k].remove(o1); // remove o1 from SA_k
														
							// determines the elements that may qualify for insertion in SweepArea j
							Iterator<TemporalObject<E>> addSA_j = TemporalObject.timeStampedObjects(o1.getObject(), t2.intersectInverse(t1));
							while (addSA_j.hasNext()) {
							    TemporalObject<E> next = addSA_j.next();
							    if (next.getEnd() <= t1.getStart())
							        sweepAreas[j].insert(next);
							    else 
							        insertIntoSA_j.add(next);
							}
						
							// determines the elements that may qualify for insertion in SweepArea k					
							Iterator<TemporalObject<E>> insertIntoSA_k_it = TemporalObject.timeStampedObjects(o2.getObject(), t1.intersectInverse(t2));
							while(insertIntoSA_k_it.hasNext()) 
								sweepAreas[k].insert(insertIntoSA_k_it.next());
						}
					}
				}
				Iterator<TemporalObject<E>> insertIntoSA_j_it = insertIntoSA_j.iterator();
				while(insertIntoSA_j_it.hasNext()) 
					sweepAreas[j].insert(insertIntoSA_j_it.next());
			}
		}
		
		// expiration
		TemporalObject<E> reorganizeObject = new TemporalObject<E>(object.getObject(),
			new TimeInterval(minTimeStamp, object.getEnd())
		);
		if (k == 0) { // left SweepArea
			transferIterator(sweepAreas[0].expire(reorganizeObject, 1));
			// optimization
			if (receivedDone(sourceIDs[0]) && sweepAreas[0].size() == 0) { // left input is done
				done(sourceIDs[1]);
			}
		}
		else { // right SweepArea
			sweepAreas[k].reorganize(reorganizeObject, 0);
		}
    }

	protected boolean transferIterator(Iterator<? extends TemporalObject<E>> it) {
		boolean ret = it.hasNext();
		while(it.hasNext())
			transfer(it.next());
		return ret;
	}
	
	/**
	 * If both sources called <CODE>done</CODE>,
	 * the elements of the first sweep area area are
	 * transferred to this pipe's subscribed sinks.
	 * After that, the done-call is forwarded to all subscribed sinks.
	 * 
	 * @param sourceID 
	 */
	@Override
	public void done(int sourceID) {		
		if (isClosed || isDone) return;
		processingWLock.lock();
		updateDoneStatus(sourceID);
	    try {
			if (isDone()) {
				transferIterator(sweepAreas[0].iterator());
				signalDone();
			}
			else { // optimization
				int j = Pipes.getSourceIndex(sourceIDs, sourceID);
				if (sweepAreas[j].size() == 0) 
					done(sourceIDs[1-j]);
			}
		}
	    finally {
	    	processingWLock.unlock();
	    }
	}
	
	/**
	 * Calls <CODE>super.close()</CODE> and closes
	 * both sweep areas.
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed()) {
			sweepAreas[0].close();
            sweepAreas[1].close();
        }
    }
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		return sweepAreas[0].getCurrentMemUsage() + sweepAreas[1].getCurrentMemUsage();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#processHeartbeat(long, int)
	 */
	@Override
	protected long processHeartbeat(long minTS, int sourceID) {	
		if (sourceID == Heartbeat.MEMORY_MANAGER) {
			sweepAreas[1].reorganize(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), 0);
			if (transferIterator(sweepAreas[0].expire(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), 1)))
				return NO_HEARTBEAT;
		}
		if (sourceID == sourceIDs[1] && transferIterator(sweepAreas[0].expire(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), sourceID)))
			return NO_HEARTBEAT;
		if (sourceID == sourceIDs[0])
			sweepAreas[1].reorganize(new TemporalObject<E>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), sourceID);			
		if (sweepAreas[0].size() >0 && sweepAreas[1].size() >0)
			return Math.min(Math.min(sweepAreas[0].getMinTimeStamp(), sweepAreas[1].getMinTimeStamp()), minTS);
		if (sweepAreas[0].size() >0 || sweepAreas[1].size() >0)
			return sweepAreas[0].size() >0  ? Math.min(sweepAreas[0].getMinTimeStamp(),minTS) : Math.min(sweepAreas[1].getMinTimeStamp(),minTS);
		return minTS;
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
			
		final int noOfElements1 = 1000;
		final int noOfElements2 = 500;
		final int buckets = 10;
		final int startInc = 50;
		final int intervalSize = 100;
		final long seed = 42;
		final HashMap<Integer,List<Long>> in1 = new HashMap<Integer,List<Long>>();
		final HashMap<Integer,List<Long>> in2 = new HashMap<Integer,List<Long>>();
		final Integer[] hashCodes = new Integer[buckets];
		for (int i = 0; i < buckets; i++)
			hashCodes[i] = new Integer(i);
		final Function<TemporalObject<Integer>,Integer> hash = new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> o) {
				return o.getObject()%buckets;
			}
		};
			
		RandomNumber<Integer> r1 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements1, 0
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, 0
		);	
		
		Source<TemporalObject<Integer>> s1 = Pipes.decorateWithRandomTimeIntervals(r1, in1, hashCodes, startInc, intervalSize, seed);
		Source<TemporalObject<Integer>> s2 = Pipes.decorateWithRandomTimeIntervals(r2, in2, hashCodes, startInc, intervalSize, 2*seed);
				
		TemporalDifference<Integer> d = new TemporalDifference<Integer>(s1, s2, 0, 1, 
			new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 0, 36, 4096*4096, 1024*1024),
			new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 1, 36, 4096*4096, 1024*1024)
		);
		
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
					// subtracting HashMap in2 from in1
					List<Long> l1, l2;
					for (int i = 0; i < buckets; i++) {
						l1 = in1.get(hashCodes[i]);
						l2 = in2.get(hashCodes[i]);
						for (int j = 0; l1 != null && l2 != null && j < l2.size(); j++) {
							l1.remove(l2.get(j));
						}
					}
					
					Iterator<TemporalObject<Integer>> listIt = list.iterator();
					HashMap<Integer,List<Long>> result = new HashMap<Integer,List<Long>>();
					while (listIt.hasNext()) {
						TemporalObject<Integer> t = listIt.next();
						int key = t.getObject();
						List<Long> l = result.get(hashCodes[key]);
						if (l == null) l = new LinkedList<Long>();
						l.addAll(TemporalObject.getAllSnapshots(t));					
						result.put(hashCodes[key], l);
					}
					// check for multiplicity 
					for (int i = 0; i < buckets; i++) {
						List<Long> in = in1.get(hashCodes[i]);
						List<Long> out = result.get(hashCodes[i]);
						if (in != null && out != null) {
							Iterator<Long> it = in.iterator();					
							while (it.hasNext()) {
								Object next = it.next();
								if (!out.contains(next)) 
									System.err.println("ERROR: element lost: value "+i+" at time "+next);						
							}
							System.out.println("CHECK 1 finished for bucket "+i+".");
							it = out.iterator();
							while (it.hasNext()) {
								Object next = it.next();
								if (!in.contains(next)) 
									System.err.println("ERROR: more elements than expected: value "+i+" at time "+next);
								it.remove();	
							}
							if (out.size() > 0)
								System.err.println("ERROR: more elements than expected");
							System.out.println("CHECK 2 finished for bucket "+i+".");
						}
						else {
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