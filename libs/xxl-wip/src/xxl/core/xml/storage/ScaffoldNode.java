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

import xxl.core.io.converters.FixedSizeConverter;

/**
 * A Scaffoldnode is used to group child-nodes from a markup node. In the case of a split,
 * the child-nodes will be put into another record. The parent-node gets a proxy node that points
 * to this new record and in this new record a scaffold node will be used to combine these child-nodes
 * under one node, because a record must contain none or one subtree. And the scaffold ist used to
 * keep a tree structure.
 */
public class ScaffoldNode extends Node {

	/**
	 * Constructs a converter for ScaffoldNodes.
	 */
	public static FixedSizeConverter getConverter() {
		return new FixedSizeConverter(4) {
			/**
			 * Serializes this Node to the DataOutput-Stream. Note, that this method writes 
			 * the type (one byte) at first, then the other attributes.
			 * @param dataOutput the DataOutput-Stream
			 */
			public void write(DataOutput dataOutput, Object o) throws IOException {
				ScaffoldNode n = (ScaffoldNode) o;
				dataOutput.writeShort(n.internalId);
				dataOutput.writeShort(n.internalParentId); //the internal parent ID
			}
			/**
			 * Reconstructs the attributes from the DataInput-Stream. Note, that this method
			 * doesnt expect the type information at the beginning of the stream, although the
			 * write methods puts it in the DataOutput!!
			 * @param dataInput the DataInput-Stream that contains the serialized informations.
			 */
			public Object read(DataInput dataInput, Object o) throws IOException {
				ScaffoldNode n;
				if (o==null)
					n = new ScaffoldNode();
				else
					n = (ScaffoldNode) o;

				n.internalId = dataInput.readShort();
				n.internalParentId = dataInput.readShort();
				
				return n;
			}
		};
	}

	/**
	 * List of child nodes.
	 */
	protected List childList;

	/**
	 * Constructs a new scaffold node.
	 */
	public ScaffoldNode() {
		childList = new ArrayList();
	}

	/**
	 * Clones the ScaffoldNode and its childs using an ArrayList for the child nodes.
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		ScaffoldNode node = (ScaffoldNode) super.clone();
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
	 * Returns Node.SCAFFOLD_NODE, because this is a ScaffoldNode.
	 * @return Node.SCAFFOLD_NODE
	 */
	public int getType() {
		return Node.SCAFFOLD_NODE;
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
	 * Adds the Node to the child nodes of this node.
	 * @param child the new child-node
	 */
	public void addChildNode(Node child) {
		this.childList.add(child);
	}

	/**
	 * Removes the last child node.
	 */
	public void removeLastChildNode() {
		childList.remove(childList.size()-1);
	}

	/**
	 * Outputs the content of the node.
	 * @return the String representation of the Node.
	 */
	public String toString() {
		return "Scaffold node with "+childList.size()+" child nodes.";
	}

	/**
	 * Compares two subtrees and returns true iff they are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			ScaffoldNode node = (ScaffoldNode) obj;

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
		int ret = super.hashCode();
		if (childList!=null)
			ret = ret ^ childList.size();
		return ret;
	}
}
