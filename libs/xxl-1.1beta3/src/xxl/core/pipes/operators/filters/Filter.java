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

package xxl.core.pipes.operators.filters;

import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;

/**
 * Operator component in a query graph that selects 
 * the elements that stream in by using an unary predicate.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	 // defining the filter-predicate
 * 	Predicate predicate = new Predicate () {
 * 		public boolean invoke (Object o) {
 * 			return ((Integer)o).intValue()%2 == 0;
 * 		}
 * 	};
 * 	// a simple query graph
 * 	new Tester(new Filter(new Enumerator(10000000, 0), predicate));
 * </code></pre>
 *
 *
 * @see Predicate
 * @since 1.1
 */
public class Filter<I> extends AbstractPipe<I,I> {

	/**
	 * Unary selection predicate.
	 */
	protected Predicate<? super I> predicate;

	public Filter(Predicate<? super I> predicate) {
		this.predicate = predicate;
	}
	
	/** 
	 * Creates a new Filter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param predicate Unary predicate used for filtering. If it returns <CODE>true</CODE>,
	 * 		the element will be transferred, otherwise it is discarded.
	 */ 
	public Filter(Source<? extends I> source, int sourceID, Predicate<? super I> predicate) {
		this(predicate);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	/** 
	 * Creates a new Filter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param predicate Unary predicate used for filtering. If it returns <CODE>true</CODE>,
	 * 		the element will be transferred, otherwise it is discarded.
	 */ 
	public Filter(Source<? extends I> source, Predicate<? super I> predicate) {
		this(source, DEFAULT_ID, predicate);
	}

	/** 
	 * The unary predicate is applied to the given
	 * element. If it returns <CODE>true</CODE>, the element is transferred
	 * to all of this pipe's subscribed sinks by calling <CODE>super.transfer(o)</CODE>. 
	 *
	 * @param o The element streaming in.
	 * @param ID One of The IDs this pipe specified during its subscription by the underlying sources. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		if (predicate.invoke(o))
			super.transfer(o);
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
		Predicate<Integer> predicate = new Predicate<Integer> () {
			@Override
			public boolean invoke (Integer o) {
				return o%2 == 0;
			}
		};
		Printer printer = new Printer<Integer>(new Filter<Integer>(new Enumerator(1000, 10), predicate));
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);

	}

}
