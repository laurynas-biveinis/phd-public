package xxl.tests.geotools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xxl.connectivity.geotools.FeatureTuple;
import xxl.connectivity.geotools.FeatureTupleConverter;
import xxl.connectivity.geotools.ShapefileFeatureImport;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.collections.containers.io.MultiBlockContainer;
import xxl.core.cursors.Cursor;
import xxl.core.util.Arrays;
import xxl.core.util.XXLSystem;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FeatureTupleConverter.
 */
public class TestFeatureTupleConverter {
	
	/** This main-method contains a usage example for the serialization of FeatureTuples. First
	 *  a ShapeFile is read, before the <code>FeatureTuples</code> contained in this file are written to a {@link MultiBlockContainer}.
	 * @param args you can specify a Shape-file to read by giving the name of the *.shp-file
	 *             If no parameter is given, the program reads a default-file.
	 * @throws IOException if the file cannot be read or the container cannot be written
	 */
	public static void main(String [] args) throws IOException{

		File input = new File( args.length> 0 
				? args[0]
				: XXLSystem.getDataPath(new String[]{"geo","shp","houston_rails.shp"})
			);		

		// öffnet Shapefile und speichert die Features als Tupel in einem Container
		Cursor<FeatureTuple> cursor = new ShapefileFeatureImport(input).getFeatureTuples();			
			
		FeatureTupleConverter converter = FeatureTupleConverter.getConverter( cursor.peek() );
		String[] attributeNames = cursor.peek().getAttributeNames();
		
		List<Long> featureIds = new ArrayList<Long>();
		MultiBlockContainer mContainer = new MultiBlockContainer(XXLSystem.getOutPath()+"/FeatureTupleConverterTest", 32); 
		Container container = new ConverterContainer(
					mContainer,
					converter
				);
		
		while(cursor.hasNext())
			featureIds.add( (Long) container.insert(cursor.next()) );
		
		Arrays.print(attributeNames, System.out);
		System.out.println();
		
		for(Long id : featureIds){
			Object t = container.get(id);
			System.out.println(t);
		}
		
		mContainer.delete();
	}

}
