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

import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.EveryNth;
import xxl.core.predicates.Not;
import xxl.core.predicates.Predicate;

/**
 * A special kind of a {@link Filter} that drops elements. It is often used
 * to reduce high system load (load shedding). <BR>
 * Depending on the flag <CODE>mode</CODE> the k-th element delivered by this Dropper's 
 * source will be transferred or dropped, if k % n != 0.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	// every 3rd element is dropped
 * 	Dropper dropper = new Dropper(new Enumerator(100, 0), 3);
 * 	new VisualSink(dropper, "Dropper", true);
 * </code></pre> 
 *
 * @since 1.1
 */
public class Dropper<I> extends Filter<I> {

	public Dropper(int n, boolean mode) {
		super(new EveryNth<I>(n, mode));
	}
	
	/** 
	 * Creates a new Dropper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param n Every n-th element will be transferred or dropped.
	 * @param mode If <CODE>true</CODE> every n-th element will be transferred, otherwise dropped.
	 */ 
	public Dropper(Source<? extends I> source, int sourceID, int n, boolean mode) {
		super(source, sourceID, new EveryNth<I>(n, mode));
	}

	/** 
	 * Creates a new Droppper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param n Every n-th element will be transferred or dropped.
	 * @param mode If <CODE>true</CODE> every n-th element will be transferred, otherwise dropped.
	 */ 
	public Dropper(Source<? extends I> source, int n, boolean mode) {
		super(source, new EveryNth<I>(n, mode));
	}
	
	/** 
	 * Creates a new Droppper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param drop Unary predicate used for filtering. If it returns <CODE>true</CODE>,
	 * the element will be discarded, otherwise it is transferred.
	 * 
	 */ 
	public Dropper(Source<? extends I> source, Predicate<? super I> drop) {
		super(source, new Not(drop));
	}

	/** 
	 * Creates a new Droppper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The input rate and the output rate are not measured. <BR>
	 * Every n-th element is dropped. This algorithm matches with the
	 * load shedding technique.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param n Every n-th element will be transferred or dropped.
	 */ 
	public Dropper(Source<? extends I> source, int n) {
		this(source, n, false);
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
		Dropper<Integer> dropper = new Dropper<Integer>(new Enumerator(100, 5), 3);
		new VisualSink<Integer>(dropper, "Dropper", true);
	}

}