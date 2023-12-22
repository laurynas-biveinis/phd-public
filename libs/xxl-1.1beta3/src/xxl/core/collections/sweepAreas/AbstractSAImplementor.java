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

import xxl.core.cursors.AbstractCursor;
import xxl.core.predicates.Equal;
import xxl.core.predicates.Predicate;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.ExternalTriggeredPeriodicMetaData;
import xxl.core.util.metaData.MetaDataManagement;

/**
 * A pre-implementation of the interface {@link SweepAreaImplementor}.
 * It provides the basic code for the initialization of a 
 * SweepAreaImplementor as well as a default implementation of 
 * the retrieval functionality.
 * 
 * @see SweepAreaImplementor
 * @see xxl.core.predicates.Predicate
 * @see java.util.Iterator
 */
public abstract class AbstractSAImplementor<E> implements SweepAreaImplementor<E> {
	
	public class AbstractSAImplementorMetaDataManagement extends AbstractMetaDataManagement<Object,Object> implements ExternalTriggeredPeriodicMetaData {

		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			return false;
		}

		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			return false;
		}
		
		public void updatePeriodicMetaData(long period) {			
		}
		
		public boolean needsPeriodicUpdate(Object metaDataIdentifier) {
			return false;
		}
		
	}
	
	/**
	 * Binary predicates used to query this implementor.
	 * To offer a retrieval depending on the ID passed
	 * to the query calls, an implementor requires such an 
	 * array of predicates. <br>
	 * This array is set during the {@link #setQueryPredicates(int, Predicate[])}
	 * method.
	 */
	protected Predicate<? super E>[] predicates;
	
	/**
	 * The ID of the SweepArea this implementor belongs to. <br>
	 * This ID is set during the {@link #setQueryPredicates(int, Predicate[])}
	 * method.
	 */
	protected int ID;
	
	protected Predicate<? super E> equals;
	
	protected MetaDataManagement<Object,Object> metaDataManagement;

	
	/**
	 * This constructor allows to specify a binary predicate that
	 * is used to determine object equality within the 
	 * <code>update</code> and <code>remove</code> methods.
	 * 
	 * @param equals Binary predicate determining if the two passed 
	 * 		  argument objects are equal. 
	 */
	public AbstractSAImplementor(Predicate<? super E> equals) {
		this.equals = equals;
		createMetaDataManagement();
	}
	
	/**
	 * Default constructor uses {@link Equal equal} predicate within
	 * the <code>update</code> and <code>remove</code> methods to
	 * determine object equality.
	 */
	public AbstractSAImplementor() {
		this.equals = new Equal<E>();
		createMetaDataManagement();
	}
	
	/**
	 * Initializes this implementor by setting the ID
	 * and the query-predicates. This method has to be called
	 * during the construction of the corresponding SweepArea.
	 * 
	 * @param ID the ID of the SweepArea the new implementor
	 *        should belong to.
	 * @param predicates the binary predicates used to query
	 *        the new implementor.
	 */
	public void setQueryPredicates(int ID, Predicate<? super E>[] predicates) {
		this.predicates = predicates;
		this.ID = ID;
	}
	
	/**
	 * Inserts the given element.
	 * 
	 * @param o The object to be inserted.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the insertion due to the passed argument.
	 */
	public abstract void insert(E o) throws IllegalArgumentException;
	
	
	/**
	 * Removes the specified element.
	 * 
	 * @param o The object to be removed.
	 * @return <tt>True</tt> if the removal has been successful, otherwise <tt>false</tt>.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the removal due to the passed argument.
	 */
	public abstract boolean remove(E o) throws IllegalArgumentException;

	/**
	 * Checks if element <tt>o1</tt> is contained and 
	 * if <tt>true</tt> updates it with </tt>o2</tt>.
	 * 
	 * <p>This implementation throws an
	 * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.</p>
	 * 
	 * @param o1 The object to be replaced.
	 * @param o2 The new object.
	 * @return The updated object is returned.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the update operation due to the passed arguments.
	 * @throws UnsupportedOperationException Throws an UnsupportedOperationException
	 * 		if this method is not supported.
	 */
	public E update(E o1, E o2) throws IllegalArgumentException, UnsupportedOperationException {
		throw new UnsupportedOperationException(); 
	}

	/**
	 * Clears this implementor. Removes all its
	 * elements, but holds its allocated resources.
	 */
	public abstract void clear();

	/**
	 * Closes this implementor and 
	 * releases all its allocated resources.
	 */
	public abstract void close();

	/**
	 * Returns the size of this implementor.
	 * 
	 * @return The size.
	 */
	public abstract int size();

	/**
	 * Returns an iterator over the elements of this
	 * implementor.
	 * 
	 * @return An iterator over the elements of this SweepAreaImplementor.
	 * @throws UnsupportedOperationException If this operation is not supported.
	 */
	public abstract Iterator<E> iterator();

	/**
	 * Queries this implementor with the help of the
	 * specified query object <code>o</code> and the query-predicates
	 * set during initialization, see method {@link #setQueryPredicates(int, Predicate[])}. 
	 * Returns all matching elements as an iterator. <br>
	 * <i>Note:</i>
	 * The returned iterator should not be used to remove any elements from
	 * this implementor!
	 * 
	 * @param o The query object. This object is typically probed against
	 * 		the elements contained in this implementor.
	 * @param ID An ID determining from which input this method
	 * 		is triggered.
	 * @return All matching elements of this implementor are returned as an iterator. 
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong due to the passed arguments during retrieval.
	 */
	public abstract Iterator<E> query(E o, int ID) throws IllegalArgumentException;
	
	/**
	 * Calls {@link #query(Object[], int[], int)} by setting 
	 * <code>valid</code> to <code>os.length</code>.
	 * 
	 * @param os The query objects. These objects are typically probed against
	 * 		the elements contained in this implementor.
	 * @param IDs IDs determining from which input the query objects come from.
	 * @return All matching elements of this implementor are returned as an iterator. 
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong due to the passed arguments during retrieval.
	 * @see SweepAreaImplementor#query(Object[], int[], int)
	 */
	public Iterator<E> query(E[] os, int [] IDs) throws IllegalArgumentException {
		 return query(os, IDs, os.length);
	}

	/**
	 * The default implementation is restricted to the probing with
	 * single elements by calling the method 
	 * {@link #query(Object, int)} on <code>os[0]</code> and 
	 * <code>IDs[0]</code>.  
	 * 
	 * @param os The query objects. These objects are typically probed against
	 * 		the elements contained in this implementor.
	 * @param IDs IDs determining from which input the query objects come from.
	 * @param valid Determines how many entries at the beginning of
	 *        <tt>os</tt> and <tt>IDs</tt> are valid and therefore taken into account.
	 * @return All matching elements of this SweepArea are returned as an iterator. 
	 * @throws IllegalArgumentException If <code>valid</code> is larger than 0. 
	 */
	public Iterator<E> query(E[] os, int [] IDs, int valid) throws IllegalArgumentException {
		if (valid > 0) throw new IllegalArgumentException();
		return query(os[0],IDs[0]);
	}
	
	/**
	 * Returns a {@link xxl.core.cursors.Cursor Cursor} that
	 * results from filtering the passed iterator <code>it</code> with the
	 * query-predicate at position <code>ID</code> in the predicate array.
	 * Since the query-predicates are binary, the second
	 * argument (the right one) is fixed by setting it 
	 * to the passed object <code>o</code>. <br>
	 * The same functionality could be achieved by applying a 
	 * {@link xxl.core.cursors.filters.Filter Filter} to the 
	 * iterator <code>it</code>. But in this case, the binary
	 * query-predicates have to be wrapped to unary ones using
	 * the class {@link xxl.core.predicates.RightBind RightBind}.
	 * However, the latter approach suffers from increased 
	 * efficiency since it doubles the number of method calls.
	 * 
	 * @param it The iterator to be filtered.
	 * @param o Specifies the second (right) argument when the 
	 * 		  binary query-predicate is invoked.
	 * @param ID Determines the query-predicate.
	 * 		  <code>predicates[ID]</code> is utilized for probing.
	 * @return A cursor that delivers all elements of the underlying
	 * 		   iterator where <code>predicates[ID].invoke(it.next(), o)</code>
	 * 		   holds.
	 */	
	protected Iterator<E> filter(final Iterator<? extends E> it, final E o, final int ID) {
		return new AbstractCursor<E>() {
							
			public boolean hasNextObject() {
				while(it.hasNext()) {
					if(predicates[ID].invoke(next = it.next(), o)) 
						return true;
				}
				return false;
			}
	
			public E nextObject() {
				return next;						
			}
		};
	}
	
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractSAImplementorMetaDataManagement();
	}
	
	public MetaDataManagement<Object,Object> getMetaDataManagement() {
		return metaDataManagement;
	}
	
	public CompositeMetaData<Object,Object> getMetaData() {
		return metaDataManagement.getMetaData();
	}


}
