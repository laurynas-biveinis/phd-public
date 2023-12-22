/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.connectivity.jama;

import java.util.ArrayList;
import java.util.List;

import xxl.core.math.functions.Integrable;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.queries.QueryFeedbackSelectivityEstimator;
import xxl.core.math.queries.RangeQuery;
import Jama.Matrix;

/** This class provides a selectivity estimator for {@link xxl.core.math.queries.RangeQuery range queries}
 * based upon [RC94]:
 * Chen, Chungmin Melvin Chen. Roussopoulos, Nick. Adaptive Selectivity Estimation
 * Using Query Feedback. ACM Sigmod 1994. p. 161-172,
 * using feedback informations for tuning the selectivity estimation algorithm.
 *
 * @see xxl.core.math.queries.QueryFeedbackSelectivityEstimator
 * @see xxl.core.math.queries.RangeQuery
 */

public class RoussopoulosChenQFSEstimator extends QueryFeedbackSelectivityEstimator implements RangeQuery {

	/** Interface representing model 
	 * functions used for the estimation.
	 */
	public static interface ModelFunction extends RealFunction, Integrable {

		/** evaluates the model function at a given point x 
		 * 
		 * @param x point to evaluate
		 * @return value of the function at x 
		 */
		public abstract double eval(double x);

		/** returns the primitive of the model function 
		 * 
		 * @return the primitive of the function 
		 */
		public abstract RealFunction primitive();
	}

	/** Objects of this class implementing monoms in a mathematical sense.
	 * Objects of this class could be combined in a linear manner
	 * to obtain a polynomial of any degree. In [PC94]
	 * the authors say a polynomial of degree 6 has performed well as estimation
	 * function (using query feedback).
	 */
	public static class Monom implements ModelFunction {

		/** the degree of the monom */
		protected int degree;

		/** the scalar multiplicated with the monom */
		protected double skalar = 1.0;

		/** Constructs a new Object of this class as f(x) = scalar * x^degree.
		 * 
		 * @param degree the degree (power) of the monom
		 * @param skalar the scalar of the monom
		 */
		public Monom(int degree, double skalar) {
			this.degree = degree;
			this.skalar = skalar;
		}

		/** Constructs a new Object of this class as f(x) = 1.0 * x^degree.
		 * 
		 * @param degree the degree (power) of the monom
		 */
		public Monom(int degree) {
			this(degree, 1.0);
		}

		/** Evaluates the monom (model function) at a given point x.
		 * 
		 * @param x function argument
		 * @return the monom evaluated in x
		 */
		public double eval(double x) {
			return skalar * Math.pow(x, degree);
		}

		/** Returns the primitive of the model function.
		 * 
		 * @return the primitive of the monom
		 */
		public RealFunction primitive() {
			return new Monom(degree + 1, skalar / (degree + 1));
		}
	}

	/** used model function for estimation */
	protected ModelFunction[] mf;

	/** used coefficients of the linear combination of the model functions */
	protected double[] a;

	/** size of the the entirety */
	protected int sizeGG;

	/** minimum value of the entirety */
	protected double dmax;

	/** maximum value of the entirety */
	protected double dmin;

	/** adjusting step width, i.e. the number of queries to process before any
	 * any feedback informations could be used */
	protected int m;

	/** fading weight (for further information see the corresponding paper [PC94]) */
	protected double alpha;

	/** matrix used to compute the new coeffs */
	protected Matrix G;

	/** matrix corresponding to the coefficients for the linear combination of the model functions */
	protected Matrix A;

	/** Number of model functions used */
	protected int n; //degree of the polynomial

	/** temporal variable, used for adjusting the algorithm */
	protected double[][] S; //*temporal array, used at adjust now

	/** used weights */
	protected List<Double> betas;

	/** stores the number of adjusts already done */
	private int numberOfAdjusts;

	/** Constructs a new Object of this type and initializes the parameters.
	 *
	 * @param degree degree of the used model function (polynomial)
	 * @param sizeGG the size of the entirety
	 * @param dmax maximum value of the entirety
	 * @param dmin minimum value of the entirety
	 * @param m number of true selectivities used for adjusting the algorithm  
	 * @param alpha used weight
	 * @throws IllegalArgumentException if any given argument exceeds a valid range
	 */
	public RoussopoulosChenQFSEstimator(int degree, int sizeGG, double dmax, double dmin, int m, double alpha)
		throws IllegalArgumentException {

		this.sizeGG = sizeGG;
		this.dmax = dmax;
		this.dmin = dmin;
		this.m = m;
		this.alpha = alpha;
		n = degree;
		// validate parameters
		if ((alpha <= 0) || (alpha > 1))
			throw new IllegalArgumentException("alpha must be bigger than 0 and less or equal to 1");
		if (dmax <= dmin)
			throw new IllegalArgumentException("dmax must be bigger than dmin");
		if (m <= n)
			throw new IllegalArgumentException("m must be bigger than the degree of mf");
		//
		mf = new ModelFunction[degree + 1];
		for (int i = 0; i <= degree; i++) {
			mf[i] = new Monom(i); // alle mit 1.0 * x^i initialisieren
		}
		// 
		numberOfAdjusts = 0;
		// 
		betas = new ArrayList<Double>();

		double delta = dmax - dmin;
		// calculate left borders of the intervals to begin with
		double[] li = new double[n + 1];
		for (int i = 0; i < n; i++) {
			li[i] = dmin + (((double) i / (double) (n - 1)) * delta);
		}
		li[n] = dmin;
		// calculate right borders of the intervals to begin with
		double[] re = new double[n + 1];
		for (int i = 0; i < n; i++) {
			re[i] = dmin + (((double) i / (double) (n - 1)) * delta);
		}
		re[n] = dmax;
		// calculate the selectivities to begin with (estimated, an equal distribution will be presumed.
		double[] sel = new double[n + 1];
		for (int i = 0; i < n; i++) {
			sel[i] = (sizeGG / delta);
		}
		sel[n] = sizeGG;
		//construct a double[][] needed for adjusting the model function
		S = new double[m][n + 1];
		// construct double[][] M in which the matrix coefficients are written
		double[][] M = new double[n + 1][n + 1];
		//calculate the values of the matrix coefficients
		for (int i = 0; i < n + 1; i++) {
			for (int j = 0; j < n + 1; j++) {
				M[i][j] = mf[j].primitive().eval(re[i] + 1) - mf[j].primitive().eval(li[i]);
			}
		}
		//construct a matrix X from double[][]M
		Matrix X = new Matrix(M);
		//the transpose of X
		Matrix Xt = X.transpose();
		//the inversion of Xt*X which is the Gainmatrix G
		G = (Xt.times(X)).inverse();
		//construct a matrix Y from double[] sel
		Matrix Y = new Matrix(sel, n + 1);
		//Y.print(4,0);
		//calculate A the coeff.-matrix with the starting values
		A = G.times((Xt.times(Y)));
		//construct a double[] a with the values of A
		a = new double[n + 1];
		for (int i = 0; i < n + 1; i++) {
			a[i] = A.getArray()[i][0];
		}
	}

	/** Evaluates the model functions. I.e. the linear combination
	 * of the model functions will be evaluated as f(x) = sum_{i=0}^{n} a_i * phi_i (x).
	 * 
	 * @param x function argument
	 * @return function value
	 */
	protected double invokeModelFunction(double x) {
		double r = 0.0;
		for (int i = 0; i < n + 1; i++) {
			r += a[i] * mf[i].eval(x);
		}
		return r;
	}

	/** Evaluates the primitives of the model functions. I.e. the linear combination
	 * of the primitives of the model functions will be evaluated as
	 * f(x) = sum_{i=0}^{n} a_i * PHI_i (x) =
	 * = sum_{i=0}^{n} a_i * int {phi_i (x) dx}.
	 * 
	 * @param x function argument
	 * @return function value
	 */
	protected double invokeModelFunctionIntegral(double x) {
		double r = 0.0;
		for (int i = 0; i < n + 1; i++) {
			r += a[i] * mf[i].primitive().eval(x);
		}
		return r;
	}

	/**
	 * This method gives the user the possibility to set
	 * an importance weight for each query.
	 * If the invoke method is called without calling this method
	 * before, the importance weight for the query will be set to 1.
	 * 
	 * @param b importance weight for the last estimated query
	*/
	public void setBeta(double b) {
		if ((b <= 0) || (b > 1))
			throw new RuntimeException("beta must be in(0,1]");
		betas.add(new Double(b));
	}

	/** Performs an estimation of a given <tt>range query</tt>.
	 *
	 * @param a left object of the range to query (inclusively)
	 * @param b right object of the range to query (exclusively)	 
	 * @return a numerical value to be associated with the query
	 */
	public double rangeQuery(Object a, Object b) {
		return getSelectivity(new double[] {((Number) a).doubleValue(), ((Number) b).doubleValue()});
	}

	/**
	 * Gives the estimated selectivity (absolute) of a query (interval query)
	 * calculated with the estimation function. If the estimated selectivity
	 * is negative, it will return 0 (as in [PC94] mentioned).
	 *
	 * @param query	query to process (must be given as <tt>double []</> containing
	 * left and right border of the interval query to process
	 * @return estimated selectivity
	 */
	protected double getSelectivity(Object query) {

		double l = ((double[]) query)[0];
		double h = ((double[]) query)[1];
		double es = invokeModelFunctionIntegral(h + 1) - invokeModelFunctionIntegral(l);
		if (es < 0.0) {
			es = 0.0;
		}
		else {
			if (es > sizeGG) {
				es = sizeGG;
			}
		}
		//testing if setBeta was called. If not, set the weight for
		//this query to 1.
		if (betas.size() != alreadyProcessedQueries.size()) {
			betas.add(new Double(1));
		}
		// if the number of queries reached m: adjust. 
		if (alreadyProcessedQueries.size() == m)
			adjust();
		return es;
	}

	/**
	 * Adjusts the estimation function (processing the query feedback algorithm).
	 */
	protected void adjust() {
		numberOfAdjusts++;
		//calculate the values of the matrix coeff. and write them into a
		//double[][]S.
		for (int i = 0; i < m; i++) {
			double l = ((double[])alreadyProcessedQueries.get(i)[0])[0];
			double h = ((double[])alreadyProcessedQueries.get(i)[0])[1];
			for (int j = 0; j < n + 1; j++) {
				// berechnen der Werte an den linken und rechten Grenzen
				S[i][j] = mf[j].primitive().eval(h + 1) - mf[j].primitive().eval(l);
			}
		}

		// set all fading weights to 1 except the first one 1
		//this is set to alpha and write them into double[] alphatemp
		double[] alphatemp = new double[m];
		for (int i = 1; i < m; i++) {
			alphatemp[i] = 1;
		}
		alphatemp[0] = alpha;

		//construct a matrix X from double[][] S
		Matrix X = new Matrix(S);
		//construct a matrix Gnew needed for adjusting the Gainmatrix
		Matrix Gnew = new Matrix(n + 1, n + 1);
		//construct a matrix Anew needed for adjusting the coeff.s
		Matrix Anew = new Matrix(n + 1, 1);
		//get the array with the real selectivities from collect
		double[] select = new double[m];
		for (int i = 0; i < m; i++) {
			select[i] = ((Double)alreadyProcessedQueries.get(i)[1]).doubleValue();
		}
		//construct a matrix Y from double[] select
		Matrix Y = new Matrix(select, m);
		//the adjusting: alle m Anfragen nach und nach auswerten
		for (int i = 0; i < m; i++) {
			//importance weights
			double beta = 1.0;
			betas.get(i);
			// provide alpha as Matrix for computations: 1 for all alpha excluding the first one
			Matrix alphai = new Matrix(1, 1, alphatemp[i]);
			//construct a matrix betai from the i-ths importance weight
			Matrix betai = new Matrix(1, 1, beta);
			//i-th row of the matrix Xi
			Matrix Xi = X.getMatrix(i, i, 0, n);
			//the transpose of the i-th row of the matrix Xi
			Matrix Xit = Xi.transpose();
			//si = real selectivity of the i-ths query
			Matrix si = Y.getMatrix(i, i, 0, 0);
			//calculate the new Gainmatrix
			//Gnew = G - (G*Xit) * ((1+Xi*G*Xit)^(-1)) * (Xi*G)
			//without weights:
			//Gnew = G.minus ( ((G.times(Xit)).times ((one.plus( (Xi).times( G.times(Xit)))).inverse())).times(Xi.times(G)));
			//with weights:
			//Gnew =		(G	   *	  (1/ alphatemp^2) )			  -
			Gnew = (G.times(((1.0 / alphatemp[i]) * (1.0 / alphatemp[i])))).minus
				//			(G *  Xit)	 *
			 ((((G.times(Xit)).times
						//		   ( (alphai^2)			 +			(Xi * G * Xit)		*
			 (((alphai.times(alphai)).plus(((Xi).times(G.times(Xit))).times
						//			betai^2	 )	^-1					*	(Xi * G)	 *
			 (betai.times(betai)))).inverse())).times(Xi.times(G))).times
						//		betai^2 / alphatemp^2
			 (((beta * beta) / (alphatemp[i] * alphatemp[i]))));

			//calculate the new coeff. matrix A
			//Anew = A - ((Gnew*Xit) * (Xi*A - si))
			//without weights:
			//Anew = A.minus ((Gnew.times(Xit)).times( (Xi.times(A)).minus(si)));
			//with weights:
			//Anew = A	-	  Gnew * Xit		*( Xi * a	- si)
			Anew = A.minus(((Gnew.times(Xit)).times((Xi.times(A)).minus(si)))
				//		 *	betai^2
			.times(beta * beta));
					G = Gnew.copy();
					A = Anew.copy();
		}
		//write the coeff.s of A into a double[] a
		for (int k = 0; k < n + 1; k++) {
			a[k] = A.getArray()[k][0];
		}
		//remove all elements from the vectors		 
		betas.clear();
		alreadyProcessedQueries.clear();
	}

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