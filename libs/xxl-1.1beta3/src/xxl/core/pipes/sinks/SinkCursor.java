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

package xxl.core.pipes.sinks;

import java.util.NoSuchElementException;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.cursors.Cursor;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 * Sink component in a query graph that provides an iteration
 * over the elements streaming in. A {@link Queue} is used to
 * buffer the elements coming in, which is implemented in XXL
 * as an iterator. Therefore a demand-driven processing is enabled. <BR>
 * This class realizes the design pattern <it>Adapter</it>.
 * <p>
 * <b>Intent:</b><br>
 * "Convert the interface of a class into another interface clients expect. 
 * Adapter lets classes work together that couldn't otherwise because of incompatible interfaces." <br>
 * For further information see: "Gamma et al.: <i>DesignPatterns. Elements
 * of Reusable Object-Oriented Software.</i> Addision Wesley 1998."
 * <p>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	Enumerator e = new Enumerator(10, 200);
 * 	SinkCursor cursor =  new SinkCursor(e, new ListQueue());
 * 	try {
 * 		while(cursor.hasNext()){
 * 			System.out.println(cursor.next());
 * 			Thread.currentThread().sleep(190);
 * 		}
 * 	}
 * 	catch (InterruptedException ex) {
 * 		System.err.println("ERROR : "+ex.getMessage());
 * 		ex.printStackTrace(System.err);
 * 	}
 * </code></pre>
 * @param <I> 
 * 
 * @see Cursor
 * @see Queue 
 * @since 1.1
 */
public class SinkCursor<I> extends AbstractSink<I> implements Cursor<I> {

	private boolean hasNext = false;
	private boolean computedHasNext = false;

	protected boolean isOpened = false;
	protected boolean isClosed = false;

	/**
	 * Buffers the elements streaming into this sink.
	 */
	protected Queue<I> buffer;
	
	/**
	 * The next element to be returned by the iteration.
	 */
	protected I next;

	/** 
	 * Creates a new terminal sink in a query graph.
	 *
	 * @param sources This sink gets subscribed to the specified sources.
	 * @param sourceIDs This sink uses the given IDs for subscription.
	 * @param buffer The queue buffering the elements streaming into this sink.
	 */ 
	public SinkCursor(Source<? extends I>[] sources, int[] sourceIDs, Queue<I> buffer) {
		super(sources, sourceIDs);
		this.buffer = buffer;
	}

	/** 
	 * Creates a new terminal sink in a query graph.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 * @param buffer The queue buffering the elements streaming into this sink.
	 */ 
	public SinkCursor(Source<? extends I> source, int ID, Queue<I> buffer) {
		this(new Source[]{source}, new int[]{ID}, buffer);
	}

	/** 
	 * Creates a new terminal sink in a query graph. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param buffer The queue buffering the elements streaming into this sink.
	 */ 
	public SinkCursor(Source<? extends I> source, Queue<I> buffer) {
		this(source, DEFAULT_ID, buffer);
	}

	/**
	 * The given element is inserted into the 
	 * buffer. The thread iterating over this buffer is notified, i.e.,
	 * <CODE>notify</CODE> is called on this cursor in order to wake up a 
	 * thread that is waiting on this cursor's monitor. 
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this sink specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		synchronized(buffer) {
			buffer.enqueue(o);
		}
		synchronized(this) {
			this.notify();
		}
	}

	/**
	 * Calls <CODE>super.done(ID)</CODE> and notifies
	 * the thread, that is waiting on this cursor monitor 
	 * in order to terminate its execution.
	 *
	 * @param ID The ID this sink specified during its subscription by an underlying source. 
	 * @see AbstractSink#done(int)
	 */
	@Override
	public void done(int sourceID) {
		super.done(sourceID);
		synchronized(this) {
			this.notify();
		}
	}

	/**
	 * Returns <tt>true</tt> if the iteration has more elements.
	 * (In other words, returns <tt>true</tt> if <tt>next</tt> or <tt>peek</tt> would
	 * return an element rather than throwing an exception.) <br>
	 * This operation is implemented idempotent, i.e., consequent calls to
	 * <code>hasNext()</code> do not have any effect. <BR>
	 * Locks, if the buffer contains an element. Otherwise, the thread
	 * this cursor belongs to has to wait until new elements are
	 * buffered. Except the sink received a <CODE>done</CODE> call, then
	 * the buffer is closed until all its elements have been processed. 
	 *
	 * @return <tt>true</tt> if the cursor has more elements.
	 */
	public boolean hasNext () {
		if (!isOpened) open();
		if (isClosed) throw new IllegalStateException();	
		if (computedHasNext) return hasNext;
		next = null;
		boolean hasNext;
		synchronized(buffer) {
			hasNext = !buffer.isEmpty();
			if (hasNext)
				next = buffer.dequeue();
			else
				if (isDone && buffer.size() == 0) {
					buffer.close();
					return this.hasNext = computedHasNext = false;
				}
		}
		if (hasNext)
			return this.hasNext = computedHasNext = true;
		synchronized(this) {
			try {
				this.wait(0);
			}
			catch (InterruptedException e) {
				System.err.println("ERROR : "+e.getMessage());
				e.printStackTrace(System.err);
			}
		}
		return hasNext();
	}

	/**
	 * Returns the next element in the iteration.
	 * This element will be removed from the buffer, if
	 * <tt>next</tt> is called.
	 *
	 * @return the next element in the iteration.
	 * @throws java.util.NoSuchElementException if the iteration has no more elements.
	 */
	public I next () throws NoSuchElementException {
		if (!computedHasNext) hasNext();
		if (!hasNext) throw new NoSuchElementException();
		hasNext = computedHasNext = false;
		return next;
	}
	
	/**
	 * Shows the next element in the iteration without removing it from the buffer.
	 *
	 * @return the next element in the iteration.
	 * @throws java.util.NoSuchElementException iteration has no more elements.
	 * @throws java.lang.UnsupportedOperationException if the <tt>peek</tt> operation is
	 * 		not supported by this cursor.
	 */
	public I peek () throws NoSuchElementException, UnsupportedOperationException {
		if (!computedHasNext) hasNext();
		if (!hasNext) throw new NoSuchElementException();
		return next;
	}

	/**
	 * This method is not supported.
	 *
	 * @throws java.lang.UnsupportedOperationException if the <tt>remove</tt> method is
	 * 		not supported by this cursor.
	 */
	public void remove () throws IllegalStateException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is not supported.
	 *
	 * @throws java.lang.UnsupportedOperationException if the <tt>reset</tt> method is
	 * 		not supported by this cursor.
	 */
	public void reset () throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Always returns <tt>true</tt>, because this cursor supports the
	 * <tt>peek</tt> operation.
	 *
	 * @return <tt>true</tt>
	 */
	public boolean supportsPeek () {
		return true;
	}

	/**
	 * Always returns false as <tt>remove</tt> is not supported.
	 * 
	 * @return <tt>false</tt>
	 */
	public boolean supportsRemove() {
		return false;
	}
	
	/**
	 * Always returns false as <tt>update</tt> is not supported.
	 * 
	 * @return <tt>false</tt>
	 */
	public boolean supportsUpdate() {
		return false;
	}

	/**
	 * Always returns false as <tt>reset</tt> is not supported.
	 * 
	 * @return <tt>false</tt>
	 */
	public boolean supportsReset() {
		return false;
	}

	/**
	 * This method is not supported.
	 *
	 * @param object The element that replaces the element returned
	 * 		by the last call to <tt>next</tt> or <tt>peek</tt>.
	 * @throws java.lang.IllegalStateException If there is no element which can be updated.
	 * @throws java.lang.UnsupportedOperationException If the <tt>update</tt>
	 * 		operation is not supported by this cursor.
	 */
	public void update (I object) throws IllegalStateException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see xxl.core.cursors.Cursor#open()
	 */
	public void open() {
		if(isOpened) return;
		isOpened = true;
		synchronized(buffer) {
			buffer.open();
		}
		this.openAllSources();
	}

	/**
	 * Stops the processing of this sink, i.e., the method
	 * <CODE>stop</CODE> is called.
	 */
	public void close () {
		if (isClosed) return;
		isClosed = true;
		hasNext = computedHasNext = false;
		synchronized(buffer) {
			buffer.close();
		}
		this.closeAllSources();
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
		
		Enumerator e = new Enumerator(100, 50);
		SinkCursor<Integer> cursor =  new SinkCursor<Integer>(e, new ListQueue<Integer>());
		cursor.open();
		try {
			while(cursor.hasNext()){
				System.out.println(cursor.next());
				Thread.sleep(40);
			}
		}
		catch (InterruptedException ex) {
			System.err.println("ERROR : "+ex.getMessage());
			ex.printStackTrace(System.err);
		}
		cursor.close();
	}

}
