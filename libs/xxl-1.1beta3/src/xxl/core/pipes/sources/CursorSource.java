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

import java.util.Iterator;
import java.util.NoSuchElementException;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;

/**
 * Source component in a query graph that permits access
 * to an <CODE>iterator</CODE>. In other words, this source is based
 * on a user-defined iterator, whose elements will be transferred. <BR>
 * This class realizes the design pattern <it>Adapter</it>.
 * <p>
 * <b>Intent:</b><br>
 * "Convert the interface of a class into another interface clients expect. 
 * Adapter lets classes work together that couldn't otherwise because of incompatible interfaces." <br>
 * For further information see: "Gamma et al.: <i>DesignPatterns. Elements
 * of Reusable Object-Oriented Software.</i> Addision Wesley 1998."
 * <p>
 * <b>Example usage (1):</b>
 * <br><br>
 * <code><pre>
 * 	new Printer(new CursorPipe(new xxl.cursors.Enumerator(20, 0), 100));
 * </code></pre>
 * Prints the elements of the Enumerator-Cursor, namely 20, ..., 1, with a delay
 * of 100 milliseconds between two successive ones to the
 * standard output stream.
 * 
 * @see java.util.Iterator
 * @see Cursor
 * @see SourceProcessor
 * @since 1.1
 */
public class CursorSource<T> extends AbstractSource<T> {

	/**
	 * The cursor providing the elements of this source.
	 */
	protected Cursor<T> cursor;

	/** 
	 * Creates a new autonomous initial source in a query graph
	 * retrieving its elements from the specified iterator.
	 * A user-defined processor simulates its activity. <BR>
	 * The iterator is wrapped to a cursor.
	 * 
	 * @param processor The thread simulating this source's activity.
	 * @param iterator The iterator providing the elements this source will transfer.
	 */
	public CursorSource(SourceProcessor processor, Iterator<T> iterator) {
		super(processor);
		this.cursor = Cursors.wrap(iterator);
	}
	
	/** 
	 * Creates a new autonomous initial source in a query graph
	 * retrieving its elements from the specified iterator.
	 * The source transfers its elements with a fixed rate. <BR>
	 * The iterator is wrapped to a cursor.
	 * 
	 * @param iterator The iterator providing the elements this source will transfer.
	 * @param period The delay between two succesive elements in the resulting stream.
	 */
	public CursorSource(Iterator<T> iterator, long period) {
		this(new SourceProcessor(period), iterator);
	}

	@Override
	public void open() throws SourceIsClosedException {
		super.open();
		cursor.open();
	}

	/**
	 * Closes this source by invoking <CODE>super.close</CODE>.
	 * After that the cursor is also closed.
	 * 
	 * @see AbstractSource#close
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed())
			cursor.close();
	}

	/**
	 * Retrieves the next element from the underlying cursor and thus the next element of the source. 
	 * The implementation is as follows:
	 * <BR><BR>
	 * <CODE><PRE>
	 * 	if (cursor.hasNext())
	 * 		return cursor.next();
	 * 	throw new NoSuchElementException();
	 * </CODE></PRE>
	 * 
	 * @return The next element of the underlying cursor.
	 * @throws java.util.NoSuchElementException If the cursor has no further elements.
	 */
	@Override
	public T next() throws NoSuchElementException {
		if (cursor.hasNext())
			return cursor.next();
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
		Printer printer = new Printer<Integer>(new CursorSource<Integer>(new xxl.core.cursors.sources.Enumerator(20, 0), 100));
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);
	}

}
