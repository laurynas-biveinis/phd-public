/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.LinearRing2D;

import com.vividsolutions.jts.geom.LinearRing;

/** Implementation of the {@link LinearRing2D} interface based on JTS {@link LinearRing}s.
 *  It does not provide additional functionality to <code>LineString2DAdapter</code> but is implemented to
 *  represent the complete OGC geometry model  
 */
public class LinearRing2DAdapter extends LineString2DAdapter implements LinearRing2D{

	/** Sole constructor: wraps the given {@link LinearRing} inside this object 
	 * @param linearRing the JTS-LinearRing to wrap
	 */
	public LinearRing2DAdapter(LinearRing linearRing) {
		super(linearRing);
	}
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public LinearRing getJTSGeometry(){
		return (LinearRing) super.getJTSGeometry();
	}
}
