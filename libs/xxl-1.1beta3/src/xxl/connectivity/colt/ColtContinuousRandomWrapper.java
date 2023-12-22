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

If you want to be informed on new new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.connectivity.colt;

import xxl.core.util.random.ContinuousRandomWrapper;
import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.AbstractDistribution;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import edu.cornell.lassp.houle.RngPack.RandomElement;
import edu.cornell.lassp.houle.RngPack.Ranecu;
import edu.cornell.lassp.houle.RngPack.Ranlux;
import edu.cornell.lassp.houle.RngPack.Ranmar;

/** This class provides a wrapper for using any
 * {@link edu.cornell.lassp.houle.RngPack.RandomElement random element}
 * provided in the
 * <a href="http://tilde-hoschek.home.cern.ch/hoschek/colt/index.htm">colt library</a>
 * with xxl.
 * If no random engine is given, the 
 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister} PRNG
 * will be used. Furthermore, this wrapper enables the user
 * to assign a {@link cern.jet.random.AbstractContinousDistribution continuous distribution}
 * the generated random numbers should follow.
 * As default instance, the uniform distribution over the interval [0.0, 1.0] is
 * used.
 */

public class ColtContinuousRandomWrapper implements ContinuousRandomWrapper {

	/** Constructs a {@link xxl.core.util.random.ContinuousRandomWrapper ContinuousRandomWrapper}
	 * based upon the {@link edu.cornell.lassp.houle.RngPack.Ranlux Ranlux}
	 * PRNG shipped with colt.
	 */
	public static ContinuousRandomWrapper RANLUX_BASED =
		new ColtContinuousRandomWrapper(new Ranlux(RandomNumbers.RANDOM.nextInt()));

	/** Constructs a {@link xxl.core.util.random.ContinuousRandomWrapper ContinuousRandomWrapper}
	 * based upon the {@link edu.cornell.lassp.houle.RngPack.Ranecu Ranecu}
	 * PRNG shipped with colt.
	 */
	public static ContinuousRandomWrapper RANECU_BASED =
		new ColtContinuousRandomWrapper(new Ranecu(RandomNumbers.RANDOM.nextInt()));

	/** Constructs a {@link xxl.core.util.random.ContinuousRandomWrapper ContinuousRandomWrapper}
	 * based upon the {@link edu.cornell.lassp.houle.RngPack.Ranmar Ranmar}
	 * PRNG shipped with colt.
	 */
	public static ContinuousRandomWrapper RANMAR_BASED =
		new ColtContinuousRandomWrapper(new Ranmar(RandomNumbers.RANDOM.nextInt()));

	/** used continuous distribution */
	protected AbstractDistribution distribution;

	/** Constructs a new Object of this class using the given distribution
	 * to compute random numbers.
	 * 
	 * @param distribution continuous distribution used for computing random numbers
	 */
	public ColtContinuousRandomWrapper(AbstractContinousDistribution distribution) {
		this.distribution = distribution;
	}

	/** Constructs a new Object of this class using the given 
	 * random engine.
	 * 
	 * @param randomElement random engine used
	 */
	public ColtContinuousRandomWrapper(RandomElement randomElement) {
		this(new Uniform(randomElement));
	}

	/** Constructs a new Object of this class using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister} PRNG
	 * by default.
	 * 
	 * @param seed used seed for initializing the PRNG
	 */
	public ColtContinuousRandomWrapper(int seed) {
		this(new Uniform(new MersenneTwister(seed)));
	}

	/** Constructs a new Object of this class using the
	 * {@link cern.jet.random.engine.MersenneTwister Mersenne Twister} PRNG
	 * by default.
	 */
	public ColtContinuousRandomWrapper() {
		this(new Uniform(new MersenneTwister(RandomNumbers.RANDOM.nextInt())));
	}

	/** Computes the next element of the given distribution.
	 * If no explicit distribution is given, the uniform distribution over (0.0,1.0) is used.
	 * 
	 * @return next computed random number
	 */
	public double nextDouble() {
		return distribution.nextDouble();
	}

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		ContinuousRandomWrapper drw = new ColtContinuousRandomWrapper();
		for (int i = 0; i < 50; i++)
			System.out.println(i + "-th continuous random number=" + drw.nextDouble());
		System.out.println("----------------------------------");
	}
}