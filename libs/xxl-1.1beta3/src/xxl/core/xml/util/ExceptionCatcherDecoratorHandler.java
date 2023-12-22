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
package xxl.core.xml.util;

import java.io.PrintStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import xxl.core.util.WrappingRuntimeException;

/**
 * This class implements a SAX handler which forwards its
 * method calls to a different instance of a ContentHandler.
 * Exceptions are also written on a PrintStream. This is
 * useful if your SAX parser does not give you meaningful
 * exception messages.
 */
public class ExceptionCatcherDecoratorHandler extends DefaultHandler {
	/**
	 * Handler which is decorated.
	 */
	protected DefaultHandler dh;

	/**
	 * PrintStream on which Exception are written.
	 */
	protected PrintStream ps;

	/**
	 * Creates a new instance of this class, which forwards
	 * calls to a certain instance of a DefaultHandler.
	 * @param dh DefaultHandler which is wrapped.
	 * @param ps PrintStream for Exception messages.
	 */
	public ExceptionCatcherDecoratorHandler(DefaultHandler dh, PrintStream ps) {
		this.dh = dh;
		this.ps = ps;
	}

	/**
	 * Creates a new instance of this class, which forwards
	 * calls to a certain instance of a DefaultHandler.
	 * @param dh DefaultHandler which is wrapped.
	 */
	public ExceptionCatcherDecoratorHandler(DefaultHandler dh) {
		this(dh, System.err);
	}
	
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
			dh.characters(ch, start, length);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void endDocument() throws SAXException {
		try {
			dh.endDocument();
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {
			dh.endElement(uri, localName, qName);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void endPrefixMapping(String prefix) throws SAXException {
		try {
			dh.endPrefixMapping(prefix);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void error(SAXParseException e) throws SAXException {
		try {
			dh.error(e);
		}
		catch (Exception exc) {
			e.printStackTrace(ps);
			throw new SAXException(exc);
		}
	}
	public void fatalError(SAXParseException e) throws SAXException {
		try {
			dh.fatalError(e);
		}
		catch (Exception exc) {
			e.printStackTrace(ps);
			throw new SAXException(exc);
		}
	}
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		try {
			dh.ignorableWhitespace(ch, start, length);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		try {
			dh.notationDecl(name, publicId, systemId);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void processingInstruction(String target, String data) throws SAXException {
		try {
			dh.processingInstruction(target, data);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
		try {
			return dh.resolveEntity(publicId, systemId);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void setDocumentLocator(Locator locator) {
		try {
			dh.setDocumentLocator(locator);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new WrappingRuntimeException(new SAXException(e));
		}
	}
	public void skippedEntity(String name) throws SAXException {
		try {
			dh.skippedEntity(name);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void startDocument() throws SAXException {
		try {
			dh.startDocument();
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			dh.startElement(uri, localName, qName, attributes);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		try {
			dh.startPrefixMapping(prefix, uri);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
		try {
			dh.unparsedEntityDecl(name, publicId, systemId, notationName);
		}
		catch (Exception e) {
			e.printStackTrace(ps);
			throw new SAXException(e);
		}
	}
	public void warning(SAXParseException e) throws SAXException {
		try {
			dh.warning(e);
		}
		catch (Exception exc) {
			e.printStackTrace(ps);
			throw new SAXException(exc);
		}
	}
}
