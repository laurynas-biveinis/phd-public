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
 * The EmbeddingComparator adds a bound to a given comparator.
 *  
 * @param <T> the type of the objects to be compared with this comparator.
 * @see java.util.Comparator
 */
public class EmbeddingComparator<T> implements Comparator<T>, Serializable {

	/** 
	 * The comparator to be wrapped.
	 */
	protected Comparator<? super T> comparator;

	/** 
	 * The bound of the comparator.
	 */
	protected T bound;

	/** 
	 * Determines whether the bound is the lower bound.
	 */
	protected boolean lowerBound;

	/**
	 * Creates a new EmbeddingComparator.
	 * 
	 * @param comparator the comparator to be wrapped.
	 * @param bound the bound of the comparator.
	 * @param lowerBound determines whether the bound is the lower bound.
	 */
	public EmbeddingComparator(Comparator<? super T> comparator, T bound, boolean lowerBound) {
		this.comparator = comparator;
		this.bound = bound;
		this.lowerBound = lowerBound;
	}

	/**
	 * Creates a new EmbeddingComparator.
	 * 
	 * @param comparator the comparator to be wrapped.
	 * @param lowerBound determines whether the bound is the lower bound.
	 */
	public EmbeddingComparator(Comparator<? super T> comparator, boolean lowerBound) {
		this(comparator, null, lowerBound);
	}

	/**
	 * Creates a new EmbeddingComparator.
	 * 
	 * @param comparator the comparator to be wrapped.
	 * @param bound the bound of the comparator.
	 */
	public EmbeddingComparator(Comparator<? super T> comparator, T bound) {
		this(comparator, bound, true);
	}
	
	/**
	 * Creates a new EmbeddingComparator.
	 * 
	 * @param comparator the comparator to be wrapped
	 */
	public EmbeddingComparator(Comparator<? super T> comparator) {
		this(comparator, null);
	}

	/**
	 * Compares its two arguments for order.
	 * 
	 * @param object1 the first object to be compared.
	 * @param object2 the second object to be compared.
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 */
	public int compare(T object1, T object2) {
		if (bound == object1 || (bound != null ? bound.equals(object1) : object1.equals(bound)))
			return lowerBound? Integer.MIN_VALUE: Integer.MAX_VALUE;
		if (bound == object2 || (bound != null ? bound.equals(object2) : object2.equals(bound)))
			return lowerBound? Integer.MAX_VALUE: Integer.MIN_VALUE;
		return comparator.compare(object1, object2);
	}
}
