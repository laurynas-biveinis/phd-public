/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts;

import xxl.core.spatial.geometries.GeometryCollection2D;

import com.vividsolutions.jts.geom.GeometryCollection;


/** Implementation of the {@link GeometryCollection2D} interface based on JTS {@link GeometryCollection}s.
 * @param <T> the type of the elements of this collection
 */
public class GeometryCollection2DAdapter<T extends Geometry2DAdapter> extends Geometry2DAdapter implements GeometryCollection2D<T> {
	
	/** Sole constructor: wraps the given {@link GeometryCollection} inside this object 
	 *  @param geometryCollection the JTS-GeometryCollection to wrap 
	 */	
	public GeometryCollection2DAdapter(GeometryCollection geometryCollection){
       	super(geometryCollection);		
 	}
	
	/** @inheritDoc
	 *  @see GeometryCollection#getNumGeometries()
	 */
	public int getNumGeometries() {
		return ((GeometryCollection) geometry).getNumGeometries();
	}
	
	/** @inheritDoc
	 *  @see GeometryCollection#getGeometryN(int)
	 */
	public T getGeometry(int n) {
		return (T) Geometry2DFactory.wrap( ((GeometryCollection) geometry).getGeometryN(n));
	}
	
	/** Returns the encapsulated Geometry in its declared type.
	 * @return the underlying JTS- Geometry	
	 */
	public GeometryCollection getJTSGeometry(){
		return (GeometryCollection) super.getJTSGeometry();
	}

}
