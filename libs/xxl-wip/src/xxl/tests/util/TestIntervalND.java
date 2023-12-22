package xxl.tests.util;

import xxl.core.util.Interval1D;
import xxl.core.util.IntervalND;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class IntervalND.
 */
public class TestIntervalND {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main (String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		IntervalND interval1 = new IntervalND (new Interval1D[] {
			new Interval1D (new Integer(-25), true, new Integer(25), false),
			new Interval1D (new Character('a'), new Character('z')),
			new Interval1D (new Float(0.75), new Float(13.125)),
		});

		IntervalND interval2 = new IntervalND (new Interval1D[] {
			new Interval1D (new Integer(-10), new Integer(10)),
			new Interval1D (new Character('e'), false, new Character('u'), false),
			new Interval1D (new Float(-1.25), true, new Float(7.625), false),
		});

		System.out.println("interval1: " +interval1);
		System.out.println("interval2: " +interval2 +"\n");
		System.out.println("Are the intervals equal? " +interval1.equals(interval2));
		System.out.println("Do the intervals overlap? " +interval1.overlaps(interval2));
		System.out.println("Does interval1 contain interval2? " +interval1.contains(interval2));
		System.out.println("Interval3 gets a clone of interval1.");
		IntervalND interval3 = (IntervalND)interval1.clone();
		System.out.println("Union of interval1 and interval2: " +interval1.union(interval2));
		System.out.println("Intersection of interval3 and interval2: " +interval3.intersect(interval2));
	}

}
