package xxl.tests.spatial.cursors;

import java.io.File;

import xxl.core.spatial.cursors.KPEInputCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class KPEInputCursor.
 */
public class TestKPEInputCursor {
	
	/** use-case: read KPEs from a flat file.
	 *
	 *	@param args args[0] file name, args[1] dimension
	 */
	public static void main(String[] args){

		if(args.length != 2){
			System.out.println("usage: java xxl.core.spatial.cursors.KPEInputCursor <file-name> <dim>");
			return;	
		}

		KPEInputCursor cursor = new KPEInputCursor(new File(args[0]), 1024*1024, Integer.parseInt(args[1]));

		while(cursor.hasNext())
			System.out.println(cursor.next());
		cursor.close();
	}

}
