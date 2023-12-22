package xxl.tests.spatial.cursors;

import java.io.File;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.spatial.cursors.RectangleInputIterator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RectangleInputIterator.
 */
public class TestRectangleInputIterator {

	/** use-case: read rectangles and print them to standard out
	 *
	 * @param args: args[0] file-name, args[1] dimension of the data
	 */
	public static void main(String[] args){

		if(args.length != 2){
			System.out.println("usage: java xxl.core.spatial.cursors.RectangleInputIterator <file-name> <dim>");
			return;	
		}

		Cursor c = new RectangleInputIterator(new File(args[0]), 1024*1024, Integer.parseInt(args[1]));

		Cursors.println(c);	//print elements of Cursor to standard output
		c.close();
	}

}
