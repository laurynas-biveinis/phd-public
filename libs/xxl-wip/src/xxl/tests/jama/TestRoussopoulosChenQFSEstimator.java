package xxl.tests.jama;

import xxl.connectivity.jama.RoussopoulosChenQFSEstimator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RoussopoulosChenQFSEstimator.
 */
public class TestRoussopoulosChenQFSEstimator {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		// initializing parameters
		int degree = 2;
		int sizeGG = 100;
		double dmax = 50;
		double dmin = 0;
		int m = 20;
		double alpha = 0.75;
		double left = 2;
		double right = 15;

		System.out.println("Degree of the polynom: " + degree);
		System.out.println("Size of the entirety: " + sizeGG);
		System.out.println("Minimum of the values: " + dmin);
		System.out.println("Maximum of the values: " + dmax);
		System.out.println("Number of true selectivities: " + m);
		System.out.println("Used weight: " + alpha + "\n");
		System.out.println("Interval of the range query to estimate: " + "[" + left + "," + right + "]");

		RoussopoulosChenQFSEstimator qsfEst = new RoussopoulosChenQFSEstimator(degree, sizeGG, dmax, dmin, m, alpha);
		System.out.println(
			"Estimated range query selectivity: " + qsfEst.rangeQuery(new Double(left), new Double(right)));
	}

}
