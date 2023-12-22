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

package xxl.applications.xml.relational.sax;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import xxl.applications.xml.Common;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.InputStreamMetaDataCursor;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.relational.sax.Sax;

/**
 * This is a sample usage for the xxl.xml.sax package.
 * This example reads the relation from vorlesungen.txt.
 * Then the xml data is written into the file "database.xml".
 * Therefore, "database.xml" is used as a template.
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class WriteExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main (String[] args) {
		try {
			/*
			// Using a database instead ...
			DatabaseAccess da = DatabaseAccess.loadFromPropertyFile(DatabaseAccess.getPropsDataPath()+"std.prop");
			Connection c = da.getConnection();
			
			// new statement
			Statement stmt = c.createStatement();
			
			// get some data
			ResultSet rs = stmt.executeQuery("SELECT * FROM leute");

			ResultSetMetaDataCursor cursor = new ResultSetMetaDataCursor(rs);
			*/
			System.out.println("Read relation from a text file");
			MetaDataCursor cursor = new InputStreamMetaDataCursor(Common.getRelationalDataPath()+"vorlesungen.txt");

			OutputStream out = new FileOutputStream(Common.getXMLOutPath()+"write_example.xml");
			InputStream in = new FileInputStream(new java.io.File(Common.getXMLDataPath()+"database.xml"));

			System.out.println("Merge the data into an XML file");
			Sax.DBtoXML (in, out, cursor, Sax.DEFAULT_IDENTIFIER_MAP(), "/table/people");

			cursor.close();
			System.out.println("That's all");
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}
}
