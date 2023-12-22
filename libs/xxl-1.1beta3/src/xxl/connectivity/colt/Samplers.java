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

import xxl.core.cursors.sources.Enumerator;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.sampling.RandomSamplingAssistant;
import edu.cornell.lassp.houle.RngPack.RandomElement;

/**
 *
 */
/** This class uses the sampling tools given by the
 * <a href="http://tilde-hoschek.home.cern.ch/hoschek/colt/index.htm">colt library</a>
 * to draw a sample from given data.
 *
 * @see cern.jet.random.sampling.RandomSamplingAssistant
 * @see edu.cornell.lassp.houle.RngPack.RandomElement 
 * @see cern.jet.random.engine.MersenneTwister 
 * @see cern.jet.random.sampling.WeightedRandomSampler
 */

public class Samplers {

	/** Returns a sample of the given data using the colt 
	 * {@link cern.jet.random.sampling.RandomSamplingAssistant RandomSamplingAssistant}.
	 * 
	 * @param N size of the entirety
	 * @param sampleSize size of the sample to draw
	 * @param iterator data to draw the sample from
	 * @throws IllegalArgumentException if there is not enough data to sample
	 * (N to big or the number of Objects delivered by the iterator
	 * to small)
	 * @return a sample of the data given as <tt>Object[]</tt>
	 */
	public Object[] sample(long N, int sampleSize, Iterator iterator) throws IllegalArgumentException {
		return sample(0, N, sampleSize, iterator);
	}

	/** Returns a sample of the given data using the colt 
	 * {@link cern.jet.random.sampling.RandomSamplingAssistant RandomSamplingAssistant}
	 * with the Mersenne Twister Random Engine.
	 * 
	 * @param from indicates where to sample from
	 * @param to position in the iterator to sample from
	 * @param sampleSize size of the sample to draw
	 * @param iterator data to draw the sample from
	 * @throws IllegalArgumentException if there is not enough data to sample
	 * ( (to-from) to big or the number of Objects deliveerd by the iterator
	 * to small)
	 * @return a sample of the data given as <tt>Object[]</tt>
	 */
	public Object[] sample(long from, long to, int sampleSize, Iterator iterator) throws IllegalArgumentException {
		return sample(from, to, sampleSize, iterator, new MersenneTwister());
	}

	/** Returns a sample of the given data using the colt 
	 * {@link cern.jet.random.sampling.RandomSamplingAssistant RandomSamplingAssistant}.
	 * 
	 * @param from indicates where to sample from
	 * @param to position in the iterator to sample from
	 * @param sampleSize size of the sample to draw
	 * @param iterator data to draw the sample from
	 * @param randomGenerator used for computing the sample
	 * @throws IllegalArgumentException if there is not enough data to sample
	 * ( (to-from) to big or the number of Objects delivered by the iterator
	 * to small)
	 * @return a sample of the data given a <tt>Object[]</tt>
	 */
	public Object[] sample(long from, long to, int sampleSize, Iterator iterator, RandomElement randomGenerator)
		throws IllegalArgumentException {
		if (from >= to)
			throw new IllegalArgumentException("from >= to");
		long N = to - from;
		if (to - from < sampleSize)
			throw new IllegalArgumentException("n to big");
		long count = 0;
		int pos = 0; // position in the sample
		Object[] sample = new Object[sampleSize];
		RandomSamplingAssistant rsa = new RandomSamplingAssistant(sampleSize, N, randomGenerator);
		// go to position from
		while (count < from) {
			count++;
			iterator.next();
		}
		// sample to position to
		while (count <= N) {
			Object o = iterator.next();
			count++;
			if (rsa.sampleNextElement())
				sample[pos++] = o;
		}
		return sample;
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
		
		Object[] sample=new Samplers().sample(0,99,20,new Enumerator(0,100));
		for(int i=0;i<sample.length;i++)
			System.out.println((i+1)+"-th sample element: "+sample[i]);
	}
}