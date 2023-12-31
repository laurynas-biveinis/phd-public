/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.random;

import xxl.core.math.functions.RealFunction;

/** The class provides a random number generator delivering random numbers distributed
 * like a user-defined function G. <BR>
 *
 * If G is a continuous distribution with inverse G^-1, and u is a uniform random number ~ U[0,1],
 * i.e., unifomly distributed in the interval [0,1],
 * then F^-1(u) has distribution F. So, one is able to obtain user-defined distributed random numbers
 * by using a PRNG delivering uniformly distributed random numbers and the inversal function of the
 * cdf.
 * If no analytic form could be given of the inversal function of the user-defined distribution, a
 * {@link RejectionDistributionBasedPRNG Rejection-Based} algorithm could be used.
 */

public class InversionDistributionBasedPRNG implements ContinuousRandomWrapper{

	/** the inversal function of the cdf G */
	protected RealFunction inverseCDF;

	/** PRNG delivering uniformly distributed pseudo random numbers between [0,1]*/
	protected ContinuousRandomWrapper crw;

	/** Creates a new Object of this type.
	 *
	 * @param crw PRNG delivering uniform distributed continuous random numbers, i.e., ~ U[0,1] 
	 * @param inverseCDF the inverse function G^-1 of a cdf (cumulative distribution function) G,
	 * if the delivered numbers are ~ G
	 *
	 */
	public InversionDistributionBasedPRNG( ContinuousRandomWrapper crw, RealFunction inverseCDF){
		this.crw = crw;
		this.inverseCDF = inverseCDF;
	}

	/** Return the next computed pseudo random number x with x ~ G regarding to the inversal function G^-1.
	 * @return the next computed pseudo random number x with x ~ G regarding to the inversal function G^-1.
	 */
	public double nextDouble(){
		double u = crw.nextDouble();
		double x = inverseCDF.eval( u);
		return x;
	}
}
