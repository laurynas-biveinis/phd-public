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

import java.util.Comparator;

import xxl.core.collections.queues.Heap;
import xxl.core.comparators.ComparableComparator;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;

/**
 * Operator component in a query graph that produces sorted
 * runs of the input stream according to the Replacement-Selection
 * algorithm. For further information see "[Knu73]: Knuth Donald E.:
 * The Art of Computer Programming, Vol. III, Sorting and Searching,
 * Addison-Wesley, 1973". 
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	HeapSorter sorter = new HeapSorter(
 * 		new RandomNumber(RandomNumber.DISCRETE, 100000, 0), DEFAULT_ID, 
 * 		1000, ComparableComparator.DEFAULT_INSTANCE, // max. heap size is 1000 elements 
 * 		true, true
 * 	);
 * 	new VisualSink(sorter, true);
 * </code></pre>
 * 
 * @see Heap
 * @see java.util.Comparator
 * @see ComparableComparator
 * @since 1.1
 */
public class HeapSorter<I> extends AbstractPipe<I,I> {
	
	/**
	 * The heap-implementation uses this array.
	 */
	protected I[] array;
	
	/**
	 * Current heap size, i.e. the position in the 
	 * array.
	 */
	protected int n = 0;
	
	/**
	 * Flag that signals, if a new heap is built up.
	 */
	protected boolean creatingNewHeap = false;
	
	/**
	 * Compares two elements. This comparator
	 * is used to organize the heap.
	 */
	protected Comparator<I> comparator;
	
	/**
	 * The heap used for sorting.
	 */
	protected Heap<I> heap;
	
	public HeapSorter(int size, Comparator<I> comparator) {
		this.heap = new Heap<I>(size, comparator);
		this.array = (I[])new Object[size];
		this.n = array.length;
		this.comparator = comparator;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#open()
	 */
	@Override
	public void open() throws SourceIsClosedException {
		super.open();
		heap.open();
	}
	
	/** 
	 * Creates a new HeapSorter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param size The maximum number of elements that can be stored in the heap.
	 * @param comparator The comparator that is used to organize the heap.
	 */ 
	public HeapSorter(Source<? extends I> source, int sourceID,  int size, Comparator<I> comparator) {
		this(size, comparator);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
		
	/** 
	 * Updates the input counter. Realizes the optimized version of the Replacement-Selection
	 * algorithm, that even builds up a new heap based on the same underlying object array. <BR>
	 * If the heap is not full, the incoming element is inserted directly. Otherwise,
	 * the heap's top element is compared with the incoming element. If the heap is a max-heap,
	 * its top element is transferred, if the incoming element is smaller. The top element
	 * is removed from the heap and given element is inserted.
	 * If the heap's top element is smaller than the given element, the heap flushed and
	 * a new heap is built up.
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		if (!creatingNewHeap && heap.size() >= array.length)
			creatingNewHeap = true;
		if (creatingNewHeap) {
			if (heap.isEmpty()) {
				heap = new Heap<I>(array, array.length-n, comparator);
				n = array.length;
			}
			transfer(heap.dequeue());
			array[--n] = o;
			return;
		}
		heap.enqueue(o);
	}
	
	/**
	 * Flushes the buffer, i.e., all elements
	 * of the heap are transferred. After that
	 * this method calls <CODE>super.done(ID)</CODE>
	 * and closes the heap.
	 * 
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 */
	@Override
	public void done(int sourceID) {
		if (isClosed || isDone) return;
		
		updateDoneStatus(sourceID);
		processingWLock.lock();
	    try {
	    	if (!isClosed && isDone()) {
				if (creatingNewHeap) {
					while(!heap.isEmpty())
						transfer(heap.dequeue());
					System.arraycopy(array, Math.max(n, array.length-n), array, 0, Math.min(n, array.length-n));
					heap = new Heap<I>(array, array.length-n, comparator);
					creatingNewHeap = false;
				}
				while(!heap.isEmpty())
					transfer(heap.dequeue());
				signalDone();
			}
		 }
	    finally {
	    	processingWLock.unlock();
	    }
	}
	
	/**
	 * Calls <CODE>super.close()</CODE> and 
	 * sets the internal array to <CODE>null</CODE>.
	 */
	@Override
	public void close () {
		super.close();
		if (isClosed()) {
		    heap.close();
			array = null;
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
		HeapSorter<Integer> sorter = new HeapSorter<Integer>(
			new RandomNumber<Integer>(RandomNumber.DISCRETE, 10000, 2), DEFAULT_ID, 
			100, ComparableComparator.INTEGER_COMPARATOR
		);
		new VisualSink<Integer>(sorter, true);
	}	
	
}
