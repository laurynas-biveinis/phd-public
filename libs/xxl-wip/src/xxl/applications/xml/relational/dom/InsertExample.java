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
 * This example reads the relation "web" from the database specified
 * in the "std.prop"-file (see {@link xxl.core.util.DatabaseAccess}).
 * Then the relation is appended at the path expression
 * /table/links in the document "database.xml". The resulting
 * document is then written to "database_insert.xml".
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
  */
public class InsertExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		try {
			/* Initializes the parser */
			Dom.init(new Xalan());
			
			/* create a connection to a db */
			DatabaseAccess da = DatabaseAccess.loadFromPropertyFile(DatabaseAccess.getPropsDataPath()+"std.prop");
			Connection c = da.getConnection();
			
			/* get some data */
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM web");
			
			/* create a DOM document from an existing XML file*/
			Document document = Dom.InputStreamtoDOM(new FileInputStream(Common.getXMLDataPath()+"database.xml"));
			
			/* create a new "table section"*/
			document.getDocumentElement().appendChild(document.createElement("web"));
			
			/* insert a second table*/
			Dom.DBtoDOM(new ResultSetMetaDataCursor(rs),document,"/table/web","/table/web",Dom.DEFAULT_IDENTIFIER_MAP(),true);
			
			/* write document to file */
			Dom.DOMtoOutputStream(document, new FileOutputStream(Common.getXMLOutPath()+"database_insert.xml"));
		}
		catch (Exception e) {
			System.out.println("The insert example has failed. This is possibly due to the fact");
			System.out.println("that you need a database connection for this example (std.prop).");
			System.out.println("Test the prop-file and the database.");
			System.out.println("The exception which were thrown: "+e);
		}
	}
}
