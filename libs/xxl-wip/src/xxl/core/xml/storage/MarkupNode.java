/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xxl.core.io.converters.SizeConverter;

/**
 * This class is an extention of the abstract class Node. It has the role
 * of the XML-Tags as we know from XML documents.
 *
 * An markup node can have child nodes and can standalone (i.e. the root node of a document)
 */
public class MarkupNode extends Node {

	/**
	 * Constructs a converter for MarkupNodes.
	 */
	public static SizeConverter getConverter() {
		return new SizeConverter() {
			/**
			 * Serializes this object to the given dataOutput-Stream.
			 * @param dataOutput the output where to write the node
			 */
			public void write(DataOutput dataOutput, Object o) throws IOException {
				MarkupNode n = (MarkupNode) o;

				dataOutput.writeShort(n.internalId);
				dataOutput.writeShort(n.internalParentId); //the internal parent ID
				dataOutput.writeByte((n.isAttribute())?1:0);
				dataOutput.writeShort(n.tagName.length());
				dataOutput.writeBytes(n.tagName);
			}
			/** Reads all needes informations from the given dataInput-Stream. */
			public Object read(DataInput dataInput, Object o) throws IOException {
				MarkupNode n;
				if (o==null)
					n = new MarkupNode();
				else
					n = (MarkupNode) o;
				
				n.internalId = dataInput.readShort();
				n.internalParentId = dataInput.readShort();
				n.isAttribute = dataInput.readByte()==1;
				byte[] nameB = new byte[(int) dataInput.readUnsignedShort()]; //the size of the tagName String
				dataInput.readFully(nameB);
				n.tagName = new String(nameB);
				
				return n;
			}
			/**
			 * Calculates and returns the size of this Node.
			 * Including the size of the required header-informations (i.e. size, ...).
			 * That is the size the serialized Object would require.
			 * @return the size of this Node
			 */
			public int getSerializedSize(Object o) {
				MarkupNode n = (MarkupNode) o;
				// extra Bytes: length of tagName, internalId
				return 7+n.tagName.length();
			}
		};
	}

	/**
	 * Child list of the children. 
	 */
	protected List childList;
	
	/**
	 * Tag name of the node.
	 */
	protected String tagName;
	
	/**
	 * Determines if the current node is an attribute node.
	 */
	protected boolean isAttribute;

	/**
	 * Creates an Markup node that is illegal until
	 * the tagname is set with the setTagname method.
	 */	
	public MarkupNode() {
		childList = new ArrayList();
		isAttribute = false;
	}

	/**
	 * Constructor. isAttribute is false.
	 * @param tagName the tagName of the new markup node.
	 */
	public MarkupNode(String tagName) {
		this(tagName, false);
	}

	/**
	 * Creates a new MarkupNode Object.
	 * @param tagName the tagname of this new node.
	 * @param isAttribute indicates whether or not this new markup node should be treated as an attribute.
	 */
	public MarkupNode(String tagName, boolean isAttribute) {
		this.childList = new ArrayList();
		this.tagName = tagName;
		this.isAttribute = isAttribute;
		
	}

	/**
	 * Clones the MarkupNode and its childs using an ArrayList for the child nodes.
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		MarkupNode node = (MarkupNode) super.clone();
		// node.tagName must not be set, because the String is not mutable!
		node.childList = new ArrayList();
		Iterator it = childList.iterator();
		while (it.hasNext()) {
			Node newChildNode = (Node) ((Node) it.next()).clone();
			newChildNode.internalParentId = node.internalId;
			node.childList.add(newChildNode);
		}
		return node;
	}

	/**
	 * Returns true, if this Markup-Node is an Attribute, false otherwise.
	 * @return true is this is a attribute, false otherwise.
	 */
	public boolean isAttribute() {
		return isAttribute;	
	}

	/**
	 * Returns Node.MARKUP_NODE, because this is a MarkupNode.
	 * Returns Node.MARKUP_NODE.
	 */
	public int getType() {
		return Node.MARKUP_NODE;
	}

	/**
	 * Returns the first child-node.
	 * @return the first child-node.
	 */
	public Node getFirstChild() {
		try {
			return ((Node) childList.get(0));
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns an iterator over the child nodes of this node.
	 * @return an iterator over the child nodes of this node.
	 */	
	public Iterator getChildNodes() {
		return childList.iterator();
	}
	
	/**
	 * Return the list which contains the child nodes.
	 */
	public void setChildList(List children) {
		childList = children;	
	}

	/**
	 * Return the list of child nodes.
	 */
	public List getChildList() {
		return childList;	
	}
	
	/**
	 * Return the tagname of this markup-node.
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Sets the tagName of this MarkupNode
	 * @param tagName the new tagname of this markup-node
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * Adds the Node to the child nodes of this node.
	 * @param child the new child-node
	 */
	public void addChildNode(Node child) {
		childList.add(child);
	}
	
	/**
	 * Outputs the content of the node.
	 * @return the String representation of the Node.
	 */
	public String toString() {
		if (isAttribute())
			return "Attribute node: "+tagName;
		else
			return "Element node: "+tagName;
	}

	/**
	 * Compares two subtrees and returns true iff they are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			MarkupNode node = (MarkupNode) obj;
			if (isAttribute!=node.isAttribute)
				return false;
			if (!tagName.equals(node.tagName))
				return false;
			
			if (childList==null)
				return node.childList==null;
			else {
				if (node.childList==null)
					return false;
				Iterator it1 = childList.iterator();
				Iterator it2 = node.childList.iterator();
				while (it1.hasNext() && it2.hasNext()) {
					if (!it1.next().equals(it2.next()))
						return false;
				}
				return it1.hasNext()==false && it2.hasNext()==false;
			}
		}
		else
			return false;
	}

	/**
	 * Returns a hash code for this Object.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() ^ tagName.hashCode();
	}
}
