package xxl.tests.math.statistics.nonparametric.histograms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.groupers.AggregateGrouper;
import xxl.core.cursors.groupers.ReplacementSelection;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.cursors.sources.Inductor;
import xxl.core.functions.AbstractFunction;
import xxl.core.math.statistics.nonparametric.histograms.RankBasedHistogram;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RankBasedHistogram.
 */
public class TestRankBasedHistogram {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		// used data for examples
		List data = Cursors.toList(new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(10), 500));
		System.out.println("\nused cfd");
		Iterator debug =
			new ReplacementSelection(new AggregateGrouper.CFDCursor(data.iterator()), 10, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Object[]) o2)[1]).compareTo(((Object[]) o1)[1]);
			}
		});
		while (debug.hasNext()) {
			Object[] tuple = (Object[]) debug.next();
			System.out.println("data >" + tuple[0] + "< occurred " + tuple[1] + " times");
		}

		//Used points to query
		final int q = 9;
		List pointQueries = new ArrayList();
		Cursors.toList(
			new Inductor(
				new AbstractPredicate() {
					int c = 0;
					public boolean invoke(Object o) {
						return (c++) < q ? true : false;
					}
				}, 
				new AbstractFunction() {
					public Object invoke(Object n) {
						return new Integer(((Integer) n).intValue() + 1);
					}
				},
				0
			), 
			pointQueries
		);
		//
		Iterator qe = null;

		/*********************************************************************/
		/*                            Example 0                              */
		/*********************************************************************/
		// histogram without any error ...
		RankBasedHistogram control = new RankBasedHistogram(data.iterator(), new RankBasedHistogram.Cfd());

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		RankBasedHistogram histogram = new RankBasedHistogram(data.iterator(), new RankBasedHistogram.MaxBased(4));
		System.out.println("\nrank-based histogram (count based - abs. frequencies - max based )");
		System.out.println("histogram:\n" + histogram);
		qe = pointQueries.iterator();
		while (qe.hasNext()) {
			Object next = qe.next();
			System.out.println(
				"querying("
					+ next
					+ ")= "
					+ histogram.pointQuery(next)
					+ "\t err()="
					+ Math.abs(control.pointQuery(next) - histogram.pointQuery(next)));
		}

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		//
		RankBasedHistogram histogram2 = new RankBasedHistogram(data.iterator(), new RankBasedHistogram.OptimizedMaxBased(4));
		System.out.println("\nrank-based histogram (count based - abs. frequencies - opt. max based )");
		System.out.println("histogram:\n" + histogram2);
		qe = pointQueries.iterator();
		while (qe.hasNext()) {
			Object next = qe.next();
			System.out.println(
				"querying("
					+ next
					+ ")= "
					+ histogram2.pointQuery(next)
					+ "\t err()="
					+ Math.abs(control.pointQuery(next) - histogram2.pointQuery(next)));
		}

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		//
		RankBasedHistogram histogram3 = new RankBasedHistogram(data.iterator(), new RankBasedHistogram.MaxDiff(4));
		System.out.println("\nrank-based histogram (count based - abs. frequencies - max diff )");
		System.out.println("histogram:\n" + histogram3);
		qe = pointQueries.iterator();
		while (qe.hasNext()) {
			Object next = qe.next();
			System.out.println(
				"querying("
					+ next
					+ ")= "
					+ histogram3.pointQuery(next)
					+ "\t err()="
					+ Math.abs(control.pointQuery(next) - histogram3.pointQuery(next)));
		}
	}

}
