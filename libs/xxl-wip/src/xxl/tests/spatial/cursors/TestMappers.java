package xxl.tests.spatial.cursors;

import java.io.File;
import java.util.Iterator;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.io.Convertables;
import xxl.core.spatial.cursors.Mappers;
import xxl.core.spatial.cursors.PointInputCursor;
import xxl.core.spatial.cursors.UniverseFloat;
import xxl.core.spatial.rectangles.Rectangle;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Mappers.
 */
public class TestMappers {
	
	/**
	 * Use-case:<br>
	 * <ol> 
	 *     <li>compute universe for a file containing FloatPoints 
	 *     <li>map Points of input-iterator to new value
	 *     <li>write them to disk<br>
	 * </ol>
	 * <br><br>
	 *
	 * @param args: args[0] input file-name, args[1] dimensionality of the data, args[2] output file-name
   */
	public static void main(String[] args) {
		
		if (args.length != 3) {
			System.out.println("usage: java xxl.core.spatial.cursor.Mappers <input file-name> <dim> <output file-name>");
			return;	
		}

		final String inputFile = args[0];
		final int dim = Integer.parseInt(args[1]);
		final String outputFile = args[2];
                
		//1.) compute universe for the given file:
		Aggregator ag =
			new UniverseFloat(
				new PointInputCursor(new File(inputFile), PointInputCursor.FLOAT_POINT, dim, 1024*1024),
				dim
			)
		;

		Rectangle universe = (Rectangle) ag.last();
		ag.close();

		System.out.println("# The universe of "+inputFile+" dim "+dim+" is");
		System.out.println(universe);

		//2.) map Points to unit-cube:
		Iterator it = Mappers.mapPointToUnitCube(
			new PointInputCursor(new File(args[0]), PointInputCursor.FLOAT_POINT, dim, 1024*1024),
			universe
		);				

		//3.) write the file to disk:
		Convertables.write(outputFile, it);
	}

}
