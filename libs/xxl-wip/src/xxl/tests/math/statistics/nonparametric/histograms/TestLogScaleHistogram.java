package xxl.tests.math.statistics.nonparametric.histograms;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.groupers.AggregateGrouper;
import xxl.core.cursors.groupers.ReplacementSelection;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.math.statistics.nonparametric.histograms.LogScaleHistogram;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class LogScaleHistogram.
 */
public class TestLogScaleHistogram {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		// used data for examples
		List data = Cursors.toList(new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(20), 500));
		System.out.println("frequency of data");
		Iterator debug =
			new ReplacementSelection(
				new AggregateGrouper.CFDCursor(data.iterator()), 10, 
				new Comparator() {
					public int compare(Object o1, Object o2) {
						return ((Comparable) ((Object[]) o2)[1]).compareTo(((Object[]) o1)[1]);
					}
				}
			);
		while (debug.hasNext()) {
			Object[] tuple = (Object[]) debug.next();
			System.out.println("data >" + tuple[0] + "< occurred " + tuple[1] + " times");
		}

		LogScaleHistogram hist = new LogScaleHistogram(0, 20, 5, 10);
		Iterator it = data.iterator();
		while (it.hasNext())
			hist.process(((Number) it.next()).doubleValue(), new Random().nextDouble());
		System.out.println("\nResulting histogram:" + '\n' + hist);
	}

}
