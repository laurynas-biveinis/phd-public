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
import java.util.Date;

import org.w3c.dom.Document;

import xxl.applications.xml.Common;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.xml.relational.dom.Dom;
import xxl.core.xml.relational.dom.Xalan;

/** 
 * This example constructs a MetaDataCursor out of an XML-Document
 * and then outputs the meta data and the data.
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class ReadExample
{
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		try {
			/* Initializes the parser */
			Dom.init(new Xalan());					
	 				
			Document document = Dom.InputStreamtoDOM(new FileInputStream(Common.getXMLDataPath()+"database.xml"));
			
			java.util.Map map = Dom.DEFAULT_IDENTIFIER_MAP();
			
			//map.remove("signed");
			
			MetaDataCursor cursor = Dom.DOMtoMetaDataCursor(document,"/table/people/row","/table/people/meta", null, map);
			
			Common.outputMetaData(cursor);
			
			long l1 = (new Date()).getTime();
	
			Common.output(cursor);
			
			long l2 = (new Date()).getTime();
			System.out.println (l2-l1+"ms");		
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}
