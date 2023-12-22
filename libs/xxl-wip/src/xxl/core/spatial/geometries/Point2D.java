/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries;


/** A Point2D is a 0-dimensional geometry and represents a single location in coordinate space. 
 *  It has a x-coordinate value and a y-coordinate value and its boundary is the empty set.
 *  <br><br>See <a href="./doc-files/ogc_sfs.pdf">Simple Feature Specification (pdf)</a>.
 */
public interface Point2D extends Geometry2D{

	/** The x-coordinate value for this Point.
	 * @return the x- coordinate value for this Point.
	 */
	public double getX();

	/** The y-coordinate value for this Point.
	 * @return the y- coordinate value for this Point.
	 */
	public double getY();			
}
