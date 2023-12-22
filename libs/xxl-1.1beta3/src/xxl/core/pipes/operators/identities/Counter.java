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

package xxl.core.pipes.operators.identities;

import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 * Operator component in a query graph that counts
 * the elements streaming in. <BR>
 * The counter can also be resetted.
 * <p>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	new VisualSink(new Counter(new Enumerator(1000, 10)), true);
 * </code></pre>
 * @since 1.1
 */
public class Counter<I> extends AbstractPipe<I,I> {
	
	/**
	 * The counter.
	 */
	protected long count = 0;
	
	/** 
	 * Creates a new Counter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 */ 
	public Counter(Source<? extends I> source, int ID) {
		super(source, ID);
	}

	/** 
	 * Creates a new Counter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 */ 
	public Counter(Source<? extends I> source) {
		super(source);
	}
	
	public Counter() {
		
	}

	/** 
	 * The counter is incremented and the 
	 * given element is transferred to all of this pipe's subscribed sinks 
	 * by calling <CODE>super.transfer(o)</CODE>. 
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int ID) throws IllegalArgumentException {
		count++;
		super.transfer(o);
	}

	/**
	 * Returns the current state of the counter, i.e., the 
	 * number of elements that streamed in already.
	 *
	 * @return Number of elements that streamed in already.
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Resets the counter.
	 */
	public void resetCount() {
		count = 0;
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
		new VisualSink<Integer>(new Counter<Integer>(new Enumerator(1000, 10)), true);
	}

}