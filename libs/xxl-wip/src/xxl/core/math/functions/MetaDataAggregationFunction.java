/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

import xxl.core.functions.Function;
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
	@Override
	public A invoke(A aggregate, P value) {
		return aggregationFunction.invoke(aggregate, value);
	}
	
	@Override
	public <T> MetaDataAggregationFunction<T, A, M> compose(final Function<? super T, ? extends P> function) {
		final MetaDataAggregationFunction<P, A, M> outer = this;
		return new MetaDataAggregationFunction<T, A, M>(null) {
			@Override
			public A invoke(A aggregate, T value) {
				return outer.invoke(aggregate, function.invoke(value));
			}
			
			@Override
			public M getMetaData() {
				return outer.getMetaData();
			}
		};
	}
	
	/** 
	 * Returns the metadata associated with this aggregation function.
	 * 
	 * @return the metadata information.
	 */
	public abstract M getMetaData();
		
}
