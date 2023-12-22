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

package xxl.core.pipes.sinks;

import java.io.PrintStream;

import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 * Sink component in a query graph that is usally used for test purposes.
 * This kind of sink counts the elements streaming in and monitors the 
 * time spent for processing. Furthermore, it is able to
 * write this information to a user-defined print stream.
 * <P>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	new Tester(new Enumerator(100000, 0), DEFAULT_ID, System.out, true, 10000, false);
 * </code></pre>
 * Note that Tester does not rely on temporal objects, but implements
 * the interface Heartbeat.
 * @param <I> 
 *
 * @see java.io.PrintStream
 * @since 1.1
 */
public class Tester<I> extends AbstractSink<I> implements Heartbeat {
	
	/**
	 * Counts the elements streaming in.
	 */
	protected long count;
	
	/**
	 * Counts the heartbeats streaming in.
	 */
	protected long countHB;

	/**
	 * Point in time, where this sink started
	 * its processing. Return-value of the
	 * method <CODE>System.currentTimeMillis()</CODE>.
	 */
	protected long start;
	
	/**
	 * Measurements are written to this PrintStream.
	 */
	protected PrintStream stream;
	
	/**
	 * Flag that signals if an intermediate output is generated.
	 */
	protected boolean intermediateOutput;
	
	/**
	 * Every <CODE>every</CODE> elements the time and count
	 * information is written to the specified PrintStream.
	 */
	protected int every;
	
	protected boolean printHB;

	public Tester(PrintStream stream, boolean intermediateOutput, int every) {
		count = 0;
		this.stream = stream;
		this.intermediateOutput = intermediateOutput;
		this.every = every;
		this.printHB = false;
	}
	
	/** 
	 * Creates a new terminal sink in a query graph that
	 * prints time and count information to the specified PrintStream.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 * @param stream Measurements are written to this PrintStream.
	 * @param intermediateOutput If <CODE>true</CODE> an intermediate output is generated, otherwise not.
	 * @param every Every <CODE>every</CODE> elements the time and count
	 * 		information is written to the specified PrintStream, if the flag <CODE>intermediateOutput</CODE>
	 * 		is set.
	 */ 
	public Tester(Source<? extends I> source, int ID, PrintStream stream, boolean intermediateOutput, int every) {
		this(stream, intermediateOutput, every);
		if (!Pipes.connect(source, this, ID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	public Tester(Source<? extends I> source, PrintStream stream, boolean intermediateOutput, int every) {
		this(source, DEFAULT_ID, stream, intermediateOutput, every);
	}
	
	/**
	 * @param source
	 * @param every
	 */	
	public Tester(Source<? extends I> source, int every) {
		this(source, DEFAULT_ID, System.out, true, every);
	}

	/** 
	 * Creates a new terminal sink in a query graph that
	 * prints the completion time and the number of elements completely processed
	 * to Java's standard output stream <CODE>System.out</CODE> <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 */ 
	public Tester(Source<? extends I> source) {
		this(source, DEFAULT_ID, System.out, false, 0);
	}

	/** 
	 * Creates a new terminal sink in a query graph that
	 * prints the completion time and the number of elements completely processed
	 * to the specified PrintStream. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The input rate is not measured.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param stream Measurements are written to this PrintStream.
	 */ 
	public Tester(Source<? extends I> source, PrintStream stream) {
		this(source, DEFAULT_ID, stream, false, 0);
	}
	
	/**
	 * The counter is incremented. If the flag
	 * <CODE>intermediateOutput</CODE> is set, every <CODE>every</CODE> elements
	 * the current counter and the time passed from starting this sink
	 * are written to the specified PrintStream.  
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this sink specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		count++;
		if (intermediateOutput) {
			if (count % every == 0)
				stream.println("no. of objects: "+count+"\t system time: "+(System.currentTimeMillis()-start));
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeat#heartbeat(long, int)
	 */
	public void heartbeat(long timeStamp, int sourceID) {
		processingWLock.lock();
		try {
			countHB++;
			if (intermediateOutput && printHB)
				stream.println("heartbeat: "+timeStamp+" sourceID: "+sourceID);
		}
		finally {
			processingWLock.unlock();	
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#openAllSources()
	 */
	@Override
	public void openAllSources() {
		graph.RLock.lock();
		try {
			start = System.currentTimeMillis();
			super.openAllSources();
		}
		finally {
			graph.RLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#closeAllSources()
	 */
	@Override
	public void closeAllSources() {
		graph.WLock.lock();
		try {
			super.closeAllSources();
			stream.println("total no. of objecs: "+count);
			if (printHB)
				stream.println("total no. of heartbeats: "+countHB);
			stream.println("total runtime (ms): "+(start == 0 ? 0 : System.currentTimeMillis()-start));
		}
		finally {
			graph.WLock.unlock();
		}
	}
	 
	public long getCount() {
		processingRLock.lock();
		try {
			return count;
		}
		finally {
			processingRLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeat#activateHeartbeats(boolean)
	 */
	public void activateHeartbeats(boolean on) {
		
	}
	
	public void setPrintHB(boolean printHB) {
		this.printHB = printHB;
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
		Tester tester = new Tester<Integer>(new Enumerator(100000, 0), DEFAULT_ID, System.out, true, 10000);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(tester);
		exec.startQuery(tester);		
	}

}