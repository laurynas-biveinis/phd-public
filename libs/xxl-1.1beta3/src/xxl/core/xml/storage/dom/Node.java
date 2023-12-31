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

import org.w3c.dom.DOMException;
import org.w3c.dom.UserDataHandler;


/**
 * <p>The attributes <code>nodeName</code>, <code>nodeValue</code> and
 * <code>attributes</code> are included as a mechanism to get at node
 * information without casting down to the specific derived interface. In
 * cases where there is no obvious mapping of these attributes for a
 * specific <code>nodeType</code> (e.g., <code>nodeValue</code> for an
 * <code>Element</code> or <code>attributes</code> for a <code>Comment</code>
 * ), this returns <code>null</code>. Note that the specialized interfaces
 * may contain additional and more convenient mechanisms to get and set the
 * relevant information.
 * <p>The values of <code>nodeName</code>,
 * <code>nodeValue</code>, and <code>attributes</code> vary according to the
 * node type as follows:
 * <table border='1'>
 * <tr>
 * <th>Interface</th>
 * <th>nodeName</th>
 * <th>nodeValue</th>
 * <th>attributes</th>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Attr</td>
 * <td valign='top' rowspan='1' colspan='1'>name of
 * attribute</td>
 * <td valign='top' rowspan='1' colspan='1'>value of attribute</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>CDATASection</td>
 * <td valign='top' rowspan='1' colspan='1'><code>"#cdata-section"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>
 * content of the CDATA Section</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Comment</td>
 * <td valign='top' rowspan='1' colspan='1'><code>"#comment"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>content of
 * the comment</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Document</td>
 * <td valign='top' rowspan='1' colspan='1'><code>"#document"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>DocumentFragment</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * <code>"#document-fragment"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>DocumentType</td>
 * <td valign='top' rowspan='1' colspan='1'>document type name</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Element</td>
 * <td valign='top' rowspan='1' colspan='1'>tag name</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>NamedNodeMap</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Entity</td>
 * <td valign='top' rowspan='1' colspan='1'>entity name</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>
 * EntityReference</td>
 * <td valign='top' rowspan='1' colspan='1'>name of entity referenced</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Notation</td>
 * <td valign='top' rowspan='1' colspan='1'>notation name</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>ProcessingInstruction</td>
 * <td valign='top' rowspan='1' colspan='1'>target</td>
 * <td valign='top' rowspan='1' colspan='1'>entire content excluding the target</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Text</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * <code>"#text"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>content of the text node</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * </table>
 *
 **/

public abstract class Node implements org.w3c.dom.Node{
	/**
	 * Describes the position inside the child page.
	 */
	public int index = 0;

	protected xxl.core.xml.storage.Node recNode = null;
	protected boolean readonly=false;
	protected org.w3c.dom.NamedNodeMap attributes = null;
	protected NodeList childNodes=null;

	protected Document ownerDocument = null;
	protected Node parentNode=null;

    //Methods
	/**
     * Adds the node <code>newChild</code> to the end of the list of children
     * of this node. If the <code>newChild</code> is already in the tree, it
     * is first removed.
     * @param newChild The node to add.If it is a
     *   <code>DocumentFragment</code> object, the entire contents of the
     *   document fragment are moved into the child list of this node
     * @return The node added.
     * @throws org.w3c.dom.DOMException
     *   HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *   allow children of the type of the <code>newChild</code> node, or if
     *   the node to append is one of this node's ancestors or this node
     *   itself.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created
     *   from a different document than the one that created this node.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly or
     *   if the previous parent of the node being inserted is readonly.
     */
    public org.w3c.dom.Node appendChild(org.w3c.dom.Node newChild) throws org.w3c.dom.DOMException {
        //kann sich selbst nicht hinzufügen
        if((Node)newChild==this){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR,"cannot append itself");
        }
        //wenn kein Kind haben darf, Exception werfen
        if(getNodeType()==org.w3c.dom.Node.DOCUMENT_TYPE_NODE || getNodeType()==org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE
            || getNodeType()==org.w3c.dom.Node.COMMENT_NODE || getNodeType()==org.w3c.dom.Node.TEXT_NODE
            || getNodeType()==org.w3c.dom.Node.CDATA_SECTION_NODE || getNodeType()==org.w3c.dom.Node.NOTATION_NODE
            || getNodeType()==org.w3c.dom.Node.ATTRIBUTE_NODE){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR,"this Node cannot have Children");
        }
/*        if(((Node)newChild).getOwnerDocument()!=this.ownerDocument){
            throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,"");
        }
  */
		recNode.addChildNode(((Node)newChild).recNode);
		if(this.childNodes==null)this.childNodes = new NodeList(this.ownerDocument,this,this.recNode);
		((Node)newChild).parentNode = this;
		((Node)newChild).ownerDocument = this.ownerDocument;
        //wenn node kinder hat, dann in allen kindern ownerdocument korrect setzen
/*		if(((Node)newChild).hasChildNodes()){
			for(int i=0;i<newChild.getChildNodes().getLength();i++){
				((Node)newChild.getChildNodes().item(i)).setOwnerDocument(this.ownerDocument,true);
			}
		}*/
        return newChild;

    }

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
     * <p>
     * Overwrite this methode in every subclass!!!
     * 
     * @param param If <code>true</code>, recursively clone the subtree under
     *   the specified node; if <code>false</code>, clone only the node
     *   itself (and its attributes, if it is an <code>Element</code>).
     * @return The duplicate node.
     */
    public abstract org.w3c.dom.Node cloneNode(boolean param);

	/**
     * A <code>NamedNodeMap</code> containing the attributes of this node (if
     * it is an <code>Element</code>) or <code>null</code> otherwise.
     */
    public org.w3c.dom.NamedNodeMap getAttributes() {
        return null;
    }

	/**
     * A <code>NodeList</code> that contains all children of this node. If
     * there are no children, this is a <code>NodeList</code> containing no
     * nodes.
     */
    public org.w3c.dom.NodeList getChildNodes() {
    	if(this.childNodes==null){
    		this.childNodes = new NodeList(this.ownerDocument,this,this.recNode);
    	}
			return this.childNodes; 
    }

	/**
     * The first child of this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public org.w3c.dom.Node getFirstChild() {
			if(this.childNodes==null)this.childNodes = new NodeList(this.ownerDocument,this,this.recNode);
			if(this.childNodes.getLength()==0)return null;
			return this.childNodes.item(0);
    }

	/**
     * The last child of this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public org.w3c.dom.Node getLastChild() {
        if(this.childNodes==null)this.childNodes = new NodeList(this.ownerDocument,this,this.recNode);
		if(this.childNodes.getLength()==0)return null;
        return this.childNodes.item(this.childNodes.getLength()-1);
    }

	/**
     * Returns the local part of the qualified name of this node.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code>.
     * @since DOM Level 2
     */
    public String getLocalName() {
        return null;
    }

	/**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.
     * <br>This is not a computed value that is the result of a namespace
     * lookup based on an examination of the namespace declarations in
     * scope. It is merely the namespace URI given at creation time.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code>.Per
     * the Namespaces in XML Specification  an attribute does not inherit
     * its namespace from the element it is attached to. If an attribute is
     * not explicitly given a namespace, it simply has no namespace.
     * @since DOM Level 2
     */
    public String getNamespaceURI() {
			//throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
			return null;
    }

	/**
     * The node immediately following this node. If there is no such node,
     * this returns <code>null</code>.
     */
    public org.w3c.dom.Node getNextSibling() {
			if(this.parentNode==null) return null;
			if(this.parentNode.childNodes==null)this.parentNode.childNodes = new NodeList(this.ownerDocument,this.parentNode,this.parentNode.recNode);
			return this.parentNode.childNodes.next(this);
    }

	/**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
			return null;
    }

	/**
     * A code representing the type of the underlying object, as defined above.
     */
    public abstract short getNodeType();

	/**
     * The value of this node, depending on its type; see the table above.
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @throws org.w3c.dom.DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws org.w3c.dom.DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *   fit in a <code>DOMString</code> variable on the implementation
     *   platform.
     */
    public String getNodeValue() throws org.w3c.dom.DOMException {
        return null;
    }

	/**
     * The <code>Document</code> object associated with this node. This is
     * also the <code>Document</code> object used to create new nodes. When
     * this node is a <code>Document</code> or a <code>DocumentType</code>
     * which is not used with any <code>Document</code> yet, this is
     * <code>null</code>.
     * @version DOM Level 2
     */
    public org.w3c.dom.Document getOwnerDocument() {
        return ownerDocument;
    }

	/**
     * The parent of this node. All nodes, except <code>Attr</code>,
     * <code>Document</code>, <code>DocumentFragment</code>,
     * <code>Entity</code>, and <code>Notation</code> may have a parent.
     * However, if a node has just been created and not yet added to the
     * tree, or if it has been removed from the tree, this is
     * <code>null</code>.
     */
    public org.w3c.dom.Node getParentNode(){
        return parentNode;
    }

	/**
     * The namespace prefix of this node, or <code>null</code> if it is
     * unspecified.
     * <br>Note that setting this attribute, when permitted, changes the
     * <code>nodeName</code> attribute, which holds the qualified name, as
     * well as the <code>tagName</code> and <code>name</code> attributes of
     * the <code>Element</code> and <code>Attr</code> interfaces, when
     * applicable.
     * <br>Note also that changing the prefix of an attribute that is known to
     * have a default value, does not make a new attribute with the default
     * value and the original prefix appear, since the
     * <code>namespaceURI</code> and <code>localName</code> do not change.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code>.
     * @throws org.w3c.dom.DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified prefix contains an
     *   illegal character, per the XML 1.0 specification .
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NAMESPACE_ERR: Raised if the specified <code>prefix</code> is
     *   malformed per the Namespaces in XML specification, if the
     *   <code>namespaceURI</code> of this node is <code>null</code>, if the
     *   specified prefix is "xml" and the <code>namespaceURI</code> of this
     *   node is different from "http://www.w3.org/XML/1998/namespace", if
     *   this node is an attribute and the specified prefix is "xmlns" and
     *   the <code>namespaceURI</code> of this node is different from "
     *   http://www.w3.org/2000/xmlns/", or if this node is an attribute and
     *   the <code>qualifiedName</code> of this node is "xmlns" .
     * @since DOM Level 2
     */
    public String getPrefix() {
       return null;
    }

	/**
     * The node immediately preceding this node. If there is no such node,
     * this returns <code>null</code>.
     */
    public org.w3c.dom.Node getPreviousSibling() {
		 if(this.parentNode==null) return null;
		 if(this.parentNode.childNodes==null)this.parentNode.childNodes = new NodeList(this.ownerDocument,this.parentNode,this.parentNode.recNode);
       return this.parentNode.childNodes.before(this);
    }

	/**
     * Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes,
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttributes() {
        return false;
    }

	/**
     * Returns whether this node has any children.
     * @return <code>true</code> if this node has any children,
     *   <code>false</code> otherwise.
     */
    public boolean hasChildNodes() {
        if(this.recNode==null) return false;
        if (this.childNodes == null) this.childNodes = new NodeList(this.ownerDocument,this,this.recNode);
        if(this.childNodes != null && this.childNodes.getLength()>0)return true;
        return false;
    }

    /**
     * Inserts the node <code>newChild</code> before the existing child node
     * <code>refChild</code>. If <code>refChild</code> is <code>null</code>,
     * insert <code>newChild</code> at the end of the list of children.
     * <br>If <code>newChild</code> is a <code>DocumentFragment</code> object,
     * all of its children are inserted, in the same order, before
     * <code>refChild</code>. If the <code>newChild</code> is already in the
     * tree, it is first removed.
     * @param newChild The node to insert.
     * @param refChild The reference node, i.e., the node before which the
     *   new node must be inserted.
     * @return The node being inserted.
     * @throws org.w3c.dom.DOMException
     *   HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *   allow children of the type of the <code>newChild</code> node, or if
     *   the node to insert is one of this node's ancestors or this node
     *   itself.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created
     *   from a different document than the one that created this node.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly or
     *   if the parent of the node being inserted is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>refChild</code> is not a child of
     *   this node.
     */

    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node newChild, org.w3c.dom.Node refChild)
                                         throws org.w3c.dom.DOMException {
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
        
											 
	}

	/**
     * Tests whether the DOM implementation implements a specific feature and
     * that feature is supported by this node.
     * @param feature The name of the feature to test. This is the same name
     *   which can be passed to the method <code>hasFeature</code> on
     *   <code>DOMImplementation</code>.
     * @param version This is the version number of the feature to test. In
     *   Level 2, version 1, this is the string "2.0". If the version is not
     *   specified, supporting any version of the feature will cause the
     *   method to return <code>true</code>.
     * @return Returns <code>true</code> if the specified feature is
     *   supported on this node, <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean isSupported(String feature, String version) {
        return false;
    }

	/**
     * Puts all <code>Text</code> nodes in the full depth of the sub-tree
     * underneath this <code>Node</code>, including attribute nodes, into a
     * "normal" form where only structure (e.g., elements, comments,
     * processing instructions, CDATA sections, and entity references)
     * separates <code>Text</code> nodes, i.e., there are neither adjacent
     * <code>Text</code> nodes nor empty <code>Text</code> nodes. This can
     * be used to ensure that the DOM view of a document is the same as if
     * it were saved and re-loaded, and is useful when operations (such as
     * XPointer  lookups) that depend on a particular document tree
     * structure are to be used.In cases where the document contains
     * <code>CDATASections</code>, the normalize operation alone may not be
     * sufficient, since XPointers do not differentiate between
     * <code>Text</code> nodes and <code>CDATASection</code> nodes.
     * @version DOM Level 2
     */
    public void normalize() {
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
        
    }

    /**
     * Removes the child node indicated by <code>oldChild</code> from the list
     * of children, and returns it.
     * @param oldChild The node being removed.
     * @return The node removed.
     * @throws org.w3c.dom.DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>oldChild</code> is not a child of
     *   this node.
     */
    public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild) throws org.w3c.dom.DOMException {
			//Zuerst die Exceptions abfangen
/*		if(readonly){
			throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,"this Node is readonly");
		}
		if(!this.childNodes.getContent().contains(oldChild)){
			throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_FOUND_ERR,"Node not found");
		}
		//kind enfernen
		this.childNodes.getContent().remove(oldChild);
		this.recNode.getChildList().remove( ((Node)oldChild).recNode );
		return oldChild;*/
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
        
    }
		
	/**
     * Replaces the child node <code>oldChild</code> with <code>newChild</code>
     *  in the list of children, and returns the <code>oldChild</code> node.
     * <br>If <code>newChild</code> is a <code>DocumentFragment</code> object,
     * <code>oldChild</code> is replaced by all of the
     * <code>DocumentFragment</code> children, which are inserted in the
     * same order. If the <code>newChild</code> is already in the tree, it
     * is first removed.
     * @param newChild The new node to put in the child list.
     * @param oldChild The node being replaced in the list.
     * @return The node replaced.
     * @throws org.w3c.dom.DOMException
     *   HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *   allow children of the type of the <code>newChild</code> node, or if
     *   the node to put in is one of this node's ancestors or this node
     *   itself.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created
     *   from a different document than the one that created this node.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node or the parent of
     *   the new node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>oldChild</code> is not a child of
     *   this node.
     */
	public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild, org.w3c.dom.Node oldChild)
                                        throws org.w3c.dom.DOMException {
/*		//Zuerst die Exceptions abfangen
		if(readonly || ((Node)this.parentNode).readonly){
			throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,"this Node is readonly");
		}
		if(!this.childNodes.getContent().contains(oldChild)){
			throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_FOUND_ERR,"Node not found");
		}
		if(newChild.getOwnerDocument()!=this.ownerDocument){
			throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,"Node was created from a different document than the one that created this node");
		}
		
		//If the "newChild" is already exist, it is first removed.
		if(!this.childNodes.getContent().contains(newChild)){
			this.removeChild(newChild);
		}
		
		List childs = this.childNodes.getContent();
		List recChilds = this.recNode.getChildList();
		int index = childs.indexOf(oldChild);
		int indexRec = childs.indexOf(((Node)oldChild).recNode);
		childs.set(index,newChild);
		childs.set(indexRec,((Node)newChild).recNode);
		
		this.childNodes.setContent(childs);
		this.recNode.setChilds(recChilds);
		//parent in neuem Node setzen
		((Node)newChild).parentNode = this;
		
		return oldChild;*/
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"not implemented");
        
    }

    /**
     * The value of this node, depending on its type; see the table above.
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @throws org.w3c.dom.DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws org.w3c.dom.DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *   fit in a <code>DOMString</code> variable on the implementation
     *   platform.
     */
	public abstract void  setNodeValue(String str) throws org.w3c.dom.DOMException;
	
	/**
     * The namespace prefix of this node, or <code>null</code> if it is
     * unspecified.
     * <br>Note that setting this attribute, when permitted, changes the
     * <code>nodeName</code> attribute, which holds the qualified name, as
     * well as the <code>tagName</code> and <code>name</code> attributes of
     * the <code>Element</code> and <code>Attr</code> interfaces, when
     * applicable.
     * <br>Note also that changing the prefix of an attribute that is known to
     * have a default value, does not make a new attribute with the default
     * value and the original prefix appear, since the
     * <code>namespaceURI</code> and <code>localName</code> do not change.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code>.
     * @throws org.w3c.dom.DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified prefix contains an
     *   illegal character, per the XML 1.0 specification .
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NAMESPACE_ERR: Raised if the specified <code>prefix</code> is
     *   malformed per the Namespaces in XML specification, if the
     *   <code>namespaceURI</code> of this node is <code>null</code>, if the
     *   specified prefix is "xml" and the <code>namespaceURI</code> of this
     *   node is different from "http://www.w3.org/XML/1998/namespace", if
     *   this node is an attribute and the specified prefix is "xmlns" and
     *   the <code>namespaceURI</code> of this node is different from "
     *   http://www.w3.org/2000/xmlns/", or if this node is an attribute and
     *   the <code>qualifiedName</code> of this node is "xmlns" .
     * @since DOM Level 2
     */
    public void setPrefix(String str) throws org.w3c.dom.DOMException {
    }
	
	//setzt ownerDocument auf owner, wenn rek=true, werden bei kindern auch ownerDocument gesetzt 
/*	private void setOwnerDocument(org.w3c.dom.Document owner, boolean rek) {
		this.ownerDocument = (Document)owner;
		if(rek && this.hasChildNodes() ){
			for(int i=0;i<this.getChildNodes().getLength();i++){
				((Node)this.getChildNodes().item(i)).setOwnerDocument(owner,true);
			}
		}
	}
*/	
	protected Node getNodeByType(xxl.core.xml.storage.Node node, short type){
		switch(type){
			case org.w3c.dom.Node.TEXT_NODE : return new Text(this.ownerDocument,this,node);
			case org.w3c.dom.Node.ATTRIBUTE_NODE : return new Attr(this.ownerDocument,this,node);
			case org.w3c.dom.Node.ELEMENT_NODE :return new Element(this.ownerDocument,this,node);
//			case org.w3c.dom.Node.CDATA_SECTION_NODE :return new CDATASection(node);
//			case org.w3c.dom.Node.COMMENT_NODE :return new Comment(node);
//			case org.w3c.dom.Node.DOCUMENT_NODE :return new Document(node);
			
		}
		return null;
	}

	public short compareDocumentPosition(org.w3c.dom.Node other) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String getBaseURI() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public Object getFeature(String feature, String version) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String getTextContent() throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public Object getUserData(String key) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean isDefaultNamespace(String namespaceURI) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean isEqualNode(org.w3c.dom.Node arg) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean isSameNode(org.w3c.dom.Node other) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String lookupNamespaceURI(String prefix) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public String lookupPrefix(String namespaceURI) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setTextContent(String textContent) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public Object setUserData(String key, Object data, UserDataHandler handler) {
		throw new UnsupportedOperationException("not implemented yet");
	}
}
