package xxl.tests.spatial.cursors;

import java.io.File;

import xxl.core.cursors.Cursor;
import xxl.core.spatial.points.Points;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class PointInputCursor.
 */
public class TestPointInputCursor {

	/**	Use-case. Read float-points from a flat file
	 *
	 *	@param args: args[0] file-name, args[1] dimensionality of the input-data
	*/
	public static void main(String[] args){

		if(args.length != 2){
			System.out.println("usage: java xxl.core.spatial.cursors.PointInputCursor <file-name> <dim>");
			return;	
		}

		//get an InputIterator for input-Points:
		Cursor c = Points.newFloatPointInputCursor( new File(args[0]), Integer.parseInt(args[1]) );

		while(c.hasNext()){
			System.out.println(c.next());
		}
		c.close();
		
	}

}
