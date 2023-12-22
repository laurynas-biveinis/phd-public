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

package xxl.core.pipes.operators.unions;

import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;


/**
 * Operator component in a query graph that combines
 * several input streams and builds up a new output stream.
 * The elements that stream into this operator
 * are transferred to all subscribed sinks in the sequencing
 * of their arrival.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	Enumerator e1 = new Enumerator(0, 10, 100);
 * 	Enumerator e2 = new Enumerator(10, 20, 100);
 * 	Enumerator e3 = new Enumerator(20, 30, 100);
 * 
 * 	Union union = new Union(new Source[]{e1, e2, e3});
 * 	VisualSink sink = new VisualSink(union, "Union", true);
 * 
 * 	Enumerator e4 = new Enumerator(30, 40, 70);
 * 	e4.subscribe(union, 4);
 * 	union.addSource(e4, Union.DEFAULT_ID);
 * </code></pre>
 *
 * @since 1.1
 */
public class Union<I> extends AbstractPipe<I,I> {

	public Union() {
		super();
	}
	
	public Union(Source<? extends I> source0, Source<? extends I> source1, int sourceID0, int sourceID1) {
		super(new Source[]{source0,source1}, new int[]{sourceID0, sourceID1});
	}
	
	public Union(Source<? extends I> source0, Source<? extends I> source1) {
		super(new Source[]{source0,source1}, new int[]{0,1});
	}
	
	/** 
	 * Creates a new Union as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately.
	 *
	 * @param sources This pipe gets subscribed to the specified sources.
	 * @param IDs This pipe uses the given IDs for subscription.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != IDs.length</CODE>.
	 */ 
	public Union(Source<? extends I>[] sources, int[] IDs) {
		super(sources, IDs);
	}
	
	/** 
	 * Creates a new Union as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately.
	 * For each subscription the <CODE>DEFAULT_ID</CODE> is used. 
	 *
	 * @param sources This pipe gets subscribed to the specified sources.
	 */ 
	public Union(Source<? extends I>[] sources) {
		super(sources);
	}

	/** 
	 * The given element is transferred
	 * to all of this pipe's subscribed sinks by calling <CODE>super.transfer(o)</CODE>. 
	 *
	 * @param o The element streaming in.
	 * @param ID One of the IDs this pipe specified during its subscription by the underlying sources. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
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
		Enumerator e1 = new Enumerator(0, 10, 100);
		Enumerator e2 = new Enumerator(10, 20, 100);
		Enumerator e3 = new Enumerator(20, 30, 100);

		Union<Integer> union = new Union<Integer>(new Source[]{e1, e2, e3});
		new VisualSink<Integer>(union, "Union", true);
	}

}