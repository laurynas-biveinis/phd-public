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

package xxl.core.pipes.operators;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import xxl.core.collections.MapEntry;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sinks.SinkIsDoneException;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.predicates.Predicate;

/**
 * Provides some static methods that may be useful.
 * 
 * @since 1.1
 */
public abstract class Pipes {
	
	// Let no one instantiate this class
	private Pipes() {}
	
	/**
	 * Returns the index for the given sink, i.e., its position in the internal data 
	 * structure of the specified source.
	 * 
	 * @param source The source the specified sink is subscribed to.
	 * @param sink The sink, whose index is to be determined.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 * @throws java.lang.IllegalArgumentException If the specified sink is not subscribed to the given source.
	 */
	public static <I,O> int getSinkIndex(Source<? extends I> source, Sink<? super O> sink) throws SourceIsClosedException, IllegalArgumentException {
		int noOfSinks = source.getNoOfSinks();
		for (int i = 0; i < noOfSinks; i++)
			if (source.getSink(i) == sink)
				return i;
		throw new IllegalArgumentException("Sink is not subscribed to the given source.");
	}
	
	/**
	 * Returns the index for the given source, i.e., its position in the internal data 
	 * structure of the specified sink.
	 * 
	 * @param sink The sink that is subscribed to the specified source.
	 * @param source The source, whose index is to be determined.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 * @throws java.lang.IllegalArgumentException If the specified source is not registered by the given sink.
	 */
	public static <I,O> int getSourceIndex(Sink<? super O> sink, Source<? extends I> source) throws SinkIsDoneException, IllegalArgumentException {
		int noOfSources = sink.getNoOfSources();
		for (int i = 0; i < noOfSources; i++)
			if (sink.getSource(i) == source)
				return i;
		throw new IllegalArgumentException("Source is not known by the given sink.");
	}

	public static int getSinkIndex(int[] sinkIDs, int sinkID) throws SinkIsDoneException, IllegalArgumentException {
		for (int i = 0; i < sinkIDs.length; i++) {
			if (sinkIDs[i] == sinkID)
				return i;
		}
		throw new IllegalArgumentException("No source found for the specified ID."); 
	}
	
	public static int getSourceIndex(int[] sourceIDs, int sourceID) throws SinkIsDoneException, IllegalArgumentException {
		for (int i = 0; i < sourceIDs.length; i++) {
			if (sourceIDs[i] == sourceID)
				return i;
		}
		throw new IllegalArgumentException("No source found for the specified ID."); 
	}

	/**
	 * Prints subscription information for the specified pipe, i.e.,
	 * the number of its sources and sinks, invokes <CODE>System.out.println</CODE>
	 * on the given pipe, its sources and sinks. 
	 *
	 * @param pipe The pipe, whose subscription information shall be printed.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	public static void printPipeInformation(Pipe<?,?> pipe) throws SourceIsClosedException, SinkIsDoneException {
		int noIn, noOut;
		System.out.println("Pipe: "+pipe);
		System.out.println("No. of sources:  "+(noIn = pipe.getNoOfSources()));
		System.out.println("Sources: ");
		for (int i = 0; i < noIn; i++)
			System.out.println("\t"+pipe.getSource(i));
		System.out.println("No. of sinks: "+(noOut = pipe.getNoOfSinks()));
		System.out.println("Sinks: ");
		for (int i = 0; i < noOut; i++)
			System.out.println("\t"+pipe.getSink(i));
		System.out.println();
	}

	/**
	 * Prints subscription information for the specified source, i.e.,
	 * the number of its sinks, invokes <CODE>System.out.println</CODE>
	 * on the given source and its sinks. 
	 *
	 * @param source The source, whose subscription information shall be printed.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public static void printSourceInformation(Source<?> source) throws SourceIsClosedException {
		int noOut;
		System.out.println("Source: "+source);
		System.out.println("No. of sinks: "+(noOut = source.getNoOfSinks()));
		System.out.println("Sinks: ");
		for (int i = 0; i < noOut; i++)
			System.out.println("\t"+source.getSink(i));
		System.out.println();
	}

	/**
	 * Prints subscription information for the specified sink, i.e.,
	 * the number of its sources, invokes <CODE>System.out.println</CODE>
	 * on the given sink and its sources. 
	 *
	 * @param sink The sink, whose subscription information shall be printed.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	public static void printSinkInformation(Sink<?> sink) throws SinkIsDoneException {
		int noIn;
		System.out.println("Sink: "+sink);
		System.out.println("No. of sources:  "+(noIn = sink.getNoOfSources()));
		System.out.println("Sources: ");
		for (int i = 0; i < noIn; i++)
			System.out.println("\t"+sink.getSource(i));
		System.out.println();
	}
	
	/**
	 * Establishes a connection between the specified source and the
	 * sink using the ID for subscription.
	 * 
	 * @param source The source the sink gets subscribed to.
	 * @param sink The sink to be subscribed.
	 * @param ID The ID which will be passed when transfering a new object to the specified sink.
	 * @return Returns <CODE>true</CODE>, if the connection has been established successfully, otherwise <CODE>false</CODE>.
	 * @throws SourceIsClosedException If the source has already been closed.
	 * @throws SinkIsDoneException If the sink has already finished its processing.
	 */
	public static <O> boolean connect(Source<? extends O> source, Sink<? super O> sink, int ID) throws SourceIsClosedException, SinkIsDoneException {
		return sink.addSource(source, ID) && source.subscribe(sink, ID);		
	}
	
	/**
	 * Removes a connection between the specified source and the
	 * sink using the given ID.
	 * 
	 * @param source The source the sink is subscribed to.
	 * @param sink The sink that is subscribed.
	 * @param ID The ID passed by transfering a new object to the specified sink.
	 * @return Returns <CODE>true</CODE>, if the connection has been removed successfully, otherwise <CODE>false</CODE>.
	 * @throws SourceIsClosedException If the source has already been closed.
	 * @throws SinkIsDoneException If the sink has already finished its processing.
	 */
	public static <O> boolean disconnect(Source<? extends O> source, Sink<? super O> sink, int ID) throws SourceIsClosedException, SinkIsDoneException {
		return source.unsubscribe(sink, ID) && sink.removeSource(source, ID);
	}
	
	/**
	 * Traverses the graph in reverse direction starting at a given sink. 
	 * The traversal will be stopped at those Sources that fulfill 
	 * <code>stopPredicate</code>. All sources fulfilling <code>returnPredicate</code>
	 * that are visited during the traversal are returned.
	 * 
	 * @param sink the sink to start from
	 * @param returnPredicate the Predicate the returned sources must fulfill
	 * @param stopPredicate the Predicate indicating where the traversal should stop
	 *
	 * @return all visited sources which fulfill returnPredicate
	 */
	public static Source[] getCertainPredecessors(Sink<?> sink, Predicate<Source> returnPredicate, Predicate<Source> stopPredicate) {
		LinkedList<Source> queue = new LinkedList<Source>();
		LinkedList<Source> result = new LinkedList<Source>();
		HashSet<Source> done = new HashSet<Source>();
		for (int i=0; i<sink.getNoOfSources(); i++) queue.addLast(sink.getSource(i));
	
		while (!queue.isEmpty()) {
			Source o = queue.removeFirst();
		
			if (done.contains(o)) continue;
			done.add(o);
		
			if (returnPredicate.invoke(o)) result.add(o);
		
			if (!stopPredicate.invoke(o) && (o instanceof Sink)) {
				Sink<?> s = (Sink) o;
				for (int i=0; i<s.getNoOfSources(); i++) {
					Source src = s.getSource(i);
					if (!done.contains(src)) queue.addLast(src);
				}
			}
		}
	
		Source[] array = new Source[result.size()];
		return result.toArray(array);
	}
		
	/**
	 * Traverses the graph starting at a given Source. 
	 * The traversal will be stopped at those Sinks that fulfill 
	 * <code>stopPredicate</code>. All sources fulfilling <code>returnPredicate</code>
	 * that are visited during the traversal are returned.
	 * 
	 * @param source the source to start from
	 * @param returnPredicate the Predicate the returned sinks must fulfill
	 * @param stopPredicate the Predicate indicatoing where the traversal should stop
	 *
	 * @return all visited sinks which fulfill returnPredicate
	 */
	public static Sink[] getCertainSuccessors(Source<?> source, Predicate<Sink> returnPredicate, Predicate<Sink> stopPredicate) {
		LinkedList<Sink> queue = new LinkedList<Sink>();
		LinkedList<Sink> result = new LinkedList<Sink>();
		HashSet<Sink> done = new HashSet<Sink>();
		for (int i=0; i<source.getNoOfSinks(); i++) queue.addLast(source.getSink(i));
	
		while (!queue.isEmpty()) {
			Sink o = queue.removeFirst();
		
			if (done.contains(o)) continue;
			done.add(o);
		
			if (returnPredicate.invoke(o)) result.add(o);
		
			if (!stopPredicate.invoke(o) && (o instanceof Source)) {
				Source<?> s = (Source) o;
				for (int i=0; i<s.getNoOfSinks(); i++) {
					Sink sink = s.getSink(i);
					if (!done.contains(sink)) queue.addLast(sink);
				}
			}
		}
	
		Sink[] array = new Sink[result.size()];
		return result.toArray(array);
	}

	public static <E extends TimeStampedObject> Sink<E> verifyStartTimeStampOrdering(Source<E> input) {
		return verifyAscOrdering(input, TimeStampedObject.START_TIMESTAMP_COMPARATOR);
	}
	
	public static <E extends TemporalObject> Sink<E> verifyLexicographicalOrdering(Source<E> input) {
		return verifyAscOrdering(input, TemporalObject.TIME_INTERVAL_COMPARATOR);
	}
	
	public static <I> Sink<I> verifyAscOrdering(Source<I> input, final Comparator<? super I> comp) {
		return new AbstractSink<I>(input) {
			protected I last = null;
			
			@Override
			public void processObject(I o, int sourceID) throws IllegalArgumentException {
				if (last != null && comp.compare(o, last) < 0) {
					System.err.println("Error: elements are not sorted ascendingly according to the given ordering.");	
					System.err.println("predecessor: "+last+"\tcurrent object: "+o);
				}
				last = o;
			}
		};
	}
	
	
	public static Source<TemporalObject<Integer>> decorateWithRandomTimeIntervals(Source<Integer> source, final HashMap<Integer,List<Long>> input, final Integer[] keys, final int startInc, final int intervalSize, final long seed) {
		return new Mapper<Integer,TemporalObject<Integer>>(source,
			new Function<Integer,TemporalObject<Integer>>() {
				Random random = new Random(seed);
				long start, end;
				long newStart, newEnd;
				@Override
				public TemporalObject<Integer> invoke(Integer o) {
					int key;
					newStart = start+random.nextInt(startInc);
					newEnd   = newStart+1+random.nextInt(intervalSize-1);
					if (start == newStart) newEnd = end+1+random.nextInt(intervalSize-1);
					TemporalObject<Integer> object = new TemporalObject<Integer>(key = o%keys.length, 
						new TimeInterval(start = newStart, end = newEnd)
					);
					List<Long> l = input.get(keys[key]);
					if (l == null) l = new LinkedList<Long>();
					l.addAll(TemporalObject.getAllSnapshots(object));					
					input.put(keys[key], l);
					//System.out.println("==> input: "+object);
					return object;
				}
			}
		);
	}
	
	public static final Source<TemporalObject<Integer>> decorateWithRandomIntervals(Source<Integer> source, final int startInc, final int intervalSize, final long seed) {
		return new Mapper<Integer,TemporalObject<Integer>>(source,
				new Function<Integer,TemporalObject<Integer>>() {
					Random random = new Random(seed);
					long start;
					long newStart, newEnd;
					@Override
					public TemporalObject<Integer> invoke(Integer o) {
						newStart = start+random.nextInt(startInc);
						newEnd   = newStart+1+random.nextInt(intervalSize-1);
						TemporalObject<Integer> object = new TemporalObject<Integer>(o, 
							new TimeInterval(start = newStart, newEnd)
						);
						return object;
					}
			}
		);
	}
	
	/**
	 * In contrast to <code>decorateWithRandomTimeIntervals</code>, objects from this source
	 * will all have the same time interval size.
	 * 
	 * @param source
	 * @param input
	 * @param keys
	 * @param startInc
	 * @param intervalSize interval length for objects
	 * @param seed
	 * @return Source whose objects all have the same time interval
	 */
	public static Source<TemporalObject<Integer>> decorateWithRandomTimeFixedWindowsize(Source<Integer> source, final HashMap<Integer,List<Long>> input, final Integer[] keys, final int startInc, final int intervalSize, final long seed) {
		return new Mapper<Integer,TemporalObject<Integer>>(source,
			new Function<Integer,TemporalObject<Integer>>() {
				Random random = new Random(seed);
				long start;
				long newStart, newEnd;
				@Override
				public TemporalObject<Integer> invoke(Integer o) {
					int key;
					newStart = start+random.nextInt(startInc);
					newEnd   = newStart+1+intervalSize;
					TemporalObject<Integer> object = new TemporalObject<Integer>(key = o%keys.length, 
						new TimeInterval(start = newStart, newEnd)
					);
					List<Long> l = input.get(keys[key]);
					if (l == null) l = new LinkedList<Long>();
					l.addAll(TemporalObject.getAllSnapshots(object));					
					input.put(keys[key], l);
					//System.out.println("==> input: "+object);
					return object;
				}
			}
		);
	}
	
	public static Source<TemporalObject<Integer>> decorateWithSystemTimeFixedWindowsize(Source<Integer> source, final HashMap<Integer,List<Long>> input, final Integer[] keys, final int intervalSize) {
		return new Mapper<Integer,TemporalObject<Integer>>(source,
			new Function<Integer,TemporalObject<Integer>>() {				
				@Override
				public TemporalObject<Integer> invoke(Integer o) {
					int key;
					long start = System.currentTimeMillis();
					TemporalObject<Integer> object = new TemporalObject<Integer>(key = o%keys.length, 
						new TimeInterval(start , start + intervalSize)
					);
					List<Long> l = input.get(keys[key]);
					if (l == null) l = new LinkedList<Long>();
					l.addAll(TemporalObject.getAllSnapshots(object));					
					input.put(keys[key], l);
					return object;
				}
			}
		);
	}

	public static HashMap<Integer,List<MapEntry<Integer,Long>>> accumulateMultiplicites(HashMap<Integer,List<Long>> input, Integer[] keys) {
		HashMap<Integer,List<MapEntry<Integer,Long>>> multiplicities = new HashMap<Integer,List<MapEntry<Integer,Long>>>();
		for (int i = 0; i < keys.length; i++) {
			List<Long> inputList = input.get(keys[i]);
			Collections.sort(inputList);
			Iterator<Long> it = inputList.iterator();
			List<MapEntry<Integer,Long>> outputList = new LinkedList<MapEntry<Integer,Long>>();
			int counter = 1;
			Long previous = null;
			for (Long next = null; it.hasNext(); previous = next) {
				next = it.next();
				if (previous != null) {
					if (next.equals(previous)) {
						counter++;
					}
					else {
						outputList.add(new MapEntry<Integer,Long>(counter, previous)); // {multiplicity, snapshot}
						counter = 1;
					}
				}
			}
			if (previous != null)
				outputList.add(new MapEntry<Integer,Long>(counter, previous));
			multiplicities.put(keys[i], outputList);
		}
		return multiplicities;
	}
	
}
