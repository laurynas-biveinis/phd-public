/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import xxl.core.util.Decorator;

public class DecoratorSortedSet<T> implements SortedSet<T>, Decorator<SortedSet<T>> {

	protected SortedSet<T> sortedSet;
	
	public DecoratorSortedSet(SortedSet<T> sortedSet) {
		this.sortedSet = sortedSet;		
	}

	public Comparator<? super T> comparator() {
		return sortedSet.comparator();
	}

	public T first() {
		return sortedSet.first();
	}

	public SortedSet<T> headSet(T toElement) {
		return sortedSet.headSet(toElement);
	}

	public T last() {
		return sortedSet.last();
	}

	public SortedSet<T> subSet(T fromElement, T toElement) {
		return sortedSet.subSet(fromElement, toElement);
	}

	public SortedSet<T> tailSet(T fromElement) {
		return sortedSet.tailSet(fromElement);
	}

	public boolean add(T o) {
		return sortedSet.add(o);
	}

	public boolean addAll(Collection<? extends T> c) {
		return sortedSet.addAll(c);
	}

	public void clear() {
		sortedSet.clear();
	}

	public boolean contains(Object o) {
		return sortedSet.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return sortedSet.containsAll(c);
	}

	public boolean isEmpty() {
		return sortedSet.isEmpty();
	}

	public Iterator<T> iterator() {
		return sortedSet.iterator();
	}

	public boolean remove(Object o) {
		return sortedSet.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return sortedSet.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return sortedSet.retainAll(c);
	}

	public int size() {
		return sortedSet.size();
	}

	public Object[] toArray() {
		return sortedSet.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return sortedSet.toArray(a);
	}
	
	@Override
	public SortedSet<T> getDecoree() {
		return sortedSet;
	}

}
