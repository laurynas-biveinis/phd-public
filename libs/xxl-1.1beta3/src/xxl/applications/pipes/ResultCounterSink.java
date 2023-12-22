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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/
package xxl.applications.pipes;

import java.util.ArrayList;
import java.util.List;

import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 * Sink that logs the result arrival behavior over time by storing after 
 * n processed elements the current time.
 */
public class ResultCounterSink<I> extends AbstractSink<I> {
	
	/**
	 * The time when the sink is constucted.
	 */
	long startTime;
	
	/**
	 * Result counter.
	 */
	long count;
	
	/**
	 * After each n processed result elements the time is logged.   
	 */
	long n;
	
	/**
	 * The log for the result arrival times.
	 */
	ArrayList<double []> log = new ArrayList<double []>();
	
	/**
	 * Constructs a new ResultCounterSink.
	 * 
	 * @param s the source to process.
	 * @param n After each n processed result elements the time is logged.
	 */
	public ResultCounterSink(Source<I> s, long n) {
		super(s);
		startTime = System.currentTimeMillis();
		count = 0;
		if (n < 1)
			throw new IllegalArgumentException("n must be > 0!");
		this.n = n;
	}

	/**
	 * Constructs a new ResultCounterSink which logs the time every 100 elements.
	 * 
	 * @param s the source to process.
	 */
	public ResultCounterSink(Source<I> s) {
		this(s, 100);
	}	
	
	/**
	  * Process each incoming element and logs the arrival time every n processed elements.
	  * 
	  * @param o The element streaming in.
	  * @param ID One of the IDs this pipe specified during its subscription by the underlying sources. 
	  * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
	  */
	@Override
	public void processObject(Object o, int ID) throws IllegalArgumentException {
		count++;
		if (count%n == 0) {
			double [] wert = new double[2];
			wert[0] = System.currentTimeMillis()-startTime;
			wert[1] = count;
			log.add(wert);
		}
	}

	/**
	 * The method <CODE>done</CODE> is invoked, if the source with ID <CODE>ID</CODE> 
	 * has run out of elements. <BR>
	 * Stops the processing of this sink and displays the resulting arrival pattern over time. 
	 * 
	 * @param ID One of the IDs this pipe specified during its subscription by the underlying sources. 
	 */
	@Override
	public void done(int ID) {
		super.done(ID);
		if (isDone()) {
			List [] lists = new List[] { log };
			String [] descriptions = { "Result Counter" };
			new UserOutput("Result Viever", lists, descriptions);
		}
	}
			
	/**
	 * Example usage.
	 * 
	 * @param args unused.
	 */
	public static void main(String[] args) {
		QueryExecutor.DEFAULT_INSTANCE.registerAndStartQuery(
				new ResultCounterSink<Integer>(new Enumerator(100, 10), 5)
		);
	}

}
