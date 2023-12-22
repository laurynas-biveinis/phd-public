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

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.queues.Heap;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.predicates.Predicate;

/**
 * 
 */
public class HeapSA<E> extends ImplementorBasedSweepArea<E> {

	protected Heap<E> heap;
		
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Heap<E> heap, int objectSize) {
		super(impl, ID, selfReorganize, queryPredicates, removePredicates, objectSize);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicates, removePredicates);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E> removePredicate, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicates, removePredicate);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicates);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, int dim, Heap<E> heap, int objectSize) {
		super(impl, ID, selfReorganize, queryPredicate, removePredicate, dim, objectSize);
		this.heap = heap;
	}

	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, int dim, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicate, removePredicate, dim);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, int dim, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicate, dim);
		this.heap = heap;
	}

	public HeapSA(SweepAreaImplementor<E> impl, int ID, int dim, Heap<E> heap) {
		super(impl, ID, dim);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Comparator<? super E> comparator, int objectSize) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicates, new DynamicHeap<E>(comparator), objectSize);
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicates, new DynamicHeap<E>(comparator));
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicates, new DynamicHeap<E>(comparator));
	}

	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, int dim, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicate, removePredicate, dim, new DynamicHeap<E>(comparator));
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, int dim, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicate, dim, new DynamicHeap<E>(comparator));
	}

	public HeapSA(SweepAreaImplementor<E> impl, int ID, int dim, Comparator<? super E> comparator) {
		this(impl, ID, dim, new DynamicHeap<E>(comparator));
	}


	public void insert(E o) throws IllegalArgumentException {
		super.insert(o);
		heap.enqueue(o);
	}
	
	public void clear() {
		heap.clear();
		super.clear();
	}

	public void close() {
		heap.close();
		super.close();
	}
	
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException();
	}

	public Iterator<E> expire (final E currentStatus, final int ID) {
		if (selfReorganize || this.ID != ID) { 
			return new AbstractCursor<E>() {
				public boolean hasNextObject() {
					if (heap.isEmpty())
						return false;
					return removePredicates[ID].invoke(heap.peek(), currentStatus);
				}
				
				public E nextObject() {
					next = heap.dequeue();
					impl.remove(next);
					return next;
				}
			};
		}
		return new EmptyCursor<E>();
	}

	public void reorganize(E currentStatus, int ID) throws IllegalStateException {
		if (selfReorganize || this.ID != ID) {
			while(heap.size() > 0 && removePredicates[ID].invoke(heap.peek(), currentStatus))
				impl.remove(heap.dequeue());
		}
	}
	
	/**
	 * @return Returns a string representation of the sweeparea.
	 */
	public String toString() {
		return heap.toString();
	}

	
}
