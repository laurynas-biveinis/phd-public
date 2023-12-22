/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.xml.relational.dom;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.applications.xml.Common;
import xxl.core.relational.cursors.ResultSetMetaDataCursor;
import xxl.core.util.DatabaseAccess;
import xxl.core.xml.relational.dom.Dom;
import xxl.core.xml.relational.dom.Xalan;

/** 
 * This example reads the relation "actors" from the database specified
 * in the "movie.prop"-file (see {@link xxl.core.util.DatabaseAccess}).
 * Then the relation is written into a file called "actors.html"
 * which can be viewed with a browser.
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class HtmlExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		try {
			/* Initialize the parser */	
			Dom.init(new Xalan());				
			
			/* create a connection to a db */
			DatabaseAccess da = DatabaseAccess.loadFromPropertyFile(DatabaseAccess.getPropsDataPath()+"movie.prop");
			Connection c = da.getConnection();
			
			Statement stmt = c.createStatement();
			
			/* get some data */		
			ResultSet rs = stmt.executeQuery("SELECT * FROM actors");
							
			/* create an empty document*/
			Document document = Dom.emptyDocument();
			
			Element root = document.createElement("table");
	
			/* borderlayout and background definition*/
			root.setAttribute("border","1");
			root.setAttribute("bgcolor","#CCFCCF");
	
			document.appendChild(root);
		  
		  	java.util.Map map = Dom.DEFAULT_HTML_IDENTIFIER_MAP();

		  	/* write no meta data ? */	  	
		  	//map.remove("meta");
		  	
			Dom.DBtoDOM(new ResultSetMetaDataCursor(rs),document,"/table","/table", map, false);	
			
			/* write the document in a new file */
			Dom.DOMtoOutputStream(document, new FileOutputStream(Common.getXMLOutPath()+"actors.html"));
		}
		catch (Exception e) {      
			System.out.println("The insert example has failed. This is possibly due to the fact");
			System.out.println("that you need a database connection for this example (std.prop).");
			System.out.println("Test the prop-file and the database.");
			System.out.println("The exception which were thrown: "+e);
		}
	}
}
