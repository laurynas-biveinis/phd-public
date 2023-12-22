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

import xxl.core.functions.Function;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 * Operator component in a query graph that partitions
 * the incoming elements to a number of groups. The mapping
 * between an element and its corresponding group is
 * given by a user-defined unary function.
 * <P>
 * <I>Note:</I> This group-operator performs only a partitioning,
 * but no aggregation.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	Enumerator e = new Enumerator(100, 20);
 * 	HashGrouper grouper = new HashGrouper(
 * 		e, DEFAULT_ID, 
 * 		new Function() {
 * 			public Object invoke(Object o) {
 * 				return new Integer(((Integer)o).intValue() % 10);
 * 			}
 * 		},
 * 		10,	true, true
 * 	);
 * 	new VisualSink((Group)grouper.getReferenceToGroup(0), "Group 0", true);
 * 	new VisualSink((Group)grouper.getReferenceToGroup(7), "Group 7", true);
 * </code></pre>
 *
 * @see AbstractGrouper
 * @see Group
 * @see Function
 * @since 1.1
 */
public class HashGrouper<I> extends AbstractGrouper<I> {
	
	/**
	 * Unary hash-function used to map an element
	 * to its corresponding group. <BR>
	 * Function f: I --> Integer
	 */
	protected Function<I,Integer> hashFunction;
	
	public HashGrouper(Function<I,Integer> hashFunction, int noOfGroups) {
		this.hashFunction = hashFunction;
		for (int i = 0; i < noOfGroups; i++)
			new Group<I>(this, i);
	}
	
	/** 
	 * Creates a new HashGrouper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 * @param hashFunction The unary function f: Object --> Integer used to
	 * 		to map an element to its corresponding group.
	 * @param noOfGroups The number of groups that can be addressed by the given hash-function.
	 */ 
	public HashGrouper(Source<? extends I> source, int sourceID, Function<I,Integer> hashFunction, int noOfGroups) {
		this(hashFunction, noOfGroups);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}
	
	/** 
	 * Creates a new HashGrouper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param hashFunction The unary function f: Object --> Integer used to
	 * 		to map an element to its corresponding group.
	 * @param noOfGroups The number of groups that can be addressed by the given hash-function.
	 */
	public HashGrouper(Source<? extends I> source, Function<I,Integer> hashFunction, int noOfGroups) {
		this(source, DEFAULT_ID, hashFunction, noOfGroups);
	}
		
	/**
	 * Transfers the given element to its corresponding group
	 * by applying the hash-function on it. <BR>
	 * The <CODE>process</CODE> method of this group
	 * is invoked with the element to be transferred. <BR>
	 * If the output rate is measured, the output counter
	 * is incremented.
	 * 
	 * @param o The element to be transferred.
	 * @return The number of <CODE>process</CODE> calls performed during this methods execution.
	 */	
	@Override
	public void transfer(I o) {
		graph.RLock.lock();
		try {
		    if (sinks != null) {
				int groupNo = hashFunction.invoke(o);
				sinks[groupNo].process(o, groupNo);
				((AbstractGrouperMetaDataManagement)metaDataManagement).incrementOutputCounter();
		    }
		}
		finally {
			graph.RLock.unlock();
		}
	}
	
	/**
	 * Returns a reference to the group that corresponds to the
	 * specified group-number <CODE>groupNo</CODE>.
	 * 
	 * @param groupNo The group-number.
	 * @return The group corresponding to the given group-number.
	 * @throws java.lang.IllegalArgumentException If the specified group-number is not valid.
	 */
	@Override
	public Group<I> getReferenceToGroup(int groupNo) throws IllegalArgumentException {
		if (groupNo > getNoOfSinks())
			throw new IllegalArgumentException("Specified group does not exist.");
		return (Group<I>)getSink(groupNo);
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
		HashGrouper<Integer> grouper = new HashGrouper<Integer>(
			e, DEFAULT_ID, 
			new Function<Integer,Integer>() {
				@Override
				public Integer invoke(Integer o) {
					return o % 10;
				}
			},
			10
		);	
		new VisualSink<Integer>(grouper.getReferenceToGroup(0), "Group 0", true);
		new VisualSink<Integer>(grouper.getReferenceToGroup(7), "Group 7", true);
	}
}
