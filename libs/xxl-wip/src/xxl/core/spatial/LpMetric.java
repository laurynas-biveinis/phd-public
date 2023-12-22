/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.spatial;

import xxl.core.spatial.points.Point;
import xxl.core.spatial.points.Points;
import xxl.core.util.Distance;

/**
 *	The L_p-Metric.
 *
 * @see xxl.core.spatial.points.Point
 *
 */
public class LpMetric implements Distance<Point> {

	/**
	 * The L_1 metric ("manhatten" metric).
	 */
	public static final LpMetric MANHATTEN = new LpMetric(1);

	/**
	 * The L_2 metric ("euclidean" metric).
	 */
	public static final LpMetric EUCLIDEAN = new LpMetric(2);

	/**
	 * The value p of the L_p metric.
	 */
	protected int p;

	/**
	 * Creates a new instance of the LpMetric.
	 *
	 * @param p the value for L_p.
	 */
	public LpMetric(int p){
		this.p = p;	
	}

	/**
	 * Returns the L_p distance of the given objects.
	 * 
	 * <p>Implementation:
	 * <pre><code>
	 *   public double distance(Object o1, Object o2){
	 *     return Points.lpDistance( (Point)o1, (Point)o2, p );
	 *  }
	 *
	 *  </code></pre>
	 * @param o1 first object
	 * @param o2 second object  
	 * @return returns the L_p distance of the given objects
	 */
	public double distance(Point o1, Point o2){
		return Points.lpDistance(o1, o2, p);
	}
}
