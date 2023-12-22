/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.geotools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.geotools.feature.FeatureType;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.core.io.converters.BooleanConverter;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.Converters;
import xxl.core.relational.tuples.TupleConverter;

/** A <code>FeatureTupleConverter</code> combines several base-type-converters to
 *  seriialize the content of a {@link FeatureTuple}.
 *
 */
public class FeatureTupleConverter extends TupleConverter{
	
	/** Maps a converter to each <code>FeatureType</code> so that some computation can be saved if 
	 *  there already exists a converter for a requested FeatureType.
	 */
	final static protected Map<FeatureType, FeatureTupleConverter> featureTupleConverters = new HashMap<FeatureType, FeatureTupleConverter>();	
	
	
	/** Creates a new <code>FeatureTupleConverter</code> by searching base-type-converters for each
	 *  attribute of the given {@link FeatureTuple}.
	 *  <br><br> The constructor is protected to enforce the use of <code>getConverter(FeatureTuple t)</code> in
	 *  order to get an appropriate converter. 
	 * @param t The tuple whose attribute-information is used to retrieve appropriate base-type-converters 
	 */
	protected FeatureTupleConverter(FeatureTuple t) {
		super(true, Converters.getObjectConverter(BooleanConverter.DEFAULT_INSTANCE));
		
		ArrayList<Converter<Object>> converters = new ArrayList<Converter<Object>>(t.getColumnCount());
		
		for(int i=0; i< t.getColumnCount(); i++){
			 if( Geometry2DAdapter.class.isAssignableFrom(t.getAttributeType(i+1)) )					 
				converters.add(i, Converters.getObjectConverter(Geometry2DConverter.DEFAULT_INSTANCE));
			 else converters.add(i, Converters.getConverterForJavaType( t.getAttributeType(i+1).getCanonicalName()));
		}
		this.converters = converters;
	}	
	
	
	/** Returns a converter-object for the given feature. First a map is queried for an appropriate
	 *  converter and a new converter is created only if the map doesn't already contain one for the given
	 *  feature-type.  
	 * @param featureTuple The tuple whose attribute-information is used to retrieve appropriate base-type-converters
	 * @return the converter-object for the given <code>FeatureTuple</code>
	 */
	public static FeatureTupleConverter getConverter(FeatureTuple featureTuple){
		FeatureTupleConverter converter = featureTupleConverters.get(featureTuple.featureType); 
		if( converter == null){
			converter = new FeatureTupleConverter(featureTuple);
			featureTupleConverters.put(featureTuple.featureType, converter);
		}			
		return converter;
	}
	
}
