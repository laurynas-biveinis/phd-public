package xxl.tests.jts.io;

import java.io.File;
import java.util.Iterator;

import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.connectivity.jts.io.Geometry2DFileIO;
import xxl.connectivity.jts.io.UniverseAggregator;
import xxl.core.io.converters.Converter;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UniverseAggregator.
 */
public class TestUniverseAggregator {

	/**
	 * USE CASE: compute universe for a file containing geometries
	 * and write aggregated mbr to a file "file-name.universe".
	 *
	 * @param args args[0] file-name, args[1] dimensionality of the data	 
	 */
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println(	"Computes the universe of the geometries given by the specified wkb file and saves the result in the file 'inputFilename.universe'! ");
			System.out.println(	"usage: java xxl.connectivity.jts.util.UniverseAggregator file");
			return;
		}							
		
		if(args[0].toLowerCase().endsWith(".wkb")){
			Converter<xxl.connectivity.jts.Geometry2DAdapter> converter = Geometry2DConverter.DEFAULT_INSTANCE;
			Iterator<Geometry2D> iterator = Geometry2DFileIO.read(converter, new File(args[0]));
			
			Rectangle universe = new UniverseAggregator(iterator).last();
			System.out.println("Universe: "+universe);
			Rectangles.writeSingletonRectangle(new File(args[0]+".universe"),universe);
		} else
			System.out.println("Don't know how to read file: "+args[0]);			
	}

}
