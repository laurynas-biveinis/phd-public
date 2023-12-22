package xxl.tests.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.math.statistics.parametric.aggregates.NumberOfRuns;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NumberOfRuns.
 */
public class TestNumberOfRuns {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		/* ------------------------------------------------------------------------
		   - Computes the number of runs in a random sequence of Integer-Objects  -
		   ------------------------------------------------------------------------ */
		java.util.List l = xxl.core.cursors.Cursors.toList(new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(20), 40));
		xxl.core.cursors.mappers.Aggregator aggregator = new Aggregator(l.iterator(), new NumberOfRuns());
		java.util.Iterator it = l.iterator();
		System.out.println("sequence\tnumber of runs counted so far");
		while (aggregator.hasNext())
			System.out.println(it.next() + ":\t" + aggregator.next());
	}

}
