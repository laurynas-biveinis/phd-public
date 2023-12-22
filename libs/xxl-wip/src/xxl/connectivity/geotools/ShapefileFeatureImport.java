/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.geotools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;

import xxl.connectivity.jts.Geometry2DFactory;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.util.WrappingRuntimeException;

import com.vividsolutions.jts.geom.Envelope;

/** This class provides an import-function for ESRI-Shape-files, a common GIS-file-format.
 */
public class ShapefileFeatureImport{

	/**
	 * The shapefile-dataset.
	 */
	private ShapefileDataStore dataStore = null;

	/** Creates a new {@link ShapefileFeatureImport}-object and opens the 
	 *  Shapefile-dataset specified by the given name of the *.shp- file.
	 * @param shapeFile the name of the *.shp-file to read
	 */
	public ShapefileFeatureImport(File shapeFile){
		try {
			dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
		} catch (MalformedURLException e) {
			throw new WrappingRuntimeException(e);
		}				
	}

	/** Returns the bounding rectangle of the feature-set 
	 * @return the bounding rectangle of the feature-set 
	 */
	public Rectangle getUniverse(){
		Envelope e;
		try {
			e = dataStore.getFeatureSource().getBounds();
			if(e== null)
				e = dataStore.getFeatureSource().getFeatures().getBounds();
			
			return Geometry2DFactory.envelopeToXXLRectangle(e);
		} catch (IOException e1) {
			throw new WrappingRuntimeException(e1);
		}		
	}

	/** Returns a cursor over the features contained in the shape-file.
	 * @return a cursor over the features contained in the shape-file 
	 */
	public Cursor<FeatureTuple> getFeatureTuples(){
		try {
			return 	new Mapper<Feature, FeatureTuple>(
						new AbstractFunction<Feature, FeatureTuple>(){
							@Override
							public FeatureTuple invoke(Feature f){
								return new FeatureTuple(f);
							}
						},				
						dataStore.getFeatureSource().getFeatures().iterator() 
				);
		} catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}
}
