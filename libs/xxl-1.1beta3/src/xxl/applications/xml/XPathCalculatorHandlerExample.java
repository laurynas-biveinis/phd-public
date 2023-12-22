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

package xxl.applications.xml;


import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.util.XPathCalculatorHandler;

/**
 * A test class for the XPathCalculatorHandler. An XML file is parsed
 * and the locations are written to the console.
 */
public class XPathCalculatorHandlerExample {

	/**
	 * Example using the XPathCalculatorHandler.
	 * @param args The command line options.
	 */
	public static void main(String[] args) {
		try {
			long time;
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			System.out.println("parsing the document with SAX ...... ");
			time = System.currentTimeMillis();
			spf.newSAXParser().parse(
				new java.io.File(Common.getXMLDataPath()+"com_err.xml"), //"othello.xml"),
				new XPathCalculatorHandler() {
					int count=0;
					public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws SAXException {
						super.startElement(uri, localName, qName, attributes);
						if (count++<10) {
							System.out.println(currentXPathLocation);
							if (count==10)
								System.out.println("...");
						}
					}
				}
			);
			System.out.println("done!");
			System.out.println("required time in ms: "+(System.currentTimeMillis()-time));
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}
}
