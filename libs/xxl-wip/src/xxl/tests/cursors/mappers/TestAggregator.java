package xxl.tests.cursors.mappers;

import java.util.List;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.math.Maths;
import xxl.core.math.functions.AggregationFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Aggregator.
 */
public class TestAggregator {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		Aggregator<?, ?> aggregator = new Aggregator<Integer, Integer>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 50), // the input cursor
			new AggregationFunction<Integer, Integer>() { // the aggregation function
				public Integer invoke(Integer aggregate, Integer next) {
					if (aggregate == null)
						return next;
					return Maths.max(aggregate, next);
				}
			}
		);
		
		aggregator.open();
		
		System.out.println("The result of the maximum aggregation is: " + aggregator.last());
		
		aggregator.close();
		
		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		aggregator = new Aggregator<Integer, List<Integer>>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 50), // the input cursor
			Maths.multiDimAggregateFunction(
				new AggregationFunction<Integer, Integer>() { // the aggregation function
					public Integer invoke(Integer aggregate, Integer next) {
						if (aggregate == null)
							return next;
						return Maths.max(aggregate, next);
					}
				},
				new AggregationFunction<Integer, Integer>() { // the second aggregation function
					public Integer invoke(Integer aggregate, Integer next) {
						if (aggregate == null)
							return next;
						return aggregate + next;
					}
				}
			)
		);
		
		aggregator.open();
		
		System.out.println("The result of the maximum aggregation is: " + aggregator.last());
		
		aggregator.close();
	}

}
