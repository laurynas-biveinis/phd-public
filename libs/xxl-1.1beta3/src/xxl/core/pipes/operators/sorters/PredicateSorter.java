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

package xxl.core.pipes.operators.sorters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import xxl.core.collections.queues.Heap;
import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.wrappers.QueueCursor;
import xxl.core.functions.Function;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.predicates.Predicate;

/**
 * Operator component in a query graph that sorts a
 * buffer (queue) each time the user-defined predicate
 * <CODE>overflows</CODE> is fulfilled. <BR>
 * The sorting is performed in a user-defined unary
 * function, so that this class abstracts from the sorting
 * algorithm that is used, e.g. quick-sort, merge-sort or
 * heap-sort can be realized.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	PredicateSorter sorter = new PredicateSorter(
 * 		new RandomNumber(RandomNumber.DISCRETE, 10000, 0),
 * 		new ListQueue(new ArrayList(100)),
 * 		new Predicate() {
 * 			// buffer is sorted and flushed every 100 elements
 * 			public boolean invoke(Object buffer, Object o) { 
 * 				return ((Queue)buffer).size() < 100 ? false : true;
 * 			}
 * 		},
 * 		new Function() {
 * 			public Object invoke(Object buffer) {
 * 				Queue queue = (Queue)buffer;
 * 				Object[] objects = new Object[queue.size()];
 * 				int i = 0;
 * 				while(queue.hasNext())
 * 					objects[i++] = queue.next();
 * 				Arrays.sort(objects); // MergeSort SDK
 * 				return new ArrayCursor(objects);
 * 			}
 * 		},
 * 		true, true
 * 	);
 * 	new VisualSink(sorter, "Sorter", true);
 * </code></pre> 
 *
 * @see Function
 * @see Predicate
 * @see Queue
 * @see Heap
 * @since 1.1
 */
public class PredicateSorter<I> extends AbstractPipe<I,I> {

	/**
	 * Unary function used for sorting. <BR>
	 * f: Queue --> Iterator
	 */
	protected Function<? super Queue<I>,? extends Iterator<I>> sortFunction;
	
	/**
	 * Binary predicate that determines,
	 * when an element of the buffer is transferred
	 * to all of subscribed sinks.
	 */
	protected Predicate<Object> overflows;
	
	/**
	 * The queue used for buffering.
	 */
	protected Queue<I> buffer;
	
	public PredicateSorter(Queue<I> buffer, Predicate<Object> overflows, Function<? super Queue<I>,? extends Iterator<I>> sortFunction) {
		this.sortFunction = sortFunction;
		this.overflows = overflows;
		this.buffer = buffer;
	}
	
	/** 
	 * Creates a new PredicateSorter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 * @param buffer The queue buffering the incoming elements.
	 * @param overflows Binary predicate that determines,
	 * 		when the buffer is to be sorted and flushed.
	 * @param sortFunction Unary function used for sorting. f: Queue --> Iterator.
	 */ 
	public PredicateSorter(Source<? extends I> source, int sourceID, Queue<I> buffer, Predicate<Object> overflows, Function<? super Queue<I>,? extends Iterator<I>> sortFunction) {
		this(buffer, overflows, sortFunction);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	/** 
	 * Creates a new PredicateSorter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. The specified
	 * heap is used as buffer, which organizes the incoming elments.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param heap The heap buffering and organizing the incoming elements.
	 * @param overflows Binary predicate that determines,
	 * 		when the buffer is to be flushed.
	 */ 
	public PredicateSorter(Source<? extends I> source, int sourceID, Heap<I> heap, Predicate<Object> overflows) {
		this(source, sourceID, heap, overflows,
			new Function<Queue<I>, Iterator<I>>() {
				@Override
				public Iterator<I> invoke(Queue<I> buffer) {
					return new QueueCursor<I>(buffer);
				}
			}
		);
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#open()
	 */
	@Override
	public void open() throws SourceIsClosedException {
		super.open();
		buffer.open();
	}
	
	/** 
	 * Updates the input rate. If the predicate <CODE>overflows</CODE>
	 * is fulfilled, the sort-function is applied to the buffer
	 * and each element of the resulting iterator is transferred to
	 * all of this pipe's subscribed sinks. After that the
	 * buffer is cleared and the given element is inserted.
	 * Otherwise the element is inserted directly.
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		if (overflows.invoke(buffer, o)) {
			Iterator<I> it = sortFunction.invoke(buffer);
			while(it.hasNext()) {
				I next = it.next();
				transfer(next);
			}
			buffer.clear();
		}
		buffer.enqueue(o);
	}

	/**
	 * Calls <CODE>super.close()</CODE> and 
	 * closes the queue used for buffering.
	 */
	@Override
	public void close () {
		super.close();
		if (isClosed())
			buffer.close();
	}

	/**
	 * Applies the sort-function to the buffer and 
	 * transfers all elements of the resulting iterator
	 * to the subscribed sinks of this pipe. After that
	 * this method calls <CODE>super.done(ID)</CODE>,
	 * which closes the buffer and forwards the 
	 * <CODE>done</CODE> call.
	 * 
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 */
	@Override
	public void done(int sourceID) {
		if (isClosed || isDone) return;
		
		updateDoneStatus(sourceID);
		processingWLock.lock();
	    try {
			if (isDone()) {
				Iterator<I> it = sortFunction.invoke(buffer);
				while(!isClosed() && it.hasNext())
					transfer(it.next());
				signalDone();
			}
	    }
	    finally {
	    	processingWLock.unlock();
	    }
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
		PredicateSorter<Integer> sorter = new PredicateSorter<Integer>(
			new RandomNumber<Integer>(RandomNumber.DISCRETE, 10000, 0), DEFAULT_ID,
			new ListQueue<Integer>(new ArrayList<Integer>(100)),
			new Predicate<Object>() {
				@Override
				public boolean invoke(Object buffer, Object o) {
					return ((Queue<Integer>)buffer).size() < 100 ? false : true;
				}
			},
			new Function<Queue<Integer>,Iterator<Integer>>() {
				@Override
				public Iterator<Integer> invoke(Queue<Integer> buffer) {
					Queue<Integer> queue = buffer;
					Integer[] objects = new Integer[queue.size()];
					int i = 0;
					while(!queue.isEmpty())
						objects[i++] = queue.dequeue();
					Arrays.sort(objects);
					return new ArrayCursor<Integer>(objects);
				}
			}
		);
		new VisualSink<Integer>(sorter, "Sorter", true);
	}

}
