package xxl.tests.util;

import xxl.core.util.ArrayResizer;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ArrayResizer.
 */
public class TestArrayResizer extends ArrayResizer {

	/**
	 * Creates a new ArrayResizer.
	 */
	public TestArrayResizer() {
		super();
	}
	
	/**
	 * Creates a new ArrayResizer.
	 *
	 * @param fmin is the minimal utilization of capacity concerning the ratio
	 *        between logical and physical size after an expansion or
	 *        contraction of an array.
	 */
	public TestArrayResizer(double fmin) {
		super(fmin);
	}

	/**
	 * Creates a new ArrayResizer.
	 *
	 * @param fmin is the minimal utilization of capacity concerning the ratio
	 *        between logical and physical size after an expansion or
	 *        contraction of an array.
	 * @param fover a parameter specifying the array's logical size in
	 *        proportion to the array's physical size after an expansion.
	 * @param funder a parameter specifying the array's logical size in
	 *        proportion to the array's physical size after a contraction.
	 */
	public TestArrayResizer(double fmin, double fover, double funder) {
		super(fmin, fover, funder);
	}
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*************************************************************************/
		/*                              Example 1                                */
		/*************************************************************************/
		System.out.println("------------------EXAMPLE 1-------------------");
		TestArrayResizer arrayResizer = new TestArrayResizer();			// get a default instance
		int[] ints = new int[10];										// use a primitive type array
		int size = 20;													// minimal needed size (logical size) --> grow
		int[] resizedArray = (int[])arrayResizer.resize(ints, size);	// resize-call
		System.out.println("initial array size:              " + ints.length);
		System.out.println("logical size:                    " + size);
		System.out.println("physical size:                   " + resizedArray.length + " (returned size!)");
		System.out.println("fover:                           " + arrayResizer.fover);
		System.out.println("funder:                          " + arrayResizer.funder);
		System.out.println("fmin:                            " + arrayResizer.fmin);
		System.out.println("logical size * fover:            " + size*arrayResizer.fover);
		System.out.println("logical size * funder:           " + size*arrayResizer.funder);
		System.out.println("logical size * fmin:             " + size*arrayResizer.fmin);
		System.out.println("physical size * fover:           " + resizedArray.length*arrayResizer.fover);
		System.out.println("physical size * funder:          " + resizedArray.length*arrayResizer.funder);
		System.out.println("physical size * fmin:            " + resizedArray.length*arrayResizer.fmin);
		System.out.println("NOTE the following conditions:");
		System.out.println("general:          logical size * fmin <= physical size * fmin <= logical size");
		System.out.println("grow-operation:   logical size * fover <= physical size * fover <= logical size");
		System.out.println("shrink-operation: logical size * funder <= physical size * funder <= logical size");
		System.out.println();

		/*************************************************************************/
		/*                              Example 2                                */
		/*************************************************************************/
		System.out.println("------------------EXAMPLE 2-------------------");
		arrayResizer = new TestArrayResizer(0.2);						// create new ArrayResizer with fmin = 0.2
		ints = new int[100];											// use a primitive type array
		size = 10;														// minimal needed size (logical size) --> shrink
		resizedArray = (int[])arrayResizer.resize(ints, size);			// resize-call
		System.out.println("initial array size:" + ints.length);
		System.out.println("logical size:              " + size);
		System.out.println("physical size:             " + resizedArray.length + " (returned size!)");
		System.out.println("fover:                     " + arrayResizer.fover);
		System.out.println("funder:                    " + arrayResizer.funder);
		System.out.println("fmin:                      " + arrayResizer.fmin);
		System.out.println("logical size * fover:      " + size*arrayResizer.fover);
		System.out.println("logical size * funder:     " + size*arrayResizer.funder);
		System.out.println("logical size * fmin:       " + size*arrayResizer.fmin);
		System.out.println("physical size * fover:     " + resizedArray.length*arrayResizer.fover);
		System.out.println("physical size * funder:    " + resizedArray.length*arrayResizer.funder);
		System.out.println("physical size * fmin:      " + resizedArray.length*arrayResizer.fmin);
		System.out.println("NOTE the following conditions:");
		System.out.println("general:          logical size * fmin <= physical size * fmin <= logical size");
		System.out.println("grow-operation:   logical size * fover <= physical size * fover <= logical size");
		System.out.println("shrink-operation: logical size * funder <= physical size * funder <= logical size");
		System.out.println();

		/*************************************************************************/
		/*                              Example 3                                */
		/*************************************************************************/
		System.out.println("------------------EXAMPLE 3-------------------");
		arrayResizer = new TestArrayResizer(0.2, 0.6, 0.8);				// create new ArrayResizer with fmin = 0.2, fover = 0.6 and funder = 0.8
		Integer[] objects = new Integer[] {0, 1, 2};					// use an Object array
		size = 10;														// minimal needed size (logical size) --> grow
		Integer[] resizedObjectArray = arrayResizer.resize(objects, size); // resize-call
		System.out.println("initial array size:        " + objects.length);
		System.out.println("logical size:              " + size);
		System.out.println("physical size:             " + resizedObjectArray.length + " (returned size!)");
		System.out.println("fover:                     " + arrayResizer.fover);
		System.out.println("funder:                    " + arrayResizer.funder);
		System.out.println("fmin:                      " + arrayResizer.fmin);
		System.out.println("logical size * fover:      " + size*arrayResizer.fover);
		System.out.println("logical size * funder:     " + size*arrayResizer.funder);
		System.out.println("logical size * fmin:       " + size*arrayResizer.fmin);
		System.out.println("physical size * fover:     " + resizedArray.length*arrayResizer.fover);
		System.out.println("physical size * funder:    " + resizedArray.length*arrayResizer.funder);
		System.out.println("physical size * fmin:      " + (int)(resizedObjectArray.length*arrayResizer.fmin));
		System.out.println("NOTE the following conditions:");
		System.out.println("general:          logical size * fmin <= physical size * fmin <= logical size");
		System.out.println("grow-operation:   logical size * fover <= physical size * fover <= logical size");
		System.out.println("shrink-operation: logical size * funder <= physical size * funder <= logical size");
		System.out.println();
	}

}
