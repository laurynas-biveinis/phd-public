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
package xxl.applications.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import xxl.core.collections.MapEntry;
import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ListSAImplementor;
import xxl.core.functions.Function;
import xxl.core.functions.NTuplify;
import xxl.core.math.statistics.parametric.aggregates.Sum;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.memoryManager.heartbeat.HBQueryExecutor;
import xxl.core.pipes.memoryManager.heartbeat.HeartbeatGenerator;
import xxl.core.pipes.memoryManager.heartbeat.Heartbeats;
import xxl.core.pipes.operators.AbstractTimeStampPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.differences.TemporalDifference;
import xxl.core.pipes.operators.differences.TemporalDifference.TemporalDifferenceSA;
import xxl.core.pipes.operators.distincts.TemporalDistinct;
import xxl.core.pipes.operators.distincts.TemporalDistinct.TemporalDistinctSA;
import xxl.core.pipes.operators.filters.TemporalFilter;
import xxl.core.pipes.operators.groupers.HashGrouper;
import xxl.core.pipes.operators.groupers.TemporalGroupAndAggregator;
import xxl.core.pipes.operators.identities.Coalesce;
import xxl.core.pipes.operators.identities.Split;
import xxl.core.pipes.operators.identities.Coalesce.CoalesceSA;
import xxl.core.pipes.operators.joins.TemporalJoin;
import xxl.core.pipes.operators.joins.TemporalJoin.TemporalJoinListSA;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.operators.mappers.TemporalAggregator;
import xxl.core.pipes.operators.mappers.TemporalAggregator.TemporalAggregatorSA;
import xxl.core.pipes.operators.unions.TemporalUnion;
import xxl.core.pipes.operators.unions.TemporalUnion.TemporalUnionSA;
import xxl.core.pipes.queryGraph.VisualQueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.AbstractTimeStampSink;
import xxl.core.pipes.sinks.Tester;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * Contains some tests for heartbeats. 
 */
public class HeartbeatTests {
	
	/**
	 * For tests
	 *
	 * @param <I>
	 */
	public static class TimeStampHBSource<I extends TimeStampedObject<Integer>> extends AbstractTimeStampPipe<Integer, Integer, I, I> {
		
		public TimeStampHBSource(Source<? extends I> source, int ID) {
			super(source, ID);
		}
		
		public TimeStampHBSource() {
			super();
		}
		final TimeStampHBSource ref = this;
		ListQueue<I> queue = new ListQueue<I>();
		long last =0;
		
		@Override
		public void processObject(I o, int ID) throws IllegalArgumentException {
			queue.enqueue(o);
			if (queue.size()>1) {
				I out = queue.dequeue();
				last = out.getTimeStamp();
				transfer(out);
			}
		}
		
		@Override
		public void done(int sourceID) {
			if (isClosed) return;
			
			processingWLock.lock();
			updateDoneStatus(sourceID);
		    try {
				if (isDone()) {
					transfer(queue.dequeue());
					if (queue.size() >0)
						throw new IllegalStateException("Queue still contains Elements: "+queue);
					signalDone();
				}
			}
		    finally {
		    	processingWLock.unlock();
		    }
		}
		
		// important for tests and visualization
		@Override
		public void heartbeat(long timeStamp, int sourceID) {
			processingWLock.lock();
			try {
		    	transferHeartbeat(timeStamp);
		    }
		    finally {
		    	processingWLock.unlock();
		    }			
		}
		
		public Function<?, Long> getIgnoreHB() {
			return new Function(){
				public Long invoke() {
					processingWLock.lock();
				    try {
				    	return ref.isDone ? -1l : last;
				    }
				    finally {
				    	processingWLock.unlock();
				    }					
				}
			};

		}
		
		public Function<?, Long> getSubOptimalHB() {
			return new Function(){
				public Long invoke() {					 
					processingWLock.lock();
					    try {
					    	if (ref.isDone)
					    		return -1l;
					    	return queue.size() > 0  && queue.peek().getTimeStamp() > 0 ? queue.peek().getTimeStamp()-1 : 0;
					    }				    
					    finally {
					    	processingWLock.unlock();
					    }					
					 
				}
			};

		}
		
		public Function<?, Long> getOptimalHB() {
			return new Function(){
				@Override
				public synchronized Long invoke() {
					processingWLock.lock();
				    try { 
				    	if (ref.isDone)
				    		return -1l;					 
				    	return queue.size() > 0 ? queue.peek().getTimeStamp() : 0;
				    }
				    finally {
				    	processingWLock.unlock();
				    }
				}
			};		

		}
		
		public Function<?, Long> getIncorrectHB() {
			return new Function(){
				@Override
				public synchronized Long invoke() {
					 return queue.size() > 0 ? queue.peek().getTimeStamp()+1 : 1;
				}
			};		

		}
	}
	
	public static <I extends TimeStampedObject<Integer>>void visualHBCheck(AbstractTimeStampPipe<Integer, Integer,I, I> op, HeartbeatGenerator hb, HBQueryExecutor ex) {
		VisualSink<I>[] input = new VisualSink[op.getNoOfSources()];
		AbstractTimeStampSink[] inputChecks = new AbstractTimeStampSink[op.getNoOfSources()];
		AbstractTimeStampSink[] print = new AbstractTimeStampSink[op.getNoOfSources()];		
		VisualSink<I> output;
		VisualQueryExecutor vqe = null;
		if (ex == null)
			vqe = new VisualQueryExecutor(500, hb);
		int i;
		
		for (i = 0; i < op.getNoOfSources(); i++) {
			input[i] = new VisualSink<I>(op.getSource(i), "Input "+i+" (delivered heartbeats are tranfered without any check)", true);
			input[i].getFrame().setLocation(i%2==0 ? 0: 500, i/2 * 400);
			input[i].setPrintHB(true);
			inputChecks[i] = Heartbeats.verifyHeartbeatOrdering((AbstractTimeStampPipe)op.getSource(i), "Input "+i);
			inputChecks[i].activateHeartbeats(true);			
			print[i] = Heartbeats.printHeartbeatEffect((AbstractTimeStampPipe)op.getSource(i), "Input "+i);	
			print[i].activateHeartbeats(true);
			if (ex == null) {
				vqe.registerQuery(print[i]);
				vqe.registerQuery(inputChecks[i]);
			}
			else {
				ex.registerQuery(print[i]);
				ex.registerQuery(inputChecks[i]);			
			}
		}
		output = new VisualSink<I>(op, "Output", true);
		output.getFrame().setLocation(i%2==0 ? 0: 600, i/2 * 400);
		output.setPrintHB(true);
		Heartbeats.setActivateHeartbeatsTopDown(op, true);
		Heartbeats.setHeartbeatPredicateTopDown(op, Predicates.TRUE);
		if (ex == null) {
			vqe.registerQuery(output);
			vqe.startAllQueries();
		}
	}
	
	public static <I extends TimeStampedObject<Integer>>void visualHBCheck(AbstractTimeStampPipe<Integer, Integer,I, I> op, HeartbeatGenerator hb) {
		visualHBCheck(op, hb, null);
	}
	
	public static void statelessOpsTest() {
		final int sleep = 200;
		Enumerator e = new Enumerator(50,sleep);
		Function<Integer,TemporalObject<Integer>> mapToTSO = new Function<Integer,TemporalObject<Integer>>() {
			long start =0;
			long step = sleep;
			long length = sleep*10;
			public TemporalObject<Integer> invoke(Integer o) {
				TemporalObject<Integer> tso = new TemporalObject<Integer>(o, new TimeInterval(start, start+length));
				start += step; 
				return tso;
			}
		};
		Mapper<Integer,TemporalObject<Integer>> m = new Mapper<Integer,TemporalObject<Integer>>(e, mapToTSO);
		TimeStampHBSource<TemporalObject<Integer>> hbt = new TimeStampHBSource<TemporalObject<Integer>>(m, 0);		
		TemporalFilter<Integer> f = new TemporalFilter<Integer>(hbt, new Predicate<TemporalObject<Integer>>(){
			int i=0;
			public boolean invoke(TemporalObject<Integer> o) {
				return (i++) % 2 ==0; 
			}
		});
		// Test heartbeat predicate
		//f.setHeartbeatPredicate(Predicates.TRUE);
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator(f, sleep, hbt.getOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, sleep, hbt.getSubOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(f, sleep, hbt.getIgnoreHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(f, sleep, hbt.getIncorrectHB());
		visualHBCheck(f, hb);
	}

	public static void temporalUnionExtendedMainTest() {
		final int sleep = 5;

		final int noOfElements1 = 9000;
		final int noOfElements2 = 7000;
		final int buckets = 10;
		final int startInc = 25;
		final int intervalSize = 200;
		final List<TemporalObject<Integer>> in = Collections.synchronizedList(new ArrayList<TemporalObject<Integer>>());
		
		final Integer[] hashCodes = new Integer[buckets];
		for (int i = 0; i < buckets; i++)
			hashCodes[i] = new Integer(i);
			
		RandomNumber<Integer> r1 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements1, sleep
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, sleep
		);		
		
		final Function<Integer,TemporalObject<Integer>> decorateWithIntervals = new Function<Integer,TemporalObject<Integer>>() {
			long start, end;
			public synchronized TemporalObject<Integer> invoke(Integer o) {
				start = new Long(System.currentTimeMillis()).longValue();//
				end = new Long(System.currentTimeMillis() + intervalSize).longValue();
				TemporalObject<Integer> object = new TemporalObject<Integer>(o%buckets, 
					new TimeInterval(start, end)
				);				
				in.add(object);
				return object;
			}
		};		
		Source<TemporalObject<Integer>> s1 = new Mapper<Integer, TemporalObject<Integer>>(r1, decorateWithIntervals);
		Source<TemporalObject<Integer>> s2 = new Mapper<Integer, TemporalObject<Integer>>(r2, decorateWithIntervals);
		
		TimeStampHBSource[] hbts = new TimeStampHBSource[2];
		int[] iDs = new int[2];
		for (int i = 0; i < hbts.length; i++) {
			hbts[i] = new TimeStampHBSource<TemporalObject<Integer>>(i==0 ? s1 : s2, 0);
			iDs[i] = i;
		}
		TemporalUnion<Integer> u = new TemporalUnion<Integer>(hbts, iDs, new TemporalUnionSA<Integer>(iDs.length));		
		Pipes.verifyLexicographicalOrdering(u);
		
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
		
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator();
		for (int i = 0; i < iDs.length; i++) {
				hb.addSource(hbts[i], sleep, hbts[i].getOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getSubOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIgnoreHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIncorrectHB(), i);
		}
		visualHBCheck(u, hb);
	}
	
	public static void temporalUnionTest() {		
		final int sleep = 2000;
		int numOfSources = 3;
		Enumerator[] enums = new Enumerator[numOfSources];
		Function[] mapToTSOs = new Function[numOfSources];
		Mapper[] maps = new Mapper[numOfSources];
		TimeStampHBSource[] hbts = new TimeStampHBSource[numOfSources];
		int[] iDs = new int[numOfSources];
		for (int i = 0; i < hbts.length; i++) {
			enums[i] = new Enumerator(10,sleep);
			mapToTSOs[i] = new Function<Integer,TemporalObject<Integer>>() {
				long start =0;
				long step = sleep;
				long length = sleep*10;
				public TemporalObject<Integer> invoke(Integer o) {
					TemporalObject<Integer> tso = new TemporalObject<Integer>(o, new TimeInterval(start, start+length));
					start += step; 
					return tso;
				}
			};
			maps[i] = new Mapper<Integer,TemporalObject<Integer>>(enums[i], mapToTSOs[i]);
			hbts[i] = new TimeStampHBSource<TemporalObject<Integer>>(maps[i], 0);
			iDs[i] = i;
		}
		
		TemporalUnion<Integer> u = new TemporalUnion<Integer>(hbts, iDs, new TemporalUnionSA<Integer>(iDs.length));
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator();
		for (int i = 0; i < iDs.length; i++) {
				hb.addSource(hbts[i], sleep, hbts[i].getOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getSubOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIgnoreHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIncorrectHB(), i);
		}
		visualHBCheck(u, hb);
	}
	
	public static void dupAggTest() {
		final int sleep = 2000;
		Enumerator e = new Enumerator(10,sleep);
		//RandomNumber e = new RandomNumber();
		Function<Integer,TemporalObject<Integer>> mapToTSO = new Function<Integer,TemporalObject<Integer>>() {
			long start =0;
			long step = sleep;
			long length = sleep*3;
			public TemporalObject<Integer> invoke(Integer o) {
				TemporalObject<Integer> tso = new TemporalObject<Integer>(o, new TimeInterval(start, start+length));
				start += step; 
				return tso;
			}
		};
		Mapper<Integer,TemporalObject<Integer>> m = new Mapper<Integer,TemporalObject<Integer>>(e, mapToTSO);
		TimeStampHBSource<TemporalObject<Integer>> hbt = new TimeStampHBSource<TemporalObject<Integer>>(m, 0);
		TemporalAggregator op = new TemporalAggregator(hbt, new Sum());
		//TemporalDistinct<Integer> op = new TemporalDistinct<Integer>(hbt);

		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator(hbt, sleep, hbt.getOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, sleep, hbt.getSubOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, sleep, hbt.getIgnoreHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, sleep, hbt.getIncorrectHB());
		visualHBCheck(op, hb);
	}
	
	public static void distinctExtendedMainTest() {
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
			RandomNumber.DISCRETE, noOfElements, startInc/2
		);		
		Source<TemporalObject<Integer>> s = Pipes.decorateWithRandomTimeIntervals(r, input, hashCodes, startInc, intervalSize, seed);
		TimeStampHBSource<TemporalObject<Integer>> hbt = new TimeStampHBSource<TemporalObject<Integer>>(s, 0);
		TemporalDistinct<Integer> d = new TemporalDistinct<Integer>(hbt, 0, new TemporalDistinctSA<Integer>());
		
		Pipes.verifyLexicographicalOrdering(d);
		
		AbstractSink sink = new AbstractSink<TemporalObject<Integer>>(d) {
			LinkedList<TemporalObject<Integer>> list = new LinkedList<TemporalObject<Integer>>();
			
			@Override
			public void processObject(TemporalObject<Integer> o, int sourceID) {	
				//System.out.println("o: "+o);
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
		HBQueryExecutor exec = new HBQueryExecutor();
		exec.registerQuery(sink);	
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getSubOptimalHB());
		HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIgnoreHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIncorrectHB());		
		visualHBCheck(d, hb, exec);		
	}
	
	public static void temporalAggregatorExtendedMainTest() {
		final int noOfElements = 1000;
		final int buckets = 10;
		final int startInc = 50;
		final int intervalSize = 75;
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
		TimeStampHBSource<TemporalObject<Integer>> hbt = new TimeStampHBSource<TemporalObject<Integer>>(s, 0);
	
		final Sum agg = new Sum();
		TemporalAggregator a = new TemporalAggregator(hbt, agg);
		
		Pipes.verifyLexicographicalOrdering(a);
		
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
		HBQueryExecutor exec = new HBQueryExecutor();
		exec.registerQuery(sink);		
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getSubOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIgnoreHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIncorrectHB());		
		visualHBCheck(a, hb, exec);		
		exec.startAllQueries();
	}

	public static void joinTest() {
		final int sleep = 1000;
		int numOfSources = 2;
		Enumerator[] enums = new Enumerator[numOfSources];
		Function[] mapToTSOs = new Function[numOfSources];
		Mapper[] maps = new Mapper[numOfSources];
		TimeStampHBSource[] hbts = new TimeStampHBSource[numOfSources];
		for (int i = 0; i < hbts.length; i++) {
			enums[i] = new Enumerator(30,sleep);
			mapToTSOs[i] = new Function<Integer,TemporalObject<Integer>>() {
				long start =0;
				long step = sleep;
				long length = sleep*10;
				public TemporalObject<Integer> invoke(Integer o) {
					TemporalObject<Integer> tso = new TemporalObject<Integer>(o, new TimeInterval(start, start+length));
					start += step; 
					return tso;
				}
			};
			maps[i] = new Mapper<Integer,TemporalObject<Integer>>(enums[i], mapToTSOs[i]);
			hbts[i] = new TimeStampHBSource<TemporalObject<Integer>>(maps[i], 0);			
		}
		final Function<TemporalObject<Integer>,Integer> hash = new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> o) {
				return o.getObject()%17;
			}
		};

		TemporalJoin<Integer,Integer> join = new TemporalJoin<Integer,Integer>(hbts[0], hbts[1], 0, 1,
				new TemporalJoinListSA<Integer>(
					new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
					0, 2
				),
				new TemporalJoinListSA<Integer>(
					new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
					1, 2
				),
				new Function<Integer,Integer>(){
					@Override
					public Integer invoke(Integer i1, Integer i2) {
						return i1;
					}
				}
			);
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator();
		for (int i = 0; i < hbts.length; i++) {
				//hb.addSource(hbts[i], sleep, hbts[i].getOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getSubOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIgnoreHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIncorrectHB(), i);
		}
		visualHBCheck(join, hb);
	}
	
	public static void temporalJoinExtendedMainTest() {
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
			RandomNumber.DISCRETE, noOfElements1, 10
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, 10
		);		
		
		Source<TemporalObject<Integer>> s1 = Pipes.decorateWithRandomTimeIntervals(r1, in1, hashCodes, startInc, intervalSize, seed);
		Source<TemporalObject<Integer>> s2 = Pipes.decorateWithRandomTimeIntervals(r2, in2, hashCodes, startInc, intervalSize, seed);
		TimeStampHBSource[] hbts = new TimeStampHBSource[2];
		hbts[0] = new TimeStampHBSource<TemporalObject<Integer>>(s1, 0);
		hbts[1] = new TimeStampHBSource<TemporalObject<Integer>>(s2, 0);
				
		// Symmetric Hash-Join
		//TemporalJoin<Integer,Object[]> join = new TemporalJoin<Integer,Object[]>(hbts[0], hbts[1], 0, 1,
		TemporalJoin join = new TemporalJoin(hbts[0], hbts[1], 0, 1,
			new TemporalJoinListSA<Integer>(
				new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
				0, 2
			),
			new TemporalJoinListSA<Integer>(
				new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
				1, 2
			),
			NTuplify.DEFAULT_INSTANCE
		);
		
		Pipes.verifyLexicographicalOrdering(join);	
		
		AbstractSink sink = new AbstractSink<TemporalObject<Object[]>>(join) {
			LinkedList<TemporalObject<Object[]>> list = new LinkedList<TemporalObject<Object[]>>();
			
			@Override
			public void processObject(TemporalObject<Object[]> o, int sourceID) {				
				list.add(o);
			}
			
			@Override
			public void done(int sourceID) {
				super.done(sourceID);
				if (isDone()) {
					System.out.println("Verifying results ...");
					Iterator<TemporalObject<Object[]>> listIt = list.iterator();
					HashMap<Integer,List<Long>> result = new HashMap<Integer,List<Long>>();
					while (listIt.hasNext()) {
						TemporalObject<Object[]> t = listIt.next();
						int key = ((Integer)(t.getObject())[0]).intValue();
						List<Long> l = result.get(hashCodes[key]);
						if (l == null) l = new LinkedList<Long>();
						l.addAll(TemporalObject.getAllSnapshots(t));					
						result.put(hashCodes[key], l);
					}
					
					// check for multiplicity 
					for (int i = 0; i < buckets; i++) {
						List<Long> listIn1 = in1.get(hashCodes[i]);
						List<Long> listIn2 = in2.get(hashCodes[i]);
						List<Long> out = result.get(hashCodes[i]);
						if ((listIn1 != null || listIn2 != null) && out != null) {
							Iterator<Long> it = listIn1.iterator();					
							while (it.hasNext()) {
								Long next = it.next();
								if (listIn2.contains(next)) {
									if (!out.contains(next)) 
										System.err.println("ERROR: element lost: value "+i+" at time "+next);						
								}
							}
							System.out.println("CHECK 1 finished for bucket "+i+".");
							it = out.iterator();
							while (it.hasNext()) {
								Long next = it.next();
								if (!listIn1.contains(next) || !listIn2.contains(next)) 
									System.err.println("ERROR: more elements than expected: value "+i+" at time "+next);
								it.remove();	
							}
							if (out.size() > 0)
								System.err.println("ERROR: more elements than expected");
							System.out.println("CHECK 2 finished for bucket "+i+".");
						}
						else {
							if ((listIn1 == null || listIn2 == null) && out != null)
								System.err.println("ERROR: Output, but no valid input. (i = "+i+")");
						}
					}
				}
			}
		};
		/*
		Printer printer = new Printer<TemporalObject<Object[]>>(join) {
			@Override
			public void process(TemporalObject<Object[]> object, int sourceID) {
				Object[] o = object.getObject();
				StringBuffer s = new StringBuffer();
				s.append("[");
				for (int i = 0; i < o.length-1; i++) {
					s.append(o[i]+", ");	
				}
				s.append(o[o.length-1]+"]; "+object.getTimeInterval());
				System.out.println(s);
			}
		};*/
		
		HBQueryExecutor exec = new HBQueryExecutor();
		exec.registerQuery(sink);
		//exec.registerQuery(printer);
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator();
		for (int i = 0; i < hbts.length; i++) {
				hb.addSource(hbts[i], startInc, hbts[i].getOptimalHB(), i);
				//hb.addSource(hbts[i], startInc, hbts[i].getSubOptimalHB(), i);
				//hb.addSource(hbts[i], startInc, hbts[i].getIgnoreHB(), i);
				//hb.addSource(hbts[i], startInc, hbts[i].getIncorrectHB(), i);
		}
		visualHBCheck(join, hb, exec);
		exec.startAllQueries();
	}

	public static void differenceTest() {
		final int sleep = 500;
		int numOfSources = 2;
		Enumerator[] enums = new Enumerator[numOfSources];
		Function[] mapToTSOs = new Function[numOfSources];
		Mapper[] maps = new Mapper[numOfSources];
		TimeStampHBSource[] hbts = new TimeStampHBSource[numOfSources];
		int[] iDs = new int[numOfSources];
		final Function<TemporalObject<Integer>,Integer> hash = new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> o) {
				return o.getObject()%17;
			}
		};
		enums[0] = new Enumerator(20,sleep);
		enums[1] = new Enumerator(5,25,sleep);

		for (int i = 0; i < hbts.length; i++) {			
			mapToTSOs[i] = new Function<Integer,TemporalObject<Integer>>() {
				long start =0;
				long step = sleep;
				long length = sleep*10;
				public TemporalObject<Integer> invoke(Integer o) {
					TemporalObject<Integer> tso = new TemporalObject<Integer>(o, new TimeInterval(start, start+length));
					start += step; 
					return tso;
				}
			};
			maps[i] = new Mapper<Integer,TemporalObject<Integer>>(enums[i], mapToTSOs[i]);
			hbts[i] = new TimeStampHBSource<TemporalObject<Integer>>(maps[i], 0);
			iDs[i] = i;
		}
		
		TemporalDifference<Integer> d = new TemporalDifference<Integer>(hbts[0], hbts[1], 0, 1, 
				new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 0, 36, 4096*4096, 1024*1024),
				new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 1, 36, 4096*4096, 1024*1024)
			);

		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator();
		for (int i = 0; i < iDs.length; i++) {
				hb.addSource(hbts[i], sleep, hbts[i].getOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getSubOptimalHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIgnoreHB(), i);
				//hb.addSource(hbts[i], sleep, hbts[i].getIncorrectHB(), i);
		}
		visualHBCheck(d, hb);
	}
	
	public static void temporalDifferenceExtendedMainTest() {
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
			RandomNumber.DISCRETE, noOfElements1, 2
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, 2
		);		
		
		Source<TemporalObject<Integer>> s1 = Pipes.decorateWithRandomTimeIntervals(r1, in1, hashCodes, startInc, intervalSize, seed);
		Source<TemporalObject<Integer>> s2 = Pipes.decorateWithRandomTimeIntervals(r2, in2, hashCodes, startInc, intervalSize, 2*seed);
		TimeStampHBSource[] hbts = new TimeStampHBSource[2];
		hbts[0] = new TimeStampHBSource<TemporalObject<Integer>>(s1, 0);
		hbts[1] = new TimeStampHBSource<TemporalObject<Integer>>(s2, 0);
		TemporalDifference<Integer> d = new TemporalDifference<Integer>(hbts[0], hbts[1], 0, 1, 
			new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 0, 36, 4096*4096, 1024*1024),
			new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 1, 36, 4096*4096, 1024*1024)
		);
		
		Pipes.verifyLexicographicalOrdering(d);
		
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
		HeartbeatGenerator hb = new HeartbeatGenerator();
		HBQueryExecutor exec = new HBQueryExecutor(500, hb);
		exec.registerQuery(sink);
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------		
		for (int i = 0; i < hbts.length; i++) {
				hb.addSource(hbts[i], startInc/2, hbts[i].getOptimalHB(), i);
				//hb.addSource(hbts[i], startInc/2, hbts[i].getSubOptimalHB(), i);
				//hb.addSource(hbts[i], startInc/2, hbts[i].getIgnoreHB(), i);
				//hb.addSource(hbts[i], startInc/2, hbts[i].getIncorrectHB(), i);
		}
		visualHBCheck(d, hb, exec);
		exec.startAllQueries();
	}
	
	public static void splitExtendedMainTest() {
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
		TimeStampHBSource<TemporalObject<Integer>> hbt = new TimeStampHBSource<TemporalObject<Integer>>(s, 0);
		Split<Integer> split = new Split<Integer>(hbt, 12);
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
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getOptimalHB());
		HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getSubOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIgnoreHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIncorrectHB());
		HBQueryExecutor exec = new HBQueryExecutor(500, hb);
		exec.registerQuery(sink);
		visualHBCheck(split, hb, exec);
		exec.startAllQueries();
	}
	
	public static void coalesceExtendedMainTest() {
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
		TimeStampHBSource<TemporalObject<Integer>> hbt = new TimeStampHBSource<TemporalObject<Integer>>(s, 0);
		final long epsilon = 5000;		
		Coalesce<Integer> c = new Coalesce<Integer>(hbt, 0, new CoalesceSA<Integer>(new ListSAImplementor<TemporalObject<Integer>>(), epsilon));
		// check sort order
		Pipes.verifyLexicographicalOrdering(c);

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
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getSubOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIgnoreHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIncorrectHB());
		HBQueryExecutor exec = new HBQueryExecutor();
		exec.registerQuery(sink);
		visualHBCheck(c, hb, exec);
		exec.startAllQueries();
	}
	
	public static void groupAndAggregatorExtendedMain() {
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
		TimeStampHBSource<TemporalObject<Integer>> hbt = new TimeStampHBSource<TemporalObject<Integer>>(s, 0);
		// direct implementation
		TemporalGroupAndAggregator a = new TemporalGroupAndAggregator(hbt,
			hashFunction, agg
		);
	
		// indirect implementation
		int noOfGroups = 1117;
		HashGrouper<TemporalObject<Integer>> grouper = new HashGrouper<TemporalObject<Integer>>(s, 0, hashFunction, noOfGroups);
		
		TemporalAggregator<Integer,Number>[] aggs = new TemporalAggregator[noOfGroups];
		Mapper<TemporalObject<Number>,TemporalObject<MapEntry<Integer,Number>>>[] mappers = new Mapper[noOfGroups];
		
		Function<?,Function<TemporalObject<Number>,TemporalObject<MapEntry<Integer,Number>>>> assignGroupFunction = new Function<Object,Function<TemporalObject<Number>,TemporalObject<MapEntry<Integer,Number>>>>() {
			protected int groupID = 0;
			
			@Override
			public Function<TemporalObject<Number>,TemporalObject<MapEntry<Integer,Number>>> invoke() {
				
				Function<TemporalObject<Number>,TemporalObject<MapEntry<Integer,Number>>> assignGroup = new Function<TemporalObject<Number>,TemporalObject<MapEntry<Integer,Number>>>() {
					
					protected final int id = groupID;
					
					@Override
					public TemporalObject<MapEntry<Integer,Number>> invoke(TemporalObject<Number> tso) {
						return new TemporalObject<MapEntry<Integer,Number>>(
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
			mappers[i] = new Mapper<TemporalObject<Number>,TemporalObject<MapEntry<Integer,Number>>>(aggs[i], assignGroupFunction.invoke());
		}
		TemporalUnion<MapEntry<Integer,Number>> u = new TemporalUnion<MapEntry<Integer,Number>>(mappers, new TemporalUnionSA<MapEntry<Integer,Number>>(noOfGroups));
	
		// check sorting
		Pipes.verifyLexicographicalOrdering(a);
		Pipes.verifyLexicographicalOrdering(u);
		
		// compare results of both implementations
		Coalesce<MapEntry<Integer,Number>> c1 = new Coalesce<MapEntry<Integer,Number>>(a, 0, new CoalesceSA<MapEntry<Integer,Number>>(new ListSAImplementor<TemporalObject<MapEntry<Integer,Number>>>(), intervalSize*10));
		Coalesce<MapEntry<Integer,Number>> c2 = new Coalesce<MapEntry<Integer,Number>>(u, 0, new CoalesceSA<MapEntry<Integer,Number>>(new ListSAImplementor<TemporalObject<MapEntry<Integer,Number>>>(), intervalSize*10));
		
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
						Iterator it1 = list1.iterator();
						Iterator it2 = list2.iterator();
						while (it1.hasNext()) {
							if (it2.hasNext()) {
								Object next1 = it1.next();
								Object next2 = it2.next();
								if (!next1.equals(next2))
									System.err.println("ERROR: Results not equal: "+next1+" != "+next2);
							}
							else
								System.err.println("ERROR: Missing results in indirect implementation.");
						}
						if (it2.hasNext())
							System.err.println("ERROR: Missing results in direct implementation.");
						System.out.println("CHECKS FINISHED.");
					}
					finally {
						processingWLock.unlock();
					}
				}
			}
		};

		Tester t1 = new Tester<TemporalObject<MapEntry<Integer,Number>>>(c1);
		Tester t2 = new Tester<TemporalObject<MapEntry<Integer,Number>>>(c2);
		//Tester t3= new Tester(a);		
		//--------------------------------------------------------------------------
		// tests
		//--------------------------------------------------------------------------
		HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getSubOptimalHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIgnoreHB());
		//HeartbeatGenerator hb = new HeartbeatGenerator(hbt, startInc/2, hbt.getIncorrectHB());
		HBQueryExecutor exec = new HBQueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(t1);
		exec.registerQuery(t2);
		//exec.registerQuery(t3);
		visualHBCheck(a, hb, exec);
		hb.start();
		exec.startAllQueries();		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//statelessOpsTest();
		//temporalUnionTest();
		//temporalUnionExtendedMainTest();
		//dupAggTest();
		//distinctExtendedMainTest();
		//temporalAggregatorExtendedMainTest();
		//differenceTest();
		//temporalDifferenceExtendedMainTest();
		joinTest();
		//temporalJoinExtendedMainTest();
		//splitExtendedMainTest();
		//coalesceExtendedMainTest();
		//groupAndAggregatorExtendedMain();
	}
}
