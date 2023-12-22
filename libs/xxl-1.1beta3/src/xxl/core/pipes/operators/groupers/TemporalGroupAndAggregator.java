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

package xxl.core.pipes.operators.groupers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import xxl.core.collections.MapEntry;
import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.queues.Heap;
import xxl.core.collections.sweepAreas.ListSAImplementor;
import xxl.core.cursors.AbstractCursor;
import xxl.core.functions.Function;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.Sum;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.identities.Coalesce;
import xxl.core.pipes.operators.identities.Coalesce.CoalesceSA;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.operators.mappers.TemporalAggregator;
import xxl.core.pipes.operators.mappers.TemporalAggregator.TemporalAggregatorSA;
import xxl.core.pipes.operators.unions.TemporalUnion;
import xxl.core.pipes.operators.unions.TemporalUnion.TemporalUnionSA;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Tester;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;

/**
 *
 */
public class TemporalGroupAndAggregator<E,K,V> extends AbstractTemporalPipe<E,Entry<K,V>> implements MemoryMonitorable {
	
	public static class TemporalGroupAndAggregatorSA<E,V> extends TemporalAggregatorSA<E,V> {
    	    
		protected boolean splitOptimization = true;
		    	
    	public TemporalGroupAndAggregatorSA(Predicate<? super TemporalObject<V>> removePredicate, List<TemporalObject> list, AggregationFunction<? super E,V> aggFunction, int objectSize, boolean splitOptimization) {
    		super(removePredicate, list, aggFunction, objectSize);
    		this.splitOptimization = splitOptimization;
    	}
    	
    	public TemporalGroupAndAggregatorSA(Predicate<? super TemporalObject<V>> removePredicate, List<TemporalObject> list, AggregationFunction<? super E,V> aggFunction) {
    		super(removePredicate, list, aggFunction);
    	}
    	
    	public TemporalGroupAndAggregatorSA(List<TemporalObject> list, AggregationFunction<? super E,V> aggFunction) {
    		super(list, aggFunction);
    	}
    	
    	public TemporalGroupAndAggregatorSA(Predicate<? super TemporalObject<V>> removePredicate, AggregationFunction<? super E,V> aggFunction) {
    		super(removePredicate, aggFunction);
    	}
    	
    	public TemporalGroupAndAggregatorSA(AggregationFunction<? super E,V> aggFunction) {
    		super(aggFunction);
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.pipes.operators.mappers.TemporalAggregator.TemporalAggregatorSA#expire(xxl.core.pipes.elements.TemporalObject, int)
    	 */
    	@Override
		public Iterator<TemporalObject> expire(final TemporalObject current, final int ID) {
    		return new AbstractCursor<TemporalObject>() {
    			protected boolean trueByRemovePredicate = false;
    			@Override
				public boolean hasNextObject() {
    				if (list.size() == 0)
    					return false;
    				if (removePredicate.invoke((TemporalObject<V>)list.get(0), (TemporalObject<V>)current))
    					return trueByRemovePredicate = true;
 
    				if (splitOptimization) {
	    				// split optimization
	    				TemporalObject<V> first = list.get(0);
	    				if (first.getStart() < current.getStart())  // overlaps
	    					return true;
    				}
    				return false;
    			}
    		
    			@Override
				public TemporalObject nextObject() {
    				if (trueByRemovePredicate) {
    					trueByRemovePredicate = false;
    					return list.remove(0);
    				}
    				// split optimization
    				TemporalObject<V> first = list.get(0);
    				TemporalObject<V> newTSO = new TemporalObject<V>(
						first.getObject(),
						new TimeInterval(first.getStart(), current.getStart())
    				);
    				// update
    				list.set(0, new TemporalObject<V>(first.getObject(),
    					new TimeInterval(current.getStart(), first.getEnd())	
    				));
    				return newTSO;
    			}
    		};
    	}
	}
	
	protected Hashtable<K,TemporalGroupAndAggregatorSA<E,V>> groups = new Hashtable<K,TemporalGroupAndAggregatorSA<E,V>>();
	protected Function<? super TemporalObject<E>, ? extends K> determineGroup;
	protected Function<?,? extends TemporalGroupAndAggregatorSA<E,V>> newTemporalGroupAndAggregatorSA;
	protected Heap<TemporalObject<Entry<K,V>>> heap;
	protected boolean splitOptimization = true;
	protected ArrayList<Long> startTimeStamps;
	protected long min;
	
	//	 determineGroup: TemporalObject --> Object
	public TemporalGroupAndAggregator(Function<? super TemporalObject<E>,? extends K> determineGroup, Function<?,? extends TemporalGroupAndAggregatorSA<E,V>> newTemporalGroupAndAggregatorSA, boolean splitOptimization) {
		this.determineGroup = determineGroup;
		this.newTemporalGroupAndAggregatorSA = newTemporalGroupAndAggregatorSA;
		this.heap = new DynamicHeap<TemporalObject<Entry<K,V>>>(TemporalObject.START_TIMESTAMP_COMPARATOR);
		this.splitOptimization = splitOptimization;
	}	
	
	// determineGroup: TemporalObject --> Object
	public TemporalGroupAndAggregator(Source<? extends TemporalObject<E>> source, int sourceID, Function<? super TemporalObject<E>,? extends K> determineGroup, Function<?,? extends TemporalGroupAndAggregatorSA<E,V>> newTemporalGroupAndAggregatorSA, boolean splitOptimization) {
		this(determineGroup, newTemporalGroupAndAggregatorSA, splitOptimization);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
	
	public TemporalGroupAndAggregator(Source<? extends TemporalObject<E>> source, Function<? super TemporalObject<E>,? extends K> determineGroup, final AggregationFunction<? super E,V> aggFunction) {
		this(source, DEFAULT_ID, determineGroup, new Function<Object,TemporalGroupAndAggregatorSA<E,V>>() {
			@Override
			public TemporalGroupAndAggregatorSA<E,V> invoke() {
				return new TemporalGroupAndAggregatorSA<E,V>(aggFunction);
			}
		}, true);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(TemporalObject<E> o, int sourceID) throws IllegalArgumentException {
		K groupID = determineGroup.invoke(o);
		if (!groups.containsKey(groupID))
			groups.put(groupID, newTemporalGroupAndAggregatorSA.invoke());
		groups.get(groupID).insert(o);
		
		if (!splitOptimization) 
			startTimeStamps = new ArrayList<Long>(groups.size());
	
		Enumeration<K> groupEnum = groups.keys();
		while(groupEnum.hasMoreElements()) {
			K nextGroupID = groupEnum.nextElement();
			TemporalGroupAndAggregatorSA<E,V> next = groups.get(nextGroupID);
			Iterator<TemporalObject> aggs = next.expire(o, 0);
			while(aggs.hasNext()) {
				TemporalObject<V> agg = (TemporalObject<V>)aggs.next();
				TemporalObject<Entry<K,V>> newTSO = new TemporalObject<Entry<K,V>>(
						new MapEntry<K,V>(nextGroupID, agg.getObject()), 
						agg.getTimeInterval()
				);
				heap.enqueue(newTSO);
			}
			if (!splitOptimization) {
				Iterator<TemporalObject> groupIt = next.iterator();
				if (groupIt.hasNext()) 
					startTimeStamps.add(groupIt.next().getEnd());
				else
					startTimeStamps.add(Long.MIN_VALUE);
			}
		}
				
		if (splitOptimization)
			min = o.getStart();
		else {
			min = Long.MAX_VALUE;
			for (int i = 0, n = startTimeStamps.size(); i < n; i++) 
				min = Math.min(min, startTimeStamps.get(i));
		}
		
		transferHeap(min);	
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#close()
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed()) {
			Enumeration<TemporalGroupAndAggregatorSA<E,V>> groupEnum = groups.elements();
			while(groupEnum.hasMoreElements())
				groupEnum.nextElement().close();
			heap.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#done(int)
	 */
	@Override
	public void done(int sourceID) {
		if (isClosed) return;
		processingWLock.lock();
	    try {
			Enumeration<K> groupEnum = groups.keys();
			while(groupEnum.hasMoreElements()) {
				K nextGroupID = groupEnum.nextElement();
				TemporalGroupAndAggregatorSA<E,V> next = groups.get(nextGroupID);
				Iterator<TemporalObject> aggs = next.iterator();
				while(aggs.hasNext()) {
					TemporalObject<V> agg = (TemporalObject<V>)aggs.next();
					TemporalObject<Entry<K,V>> newTSO = new TemporalObject<Entry<K,V>>(
							new MapEntry<K,V>(nextGroupID, agg.getObject()), 
							agg.getTimeInterval()
					);
					heap.enqueue(newTSO);
				}
			}
			while(!isClosed() && !heap.isEmpty())
				super.transfer(heap.dequeue());
			super.done(sourceID);
	    }
	    finally {
	    	processingWLock.unlock();
	    }
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.MemoryMonitorable#getCurrentMemUsage()
	 */
	public int getCurrentMemUsage() {
		Enumeration<TemporalGroupAndAggregatorSA<E,V>> groupEnum = groups.elements();
		int size = 0;
		int objectSize = SIZE_UNKNOWN;
		while(groupEnum.hasMoreElements()) {
			TemporalGroupAndAggregatorSA<E,V> next = groupEnum.nextElement();
			int mem = next.getCurrentMemUsage();
			if (mem == SIZE_UNKNOWN)
				return SIZE_UNKNOWN;
			if (objectSize == SIZE_UNKNOWN)
				objectSize = mem / next.size();
			size += mem;
		}
		size += heap.size()*objectSize;
		return size;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#processHeartbeat(long, int)
	 */
	@Override
	protected long processHeartbeat(long minTS, int sourceID) {		
		if (transferHeap(minTS))
			return NO_HEARTBEAT;
		if (heap.size()>0)
			Math.min(heap.peek().getStart(), minTS);
		if (groups.size() >0)
			return Math.min(min, minTS);
		return minTS;
	}
	
	/**
	 * @param min
	 * @return
	 */
	protected boolean transferHeap(long min) {
		boolean ret = false;
		while (!heap.isEmpty() && heap.peek().getStart() <= min) {
			transfer(heap.dequeue());
			ret = true; 
		}
		return ret;
	}
	
	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		final int noOfElements = 1000;
		final int buckets = 10;
		final int startInc = 50;
		final int intervalSize = 75;
		final long seed = 42;
		
		RandomNumber<Integer> r = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements, 0
		);		
		
		Source<TemporalObject<Integer>> s = new Mapper<Integer,TemporalObject<Integer>>(r,
			new Function<Integer,TemporalObject<Integer>>() {
				Random random = new Random(seed);
				long start, end;
				long newStart, newEnd;
				@Override
				public TemporalObject<Integer> invoke(Integer o) {
					newStart = start+random.nextInt(startInc);
					newEnd   = newStart+1+random.nextInt(intervalSize-1);
					if (start == newStart) newEnd = end+random.nextInt(intervalSize-1);
					TemporalObject<Integer> object = new TemporalObject<Integer>(o%buckets, 
						new TimeInterval(start = newStart, end = newEnd)
					);
					return object;
				}
			}
		);
	
		final Sum agg = new Sum();
		final Function<TemporalObject<Integer>,Integer> hashFunction = new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> o) {
				return o.getObject()%1117;
			}
		};
		
		// direct implementation
		TemporalGroupAndAggregator<Integer,Integer,Number> a = new TemporalGroupAndAggregator<Integer,Integer,Number>(s,
			hashFunction, agg
		);
	
		// indirect implementation
		int noOfGroups = 1117;
		HashGrouper<TemporalObject<Integer>> grouper = new HashGrouper<TemporalObject<Integer>>(s, 0, hashFunction, noOfGroups);
		
		TemporalAggregator<Integer,Number>[] aggs = new TemporalAggregator[noOfGroups];
		Mapper<TemporalObject<Number>,TemporalObject<Entry<Integer,Number>>>[] mappers = new Mapper[noOfGroups];
		
		Function<?,Function<TemporalObject<Number>,TemporalObject<Entry<Integer,Number>>>> assignGroupFunction = new Function<Object,Function<TemporalObject<Number>,TemporalObject<Entry<Integer,Number>>>>() {
			protected int groupID = 0;
			
			@Override
			public Function<TemporalObject<Number>,TemporalObject<Entry<Integer,Number>>> invoke() {
				
				Function<TemporalObject<Number>,TemporalObject<Entry<Integer,Number>>> assignGroup = new Function<TemporalObject<Number>,TemporalObject<Entry<Integer,Number>>>() {
					
					protected final int id = groupID;
					
					@Override
					public TemporalObject<Entry<Integer,Number>> invoke(TemporalObject<Number> tso) {
						return new TemporalObject<Entry<Integer,Number>>(
								new MapEntry<Integer,Number>(new Integer(id), tso.getObject()), 
								tso.getTimeInterval()
						);
					}
				};
				groupID++;
				return assignGroup;
			}
		};		
		for (int i = 0; i < noOfGroups; i++) {
			aggs[i] = new TemporalAggregator<Integer,Number>(grouper.getReferenceToGroup(i), new TemporalAggregatorSA<Integer,Number>(agg));
			mappers[i] = new Mapper<TemporalObject<Number>,TemporalObject<Entry<Integer,Number>>>(aggs[i], assignGroupFunction.invoke());
		}
		TemporalUnion<Entry<Integer,Number>> u = new TemporalUnion<Entry<Integer,Number>>(mappers, new TemporalUnionSA<Entry<Integer,Number>>(noOfGroups));
	
		// check sorting
		Pipes.verifyStartTimeStampOrdering(a);
		Pipes.verifyStartTimeStampOrdering(u);
		
		// compare results of both implementations
		Coalesce<Entry<Integer,Number>> c1 = new Coalesce<Entry<Integer,Number>>(a, 0, new CoalesceSA<Entry<Integer,Number>>(new ListSAImplementor<TemporalObject<Entry<Integer,Number>>>(), intervalSize*10));
		Coalesce<Entry<Integer,Number>> c2 = new Coalesce<Entry<Integer,Number>>(u, 0, new CoalesceSA<Entry<Integer,Number>>(new ListSAImplementor<TemporalObject<Entry<Integer,Number>>>(), intervalSize*10));
		
		AbstractSink sink = new AbstractSink<TemporalObject<MapEntry<Integer,Double>>>(new Source[]{c1,c2}, new int[]{1,2}) {
			LinkedList<TemporalObject<MapEntry<Integer,Double>>> list1 = new LinkedList<TemporalObject<MapEntry<Integer,Double>>>();
			LinkedList<TemporalObject<MapEntry<Integer,Double>>> list2 = new LinkedList<TemporalObject<MapEntry<Integer,Double>>>();
			
			@Override
			public void processObject(TemporalObject<MapEntry<Integer,Double>> o, int sourceID) {				
				if (sourceID == 1) {
					list1.add(o);
					return;
				}
				if (sourceID == 2) {
					list2.add(o);
					return;
				}
			}
			
			@Override
			public void done(int sourceID) {
				super.done(sourceID);
				if (isDone()) {
					processingWLock.lock();
					try {
						System.out.println("list1: "+list1);
						System.out.println("list2: "+list2);
						Iterator it1 = list1.iterator();
						while (it1.hasNext()) {
							Object next1 = it1.next();
							if (!list2.contains(next1)) 
								System.err.println("ERROR: Missing results in indirect implementation: "+next1);
							else 
								list2.remove(next1);
						}
						Iterator it2 = list2.iterator();
						while (it2.hasNext()) 
							System.err.println("ERROR: Missing results in direct implementation: "+it2.next());
						System.out.println("CHECKS FINISHED.");
					}
					finally {
						processingWLock.unlock();
					}
				}
			}
		};

		Tester t1 = new Tester<TemporalObject<Entry<Integer,Number>>>(c1);
		Tester t2 = new Tester<TemporalObject<Entry<Integer,Number>>>(c2);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(t1);
		exec.registerQuery(t2);
		exec.startAllQueries();		
	}

}
