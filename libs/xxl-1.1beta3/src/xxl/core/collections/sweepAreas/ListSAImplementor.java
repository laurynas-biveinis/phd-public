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
package xxl.core.collections.sweepAreas;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.predicates.Equal;
import xxl.core.predicates.Predicate;

/**
 * A list-based implementation of the interface
 * {@link SweepAreaImplementor}.
 * 
 * @see SweepAreaImplementor
 * @see java.util.Iterator
 * @see java.util.List
 */
public class ListSAImplementor<E> extends AbstractSAImplementor<E> {

	/**
	 * The list storing the elements.
	 */
	protected List<E> list;
	
	/**
	 * Constructs a new ListSAImplementor.
	 * 
	 * @param list The list used to store the elements.
	 * @param equals Binary predicate determining if the two passed 
	 * 		  argument objects are equal. This predicate is used inside the 
	 * 		  <code>update</code> and <code>remove</code> methods instead
	 * 		  of {@link Equal equal} predicate. 
	 */
	public ListSAImplementor(List<E> list, Predicate<? super E> equals) {
		super(equals);
		this.list = list;
	}
	
	/**
	 * Constructs a new ListSAImplementor.
	 * 
	 * @param list The list used to store the elements.
	 */
	public ListSAImplementor(List<E> list) {
		this.list = list;
	}

	/**
	 * Constructs a new ListSAImplementor based
	 * on a LinkedList.
	 */
	public ListSAImplementor() {
		this(new LinkedList<E>());
	}
	
	/**
	 * Appends the given element to the list.
	 * 
	 * @param o The object to be inserted.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the insertion due to the passed argument.
	 */
	public void insert(E o) throws IllegalArgumentException {
		list.add(list.size(), o);
	}
	
	/**
	 * Removes the specified element from the list.
	 * 
	 * @param o The object to be removed.
	 * @return <tt>True</tt> if the removal has been successful, otherwise <tt>false</tt>.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the removal due to the passed argument.
	 */
	public boolean remove(E o) throws IllegalArgumentException {
		Iterator<E> it = list.iterator();
		while (it.hasNext()) {
			if (equals.invoke(o, it.next())) {
				it.remove();
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * Checks if element <tt>o1</tt> is contained in the list and 
	 * if <tt>true</tt> replaces it by </tt>o2</tt>. 
	 * 
	 * @param o1 The object to be replaced.
	 * @param o2 The new object.
	 * @return The updated object is returned.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the update operation due to the passed arguments.
	 */
	public E update(E o1, E o2) throws IllegalArgumentException {
		for (int i = 0, j = list.size(); i < j; i++) {
			if (equals.invoke(o1, list.get(i))) {
				return list.set(i, o2);
			}
		}
		throw new IllegalArgumentException("Object o1 is not contained.");
	}

	/**
	 * Returns the size of the list.
	 * 
	 * @return The size of the list.
	 */
	public int size () {
		return list.size();
	}

	/**
	 * Clears the list.
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * Clears the list.
	 */
	public void close() {
		clear();
	}

	/**
	 * Returns an iterator over the elements of the list.
	 * 
	 * @return An iterator over the elements of the list.
	 */	
	public Iterator<E> iterator() {
		return list.iterator();
	}
	
	/**
	 * Queries the list by performing a sequential scan.
	 * Returns all elements that match with the given 
	 * element <tt>o</tt> according to the user-defined
	 * binary query-predicate. The query-predicates are 
	 * set via the
	 * {@link #setQueryPredicates(int, xxl.core.predicates.Predicate[])} 
	 * method, which is typically called inside the
	 * constructor of a SweepArea. <br>
	 * <i>Note:</i>
	 * The returned iterator should not be used to remove any 
	 * elements from this implementor!
	 *  
	 * @param o The query object.
	 * @param ID An ID determining from which input this method
	 * 		is triggered.
	 * @return An iterator delivering all matching elements.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the query operation due to the passed argument.
	 * @see #filter(Iterator, Object, int)
	 */	
	public Iterator<E> query (final E o, final int ID) throws IllegalArgumentException {
		if (list.size()==0) 
			return new EmptyCursor<E>();
		return filter(list.iterator(), o, ID);	
	}

}
