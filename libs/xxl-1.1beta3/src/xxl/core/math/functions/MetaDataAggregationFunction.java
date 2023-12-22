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

import xxl.core.util.metaData.MetaDataProvider;

/**
 * Decorates a given aggregation function with metadata information.
 * 
 * @param <P> the type of the aggregated values.
 * @param <A> the return type of the function, i.e., the type of the aggregate.
 * @param <M> the type of the mata data provided by this aggregation-function.
 */
public abstract class MetaDataAggregationFunction<P, A, M> extends DecoratorAggregationFunction<P, A> implements MetaDataProvider<M> {

	/**
	 * Constructs a new Oject of this type.
	 * 
	 * @param aggregationFunction the aggregation function
	 */
	public MetaDataAggregationFunction(AggregationFunction<? super P, A> aggregationFunction) {
		super(aggregationFunction);
	}

	/**
	 * Returns the result of the aggregation-function passed in the constructor
	 * as an object of the aggregate type. The function is invoked with the
	 * last aggregation value (the last status of the aggregation) and the next
	 * value that should be considered by the aggregation-function.
	 * 
	 * @param aggregate the last aggregate returned by the function. When it is
	 *        set to <code>null</code> the aggregation-function is initialized,
	 *        i.e., it acts as it is called the very first time.
	 * @param value the next value that should be considered by the
	 *        aggregation-function.
	 * @return the new aggregate.
	 */
	public A invoke(A aggregate, P value) {
		return aggregationFunction.invoke(aggregate, value);
	}
	
	/** 
	 * Returns the metadata associated with this aggregation function.
	 * 
	 * @return the metadata information.
	 */
	public abstract M getMetaData();
		
}
