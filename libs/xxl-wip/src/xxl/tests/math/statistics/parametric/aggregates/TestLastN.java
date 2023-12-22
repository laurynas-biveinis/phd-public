package xxl.tests.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.math.statistics.parametric.aggregates.LastN;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class LastN.
 */
public class TestLastN {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		int n = 3;
		java.util.Iterator it = xxl.core.cursors.sources.Inductors.naturalNumbers(0, 20);
		System.out.println("Establishing temporal memory of size " + n);
		Aggregator agg = 
			new Aggregator(
				it, // the input-Cursor
				new LastN(3) // aggregate function
			);
		while (agg.hasNext()) {
			System.out.println(
				"Last seen " + n + " objects: " + java.util.Arrays.deepToString((Object[]) agg.next()));
		}
		agg.close();
	}

}
