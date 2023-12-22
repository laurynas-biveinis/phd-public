/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util;

/**
 * An interface for determining the distance between two objects (see also
 * Comparable <--> Comparator).
 * 
 * @param <T> the type of the object whose distance related to this object can
 *        be determined.
 * @see xxl.core.util.Distance
 */
public interface DistanceTo<T> {

	/**
	 * Computes the distance between this object and the given object.
	 * 
	 * @param object to calculate distance to.
	 * @return returns the distance between this object and the given one.
	 */
	public abstract double distanceTo(T object);
	
}
