/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.collections;

import java.util.Comparator;

import xxl.core.util.Decorator;

public class DecoratorComparator<T> implements Comparator<T>, Decorator<Comparator<T>> {

	protected Comparator<T> comparator;
	
	public DecoratorComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	public int compare(T o1, T o2) {
		return comparator.compare(o1, o2);
	}

	public Comparator<T> getDecoree() {
		return comparator;
	}

}
