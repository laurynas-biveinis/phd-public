/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.MultiPoint2D;

import com.vividsolutions.jts.geom.MultiPoint;

/** Implementation of the {@link MultiPoint2D} interface based on JTS {@link MultiPoint}s.
 *  It does not provide additional functionality to <code>GeometryCollection2DAdapter</code> but is implemented to
 *  represent the complete OGC geometry model  
 */
public class MultiPoint2DAdapter extends GeometryCollection2DAdapter<Point2DAdapter> implements MultiPoint2D<Point2DAdapter> {

	/** Sole constructor: wraps the given {@link MultiPoint} inside this object 
	 * @param multiPoint The JTS-MultiPoint to wrap
	 */	
	public MultiPoint2DAdapter(MultiPoint multiPoint){
		super(multiPoint);
 	}		
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public MultiPoint getJTSGeometry(){
		return (MultiPoint) super.getJTSGeometry();
	}

}
