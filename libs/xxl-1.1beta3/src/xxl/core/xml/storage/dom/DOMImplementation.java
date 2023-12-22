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

package xxl.core.xml.storage.dom;

public class DOMImplementation implements org.w3c.dom.DOMImplementation{
    
	/**
     * Test if the DOM implementation implements a specific feature.
     * @param feature The name of the feature to test (case-insensitive). The 
     *   values used by DOM features are defined throughout the DOM Level 2 
     *   specifications and listed in the  section. The name must be an XML 
     *   name. To avoid possible conflicts, as a convention, names referring 
     *   to features defined outside the DOM specification should be made 
     *   unique.
     * @param version This is the version number of the feature to test. In 
     *   Level 2, the string can be either "2.0" or "1.0". If the version is 
     *   not specified, supporting any version of the feature causes the 
     *   method to return <code>true</code>.
     * @return <code>true</code> if the feature is implemented in the 
     *   specified version, <code>false</code> otherwise.
     */
    public boolean hasFeature(String feature, String version){
        return false;
    };

    /**
     * Creates an empty <code>DocumentType</code> node. Entity declarations 
     * and notations are not made available. Entity reference expansions and 
     * default attribute additions do not occur. It is expected that a 
     * future version of the DOM will provide a way for populating a 
     * <code>DocumentType</code>.
     * @param qualifiedName The qualified name of the document type to be 
     *   created.
     * @param publicId The external subset public identifier.
     * @param systemId The external subset system identifier.
     * @return A new <code>DocumentType</code> node with 
     *   <code>Node.ownerDocument</code> set to <code>null</code>.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed.
     *   <br>NOT_SUPPORTED_ERR: May be raised by DOM implementations which do 
     *   not support the <code>"XML"</code> feature, if they choose not to 
     *   support this method. Other features introduced in the future, by 
     *   the DOM WG or in extensions defined by other groups, may also 
     *   demand support for this method; please consult the definition of 
     *   the feature to see if it requires this method. 
     * @since DOM Level 2
     */
    public org.w3c.dom.DocumentType createDocumentType( String qualifiedName,
                                                        String publicId, String systemId)
                                                        throws org.w3c.dom.DOMException{
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
    };

    /**
     * Creates a DOM Document object of the specified type with its document 
     * element.
     * @param namespaceURI The namespace URI of the document element to 
     *   create.
     * @param qualifiedName The qualified name of the document element to be 
     *   created.
     * @param doctype The type of document to be created or <code>null</code>.
     *   When <code>doctype</code> is not <code>null</code>, its 
     *   <code>Node.ownerDocument</code> attribute is set to the document 
     *   being created.
     * @return A new <code>Document</code> object.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed, if the <code>qualifiedName</code> has a prefix and the 
     *   <code>namespaceURI</code> is <code>null</code>, or if the 
     *   <code>qualifiedName</code> has a prefix that is "xml" and the 
     *   <code>namespaceURI</code> is different from "
     *   http://www.w3.org/XML/1998/namespace" , or if the DOM 
     *   implementation does not support the <code>"XML"</code> feature but 
     *   a non-null namespace URI was provided, since namespaces were 
     *   defined by XML.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>doctype</code> has already 
     *   been used with a different document or was created from a different 
     *   implementation.
     *   <br>NOT_SUPPORTED_ERR: May be raised by DOM implementations which do 
     *   not support the "XML" feature, if they choose not to support this 
     *   method. Other features introduced in the future, by the DOM WG or 
     *   in extensions defined by other groups, may also demand support for 
     *   this method; please consult the definition of the feature to see if 
     *   it requires this method. 
     * @since DOM Level 2
     */
    public org.w3c.dom.Document createDocument(String str, String str1,
                                                org.w3c.dom.DocumentType documentType)
                                                throws org.w3c.dom.DOMException {
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
        
    }
    
	public Object getFeature(String feature, String version) {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
