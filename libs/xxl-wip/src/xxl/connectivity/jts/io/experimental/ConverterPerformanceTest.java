/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.jts.io.experimental;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.connectivity.jts.io.Geometry2DFileIO;
import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.connectivity.jts.io.WKTFileIO;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.io.converters.Converter;
import xxl.core.spatial.geometries.Geometry2D;

/**
 * Performs a simple speed-test of the different converters available for 
 * {@link Geometry2DAdapter}-objects by writing and reading a huge number 
 * of objects to and from a file-stream and comparing the elapsed time.
 *
 */
public class ConverterPerformanceTest {

	
	/** The test-program: Repeatedly reads geometries from a file and writes them back to disk. 
	 * @param f specifies the WKT-file to read.
	 * @param runs determines the number of runs, that is 
	 *             how often the file is read in and written back.
	 * @throws IOException
	 */
	public static void test(File f, int runs) throws IOException{
		LinkedList<Geometry2D> geometries = new LinkedList<Geometry2D>();
		
		Cursor<Geometry2D> cursor = WKTFileIO.read(f);		
		while(cursor.hasNext())
			geometries.add(cursor.next());

		for(int c=2; c<4; c++ ){
			Converter converter;
			switch(c){
				case 0  : converter = SerializableGeometryConverter.DEFAULT_INSTANCE; break;
				case 1  : converter = WKTGeometryConverter.DEFAULT_INSTANCE; break;
				case 2  : converter = JTSGeometryConverter.DEFAULT_INSTANCE; break;
				default : converter = Geometry2DConverter.DEFAULT_INSTANCE; break;
			}

			long s = System.currentTimeMillis();

			for(int i=0; i< runs; i++){
				System.out.println((i+1)+ ". run");				
				File temp = new File(f.getName()+".tmp");
				Geometry2DFileIO.write(geometries.iterator(), converter, temp);			
				if( Cursors.count( Geometry2DFileIO.read( converter, temp)) != geometries.size())
					System.out.println("Error!");
				temp.delete();
			}
			
			System.out.println(	"Serializing and deserializing "+(geometries.size()*runs)+
								" geometries took :" + (System.currentTimeMillis()-s)+
								"ms  with the "+converter.getClass().getSimpleName());
		
		}
	}
}
