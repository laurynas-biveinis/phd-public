/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage.dom;

import org.w3c.dom.Node;

/**
 * Is implemented for named Nodes as <code>Attr</code>
 */
public class NamedNodeMap implements org.w3c.dom.NamedNodeMap {

	//private Variablen
	private boolean readonly = false;
	private xxl.core.xml.storage.dom.Document ownerDocument;
	private xxl.core.xml.storage.dom.Node parentNode;
	private xxl.core.xml.storage.Node node;

	/**
	 * Create a new Instance of <code>NamedNodeMap</code> to a <code>Element</code> node. 
	 * 
	 * @param ownerDoc Owner document
	 * @param parent   Parent node for this map.
	 * @param node     <code>storage Markup</code> node that this map based on.
	 */
	protected NamedNodeMap(xxl.core.xml.storage.dom.Document ownerDoc, xxl.core.xml.storage.dom.Node parent, xxl.core.xml.storage.Node node) {
		this.ownerDocument = ownerDoc;
		this.parentNode = parent;
		this.node = node;
	}

	/**
	 * Retrieves a node specified by name.
	 * 
	 * @param name The <code>nodeName</code> of a node to retrieve.
	 * @return A <code>Node</code> with the specified <code>nodeName</code>, 
	 * 	or <code>null</code> if it does not identify any node in this map.
	 */
	public org.w3c.dom.Node getNamedItem(String name) {
		//java.util.Iterator it = this.ownerDocument.tree.getXMLChilds(node);
		//while (it.hasNext()) System.out.println("Kind: " + ((xxl.xml.storage.Node)it.next()).toString());
		//System.out.println(it.toString());
		java.util.Iterator listIterator = this.ownerDocument.tree.getXMLChildren(node);;
		while (listIterator.hasNext()) {
			xxl.core.xml.storage.Node node = (xxl.core.xml.storage.Node) listIterator.next();
			if (node.getType() == xxl.core.xml.storage.Node.MARKUP_NODE
				&& ((xxl.core.xml.storage.MarkupNode) node).isAttribute()
				&& ((xxl.core.xml.storage.MarkupNode) node).getTagName().equals(name))
				return new Attr(this.ownerDocument, this.parentNode, node);
		}
		return null;
	}

	/**
	 * Adds a node using its <code>nodeName</code> attribute. If a node with 
	 * that name is already present in this map, it is replaced by the new one.
	 * 
	 * @param arg A node to store in this map. The node will later be 
	 *   accessible using the value of its <code>nodeName</code> attribute.
	 * @return If the new <code>Node</code> replaces an existing node the 
	 *   replaced <code>Node</code> is returned, otherwise <code>null</code> 
	 *   is returned.
	 * 
	 * @throws org.w3c.dom.DOMException
	 *   WRONG_DOCUMENT_ERR: Raised if <code>arg</code> was created from a 
	 *   different document than the one that created this map.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this map is readonly.
	 *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>arg</code> is an 
	 *   <code>Attr</code> that is already an attribute of another 
	 *   <code>Element</code> object. The DOM user must explicitly clone 
	 *   <code>Attr</code> nodes to re-use them in other elements.
	 *   <br>HIERARCHY_REQUEST_ERR: Raised if an attempt is made to add a node 
	 *   doesn't belong in this NamedNodeMap. Examples would include trying 
	 *   to insert something other than an Attr node into an Element's map 
	 *   of attributes, or a non-Entity node into the DocumentType's map of 
	 *   Entities.
	 */
	public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node arg) throws org.w3c.dom.DOMException {
		if (this.readonly)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Map is read only");
		if (arg.getOwnerDocument() != this.ownerDocument)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,
				"Map and node owner document are different");
		if (arg.getParentNode() != this.parentNode)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.INUSE_ATTRIBUTE_ERR,
				"Node is in use by another element");
		if (this.parentNode.getNodeType() == Node.ELEMENT_NODE && arg.getNodeType() != Node.ATTRIBUTE_NODE)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR,
				"Only Attr node is allowed for a Element node.");

		xxl.core.xml.storage.dom.Node node = (xxl.core.xml.storage.dom.Node) this.getNamedItem(arg.getNodeName());
		if (node == null) {
			// Knoten ist nicht vorhanden -> einfügen
			this.node.addChildNode(((xxl.core.xml.storage.dom.Node) arg).recNode);
		} else {
			// Knoten wurde gefunden -> ersetzen und zurückgeben
			java.util.List childs = this.node.getChildList();
			int pos = childs.indexOf(node);
			childs.remove(node);
			childs.add(pos, ((xxl.core.xml.storage.dom.Node) arg).recNode);
			return arg;
		}
		return null;
	}

	/**
	 * Removes a node specified by name. 
	 * 
	 * @param name The <code>nodeName</code> of the node to remove.
	 * @return The node removed from this map if a node with such a name exists.
	 * 
	 * @exception DOMException
	 *   NOT_FOUND_ERR: Raised if there is no node named <code>name</code> in 
	 *   this map.
	 *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this map is readonly.
	 */
	public org.w3c.dom.Node removeNamedItem(String name) throws org.w3c.dom.DOMException {
		if (this.readonly)
			throw new org.w3c.dom.DOMException(
				org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"Map is read only");
		java.util.Iterator listIterator = this.ownerDocument.tree.getXMLChildren(node);
		while (listIterator.hasNext()) {
			xxl.core.xml.storage.Node node = (xxl.core.xml.storage.Node) listIterator.next();
			if (node.getType() == xxl.core.xml.storage.Node.MARKUP_NODE
				&& ((xxl.core.xml.storage.MarkupNode) node).isAttribute()
				&& ((xxl.core.xml.storage.MarkupNode) node).getTagName().equals(name)) {
				java.util.List childs = this.node.getChildList();
				childs.remove(node);
				return new Attr(this.ownerDocument, this.parentNode, node);
			}
		}

		// es wurde kein Knoten gefunden -> Exception
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_FOUND_ERR, "Node not found in map");
	}

	/**
	 * Returns the <code>index</code>th item in the map. If <code>index</code> 
	 * is greater than or equal to the number of nodes in this map, this 
	 * returns <code>null</code>.
	 * 
	 * @param index Index into this map.
	 * @return The node at the <code>index</code>th position in the map, or 
	 *   <code>null</code> if that is not a valid index.
	 */
	public org.w3c.dom.Node item(int index) {
		int i = 0;
		java.util.Iterator listIterator = this.ownerDocument.tree.getXMLChildren(node);;
		while (listIterator.hasNext()) {
			xxl.core.xml.storage.Node node = (xxl.core.xml.storage.Node) listIterator.next();
			if (node.getType() == xxl.core.xml.storage.Node.MARKUP_NODE
				&& ((xxl.core.xml.storage.MarkupNode) node).isAttribute())
				if (i == index)
					return new Attr(this.ownerDocument, this.parentNode, node);
				else
					i++;
		}
		return null;
	}

	/**
	 * The number of nodes in this map. 
	 * The range of valid child node indices is <code>0</code> to <code>length-1</code> inclusive.
	 * 
	 * @return Number of nodes.
	 */
	public int getLength() {
		int i = 0;
		java.util.Iterator listIterator = this.ownerDocument.tree.getXMLChildren(node);
		while (listIterator.hasNext()) {
			xxl.core.xml.storage.Node node = (xxl.core.xml.storage.Node) listIterator.next();
			if (node.getType() == xxl.core.xml.storage.Node.MARKUP_NODE
				&& ((xxl.core.xml.storage.MarkupNode) node).isAttribute())
				i++;
		}
		return i;
	}

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @param namespaceURI The namespace URI of the node to retrieve.
	 * @param localName The local name of the node to retrieve.
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @since DOM Level 2
	 */
	public org.w3c.dom.Node getNamedItemNS(String namespaceURI, String localName) {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	}

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @param arg A node to store in this map. 
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 *   
	 * @since DOM Level 2
	 */
	public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node arg) throws org.w3c.dom.DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "namespaces aren't supported");
	}

	/**
	 * Is not supported in this implementation of DOM
	 * 
	 * Its return allways a <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @param namespaceURI The namespace URI of the node to remove.
	 * @param localName The local name of the node to remove.
	 * @return <code>NOT_SUPPORTED_ERR</code>
	 * 
	 * @since DOM Level 2
	 */
	public org.w3c.dom.Node removeNamedItemNS(String namespaceURI, String localName) throws org.w3c.dom.DOMException {
		return null;
	}
}
