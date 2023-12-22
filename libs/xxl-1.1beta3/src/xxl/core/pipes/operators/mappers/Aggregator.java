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
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.Average;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Operator component in a query graph that performs an
 * incremental aggregation. User-defined binary aggregation
 * functions are applied to an incoming element as
 * follows: 
 * <BR><BR>
 * <CODE><PRE>
 * 	aggregate = function.invoke(aggregate, o)
 * </CODE></PRE>
 * So the next aggregation value is computed by applying the binary
 * aggregation function to the last aggreation value and the incoming
 * element <CODE>o</CODE>. After that the new computed aggregation
 * value is transferred to all subscribed sinks. <BR>
 * Futhermore the OnlineAggregator offers the possibility to define more than
 * one aggregation function, e.g., the user is able to compute
 * a sum and an average of the same input stream simultaneously 
 * in only one operator. <br>
 * <I>Note:</I> The aggregation function has to handle the situation
 * that the parameter <CODE>aggregate</CODE> may be <CODE>null</CODE>.
 * In that case the aggregate has to be initialized correctly.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	new Printer(
 * 		new OnlineAggregator(
 * 			new RandomNumber(new JavaDiscreteRandomWrapper(), 100, 10, false),
 * 			new Average()
 * 		)
 * 	);
 * </code></pre>
 *
 * @see Function
 * @since 1.1
 */
public class Aggregator<I,O> extends AbstractPipe<I,O> {

	/**
	 * Binary aggregation function.
	 */
	protected AggregationFunction<? super I,O> function;
	
	/**
	 * Current aggregation value.
	 */
	protected O aggregate = null;

	public Aggregator (AggregationFunction<? super I,O> function) {
		this.function = function;
	}
	
	/** 
	 * Creates a new OnlineAggregator as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 * @param function Binary aggregation function, that is applied to the last
	 * 		aggregation value and the next incoming element. It returns
	 * 		the next aggregation value.
	 */ 
	public Aggregator (Source<? extends I> source, int sourceID, AggregationFunction<? super I,O> function) {
		this(function);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	/** 
	 * Creates a new OnlineAggregator as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param function Binary aggregation function, that is applied to the last
	 * 		aggregation value and the next incoming element. It returns
	 * 		the next aggregation value.
	 */ 
	public Aggregator (Source<? extends I> source, AggregationFunction<? super I,O> function) {
		this(source, DEFAULT_ID, function);
	}

	/** 
	 * The binary aggregation function is applied to the
	 * last aggregation value and the given  element. The resulting new aggregation value
	 * is transferred to all of this pipe's subscribed sinks by calling <CODE>super.transfer(aggregate)</CODE>. <BR>
	 * The implementation looks as follows:
	 * <BR><BR>
	 * <CODE><PRE>
	 * 	super.transfer(aggregate = function.invoke(aggregate, o));
	 * </CODE></PRE>
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		super.transfer(aggregate = function.invoke(aggregate, o));
	}

	/**
	 * Calls <CODE>super.close()</CODE> and sets 
	 * aggregate to <CODE>null</CODE>.
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed())
			aggregate = null;
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
		Printer printer = new Printer<Number>(
			new Aggregator<Integer,Number>(
				new RandomNumber<Integer>(new JavaDiscreteRandomWrapper(), 100, 10),
				new Average()
			)
		);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);
	}

}