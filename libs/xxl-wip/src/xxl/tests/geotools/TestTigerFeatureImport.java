package xxl.tests.geotools;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import xxl.connectivity.geotools.FeatureTuple;
import xxl.connectivity.geotools.TigerFeatureImport;
import xxl.connectivity.jts.visual.VisualGeometry2DCursor;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.util.XXLSystem;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class TigerFeatureImport.
 */
public class TestTigerFeatureImport {
		
	
	/** A use case to demonstrate how to open a Tiger-Line-dataset. 
	 * @param args you can specify a Tiger/Line-dataset to read by giving the name of one of
	 *        the tiger-files. If no parameter is given, the program reads a default-dataset.
	 * @throws IOException if there occurs any error opening the data-set 
	 */
	public static void main(String [] args) throws IOException{

		File input = new File( args.length> 0 
									? args[0]
									: XXLSystem.getDataPath(new String[]{"geo","tgr","la_tgr06001.rt1"})
								);		
		
		TigerFeatureImport tfi = new TigerFeatureImport(input);
		
		String feature = tfi.getFeatureNames()[0];					
		
		VisualOutput out = new VisualOutput(feature, tfi.getUniverse(feature),700);
		
		System.out.println(	"Features contained in this Tiger/Line feature-set: " +			
			Cursors.count(
				new VisualGeometry2DCursor(
					new Mapper<FeatureTuple, Geometry2D>(
							new AbstractFunction<FeatureTuple, Geometry2D>(){
								public Geometry2D invoke(FeatureTuple f){
									return f.getDefaultGeometry();
								}							
							},
							tfi.getFeatureTuples(feature)
					),  
					out,
					Color.red
				)
			)
		);
		out.repaint();
		System.out.println("MBR: "+tfi.getUniverse(feature));		
	}	

}
