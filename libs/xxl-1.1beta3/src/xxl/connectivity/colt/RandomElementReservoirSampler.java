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

		Iterator it = 
			new RandomElementReservoirSampler(
				new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(100), 50),
				10,
				1
			);
		int c = 0;
		while (it.hasNext()) {
			Object[] o = (Object[]) it.next();
			if(o==null) continue;
			System.out.print(c++);
			for (int i = 0; i < o.length; i++)
				System.out.print(": " + o[i]);
			System.out.println();
		}
		System.out.println("---------------------------------");
	}
}