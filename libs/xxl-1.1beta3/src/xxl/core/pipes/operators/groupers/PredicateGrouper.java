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
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;

/**
 * Operator component in a query graph that partitions
 * the incoming elements to a number of groups. A user-defined
 * unary predicate is applied to each incoming element.
 * If its return value is <CODE>true</CODE>, a new group
 * starts and the element is transferred to it, otherwise
 * the element is transferred to the current group. <BR>
 * This kind of group-operation implies that only one
 * group is subscribed at a moment to the group-operator,
 * because the groups are filled sequentially.
 * <P>
 * <I>Note:</I> This group-operator performs only a partitioning,
 * but no aggregation.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	Enumerator e = new Enumerator(100, 100);
 * 	final PredicateGrouper grouper = new PredicateGrouper(
 * 		e, DEFAULT_ID, 
 * 		new Predicate() {
 * 			protected int last = 0;
 * 			
 * 			public boolean invoke(Object o) {
 * 				int i = ((Integer)o).intValue();
 * 				if (i > last + 9) {
 * 					last = i;
 * 					return true;
 * 				}
 * 				return false;
 * 			}
 * 		}, true, true
 * 	);
 * 	new VisualSink((Group)grouper.getReferenceToGroup(2), "Group 2", true);
 * 	new VisualSink((Group)grouper.getReferenceToGroup(7), "Group 7", true);
 * </code></pre>
 *
 * @see AbstractGrouper
 * @see Group
 * @see Predicate
 * @since 1.1
 */
public class PredicateGrouper<I> extends AbstractGrouper<I> {
	
	/**
	 * Unary predicate that determines, if an incoming
	 * element belongs to the current group or a new
	 * group has to be created. 
	 */
	protected Predicate<I> predicate;
	
	/**
	 * The group-number of the current group.
	 */
	protected int currentGroupNo = 0;
	
	/**
	 * A reference to the current group.
	 */
	protected Group<I> currentGroup;
	
	public PredicateGrouper(Predicate<I> predicate) {
		super();
		this.predicate = predicate;
	}
	
	/** 
	 * Creates a new PredicateGrouper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param predicate Unary predicate that determines, if an incoming
	 * 		element belongs to the current group or a new
	 * 		group has to be created. 
	 */ 
	public PredicateGrouper(Source<? extends I> source, int sourceID, Predicate<I> predicate) {
		this(predicate);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
	
	/**
	 * Evaluates the group-predicate for the given element.
	 * If the predicate's return value is <CODE>true</CODE>, a new group
 	 * starts and the element is transferred to it, otherwise
 	 * the element is transferred to the current group. <BR>
	 * The <CODE>process</CODE> method of this group
	 * is invoked with the element to be transferred. <BR>
	 * If the output rate is measured, the output counter
	 * is incremented.
	 * 
	 * @param o The element to be transferred.
	 * @return The number of <CODE>process</CODE> calls performed during this methods execution.
	 * 		In this case the return value is always 1.
	 */	
	@Override
	public void transfer(I o) {
		graph.RLock.lock();
		try {
			if (isClosed) return; // no interest in other groups
			if (currentGroup == null)
			    currentGroup = getGroup(currentGroupNo);
			if (predicate.invoke(o)) {
			    if (currentGroup != null) {
			    	currentGroup.updateDoneStatus(currentGroupNo);
			    	currentGroup.signalDone();
			    }
				currentGroupNo++;
				currentGroup = getGroup(currentGroupNo);
			}
			if (currentGroup != null) {
				currentGroup.process(o, currentGroupNo);
				((AbstractGrouperMetaDataManagement)metaDataManagement).incrementOutputCounter();
			}
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/** 
	 * Returns the current group-number.
	 *
	 * @return The current group-number.
	 */
	public int getCurrentGroupNo() {
		return currentGroupNo;
	}

	/**
	 * Returns a reference to the group that corresponds to the
	 * specified group-number <CODE>groupNo</CODE>.
	 * 
	 * @param groupNo The group-number.
	 * @return The group corresponding to the given group-number.
	 * @throws java.lang.IllegalArgumentException If the group corresponding to 
	 * 		this number has already been processed completely.
	 */
	@Override
	public Group<I> getReferenceToGroup(int groupNo) throws IllegalArgumentException {
		if (groupNo < this.currentGroupNo)
			throw new IllegalArgumentException("Group has alreay been processed completely.");
		Group<I> group = getGroup(groupNo);
		return group == null ?  new Group<I>(this, groupNo) : group;
	}
	
	protected Group<I> getGroup(int groupNo) {
	    graph.RLock.lock();
	    try {
		    if (sinks != null)
				for (int i = 0; i < sinks.length; i++)
					if (getSinkID(sinks[i]) == groupNo)
						return (Group<I>)sinks[i];
			return null;
	    }
	    finally {
	    	graph.RLock.unlock();
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
		Enumerator e = new Enumerator(100, 100);
		final PredicateGrouper<Integer> grouper = new PredicateGrouper<Integer>(
			e, DEFAULT_ID, 
			new Predicate<Integer>() {
				protected int last = 0;
				
				@Override
				public boolean invoke(Integer o) {
					if (o > last + 9) {
						last = o;
						return true;
					}
					return false;
				}
			}
		);
		new VisualSink<Integer>(grouper.getReferenceToGroup(2), "Group 2", true);
		new VisualSink<Integer>(grouper.getReferenceToGroup(7), "Group 7", true);
	}
}
