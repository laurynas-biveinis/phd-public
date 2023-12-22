package xxl.tests.jts.io;

import java.io.File;
import java.util.Iterator;

import xxl.connectivity.jts.io.WKBFileIO;
import xxl.core.cursors.Cursors;
import xxl.core.spatial.geometries.Geometry2D;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class WKBFileIO.
 */
public class TestWKBFileIO {
	
	/** USE CASE: Returns an Iterator of {@link xxl.core.spatial.geometries.Geometry2D Geometry2D}- objects 
	 *  read in from the given text file  
	 * @param args Use Case Parameter: 
	 * 					the first parameter specifies the name of the WKB- file to read; 
	 * 					the optional second parameter defines the buffersize to use
	 */
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("Reads textencoded Geometries from the given text File using a buffered input reader!");
			System.out.println("usage: java xxl.connectivity.jts.io.WKBFileIO wkt-File [bufferSize]");
			return;
		}
		
		File file1 = new File(args[0]);
		int bufferSize = args.length > 1  ? Integer.parseInt(args[1]) : 4096;
		Iterator<Geometry2D> it = WKBFileIO.read(file1, bufferSize);
		System.out.println("Reading file "+file1.getName()+"...");
		long s= System.currentTimeMillis();		
		int results = Cursors.count(it);		
		long e= System.currentTimeMillis();
		System.out.println(results+" geometries read in "+((e-s)/1000d)+"s");						
	}	

}
