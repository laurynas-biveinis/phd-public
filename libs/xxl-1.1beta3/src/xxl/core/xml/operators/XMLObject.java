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

package xxl.core.xml.operators;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;



/**
 * This class implements XMLDocumentMetaData using a map
 * for storing the meta data.
 */
public class XMLObject implements XMLDocumentMetaData {
	/**
	 * DOM-Document which is inside this XMLObject.
	 */
	protected Document document;
	/**
	 * The map used for storing meta data to a document.
	 */
	protected Map metaData;

	/**
	 * Constructs an XMLObject using certain type of Map.
	 * @param document Associated DOM-Document.
	 * @param map the Map which is used to store the meta data.
	 */
	public XMLObject (Document document, Map map) {
		this.document = document;
		this.metaData = map;
	}
	
	/**
	 * Constructs an XMLObject using a HashMap.
	 * @param document Associated DOM-Document.
	 */
	public XMLObject (Document document) {
		this (document, new HashMap());
	}
	
	/** 
	 * Sets a meta data entry with key and value.
	 * @param key Name of the meta data entry.
	 * @param value Value of the meta data entry.
	 */
	public void putMetaDataEntry(String key, Object value) {
		metaData.put(key,value);
	}
	
	/** 
	 * Returns a meta data entry or null, if no such
	 * entry exists.
	 * @param key Name of the meta data entry.
	 * @return Value of the meta data entry or null if such
	 *		an entry does not exist.
	 */
	public Object getMetaDataEntry(String key) {
		return metaData.get(key);
	}
	
	/**
	 * Returns the DOM-Document stored inside.
	 * @return the DOM-Document.
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * Sets the DOM-Document which is stored inside.
	 * @param document the DOM-Document.
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/**
	 * Returns the meta data (here, an object of type
	 * Map has to be returned). Be careful with modifications,
	 * because the meta data is not copied (reference to the
	 * meta data!).
	 * @return map containing all meta data.
	 */
	public Object getMetaData() {
		return metaData;
	}
}
