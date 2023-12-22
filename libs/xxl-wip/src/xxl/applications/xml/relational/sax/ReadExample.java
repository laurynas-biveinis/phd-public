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
 * This example constructs a MetaDataCursor out of an XML-Document
 * and then outputs the meta data and the data.
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class ReadExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		int source=0;
		if (args.length>0)
			source = Integer.parseInt(args[0]);
		MetaDataCursor cursor = Common.getSource(source);
		
		// It may be that the meta data is only accessible after the first tuple
		// has been read (if the location of the meta data is not given or the InputStream
		// is not resetable and the meta data is places behind the data).
		if (cursor.getMetaData() != null) {
			System.out.println("Meta data is known before first tuple is read");
			Common.outputMetaData(cursor);
			Cursors.println(cursor);
		}
		else {
			System.out.println("Meta data is known after the first tuple has been read");
			Cursors.println(cursor);
			Common.outputMetaData(cursor);
		}
		
		cursor.close();
	}
}
