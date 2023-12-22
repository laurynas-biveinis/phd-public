/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

import xxl.core.functions.AbstractFunction;

/**
 * This class wraps an aggregation-function to a normal function.
 * 
 * @param <P> the type of the aggregated values.
 * @param <A> the return type of the function, i.e., the type of the aggregate.
 */
public class AggregationFunctionFunction<A, P extends A> extends AbstractFunction<P, A> {

	/**
	 * The aggregation-function to be wrapped.
	 */
	protected AggregationFunction<? super P, A> aggregationFunction;
	
	/**
	 * Creates a new function that wraps the specified aggregation-function.
	 * 
	 * @param aggregationFunction the aggregation-function to be wrapped.
	 */
	public AggregationFunctionFunction(AggregationFunction<? super P, A> aggregationFunction) {
		this.aggregationFunction = aggregationFunction;
	}
	
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
	@Override
	public A invoke(P aggregate, P value) {
		return aggregationFunction.invoke(aggregate, value);
	}

}
