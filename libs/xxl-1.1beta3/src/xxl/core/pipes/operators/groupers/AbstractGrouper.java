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

package xxl.core.pipes.operators.groupers;

import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.identities.IdentityPipe;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;

/**
 * Abstract framework for the group-operation. A group operator
 * consists of a singular pipe, where the elements stream in,
 * and a group-function or -predicate is applied to them,
 * which determines the group an element belongs to. 
 * A number of {@link IdentityPipe IdentityPipes} are subscribed
 * to this group-operator, each modelling an own group. If a 
 * sink wants to get elements from a group-operator, it has to
 * subscribe to a special group, otherwise an exception 
 * is thrown to indicate that no group has been selected. <BR>
 * The groups can be referenced via the method 
 * <CODE>getReferenceToGroup(groupNo)</CODE>. The method
 * <CODE>transfer</CODE> maps an incoming element to its
 * corresponding group(s). Thus, both methods are abstract because
 * they depend on a group-operator's implementation. 
 * <P>
 * <I>Note:</I> A group-operator performs only a partitioning,
 * but no aggregation.
 *
 * @see Group
 * @see HashGrouper
 * @see PredicateGrouper
 * @since 1.1
 */
public abstract class AbstractGrouper<I> extends IdentityPipe<I> {
	
	/**
	 * Models a group. If a 
 	 * sink wants to get elements from a group-operator, it has to
 	 * subscribe to a special group, otherwise an exception 
 	 * is thrown to indicate that no group has been selected.
	 */
	public static class Group<I> extends IdentityPipe<I> {
	
		/** 
		 * Creates a new group as an integral part of a group-operator. <BR>
		 * The subscription to the specified source (the group-operator) is fulfilled immediately.
		 *
		 * @param source This pipe gets subscribed to the specified source (group-operator).
		 * @param ID This pipe uses the given ID for subscription.
		 */ 
		public Group(Source<? extends I> source, int sourceID) {
			super(source, sourceID);
		}
		
	}
	
	public class AbstractGrouperMetaDataManagement extends AbstractPipeMetaDataManagement {
			
		protected void incrementOutputCounter() {
			if (measureOutputRate) {
				synchronized(metaDataManagement) {
					outputCounter++;
			    }
			}
		}
	}
	
	public AbstractGrouper() {
		this.metaDataManagement = new AbstractGrouperMetaDataManagement();
	}
	
	/** 
	 * Helps to create a new group-operator as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 */ 
	public AbstractGrouper(Source<? extends I> source, int sourceID) {
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
		this.metaDataManagement = new AbstractGrouperMetaDataManagement();
	}
	
	/**
	 * Subscribes the specified sink (group) with the given ID to this pipe.
	 *
	 * @param sink The sink (group) to be subscribed.
	 * @param sourceID The ID which will be passed when transfering a new object to the specified sink.
	 * @return Returns <CODE>true</CODE>, if the subscribe operation has been successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the pipe has already been closed.
	 * @throws java.lang.UnsupportedOperationException If the specified sink is no instance of the class Group.
	 */
	@Override
	public boolean subscribe(Sink<? super I> sink, int sourceID) throws SourceIsClosedException {
		if (!(sink instanceof Group))
			throw new UnsupportedOperationException("Please subscribe to a special group.");
		return super.subscribe(sink, sourceID);
	}
	
	/**
	 * The given sink (group) cancels its subscription. The registered sink with the given ID 
	 * will not be informed about new elements any longer. 
	 * 
	 * @param sink The sink (group) whose subscription is to be cancelled.
	 * @param ID The ID of the sink whose subscription is to be cancelled.
	 * @return Returns <CODE>true</CODE>, if the unsubscribe operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the pipe has already been closed.
	 * @throws java.lang.UnsupportedOperationException If the specified sink is no instance of the class Group.
	 */	
	@Override
	public boolean unsubscribe(Sink<? super I> sink, int sourceID) throws SourceIsClosedException {
		if (!(sink instanceof Group))
			throw new UnsupportedOperationException("Please unsubscribe to a special group.");
		return super.unsubscribe(sink, sourceID);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#close()
	 */
	@Override
	public void close() {
		if (isClosed) return;			
				
		processingWLock.lock();
		graph.WLock.lock();
		try {
			if (isClosed) return;
			if (sinks != null) {
				for (int i = 0; i < sinks.length; i++)
					super.unsubscribe(sinks[i], sinkIDs[i]);
			}
			sinks = null;
			sinkIDs = null;
			isOpened = false;
			isClosed = true;
		}
		finally {
			processingWLock.unlock();
			try {
				if (sources != null) {
					for (int i = 0; i < sources.length; i++)
						if (!sources[i].isClosed()) {
							sources[i].unsubscribe(this, sourceIDs[i]);
							sources[i].close();
						}
				}
				sources = null;
				sourceIDs = null;
				sourcesDone = null;
			}
			finally {
				graph.WLock.unlock();
			}
		}
	}
	
	/**
	 * Transfers the given element to its corresponding group(s). <BR>
	 * The <CODE>process</CODE> method for each of these sinks
	 * is invoked with the element to be transferred and
	 * the ID the corresponding sink (group) initially specified during its subscription. <BR>
	 * If the output rate is measured, the output counter
	 * is incremented by adding the number of <CODE>process</CODE> calls.
	 * 
	 * @param o The element to be transferred.
	 * @return The number of <CODE>process</CODE> calls performed during this methods execution.
	 */	
	@Override
	public abstract void transfer(I o);
	
	/**
	 * Returns a reference to the group that corresponds to the
	 * specified group-number <CODE>groupNo</CODE>.
	 * 
	 * @param groupNo The group-number.
	 * @return The group corresponding to the given group-number.
	 * @throws java.lang.IllegalArgumentException If the specified group-number is not valid.
	 */
	public abstract Group<I> getReferenceToGroup(int groupNo) throws IllegalArgumentException;
	
}
