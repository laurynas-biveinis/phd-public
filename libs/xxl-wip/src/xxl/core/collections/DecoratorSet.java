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
import java.util.Iterator;
import java.util.Set;

import xxl.core.util.Decorator;

public class DecoratorSet<T> implements Set<T>, Decorator<Set<T>> {

	protected Set<T> set;
	
	public DecoratorSet(Set<T> set) {
		this.set = set;
	}
	
	public boolean add(T o) {
		return set.add(o);
	}

	public boolean addAll(Collection<? extends T> c) {
		return set.addAll(c);
	}

	public void clear() {
		set.clear();
	}

	public boolean contains(Object o) {
		return set.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	public boolean isEmpty() {
		return set.isEmpty();
	}

	public Iterator<T> iterator() {
		return set.iterator();
	}

	public boolean remove(Object o) {
		return set.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return set.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return set.retainAll(c);
	}

	public int size() {
		return set.size();
	}

	public Object[] toArray() {
		return set.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}
	
	@Override
	public Set<T> getDecoree() {
		return set;
	}

}
