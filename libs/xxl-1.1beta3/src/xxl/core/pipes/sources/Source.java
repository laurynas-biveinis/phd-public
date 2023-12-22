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

package xxl.core.pipes.sources;

import xxl.core.pipes.queryGraph.Node;
import xxl.core.pipes.sinks.Sink;

/**
 * Our framework for query processing is based on dataflow graphs, therefore
 * it consists of three fundamental types of components: sources, operators, and sinks.
 * Sources represent the initial components of a query graph, whereas sinks
 * act as the terminal components. Operators can only appear 'inside'
 * a query graph. <P> 
 * This interface models an arbitrary active source in a query graph. A source can be accessed with a publish-/subscribe mechanism.
 * Sinks (interface {@link Sink}) can be subscribed to a source.
 * If a source is opened, it continuously delivers its elements to all subscribers until
 * it gets closed. <BR>
 * An operator (interface {@link xxl.core.pipes.operators.Pipe}) within a query graph extends the interfaces {@link Source}
 * and {@link Sink}, because on the hand, it receives elements from its underlying
 * sources and on the other hand, it transfers its results to its subscribed sinks.
 * 
 * @since 1.1
 */
public abstract interface Source<O> extends Node {

	/**
	 * Opens the source and starts processing. This method should be implemented
	 * in an idempotent manner.
	 *
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public abstract void open() throws SourceIsClosedException;

	/**
	 * Closes this source. All resources allocated by this source should be released.
	 */
	public abstract void close();

	/**
	 * Transfers the given element to all subscribed sinks.
	 *
	 * @param o The element to be transferred.
	 */
	public abstract void transfer(O o);

	/**
	 * Subscribes the specified sink with the given ID to this source.
	 * The ID is necessary to distinguish different sources when using operators
	 * based on multiple inputs. <BR>
	 * The sink and its corresponding ID have to be stored in an internal datastructure by this source 
	 * and each sink can be accessed by an index.
	 *
	 * @param sink The sink to be subscribed.
	 * @param sinkID The ID which will be passed when transfering a new object to the specified sink.
	 * @return Returns <CODE>true</CODE>, if the subscribe operation has been successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public abstract boolean subscribe(Sink<? super O> sink, int sinkID) throws SourceIsClosedException;

	/**
	 * The given sink cancels its subscription. The registered sink with the given ID will not be informed about
	 * new elements any longer. If a sink is subscribed plurally, i.e. the same sink is subscribed with different IDs, only the subscription
	 * concerning the specified ID will be removed.
	 *
	 * @param sink The sink whose subscription is to be cancelled.
	 * @param sinkID The ID of the sink whose subscription is to be cancelled.
	 * @return Returns <CODE>true</CODE>, if the unsubscribe operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public abstract boolean unsubscribe(Sink<? super O> sink, int sinkID) throws SourceIsClosedException;

	/**
	 * Returns a reference to a Sink belonging to the specified index.
	 * A sink was associated with this index when it called the <CODE>subscribe</CODE> method.
	 *
	 * @param index The index used to access subscribed sinks within this source.
	 * @return The sink belonging to the specified index.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 * @throws java.lang.IllegalArgumentException If no sinks have been subscribed.
	 * @throws java.lang.IndexOutOfBoundsException If the specified index does not exist.
	 */
	public abstract Sink<? super O> getSink(int index) throws SourceIsClosedException, IllegalArgumentException, IndexOutOfBoundsException;

	/**
	 * Returns the ID of the sink, which has initially been specified when the given sink 
	 * got subscribed to this source. <BR>
	 *
	 * @param sink The sink whose subscription ID should be detected.
	 * @return The first matching ID of the specified sink.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 * @throws java.lang.IllegalArgumentException If the specified sinks is not subscribed.
	 */
	public abstract int getSinkID(Sink<? super O> sink) throws SourceIsClosedException, IllegalArgumentException;

	/**
	 * Returns the number of sinks that are currently subscribed to this source.
	 *
	 * @return The number of sinks.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public abstract int getNoOfSinks() throws SourceIsClosedException;

	/**
	 * Tests if this source is already opened.
	 *
	 * @return Returns <CODE>true</CODE>, if the source is already opened, otherwise <CODE>false</CODE>.
	 */
	public abstract boolean isOpened();

	/**
	 * Detects if this source has already been closed.
	 *
	 * @return Returns <CODE>true</CODE>, if the source has already been closed, otherwise <CODE>false</CODE>.
	 */
	public abstract boolean isClosed();
	
}