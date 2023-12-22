/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
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
