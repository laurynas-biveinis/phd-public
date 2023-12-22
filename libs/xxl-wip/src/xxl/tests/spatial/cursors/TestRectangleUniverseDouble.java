package xxl.tests.spatial.cursors;

import java.io.File;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.io.Convertable;
import xxl.core.io.Convertables;
import xxl.core.spatial.cursors.KPEInputCursor;
import xxl.core.spatial.cursors.Mappers;
import xxl.core.spatial.cursors.RectangleUniverseDouble;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RectangleUniverseDouble.
 */
public class TestRectangleUniverseDouble {

	/**
	 * use-case: compute universe for a file containing KPEs with
	 * DoubleRectangle data-objects (common data format for spatial data)
	 * and write aggregate rectangle to a file "file-name.universe".
	 *
	 * @param args: args[0] file-name, args[1] dimensionality of the data
	 */
	public static void main(String[] args){

		if(args.length != 2){
			System.out.println("usage: java xxl.core.spatial.cursors.UniverseFunction <file-name> <dim>");
			return;	
		}

		final int dim = Integer.parseInt(args[1]);
		
		Aggregator ag =
			new RectangleUniverseDouble(
					Mappers.mapKPEToData(
						new KPEInputCursor( new File(args[0]), 1024*1024, dim )
					),
					dim
			);
		
		Object last = ag.last();
		System.out.println("# The universe of "+args[0]+" dim "+args[1]+" is");
		System.out.println(last);
		ag.close();

		Convertables.write(args[0]+".universe",(Convertable)last);
	}

}
