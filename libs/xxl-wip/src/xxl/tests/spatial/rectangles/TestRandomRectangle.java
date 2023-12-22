package xxl.tests.spatial.rectangles;

import xxl.core.spatial.rectangles.RandomRectangle;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RandomRectangle.
 */
public class TestRandomRectangle {

	/** 
	 *  Test main method
	 *	create 1000 random rectangles and print them to standard out
	 *  @param args array of <tt>String</tt> arguments - is not used here
	 */
	public static void main(String[] args){

		RandomRectangle rr = new RandomRectangle(0.1, 2, 1000);

		while(rr.hasNext()){
			System.out.println(rr.next());
		}
	}

}
