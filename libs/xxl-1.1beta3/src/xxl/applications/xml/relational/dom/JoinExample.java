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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xxl.applications.xml.Common;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.MetaDataCursor;
import xxl.core.relational.cursors.NestedLoopsJoin;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.relational.dom.Dom;
import xxl.core.xml.relational.dom.Xalan;

/** 
 * This example constructs a two MetaDataCursors out of one XML-Document.
 * Then, a nested loops join is performed.
 * <p>
 * All XML examples read their sourcefiles from xxlrootpath/data/xml and write results
 * to xxloutpath/applications/release/xml. So, the environment variables
 * have to be passed to the application with the -D option. This is done
 * automatically within the script "xxl".
 */
public class JoinExample {

	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		Dom.init(new Xalan());
		Document document=null;
		
		try {
			document = Dom.InputStreamtoDOM(new FileInputStream(Common.getXMLDataPath()+"database_insert.xml"));
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		
		Map m = Dom.DEFAULT_IDENTIFIER_MAP();
		MetaDataCursor leute = Dom.DOMtoMetaDataCursor(document,"/table/people/row","/table/people/meta",null, m);
		MetaDataCursor web = Dom.DOMtoMetaDataCursor(document,"/table/web/row","/table/web/meta", null, m);
		
		System.out.println("Relation leute");
		System.out.println("==============");
		Cursors.println(leute);
		leute.reset();
		System.out.println("Relation web");
		System.out.println("==============");
		Cursors.println(web);
		web.reset();
		
		// leute = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.cursors.sources.EmptyCursor.DEFAULT_INSTANCE,leute.getMetaData());
		// web = xxl.core.cursors.Cursors.wrapToMetaDataCursor(xxl.core.cursors.sources.EmptyCursor.DEFAULT_INSTANCE,web.getMetaData());
		MetaDataCursor cursor = new NestedLoopsJoin(leute,web,null,ArrayTuple.FACTORY_METHOD,NestedLoopsJoin.Type.NATURAL_JOIN);
		
		// Cursors.println(cursor);
		
		Document doc = Dom.emptyDocument();
		
		Element root = doc.createElement("table");
		
		doc.appendChild(root);
		
		Dom.DBtoDOM(cursor,doc,"/table");
		
		OutputStream os=null;
		try {
			os = new FileOutputStream(Common.getXMLOutPath()+"join.xml");
			// os = System.out;
			Dom.DOMtoOutputStream(doc, os);
			System.out.println("Join results were written to join.xml.");
		}
		catch (Exception e) {
			try {
				os.flush();
			}
			catch (Exception e2) {}
			throw new WrappingRuntimeException(e);
		}
	}
}
