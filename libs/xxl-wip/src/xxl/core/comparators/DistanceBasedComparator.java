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

import xxl.core.util.Distance;

/**
 * The DistanceBasedComparator compares two objects by determining
 * their distances to a given reference object.
 * The object which is closer to the reference object based on the distance
 * function will be seen as smaller object.
 *
 * @param <T> the type of the objects to be compared with this comparator.
 * @see java.util.Comparator
 * @see xxl.core.util.Distance
 */
public class DistanceBasedComparator<T> implements Comparator<T>, Serializable {

	/**
	 * Distance function to compute the distance of the given object to the
	 * reference object.
	 */
	protected Distance<? super T> distance;

	/**
	 * Object used as reference for the DistanceBasedComparator.
	 */
	protected T reference;

	/** 
	 * Constructs a new object of this class.
	 *
	 * @param reference reference object
	 * @param distance {@link xxl.core.util.Distance distance function} used for computing the distances
	 *        of the objects to compare
	 */
	public DistanceBasedComparator(T reference, Distance<? super T> distance) {
		this.reference = reference;
		this.distance = distance;
	}

	/** 
	 * Compares two objects based on the distance to a given reference object.
	 * For details see {@link java.util.Comparator}.
	 * 
	 * @param o1 the first object to be compared
	 * @param o2 the second object to be compared
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second wrt to the given
	 *         distance function and reference object
	 * @see java.util.Comparator
	 */
	public int compare(T o1, T o2) {
		double d1 = distance.distance(o1, reference);
		double d2 = distance.distance(o2, reference);
		return d1 < d2 ? -1 : d1 > d2 ? 1 : 0;
	}
}
