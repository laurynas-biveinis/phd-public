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

package xxl.core.pipes.operators.mappers;

import xxl.core.functions.Function;
import xxl.core.math.statistics.parametric.aggregates.ReservoirSample;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Tester;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.random.DiscreteRandomWrapper;
import xxl.core.util.random.JavaContinuousRandomWrapper;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Operator component in a query graph that realizes online sampling
 * as a special kind of an aggregation operator based on a 
 * {@link xxl.core.math.statistics.parametric.aggregates.ReservoirSample reservoir sampling function}. <BR>
 * Three different strategies available, that have been published in 
 * "[Vit85]: Jeffrey Scott Vitter, Random Sampling with a Reservoir, in
 * ACM Transactions on Mathematical Software, Vol. 11, NO. 1, March 1985, Pages 37-57".
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	new Tester(
 * 		new Mapper(
 * 			new ReservoirSampler(
 * 				new Enumerator(100, 10),
 * 				new ReservoirSample(10, new ReservoirSample.XType(10)) // sample's size is 10
 * 			),
 * 			new Function() { // formatting output
 * 				protected int c = 0; 
 * 
 * 				public Object invoke(Object o) {
 * 					Object[] next = (Object[])o;
 * 					System.out.println(c++);
 * 					if (next != null) {
 * 						for (int i = 0; i < next.length; i++)
 * 							System.out.print(": " + next[i]);
 * 						System.out.println();
 * 					}
 * 					else
 * 						System.out.println(": reservoir not yet initialized!");
 * 					return o;
 * 				}
 * 			}
 * 		)
 * 	);
 * </code></pre>
 *
 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample
 * @see ContinuousRandomWrapper
 * @see DiscreteRandomWrapper
 * @since 1.1
 */
public class ReservoirSampler extends Aggregator<Number,Number[]> {

	/**
	 * Indicates the use of type R for sampling strategy.
	 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample
	 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample.RType
	 */
	public static final int RTYPE = 0;

	/**
	 * Indicates the use of type X for sampling strategy.
	 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample
	 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample.XType
	 */
	public static final int XTYPE = 1;

	/**
	 * Indicates the use of type Y for sampling strategy. This type is
	 * not available so far due to the lack of information about the
	 * used distribution to determine the position in the reservoir
	 * for a sampled object.
	 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample
	 */
	public static final int YTYPE = 2;

	/**
	 * Indicates the use of type Z for sampling strategy.
	 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample
	 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample.ZType
	 */
	public static final int ZTYPE = 3;

	public ReservoirSampler(int n, int type,
			ContinuousRandomWrapper crw, DiscreteRandomWrapper drw) throws IllegalArgumentException {
		super(new ReservoirSample(n, new ReservoirSample.RType (n, crw, drw)));
		switch ( type){
			case RTYPE:
				this.function = new ReservoirSample(n, new ReservoirSample.RType ( n, crw, drw) );
			break;
			case XTYPE:
				this.function = new ReservoirSample(n, new ReservoirSample.XType ( n, crw, drw) );
			break;
			case YTYPE:
				//this.function = new ReservoirSample (n, new ReservoirSample.YType (n, crw, drw) );
				throw new IllegalArgumentException("Type y is not supported so far. See javadoc xxl.functions.ReservoirSample for details!");
			//break;
			case ZTYPE:
				this.function = new ReservoirSample(n, new ReservoirSample.ZType ( n, crw, drw) );
			break;
			default:
				throw new IllegalArgumentException("unknown sampling strategy given!");
		}
	}
	
	/** 
	 * Creates a new ReservoirSampler as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 * @param n Size of the sample to draw.
	 * @param type Strategy used to determine the position of the treated object
	 * 		in the sampling reservoir.
	 * @param crw PRNG for computing continuous random numbers.
	 * @param drw PRNG for computing discrete random numbers.
	 * @throws IllegalArgumentException If an unknown sampling strategy has been specified.
	 */ 
	public ReservoirSampler(Source<? extends Number> source, int sourceID, int n, int type,
			ContinuousRandomWrapper crw, DiscreteRandomWrapper drw) throws IllegalArgumentException {
		this(n, type, crw, drw);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	/** 
	 * Creates a new ReservoirSampler as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param n Size of the sample to draw.
	 * @param type Strategy used to determine the position of the treated object
	 * 		in the sampling reservoir.
	 * @param crw PRNG for computing continuous random numbers.
	 * @param drw PRNG for computing discrete random numbers.
	 * @throws IllegalArgumentException If an unknown sampling strategy has been specified.
	 */ 
	public ReservoirSampler(Source<? extends Number> source, int n, int type,
			ContinuousRandomWrapper crw, DiscreteRandomWrapper drw) throws IllegalArgumentException {
		this(source, DEFAULT_ID, n, type, crw, drw);
	}
	
	/** 
	 * Creates a new ReservoirSampler as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription. The input rate
	 * and the output rate are not measured. <BR>
	 * Instances of the classes {@link JavaContinuousRandomWrapper} and {@link JavaDiscreteRandomWrapper} 
	 * are used to generate the necessary random numbers.
	 * 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param n Size of the sample to draw.
	 * @param type Strategy used to determine the position of the treated object
	 * 		in the sampling reservoir.
	 * @throws IllegalArgumentException If an unknown sampling strategy has been specified.
	 */ 
	public ReservoirSampler(Source<? extends Number> source, int n, int type) throws IllegalArgumentException {
		this(source, n, type, new JavaContinuousRandomWrapper(), new JavaDiscreteRandomWrapper());
	}

	/** 
	 * Creates a new ReservoirSampler as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription. The input rate
	 * and the output rate are not measured. <BR>
	 * Instances of the classes {@link JavaContinuousRandomWrapper} and {@link JavaDiscreteRandomWrapper} 
	 * are used to generate the necessary random numbers.
	 * 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param n Size of the sample to draw.
	 * @param strategy strategy for an online reservoir sampling with size <CODE>n</CODE>.
	 */ 
	public ReservoirSampler(Source<? extends Number> source, int n, ReservoirSample.ReservoirSamplingStrategy strategy){
		this(source, new ReservoirSample(n, strategy));
	}

	/**
	 * Creates a new ReservoirSampler as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param reservoirSample Function providing an online sampling.
	 */
	public ReservoirSampler(Source<? extends Number> source, ReservoirSample reservoirSample){
		super(source, reservoirSample);
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
		Tester tester = new Tester<Number[]>(
			new Mapper<Number[],Number[]>(
				new ReservoirSampler(
					new Enumerator(100, 10),
					new ReservoirSample(10, new ReservoirSample.XType(10))
				),
				new Function<Number[],Number[]>() {
					protected int c = 0;

					@Override
					public Number[] invoke(Number[] next) {
						System.out.println(c++);
						if (next != null) {
							for (int i = 0; i < next.length; i++)
								System.out.print(": " + next[i]);
							System.out.println();
						}
						else
							System.out.println(": reservoir not yet initialized!");
						return next;
					}
				}
			)
		);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(tester);
		exec.startQuery(tester);
	}

}