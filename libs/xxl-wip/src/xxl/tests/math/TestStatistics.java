package xxl.tests.math;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.groupers.AggregateGrouper;
import xxl.core.cursors.groupers.ReplacementSelection;
import xxl.core.cursors.groupers.AggregateGrouper.CFDCursor;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.math.Statistics;
import xxl.core.math.numerics.splines.RB1CubicBezierSpline;
import xxl.core.util.Distance;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Statistics.
 */
public class TestStatistics {

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
		// Gaussian distribution
		double p = 0.0;
		int steps = 100;
		for (int i = 1; i <= steps; i++) {
			p = 1.0 + (1.0 / steps) - ((double) i / (double) steps);
			System.out.println(p + "-quantil of N(0,1)\t=" + Statistics.normalQuantil(p));
		}
		System.out.println();

		/*********************************************************************/
		/*                            Example 1b                             */
		/*********************************************************************/
		// Triweight kernel
		steps = 100;
		double min = -1.0;
		double max = 1.0;
		System.out.println("x \t tri(x) \t tri'(x) \t int(trix(x))dx");
		for (int i = 0; i <= steps; i++) {
			p = min + (max - min) * i / steps;
			System.out.println(
				p
					+ "\t"
					+ Statistics.triweight(p)
					+ "\t"
					+ Statistics.triweightDerivative(p)
					+ "\t"
					+ Statistics.triweightPrimitive(p));
		}
		System.out.println();

		/*********************************************************************/
		/*                            Example 1c                             */
		/*********************************************************************/
		// CosineArch kernel
		System.out.println(
			"x \t cosArch(x) \t cosArch'(x) \t int(cosArch(x))dx");
		for (int i = 0; i <= steps; i++) {
			p = min + (max - min) * i / steps;
			System.out.println(
				p
					+ "\t"
					+ Statistics.cosineArch(p)
					+ "\t"
					+ Statistics.cosineArchDerivative(p)
					+ "\t"
					+ Statistics.cosineArchPrimitive(p));
		}
		System.out.println();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		// cumulative frequency distribution to double[], int[]
		Cursor cfd = new CFDCursor(new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(10), 100));
		Object[] cfdComp = Statistics.doubleArrayCFD(cfd);
		System.out.println("\nvalues:");
		System.out.println(java.util.Arrays.toString((double[]) cfdComp[0]));
		System.out.println("frequencies:");
		System.out.println(java.util.Arrays.toString((int[]) cfdComp[1]));

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		// compress the normal distribution with a spline
		Function gaussian = new AbstractFunction() {
			public Object invoke(Object x) {
				double x0 = ((Number) x).doubleValue();
				return new Double(Statistics.gaussian(x0));
			}
		};
		double[] grid = new double[7];
		for (int i = -3; i < 4; i++) {
			grid[i + 3] = i;
		}
		
		System.out.println("\ngaussian:\tapprox. with a spline");
		System.out.println(java.util.Arrays.toString(grid));
		System.out.println(java.util.Arrays.toString(Statistics.evalReal1DFunction ( grid, gaussian)) );
		
		Function spline = new RB1CubicBezierSpline(grid, Statistics.evalReal1DFunction(grid, gaussian));
		for (int i = -30; i < 31; i++) {
			double x = i / 10.0;
			System.out.println(
				"gaussian ("
					+ x
					+ ")="
					+ Statistics.gaussian(x)
					+ "\tspline("
					+ x
					+ ")="
					+ spline.invoke(new Double(x)));
		}
		
		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		// n maximum differences of a given data set and a distance function
		
		// used data for examples
		System.out.println ("\nused cfd");
		List data = Cursors.toList (new ReplacementSelection (
					new AggregateGrouper.CFDCursor(
						new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(10),500)),
						10,
						new Comparator(){
							public int compare ( Object o1, Object o2){
								return ((Comparable) ((Object[]) o2)[1]).compareTo (((Object[])o1)[1]);
							}
						}
					)
				);

		// debug
		Iterator debug = data.iterator();
		int pos = 0;
		while ( debug.hasNext() ){
			Object [] tuple = (Object []) debug.next();
			System.out.println ((pos++) + ": data >" + tuple[0] + "< has occurred " + tuple[1] + " times");
		}
		// end of debug

		int n = 6;
		System.out.println ("\n"+n+" biggest diffs in the data");
		System.out.println ( 
			java.util.Arrays.toString( 
					Statistics.maxDiff ( 
					data.iterator(),
					new Distance(){
					 	public double distance ( Object o1, Object o2){
					 		return Math.abs(
					 			((Number) ((Object[]) o1)[1]).doubleValue() -
					 			((Number) ((Object[]) o2)[1]).doubleValue());
					 	}
					}, n, true
				)
			)
		);
	}

}
