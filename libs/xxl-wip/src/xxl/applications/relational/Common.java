/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.relational;

import java.io.File;

import xxl.core.util.XXLSystem;

/**
 * A class that contains common functionality for the relational examples.
 */
public class Common {
	
	/**
	 * This class is not instanciable.
	 */
	private Common() {}

	/** 
	 * Constructs the output directory and returns the path. The returned path
	 * contains a file separator at the end.
	 *
	 * @return the path to the output directory.
	 */
	public static String getOutPath() {
		String path = XXLSystem.getOutPath()
		              + System.getProperty("file.separator")
		              + "output"
		              + System.getProperty("file.separator")
		              + "applications"
		              + System.getProperty("file.separator")
		              + "relational";
		File f = new File(path);
		f.mkdirs();
		return path + System.getProperty("file.separator");
	}
	
	/** 
	 * Returns the data path. The returned path contains a file separator  at
	 * the end.
	 *
	 * @return the path to the data directory.
	 */
	public static String getDataPath() {
		return XXLSystem.getRootPath()
		       + System.getProperty("file.separator")
		       + "data"
		       + System.getProperty("file.separator")
		       + "relational"
		       + System.getProperty("file.separator");
	}
}
