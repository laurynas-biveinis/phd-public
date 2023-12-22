/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
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
