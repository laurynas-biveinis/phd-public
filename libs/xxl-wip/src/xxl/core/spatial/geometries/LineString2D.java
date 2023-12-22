/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries;

/** A LineString is a Curve with linear interpolation between points. Each consecutive pair of points defines a
 *  line segment.
 */
public interface LineString2D extends Curve2D{
	
	/** Returns the number of points in this LineString.
	 * @return the number of points in this LineString.
	 */
	public int getNumberOfPoints();
	
	/** Returns the n-th point in this LineString.
	 * @param n the number of the Point2D to return
	 * @return the specified Point2D in this LineString
	 */
	public Point2D getPoint(int n); 
}
