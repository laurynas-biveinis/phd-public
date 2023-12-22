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

package xxl.core.pipes.operators.identities;

import java.io.PrintStream;

import xxl.core.functions.Function;
import xxl.core.functions.Print;
import xxl.core.functions.Println;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.sources.Source;

/**
 * A special kind of a {@link Mapper} that applies
 * an unary print-function to each incoming element that 
 * writes this element's string representation to a user-defined
 * {@link java.io.PrintStream PrintStream}.
 * 
 * @since 1.1
 */
public class Logger<I> extends Mapper<I,I> {

	
	public Logger(PrintStream printStream, boolean println) {
		super(println ? (Function<I,I>)new Println<I>(printStream) : new Print<I>(printStream));
	}
	
	/** 
	 * Creates a new Logger as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 * @param printStream The PrintStream the elements' string representation is forwarded to.
	 * @param println If <CODE>true</CODE> the elements' string representation is separted by a CR/LF, otherwise not.
	 */
	public Logger(Source<? extends I> source, int ID, PrintStream printStream, boolean println) {
		super(source, ID, println ? (Function<I,I>)new Println<I>(printStream) : new Print<I>(printStream));
	}

	/** 
	 * Creates a new Logger as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param printStream The PrintStream the elements' string representation is forwarded to.
	 * @param println If <CODE>true</CODE> the elements' string representation is separted by a CR/LF, otherwise not.
	 */
	public Logger(Source<? extends I> source, PrintStream printStream, boolean println) {
		this(source, DEFAULT_ID, printStream, println);
	}

	/** 
	 * Creates a new Logger as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription. The input rate
	 * and the output rate are not measured. <BR>
	 * All incoming elements are printed to Java's standard output stream <CODE>System.out</CODE>.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param println If <CODE>true</CODE> the elements' string representation is separted by a CR/LF, otherwise not.
	 */
	public Logger(Source<? extends I> source, boolean println) {
		super(source, println ? (Function<I,I>)new Println<I>() : new Print<I>());
	}

	/** 
	 * Creates a new Logger as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription. The input rate
	 * and the output rate are not measured. <BR>
	 * All incoming elements are printed to Java's standard output stream <CODE>System.out</CODE>
	 * and separated by a CR/LF.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 */
	public Logger(Source<? extends I> source) {
		this(source, true);
	}
	
}
