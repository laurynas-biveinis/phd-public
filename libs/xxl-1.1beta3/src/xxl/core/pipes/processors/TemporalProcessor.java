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

package xxl.core.pipes.processors;

import java.util.BitSet;
import java.util.NoSuchElementException;

import xxl.core.collections.MapEntry;
import xxl.core.collections.queues.Heap;
import xxl.core.comparators.FeatureComparator;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.sources.AbstractSource;

/**
 * A thread that controls the registered data sources according to their temporal 
 * ordering (application time).
 *
 * @param <E>
 */
public class TemporalProcessor<E extends TimeStampedObject> extends SourceProcessor {
	
	protected Heap<MapEntry<E,Integer>> heap;		
	protected long physicalDelay;
	protected final long logicalTimeSteps;
	protected long currentLogicalTime;
	protected boolean initPhase = true;
	protected BitSet buffered;
		
	public TemporalProcessor(long physicalDelay, long logicalTimeSteps) {
		super(physicalDelay);
		this.logicalTimeSteps = logicalTimeSteps;
	}

	public TemporalProcessor() {
		this(0, 1);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void process() {
		fillHeap();
		if (currentLogicalTime == Long.MIN_VALUE)
			currentLogicalTime = heap.peek().getKey().getTimeStamp();
		transfer();
		checkTermination();
		currentLogicalTime += logicalTimeSteps;
	}
	
	@SuppressWarnings("unchecked")
	protected void fillHeap() {
		AbstractSource source;
		for (int i = 0; i < sources.size(); i++) {
			source = sources.get(i);
			if (source.isClosed())
				closedOrDone.set(i);
			if (!closedOrDone.get(i) && !buffered.get(i)) {
				try {
					MapEntry<E,Integer> e = new MapEntry<E,Integer>((E)source.next(), i);
					heap.enqueue(e);
					buffered.flip(i);
				}
				catch (NoSuchElementException nsee) {
					source.signalDone();
					closedOrDone.set(i);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void transfer() {
		while (!heap.isEmpty() && heap.peek().getKey().getTimeStamp() < currentLogicalTime) {
			MapEntry<E,Integer> top = heap.dequeue();
			int i = top.getValue();
			sources.get(i).transfer(top.getKey());
			buffered.clear(i);
		}
	}
	
	@Override
	protected void onStart() {
		initPhase = false;
		heap = new Heap<MapEntry<E,Integer>>(sources.size(), 
			new FeatureComparator<E, MapEntry<E,Integer>>(TimeStampedObject.START_TIMESTAMP_COMPARATOR,
				new Function<MapEntry<E,Integer>, E>() {
					@Override
					public E invoke(MapEntry<E,Integer> entry) {
						return entry.getKey();
					}
				}
			)
		);
		buffered = new BitSet(sources.size());
		currentLogicalTime = Long.MIN_VALUE;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void registerSource(AbstractSource source) {
		super.registerSource(source);
		if (!initPhase) {
			throw new RuntimeException("Cannot add new source after starting.");
		}
	}
	
}
