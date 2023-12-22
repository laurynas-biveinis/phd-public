/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.xml.relational.dom;

import java.io.FileInputStream;

import org.w3c.dom.Document;

import xxl.applications.xml.Common;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.xml.relational.dom.Dom;
import xxl.core.xml.relational.dom.Xalan;

/** 
 * This example constructs a MetaDataCursor out of an XML-Document
 * and then tests the performance (time for a walk through).  
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class PerformanceTest
{
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args)
	{
		try {
			// Initializes the parser
			Dom.init(new Xalan());

			java.util.Map map = Dom.DEFAULT_IDENTIFIER_MAP();
			map.put("data","table");
			map.put("datacol","tr");
			map.put("datarow","td");

			long t0 = System.currentTimeMillis();
	 		
			Document document = Dom.InputStreamtoDOM(new FileInputStream(Common.getXMLDataPath()+"actors.html"));
			//Document document = Dom.InputStreamtoDOM(new FileInputStream(Common.getXMLDataPath()+"database.xml"));

			//map.remove("signed");
			
			MetaDataCursor cursor = Dom.DOMtoMetaDataCursor(document,"/table/tr[2]", null, null, map);
			//MetaDataCursor cursor = Dom.DOMtoMetaDataCursor(document,"/table/people/row","/table/people/meta", null, map);
			
			Common.outputMetaData(cursor);
			System.out.println();
			
			int numberOfTuples=0;
			long t1 = System.currentTimeMillis();
			
			numberOfTuples = Cursors.count(cursor);
			
			long t2 = System.currentTimeMillis();
			
			System.out.println(numberOfTuples+" tuples seen.");
			System.out.println ((t2-t1)+"ms");
			System.out.println ("Total time (including DOM tree creation): "+(t2-t0)+"ms");
			
			cursor.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
