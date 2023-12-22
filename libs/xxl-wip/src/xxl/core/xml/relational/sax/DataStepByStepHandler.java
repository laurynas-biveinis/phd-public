/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.relational.sax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import xxl.core.util.concurrency.Channel;

/**
	This class is used in connectivity.xml.sax.XMLResultSet. The specialty of this handler is 
	the step by step computation. <br>A DataStepByStepHandler uses an xxl.util.Channel to communicate with other objects.
	A channel can contain at most one Object. This Handler reads a complete row and puts the data in the channel.
	Then it handles another row, but when it tries to put the data in the channel, the current thread stops until the
	previous data is taken out of the channel. When this handler handles the endDocument event, the endDocument method puts
	null into the channel. That shall indicate that no more data comes through this channel.
*/
public class DataStepByStepHandler extends org.xml.sax.helpers.DefaultHandler   {

	private int level;
	
	/** A list, that contains the data (string) for each column in the current row. */
	private List columns;
	
	private Channel channel;
	
	/** Determines the name of the tag of a datarow */
	private String datarowString;
	
	private Object endMarker;
	
	private boolean sendEndMarker;
	
	/**
		Creates the Handler.
		@param channel This channel is used to communicate with other objects.
		@param identifierMap A user specified identifier map to configure the XML processing. See the description in the Sax class for details.
		@param sendEndMarker Determines if the end marker is sent via the channel.
		@param endMarker Object which is sent after processing the current document.
	*/
	public DataStepByStepHandler (Channel channel, Map identifierMap, boolean sendEndMarker, Object endMarker) {
		this.channel = channel;
		this.sendEndMarker = sendEndMarker;
		this.endMarker = endMarker;
		
		datarowString = (String) identifierMap.get("datarow");
		columns = new ArrayList();
		level = 0;
	}
	
	/**
		Puts the endMarker into the channel if it is wanted.
		This indicates, that no more data comes through this channel.
	*/	
	public void endDocument() {
		if (sendEndMarker)
			channel.put(endMarker);
	}
	
	/**
	 * Adds a column.
	 * @param ch The characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the character array.
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		columns.add(new String (ch, start, length));
	}
	
	/**
	 * Handles the startElement event.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes The specified or defaulted attributes.
	 */
	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) {        
		level++;	
		if (qName.equals(datarowString))
			this.columns = new ArrayList();
	}
	
	/**
		Handles the startDocument event (nothing to do).
	*/
	public void startDocument() {
	}
	
	/**
	 * Handles the endElement event. This method puts data in the channel, when a row is completely handled.
	 * @param uri The namespace URI
	 * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) {
		level--;
		if (qName.equals(datarowString))
			channel.put(columns);
	}
}
