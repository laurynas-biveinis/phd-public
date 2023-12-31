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

package xxl.core.cursors.unions;

import java.util.Iterator;
import java.util.Stack;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.Function;

/**
 * A flatten gets an input iterator which has a certain
 * hirarchical structure and flattens this structure, 
 * so that only the interesting objects are returned.
 * <p>
 * For more details look into the example inside the 
 * main method.
 *
 * @see java.util.Iterator
 * @see xxl.core.cursors.Cursor
 * @see xxl.core.cursors.AbstractCursor
 */
public class Flatten extends AbstractCursor {

	/**
	 * Special function which can be used to flatten a cursor
	 * that contains objects and iterators.
	 */
	public static final Function ITERATOR_FLATTEN_FUNCTION = new Function() {
		public Object invoke(Object o) {
			if (o instanceof Iterator)
				return o;
			else
				return null;
		}
	};

	/**
	 * Function which gets the currently processed element  
	 * and returns an Object. There are three possibilities:
	 * <ol>
	 * <li>null (which means that the next element
	 * is the next element of the cursor)</li>
	 * <li>A cursor which contains elements (and maybe results).</li>
	 * <li>An other object (the next element to be processed). 
	 * In this case, the function is called again on the object 
	 * until the function returns null. Then, the last element is
	 * returned.</li>
	 * </ol>
	 */
	protected Function getCursor;

	/** Stack which contains the currently processed cursors. */
	protected Stack stack;

	/** The next object which will be returned. */
	protected Object nextObject;

	/**
	 * Creates a new sequentializer backed on an iteration of input iterations.
	 * Every iterator given to this constructor is wrapped to a cursor.
	 *
	 * @param iteratorsCursor iteration of input iterations to be sequentialized.
	 * @param getCursor Function which gets the currently processed element  
	 * 	and returns an Object.
	 */
	public Flatten(Iterator iteratorsCursor, Function getCursor) {
		this.getCursor = getCursor;
		
		stack = new Stack();
		stack.add(Cursors.wrap(iteratorsCursor));
		
		nextObject = null;
	}

	/**
	 * Returns <tt>true</tt> if the iteration has more elements. (In other words,
	 * returns <tt>true</tt> if <tt>next</tt> or <tt>peek</tt> would return an
	 * element rather than throwing an exception.)
	 * 
	 * <p>The attribute <tt>cursor</tt> is set by this method to the next input
	 * iteration as follows:
	 * <pre>
	 *     while (!cursor.hasNext()) {
	 *         cursor.close();
	 *         if (iteratorsCursor.hasNext())
	 *             cursor = Cursors.wrap((Iterator)iteratorsCursor.next());
	 *         else
	 *             cursor = EmptyCursor.DEFAULT_INSTANCE;
	 *     }
	 * </pre>
	 * If the next input iteration is given by an iterator it is wrapped to a
	 * cursor. The method returns whether the currently processed input iteration
	 * contains further elements, i.e., the result of the <tt>hasNext</tt> method
	 * of <tt>cursor</tt>.
	 *
	 * @return <tt>true</tt> if the sequentializer has more elements.
	 */
	protected boolean hasNextObject() {
		while (true) {
			if (stack.isEmpty())
				return false;
			Cursor cursor = (Cursor) stack.peek();
			if (!cursor.hasNext()) {
				stack.pop();
				cursor.close();
			}
			else {
				Object nextCursor = cursor.peek();
				while (true) {
					nextCursor = getCursor.invoke(nextCursor);
					if (nextCursor==null)
						return true; // The cursor has the next element!
					else if (nextCursor instanceof Iterator) {
						cursor.next(); // The element (the cursor) is processed
						stack.push(Cursors.wrap((Iterator) nextCursor));
						break;
					}
					else
						// The function returned an object which will be returned.
						nextObject = nextCursor;
				}
			}
		}
	}

	/**
	 * Returns the next element in the iteration. This element will be
	 * accessible by some of the sequentializer's methods, e.g., <tt>update</tt>
	 * or <tt>remove</tt>, until a call to <tt>next</tt> or <tt>peek</tt> occurs.
	 * This is calling <tt>next</tt> or <tt>peek</tt> proceeds the iteration and
	 * therefore its previous element will not be accessible any more.
	 * 
	 * @return the next element in the iteration.
	 */
	protected Object nextObject() {
		if (nextObject!=null) {
			Object ret = nextObject;
			nextObject = null;
			return ret;
		}
		else
			return ((Cursor) stack.peek()).next();
	}

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {
		
		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		Cursor cursor = new ArrayCursor(new Object[] {
			new Integer(1),
			new xxl.core.cursors.sources.Enumerator(2,4),
			new Integer(4),
			EmptyCursor.DEFAULT_INSTANCE,
			new ArrayCursor(new Object[] {
				EmptyCursor.DEFAULT_INSTANCE,
				new xxl.core.cursors.sources.Enumerator(5,8),
				new Integer(8),
				new xxl.core.cursors.sources.Enumerator(9,10),
				EmptyCursor.DEFAULT_INSTANCE
			}),
			new Integer(10)
		});
		
		Flatten flatten = new Flatten(
			cursor,
			ITERATOR_FLATTEN_FUNCTION
		);
		
		flatten.open();
		
		while (flatten.hasNext())
			System.out.print(flatten.next() + "; ");
		System.out.flush();
		System.out.println();
		
		flatten.close();
	}
}
