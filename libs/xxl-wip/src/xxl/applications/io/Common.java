/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.io;

import java.io.File;

import xxl.core.util.XXLSystem;

/**
 * Class that contains common functionality for the i/o examples.
 */
public class Common {
	/** This class is not instanciable */
	private Common () {
	}

	/** 
	 * Constructs the output directory and returns the path. The returned path contains a file separator 
	 * at the end.
	 *
	 * @return String - the OutPath
	 */
	public static String getOutPath() {
		String path = XXLSystem.getOutPath() + System.getProperty("file.separator") +
			"output" + System.getProperty("file.separator") + 
			"applications" + System.getProperty("file.separator") + 
			"io";
		File f = new File(path);
		f.mkdirs();
		return path + System.getProperty("file.separator");
	}

	/** 
	 * Returns the relational data path. The returned path contains a file separator 
	 * at the end.
	 *
	 * @return String - the data path to relational sample files
	 */
	public static String getRelationalDataPath() {
		return XXLSystem.getRootPath() + System.getProperty("file.separator") + 
			"data" + System.getProperty("file.separator") +
			"relational" + System.getProperty("file.separator");
	}
}
