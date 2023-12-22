/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.geotools;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.connectivity.jts.Geometry2DFactory;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.spatial.geometries.Geometry2D;

import com.vividsolutions.jts.geom.Geometry;

/** A {@link Feature} (see <a href="http://www.geotools.org" target=_blank>http://www.geotools.org</a> 
 *  for further explanation) connects spatial data with thematic attributes. This
 *  class wraps a GeoTools-{@link Feature}-object and publishes its main functionality
 *  to XXL, that means the original <code>Feature</code>-methods which return JTS-{@link Geometry}-objects
 *  are wrapped to return XXL-{@link Geometry2D}-objects.
 *	<br><br>
 *  See {@link FeatureTupleConverter}, {@link ShapefileFeatureImport} or {@link TigerFeatureImport}
 *  for some use-cases of this class.
 */
public class FeatureTuple extends ArrayTuple{

	/** the "scheme" of this feature: contains typeinformation for each attribute of this tuple */
	protected FeatureType featureType = null;
	
	/** the attribute index of the default geometry */
	protected int geometryIndex;			

	/** Creates a new  <code>FeatureTuple</code>-object encapsulating the given GeoTools-<code>Feature</code>.
	 * @param feature the <code>Feature</code> to wrap
	 */
	public FeatureTuple(Feature feature){		
		super(feature.getAttributes(null));
		featureType = feature.getFeatureType();
		geometryIndex = featureType.find(featureType.getDefaultGeometry());		
		for(int i=0; i< tuple.length; i++)			
			if(	Geometry.class.isAssignableFrom( featureType.getAttributeType(i).getType() ) )
				tuple[i] = Geometry2DFactory.wrap((Geometry) tuple[i]);
	}
	
	
	/** Returns the default-geometry connected to this feature.
	 * @return the default-geometry
	 */
	public Geometry2D getDefaultGeometry(){
		return (Geometry2D) tuple[geometryIndex];
	}		
	
	/** Sets the default-geometry of this feature to the given one.
	 * @param geometry the new default-geometry of this feature
	 */
	public void setDefaultGeometry(Geometry2D geometry){
		tuple[geometryIndex] = geometry;
	}
		
	
	/** Returns the column-index of the attribute named <it>attributeName</it>
	 * @param attributeName the name of the attribute of which the index is requested
	 * @return the index of the attribute
	 */
	public int getColumn(String attributeName){
		return featureType.find( attributeName ) + 1;
	}
	
	/** Returns the name of the attribute at column-index <it>columnIndex</it>
	 * @param columnIndex the index of the attribute of which the name is requested
	 * @return the name of the attribute at index <it> columnIndex</it>
	 */
	public String getAttributeName(int columnIndex){		
		return featureType.getAttributeType(columnIndex-1).getName();
	}
	
	
	/** Returns the type of the attribute at index <it>columnIndex</it> (in the range from 1 to <code>getColumnCount()</code> )
	 * @param columnIndex the index of the attribute of which the type is requested
	 * @return the type of the attribute at index <it> columnIndex</it>
	 */
	public Class<?> getAttributeType(int columnIndex){		
		Class<?> type = featureType.getAttributeType(columnIndex-1).getType();		
		return Geometry.class.isAssignableFrom(type) ? Geometry2DAdapter.class : type;
	}	

	/** Returns the type of the attribute named <it>attributeName</it>
	 * @param attributeName the name of the attribute of which the index is requested
	 * @return the type of the attribute at index <it> columnIndex</it>
	 */
	public Class<?> getAttributeType(String attributeName){		
		Class<?> type = featureType.getAttributeType(attributeName).getType();		
		return Geometry.class.isAssignableFrom(type) ? Geometry2DAdapter.class : type;
	}	
	
	/** Returns the names of all attributes of this feature.
	 * @return the names of all attributes of this feature.
	 */
	public String[] getAttributeNames(){
		String[] s = new String[tuple.length];
		for(int i=0; i< s.length; s[i] = getAttributeName(i+1), i++);
		return s;
	}
	
	/** Returns the types of all attributes of this feature.
	 * @return the types of all attributes of this feature.
	 */
		public Class<?>[] getAttributeTypes(){		
		Class<?>[] c = new Class[tuple.length];
		for(int i=0; i<c.length; c[i] = getAttributeType(i+1), i++);
		return c;
	}
}
