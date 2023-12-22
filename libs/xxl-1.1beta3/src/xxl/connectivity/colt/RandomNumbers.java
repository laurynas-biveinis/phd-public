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

package xxl.connectivity.colt;

import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.functions.RealFunctions;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.random.DiscreteRandomWrapper;
import xxl.core.util.random.InversionDistributionBasedPRNG;
import xxl.core.util.random.RejectionDistributionBasedPRNG;
import cern.jet.random.Binomial;
import cern.jet.random.Exponential;
import cern.jet.random.Normal;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/** This class provides some static methods to obtain pseudo random numbers
 * of different distributions using the
 * <a href="http://tilde-hoschek.home.cern.ch/hoschek/colt/index.htm">colt library</a>
 * with the {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
 * PRNG (pseudo random number generator).
 */

public class RandomNumbers {

	/** used random seed to initialize the colt PRNGs */
	protected static java.util.Random RANDOM = new java.util.Random(System.currentTimeMillis());

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private RandomNumbers() {}

	/*********************************************************************/
	/*                            Continuous                             */
	/*********************************************************************/

	/** Returns an {@link java.util.Iterator iterator} delivering standard
	 * normal distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator).
	 * <br><b>Note</b>:<br>
	 * The returned {@link java.util.Iterator iterator} delivers an infinite set
	 * of objects of type <tt>Double</tt>, so <b>don't</b> call
	 * <code>Cursors.last( RandomNumbers.gaussion())</code>.
	 * 
	 * @return an {@link java.util.Iterator iterator} delivering an infinity set of 
	 * standard normal distributed random numbers
	 */
	public static ContinuousRandomNumber gaussian() {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(
				new Normal(
					0.0, 
					1.0, 
					new MersenneTwister(RANDOM.nextInt())
				)
			)
		);
	}

	/** Returns an {@link java.util.Iterator iterator} delivering
	 * normal distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator).
	 * <br><b>Note</b>:<br>
	 * The returned {@link java.util.Iterator iterator} delivers an infinite set
	 * of objects of type <tt>Double</tt>, so <b>don't</b> call
	 * <code>Cursors.last( RandomNumbers.gaussion())</code>.
	 * 
	 * @param mean the mean of the normal distribution
	 * @param standardDeviation the standard deviation of the normal distribution
	 * @return an {@link java.util.Iterator iterator} delivering an infinity set of 
	 * standard normal distributed random numbers
	 */
	public static ContinuousRandomNumber gaussian(double mean, double standardDeviation) {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(
				new Normal(mean, standardDeviation, new MersenneTwister(RANDOM.nextInt()))
			)
		);
	}

	/** Returns an {@link java.util.Iterator iterator} delivering
	 * a given number of normal distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator).
	 * 
	 * @param mean the mean of the normal distribution
	 * @param standardDeviation the standard deviation of the normal distribution
	 * @param numberOfElements number of delivered pseudo random numbers
	 * @return an {@link java.util.Iterator iterator} delivering a given number of 
	 * normal distributed random numbers
	 */
	public static ContinuousRandomNumber gaussian(double mean, double standardDeviation, long numberOfElements) {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(	
				new Normal(
					mean, 
					standardDeviation, 
					new MersenneTwister(RANDOM.nextInt())
				)
			),
			numberOfElements
		);
	}

	/** Returns an {@link java.util.Iterator iterator} delivering
	 * a given number of normal distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator) initialized with the given seed.
	 * This method can be used to generate a sequence of random numbers repetitive.
	 * 
	 * @param mean the mean of the normal distribution
	 * @param standardDeviation the standard deviation of the normal distribution
	 * @param numberOfElements number of delivered pseudo random numbers
	 * @param seed used seed to initialize the PRNG
	 * @return an {@link java.util.Iterator iterator} delivering a given number of 
	 * normal distributed random numbers
	 */
	public static ContinuousRandomNumber gaussian(
		double mean,
		double standardDeviation,
		long numberOfElements,
		int seed) {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(new Normal(mean, standardDeviation, new MersenneTwister(seed))),
			numberOfElements);
	}

	/** Returns an {@link java.util.Iterator iterator} delivering
	 * exponential distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator).
	 * <br><b>Note</b>:<br>
	 * The returned {@link java.util.Iterator iterator} delivers an infinite set
	 * of objects of type <tt>Double</tt>, so <b>don't</b> call
	 * <code>Cursors.last( RandomNumbers.exponential())</code>.
	 * @param lambda the lambda parameter (1/mean) of the exponential distribution
	 * @return an {@link java.util.Iterator iterator} delivering an infinity set of 
	 * exponential distributed random numbers
	 */
	public static ContinuousRandomNumber exponential(double lambda) {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(new Exponential(lambda, new MersenneTwister(RANDOM.nextInt()))));
	}

	/** Returns an {@link java.util.Iterator iterator} delivering
	 * a given number of exponential distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator).
	 * 
	 * @param lambda the lambda parameter (1/mean) of the exponential distribution
	 * @param numberOfElements number of delivered pseudo random numbers
	 * @return an {@link java.util.Iterator iterator} delivering a given number of 
	 * exponential distributed random numbers
	 */
	public static ContinuousRandomNumber exponential(double lambda, long numberOfElements) {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(new Exponential(lambda, new MersenneTwister(RANDOM.nextInt()))),
			numberOfElements);
	}
	
	/** Returns an {@link java.util.Iterator iterator} delivering
	 * a given number of uniformly continuous distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator).
	 * 
	 * @param min lower bound
	 * @param max upper bound
	 * @param numberOfElements number of delivered pseudo random numbers
	 * @return an {@link java.util.Iterator iterator} delivering a given number of 
	 * uniformly continuous distributed random numbers
	 */
	public static ContinuousRandomNumber cuniform(double min, double max, long numberOfElements) {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(new Uniform(min, max, new MersenneTwister(RANDOM.nextInt()))),
			numberOfElements);
	}

	/** Returns an {@link java.util.Iterator iterator} delivering
	 * an infinity set of uniformly continuous distributed random numbers using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * PRNG (pseudo random number generator).
	 * <br><b>Note</b>:<br>
	 * The returned {@link java.util.Iterator iterator} delivers an infinite set
	 * of objects of type <tt>Double</tt>, so <b>don't</b> call
	 * <code>Cursors.last( RandomNumbers.cuniform())</code>.
	 * 
	 * @param min lower bound
	 * @param max upper bound
	 * @return an {@link java.util.Iterator iterator} delivering an infinite set of 
	 * uniformly continuous distributed random numbers
	 */
	public static ContinuousRandomNumber cuniform(double min, double max) {
		return new ContinuousRandomNumber(
			new ColtContinuousRandomWrapper(new Uniform(min, max, new MersenneTwister(RANDOM.nextInt()))));
	}

	
	/*********************************************************************/
	/*                            Discrete                               */
	/*********************************************************************/

	// Binomial

	/** Provides binomial distributed random elements.
	 * 
	 * @param n possible values k=0,...,n
	 * @param p binomial probability
	 * @return cursor delivering binomial distributed random elements
	 */
	public static DiscreteRandomNumber binomial(int n, double p) {
		return new DiscreteRandomNumber(
			new ColtDiscreteRandomWrapper(new Binomial(n, p, new MersenneTwister(RANDOM.nextInt()))));
	}

	/** Provides a given number of binomial distributed random elements.
	 * 
	 * @param n possible values k=0,...,n
	 * @param p binomial probability
	 * @param numberOfElements number of elements
	 * @return cursor delivering binomial distributed random elements
	 */
	public static DiscreteRandomNumber binomial(int n, double p, long numberOfElements) {
		return new DiscreteRandomNumber(
			new ColtDiscreteRandomWrapper(new Binomial(n, p, new MersenneTwister(RANDOM.nextInt()))),
			numberOfElements);
	}

	// Poisson

	
	/** Provides poisson distributed random elements.
	 * 
	 * @param lambda parameter lambda of the poisson distribution 
	 * @return cursor delivering binomial distributed random elements
	 */
	public static DiscreteRandomNumber poisson(double lambda) {
		return new DiscreteRandomNumber(
			new ColtDiscreteRandomWrapper(new Poisson(lambda, new MersenneTwister(RANDOM.nextInt()))));
	}

	/** Provides a given number of poisson distributed random elements.
	 * 
	 * @param lambda parameter lambda of the poisson distribution
	 * @param numberOfElements number of elements 
	 * @return cursor delivering binomial distributed random elements
	 */
	public static DiscreteRandomNumber poisson(double lambda, long numberOfElements) {
		return new DiscreteRandomNumber(
			new ColtDiscreteRandomWrapper(new Poisson(lambda, new MersenneTwister(RANDOM.nextInt()))),
			numberOfElements);
	}
	
	/** Provides a given number of poisson distributed random elements.
	 * 
	 * @param lambda parameter lambda of the poisson distribution
	 * @param numberOfElements number of elements
	 * @param seed the seed
	 * @return cursor delivering binomial distributed random elements
	 */
	public static DiscreteRandomNumber poisson(double lambda, long numberOfElements, int seed ) {
		return new DiscreteRandomNumber(
			new ColtDiscreteRandomWrapper(new Poisson(lambda, new MersenneTwister(seed))),
			numberOfElements);
	}
	
	// discrete uniform distribution

	/** Provides uniformly discrete distributed random numbers from min to max, i.e., from
	 * the closed interval [min, max] including min and max.
	 * 
	 * @param min lower bound
	 * @param max upper bound
	 * @return cursor delivering uniformly distributed discrete random numbers
	 */
	public static DiscreteRandomNumber duniform(final int min, final int max) {
		final Uniform uniform = new Uniform(new MersenneTwister(RANDOM.nextInt()));
		return new DiscreteRandomNumber(
			new DiscreteRandomWrapper() {
				Uniform u = uniform;
				int minInt = min;
				int maxInt = max;

				public int nextInt() {
					return u.nextIntFromTo(minInt, maxInt);
				}
			}
		);
	}

	/**
	 * Provides a given number of uniformly discrete distributed random numbers from min to max, i.e., from
	 * the closed interval [min, max] including min and max.
	 * 
	 * @param min lower bound
	 * @param max upper bound
	 * @param numberOfElements Number of elements of the cursor
	 * @return cursor delivering uniformly distributed discrete random numbers
	 */
	public static DiscreteRandomNumber duniform(final int min, final int max, long numberOfElements) {
		final Uniform uniform = new Uniform(new MersenneTwister(RANDOM.nextInt()));
		return new DiscreteRandomNumber(
			new DiscreteRandomWrapper() {
				Uniform u = uniform;
				int minInt = min;
				int maxInt = max;
	
				public int nextInt() {
					return u.nextIntFromTo(minInt, maxInt);
				}
			}, 
			numberOfElements
		);
	}

	/**
	 * Provides a given number of uniformly discrete distributed numbers from min to max, i.e from
	 * the closed interval [min, max] including min and max. Additionally, the PRNG is initialized 
	 * with a given seed.
	 * 
	 * @param min lower bound
	 * @param max upper bound
	 * @param numberOfElements Number of elements of the cursor
	 * @param seed used seed to initialize the PRNG
	 * @return cursor delivering uniformly distributed discrete random numbers
	 */
	public static DiscreteRandomNumber duniform(final int min, final int max, long numberOfElements, int seed) {
		final Uniform uniform = new Uniform(new MersenneTwister(seed));
		return new DiscreteRandomNumber(
			new DiscreteRandomWrapper() {
				Uniform u = uniform;
				int minInt = min;
				int maxInt = max;
	
				public int nextInt() {
					return u.nextIntFromTo(minInt, maxInt);
				}
			}, 
			numberOfElements
		);
	}

	/* user defined random numbers */

	/** Provides random numbers following the CP1-distribution.
	 * 
	 * @see xxl.core.math.functions.RealFunctions#pdfCP01()
	 * @param numberOfElements number of elements
	 * @param seed1 seed for the inversion distribution based PRNG
	 * @param seed2 seed for the rejection distribution based PRNG
	 * @return cursor delivering CP1 distributed random numbers
	 */
	public static ContinuousRandomNumber cp01(long numberOfElements, int seed1, int seed2) {
		RealFunction f = RealFunctions.pdfCP01();
		RealFunction g = RealFunctions.pdfCont02();
		RealFunction G_inv = RealFunctions.invDistCont02();
		double c = 2.4;
		ContinuousRandomWrapper u_rnd1 = new ColtContinuousRandomWrapper(seed1);
		ContinuousRandomWrapper u_rnd2 = new ColtContinuousRandomWrapper(seed2);
		ContinuousRandomWrapper g_rnd = new InversionDistributionBasedPRNG(u_rnd1, G_inv);
		RejectionDistributionBasedPRNG rb = new RejectionDistributionBasedPRNG(u_rnd2, f, g, c, g_rnd);
		return new ContinuousRandomNumber(rb, numberOfElements);
	}

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		ContinuousRandomNumber randomnumbers1 = gaussian(2.0, 1.0, 500);
		long i = 0;
		System.out.println("- gaussian distribution ------------------");
		while (randomnumbers1.hasNext()) {
			System.out.println((i++) + "\t" + randomnumbers1.next());
		}
		System.out.println("---------------------------------------");
		randomnumbers1.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		DiscreteRandomNumber randomnumbers2 = binomial(20, 0.4, 500);
		i = 0;
		System.out.println("- binomial distribution ---------------");
		while (randomnumbers2.hasNext()) {
			System.out.println((i++) + "\t" + randomnumbers2.next());
		}
		System.out.println();
		System.out.println("---------------------------------------");
		randomnumbers2.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		DiscreteRandomNumber randomnumbers3 = duniform(1, 100, 1000);
		i = 0;
		System.out.println("- discrete uniform distribution -------");
		while (randomnumbers3.hasNext()) {
			System.out.println((i++) + "\t" + randomnumbers3.next());
		}
		System.out.println();
		System.out.println("---------------------------------------");
		randomnumbers3.close();

		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		ContinuousRandomNumber randomnumbers4 = cuniform(0.0, 1.0);
		i = 0;
		System.out.println("- continuous uniform distribution -------");
		while (i < 100) {
			System.out.println((i++) + "\t" + randomnumbers4.next());
		}
		System.out.println();
		System.out.println("---------------------------------------");
		randomnumbers4.close();
	}
}