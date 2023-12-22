package xxl.tests.jts.io;

import java.io.File;
import java.util.Iterator;

import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.connectivity.jts.io.Geometry2DFileIO;
import xxl.core.cursors.Cursors;
import xxl.core.io.converters.Converter;
import xxl.core.spatial.geometries.Geometry2D;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Geometry2DFileIO.
 */
public class TestGeometry2DFileIO {

	/** USE CASE: Returns an Iterator of {@link xxl.core.spatial.geometries.Geometry2D Geometry2D}- objects 
	 *  read in from the given file  
	 * @param args Use Case Parameter: 
	 * 				<ul><li>the first parameter specifies the name of file to read;</li> 
	 * 					<li>the optional second parameter defines the buffersize to use</li>
	 * 					<li>the extension of the file specifies the converter to use (wkb, wkt, bin)</li>
	 * 				</ul>  
	 */
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("Reads binary encoded Geometries from the given File using a buffered input reader!");
			System.out.println("usage: java xxl.connectivity.jts.io.Geometry2DFileIO wkb-File [bufferSize]");
			return;
		}
		
		if(args[0].toLowerCase().endsWith(".wkb")){
			Converter<xxl.connectivity.jts.Geometry2DAdapter> converter = Geometry2DConverter.DEFAULT_INSTANCE;
			int bufferSize = args.length > 1  ? Integer.parseInt(args[1]) : 4096;
			Iterator<Geometry2D> it = Geometry2DFileIO.read(converter, new File(args[0]), bufferSize);
			System.out.println("Reading file "+args[0]+"...");
			long s= System.currentTimeMillis();
			int results = Cursors.count(it);		
			long e= System.currentTimeMillis();
			System.out.println(results+" geometries read in "+((e-s)/1000d)+"s");					
		} else 		
			System.out.println("Don't know how to read file: "+args[0]);		
	}	

}
