/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.math.functions;

public class CountingAggregationFunction<P,A> extends DecoratorAggregationFunction<P,A> {

	protected long initCalls = 0;
	protected long calls = 0;
	
	public CountingAggregationFunction(AggregationFunction<? super P,A> aggregationFunction) {
		super(aggregationFunction);
	}
	
	public A invoke(A aggregate, P value) {
		if (aggregate == null)
			initCalls++;
		calls++;
		return super.invoke(aggregate, value);
	}
	
	public long getCalls() {
		return calls;
	}

	public long getInitCalls() {
		return initCalls;
	}

	public void resetCounters() {
		this.initCalls = 0;
		this.calls = 0;
	}

}
