/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.comparators;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The LexicographicalComparator creates a lexicographical ordering.
 * 
 * @param <T> the type of the objects to be compared.
 * @see java.util.Comparator
 */
public class LexicographicalComparator<T> implements Comparator<T>, Serializable {
	
	/**
	 * The comparators to be used for the different levels of the
	 * lexicographical ordering.
	 */
	protected Comparator<? super T>[] comparators;

	/**
	 * Creates a new LexicographicalComparator.
	 * 
	 * @param comparators the comparators to be used for the different levels
	 *        of the lexicographical ordering
	*/
	public LexicographicalComparator(Comparator<? super T>... comparators) {
		this.comparators = comparators;
	}

	/** 
	 * Compares its two arguments for order.
	 * 
	 * @param object1 the first object to be compared.
	 * @param object2 the second object to be compared.
	 * @return the result of the comparison.
	 */
	public int compare(T object1, T object2) {
		int result = 0;
		for (Comparator<? super T> comparator : comparators)
			if ((result = comparator.compare(object1, object2)) != 0)
				return result;
		return result;
	}
}
