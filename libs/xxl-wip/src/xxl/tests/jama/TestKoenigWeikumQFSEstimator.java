package xxl.tests.jama;

import xxl.connectivity.jama.KoenigWeikumQFSEstimator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class KoenigWeikumQFSEstimator.
 */
public class TestKoenigWeikumQFSEstimator {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		double[] val = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		int[] freq = { 7, 6, 5, 6, 10, 13, 12, 22, 25, 33 };
		int nob = 3;
		int noq = 3;
		Object query1 = new double[] { 1, 3 }; //13
		Object query2 = new double[] { 2, 7 }; //40
		Object query3 = new double[] { 5, 11 }; //173

		KoenigWeikumQFSEstimator estimator = new KoenigWeikumQFSEstimator(val, freq, nob, 1, noq);

		for (int i = 0; i < nob; i++) {
			System.out.println("Bucket:" + i);
			System.out.println("lower bound:" + estimator.vl[i]);
			System.out.println("upper bound:" + estimator.vh[i]);
			System.out.println("slope:" + estimator.b[i]);
			System.out.println("axis:" + estimator.a[i]);
			System.out.println("values:" + estimator.q[i]);
			System.out.println();
		} //end of for

		System.out.println("selectivity:" + estimator.estimate(query1, 13));
		System.out.println("selectivity:" + estimator.estimate(query2, 40));
		System.out.println("selectivity:" + estimator.estimate(query3, 173));

		System.out.println("selectivity:" + estimator.estimate(query1, 13));
		System.out.println("selectivity:" + estimator.estimate(query2, 40));
		System.out.println("selectivity:" + estimator.estimate(query3, 173));

		System.out.println("selectivity:" + estimator.estimate(query1, 13));
		System.out.println("selectivity:" + estimator.estimate(query2, 40));
		System.out.println("selectivity:" + estimator.estimate(query3, 173));

	}

}
