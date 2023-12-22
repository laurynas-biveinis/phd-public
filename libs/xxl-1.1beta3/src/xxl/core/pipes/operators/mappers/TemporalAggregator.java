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
package xxl.core.pipes.operators.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import xxl.core.collections.sweepAreas.AbstractSweepArea;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.Function;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.Sum;
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
 * The temporal aggregation operator.
 * 
 * @since 1.1
 */
public class TemporalAggregator<I,O> extends AbstractTemporalPipe<I, O> implements MemoryMonitorable {

    public static class TemporalAggregatorSA<I,O> extends AbstractSweepArea<TemporalObject> {
    	
        protected Predicate<? super TemporalObject<O>> removePredicate;
    	protected AggregationFunction<? super I,O> aggFunction;
    	protected List<TemporalObject> list;
    	     	    	    	
    	public TemporalAggregatorSA(Predicate<? super TemporalObject<O>> removePredicate, List<TemporalObject> list, AggregationFunction<? super I,O> aggFunction, int objectSize) {
    		super(objectSize);
    		this.removePredicate = removePredicate;
    		this.list = list;
    		this.aggFunction = aggFunction;
    	}
    	
    	public TemporalAggregatorSA(Predicate<? super TemporalObject<O>> removePredicate, List<TemporalObject> list, AggregationFunction<? super I,O> aggFunction) {
    		this(removePredicate, list, aggFunction, MemoryMonitorable.SIZE_UNKNOWN);
    	}
    	
    	public TemporalAggregatorSA(List<TemporalObject> list, AggregationFunction<? super I,O> aggFunction) {
    	    this(TemporalObject.INTERVAL_OVERLAP_REORGANIZE, list, aggFunction);
    	}
    	
    	public TemporalAggregatorSA(Predicate<? super TemporalObject<O>> removePredicate, AggregationFunction<? super I,O> aggFunction) {
    	    this(removePredicate, new ArrayList<TemporalObject>(), aggFunction);
    	}
    	
    	public TemporalAggregatorSA(AggregationFunction<? super I,O> aggFunction) {
    	    this(TemporalObject.INTERVAL_OVERLAP_REORGANIZE, aggFunction);
    	}
    	
    	@Override
		public void insert(TemporalObject object) throws IllegalArgumentException {
    		TemporalObject<I> o = (TemporalObject<I>)object;	
    		TimeInterval t = o.getTimeInterval();
    		long lastEnd = o.getStart();
    		boolean filledUp = false;
    		TemporalObject<O> o1;
    		TimeInterval t1;
    		
    		int index = Collections.binarySearch(list, o, TemporalObject.START_TIMESTAMP_COMPARATOR);	
    		int i = index < 0 ? -index-1 : index;
    		int j;
    		// predecessor may overlap with o
    		if (i-1 >= 0 && i-1 < list.size() && t.overlaps(list.get(i-1).getTimeInterval())) {
    			j = i-1;
    		}
    		else { // successor may overlap with o
    			j = i;
    			if (j == list.size() || (!t.overlaps(list.get(j).getTimeInterval()))) {
    				list.add(j, new TemporalObject<O>(
    					aggFunction.invoke(null, o.getObject()), 
    					new TimeInterval(lastEnd, o.getEnd())
    				));
    				return;
    			}
    		} 
    		// adjusting aggregates	
    		while(j < list.size() && t.overlaps(t1 = (o1 = list.get(j)).getTimeInterval())) {
    			if (o1.getStart() < o.getStart()) {
    				list.set(j, new TemporalObject<O>(
    					o1.getObject(), 
    					new TimeInterval(o1.getStart(), o.getStart())
    				));
    				if (o.getEnd() < o1.getEnd()) {
    					list.add(++j, new TemporalObject<O>(
    						aggFunction.invoke(o1.getObject(), o.getObject()),
    						t.intersect(t1)
    					));
    					list.add(++j, new TemporalObject<O>(
    						o1.getObject(),
    						new TimeInterval(o.getEnd(), o1.getEnd())
    					));
    				}
    				else {
    					list.add(++j, new TemporalObject<O>(
    						aggFunction.invoke(o1.getObject(), o.getObject()),
    						new TimeInterval(o.getStart(), o1.getEnd())
    					));
    				}
    			}
    			else {
    				if (lastEnd < o1.getStart()) {
    					list.set(j, new TemporalObject<O>(
    						aggFunction.invoke(null, o.getObject()), 
    						new TimeInterval(lastEnd, o1.getStart())
    					));
    					filledUp = true;
    				}
    				if (t1.equals(t.intersect(t1))) {
    					if (filledUp) {
    						list.add(++j, new TemporalObject<O>(
    							aggFunction.invoke(o1.getObject(), o.getObject()), 
    							t1
    						));
    					}
    					else {
    						list.set(j, new TemporalObject<O>(
    							aggFunction.invoke(o1.getObject(), o.getObject()), 
    							t1
    						));
    					}
    					filledUp = false;
    				}
    				else {// o1.getEnd() > o.getEnd()
    					if (filledUp) {
    						list.add(++j, new TemporalObject<O>(
    							aggFunction.invoke(o1.getObject(), o.getObject()), 
    							new TimeInterval(o1.getStart(), o.getEnd())
    						));
    					}
    					else {					
    						list.set(j, new TemporalObject<O>(
    							aggFunction.invoke(o1.getObject(), o.getObject()), 
    							new TimeInterval(o1.getStart(), o.getEnd())
    						));
    					}
    					list.add(++j, new TemporalObject<O>(
							o1.getObject(), 
							new TimeInterval(o.getEnd(), o1.getEnd())
						));	
    					filledUp = false;
    				}
    			}
    			lastEnd = o1.getEnd();
    			j++;
    			i = j;
    		}
    		if (lastEnd < o.getEnd()) {
    			list.add(i, new TemporalObject<O>(
    				aggFunction.invoke(null, o.getObject()), 
    				new TimeInterval(lastEnd, o.getEnd())
    			));
    		}
    		// determine aggregate size
    		if (checkObjectSize)
    			computeObjectSize(list.get(0)); 		
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#expire(java.lang.Object, int)
    	 */
    	@Override
		public Iterator<TemporalObject> expire(final TemporalObject currentStatus, final int ID) {
    		return new AbstractCursor<TemporalObject>() {
    			@Override
				public boolean hasNextObject() {
    				if (list.size() == 0)
    					return false;
    				return removePredicate.invoke((TemporalObject<O>)list.get(0), (TemporalObject<O>)currentStatus);
    			}
    		
    			@Override
				public TemporalObject nextObject() {
    				return list.remove(0);
    			}
    		};
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#clear()
    	 */
    	@Override
		public void clear() {
    		list.clear();
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#close()
    	 */
    	@Override
		public void close() {
    		list.clear();
    	}

    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#size()
    	 */
    	@Override
		public int size() {
    		return list.size();
    	}

    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#iterator()
    	 */
    	@Override
		public Iterator<TemporalObject> iterator() throws UnsupportedOperationException {
    		return list.listIterator();
    	}
    	
    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#query(java.lang.Object, int)
    	 */
    	@Override
		public Iterator<TemporalObject> query(TemporalObject o, int ID) throws IllegalArgumentException {
    		return new EmptyCursor<TemporalObject>();
    	}

    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#query(E[], int[], int)
    	 */
    	@Override
		public Iterator<TemporalObject> query(TemporalObject [] os, int [] IDs, int valid) throws IllegalArgumentException {
    		return new EmptyCursor<TemporalObject>();
    	}

    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#query(E[], int[])
    	 */
    	@Override
		public Iterator<TemporalObject> query(TemporalObject [] os, int [] IDs) throws IllegalArgumentException {
    		return new EmptyCursor<TemporalObject>();
    	}

    	/* (non-Javadoc)
    	 * @see xxl.core.collections.sweepAreas.AbstractSweepArea#reorganize(java.lang.Object, int)
    	 */
    	@Override
		public void reorganize(TemporalObject currentStatus, int ID) throws IllegalStateException {
    		throw new UnsupportedOperationException("Results are produced during reorganization; use method expire instead.");	
    	}
    	
    	/**
    	 * @return
    	 */
    	public long getMinTimeStamp() {
    		return list.get(0).getStart();
    	}

    }
    
	protected TemporalAggregatorSA<I,O> sweepArea;
	
	/**
	 * @param sweepArea
	 */
	public TemporalAggregator(TemporalAggregatorSA<I,O> sweepArea) {
		this.sweepArea = sweepArea;		
	}
	
	/** 
	 * Creates a new TemporalAggregator as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately. <BR> 
	 * The aggregate function is invoked on elements of <CODE>source</CODE>.
	 * 
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription to the source.
	 * @param sweepArea TemporalAggregatorSweepArea managing the elements delivered by the source.
	 */
   	public TemporalAggregator(Source<? extends TemporalObject<I>> source, int sourceID, TemporalAggregatorSA<I,O> sweepArea) {
		this(sweepArea);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
   	
	/** 
	 * Creates a new TemporalAggregator as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately using a default id. 
	 * The aggregate function is invoked on elements of <CODE>source</CODE>. <BR>
	 * The object size is set to unknown.
	 * 
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sweepArea TemporalDistinctSweepArea managing the elements delivered by the source.
	 */
	public TemporalAggregator(Source<? extends TemporalObject<I>> source, TemporalAggregatorSA<I,O> sweepArea) {
		this(source, DEFAULT_ID, sweepArea);
	}
	
	/**
	 * @param source
	 * @param aggFunction
	 */
	public TemporalAggregator(Source<? extends TemporalObject<I>> source, AggregationFunction<? super I,O> aggFunction) {
		this(source, DEFAULT_ID, new TemporalAggregatorSA<I,O>(aggFunction));
	}
		
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(TemporalObject<I> o, int sourceID) throws IllegalArgumentException {
		sweepArea.insert(o);
		transferIterator(sweepArea.expire(o, 0));
	}

	/**
	 * @param it
	 * @return
	 */
	protected boolean transferIterator(Iterator<? extends TemporalObject> it) {
		boolean ret = it.hasNext();
		while(it.hasNext())
			transfer(it.next());
		return ret;
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
		if (transferIterator(sweepArea.expire(new TemporalObject<I>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), sourceID)))
			return NO_HEARTBEAT;
		return sweepArea.size() >0 ?  sweepArea.getMinTimeStamp() : minTS;
	}
		
	public static void main(String[] args) {
		
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		final int noOfElements = 1000;
		final int buckets = 10;
		final int startInc = 20;
		final int intervalSize = 120;
		final long seed = 42;
		final HashMap<Long,List<Integer>> input = new HashMap<Long,List<Integer>>();
		final Long[] hashCodes = new Long[(noOfElements*startInc)+intervalSize];
		for (int i = 0; i < hashCodes.length; i++)
			hashCodes[i] = new Long(i);	
		
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
					Integer value;
					newStart = start+random.nextInt(startInc);
					newEnd   = newStart+1+random.nextInt(intervalSize-1);
					if (start == newStart) newEnd = end+random.nextInt(intervalSize-1);
					TemporalObject<Integer> object = new TemporalObject<Integer>(value = o % buckets, 
						new TimeInterval(start = newStart, end = newEnd)
					);
					List<Long> tmp = TemporalObject.getAllSnapshots(object);
					int key;
					List<Integer> l;
					for (int i = 0; i < tmp.size(); i++) {
						key = (int)tmp.get(i).longValue();
						l = input.get(hashCodes[key]);
						if (l == null) l = new LinkedList<Integer>();
						l.add(value);
						input.put(hashCodes[key], l);
					}
					return object;
				}
			}
		);
	
		final Sum agg = new Sum();
		TemporalAggregator<Integer,Number> a = new TemporalAggregator<Integer,Number>(s, agg);
		
		Pipes.verifyStartTimeStampOrdering(a);
		
		AbstractSink sink = new AbstractSink<TemporalObject<Number>>(a) {
			LinkedList<TemporalObject<Number>> list = new LinkedList<TemporalObject<Number>>();
			
			@Override
			public void processObject(TemporalObject<Number> o, int sourceID) {				
				list.add(o);
			}
			
			@Override
			public void done(int sourceID) {
				super.done(sourceID);
				if (isDone()) {
					Iterator<TemporalObject<Number>> listIt = list.iterator();
					HashMap<Long,List<Number>> result = new HashMap<Long,List<Number>>();
					while (listIt.hasNext()) {
						TemporalObject<Number> next = listIt.next();
						List<Long> tmp = TemporalObject.getAllSnapshots(next);
						int key;
						List<Number> l;
						for (int i = 0; i < tmp.size(); i++) {
							key = (int)tmp.get(i).longValue();
							l = result.get(hashCodes[key]);
							if (l == null) l = new LinkedList<Number>();
							l.add(next.getObject()); 
							result.put(hashCodes[key], l);
						}
					}
					
					List<Integer> in;
					List<Number> out;
					for (int i = 0; i < hashCodes.length; i++) {
						in = input.get(hashCodes[i]);
						out = result.get(hashCodes[i]);	
						if ((in == null && out != null) || (in != null && out == null))
							System.err.println("ERROR: Difference in aggregate at snapshot "+i);
						if (in != null && out != null) {
							if (out.size() > 1)
								System.out.println("ERROR at snapshot "+i);
							Number aggValue = null;
							for (int j = 0; j < in.size(); j++) {
								aggValue = agg.invoke(aggValue, in.get(j));
							}
							if (!aggValue.equals(out.get(0))) {
								System.err.println("ERROR: Difference in aggregate at snapshot "+i);
								System.err.println("proper value: "+aggValue+" current value: "+out.get(0));
							}
						}
						System.out.println("CHECK finished for snapshot: "+i);		
					}
				}
			}
		};
		
		Printer printer = new Printer<TemporalObject<Number>>(a);
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(printer);
		exec.startAllQueries();
		
	}
}
