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

package xxl.core.pipes.operators.mappers;

import xxl.core.functions.Function;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.sources.Source;

/**
 * Operator component in a query graph that applies an user-defined
 * unary function to each incoming element. The function's return
 * value is transferred to all subscribed sinks.
 *
 * @see xxl.core.functions.Function
 * @see xxl.core.pipes.operators.identities.Logger
 * @since 1.1
 */
public class Mapper<I,O> extends AbstractPipe<I,O> {

	/**
	 * Unary function applied to each incoming element:
	 * f: Object --> Object
	 */
	protected Function<? super I, ? extends O> mapping;

	/** 
	 * Creates a new Mapper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param mapping Unary function that is applied to each incoming element. The function's return
 	 * 		value is transferred to all subscribed sinks.
	 */ 
	public Mapper(Source<? extends I> source, int ID, Function<? super I, ? extends O> mapping) {
		super(source, ID);
		this.mapping = mapping;
	}

	/** 
	 * Creates a new Mapper as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param mapping Unary function that is applied to each incoming element. The function's return
 	 * 		value is transferred to all subscribed sinks.
	 */ 
	public Mapper(Source<? extends I> source, Function<? super I, ? extends O> mapping) {
		this(source, DEFAULT_ID, mapping);
	}
	
	public Mapper(Function<? super I, ? extends O> mapping) {
		this.mapping = mapping;
	}
	
	/** 
	 * The unary mapping function is applied to the given
	 * element. Its return value is transferred to all of this pipe's subscribed 
	 * sinks.
	 * Therefore the implementation looks as follows:
	 * <BR><BR>
	 * <CODE><PRE>
	 * 	super.transfer(mapping.invoke(o));
	 * </CODE></PRE>
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		super.transfer(mapping.invoke(o));
	}
	
	public Function<? super I, ? extends O> getMapping() {
		return mapping;
	}

}
