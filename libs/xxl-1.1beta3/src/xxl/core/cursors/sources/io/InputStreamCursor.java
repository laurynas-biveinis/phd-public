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

package xxl.core.cursors.sources.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import xxl.core.cursors.AbstractCursor;
import xxl.core.io.converters.Converter;
import xxl.core.util.WrappingRuntimeException;

/**
 * This class provides an iteration that depends on a
 * {@link java.io.DataInputStream data input stream}. It iterates over the
 * objects that are read out of the underlying input stream. A converter is
 * used in order to read out the serialized objects. Additional to the
 * <code>hasNextObject</code> and <code>nextObject</code> methods this class
 * implements the <code>close</code> method in order to close the underlying
 * input stream if necessary.
 * 
 * <p><b>Example usage (1):</b>
 * <code><pre>
 *   // create a new data output stream
 *
 *   ByteArrayOutputStream baos = new ByteArrayOutputStream();
 *   DataOutputStream dos = new DataOutputStream(baos);
 *
 *   // create a new integer converter
 *
 *   IntegerConverter converter = IntegerConverter.DEFAULT_INSTANCE;
 *
 *   // write the integers from 0 to 10 to the data output stream
 *
 *   for (int i = 0; i &lt; 11; i++)
 *       try {
 *           converter.writeInt(dos, i);
 *       }
 *       catch (IOException ioe) {
 *           System.out.println("An I/O error occured.");
 *       }
 *
 *   // create a new data input stream
 *
 *   ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
 *   DataInputStream dis = new DataInputStream(bais);
 *
 *   // create a new input stream cursor that depends on this data output stream
 *
 *   InputStreamCursor&lt;Integer&gt; cursor = new InputStreamCursor&lt;Integer&gt;(dis, converter);
 * 
 *   // open the cursor
 * 
 *   cursor.open();
 *
 *   // print all elements of the cursor
 *
 *   while (cursor.hasNext())
 *       System.out.println(cursor.next());
 *
 *   // close the data output stream and the input stream cursor after use
 *
 *   cursor.close();
 *   try {
 *       dos.close();
 *   }
 *   catch (IOException ioe) {
 *       System.out.println("An I/O error occured.");
 *   }
 * </pre></code></p>
 *
 * <p><b>Example usage (2).</b>
 * <code><pre>
 *   // create a new data output stream
 *
 *   baos = new ByteArrayOutputStream();
 *   dos = new DataOutputStream(baos);
 *
 *   // create a new integer converter
 *
 *   converter = IntegerConverter.DEFAULT_INSTANCE;
 *
 *   // write the integers from 0 to 10 to the data output stream
 *
 *   for (int i = 0; i &lt; 11; i++)
 *       try {
 *           converter.writeInt(dos, i);
 *       }
 *       catch (IOException ioe) {
 *           System.out.println("An I/O error occured.");
 *       }
 *
 *   // create a new data input stream
 *
 *   bais = new ByteArrayInputStream(baos.toByteArray());
 *   dis = new DataInputStream(bais);
 *
 *   // create a new input stream cursor that depends on this data output stream
 *
 *   cursor = new InputStreamCursor&lt;Integer&gt;(dis, converter);
 *
 *   // open the cursor
 * 
 *   cursor.open();
 *
 *   // print the elements of the cursor and close it when 5 has been printed
 *
 *   while (cursor.hasNext()) {
 *       int i = cursor.next();
 *       System.out.println(i);
 *       if (i == 5)
 *           cursor.close();
 *   }
 *
 *   // close the data output stream after use
 *
 *   try {
 *       dos.close();
 *   }
 *   catch (IOException ioe) {
 *       System.out.println("An I/O error occured.");
 *   }
 * </pre></code></p>
 *
 * @param <E> the type of the elements read from the underlying input stream.
 * @see java.util.Iterator
 * @see xxl.core.cursors.Cursor
 * @see xxl.core.cursors.AbstractCursor
 * @see java.io.DataInputStream
 */
public class InputStreamCursor<E> extends AbstractCursor<E> {

	/**
	 * The underlying data input stream contains the serialized objects of this
	 * iteration.
	 */
	protected DataInputStream input;

	/**
	 * The converter is used in order to read out the serialized objects of
	 * this iteration.
	 */
	protected Converter<? extends E> converter;

	/**
	 * Constructs a new input stream cursor that depends on the specified data
	 * input stream and uses the specified converter in order to read out the
	 * serialized objects.
	 *
	 * @param input the data input stream that contains the serialized objects
	 *        of this iteration.
	 * @param converter the converter that is used for reading out the
	 *        serialized objects of this iteration.
	 */
	public InputStreamCursor(DataInputStream input, Converter<? extends E> converter) {
		this.input = input;
		this.converter = converter;
	}

	/**
	 * Closes the cursor, i.e., signals the cursor to clean up resources, close
	 * files, etc. When a cursor has been closed calls to methods like
	 * <code>next</code> or <code>peek</code> are not guaranteed to yield
	 * proper results. Multiple calls to <code>close</code> do not have any
	 * effect, i.e., if <code>close</code> was called the cursor remains in the
	 * state <i>closed</i>.
	 * 
	 * <p>Note, that a closed cursor usually cannot be opened again because of
	 * the fact that its state generally cannot be restored when resources are
	 * released respectively files are closed.</p>
	 */
	public void close() {
		if (!isClosed)
			try {
				input.close();
			}
			catch (IOException ie) {
				throw new WrappingRuntimeException(ie);
			}
		super.close();
	}

	/**
	 * Returns <code>true</code> if the iteration has more elements. (In other
	 * words, returns <code>true</code> if <code>next</code> or
	 * <code>peek</code> would return an element rather than throwing an
	 * exception.)
	 * 
	 * @return <code>true</code> if the cursor has more elements.
	 */
	protected boolean hasNextObject() {
		try {
			next = converter.read(input, null);
			return true;
		}
		catch (EOFException ee) {
			return false;
		}
		catch (IOException ie) {
			throw new WrappingRuntimeException(ie);
		}
	}

	/**
	 * Returns the next element in the iteration. This element will be
	 * accessible by some of the cursor's methods, e.g., <code>update</code> or
	 * <code>remove</code>, until a call to <code>next</code> or
	 * <code>peek</code> occurs. This is calling <code>next</code> or
	 * <code>peek</code> proceeds the iteration and therefore its previous
	 * element will not be accessible any more.
	 * 
	 * @return the next element in the iteration.
	 */
	protected E nextObject() {
		return next;
	}

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new data output stream
		
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
		
		// create a new integer converter
		
		xxl.core.io.converters.IntegerConverter converter = xxl.core.io.converters.IntegerConverter.DEFAULT_INSTANCE;
		
		// write the integers from 0 to 10 to the data output stream
		
		for (int i = 0; i < 11; i++)
			try {
				converter.writeInt(dos, i);
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
		
		// create a new data input stream
		
		java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
		java.io.DataInputStream dis = new java.io.DataInputStream(bais);
		
		// create a new input stream cursor that depends on this data output
		// stream
		
		InputStreamCursor<Integer> cursor = new InputStreamCursor<Integer>(dis, converter);
		
		// open the cursor
		
		cursor.open();
		
		// print all elements of the cursor
		
		while (cursor.hasNext())
			System.out.println(cursor.next());
		
		// close the data output stream and the input stream cursor after use
		
		cursor.close();
		try {
			dos.close();
		}
		catch (IOException ioe) {
			System.out.println("An I/O error occured.");
		}
		System.out.println();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// create a new data output stream
		
		baos = new java.io.ByteArrayOutputStream();
		dos = new java.io.DataOutputStream(baos);
		
		// create a new integer converter
		
		converter = xxl.core.io.converters.IntegerConverter.DEFAULT_INSTANCE;
		
		// write the integers from 0 to 10 to the data output stream
		
		for (int i = 0; i < 11; i++)
			try {
				converter.writeInt(dos, i);
			}
			catch (IOException ioe) {
				System.out.println("An I/O error occured.");
			}
		
		// create a new data input stream
		
		bais = new java.io.ByteArrayInputStream(baos.toByteArray());
		dis = new java.io.DataInputStream(bais);
		
		// create a new input stream cursor that depends on this data output
		// stream
		
		cursor = new InputStreamCursor<Integer>(dis, converter);
		
		// open the cursor
		
		cursor.open();
		
		// print the elements of the cursor and clear it when 5 has been
		// printed
		
		while (cursor.hasNext()) {
			int i = cursor.next();
			System.out.println(i);
			if (i == 5) {
				cursor.close();
				break;
			}
		}
		
		// close the data output stream after use
		
		try {
			dos.close();
		}
		catch (IOException ioe) {
			System.out.println("An I/O error occured.");
		}
		System.out.println();
	}
}
