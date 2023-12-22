package xxl.tests.geotools;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import xxl.connectivity.geotools.FeatureTuple;
import xxl.connectivity.geotools.ShapefileFeatureImport;
import xxl.connectivity.jts.visual.VisualGeometry2DCursor;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.util.XXLSystem;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ShapefileFeatureImport.
 */
public class TestShapefileFeatureImport {
	
	
	
	
	/** A use case to demonstrate how to open a Shape-file-dataset. 
	 * @param args you can specify a Shape-file to read by giving the name of the *.shp-file
	 *             If no parameter is given, the program reads a default-file.
	 * @throws IOException if ther occurs any error reading the file
	 */
	public static void main(String [] args) throws IOException{

		File input = new File( args.length> 0 
									? args[0]
									: XXLSystem.getDataPath(new String[]{"geo","shp","houston_roads.shp"})
								);		
		
		ShapefileFeatureImport sfi = new ShapefileFeatureImport(input);
								
		
		VisualOutput out = new VisualOutput(input.getName(), sfi.getUniverse(),700);
		
		System.out.println(	"Features contained in this shapefile: " +			
			Cursors.count(
				new VisualGeometry2DCursor(
					new Mapper<FeatureTuple, Geometry2D>(
							new AbstractFunction<FeatureTuple, Geometry2D>(){
								public Geometry2D invoke(FeatureTuple f){
									return f.getDefaultGeometry();
								}							
							},
							sfi.getFeatureTuples()
					),  
					out,
					Color.red
				)
			)
		);
		out.repaint();
		System.out.println("MBR: "+sfi.getUniverse());		
	}	

}
