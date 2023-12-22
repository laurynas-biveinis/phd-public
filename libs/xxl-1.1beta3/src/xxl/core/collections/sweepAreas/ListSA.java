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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.predicates.Predicate;

/**
 * A ListSA is a SweepArea that additionally links its elements with 
 * the help of a list. Each time an element is inserted 
 * into the underlying SweepAreaImplementor, it is appended
 * to the internal list at the same time. The reorganization
 * and expiration, respectively, is based on the linkage. 
 * The remove-predicate is applied to the first element
 * of the list, and if it holds, this element is removed
 * from the list as well as the underlying implementor.
 * This step is performed as long as the remove-predicate
 * holds. 
 * 
 * @see ImplementorBasedSweepArea
 * @see SweepAreaImplementor
 */
public class ListSA<E> extends ImplementorBasedSweepArea<E> {
	
	/**
	 * The internal list providing an additional linkage
	 * of the SweepAreaImplementor's elements.
	 */
	protected List<E> list;
	
	public enum ReorganizeModes {
		LAZY_REORGANIZE, 
		SAVEMEMORY_REORGANIZE
	}
	
	/**
	 * The ListSA supports two different modes of reorganization. In the lazy mode, elements in the 
	 * list are tested starting at the beginning. If one element can not be dropped, the reorganization 
	 * stops. In the save memory mode the in each reorganization step the whole list is checked for
	 * elements which can be dropped. 
	 * Note that there ist only a difference in the modes if ordering the elements by start- or 
	 * end-timestamps gives different orders.
	 */
	protected ReorganizeModes reorganizeMode = ReorganizeModes.LAZY_REORGANIZE;
	
	/**
	 * Constructs a new ListSA.
	 *  
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param reorganizeMode The mode used for reorganization.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 * @param removePredicates An array of binary remove-predicates utilized during the reorganization.
	 * 		  With the help of this predicates, the elements of a SweepArea are probed for expiration.
	 * 		  In analogy to the query-predicates, the predicate actually applied to this ListSA 
	 * 		  depends on the ID passed to the expiration and reorganization call, respectively.
	 * @param list The list maintaining the linkage of the implementor's elements.
	 * @param objectSize The object size of the elements in the sweeparea. 
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, ReorganizeModes reorganizeMode, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates
			, List<E> list, int objectSize) {
		
		super(impl, ID, selfReorganize, queryPredicates, removePredicates, objectSize);
		this.reorganizeMode = reorganizeMode;
		this.list = list;
	}	
	
	/**
	 * Constructs a new ListSA.
	 *  
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 * @param removePredicates An array of binary remove-predicates utilized during the reorganization.
	 * 		  With the help of this predicates, the elements of a SweepArea are probed for expiration.
	 * 		  In analogy to the query-predicates, the predicate actually applied to this ListSA 
	 * 		  depends on the ID passed to the expiration and reorganization call, respectively.
	 * @param list The list maintaining the linkage of the implementor's elements.
	 * @param objectSize The object size of the elements in the sweeparea. 
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates
			, List<E> list, int objectSize) {
		
		this (impl, ID, selfReorganize, ReorganizeModes.LAZY_REORGANIZE, queryPredicates, removePredicates, list, objectSize);
	}
	
	
	/**
	 * Constructs a new ListSA.
	 *  
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 * @param removePredicates An array of binary remove-predicates utilized during the reorganization.
	 * 		  With the help of this predicates, the elements of a SweepArea are probed for expiration.
	 * 		  In analogy to the query-predicates, the predicate actually applied to this ListSA 
	 * 		  depends on the ID passed to the expiration and reorganization call, respectively.
	 * @param list The list maintaining the linkage of the implementor's elements.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, List<E> list) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicates, list, MemoryMonitorable.SIZE_UNKNOWN);		
	}

	
	/**
	 * Constructs a new ListSA and uses the predicate <code>removePredicate</code> as
	 * default during reorganization. 
	 * 
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 * @param removePredicate Default predicate for reorganization, which is applied independently 
	 * 		  from the ID passed to reorganization and expiration calls.
	 * @param list The list maintaining the linkage of the implementor's elements.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E> removePredicate, List<E> list, int objectSize) {
		super(impl, ID, selfReorganize, queryPredicates, removePredicate, objectSize);
		this.list = list;
		this.reorganizeMode = ReorganizeModes.LAZY_REORGANIZE;
	}		
	
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E> removePredicate, List<E> list) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicate, list, SIZE_UNKNOWN);		
	}
	/**
	 * Constructs a new ListSA, but performs no reorganization. 
	 * This is achieved by internally reorganizing with 
	 * <code>Predicates.FALSE</code>.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 * @param list The list maintaining the linkage of the implementor's elements.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, List<E> list) {
		super(impl, ID, selfReorganize, queryPredicates);
		this.list = list;
		this.reorganizeMode = ReorganizeModes.LAZY_REORGANIZE;
	}
	
	/**
	 * Constructs a new ListSA with a generally applicable query- and reorganization predicate. 
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicate A generally applicable binary query-predicate used to probe this ListSA.
	 * 		  Hence, this predicate is applied independently from the ID passed to query calls.
	 * @param removePredicate A generally applicable binary remove-predicates utilized during the reorganization.
	 * 		  With the help of this predicates, the elements of a ListSA are probed for expiration.
	 * 		  This predicate is applied independently from the ID passed to reorganization and expiration calls.
	 * @param dim The dimensionality of this ListSA, i.e., the number of possible inputs or in other words
	 * 		  the number of different IDs that can be passed to method calls of this ListSA. 
	 * @param list The list maintaining the linkage of the implementor's elements.
	 * @param objectSize The object size of the elements in the sweeparea.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, ReorganizeModes reorganizeMode, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate
			, int dim, List<E> list, int objectSize) {
		super(impl, ID, selfReorganize, queryPredicate, removePredicate, dim, objectSize);
		this.list = list;
		this.reorganizeMode = reorganizeMode;
	}


	/**
	 * Constructs a new ListSA with a generally applicable query- and reorganization predicate. 
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicate A generally applicable binary query-predicate used to probe this ListSA.
	 * 		  Hence, this predicate is applied independently from the ID passed to query calls.
	 * @param removePredicate A generally applicable binary remove-predicates utilized during the reorganization.
	 * 		  With the help of this predicates, the elements of a ListSA are probed for expiration.
	 * 		  This predicate is applied independently from the ID passed to reorganization and expiration calls.
	 * @param dim The dimensionality of this ListSA, i.e., the number of possible inputs or in other words
	 * 		  the number of different IDs that can be passed to method calls of this ListSA. 
	 * @param list The list maintaining the linkage of the implementor's elements.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, int dim, List<E> list) {
		super(impl, ID, selfReorganize, queryPredicate, removePredicate, dim);
		this.list = list;
		this.reorganizeMode = ReorganizeModes.LAZY_REORGANIZE;
	}
	
	/**
	 * Constructs a new ListSA with a generally applicable query- and reorganization predicate.
	 * This ListSA performs no reorganization. This is achieved by internally reorganizing with 
	 * <code>Predicates.FALSE</code>.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicate A generally applicable binary query-predicate used to probe this ListSA.
	 * 		  Hence, this predicate is applied independently from the ID passed to query calls.
	 * @param dim The dimensionality of this ListSA, i.e., the number of possible inputs or in other words
	 * 		  the number of different IDs that can be passed to method calls of this ListSA. 
	 * @param list The list maintaining the linkage of the implementor's elements.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, int dim, List<E> list) {
		super(impl, ID, selfReorganize, queryPredicate, dim);
		this.list = list;
		this.reorganizeMode = ReorganizeModes.LAZY_REORGANIZE;
	}

	/**
	 * Constructs a new ListSA with a generally applicable query- and reorganization predicate.
	 * This ListSA uses <code>Equal.DEFAULT_INSTANCE</code> for querying and 
	 * <code>new Not(Equal.DEFAULT_INSTANCE)</code> for reorganization. A self-reorganization
	 * is permitted.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param dim The dimensionality of this ListSA, i.e., the number of possible inputs or in other words
	 * 		  the number of different IDs that can be passed to method calls of this ListSA. 
	 * @param list The list maintaining the linkage of the implementor's elements.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, int dim, List<E> list) {
		super(impl, ID, dim);
		this.list = list;
		this.reorganizeMode = ReorganizeModes.LAZY_REORGANIZE;
	}
	
	/**
	 * Constructs a new ListSA. 
	 * The elements of the implementor are linked
	 * by an {@link java.util.ArrayList ArrayList}.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 * @param removePredicates An array of binary remove-predicates utilized during the reorganization.
	 * 		  With the help of this predicates, the elements of a ListSA are probed for expiration.
	 * 		  In analogy to the query-predicates, the predicate actually applied to this ListSA 
	 * 		  depends on the ID passed to the expiration and reorganization call, respectively.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicates, new ArrayList<E>());
	}
	
	/**
	 * Constructs a new ListSA and uses the predicate <code>removePredicate</code> as
	 * default during reorganization. The elements of the implementor are linked
	 * by an {@link java.util.ArrayList ArrayList}.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 * @param removePredicate Default predicate for reorganization, which is applied independently 
	 * 		  from the ID passed to reorganization and expiration calls.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E> removePredicate) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicate, new ArrayList<E>());
	}
	
	/**
	 * Constructs a new ListSA, but performs no reorganization. 
	 * This is achieved by internally reorganizing with 
	 * <code>Predicates.FALSE</code>. The elements of the implementor are linked
	 * by an {@link java.util.ArrayList ArrayList}.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicates An array of binary query-predicates used to probe this ListSA. 
	 * 		  Depending on the ID passed to the query-call, different query-predicates can be chosen
	 * 		  for retrieval.
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates) {
		this(impl, ID, selfReorganize, queryPredicates, new ArrayList<E>());
	}

	/**
	 * Constructs a new ListSA with a generally applicable query- and 
	 * reorganization predicate. 
	 * The elements of the implementor are linked
	 * by an {@link java.util.ArrayList ArrayList}.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicate A generally applicable binary query-predicate used to probe this ListSA.
	 * 		  Hence, this predicate is applied independently from the ID passed to query calls.
	 * @param removePredicate A generally applicable binary remove-predicates utilized during the reorganization.
	 * 		  With the help of this predicates, the elements of a SweepArea are probed for expiration.
	 * 		  This predicate is applied independently from the ID passed to reorganization and expiration calls.
	 * @param dim The dimensionality of this ListSA, i.e., the number of possible inputs or in other words
	 * 		  the number of different IDs that can be passed to method calls of this SweepArea. 
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, int dim) {
		this(impl, ID, selfReorganize, queryPredicate, removePredicate, dim, new ArrayList<E>());
	}
	
	/**
	 * Constructs a new ListSA with a generally applicable query- and reorganization predicate.
	 * This ListSA performs no reorganization. This is achieved by internally reorganizing with 
	 * <code>Predicates.FALSE</code>. The elements of the implementor are linked
	 * by an {@link java.util.ArrayList ArrayList}.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param selfReorganize A flag to determine if this ListSA can reorganize itself.
	 * @param queryPredicate A generally applicable binary query-predicate used to probe this ListSA.
	 * 		  Hence, this predicate is applied independently from the ID passed to query calls.
	 * @param dim The dimensionality of this ListSA, i.e., the number of possible inputs or in other words
	 * 		  the number of different IDs that can be passed to method calls of this ListSA. 
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, int dim) {
		this(impl, ID, selfReorganize, queryPredicate, dim, new ArrayList<E>());
	}

	/**
	 * Constructs a new ListSA with a generally applicable query- and reorganization predicate.
	 * This ListSA uses <code>Equal.DEFAULT_INSTANCE</code> for querying and 
	 * <code>new Not(Equal.DEFAULT_INSTANCE)</code> for reorganization. A self-reorganization
	 * is permitted. The elements of the implementor are linked
	 * by an {@link java.util.ArrayList ArrayList}.
	 * 
	 * @param impl The underlying implementor.
	 * @param ID The ID of this ListSA.
	 * @param dim The dimensionality of this ListSA, i.e., the number of possible inputs or in other words
	 * 		  the number of different IDs that can be passed to method calls of this ListSA. 
	 */
	public ListSA(SweepAreaImplementor<E> impl, int ID, int dim) {
		this(impl, ID, dim, new ArrayList<E>());
	}

	/**
	 * First inserts the given element into the list and
	 * then inserts it into the implementor. 
	 * 
	 * @param o The object to be inserted.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException
	 * 		if something goes wrong with the insertion due to the passed argument.
	 */
	public void insert(E o) throws IllegalArgumentException {
		super.insert(o);
		list.add(o);
	}
	
	/**
	 * Returns an iterator over the elements of this 
	 * ListSA by delivering an iterator over the list.
	 * Returns <code>impl.iterator()</code>.
	 * 
	 * @return An iterator over this ListSA.
	 */
	public Iterator<E> iterator() {
		return list.iterator();
	}

	/**
	 * Clears this ListSA by clearing the list and the
	 * underlying implementor.
	 */
	public void clear() {
		list.clear();
		super.clear();
	}

	/**
	 * Closes this ListSA. First the list is cleared.
	 * Second the underlying implementor is closed.
	 */
	public void close() {
		list.clear();
		super.close();
	}
	
	/**
	 * Determines which elements in this ListSA expire with respect to the object
	 * <tt>currentStatus</tt> and an <tt>ID</tt>. The latter is commonly used
	 * to differ by which input this reorganization step is initiated.<br>
	 * If no elements qualify for removal, an empty cursor is returned and all 
	 * elements are remained. <br>
	 * In order to remove the expired elements, either the returned iterator has to 
	 * support and execute the remove operation for each expired element during traversal
	 * or the {@link #reorganize(Object, int)} has to be overwritten to perform 
	 * the final removal. <p>
	 * A cursor is returned that delivers the elements of this ListSA by
	 * traversing the linkage as long as <code>removePredicates[ID].invoke(list.get(0), currentStatus)</code>
	 * holds. Consuming element of this cursor implies that it is removed from the list as well 
	 * as the implementor.
	 * 
	 * @param currentStatus The object containing the necessary information
	 * 		to detect expired elements.
	 * @param ID An ID determining from which input this method
	 * 		is triggered.
	 * @return an iteration over the elements in this ListSA that expire with
	 *         respect to the object <tt>currentStatus</tt> and an <tt>ID</tt>.
	 * @throws UnsupportedOperationException An UnsupportedOperationException is thrown, if
	 * 		this method is not supported by this ListSA.
	 * @throws IllegalStateException Throws an IllegalStateException if
	 * 		   this method is called at an invalid state.
	 */
	public Iterator<E> expire (final E currentStatus, final int ID) throws UnsupportedOperationException, IllegalStateException {
		if (selfReorganize || this.ID != ID) { 
			if (reorganizeMode == ReorganizeModes.LAZY_REORGANIZE) {
				return new AbstractCursor<E>() {
					public boolean hasNextObject() {
						if (list.size() == 0)
							return false;
						return removePredicates[ID].invoke(list.get(0), currentStatus);
					}
					
					public E nextObject() {
						next = list.remove(0);
						impl.remove(next);
						return next;
					}
				};
			}
			else if (reorganizeMode == ReorganizeModes.SAVEMEMORY_REORGANIZE) {
				return new AbstractCursor<E>() {
					
					int position = 0;
					
					public boolean hasNextObject() {
						while(position < list.size() && !removePredicates[ID].invoke(list.get(position), currentStatus))
							position++;
						return list.size() > position; 
					}
					
					public E nextObject() {
						next = list.remove(position);
						impl.remove(next);
						return next;
					}
				};				
			}
		}
		return new EmptyCursor<E>();
	}

	/**
	 * In contrast to the method {@link #expire(Object, int)}, this method removes
	 * all expired elements from a ListSA without returning them. 
	 * The reorganization follows the internal linkage provided 
	 * by a ListSA. 
	 * In the lazy reorganization mode, as long as 
	 * <code>removePredicates[ID].invoke(list.get(0), currentStatus)</code>
	 * holds <code>impl.remove(list.remove(0))</code> is called. 
	 * In the save memory reorganization mode, the whole list is checked for
	 * exprired elements<BR>
	 * 
	 * @param currentStatus The object containing the necessary information
	 * 		  to perform the reorganization step.
	 * @param ID An ID determining from which input this reorganization step
	 * 		   is triggered.
	 * @throws UnsupportedOperationException An UnsupportedOperationException is thrown, if
	 * 		   is method is not supported by this ListSA.
	 * @throws IllegalStateException Throws an IllegalStateException if
	 * 		   this method is called at an invalid state.
	 */
	public void reorganize(E currentStatus, int ID) throws UnsupportedOperationException, IllegalStateException {
		if (selfReorganize || this.ID != ID) {
			if (reorganizeMode == ReorganizeModes.LAZY_REORGANIZE) {
				while(list.size() > 0 && removePredicates[ID].invoke(list.get(0), currentStatus))
					impl.remove(list.remove(0));
			}
			else if (reorganizeMode == ReorganizeModes.SAVEMEMORY_REORGANIZE) {
				Iterator<E> it = list.iterator();
				while (it.hasNext()) {
					E next = it.next();
					if (removePredicates[ID].invoke(next, currentStatus)) {
						it.remove();
						impl.remove(next);
					}			
				}	
			}
		}
	}
	
	/**
	 * Returns the current reorganization mode.
	 * 
	 * @return The current reorganization mode.
	 */
	public ReorganizeModes getReorganizeMode() {
		return reorganizeMode;		
	}

	/**
	 * Sets the reorganization mode.
	 * 
	 * @param reorganizeMode The new reorganization mode. 
	 */
	public void setReorganizeMode(ReorganizeModes reorganizeMode) {
		this.reorganizeMode = reorganizeMode;		
	}

}
