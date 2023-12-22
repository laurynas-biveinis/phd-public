package xxl.tests.spatial.cursors;

import java.io.File;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.spatial.cursors.PointInputCursor;
import xxl.core.spatial.cursors.UniverseDouble;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UniverseDouble.
 */
public class TestUniverseDouble {

	/**
	 * use-case: compute universe for a file containing DoublePoints
	 *
	 * @param args: args[0] file-name, args[1] dimensionality of the data
	 **/
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("usage: java xxl.core.spatial.cursors.UniverseFunction <file-name> <dim>");
			return;	
		}
		final int dim = Integer.parseInt(args[1]);
		
		Aggregator ag =
			new UniverseDouble(
				new PointInputCursor(new File(args[0]), PointInputCursor.DOUBLE_POINT, dim, 1024*1024),
				dim
		);
		
		System.out.println("# The universe of "+args[0]+" dim "+args[1]+" is");
		System.out.println(ag.last());
		ag.close();
	}

}
