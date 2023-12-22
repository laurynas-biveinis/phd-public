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

public class CountingComparator<T> extends DecoratorComparator<T> {

	protected long calls;
	
	public CountingComparator(Comparator<T> comparator) {
		super(comparator);
		calls = 0;
	}
	
	public int compare(T o1, T o2) {
		calls++;
		return super.compare(o1, o2);
	}
	
	public long getNoOfCalls() {
		return calls;
	}

	public void resetCounter() {
		this.calls = 0;
	}

}
