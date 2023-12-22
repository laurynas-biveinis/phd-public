/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.Point2D;

import com.vividsolutions.jts.geom.Point;


/** Implementation of the {@link Point2D} interface based on JTS {@link Point}s.  
 */
public class Point2DAdapter extends Geometry2DAdapter implements Point2D{

	/** Sole constructor: wraps the given {@link Point} inside this object 
	 * @param point The JTS-Point to wrap
	 */
	public Point2DAdapter(Point point){
		super(point);	
	}	

	/** @inheritDoc
	 *  @see Point#getX()
	 */
	public double getX(){ 
		return ((Point) geometry).getX(); 
	}
	
	/** @inheritDoc
	 *  @see Point#getY()
	 */
	public double getY(){ 
		return ((Point) geometry).getY(); 
	}

	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public Point getJTSGeometry(){
		return (Point) super.getJTSGeometry();
	}
}
