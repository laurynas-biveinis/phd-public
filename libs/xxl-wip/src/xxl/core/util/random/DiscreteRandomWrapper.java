/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.random;

/** This class provides a interface for wrapping different kinds of PRNGs
 * (pseudo random number generator) for using them within xxl.
 * A default implementation is given by 
 * {@link xxl.core.util.random.JavaDiscreteRandomWrapper JavaDiscreteRandomWrapper}
 * using {@link java.util.Random java build-in} PRNG.<br>
 * <b>Caution</b>:
 * It is recommended not to use this PRNG cause of its lack of speed and
 * quality.<br>
 * Instead one can use the MersenneTwister PRNG provided by the java library
 * colt (http://tilde-hoschek.home.cern.ch/~hoschek/colt/index.htm")..
 * An implementation of this interface for using them with the colt library could be found in
 * the connectivty.colt package.
 *
 * @see java.util.Random
 * @see xxl.core.util.random.JavaContinuousRandomWrapper
 * @see xxl.core.util.random.JavaDiscreteRandomWrapper
 * @see xxl.core.util.random.DiscreteRandomWrapper
 */

public interface DiscreteRandomWrapper {

	/** Returns the next randomly distributed <tt>integer</tt> value given by
	 * the wrapped PRNG.
	 * @return the next randomly distributed integer value.
	 * @see java.util.Random
	 * @see xxl.core.util.random.JavaDiscreteRandomWrapper
	 */
	public abstract int nextInt();
}
