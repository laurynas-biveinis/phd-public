/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.xml.relational.sax;

import xxl.applications.xml.Common;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.MetaDataCursor;

/**
 * This example reads and outputs an XML file with SAX, resets the
 * cursor and then performs an output again.
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class ResetExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		int source=0;
		if (args.length>0)
			source = Integer.parseInt(args[0]);
		MetaDataCursor cursor = Common.getSource(source);
		
		Cursors.println(cursor);
		Common.outputMetaData(cursor);

		System.out.println("================= RESET =========================");
		cursor.reset();
	
		Cursors.println(cursor);
		
		cursor.close();
	}
}
