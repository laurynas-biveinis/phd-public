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

import java.io.OutputStream;

import org.xml.sax.Attributes;

import xxl.core.util.WrappingRuntimeException;

/**
	A simple Handler that copies an XML document in an OutputStream.
	<br><br><b>Sample usage:</b> In order to write the ResultSet-data into a XML Document,
	i combine a MirrorHandler, an XMLWriterHandler and a DecoratorXPathHandler. 
	The MirrorHandler is the default handler of the XPathHandler, it writes the complete
	document source into the OutputStream. The XMLWriterHandler writes the data into this stream.
*/
public class MirrorHandler extends org.xml.sax.helpers.DefaultHandler {
	
	/**
		An java.util.OutputStream where the document shall be copied.
	*/
	private OutputStream out;
	
	/**
		Creates the Handler.
		@param out The java.util.OutputStream where the document shall be copied.
	*/	
	public MirrorHandler (OutputStream out) {
		this.out = out;	
	}

	/**
	 * Writes <qName  ... and the attributes .....> in the OutputStream.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes The specified or defaulted attributes.
	 */
	public void  startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) {
		try {
			String s = "<"+qName;
			out.write(s.getBytes());
			
			for (int i=0; i< attributes.getLength(); i++)
				out.write((" "+attributes.getQName(i)+"=\""+attributes.getValue(i)+"\"").getBytes());
			
			out.write((">").getBytes());
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * Writes </qName> in the OutputStream.
	 * Handles the event of a closing tag.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName)  {
		try {
		String s = "</"+qName+">";
		out.write(s.getBytes());
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * Writes the characters in the OutputStream.
	 * Handles the event of characters between tags.
	 * Calls the Function functionC.
	 * @param ch The characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the character array.
	 */
	public void characters(char[] ch, int start, int length) {
		try {
			out.write(new String(ch, start, length).getBytes());
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}		
	}
}
