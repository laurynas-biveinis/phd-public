package xxl.tests.util;

import xxl.core.util.Interval1D;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Interval1D.
 */
public class TestInterval1D {

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
		Interval1D interval1 = new Interval1D(new Integer(-20), true, new Integer(20), false);
		Interval1D interval2 = new Interval1D(new Integer(1), new Integer(30));
		System.out.println("Interval1:");
		System.out.println("\tleft border: " +interval1.border(false));
		System.out.println("\tleft border included? " +interval1.includes(false));
		System.out.println("\tright border: " +interval1.border(true));
		System.out.println("\tright border included? " +interval1.includes(true));
		System.out.println("Printed directly to output stream: " +interval1 +"\n");
		System.out.println("Interval2:");
		System.out.println("\tleft border: " +interval2.border(false));
		System.out.println("\tleft border included? " +interval2.includes(true));
		System.out.println("\tright border: " +interval2.border(true));
		System.out.println("\tright border included? " +interval2.includes(true));
		System.out.println("Printed directly to output stream: " +interval2 +"\n");
		System.out.println("Are the intervals equal? " +interval1.equals(interval2));
		System.out.println("Do the intervals overlap? " +interval1.overlaps(interval2));
		System.out.println("Does interval1 contain interval2? " +interval1.contains(interval2));
		System.out.println("Interval3 gets a clone of interval1.");
		Interval1D interval3 = (Interval1D)interval1.clone();
		System.out.println("Union of interval1 and interval2: " +interval1.union(interval2));
		System.out.println("Intersection of interval3 and interval2: " +interval3.intersect(interval2));
	}

}
