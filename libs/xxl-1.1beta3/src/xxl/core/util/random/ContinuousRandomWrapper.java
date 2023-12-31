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

package xxl.core.util.random;

/** This class provides an interface for wrapping different kinds of PRNGs
 * (pseudo random number generator) for using them within xxl.
 * A default implementation is given by 
 * {@link xxl.core.util.random.JavaContinuousRandomWrapper JavaContinuousRandomWrapper}
 * using {@link java.util.Random java build-in} PRNG.<br>
 * <b>Caution</b>:
 * It is recommended not to use this PRNG cause of its lack of speed and
 * quality.<br>
 * Instead one can use the MersenneTwister PRNG provided by the java library
 * colt (http://tilde-hoschek.home.cern.ch/~hoschek/colt/index.htm").
 * An implementation of this interface for using them with the colt library could be found in
 * the connectivty.colt package.
 *
 * @see java.util.Random
 * @see xxl.core.util.random.JavaContinuousRandomWrapper
 * @see xxl.core.util.random.JavaDiscreteRandomWrapper
 * @see xxl.core.util.random.DiscreteRandomWrapper
 */

public interface ContinuousRandomWrapper {

	/** Returns the next randomly distributed double value given by
	 * the wrapped PRNG.
	 * @return the next randomly distributed double value.
	 * @see java.util.Random
	 * @see xxl.core.util.random.JavaContinuousRandomWrapper
	 */
	public abstract double nextDouble();
}