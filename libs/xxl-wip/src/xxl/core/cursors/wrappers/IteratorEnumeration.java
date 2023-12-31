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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The iterator-enumeration wraps an {@link java.util.Iterator iterator} to an
 * {@link java.util.Enumeration enumeration}, i.e., the wrapped iterator can be
 * accessed via the methods of the enumeration interface. Therefore the methods
 * <tt>hasMoreElements</tt> and <tt>nextElement</tt> have to be implemented as
 * follows:
 * <pre>
 *     public boolean hasMoreElements() {
 *         return iterator.hasNext();
 *     }
 * 
 *     public Object nextElement() throws NoSuchElementException {
 *         return iterator.next();
 *     }
 * </pre>
 * The required functionality is delivered by applying the methods
 * <tt>hasNext</tt> and <tt>next</tt> to the given iterator.
 *
 * @see java.util.Iterator
 * @see java.util.Enumeration
 */
public class IteratorEnumeration implements Enumeration {

	/**
	 * The internally used iterator that is wrapped to an enumeration.
	 */
	protected Iterator iterator;

	/**
	 * Creates a new iterator-enumeration.
	 *
	 * @param iterator the iterator to be wrapped to an enumeration.
	 */
	public IteratorEnumeration(Iterator iterator) {
		this.iterator = iterator;
	}

	/**
	 * Tests if this enumeration contains more elements.
	 * 
	 * @return <tt>true</tt> if and only if this enumeration object contains at
	 *         least one more element to provide; <tt>false</tt> otherwise.
	 */
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	/**
	 * Returns the next element of this enumeration if this enumeration object
	 * has at least one more element to provide.
	 * 
	 * @return the next element of this enumeration.
	 * @throws NoSuchElementException if no more elements exist.
	 */
	public Object nextElement() throws NoSuchElementException {
		return iterator.next();
	}
	
}

