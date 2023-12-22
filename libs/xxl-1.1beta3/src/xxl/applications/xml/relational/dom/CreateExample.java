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

package xxl.applications.xml.relational.dom;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.w3c.dom.Document;

import xxl.applications.xml.Common;
import xxl.core.relational.cursors.ResultSetMetaDataCursor;
import xxl.core.util.DatabaseAccess;
import xxl.core.xml.relational.dom.Dom;
import xxl.core.xml.relational.dom.Xalan;

/**
 * This is a sample usage for the xxl.xml.dom package.
 * This example reads the relation "leute" from the database specified
 * in the "std.prop"-file (see {@link xxl.core.util.DatabaseAccess}).
 * Then a new xml document is created and the relation is appended at 
 * the path expression /table/people. Then the document is written to disk
 * in "database.xml".
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class CreateExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		try {
			/* Initialize the parser */
			Dom.init(new Xalan());	
			
			System.out.println("Start database connection");
			/* create a connection to a db */
			DatabaseAccess da = DatabaseAccess.loadFromPropertyFile(DatabaseAccess.getPropsDataPath()+"std.prop");
			Connection c = da.getConnection();
			
			/* new statement */
			Statement stmt = c.createStatement();
			
			System.out.println("Start query");
			/* get some data */
			ResultSet rs = stmt.executeQuery("SELECT * FROM leute");		
							
			/* create a new document */
			Document document = Dom.emptyDocument("table");
			
			/* insert a new node into this document */
			document.getDocumentElement().appendChild(document.createElement("people"));
	
			/* use the default ifentifier-map*/		
		  	java.util.Map map = Dom.DEFAULT_IDENTIFIER_MAP();
		  	
		  	/* write no meta data ? */  	
		  	//map.remove("meta");
		  	
			System.out.println("Construct DOM tree");
		  	/* insert data in the document, an XPath Expression shows the location where to place the data/metadata */
			Dom.DBtoDOM(new ResultSetMetaDataCursor(rs),document,"/table/people","/table/people", map, true);	
				
			System.out.println("Write tree to file database.xml");
			/* write the document in a new file */
			Dom.DOMtoOutputStream(document, new FileOutputStream(Common.getXMLOutPath()+"database.xml"));
			
			System.out.println("That's all");
		}
		catch (Exception e) {
			System.out.println("The insert example has failed. This is possibly due to the fact");
			System.out.println("that you need a database connection for this example (std.prop).");
			System.out.println("Test the prop-file and the database.");
			System.out.println("The exception which were thrown: "+e);
		}
	}
}
