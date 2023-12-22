/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.xml.relational.sax;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import xxl.applications.xml.Common;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.NestedLoopsJoin;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.relational.sax.Sax;

/** 
 * This example constructs a two MetaDataCursors out of one XML-Document.
 * Then, a nested loops join is performed. The result is included in the
 * "empty.xml"-structure and written to "join.xml".
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class JoinExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		System.out.println("Open two sources");
		MetaDataCursor cursor1 = Common.getSource(4);
		MetaDataCursor cursor2 = Common.getSource(5);
		
		System.out.println("Perform a nested loops join");
		MetaDataCursor cursor = new NestedLoopsJoin(cursor1,cursor2,null,ArrayTuple.FACTORY_METHOD,NestedLoopsJoin.Type.NATURAL_JOIN);

		//xxl.core.cursors.Cursors.println(cursor);
		
		try {
			OutputStream out = new FileOutputStream(Common.getXMLOutPath()+"join.xml");
			InputStream in = new FileInputStream((new java.io.File(Common.getXMLDataPath()+"empty.xml")));
			System.out.println("Write the result to join.xml");
			Sax.DBtoXML (in, out, cursor, Sax.DEFAULT_IDENTIFIER_MAP(),"/root/empty");
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}

		// here, it is extremly important to free all resources, because else the
		// thread of the sax.XMLMetaDataCursor will not be stopped.
		cursor.close();
		System.out.println("Everything closed!");
	}
}
