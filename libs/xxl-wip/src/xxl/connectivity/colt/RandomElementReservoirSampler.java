/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.colt;

import java.util.Iterator;

import xxl.core.cursors.mappers.ReservoirSampler;
import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.RandomElement;

/** This class provide a sampling algorithm based upon the 
 * {@link xxl.core.math.statistics.parametric.aggregates.ReservoirSample
 * reservoir sampling} algorithm using a given PRNG
 * shipped with the
 * <a href="http://tilde-hoschek.home.cern.ch/hoschek/colt/index.htm">colt library</a>.
 * <br>
 * There are three types of strategies available based on
 * [Vit85]: Jeffrey Scott Vitter, Random Sampling with a Reservoir, in
 * ACM Transactions on Mathematical Software, Vol. 11, NO. 1, March 1985, Pages 37-57.
 * For further details see {@link xxl.core.math.statistics.parametric.aggregates.ReservoirSample ReservoirSample}
 * and {@link xxl.core.cursors.mappers.ReservoirSampler ReservoirSampler}.
 *
 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample
 * @see xxl.core.cursors.mappers.ReservoirSampler
 * @see xxl.core.cursors.mappers.Aggregator
 * @see xxl.core.cursors.Cursor
 */

public class RandomElementReservoirSampler extends ReservoirSampler {

	/** Constructs a new aggregator that provides a reservoir sampling as output.
	 * 
	 * @param input iterator to draw the sample from
	 * @param n size of the sample to draw
	 * @param type strategy used for determining the position of the treated object
	 * in the sampling reservoir
	 * @param randomGenerator used PRNG shipped with colt for the random wrappers
	 * @throws IllegalArgumentException if an unknown sampling strategy is given
	 */
	public RandomElementReservoirSampler(Iterator input, int n, int type, RandomElement randomGenerator)
		throws IllegalArgumentException {
		super(
			input,
			n,
			type,
			new ColtContinuousRandomWrapper(randomGenerator),
			new ColtDiscreteRandomWrapper(randomGenerator));
	}

	/** Constructs a new aggregator that provides a reservoir sampling as output
	 * using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister}
	 * as PRNG.
	 * 
	 * @param input iterator to draw the sample from
	 * @param n size of the sample to draw
	 * @param type strategy used for determining the position of the treated object
	 * in the sampling reservoir
	 * @throws IllegalArgumentException if an unknown sampling strategy is given
	 */
	public RandomElementReservoirSampler(Iterator input, int n, int type) throws IllegalArgumentException {
		this(input, n, type, new MersenneTwister(RandomNumbers.RANDOM.nextInt()));
	}
}
