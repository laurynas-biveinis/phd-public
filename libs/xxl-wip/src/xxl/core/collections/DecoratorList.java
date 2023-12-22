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
import java.util.List;
import java.util.ListIterator;

import xxl.core.util.Decorator;

public class DecoratorList<T> implements List<T>, Decorator<List<T>> {

	protected List<T> list;
	
	public DecoratorList(List<T> list) {
		this.list = list;
	}
	
	public boolean add(T arg0) {
		return list.add(arg0);
	}

	public void add(int arg0, T arg1) {
		list.add(arg0, arg1);
	}

	public boolean addAll(Collection<? extends T> arg0) {
		return list.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		return list.addAll(arg0, arg1);
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object arg0) {
		return list.contains(arg0);
	}

	public boolean containsAll(Collection<?> arg0) {
		return list.containsAll(arg0);
	}

	public T get(int arg0) {
		return list.get(arg0);
	}

	public int indexOf(Object arg0) {
		return list.indexOf(arg0);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<T> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object arg0) {
		return list.lastIndexOf(arg0);
	}

	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	public ListIterator<T> listIterator(int arg0) {
		return list.listIterator(arg0);
	}

	public boolean remove(Object arg0) {
		return list.remove(arg0);
	}

	public T remove(int arg0) {
		return list.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0) {
		return list.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0) {
		return list.retainAll(arg0);
	}

	public T set(int arg0, T arg1) {
		return list.set(arg0, arg1);
	}

	public int size() {
		return list.size();
	}

	public List<T> subList(int arg0, int arg1) {
		return list.subList(arg0, arg1);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <A> A[] toArray(A[] arg0) {
		return list.toArray(arg0);
	}
	
	@Override
	public List<T> getDecoree() {
		return list;
	}

}
