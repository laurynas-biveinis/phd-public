package xxl.tests.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sorters.ShuffleCursor;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.cursors.sources.Inductors;
import xxl.core.functions.Print;
import xxl.core.math.statistics.parametric.aggregates.SumEstimator;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SumEstimator.
 */
public class TestSumEstimator {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		// Summarize the natural numbers from 1 to 100,
		// first in an ascending order,
		// second randomly ordered.
		java.util.Iterator it = xxl.core.cursors.sources.Inductors.naturalNumbers(1, 100);
		xxl.core.cursors.mappers.Aggregator agg = new Aggregator<Number,Number>(it, new SumEstimator(100));

		System.out.println("ExpectedSum: (data given in ascending order)");
		xxl.core.cursors.Cursors.forEach(new Print(), agg);

		it = Inductors.naturalNumbers(1, 100);
		agg =
			new Aggregator(
				new ShuffleCursor(it, new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(new java.util.Random()))),
				new SumEstimator(100)
			);
		System.out.println(
			"\n--------------------------------------------\nExpectedSum: (data given in shuffled order)");
		while(agg.hasNext())
		    System.out.println(agg.next());
	}

}
