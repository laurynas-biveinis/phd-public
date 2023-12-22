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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import xxl.applications.xml.Common;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.metaData.ResultSetMetaDatas;

/** 
 * This example constructs a MetaDataCursor out of an XML-Document
 * and then tests the performance (time for a walk through).
 * <p>
 * All XML examples read their sourcefiles from "xxlrootpath/data/xml" and write results
 * to "xxloutpath/applications/release/xml". So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class PerformanceTest {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		Thread.currentThread().setName("main Thread");
		
		int source=0;
		if (args.length>0)
			source = Integer.parseInt(args[0]);
		MetaDataCursor cursor = Common.getSource(source);

   		ResultSetMetaData rsmd = ResultSetMetaDatas.getResultSetMetaData(cursor);
   		if (rsmd!=null) {
   			try {
				System.out.println("Number of Columns: "+rsmd.getColumnCount());
			}
			catch (SQLException e) {}
		}
		else
			System.out.println("Meta data could not be accessed.");

		int numberOfTuples=0;
		long l1 = (new Date()).getTime();

		numberOfTuples = Cursors.count(cursor);
		
		long l2 = (new Date()).getTime();
		
		System.out.println(numberOfTuples+" tuples seen.");
		System.out.println (l2-l1+"ms");
		
		cursor.close();
	}
}
