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

import org.w3c.dom.TypeInfo;

/**
 * The <code>Attr</code> class represents an attribute in an <code>Element</code> object. 
 * 
 * <p><code>Attr</code> objects inherit the <code>Node</code> interface, but 
 * since they are not actually child nodes of the element they describe, the 
 * DOM does not consider them part of the document tree. Thus, the 
 * <code>Node</code> attributes <code>parentNode</code>, 
 * <code>previousSibling</code>, and <code>nextSibling</code> have a 
 * <code>null</code> value for <code>Attr</code> objects.  
 * <code>Attr</code> nodes may not be immediate children of a 
 * <code>DocumentFragment</code>. However, they can be associated with 
 * <code>Element</code> nodes contained within a 
 * <code>DocumentFragment</code>. In short, users and implementors of the 
 * DOM need to be aware that <code>Attr</code> nodes have some things in 
 * common with other objects inheriting the <code>Node</code> interface, but 
 * they also are quite distinct.</p>
 * <p>The attribute's effective value is determined only, when attribute has 
 * been explicitly assigned any value, that value is the 
 * attribute's effective value; Other kind of value determination we are not supported.
 * Values are simple strings.</p>
 */
public class Attr extends Node implements org.w3c.dom.Attr {

	//Konstruktoren
	/**
	 * A standart constructor for a new Attr node from a exsist recNode.
	 * 
	 * @param owner document for this new Attr node.
	 * @param parent node for this new Attr node.
	 * @param Node from storage pakage, that represent a Attr node. 
	 * 	Also a Markup node with a Literal node and boolean value for 
	 * 	isAttribut is true.  
	 */
	protected Attr(xxl.core.xml.storage.dom.Document owner, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.Node node) {
		this.ownerDocument = owner;
		this.parentNode = parent;
		this.recNode = (xxl.core.xml.storage.MarkupNode) node;
	}

	/**
	 * A standart constructor for a new Attr node from a string. 
	 * A node for this Attr in EXTree isn't exsist. This constructor is
	 * usefull for completely new attributes. 
	 * 
	 * @param owner document for this new Attr node.
	 * @param parent node for this new Attr node.
	 * @param Name of the attribute.  
	 */
	protected Attr(String name, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.dom.Document owner) {
		this.ownerDocument = owner;
		this.parentNode = parent;
		this.recNode = new xxl.core.xml.storage.MarkupNode(name, true);
	}

	//Methoden

	/**
	 * Returns the name of the attribute.
	 */
	public String getName() {
		return ((xxl.core.xml.storage.MarkupNode) this.recNode).getTagName();
	};

	/**
	 * Proof if a value of this attribute exsist.
	 * 
	 * @return <code>true</code> if a value exsist.
	 */
	public boolean getSpecified() {
		xxl.core.xml.storage.LiteralNode litNode =
					(xxl.core.xml.storage.LiteralNode) ((xxl.core.xml.storage.MarkupNode) this.recNode).getFirstChild();
		String value = new String(litNode.getContent());
		return (value != null);
	};

	/**
	 * The value of the attribute is returned as a string.
	 * 
	 * @return The value of the attribute.
	 */
	public String getValue() {
		xxl.core.xml.storage.LiteralNode litNode =
			(xxl.core.xml.storage.LiteralNode) ((xxl.core.xml.storage.MarkupNode) this.recNode).getFirstChild();
		return new String(litNode.getContent());
	};

	/**
	 * Set value of the attribute as simple string.
	 * 
	 * @exception DOMException
	 *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
	 */
	public void setValue(String value) throws org.w3c.dom.DOMException {

		/* 
		 * Ein Attr Knoten in storage besteht aus einem Markup und enem Literal
		 * Knoten. Also wird der eigentliche Inhalt implements LiteralNode
		 * gespeichert. Bevor aber dieser gesetzt wird wird zuerst geprüft
		 * ob der Konoten schon vorhanden ist und nur aktualisiert werden soll.
		 */
		if (this.readonly)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Node is read only");
		java.util.Iterator childs = this.ownerDocument.tree.getXMLChildren(this.recNode); ;
		if (childs.hasNext()) {
			xxl.core.xml.storage.LiteralNode valueNode = (xxl.core.xml.storage.LiteralNode) childs.next();
			valueNode.setContent(value.getBytes());
		} else {
			xxl.core.xml.storage.LiteralNode valueNode =
				new xxl.core.xml.storage.LiteralNode(value.getBytes(), xxl.core.xml.storage.LiteralNode.STRING);
			this.recNode.addChildNode(valueNode);
		}
	};

	/**
	 * The <code>Element</code> node this attribute is attached to or
	 * <code>null</code> if this attribute is not in use.
	 * 
	 * @return Owner element.
	 * @since DOM Level 2
	 */
	public org.w3c.dom.Element getOwnerElement() {
		return (Element) this.parentNode;
	}

	/**
	* A atribute returns always <code>null</code>.
	* 
	* @return <code>null</code>
		*/
	public org.w3c.dom.Node getParentNode() {
		return null;
	}

	/**
		* A atribute returns always <code>null</code>.
		* 
		* @return <code>null</code>
		*/
	public org.w3c.dom.Document getOwnerDocument() {
		return null;
	}

	/** 
	 * Produce a clone of the attribute with the same value,
	 * but without link to owner element and document.
	 *   
	 * @param By attribute without effect
	 * @return The duplicate node.
	 */
	public org.w3c.dom.Node cloneNode(boolean param) {
		Attr clone = new Attr(this.getName(), null, null);
		clone.setValue(this.getValue());
		return clone;
	}

	/** 
	 * The name of this node.
	 * By attribute it is a name of the attribute.
	 * 
	 * @return Name of the attribute 
	 */
	public String getNodeName() {
		return ((xxl.core.xml.storage.MarkupNode) this.recNode).getTagName();
	}

	/**
	 * Get type of this Node. 
	 * It ist <code>Node.ATTRIBUTE_NODE</code> for a attribute.
	 * 
	 * @return <code>Node.ATTRIBUTE_NODE</code>
	 */
	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}

	/**
	 * Get value of this node. 
	 * By a attribute it is this value.
	 * 
	 * @return Value of attribute. 
	 */
	public String getNodeValue() throws org.w3c.dom.DOMException {
		return this.getValue();
	}

	/**
	 * Set value for the attribute.
	 * The type from value is a simple string. 
	 * 
	 * @param A string as value for the attribute. 
	 */
	public void setNodeValue(String str) throws org.w3c.dom.DOMException {
		this.setValue(str);
	}



	/**
	 * Attr have no such node, this allways returns <code>null</code>.
	 * 
	 * @return <code>null</code>
	 */
	public org.w3c.dom.Node getPreviousSibling() {
		return null;
	}

	/**
	 * Attr have no such node, this allways returns <code>null</code>.
	 * 
	 * @return <code>null</code>
	 */
	public org.w3c.dom.Node getNextSibling() {
		return null;
	}

	public TypeInfo getSchemaTypeInfo() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean isId() {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
