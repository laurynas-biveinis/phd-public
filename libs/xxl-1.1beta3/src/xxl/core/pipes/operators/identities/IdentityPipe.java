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
import xxl.core.pipes.sources.Source;

/**
 * Operator component in a query graph that performs
 * the identity operation. All elements are directly 
 * transferred to this pipe's subscribed sinks.
 * 
 * @since 1.1
 */
public class IdentityPipe<I> extends AbstractPipe<I,I> {
	
	/** 
	 * Create a new IdentityPipe as an internal component of a query graph.
	 * The subscription to the specified sources is fulfilled immediately.
	 *
	 * @param sources This pipe gets subscribed to the specified sources.
	 * @param IDs This pipe uses the given IDs for subscription.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != IDs.length</CODE>.
	 */ 
	public IdentityPipe(Source<? extends I>[] sources, int[] IDs) {
		super(sources, IDs);
	}

	/** 
	 * Creates a new IdentityPipe as an internal component of a query graph.
	 * The subscription to the specified sources is fulfilled immediately. <BR>
	 *  For each subscription the <CODE>DEFAULT_ID</CODE> is used. 
	 *
	 * @param sources This pipe gets subscribed to the specified sources.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != IDs.length</CODE>.
	 */ 
	public IdentityPipe(Source<? extends I>[] sources) {
		super(sources);
	}

	/** 
	 * Creates a new IdentityPipe as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 */
	public IdentityPipe(Source<? extends I> source, int ID) {
		super(source, ID);
	}

	/** 
	 * Creates a new IdentityPipe as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately.
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 */ 
	public IdentityPipe(Source<? extends I> source) {
		super(source);
	}

	/** 
	 * Creates a new IdentityPipe as an internal component of a query graph. <BR>
	 * No subscriptions will be performed.
	 */
	public IdentityPipe() {	
		super();
	}
	
	/**
	 * The implementation looks as follows:
	 * <BR><BR>
	 * <CODE><PRE>
	 * 	transfer(o);
	 * </CODE></PRE> 
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int ID) throws IllegalArgumentException {
		transfer(o);
	}
	
}
