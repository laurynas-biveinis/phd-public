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

package xxl.core.xml.relational.sax;

import org.xml.sax.Attributes;

/**
 * A simple handler that copies the XML document to the standard output.
 */
public class EchoHandler extends org.xml.sax.helpers.DefaultHandler {

	/**
	 * Constructs a new EchoHandler.
	 */
	public EchoHandler () {				
	}
	
	/**
	 * Signals the start of a document.
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	public void startDocument() {
	}

	/**
	 * Handles the event of a starting tag.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes The specified or defaulted attributes.
	 */
	public void  startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) {
		System.out.println ("<"+qName+">");
	}

	/**
	 * Handles the event of a closing tag.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) {
		System.out.println ("</"+qName+">");
	}

	/**
	 * Handles the event of characters between tags.
	 * @param ch The characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the character array.
	 */
	public void characters(char[] ch, int start, int length) {
		System.out.println(new String(ch, start, length));		
	}
}
