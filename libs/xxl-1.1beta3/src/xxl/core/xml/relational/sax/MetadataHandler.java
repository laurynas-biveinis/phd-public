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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import xxl.core.util.concurrency.AsynchronousChannel;

/**
	A simple handler, that reads only one "row" and stores the data. 
	The user can get this data by calling getColumns() and getAttributes().<br> <br>
	<b>Note:</b> This handler expects a well configured identifier map.<br><br>
	<b>Sample usage:</b> An XMLResultSet uses this handler, in combination with a DataStepByStepHandler
	and a DecoratorXPathHandler, to retrieve data from an XML document.
*/
public class MetadataHandler extends org.xml.sax.helpers.DefaultHandler   {	

	private int column;
	
	private int level;
	
	private List columns;
	
	private List attributes;
	
	/**
	 * The thread which is used inside.
	 */
	public Thread thread;
	
	private Map identifier;
	
	private AsynchronousChannel eventChannel;
	
	Object notificationObject;

	/**
	 *	Constructs the handler.
	 *	@param eventChannel Channel which is used for sending events.
	 *	@param identifier a map, that holds at least the notation of the XML tags, which are used in the 
	 *	metadata section of the document.
	 *	@param notificationObject Object which is sent in the channel after the meta data is availlable
	 *		(application dependant - can be null).
	 */
	public MetadataHandler(AsynchronousChannel eventChannel, Map identifier, Object notificationObject) {
		this.identifier = identifier;
		this.eventChannel = eventChannel;
		this.notificationObject = notificationObject;

		column = 0;
		level = 0;
		columns = new ArrayList();
		attributes = new ArrayList();
	}	

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	public void endDocument() {
	}
	
	/**
	 * Returns a List, that contains the column names.
	 * @return The columns as list.
	 */
	public List getColumns() {
		return columns;	
	}
	
	/**
	 * Returns a List, that contains a "attribute" Map for each column. This Map maps attribute names to attribute values.
	 * @return The attributes as list.
	 */
	public List getAttributes() {
		return attributes;
	}
	
	/**
	 * Handles the event of characters between tags.
	 * @param ch The characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the character array.
	 * @throws SAXException
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		columns.add(new String (ch, start, length));
	}
	
	/**
	 * Handles the startElement event. If the qName equals the value of the "metacol" key from the identifier map, 
	 * the data between this tag will be handled like metadata.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes The specified or defaulted attributes.
	 */
	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) {
		level++;
		thread = Thread.currentThread();
		if (qName.equals(identifier.get("metacol"))) {
			column++;
			Map map = new HashMap();
			for (int i=0;i<attributes.getLength();i++) map.put(attributes.getQName(i), attributes.getValue(i));
			this.attributes.add(map);
		}		
	}
	
	/**
	 * Handles the event of a closing tag.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) {
		level--;
		if (level==0)
			eventChannel.put(notificationObject);
	}
}
