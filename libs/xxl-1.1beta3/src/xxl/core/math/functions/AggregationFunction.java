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

package xxl.core.math.functions;

import java.io.Serializable;

import xxl.core.functions.Function;

/**
 * An aggregation-function is a binary function that computes an aggregate. The
 * function is called with the last aggregate and the next value that should be
 * added to the aggregate. Calling the function with an aggregate set to
 * <code>null</code> initializes the aggregation-function, i.e., the function
 * acts as it is called the very first time.
 * 
 * @param <P> the type of the aggregated values.
 * @param <A> the return type of the function, i.e., the type of the aggregate.
 */
public abstract class AggregationFunction<P, A> implements Serializable {

	/**
	 * Returns the result of the aggregation-function as an object of the
	 * aggregate type. The function is invoked with the last aggregation value
	 * (the last status of the aggregation) and the next value that should be
	 * considered by the aggregation-function.
	 * 
	 * @param aggregate the last aggregate returned by the function. When it is
	 *        set to <code>null</code> the aggregation-function is initialized,
	 *        i.e., it acts as it is called the very first time.
	 * @param value the next value that should be considered by the
	 *        aggregation-function.
	 * @return the new aggregate.
	 */
	public abstract A invoke(A aggregate, P value);
	
	public <T> AggregationFunction<T, A> compose(final Function<? super T, ? extends P> function) {
		final AggregationFunction<P, A> outer = this;
		return new AggregationFunction<T, A>() {
			public A invoke(A aggregate, T value) {
				return outer.invoke(aggregate, function.invoke(value));
			}
		};
	}

}