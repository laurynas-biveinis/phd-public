/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
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
