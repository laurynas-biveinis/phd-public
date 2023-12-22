/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
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
