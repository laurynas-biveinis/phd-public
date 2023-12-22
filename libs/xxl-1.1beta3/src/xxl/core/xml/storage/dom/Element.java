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
import org.w3c.dom.TypeInfo;

/**
 * The <code>Element</code> represents an element in a XML document. 
 * 
 * <p>Elements may have attributes associated with them.</p> 
 * 
 * <p>There are methods on the Element interface to retrieve either 
 * an Attr object by name or an attribute value by name.</p>
 */
public class Element extends Node implements org.w3c.dom.Element {

	/**
	 * A standard constructor to get a <code>Element</code> node to a exist <code>storage</code> node.
	 * A <code>Element</code> is equivalent to <code>Markup</code> node from <code>storage</code>  
	 * 
	 * @param Owner document
	 * @param Parent node
	 * @param <code>storage Markup</code> node for the element  
	 */
	protected Element(xxl.core.xml.storage.dom.Document owner, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.Node node) {
		this.ownerDocument = owner;
		this.parentNode = parent;
		this.recNode = node;
		this.childNodes = null;
	}

	/**
	 * A standard constructor to get a new <code>Element</code> node without a exist <code>storage</code> node.
	 * Its create a new <code>Markup</code> node too.
	 * 
	 * @param Name of the element.
	 * @param Parent node.
	 * @param Owner document  
	 */
	protected Element(String tagName, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.dom.Document owner) {
		this.ownerDocument = owner;
		this.parentNode = parent;
		this.recNode = new xxl.core.xml.storage.MarkupNode(tagName);
	}

	//Methoden

	/**
	 * The name of the element. 
	 * Note that this is case-preserving in XML, as are all of the operations of the DOM. 
	 * 
	 * @return Name of the element.
	 */
	public String getTagName() {
		return ((xxl.core.xml.storage.MarkupNode) this.recNode).getTagName();
	};

	/**
	 * Retrieves an attribute value by name.
	 * 
	 * @param name The name of the attribute to retrieve.
	 * @return The <code>Attr</code> value as a string, or the empty string 
	 *   if that attribute does not have a specified value.
	 */
	public String getAttribute(String name) {
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		Attr result = (Attr) this.attributes.getNamedItem(name);
		if (result != null)
			return result.getValue();
		return "";
	};

	/**
	 * Adds a new attribute. 
	 * If an attribute with that name is already present in the element, 
	 * its value is changed to be that of the value parameter. 
	 * This value is a simple string. 
	 * 
	 * @param name The name of the attribute to create or alter.
	 * @param value Value to set in string form.
	 * 
	 * @exception DOMException
	 *   INVALID_CHARACTER_ERR: Raised if the specified name contains an 
	 *   illegal character.<br>
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void setAttribute(String name, String value) throws org.w3c.dom.DOMException {
		if (this.readonly)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Node is read only");
		xxl.core.xml.storage.MarkupNode child = new xxl.core.xml.storage.MarkupNode(name, true);
		xxl.core.xml.storage.LiteralNode valueNode =
			new xxl.core.xml.storage.LiteralNode(value.getBytes(), xxl.core.xml.storage.LiteralNode.STRING);
		child.addChildNode(valueNode);
		xxl.core.xml.storage.dom.Node attr = new Attr(this.ownerDocument, this, child);
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		this.attributes.setNamedItem((org.w3c.dom.Node) attr);
		this.recNode.addChildNode(child);
	};

	/**
	 * Removes an attribute by name. 
	 * 
	 * @param name The name of the attribute to remove.
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 */
	public void removeAttribute(String name) throws org.w3c.dom.DOMException {
		if (this.readonly)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Node is read only");
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		this.attributes.removeNamedItem(name);

	};

	/**
	 * Retrieves an attribute node by name.
	 *
	 * @param name The name (<code>nodeName</code>) of the attribute to retrieve.
	 * @return The <code>Attr</code> node with the specified name (
	 *   <code>nodeName</code>) or <code>null</code> if there is no such 
	 *   attribute.
	 */
	public org.w3c.dom.Attr getAttributeNode(String name) {
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		return (org.w3c.dom.Attr) this.attributes.getNamedItem(name);
	};

	/**
	 * Adds a new attribute node. 
	 * 
	 * If an attribute with that name (<code>nodeName</code>) is already present 
	 * in the element, it is replaced by the new one. <br>
	 * 
	 * @param newAttr The <code>Attr</code> node to add to the attribute list.
	 * @return If the <code>newAttr</code> attribute replaces an existing 
	 *   attribute, the replaced <code>Attr</code> node is returned, 
	 *   otherwise <code>null</code> is returned.
	 * 
	 * @exception DOMException
	 *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from a 
	 *   different document than the one that created the element.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an 
	 *   attribute of another <code>Element</code> object. The DOM user must 
	 *   explicitly clone <code>Attr</code> nodes to re-use them in other 
	 *   elements.
	 */
	public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr newAttr) throws org.w3c.dom.DOMException {
		if (this.readonly)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Node is read only");
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		((xxl.core.xml.storage.dom.Attr) newAttr).parentNode = this;
		org.w3c.dom.Node attr = this.attributes.setNamedItem((org.w3c.dom.Node) newAttr);
		return (org.w3c.dom.Attr) attr;
	};

	/**
	 * Removes the specified attribute node. 
	 * 
	 * @param oldAttr The <code>Attr</code> node to remove from the attribute list.
	 * @return The <code>Attr</code> node that was removed.
	 * 
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
	 *   <br>NOT_FOUND_ERR: Raised if <code>oldAttr</code> is not an attribute 
	 *   of the element.
	 */
	public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr oldAttr) throws org.w3c.dom.DOMException {
		if (this.readonly)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Node is read only");
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		org.w3c.dom.Node attr = this.attributes.removeNamedItem(oldAttr.getNodeName());
		return (org.w3c.dom.Attr) attr;
	};

	/**
	 * Returns a <code>NodeList</code> of all descendant <code>Elements</code> 
	 * with a given tag name, in the order in which they are encountered in 
	 * a preorder traversal of this <code>Element</code> tree.
	 * 
	 * @param name The name of the tag to match on. The special value "*" 
	 *   matches all tags.
	 * @return A list of matching <code>Element</code> nodes.
	 */
	public org.w3c.dom.NodeList getElementsByTagName(String name) {
		return (org.w3c.dom.NodeList) this.preoderTraverse(this, this, name);
	};

	private xxl.core.xml.storage.dom.NodeList preoderTraverse(Node startNode, Node node, String tagName) {
		NodeList foundList = new NodeList();
		if (node != null) {
			if (node != startNode && node.getNodeType() == Node.ELEMENT_NODE && (node.getNodeName().equals(tagName) || tagName.equals("*"))){
				foundList.append(node);
			} 
			org.w3c.dom.NodeList liste = node.getChildNodes();
			if (liste != null) {
				int lenght = liste.getLength();
				for (int i = 0; i < lenght; i++) {
					xxl.core.xml.storage.dom.NodeList tempList = preoderTraverse(startNode, (xxl.core.xml.storage.dom.Node) liste.item(i), tagName);
					int tempLenght = tempList.getLength();
					for (int j = 0; j < tempLenght; j++) {
						foundList.append(tempList.item(j));
					}
				}
			}
		}
		return foundList;
	}

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * @param namespaceURI The namespace URI of the attribute to retrieve.
	 * @param localName The local name of the attribute to retrieve.
	 * @return <code>DOMException.NOT_SUPPORTED_ERR</code>
	 * 
	 * @since DOM Level 2
	 */
	public String getAttributeNS(String namespaceURI, String localName) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	};

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @param namespaceURI The namespace URI of the attribute to create or alter.
	 * @param qualifiedName The qualified name of the attribute to create or alter.
	 * @param value The value to set in string form.
	 * 
	 * @since DOM Level 2
	 */
	public void setAttributeNS(String namespaceURI, String qualifiedName, String value)
		throws org.w3c.dom.DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");

	};

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 *
	 * @param namespaceURI The namespace URI of the attribute to remove.
	 * @param localName The local name of the attribute to remove.
	 *
	 * @since DOM Level 2
	 */
	public void removeAttributeNS(String namespaceURI, String localName) throws org.w3c.dom.DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	};

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @param namespaceURI The namespace URI of the attribute to retrieve.
	 * @param localName The local name of the attribute to retrieve.
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @since DOM Level 2
	 */
	public org.w3c.dom.Attr getAttributeNodeNS(String namespaceURI, String localName) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	};

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @param newAttr The <code>Attr</code> node to add to the attribute list.
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 *
	 * @since DOM Level 2
	 */
	public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr newAttr) throws org.w3c.dom.DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	};

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 *
	 * @param namespaceURI The namespace URI of the elements to match on. 
	 * 		The special value "*" matches all namespaces.
	 * @param localName The local name of the elements to match on. 
	 * 		The special value "*" matches all local names.
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @since DOM Level 2
	 */
	public org.w3c.dom.NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	};

	/**
	 * Returns <code>true</code> when an attribute with a given name is 
	 * specified on this element, <code>false</code> otherwise.
	 * 
	 * @param name The name of the attribute to look for.
	 * @return <code>true</code> if an attribute with the given name is 
	 *   specified on this element, <code>false</code> otherwise.
	 * 
	 * @since DOM Level 2
	 */
	public boolean hasAttribute(String name) {
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		return (this.attributes.getNamedItem(name) != null);
	};

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 *
	 * @param namespaceURI The namespace URI of the attribute to look for.
	 * @param localName The local name of the attribute to look for.
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @since DOM Level 2
	 */
	public boolean hasAttributeNS(String namespaceURI, String localName) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	}

	/**
	 *  Returns a duplicate of this node, i.e., serves as a generic copy
	 * constructor for nodes. The duplicate node has no parent; (
	 * <code>parentNode</code> is <code>null</code>.).
	 * <br>Cloning an <code>Element</code> copies all attributes and their values.
	 * 
	 * @param deep If <code>true</code>, recursively clone the subtree under
	 *   the specified node; if <code>false</code>, clone only the node
	 *   itself (and its attributes, if it is an <code>Element</code>).
	 * @return The duplicate node.
	 */
	public org.w3c.dom.Node cloneNode(boolean param) {
		Element clone = new Element(this.getTagName(), null, null);
		clone.parentNode = null;
		// Attribute werden auf jeden Fall kopiert
		NamedNodeMap cloneAttributes = new NamedNodeMap(null, clone, clone.recNode);
		int attrLength = this.attributes.getLength();
		for (int i = 0; i < attrLength; i++) {
			Node attr = (Node) ((Node) this.attributes.item(i)).cloneNode(param);
			attr.parentNode = this;
			cloneAttributes.setNamedItem(attr);
		}
		clone.attributes = cloneAttributes;

		// kopiere alle Kinder diesen Knotes, falls gefordert 	
		if (param) {
			int childrenLength = this.childNodes.getLength();
			NodeList cloneChilds = new NodeList(null, clone, clone.recNode);
			for (int i = 0; i < childrenLength; i++) {
				Node child = (Node) ((Node) this.childNodes.item(i)).cloneNode(param);
				child.parentNode = this;
				cloneChilds.append(child);
			}

			clone.childNodes = cloneChilds;
		}
		return (org.w3c.dom.Node) clone;
	}

	/**
	 * Get the type of the node.
	 * It is  <code>org.w3c.dom.Node.ELEMENT_NODE</code> for a element.
	 * 
	 * @return <code>org.w3c.dom.Node.ELEMENT_NODE</code>
	 */
	public short getNodeType() {
		return org.w3c.dom.Node.ELEMENT_NODE;
	}

	/**
	 * Get Name of the element. It is equals to tag name.
	 * 
	 * @return Name of the element  
	 */
	public String getNodeName() {
		return this.getTagName();
	}

	/** 
	 * This method has no effect for a element.  
	 */
	public void setNodeValue(String str) throws org.w3c.dom.DOMException {
	}

	/**
	 * Returns whether this node  has any attributes.
	 * 
	 * @return <code>true</code> if this node has any attributes,
	 *   <code>false</code> otherwise.
	 * 
	 * @since DOM Level 2
	 */
	public boolean hasAttributes() {
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		if (this.attributes.getLength() > 0)
			return true;
		return false;
	}

	/**
	 * A <code>NamedNodeMap</code> containing the attributes of this node or <code>null</code> otherwise.
	 * 
	 * @return A <code>NamedNodeMap</code> containing the attributes of this element
	 */
	public org.w3c.dom.NamedNodeMap getAttributes() {
		if (this.attributes == null)
			this.attributes = new NamedNodeMap(this.ownerDocument, this, this.recNode);
		return this.attributes;
	}
	
	public TypeInfo getSchemaTypeInfo() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setIdAttribute(String name, boolean isId) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setIdAttributeNode(org.w3c.dom.Attr idAttr, boolean isId) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
