/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.math.Maths;
import xxl.core.spatial.LpMetric;

/**
 * An interface for determining the distance between two objects (see also
 * Comparable <--> Comparator).
 * 
 * @param <T> the type of the objects whose distance can be determined by this
 *        class.
 * @see xxl.core.util.DistanceTo
 */
public interface Distance<T> {

	/**
	 * A kind of factory method that returns a new distance function
	 * according to the L_1 metric ("manhatten" metric).
	 */
	public static final Function<Object, Distance> MANHATTEN = new AbstractFunction<Object, Distance>() {
		public Distance invoke() {
			return LpMetric.MANHATTEN;
		}
	};

	/**
	 * A kind of factory method that returns a new distance function
	 * according to the L_2 metric ("euclidean" metric).
	 */
	public static final Function<Object, Distance> EUCLIDEAN = new AbstractFunction<Object, Distance>() {
		public Distance invoke() {
			return LpMetric.EUCLIDEAN;
		}
	};

	/**
	 * A kind of factory method that returns a new distance function
	 * according to the Levenshtein distance.
	 */
	public static final Function<Object, Distance> LEVENSHTEIN = new AbstractFunction<Object, Distance>() {
		public Distance invoke() {
			return new Distance () {
				public double distance(Object object1, Object object2) {
					return Maths.levenshteinDistance((String)object1, (String)object2);
				}
			};
		}
	};

	/**
	 * Computes the distance between the given objects.
	 * 
	 * @param object1 first object
	 * @param object2 second object
	 * @return returns the distance between given objects 
	 */
	public abstract double distance(T object1, T object2);

}
