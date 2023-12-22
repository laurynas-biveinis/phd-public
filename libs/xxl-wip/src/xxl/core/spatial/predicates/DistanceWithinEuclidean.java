/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.spatial.predicates;

import xxl.core.spatial.LpMetric;

/**
 *	A distance predicate based on the
 *	euclidean distance-measure (This distance-measure corresponds to the
 *	L_2-metric).
 *
 *  @see xxl.core.predicates.DistanceWithin
 *  @see xxl.core.spatial.LpMetric
 *
 */
public class DistanceWithinEuclidean extends xxl.core.predicates.DistanceWithin{

	/** Creates a new DistanceWithinEuclidean instance.
     *
	 * @param epsilon the double value represents the maximum distance
	 *        between two objects such that the predicate returns
	 *        <tt>true</tt>.
	 */
	public DistanceWithinEuclidean(double epsilon){
		super(LpMetric.EUCLIDEAN, epsilon);
	}
}
