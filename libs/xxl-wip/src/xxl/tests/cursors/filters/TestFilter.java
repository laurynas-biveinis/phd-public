package xxl.tests.cursors.filters;

import xxl.core.cursors.filters.Filter;
import xxl.core.predicates.AbstractPredicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Filter.
 */
public class TestFilter {

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
		
		Filter<Integer> filter = new Filter<Integer>(
		    new xxl.core.cursors.sources.Enumerator(11),
		    new AbstractPredicate<Integer>() {
				public boolean invoke(Integer next) {
					return next % 2 == 0;
				}
			}
		);
		
		filter.open();
		
		while (filter.hasNext())
			System.out.println(filter.next());
		
		filter.close();
	}

}
