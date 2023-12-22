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

import org.geotools.data.tiger.TigerDataStore;
import org.geotools.feature.Feature;

import xxl.connectivity.jts.Geometry2DFactory;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.util.WrappingRuntimeException;

import com.vividsolutions.jts.geom.Envelope;

/** A class to read Tiger/Line-files:   <br>
 *  Each Tiger/Line-dataset contains one or more feature-sets, e.g. roads, railways,... .
 *  You can access the names of the feature-sets via {@link TigerFeatureImport#getFeatureNames()}
 *  and retrieve the features of these sets by {@link TigerFeatureImport#getFeatureTuples(String)}, which
 *  returns a cursor of {@link FeatureTuple}-objects.
 *
 */
public class TigerFeatureImport{
	
	/**
	 * The Tiger/Line-dataset.
	 */
	private TigerDataStore dataStore = null;
	
	
	/** Creates a new {@link TigerFeatureImport}-object and opens the 
	 *  Tiger/Line-dataset specified by one of the Tiger/Line-files 
	 * @param tigerFile one of the files of the Tiger/Line-dataset to read
	 */
	public TigerFeatureImport(File tigerFile){	
		dataStore = new TigerDataStore(tigerFile.getParent());		
	}
	
	
	/** Returns the names of the Feature-sets contained in the current dataset
	 * @return the names of the Feature-sets contained in the current dataset
	 */
	public String[] getFeatureNames(){
		return dataStore.getTypeNames();
	}
	
	
	/** Returns the bounding rectangle of the feature-set specified by the given name
	 * @param featureName the name of the feature-set of which the bounding rectangle is requested
	 * @return the bounding rectangle of the feature-set specified by the given name
	 */
	public Rectangle getUniverse(String featureName){		
		Envelope e = null; 
		try {	
			e= dataStore.getFeatureSource(featureName).getBounds();		
			if(e== null)
				e = dataStore.getFeatureSource(featureName).getFeatures().getBounds();
		
			return Geometry2DFactory.envelopeToXXLRectangle(e);
		} catch (IOException e1) {
			throw new WrappingRuntimeException(e1);
		}	
	}
	
	
	/** Returns a cursor over the features contained in the feature-set specified by the given name.
	 * @param featureName the name of the feature-set of which the elements are requested
	 * @return a cursor over the features contained in the feature-set specified by the given name. 
	 */
	public Cursor<FeatureTuple> getFeatureTuples(String featureName){
		try {
			return	new Mapper<Feature, FeatureTuple>(
				new AbstractFunction<Feature, FeatureTuple>(){
					@Override
					public FeatureTuple invoke(Feature f){
						return new FeatureTuple(f);
					}
				},				
				dataStore.getFeatureSource(featureName).getFeatures().iterator() 
			);
		} catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}
}
