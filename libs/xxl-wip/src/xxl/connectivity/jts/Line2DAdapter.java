/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.Line2D;

import com.vividsolutions.jts.geom.LineString;

/** Implementation of the {@link Line2D} interface based on JTS {@link LineString}s.
 *  It does not provide additional functionality to <code>LineString2DAdapter</code> but is implemented to
 *  represent the complete OGC geometry model  
 */
public class Line2DAdapter extends LineString2DAdapter implements Line2D{

	/** Sole constructor: wraps the given {@link LineString} inside this object 
	 * @param lineString the JTS-LineString to wrap
	 */
	public Line2DAdapter(LineString lineString) {
		super(lineString); 
		if(geometry.getNumPoints()> 2) 
			throw new IllegalArgumentException("LineString "+lineString+" is not a Line!");
	}		
}
