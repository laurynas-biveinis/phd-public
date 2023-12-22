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
package xxl.core.pipes.memoryManager.heartbeat;

import java.util.ArrayList;
import java.util.Iterator;

import xxl.core.cursors.sources.Enumerator;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.operators.AbstractTimeStampPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sinks.AbstractTimeStampSink;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * Contains some static methods for heartbeats. 
 */
public class Heartbeats {

	private Heartbeats() {}
	
	/**
	 * A class for storing skew bound entries. For a detailed discussion see "Widom and Srivastava - 
	 * Flexible Time Managemaent in Data Stream Systems".
	 */
	public static class SkewBoundEntry{
		protected long t;
		protected long delta;
		
		public long getDelta() {
			return delta;
		}
		public void setDelta(long delta) {
			this.delta = delta;
		}
		public long getT() {
			return t;
		}
		public void setT(long t) {
			this.t = t;
		}
	}
	
	/*---------------------------------------------------------------------------------
	 *  Helper methods
	 ---------------------------------------------------------------------------------*/
	
	public static void notNull(Object o, String s) {
		if (o == null)
			throw new IllegalArgumentException(s+", "+o+" must not be null");
	}
	
	public static void positive(long l, String s) {
		if (l <= 0)
			throw new IllegalArgumentException(s+", "+l+" is not positive");
	}
	
	/**
	 * A Function to generate heartbeats using System.currentTimeMillis() - delta. It 
	 * can be used when incoming objects get the system time as timestamp and the skew is known.  
	 * @param delta delivers 
	 * @return A function to generate heartbeats.
	 */
	@SuppressWarnings({"unchecked","serial"})
	public static Function<?, Long> getSystemTimeHB(final long delta) {
		return new Function(){
			@Override
			public Long invoke() {
				return new Long(System.currentTimeMillis() - delta);
			}
		};		
	}

	/**
	 * A Function to generate heartbeats using System.currentTimeMillis. It can be used
	 * for source.setNextHBFunction when incoming objects get the system time as timestamp.
	 * @return A function to generate heartbeats.
	 */
	public static Function<?, Long> getSystemTimeHB() {
		return getSystemTimeHB(0l);
	}
	
	/**
	 * A test function to generate heartbeats using an enumerator.   
	 * @param numberOfHeartbeats the number of heartbeats that are delivered. A 
	 * value below zero delivers infinite number of heartbeats.
	 * @return A function to generate test heartbeats. 
	 */
	@SuppressWarnings({"unchecked","serial"})
	public static Function<?, Long> getTestHB(final int numberOfHeartbeats) {
		return new Function(){
			Enumerator e = numberOfHeartbeats <= 0 
				? new Enumerator() : new Enumerator(0, numberOfHeartbeats); 
			@Override
			public Long invoke() {
				return new Long(e.next());
			}
		};		
	}
	
	/**
	 * A test function to generate heartbeats using an enumerator.
	 * value below zero delivers infinite number of heartbeats.
	 * @return A function to generate test heartbeats. 
	 */
	@SuppressWarnings({"unchecked","serial"})
	public static Function<?, Long> getTestHBCursor(final Enumerator c) {
		return new Function(){
			@Override
			public Long invoke() {
				return new Long(c.next());
			}
		};		
	}
	
	/**
	 * A test function to generate heartbeats using an enumerator.
	 * value below zero delivers infinite number of heartbeats.
	 * @return A function to generate test heartbeats. 
	 */
	@SuppressWarnings({"unchecked","serial"})
	public static Function<?, Long> getTestHBCursor(final Iterator<Long> i) {
		return new Function(){
			@Override
			public Long invoke() {
				 return new Long(i.hasNext() ? i.next() : new Long(Heartbeat.NO_HEARTBEAT));				
			}
		};		
	}
	
	/**
	 * A test function to generate infinite heartbeats using an enumerator.
	 * @return A function to generate test heartbeats. 
	 */
	public static Function<?, Long> getTestHB() {
		return getTestHB(-1);		
	}
	
	public static Function<?, Long> getMinTS(final AbstractTimeStampPipe pipe) {
		return new Function(){
			@Override
			public Long invoke() {
				 return pipe.getMinTimeStamp();				
			}
		};		
	}

	//---------------------------------------------------------------------------------------
	// Functions for Memorymanager.HeartbeatManager or Sources
	//---------------------------------------------------------------------------------------

	// heartbeats and skew bound matrix [i][j]. Doesn't use diagonal entries.
	// i --> source
	// j --> time distance to jth source
	@SuppressWarnings({"unchecked","serial"})
	public static Function<?, Long> getMatrixHBFunction(final AbstractTimeStampPipe pipe, final long[][] matrix) {
		notNull(matrix, "matrix[][]");
		notNull(pipe, "pipe");
		for (long[] outer: matrix) {
			notNull(outer, "matrix[]");
			if (pipe.getLatestTimeStamps().length != outer.length)
				throw new IllegalArgumentException("Size of matrix and minimums don't match");
		}
		return new Function() {
			@Override
			public Long invoke() {
				long[] sums = new long[pipe.getLatestTimeStamps().length];				
				for (int i=0; i<sums.length;i++) {
					sums[i]= Long.MIN_VALUE;
					for (int j=0; j<sums.length;j++)
						sums[i] = Math.max(pipe.getLatestTimeStamps()[i] - matrix[i][j], sums[i]);					
				}
				long max = Long.MIN_VALUE; 
				for (Long l : sums)
					max = Math.min(max, l);
				return max;
			}
		};
	}

	// heartbeats and skew bound matrix [i][j]. Doesn't use diagonal entries. uses application	
	// and system time.
	// i --> source
	// j --> time distance to jth source
	@SuppressWarnings({"unchecked","serial"})
	public static Function<?, Long> getMatrixHBFunction(final AbstractTimeStampPipe pipe, final SkewBoundEntry[][] matrix) {
		notNull(matrix, "matrix[][]");
		notNull(pipe, "pipe");
		for (SkewBoundEntry[] outer: matrix) {
			notNull(outer, "matrix[]");
			if (pipe.getLatestTimeStamps().length != outer.length)
				throw new IllegalArgumentException("Size of matrix and minimums don't match");
		}
		return new Function() {
			@Override
			public Long invoke() {
				long[] sums = new long[pipe.getLatestTimeStamps().length];				
				for (int i=0; i<sums.length;i++) {
					sums[i]= Long.MIN_VALUE;
					for (int j=0; j<sums.length;j++)
						sums[i] = Math.max(pipe.getLatestTimeStamps()[i] - (matrix[i][j].delta + matrix[i][j].t), sums[i]);					
				}
				long max = Long.MIN_VALUE; 
				for (Long l : sums)
					max = Math.min(max, l);
				return max;
			}
		};
	}

	//---------------------------------------------------------------------------------------
	// heartbeat predicates 
	//---------------------------------------------------------------------------------------
	
	/**
	 * @param millis 
	 * @return a predicate that is true if the time between two invoke calls is greater or equal millis milli seconds. 
	 * 
	 */
	@SuppressWarnings({"unchecked","serial"})
	public static Predicate everyNMillis(final long millis) {
		return new Predicate(){
			long time = System.currentTimeMillis();
			@Override
			public boolean invoke() {
				if (System.currentTimeMillis() - time > millis) {
					time=System.currentTimeMillis();
					return true;
				}
				return false;					
			}
		};
	}
	
	/**
	 * 
	 * @param op
	 * @param pos
	 * @return true, iff the ith source of op has the latest timestamp 
	 */
	@SuppressWarnings("unchecked")
	public static Predicate minTimeStampSource(final AbstractTimeStampPipe op, final int pos) {
		return new Predicate(){
			public boolean invoke() {				
				return op.getLatestTimeStamps()[pos] == op.getMinTimeStamp();
			}
		};
	}
	
	/**
	 * 
	 * @param op
	 * @param pos
	 * @param epsilon
	 * @return true, iff the ith source of op has the latest timestamp or the difference is <= epsilon  
	 */
	@SuppressWarnings("unchecked")
	public static Predicate epsilonMinTimeStampSource(final AbstractTimeStampPipe op, final int pos, final int epsilon) {
		return new Predicate(){
			public boolean invoke() {				
				return op.getLatestTimeStamps()[pos] - op.getMinTimeStamp() <= epsilon;
			}
		};
	}

	/**
	 * 
	 * @param op
	 * @param diff
	 * @return true, iff |latest timestamp - lastHB| >= diff  
	 */
	@SuppressWarnings("unchecked")
	public static Predicate minLastHBDifference(final AbstractTimeStampPipe op, final int diff) {
		return new Predicate(){
			public boolean invoke() {				
				return Math.abs(op.getLastHB() - op.getMinTimeStamp()) >= diff;
			}
		};
	}
	
	/**
	 * @param n
	 * @return true, for every nth call  
	 */
	@SuppressWarnings("unchecked")
	public static Predicate everyNTh(final int n) {
		return new Predicate(){
			int counter =0;
			public boolean invoke() {				
				return ++counter %n==0;
			}
		};
	}
	
	//---------------------------------------------------------------------------------------
	// Activates or deactivates heartbeats in a query graph top-down or bottom-up
	//---------------------------------------------------------------------------------------
	
	public static void setActivateHeartbeatsBottomUp(Source[] sources, boolean on) {
		ArrayList<Sink<?>> l = new ArrayList<Sink<?>>();
		for (Source<?> source : (Source[])sources) {
			for (int i=0; i < source.getNoOfSinks();i++) {
				l.add(source.getSink(i));				
			}
			if (source instanceof Heartbeat)
				((Heartbeat)source).activateHeartbeats(on);			
		}
		while (!l.isEmpty()) {
			Sink<?> entry = l.remove(0);
			if (entry instanceof Heartbeat)
				((Heartbeat)entry).activateHeartbeats(on);
			if (entry instanceof Source)
				for (int i=0; i < ((Source)entry).getNoOfSinks();i++)
					l.add(((Source)entry).getSink(i));
		}
	}
	
	public static void setActivateHeartbeatsTopDown(Sink[] sinks, boolean on) {
		ArrayList<Source<?>> l = new ArrayList<Source<?>>();
		for (Sink<?> sink : (Sink[])sinks) {
			for (int i=0; i < sink.getNoOfSources();i++)				
				l.add(sink.getSource(i));
			if (sink instanceof Heartbeat)
				((Heartbeat)sink).activateHeartbeats(on);
		}
		while (!l.isEmpty()) {
			Source<?> entry = l.remove(0);
			if (entry instanceof Heartbeat)
				((Heartbeat)entry).activateHeartbeats(on);
			if (entry instanceof Sink)
				for (int i=0; i < ((Sink)entry).getNoOfSources();i++)
					l.add(((Sink)entry).getSource(i));
		}
	}
	
	public static void setActivateHeartbeatsTopDown(Sink<?> sink, boolean on) {
		setActivateHeartbeatsTopDown(new Sink[]{sink}, on);
	}
	
	//---------------------------------------------------------------------------------------
	// Sets the heartbeat predicate in a query graph top-down or bottom-up
	//---------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public static void setHeartbeatPredicateBottomUp(Source[] sources, Predicate pred) {
		ArrayList<Sink<?>> l = new ArrayList<Sink<?>>();
		for (Source<?> source : (Source[])sources) {
			for (int i=0; i < source.getNoOfSinks();i++) {
				l.add(source.getSink(i));				
			}
			if (source instanceof HeartbeatPipe)
				((HeartbeatPipe)source).setHeartbeatPredicate(pred);			
		}
		while (!l.isEmpty()) {
			Sink<?> entry = l.remove(0);
			if (entry instanceof HeartbeatPipe)
				((HeartbeatPipe)entry).setHeartbeatPredicate(pred);
			if (entry instanceof Source)
				for (int i=0; i < ((Source)entry).getNoOfSinks();i++)
					l.add(((Source)entry).getSink(i));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setHeartbeatPredicateTopDown(Sink[] sinks, Predicate pred) {
		ArrayList<Source<?>> l = new ArrayList<Source<?>>();
		for (Sink<?> sink : (Sink[])sinks) {
			for (int i=0; i < sink.getNoOfSources();i++)				
				l.add(sink.getSource(i));
			if (sink instanceof HeartbeatPipe)
				((HeartbeatPipe)sink).setHeartbeatPredicate(pred);			
		}
		while (!l.isEmpty()) {
			Source<?> entry = l.remove(0);
			if (entry instanceof HeartbeatPipe)
				((HeartbeatPipe)entry).setHeartbeatPredicate(pred);			
			if (entry instanceof Sink)
				for (int i=0; i < ((Sink)entry).getNoOfSources();i++)
					l.add(((Sink)entry).getSource(i));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setHeartbeatPredicateTopDown(Sink<?> sink, Predicate pred) {
		setHeartbeatPredicateTopDown(new Sink[]{sink}, pred);
	}
	
	//---------------------------------------------------------------------------------------
	// heartbeat ordering and check if heartbeat updates minimums
	//---------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public static <I> AbstractTimeStampSink<I, TimeStampedObject<I>> verifyHeartbeatOrdering(AbstractTimeStampPipe[] sources, int[] sourceIDs, final String name) {
		return new AbstractTimeStampSink<I, TimeStampedObject<I>>(sources, sourceIDs) {
			@Override
			public void process(TimeStampedObject<I> o, int sourceID) throws IllegalArgumentException {
				processingWLock.lock();
				graph.RLock.lock();
				try {
					long time = o.getTimeStamp();
					int index = Pipes.getSourceIndex(sourceIDs, sourceID);
					if ((lastHB != -1 && lastHB != Long.MAX_VALUE && time < lastHB) || (latestTimeStamps[index] != Long.MAX_VALUE && latestTimeStamps[index] > time)) {
						throw new HeartbeatException(name+": Results might be lost. Element: "+o
								+" lastHB: "+lastHB+" latestTimeStamps["+sourceID+"]: "+latestTimeStamps[index]);
					}
					latestTimeStamps[index] = Math.max(time, latestTimeStamps[index]);
					updateMinTimeStamp();			
					super.process(o, sourceID);
					lastHB = Math.max(lastHB, minTimeStamp);
				}
				finally {
					graph.RLock.unlock();
					processingWLock.unlock();
				}		
			}

			@Override
			public void processObject(TimeStampedObject<I> o, int sourceID) throws IllegalArgumentException {}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static <I> AbstractTimeStampSink<I, TimeStampedObject<I>> verifyHeartbeatOrdering(AbstractTimeStampPipe source, String name) {
		return verifyHeartbeatOrdering(new AbstractTimeStampPipe[]{source}, new int[]{0}, name);
	}

	@SuppressWarnings("unchecked")
	public static <I> AbstractTimeStampSink<I, TimeStampedObject<I>> verifyHeartbeatOrdering(AbstractTimeStampPipe source) {
		return verifyHeartbeatOrdering(source, "");
	}
	
	@SuppressWarnings("unchecked")
	public static <I> AbstractTimeStampSink<I, TimeStampedObject<I>> printHeartbeatEffect(AbstractTimeStampPipe[] sources, int[] sourceIDs, final String name, final Predicate pred) {
		final AbstractTimeStampPipe<I, I, TimeStampedObject<I>, TimeStampedObject<I>> pipe = new AbstractTimeStampPipe<I, I, TimeStampedObject<I>, TimeStampedObject<I>>(sources, sourceIDs) {
			StringBuffer out = new StringBuffer();
			@Override
			@SuppressWarnings("unchecked")
			public void heartbeat(long timeStamp, int sourceID) {
				if(!activateHeartbeats) {
					out.append(name+": Heartbeat <"+timeStamp+"> from "+sourceID+" is ignored because heartbeats are not activated\n");
					return;
				}
				out.append(name+": Heartbeat <"+timeStamp+"> from "+sourceID+" is processed ...");
				processingWLock.lock();
				try {					
					if (isOpened && ! isClosed) { // sinks may be null
						if (sourceID != MEMORY_MANAGER) { // heartbeat from source
							int index = Pipes.getSourceIndex(sourceIDs, sourceID);
							if (latestTimeStamps[index] < timeStamp) {
								out.append(" replaces local minmum "+latestTimeStamps[index]+" ...");
								latestTimeStamps[index] = timeStamp;
							}
							else
								out.append(" doesn't replaces local minmum "+latestTimeStamps[index]+" ...");
						}
						else { // heartbeat from memory manager
							out.append(" affects all local minmum: "); 
							for (int i = 0; i < latestTimeStamps.length; i++) {
								out.append(latestTimeStamps[i]+"-->");
								latestTimeStamps[i] = Math.max(latestTimeStamps[i], timeStamp);
								out.append(latestTimeStamps[i]+" ");
							}
						}	
						updateMinTimeStamp();
						out.append("lastHB:" +lastHB+" minTimeStamp:" +minTimeStamp+" ");
						if (lastHB < minTimeStamp) {							
							boolean inv = heartbeatPredicate.invoke();
							out.append(" predicate inv: "+inv);
							if (inv)
								{
									long heartbeat = processHeartbeat(minTimeStamp, sourceID); 
									if (heartbeat > 0) {
										out.append(" transfering hb: "+heartbeat);
										transferHeartbeat(heartbeat);
									}
									else
										out.append(" no hb tranfered: "+heartbeat);
									lastHB = heartbeat > 0 ? heartbeat : minTimeStamp;
								}
						}
					}
				}
				finally {
					processingWLock.unlock();
					System.out.println(out.toString());
					out.delete(0, out.length());
				}
			}

			@Override
			public void processObject(TimeStampedObject<I> o, int sourceID) throws IllegalArgumentException {
				transfer(o);
			}			
		};
		pipe.setHeartbeatPredicate(pred);
		return new AbstractTimeStampSink<I, TimeStampedObject<I>>(pipe) {
			@Override
			public void processObject(TimeStampedObject<I> o, int sourceID) throws IllegalArgumentException {}
			
			@Override
			public void activateHeartbeats(boolean on) {
				processingWLock.lock();
				try {
					this.activateHeartbeats = on;
					pipe.activateHeartbeats(on);
				}
				finally {
					processingWLock.unlock();	
				}
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static <I> AbstractTimeStampSink<I, TimeStampedObject<I>> printHeartbeatEffect(AbstractTimeStampPipe source, String name) {
		return printHeartbeatEffect(new AbstractTimeStampPipe[]{source}, new int[]{0}, name, Predicates.TRUE);
	}

	@SuppressWarnings("unchecked")
	public static <I> AbstractTimeStampSink<I, TimeStampedObject<I>> printHeartbeatEffect(AbstractTimeStampPipe source) {
		return printHeartbeatEffect(source, "");
	}
}
