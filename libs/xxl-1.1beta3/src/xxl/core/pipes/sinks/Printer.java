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

import xxl.core.functions.Function;
import xxl.core.functions.Print;
import xxl.core.functions.Println;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sources.Source;

/**
 * Sink component in a query graph that prints the elements streaming in
 * to a user-defined {@link java.io.PrintStream PrintStream}.
 *
 * @see java.io.PrintStream
 * @since 1.1
 */
public class Printer<I> extends AbstractSink<I> {

	/**
	 * Print-function.
	 */
	protected Function<I,I> printFunction;
	protected PrintStream printStream;
	
	public Printer(PrintStream printStream, boolean println) {
		setPrintStream(printStream, println);
	}
	
	/** 
	 * Creates a new terminal sink in a query graph that
	 * prints all incoming elements to the specified PrintStream.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 * @param printStream The PrintStream the elements' string representation is forwarded to.
	 * @param println If <CODE>true</CODE> the elements' string representation is separated by a CR/LF, otherwise not.
	 */ 	
	public Printer(Source<? extends I> source, int sourceID, PrintStream printStream, boolean println) {
		this(printStream, println);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	/** 
	 * Creates a new terminal sink in a query graph that
	 * prints all incoming elements to the specified PrintStream. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param printStream The PrintStream the elements' string representation is forwarded to.
	 * @param println If <CODE>true</CODE> the elements' string representation is separted by a CR/LF, otherwise not.
	 */ 
	public Printer(Source<? extends I> source, PrintStream printStream, boolean println) {
		this(source, DEFAULT_ID, printStream, println);
	}

	/** 
	 * Creates a new terminal sink in a query graph that
	 * prints all incoming elements to Java's standard output stream <CODE>System.out</CODE>. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The input rate is not measured.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param println If <CODE>true</CODE> the elements' string representation is separted by a CR/LF, otherwise not.
	 */ 
	public Printer(Source<? extends I> source, boolean println) {
		super(source);
		setPrintStream(System.out, println);
	}

	/** 
	 * Creates a new terminal sink in a query graph that
	 * prints all incoming elements to Java's standard output stream <CODE>System.out</CODE>. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 * The input rate is not measured. The elements' string representation is separted by a CR/LF.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 */ 
	public Printer(Source<? extends I> source) {
		this(source, true);
	}

	public void setPrintStream(PrintStream printStream, boolean println) {
		if (printStream != null) {
			this.printStream = printStream;
			if (println)
				this.printFunction = new Println<I>(printStream);
			else
				this.printFunction = new Print<I>(printStream);
		}
	}
	
	/**
	 * The print-function is applied with the
	 * element <CODE>o</CODE>.
	 *
	 * @param o The element streaming in.
	 * @param ID The ID this sink specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(I o, int sourceID) throws IllegalArgumentException {
		printFunction.invoke(o);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#done(int)
	 */
	@Override
	public void done(int sourceID) {
		super.done(sourceID);
		if (isDone())
			printStream.close();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#closeAllSources()
	 */
	@Override
	public void closeAllSources() {
		super.closeAllSources();
		printStream.close();
	}

}