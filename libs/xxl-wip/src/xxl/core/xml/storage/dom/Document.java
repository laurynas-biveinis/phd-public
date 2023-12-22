/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

import java.util.regex.Pattern;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;

import xxl.core.xml.storage.EXTree;

/**
 * The Document class represents the entire XML document. 
 * Conceptually, it is the root of the document tree, and provides the primary access to the document's data.
 * <br>
 * Since elements, text nodes, comments, processing instructions, etc. cannot exist outside the context of a Document, 
 * the Document class also contains the factory methods needed to create these objects. 
 * The Node objects created have a ownerDocument attribute which associates them with the Document within whose context 
 * they were created.
 */ 
public class Document extends Node implements org.w3c.dom.Document {

	//private Variablen
	private DocumentType doctype = null;
	private org.w3c.dom.DOMImplementation implementation = null;
	protected EXTree tree;
		
	/**
	 * Create a document to a <code>EXTree</code>
	 * 
	 * @param tree A tree from <code>storage</code> package.
	 */
	protected Document(EXTree tree) {
		this.tree = tree;
		this.recNode = tree.getRootNode();
		this.parentNode = null;
		this.ownerDocument= null;
	}
		
	/**
     * This is a convenience attribute that allows direct access to the child 
     * node that is the root element of the document. 
     * 
     * @return Root node of this Document.
     */
    public org.w3c.dom.Element getDocumentElement(){
        return (org.w3c.dom.Element) this.getFirstChild();
    };
	 
	 /**
	  * The first child of this document. 
	  * This method allways return root node of this document.
	  *  
	  * @return Root node
	  */
	 public org.w3c.dom.Node getFirstChild(){
	     return new Element(this, this, this.recNode);
	 }
	 
	 /**
	  * The last child of this document. 
	  * This method allways return root node of this document.
	  * 
	  * @return Root node
	  */
	 public org.w3c.dom.Node getLastChild(){
	     return new Element(this, this, this.recNode);
	 }
	 
	/**
	 * A <code>NodeList</code> that contains all children of this node. If
	 * there are no children, this is a <code>NodeList</code> containing no
	 * nodes. This method replase the same method in Node.
	 */
	public org.w3c.dom.NodeList getChildNodes() {
		if(this.recNode==null) return new NodeList();
		if(this.childNodes==null) {
			this.childNodes = new NodeList(); 
			this.childNodes.append(new Element(this, this, this.recNode));
		}
	  //System.out.println("popal : " + this.childNodes.item(0).getNodeName());
	  //System.out.println("popal : " + new Element(this, this, this.recNode).getNodeName());
	  
	  return this.childNodes;
  }
 
	 
	 /**
     * Creates an element of the type specified. 
     * 
     * @param tagName The name of the element type to instantiate.  
     *   This is case-sensitive. 
     * @return A new <code>Element</code> object with the 
     *   <code>nodeName</code> attribute set to <code>tagName</code>.
     * 
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     */
    public org.w3c.dom.Element createElement(String tagName) throws org.w3c.dom.DOMException{
		if (!Pattern.matches("#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]", "tagName") )  throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "invalid character in name"); 
	    return new Element(tagName, null, this);
    };
	 
	 /**
     * Creates a <code>Text</code> node given the specified string.
     * 
     * @param data The data for the node.
     * @return The new <code>Text</code> object.
     */
    public org.w3c.dom.Text createTextNode(String data){
        return new Text(data, null, this);
    };
	 
	 /**
     * Creates a <code>Comment</code> node given the specified string.
     * 
     * @param data The data for the node.
     * @return The new <code>Comment</code> object.
     */
    public org.w3c.dom.Comment createComment(String data){
        return new Comment(data, null, this);
    };

    /**
     * Creates a <code>CDATASection</code> node whose value is the specified string.
     * 
     * @param data The data for the <code>CDATASection</code> contents.
     * @return The new <code>CDATASection</code> object.
     */
    public org.w3c.dom.CDATASection createCDATASection(String data) throws org.w3c.dom.DOMException{
        return new CDATASection(data, null, this);
    };
	 	 
	/**
     * Creates an <code>Attr</code> of the given name. 
     * 
     * <br>Note that the <code>Attr</code> instance can then be set on an 
     * <code>Element</code> using the <code>setAttributeNode</code> method. 
     *
     * @param name The name of the attribute.
     * @return A new <code>Attr</code> object with the <code>nodeName</code> 
     *   attribute set to <code>name</code> The value of the attribute is the empty string.
     * 
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an illegal character.
     */
    public org.w3c.dom.Attr createAttribute(String name) throws org.w3c.dom.DOMException{
		if (!Pattern.matches("#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]", "tagName") )  throw new DOMException(DOMException.INVALID_CHARACTER_ERR, "invalid character in name"); 
	    return new Attr(name, null, this);
    };
	 	 
	/**
     * Returns a <code>NodeList</code> of all the <code>Elements</code> with a 
     * given tag name in the order in which they are encountered in a 
     * preorder traversal of the <code>Document</code> tree.
     * 
     * @param tagname The name of the tag to match on. The special value "*" 
     *   matches all tags.
     * @return A new <code>NodeList</code> object containing all the matched 
     *   <code>Elements</code>.
     */
    public org.w3c.dom.NodeList getElementsByTagName(String tagname){
        return ( (org.w3c.dom.Element) this.getFirstChild()).getElementsByTagName(tagname);
    };
	 
	/**
     * Return allways <code>null<code> because ID's are not implemented.
     *  
     * @param elementId The unique <code>id</code> value for an element.
     * @return <code>null<code>
     * 
     * @since DOM Level 2
     */
    public org.w3c.dom.Element getElementById(String elementId){
        return null;
    }
	 
	 /**
	  * Return type of this Note. 
	  * It is <code>org.w3c.dom.Node.DOCUMENT_NODE</code> for document.
	  * 
	  * @return <code>org.w3c.dom.Node.DOCUMENT_NODE</code>
	  */
	 public short getNodeType() {
        return org.w3c.dom.Node.DOCUMENT_NODE;
    }
    
	 /**
	  * Return name of this Note. It is <code>#document</code> for document
	  * 
	  * @return #document
	  */
    public String getNodeName() {
        return "#document";
    }
	
	 /**
	  * For document is nothink to do. 
	  *
	  */
	 public void setNodeValue(String str) throws org.w3c.dom.DOMException {
	 }
	 
	/**
     * This implementation of DOM don't support Document Type Declaration.
	 * So the result ist allways <code>null</code>.
	 * 
	 * @return <code>null</code>  
	 */
    public org.w3c.dom.DocumentType getDoctype(){
        return doctype;
    };

    /**
     * The <code>DOMImplementation</code> object that handles this document. A 
     * DOM application may use objects from multiple implementations.
     * 
     * @return Implementation. 
     */
    public org.w3c.dom.DOMImplementation getImplementation(){
        return implementation;
    };

	 /**
     * Creates an empty <code>DocumentFragment</code> object.
     * We aren't supported <code>DocumentFragment</code> at the moment.
     * 
     * @return <code>null</code>
     */
    public org.w3c.dom.DocumentFragment createDocumentFragment(){
        return null;
    };

	 /**
     * This DOM implementation don't support Processing Instructions. The result ist allways <code>null</code>
     * 
     * @param target The target part of the processing instruction.
     * @param data The data for the node.
     * @return <code>null</code>.
     */
    public  org.w3c.dom.ProcessingInstruction createProcessingInstruction(String target, String data) throws org.w3c.dom.DOMException{
        return null;
    };
    
   

    /**
     * <code>EntityReference</code> aren't supported.
     * 
     * @param name The name of the entity to reference.
     * @return <code>null</code>.
     */
    public org.w3c.dom.EntityReference createEntityReference(String name) throws org.w3c.dom.DOMException{
        return null;
    };

   

    /**
     * Imports a node from another document to this document. The returned 
     * node has no parent; (<code>parentNode</code> is <code>null</code>). 
     * The source node is not altered or removed from the original document; 
     * this method creates a new copy of the source node.
     * <br>For all nodes, importing a node creates a node object owned by the 
     * importing document, with attribute values identical to the source 
     * node's <code>nodeName</code> and <code>nodeType</code>, plus the 
     * attributes related to namespaces (<code>prefix</code>, 
     * <code>localName</code>, and <code>namespaceURI</code>). As in the 
     * <code>cloneNode</code> operation on a <code>Node</code>, the source 
     * node is not altered.
     * <br>Additional information is copied as appropriate to the 
     * <code>nodeType</code>, attempting to mirror the behavior expected if 
     * a fragment of XML or HTML source was copied from one document to 
     * another, recognizing that the two documents may have different DTDs 
     * in the XML case. The following list describes the specifics for each 
     * type of node. 
     * <dl>
     * <dt>ATTRIBUTE_NODE</dt>
     * <dd>The <code>ownerElement</code> attribute 
     * is set to <code>null</code> and the <code>specified</code> flag is 
     * set to <code>true</code> on the generated <code>Attr</code>. The 
     * descendants of the source <code>Attr</code> are recursively imported 
     * and the resulting nodes reassembled to form the corresponding subtree.
     * Note that the <code>deep</code> parameter has no effect on 
     * <code>Attr</code> nodes; they always carry their children with them 
     * when imported.</dd>
     * <dt>DOCUMENT_FRAGMENT_NODE</dt>
     * <dd>If the <code>deep</code> option 
     * was set to <code>true</code>, the descendants of the source element 
     * are recursively imported and the resulting nodes reassembled to form 
     * the corresponding subtree. Otherwise, this simply generates an empty 
     * <code>DocumentFragment</code>.</dd>
     * <dt>DOCUMENT_NODE</dt>
     * <dd><code>Document</code> 
     * nodes cannot be imported.</dd>
     * <dt>DOCUMENT_TYPE_NODE</dt>
     * <dd><code>DocumentType</code> 
     * nodes cannot be imported.</dd>
     * <dt>ELEMENT_NODE</dt>
     * <dd>Specified attribute nodes of the 
     * source element are imported, and the generated <code>Attr</code> 
     * nodes are attached to the generated <code>Element</code>. Default 
     * attributes are not copied, though if the document being imported into 
     * defines default attributes for this element name, those are assigned. 
     * If the <code>importNode</code> <code>deep</code> parameter was set to 
     * <code>true</code>, the descendants of the source element are 
     * recursively imported and the resulting nodes reassembled to form the 
     * corresponding subtree.</dd>
     * <dt>ENTITY_NODE</dt>
     * <dd><code>Entity</code> nodes can be 
     * imported, however in the current release of the DOM the 
     * <code>DocumentType</code> is readonly. Ability to add these imported 
     * nodes to a <code>DocumentType</code> will be considered for addition 
     * to a future release of the DOM.On import, the <code>publicId</code>, 
     * <code>systemId</code>, and <code>notationName</code> attributes are 
     * copied. If a <code>deep</code> import is requested, the descendants 
     * of the the source <code>Entity</code> are recursively imported and 
     * the resulting nodes reassembled to form the corresponding subtree.</dd>
     * <dt>
     * ENTITY_REFERENCE_NODE</dt>
     * <dd>Only the <code>EntityReference</code> itself is 
     * copied, even if a <code>deep</code> import is requested, since the 
     * source and destination documents might have defined the entity 
     * differently. If the document being imported into provides a 
     * definition for this entity name, its value is assigned.</dd>
     * <dt>NOTATION_NODE</dt>
     * <dd>
     * <code>Notation</code> nodes can be imported, however in the current 
     * release of the DOM the <code>DocumentType</code> is readonly. Ability 
     * to add these imported nodes to a <code>DocumentType</code> will be 
     * considered for addition to a future release of the DOM.On import, the 
     * <code>publicId</code> and <code>systemId</code> attributes are copied.
     * Note that the <code>deep</code> parameter has no effect on 
     * <code>Notation</code> nodes since they never have any children.</dd>
     * <dt>
     * PROCESSING_INSTRUCTION_NODE</dt>
     * <dd>The imported node copies its 
     * <code>target</code> and <code>data</code> values from those of the 
     * source node.</dd>
     * <dt>TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE</dt>
     * <dd>These three 
     * types of nodes inheriting from <code>CharacterData</code> copy their 
     * <code>data</code> and <code>length</code> attributes from those of 
     * the source node.</dd>
     * </dl> 
     * @param importedNode The node to import.
     * @param deep If <code>true</code>, recursively import the subtree under 
     *   the specified node; if <code>false</code>, import only the node 
     *   itself, as explained above. This has no effect on <code>Attr</code>
     *   , <code>EntityReference</code>, and <code>Notation</code> nodes.
     * @return The imported node that belongs to this <code>Document</code>.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if the type of node being imported is not 
     *   supported.
     * @since DOM Level 2
     */
    public org.w3c.dom.Node importNode(org.w3c.dom.Node importedNode, boolean deep) throws org.w3c.dom.DOMException{
        return null;
    };
    
    /**
	 * This DOM implementation don't support namespaces.
	 * 
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 * 
     * @since DOM Level 2
     */
    public org.w3c.dom.Element createElementNS(String namespaceURI, String qualifiedName) throws org.w3c.dom.DOMException{
       throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported"); 
	 };

    /**
     * This DOM implementation don't support namespaces.
     * 
     * @return <code>NOT_SUPPORTED_ERR</code>
     * 
	 * @since DOM Level 2
     */
    public org.w3c.dom.Attr createAttributeNS(String namespaceURI, String qualifiedName) throws org.w3c.dom.DOMException{
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
    };

    /**
     * This DOM implementation don't support namespaces.
     * 
     * @return <code>NOT_SUPPORTED_ERR</code>
     * 
	 * @since DOM Level 2
     */
    public org.w3c.dom.NodeList getElementsByTagNameNS(String namespaceURI, String localName){
         throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
    };

 
    
    /** 
     * Returns a duplicate of this node, i.e., serves as a generic copy
     * constructor for nodes. The duplicate node has no parent; (
     * <code>parentNode</code> is <code>null</code>.).
     * <br>Cloning an <code>Element</code> copies all attributes and their
     * values, including those generated by the XML processor to represent
     * defaulted attributes, but this method does not copy any text it
     * contains unless it is a deep clone, since the text is contained in a
     * child <code>Text</code> node. Cloning an <code>Attribute</code>
     * directly, as opposed to be cloned as part of an <code>Element</code>
     * cloning operation, returns a specified attribute (
     * <code>specified</code> is <code>true</code>). Cloning any other type
     * of node simply returns a copy of this node.
     * <br>Note that cloning an immutable subtree results in a mutable copy,
     * but the children of an <code>EntityReference</code> clone are readonly
     * . In addition, clones of unspecified <code>Attr</code> nodes are
     * specified. And, cloning <code>Document</code>,
     * <code>DocumentType</code>, <code>Entity</code>, and
     * <code>Notation</code> nodes is implementation dependent.
     * @param deep If <code>true</code>, recursively clone the subtree under
     *   the specified node; if <code>false</code>, clone only the node
     *   itself (and its attributes, if it is an <code>Element</code>).
     * @return The duplicate node.
     */
    public org.w3c.dom.Node cloneNode(boolean param) {
    	return null;
    }
	/* (Kein Javadoc)
	 * @see org.w3c.dom.Node#hasChildNodes()
	 */
	public boolean hasChildNodes() {
		if(this.recNode==null) return false;
		if (this.childNodes == null) {
			this.childNodes = new NodeList();  
			this.childNodes.append(new Element(this, this, this.recNode));
		}
		if(this.childNodes != null && this.childNodes.getLength()>0)return true;
		return false;
	}

	public org.w3c.dom.Node adoptNode(org.w3c.dom.Node source) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String getDocumentURI() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public DOMConfiguration getDomConfig() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String getInputEncoding() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean getStrictErrorChecking() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String getXmlEncoding() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean getXmlStandalone() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String getXmlVersion() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void normalizeDocument() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public org.w3c.dom.Node renameNode(org.w3c.dom.Node n, String namespaceURI, String qualifiedName) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setDocumentURI(String documentURI) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setStrictErrorChecking(boolean strictErrorChecking) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setXmlVersion(String xmlVersion) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
