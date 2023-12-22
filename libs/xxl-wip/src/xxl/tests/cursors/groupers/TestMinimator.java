package xxl.tests.cursors.groupers;

import java.util.Iterator;

import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.groupers.Minimator;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Identity;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Minimator.
 */
public class TestMinimator {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		Minimator<Integer, Integer> minimator = new Minimator<Integer, Integer>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 10),
			new Identity<Integer>(),
			ComparableComparator.INTEGER_COMPARATOR
		);
		
		minimator.open();
		
		while (minimator.hasNext())
			System.out.print(minimator.next().getKey() +"; ");
		System.out.flush();
		System.out.println();
		
		minimator.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		Object[][] persons = new Object[][] {
			new Object[] {"Tobias", 23},
			new Object[] {"Juergen", 23},
			new Object[] {"Martin", 26},
			new Object[] {"Bjoern", 28},
			new Object[] {"Jens", 27},
			new Object[] {"Bernhard", 35},
			new Object[] {"Jochen", 29},
		};// Object[0] --> name, Object[1] --> age
		Minimator<Object[], Integer> minimator2 = new Minimator<Object[], Integer>(
			new xxl.core.cursors.sources.ArrayCursor<Object[]>(persons),
			new AbstractFunction<Object[], Integer>() {
				public Integer invoke(Object[] person) {
					return (Integer)person[1];
				}
			},
			new xxl.core.comparators.InverseComparator<Integer>(ComparableComparator.INTEGER_COMPARATOR)
		);
		
		minimator2.open();
		
		Iterator<Object[]> results;
		while (minimator2.hasNext()) {
			results = minimator2.next().getValue().listIterator(0);
			while (results.hasNext())
				System.out.print("Name: " + results.next()[0] + "; ");
			System.out.flush();
			System.out.println();
		}
		
		minimator2.close();
	}

}
