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

package xxl.core.pipes.operators.filters;

import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Tester;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/**
 * A Sampler is a specialization of the class {@link Filter}, which guarantees
 * that each element of the input stream takes place in the output stream with
 * the same probability <CODE>p</CODE>. This processing guarantees that the output stream 
 * represents a sample of the input stream. <BR>
 * An instance of the class {@link ContinuousRandomNumber} is used to generate
 * uniformly distributed continuous random numbers within range [0, 1]. The parameter
 * <CODE>p</CODE> denotes the Bernoulli probability. For each incoming element
 * a new random number is generated and compared with the specified Bernoulli probability.
 * If the random number is lower than <CODE>p</CODE> the element is transferred,
 * otherwise discarded. 
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	Sampler sampler = new Sampler(
 * 		new Enumerator(10000, 0),
 * 		new ContinuousRandomNumber(new JavaContinuousRandomWrapper()),
 * 		0.5d // Bernoulli probability
 * 	);
 * 	new Tester(sampler);
 * </code></pre>
 *
 * @see ContinuousRandomNumber
 * @see Predicate
 * @since 1.1
 */
public class Sampler<I> extends Filter<I> {

	public Sampler(final ContinuousRandomNumber crn, final double p) {
		super(new Predicate<I>() {
				@Override
				public boolean invoke (I o) {
					if (crn.hasNext()) {
						double number = crn.next().doubleValue();
						if(number < 0 || number > 1)
							throw new IllegalArgumentException("Delivered random number not in range [0, 1].");
						if (number < p)
							return true;
						return false;
					}
					throw new IllegalArgumentException("ContinuousRandomNumber generator has no more elements!");
				}
			}
		);
		if(p == 0 || p > 1) throw new IllegalArgumentException("p has to be in range (0, 1].");
	}
	
	/** 
	 * Creates a new Sampler as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param crn ContinuousRandomNumber generator.
	 * @param p Bernoulli probability. For each incoming element
 	 * 		a new random number is generated and compared with the specified Bernoulli probability.
 	 * 		If the random number is lower than <CODE>p</CODE> the element is transferred,
 	 * 		otherwise discarded. 
	 */ 
	public Sampler(Source<? extends I> source, int sourceID, final ContinuousRandomNumber crn, final double p) {
		this(crn, p);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	/** 
	 * Creates a new Sampler as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param crn ContinuousRandomNumber generator.
	 * @param p Bernoulli probability. For each incoming element
 	 * 		a new random number is generated and compared with the specified Bernoulli probability.
 	 * 		If the random number is lower than <CODE>p</CODE> the element is transferred,
 	 * 		otherwise discarded. 
	 */ 
	public Sampler(Source<? extends I> source, final ContinuousRandomNumber crn, final double p) {
		this(source, DEFAULT_ID, crn, p);
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
		Sampler<Integer> sampler = new Sampler<Integer>(
			new Enumerator(10000, 0),
			new ContinuousRandomNumber(new JavaContinuousRandomWrapper()),
			0.5d
		);
		Tester tester = new Tester<Integer>(sampler);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(tester);
		exec.startQuery(tester);
	}

}
