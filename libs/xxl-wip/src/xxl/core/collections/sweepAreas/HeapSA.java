/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
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
		
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Predicate<? super E> equals, Heap<E> heap, int objectSize) {
		super(impl, ID, selfReorganize, queryPredicates, removePredicates, equals, objectSize);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Predicate<? super E> equals, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicates, removePredicates, equals);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E> removePredicate, Predicate<? super E> equals, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicates, removePredicate, equals);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E> equals, Heap<E> heap) {
		super(impl, ID, equals, selfReorganize, queryPredicates);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, Predicate<? super E> equals, int dim, Heap<E> heap, int objectSize) {
		super(impl, ID, selfReorganize, queryPredicate, removePredicate, equals, dim, objectSize);
		this.heap = heap;
	}

	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, Predicate<? super E> equals, int dim, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicate, removePredicate, equals, dim);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> equals, int dim, Heap<E> heap) {
		super(impl, ID, selfReorganize, queryPredicate, dim, equals);
		this.heap = heap;
	}

	public HeapSA(SweepAreaImplementor<E> impl, int ID, int dim, Heap<E> heap) {
		super(impl, ID, dim);
		this.heap = heap;
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Predicate<? super E> equals, Comparator<? super E> comparator, int objectSize) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicates, equals, new DynamicHeap<E>(comparator), objectSize);
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E>[] removePredicates, Predicate<? super E> equals, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicates, removePredicates, equals, new DynamicHeap<E>(comparator));
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E>[] queryPredicates, Predicate<? super E> equals, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicates, equals, new DynamicHeap<E>(comparator));
	}

	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> removePredicate, Predicate<? super E> equals, int dim, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicate, removePredicate, equals, dim, new DynamicHeap<E>(comparator));
	}
	
	public HeapSA(SweepAreaImplementor<E> impl, int ID, boolean selfReorganize, Predicate<? super E> queryPredicate, Predicate<? super E> equals, int dim, Comparator<? super E> comparator) {
		this(impl, ID, selfReorganize, queryPredicate, equals, dim, new DynamicHeap<E>(comparator));
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
	
	/**
	 * Returned iterator is destroying!
	 */
	public Iterator<E> destroyingIterator() {
		return new AbstractCursor<E>() {
			@Override
			public boolean hasNextObject() {
				return !heap.isEmpty();
			}
			
			@Override
			public E nextObject() {
				next = heap.dequeue();
				impl.remove(next);
				return next;
			}
		};
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
