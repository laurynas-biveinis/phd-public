/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.operators;

import org.w3c.dom.Document;

import xxl.core.util.metaData.MetaDataProvider;

/**
 * Interface defining meta data for XML Documents.
 * Standardized meta data entries are:
 * <ul>
 * <li>filename: associated filename with the document (for example the initial filename)</li>
 * <li>simplexpathlocation: allowed inside XPath expressions only: /, name, [number]</li>
 * </ul>
 */
public interface XMLDocumentMetaData extends MetaDataProvider {
	/** 
	 * Sets a meta data entry with key and value.
	 * @param key Name of the meta data entry.
	 * @param value Value of the meta data entry.
	 */
	public void putMetaDataEntry(String key, Object value);
	/** 
	 * Returns a meta data entry or null, if no such
	 * entry exists.
	 * @param key Name of the meta data entry.
	 * @return Value of the meta data entry or null if such
	 *		an entry does not exist.
	 */
	public Object getMetaDataEntry(String key);
	/**
	 * Returns the DOM-Document stored inside.
	 * @return the DOM-Document.
	 */
	public Document getDocument();	
	/**
	 * Sets the DOM-Document which is stored inside.
	 * @param document the DOM-Document.
	 */
	public void setDocument(Document document);	
	/**
	 * Returns the meta data (here, an object of type
	 * Map has to be returned). Be careful with modifications,
	 * because the meta data is not copied (reference to the
	 * meta data!).
	 * @return map containing all meta data.
	 */
	public Object getMetaData();
}
