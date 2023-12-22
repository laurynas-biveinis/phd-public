package xxl.tests.cursors.groupers;

import xxl.core.cursors.groupers.AggregateGrouper;
import xxl.core.cursors.groupers.ReplacementSelection;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AggregateGrouper.
 */
public class TestAggregateGrouper {

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
		
		// cumulative frequency distribution
		
		xxl.core.cursors.Cursor cfd = new AggregateGrouper.CFDCursor(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(10), 100)
		);

		cfd.open();
		
		System.out.println("\ncumulative frequency distribution (unsorted):");
		while (cfd.hasNext()) {
			Object[] next = (Object[])cfd.next();
			System.out.println(next[0] + " has occured " + next[1] + " times");
		}
		
		cfd.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		// remove duplicates
		
		xxl.core.cursors.Cursor noDubs = new xxl.core.cursors.mappers.Mapper(
			new AbstractFunction() {
				public Object invoke(Object o) {
					return ((Object[])o)[0];
				}
			},
			new AggregateGrouper.DuplicatesRemover(
				new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(10), 100)
			)
		);

		noDubs.open();
		
		System.out.println("\nremove any duplicates");
		while (noDubs.hasNext())
			System.out.println(noDubs.next());
			
		noDubs.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		
		// cumulative frequency distribution (sorted by value)
		
		xxl.core.cursors.Cursor cfdSort = new ReplacementSelection(
			new AggregateGrouper.CFDCursor(
				new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(10), 100)
			),
			100,
			new java.util.Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Integer)((Object[])o1)[0]).intValue() - ((Integer)((Object[])o2)[0]).intValue();
				}
			}
		);
		
		cfdSort.open();
		
		System.out.println("\ncumulative frequency distribution (sorted by value):");
		while (cfdSort.hasNext()) {
			Object[] next = (Object[])cfdSort.next();
			System.out.println(next[0] + " has occured " + next[1] + " times");
		}
		
		cfdSort.close();

		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		
		// cumulative frequency distribution (sorted by frequency)
		
		xxl.core.cursors.Cursor cfdSort2 = new ReplacementSelection(
			new AggregateGrouper.CFDCursor(
				new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(10), 100)
			),
			100,
			new java.util.Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Long)((Object[])o1)[1]).intValue() - ((Long)((Object[])o2)[1]).intValue();
				}
			}
		);
		
		cfdSort2.open();

		System.out.println("\ncumulative frequency distribution (sorted by frequency):");
		while (cfdSort2.hasNext()) {
			Object[] next = (Object[])cfdSort2.next();
			System.out.println(next[0] + " has occured " + next[1] + " times");
		}
		
		cfdSort2.close();

		/*********************************************************************/
		/*                            Example 5                              */
		/*********************************************************************/
		
		// discrete (integer) equi-width histogram
		
		xxl.core.cursors.Cursor histogram = new AggregateGrouper(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(10), 500),
			new AbstractFunction() {
				int start = 0;
				int binWidth = 3;
				public Object invoke(Object integer) {
					int v = ((Integer)integer).intValue();
					int bin = v / binWidth;
					return "[ " + Integer.toString(bin*binWidth) + ", " + Integer.toString((bin+1)*binWidth) + ")";
				}
			},
			new AbstractFunction() {
				public Object invoke() {
					return new xxl.core.math.statistics.parametric.aggregates.StatefulAverage();
				}
			}
		);
		
		histogram.open();
		
		System.out.println("\nequi-width (discrete) histogram distribution:");
		while (histogram.hasNext()) {
			Object[] next = (Object[])histogram.next();
			System.out.println(next[0] + " has average of " + next[1]);
		}
		
		histogram.close();
	}

}
