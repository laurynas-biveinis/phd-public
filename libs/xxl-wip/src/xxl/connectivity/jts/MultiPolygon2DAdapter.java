/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.MultiPolygon2D;

import com.vividsolutions.jts.geom.MultiPolygon;

/** Implementation of the {@link MultiPolygon2D} interface based on JTS {@link MultiPolygon}s.
 *  It does not provide additional functionality to <code>GeometryCollection2DAdapter</code> but is implemented to
 *  represent the complete OGC geometry model  
 */
public class MultiPolygon2DAdapter extends GeometryCollection2DAdapter<Polygon2DAdapter> implements MultiPolygon2D<Polygon2DAdapter>{

	/** Sole constructor: wraps the given {@link MultiPolygon} inside this object 
	 * @param multiPolygon The JTS-MultiPolygon to wrap
	 */
	public MultiPolygon2DAdapter(MultiPolygon multiPolygon){
		super(multiPolygon);
 	}
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public MultiPolygon getJTSGeometry(){
		return (MultiPolygon) super.getJTSGeometry();
	}

}
