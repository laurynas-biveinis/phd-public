package xxl.tests.cursors.intersections;

import java.util.Iterator;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.intersections.NestedLoopsIntersection;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NestedLoopsIntersection.
 */
public class TestNestedLoopsIntersection {
	
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
		
		System.out.println("Example 1:");
		
		NestedLoopsIntersection<Integer> intersection = new NestedLoopsIntersection<Integer>(
			java.util.Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10).iterator(),
			java.util.Arrays.asList(0, 2, 4, 6, 8, 10).iterator(),
			new AbstractFunction<Object, Iterator<Integer>>() {
				public Iterator<Integer> invoke() {
					return java.util.Arrays.asList(0, 2, 4, 6, 8, 10).iterator();
				}
			}
		);
		
		intersection.open();
		
		Cursors.println(intersection);
		
		intersection.close();
		
		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		System.out.println("\nExample 2:");
		
		intersection = new NestedLoopsIntersection<Integer>(
			java.util.Arrays.asList(2, 2).iterator(),
			java.util.Arrays.asList(2, 2, 2).iterator(),
			new AbstractFunction<Object, Iterator<Integer>>() {
				public Iterator<Integer> invoke() {
					return java.util.Arrays.asList(2, 2, 2).iterator();
				}
			}
		);
		
		intersection.open();
		
		Cursors.println(intersection);
		
		intersection.close();
	}

}
