/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.tests.jts.io.experimental;

import java.io.File;
import java.io.IOException;

import xxl.connectivity.jts.io.experimental.ConverterPerformanceTest;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ConverterPerformanceTest.
 */
public class TestConverterPerformanceTest {
	
	/** The main-program: Repeatedly reads geometries from a file and writes them back to disk. 
	 * @param args Parameter 1 specifies the WKT-file to read and 
	 *             Parameter 2 determines the number of runs, that is 
	 *             how often the file is read in and written back.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		if (args.length == 2)
			ConverterPerformanceTest.test(new File(args[0]), Integer.parseInt(args[1]));		
	}

}
