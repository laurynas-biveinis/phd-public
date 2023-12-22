package xxl.core.math.functions;

import xxl.core.functions.Function;

public abstract class StatelessAggregationFunction<P, A, R> extends AggregationFunction<P, A> {

	public abstract Function<? super A, ? extends R> getAggregateMapping();
	
}
