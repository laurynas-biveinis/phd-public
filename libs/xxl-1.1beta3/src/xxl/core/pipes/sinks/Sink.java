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

import xxl.core.pipes.queryGraph.Node;
import xxl.core.pipes.sources.Source;

/**
 * The framework for query processing is based on dataflow graphs, therefore
 * it consists of three fundamental types of components: sources, operators and sinks.
 * Sources represent the initial components of a query graph whereas sinks
 * act as terminal components. Operators can only appear 'inside'
 * a query graph. <P> 
 * This interface models an arbitrary sink in a query graph. A sink continuously consumes
 * elements streaming in from different sources, i.e., the sources call
 * the sink's <CODE>process</CODE> method permanently. If a source runs out of elements,
 * it calls the sink's <CODE>done</CODE> method. <BR>
 * An operator (interface {@link xxl.core.pipes.operators.Pipe}) extends the interfaces {@link Source}
 * and {@link Sink}, because on the hand it receives elements from its underlying
 * sources and on the other hand it transfers its results to its subscribed sinks.
 * 
 * @since 1.1
 */
public abstract interface Sink<I> extends Node {
	
	/**
	 * This method will be called in the <CODE>transfer</CODE> method of each source
	 * this sink got subscribed to. The element to be transferred will be sent to this
	 * sink selecting it as the first paramenter. The second parameter specifies
	 * the ID this sink used during its subscription. Therefore, the ID determines from
	 * which source this elements is from. <BR>
	 * The element should not be cloned if it is transferred to multiple sinks which
	 * have been registered by a source. Due to performance reasons this source should rather 
	 * use the same element and pass it to all subscribed sinks based on the assumption
	 * that these sinks will not perform any changes on this element, unless they clone
	 * it themselves.
	 *
	 * @param o The element streaming in.
	 * @param sourceID The ID this sink specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	public abstract void process(I o, int sourceID) throws IllegalArgumentException;

	/**
	 * The method <CODE>done</CODE> is invoked, if the source with ID <CODE>ID</CODE> 
	 * runs out of elements.
	 *
	 * @param ID The ID this sink specified during its subscription by an underlying source. 
	 */
	public abstract void done(int sourceID);

	/**
	 * Similar to the <CODE>subscribe</CODE> method of a source, this method
	 * establishes a connection between a sink and a source during runtime. The intention of
	 * both methods is to build up a double-linked query graph that is completely navigatable. <BR>
	 * The method should be used to store a reference to the given underlying source
	 * together with the ID specified during this sink's subscription. <BR>
	 *
	 * @param source The source this sink got subscriped to.
	 * @param sourceID The ID this sinks used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the source has successfully been added, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	public abstract boolean addSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException;

	/**
	 * In conformity to the <CODE>unsubscribe</CODE> method of a source, this
	 * method removes the connection between a sink and a source on the sink's side.
	 * It removes the given source with ID <CODE>ID</CODE> from the
	 * internal datastructure used to store this sink's references to its sources.
	 * 
	 * @param source The source this sink got subscriped to.
	 * @param sourceID The ID this sinks used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the remove operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	public abstract boolean removeSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException;

	/**
	 * Returns a reference to a source belonging to the specified index, i.e.,
	 * the index which was returned for this source when the method <CODE>addSource</CODE>
	 * was called.
	 * 
	 * @param index The index used to access sources in an internal datastructure of this sink.
	 * @return The source belonging to the specified index.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 * @throws java.lang.IllegalArgumentException If no sources have been added.
	 * @throws java.lang.IndexOutOfBoundsException If the specified index does not exist.
	 */
	public abstract Source<? extends I> getSource(int index) throws SinkIsDoneException, IllegalArgumentException, IndexOutOfBoundsException;

	/** 
	 * Returns the first matching ID this sink used during its subscription by the specified source.
	 * This ID has already been stored, when the <CODE>addSource</CODE> method was invoked.
	 *
	 * @param source The source this sink got subscribed to.
	 * @return The ID this sink specified during its subscription.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 * @throws java.lang.IllegalArgumentException If the specified sources has not been added.
	 */
	public abstract int getSourceID(Source<? extends I> source) throws SinkIsDoneException, IllegalArgumentException;

	/**
	 * Returns the number of sources that are currently connected with this sink.
	 *
	 * @return The number of sources.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	public abstract int getNoOfSources() throws SinkIsDoneException;

	/**
	 * If a sink is done, it will not receive further elements as all associated sources are done.
	 * 
	 * @return Returns <CODE>true</CODE>, if the sink is already done, otherwise <CODE>false</CODE>.
	 */
	public abstract boolean isDone();
		
	/**
	 * Indicates whether this sink received done from the source with ID sourceID.
	 * 
	 * @param sourceID ID of the source 
	 * @return Returns <CODE>true</CODE>, if the sink received a done from the source with ID sourceID.
	 */
	public abstract boolean receivedDone(int sourceID);
	
}
