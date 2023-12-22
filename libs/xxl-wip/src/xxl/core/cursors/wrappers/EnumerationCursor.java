/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.cursors.wrappers;

import java.util.Enumeration;

import xxl.core.cursors.AbstractCursor;

/**
 * This class provides a wrapper for
 * {@link java.util.Enumeration enumerations}, i.e., an emumeration can be
 * accessed via the {@link xxl.core.cursors.Cursor cursor} interface. The
 * constructor of this class takes an enumeration and wraps it to a cursor. The
 * cursor functionality is projected on the enumeration, therefore the methods
 * <code>remove</code>, <code>update</code> and <code>reset</code> are not
 * supported.
 *
 * @param <E> the type of the elements returned by this iteration.
 * @see java.util.Iterator
 * @see xxl.core.cursors.Cursor
 * @see xxl.core.cursors.AbstractCursor
 * @see java.util.Enumeration
 */
public class EnumerationCursor<E> extends AbstractCursor<E> {

	/**
	 * The internally used enumeration that is wrapped to a cursor.
	 */
	protected Enumeration<? extends E> enumeration;

	/**
	 * Creates a new enumeration-cursor.
	 *
	 * @param enumeration the enumeration to be wrapped to a cursor.
	 */
	public EnumerationCursor(Enumeration<? extends E> enumeration) {
		this.enumeration = enumeration;
	}

	/**
	 * Returns <code>true</code> if the iteration has more elements. (In other
	 * words, returns <code>true</code> if <code>next</code> or
	 * <code>peek</code> would return an element rather than throwing an
	 * exception.)
	 * 
	 * @return <code>true</code> if the enumeration-cursor has more elements.
	 */
	protected boolean hasNextObject() {
		return enumeration.hasMoreElements();
	}

	/**
	 * Returns the next element in the iteration. This element will be
	 * accessible by some of the enumeration-cursor's methods, e.g.,
	 * <code>update</code> or <code>remove</code>, until a call to
	 * <code>next</code> or <code>peek</code> occurs. This is calling
	 * <code>next</code> or <code>peek</code> proceeds the iteration and
	 * therefore its previous element will not be accessible any more.
	 * 
	 * @return the next element in the iteration.
	 */
	protected E nextObject() {
		return enumeration.nextElement();
	}
	
}
