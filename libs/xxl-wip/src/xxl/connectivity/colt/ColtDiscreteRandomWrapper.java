/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.colt;

import xxl.core.util.random.DiscreteRandomWrapper;
import cern.jet.random.AbstractDiscreteDistribution;
import cern.jet.random.AbstractDistribution;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import edu.cornell.lassp.houle.RngPack.Ranecu;
import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.cornell.lassp.houle.RngPack.Ranmar;

/** This class provides a wrapper for using any
 * {@link edu.cornell.lassp.houle.RngPack.RandomElement random element}
 * given from the
 * <a href="http://tilde-hoschek.home.cern.ch/hoschek/colt/index.htm">colt library</a>
 * with xxl.
 * If no random engine is given the
 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twiseter} PRNG
 * will be used. Furthermore, this wrapper enables the user
 * to assign a {@link cern.jet.random.AbstractDiscreteDistribution discrete distribution}
 * the generated random numbers should follow.
 * Per default the discrete uniform distribution of the interval
 * [Integer.MIN_VALUE, Integer_MAXVALUE] is
 * used.
 */

public class ColtDiscreteRandomWrapper implements DiscreteRandomWrapper {

	/** This class uses the
	 * {@link cern.jet.random.Uniform continuous uniform distribution}
	 * shipped with colt to provide a discrete uniform distribution.
	 */
	public static class DiscreteUniform extends AbstractDiscreteDistribution {

		/** internally used continuous uniform distribution to build a
		 * discrete uniform distribution */
		protected Uniform uniform;

		/** lower border of the interval the generated random numbers belong to */
		protected int from;

		/** upper border of the interval the generated random numbers belong to */
		protected int to;

		/** Constructs a new Object of this type.
		 * 
		 * @param from lower border of the interval the generated random numbers belong to
		 * @param to upper border of the interval the generated random numbers belong to
		 * @param randomGenerator used random engine
		 */
		public DiscreteUniform(int from, int to, RandomElement randomGenerator) {
			uniform = new Uniform(randomGenerator);
			this.from = from;
			this.to = to;
		}

		/** Constructs a new Object of this class using the
		 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister} PRNG
		 * by default.
		 * 
		 * @param from lower border of the interval the generated random numbers belong to
		 * @param to upper border of the interval the generated random numbers belong to
		 */
		public DiscreteUniform(int from, int to) {
			this(from, to, new MersenneTwister(RandomNumbers.RANDOM.nextInt()));
		}

		/** Constructs a new Object of this class computing
		 * random numbers of the interval [0, Integer_MAXVALUE].
		 * 
		 * @param randomGenerator used random engine
		 */
		public DiscreteUniform(RandomElement randomGenerator) {
			this(0, Integer.MAX_VALUE, randomGenerator);
		}

		/** Constructs a new Object of this class using the
		 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister} PRNG
		 * computing
		 * random numbers of the interval [0, Integer_MAXVALUE].
		 */
		public DiscreteUniform() {
			this(0, Integer.MAX_VALUE);
		}

		/** Returns the next computed random number.
		 * 
		 * @return the next computed random number
		 */
		@Override
		public int nextInt() {
			return uniform.nextIntFromTo(from, to);
		}
	}

	/** Gives a {@link xxl.core.util.random.ContinuousRandomWrapper ContinuousRandomWrapper}
	 * based upon the {@link edu.cornell.lassp.houle.RngPack.Ranlux Ranlux}
	 * PRNG shipped with colt.
	 */
	public static DiscreteRandomWrapper RANLUX_BASED =
		new ColtDiscreteRandomWrapper(new Ranlux(RandomNumbers.RANDOM.nextInt()));

	/** Gives a {@link xxl.core.util.random.ContinuousRandomWrapper ContinuousRandomWrapper}
	 * based upon the {@link edu.cornell.lassp.houle.RngPack.Ranecu Ranecu}
	 * PRNG shipped with colt.
	 */
	public static DiscreteRandomWrapper RANECU_BASED =
		new ColtDiscreteRandomWrapper(new Ranecu(RandomNumbers.RANDOM.nextInt()));

	/** Gives a {@link xxl.core.util.random.ContinuousRandomWrapper ContinuousRandomWrapper}
	 * based upon the {@link edu.cornell.lassp.houle.RngPack.Ranmar Ranmar}
	 * PRNG shipped with colt.
	 */
	public static DiscreteRandomWrapper RANMAR_BASED =
		new ColtDiscreteRandomWrapper(new Ranmar(RandomNumbers.RANDOM.nextInt()));

	/** used discrete distribution */
	protected AbstractDistribution distribution;

	/** Constructs a new Object of this class using the given distribution
	 * to compute random numbers.
	 * 
	 * @param distribution discrete distribution used for computing random numbers
	 */
	public ColtDiscreteRandomWrapper(AbstractDiscreteDistribution distribution) {
		this.distribution = distribution;
	}

	/** Constructs a new Object of this class using the given
	 * random engine.
	 * 
	 * @param randomElement random engine used
	 */
	public ColtDiscreteRandomWrapper(RandomElement randomElement) {
		this(new DiscreteUniform(randomElement));
	}

	/** Constructs a new Object of this class using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister} PRNG
	 * by default.
	 * 
	 * @param seed used seed for intitializing the PRNG
	 */
	public ColtDiscreteRandomWrapper(int seed) {
		this(new DiscreteUniform(new MersenneTwister(seed)));
	}

	/** Constructs a new Object of this class using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister} PRNG
	 * by default.
	 */
	public ColtDiscreteRandomWrapper() {
		this(new DiscreteUniform(new MersenneTwister(RandomNumbers.RANDOM.nextInt())));
	}

	/** Computes the next element of the given distribution.
	 * If no distribution is given the
	 * {@link DiscreteUniform discrete uniform distribution} is used.
	 * 
	 * @return the next computed random number
	 */
	public int nextInt() {
		return distribution.nextInt();
	}
}
