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

package xxl.core.pipes.sources;

import java.util.NoSuchElementException;

import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Tester;

/**
 * Source component in a query graph that delivers an ascending
 * or descending sequence of integer objects.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	new VisualSink(new Enumerator(10, 1000), true);
 * </code></pre>
 * Returns the integers 0, ..., 9 with a delay of one second between two
 * successive values.
 * 
 * @see SourceProcessor
 * @since 1.1
 */
public class Enumerator extends AbstractSource<Integer> {

	/** Start of the returned integer sequence (inclusive). */
	protected int from;
	
	/** End of the returned integer sequence (exclusive). */
	protected int to;
	
	/** The next element to be transferred. */
	protected int next;
	
	/** If <tt>true</tt> the sequence is ascending, else the sequence is descending. */
	protected boolean up;

	/**
	 * Creates a new Enumerator with a user-defined processor
	 * simulating the activity of this source.
	 *
	 * @param processor The thread simulating this source's activity.
	 * @param from Start of the returned integer sequence (inclusive).
	 * @param to End of the returned integer sequence (exclusive).
	 */
	public Enumerator(SourceProcessor processor, int from, int to) {
		super(processor);
		this.from = from;
		this.to = to;
		this.up = from <= to;
		this.next = from;
	}
	
	/**
	 * Creates a new Enumerator with a fixed output rate. 
	 *
	 * @param from Start of the returned integer sequence (inclusive).
	 * @param to End of the returned integer sequence (exclusive).
	 * @param period The delay between two successive elements in the resulting stream.
	 */
	public Enumerator(int from, int to, long period) {
		this(new SourceProcessor(period),  from, to);
	}

	/**
	 * Creates a new Enumerator with a fixed output rate. <BR>
	 * The output rate is not measured. Returns a	 
	 * sequence from 0 to <CODE>number-1</CODE>.
	 *
	 * @param number End of the returned integer sequence (exclusive).
	 * @param period The delay between two successive elements in the resulting stream.
	 */
	public Enumerator(int number, long period) {
		this(0, number, period);
	}

	/**
	 * Defines the next element to be transferred.
	 * The implementation is as follows:
	 * <BR><BR>
	 * <CODE><PRE>
	 * 	if (up ? next < to : next > to)
	 * 		return new Integer(up ? next++ : next--);
	 * 	throw new NoSuchElementException();
	 * </CODE></PRE>
	 *
	 * @return The next integer object that is emitted by this source.
	 * @throws java.util.NoSuchElementException If all elements of the specified 
	 * 		interval have been returned.
	 */
	@Override
	public Integer next() throws NoSuchElementException {
		if (up ? next < to : next > to)
			return new Integer(up ? next++ : next--);
		throw new NoSuchElementException();
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
		Tester tester = new Tester<Integer>(new Enumerator(1000, 2));
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(tester);
		exec.startQuery(tester);		
	}

}
