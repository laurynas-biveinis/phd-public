package xxl.tests.math.statistics.parametric.aggregates;

import java.util.Arrays;
import java.util.List;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.Inductors;
import xxl.core.functions.AbstractFunction;
import xxl.core.math.Maths;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.LastNthAverage;
import xxl.core.math.statistics.parametric.aggregates.NumericalAggregateND;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NumericalAggregateND.
 */
public class TestNumericalAggregateND {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1a                             */
		/*********************************************************************/
		int c = 0;
		xxl.core.cursors.mappers.Aggregator agg = null;
		// making data for testing, storing it in a List to use it for every example
		java.util.Iterator it1 = Inductors.naturalNumbers(0, 49);
		java.util.Iterator it2 = Inductors.naturalNumbers(0, 49);
		java.util.Iterator it3 = 
			new Mapper(
				new AbstractFunction<Object,Object>() {
					public Object invoke(List<? extends Object> list) {
						double[] d = new double[2];
						d[0] = ((Number) (list.get(0))).doubleValue();
						d[1] = ((Number) (list.get(1))).doubleValue() + 10.0;
						return d;
					}
				},new java.util.Iterator[] { it1, it2 }
			);
		// storing data in a list
		java.util.List list = new java.util.ArrayList();
		xxl.core.cursors.Cursors.toList(it3, list);
		// printing data for evaluation
		java.util.Iterator temp = list.iterator();
		System.out.println("data used for demo:");
		while (temp.hasNext())
			System.out.println(Arrays.toString((double[]) temp.next()));
		System.out.println("-------------------------------");
		c = 0;
		agg =
			new Aggregator(
				list.iterator(),
				Maths.multiDimAggregateFunction(new AggregationFunction[] {
					NumericalAggregateND.MINIMUM_ND,
					NumericalAggregateND.VARIANCE_ND,
					NumericalAggregateND.AVERAGE_ND,
					NumericalAggregateND.MAXIMUM_ND,
					new NumericalAggregateND(
						new AbstractFunction() { 
							public Object invoke() { 
								return new LastNthAverage(5);
							}
						}
					)
				}
			));
		while (agg.hasNext()) {
			Object next = agg.next();
			System.out.print("step=" + (c++));
			if (next != null) {
				List nx = (List) next;
				System.out.print("\nmin: " + Arrays.toString((double[]) nx.get(0)));
				System.out.println("\tvar: " + Arrays.toString((double[]) nx.get(1)));
				System.out.print("avg: " + Arrays.toString((double[]) nx.get(2)));
				System.out.println("\tmax: " + Arrays.toString((double[]) nx.get(3)));
				System.out.println("moving avg(5): " + Arrays.toString((double[]) nx.get(4)));
			} else
				System.out.println("\tnot all used aggregation functions initialized!");
		}
	}

}
