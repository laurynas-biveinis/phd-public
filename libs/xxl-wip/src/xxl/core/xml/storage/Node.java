/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import xxl.core.functions.Function;
import xxl.core.io.Block;
import xxl.core.io.converters.FixedSizeConverter;
import xxl.core.io.converters.SizeConverter;
import xxl.core.xml.util.Utils;

/**
 * The super class of all nodes of the EXTree.
 */
public abstract class Node implements Cloneable {

	/**
	 * Type constant for a MarkupNode. 
	 */
	public static final byte MARKUP_NODE = 1;
	/**
	 * Type constant for a LiteralNode. 
	 */
	public static final byte LITERAL_NODE = 2;
	/**
	 * Type constant for a ScaffoldNode. 
	 */
	public static final byte SCAFFOLD_NODE = 3;
	/**
	 * Type constant for a ProxyNode. 
	 */
	public static final byte PROXY_NODE = 4;

	/**
	 * Chooses the node-constructor by the given type and
	 * returns a empty node with this type.
	 * If i.e. type==Node.MARKUP_NODE, a MarkupNode will be
	 * returned.
	 */
	public static Node getNodeByType(byte type) {
		switch (type) {
		case Node.MARKUP_NODE:
			return new MarkupNode();
		case Node.LITERAL_NODE:
			return new LiteralNode();
		case Node.SCAFFOLD_NODE:
			return new ScaffoldNode();
		case Node.PROXY_NODE:
			return new ProxyNode();
		default:
			throw new RuntimeException("Illegal Node Type "+type);
		}
	}

	/**
	 * Returns a subtree converter for a given id converter. 
	 * @param idConverter converter which converts the ids inside the subtree.
	 * @return SizeConverter converter for the subtree.
	 */
	public static SubtreeConverter getSubtreeConverter(FixedSizeConverter idConverter) {
		return new SubtreeConverter(
			new NodeConverter(
				new SizeConverter[] {
					MarkupNode.getConverter(),
					LiteralNode.getConverter(),
					ScaffoldNode.getConverter(),
					ProxyNode.getConverter(idConverter)
				}
			),
			idConverter
		);
	}

	/**
	 * Returns a record that contains the whole subtree determined by the
	 * given root-node.
	 * @param root the root-node of the subtree that should be serialized into the record
	 * @return a record that contains the whole subtree where this is the root-node.
	 */
	public static Block subtreeToRecord(short maxInternalId, Node root, SizeConverter subtreeConverter) {
		try {
			int size = subtreeConverter.getSerializedSize(root);
			byte[] bytes = new byte[size+2];
			Block record = new Block(bytes);
			DataOutputStream outputStream = record.dataOutputStream();
			outputStream.writeShort((short) maxInternalId);
			subtreeConverter.write(outputStream,root);
			return record;
		}
		catch (IOException e) {
			throw new RuntimeException("Serialization to Record failed");
		}
	}

	/**
	 * The parent identifyer inside the container.
	 */
	protected Object parentId;
	/**
	 * Internal ids used by the SubtreeConverter for storage purposes.
	 */
	protected short internalId;
	/**
	 * Internal ids used by the SubtreeConverter for storage purposes.
	 */
	protected short internalParentId;

	/**
	 * Replaces the proxy that points to the record with oldId by the given node.
	 * Return true if the replacement was usccesfull, false otherwise.
	 * Under the assumption that there can exist at most one proxy node which
	 * points to the record with the old id, the algorithm can stop when one
	 * proxy has been overwritten by the node.
	 * @param oldId Id which is searched
	 * @param node Replaces the ProxyNode with oldId.
	 */
	protected boolean replaceProxyByNode(Object oldId, Node node) {

		Iterator it = getChildNodes();
		int counter=0;

		while (it.hasNext()) {
			Node child = (Node) it.next();

			if ((child.getType()==Node.PROXY_NODE)) {
				if (((ProxyNode)child).getChildId().equals(oldId)) {
					List list = getChildList();
					list.set(counter, node);
					setChildList(list);
					return true;
				}
			}
			else
				if (child.replaceProxyByNode(oldId, node)) 
					return true;

			counter++;
		}
		return false;
	}

	/**
	 * Return true if the tree (just the subtree in one record) given by root contains a proxy that leads to
	 * the record with the identifyer id. Else otherwise.
	 * @param id the identifyer
	 * @return true if such a ProxyNode is found.
	 */
	protected boolean containsProxyId(Object id) {

		if ((getType()==Node.PROXY_NODE))
			if (((ProxyNode)this).getChildId().equals(id))
				return true;

		Iterator it = getChildNodes();

		while (it.hasNext()) {
			Node child = (Node) it.next();
			if (child.containsProxyId(id)) 
				return true;
		}
		return false;
	}

	/**
	 * Removes the Proxy in the subtree beginning by root.
	 * Under the assumption that there can exist at most one proxy node which
	 * points to the record with the old id, the algorithm can stop when one
	 * proxy has been overwritten by the node.
	 * @return true if the removing was successfull, false otherwise
	 */
	protected boolean removeProxy(Object oldId) {

		Iterator it = getChildNodes();
		int counter=0;

		while (it.hasNext()) {
			Node child = (Node) it.next();

			if ((child.getType()==Node.PROXY_NODE)) {
				if (((ProxyNode)child).getChildId().equals(oldId)) {
					List list = getChildList();
					list.remove(counter);
					setChildList(list);
					return true;
				}
			}
			else
				if (child.removeProxy(oldId)) 
					return true;
			counter++;
		}

		return false;
	}

	/**
	 * Return the number of child nodes. Calls getChildList().size().
	 * @return the number of child nodes.
	 */
	public int getNumberOfChildren() {
		return getChildList().size();
	}

	/**
	 * Prints the subtree as XML to the PrintStream. This method can write 
	 * the complete EXTree tree, beginning at root, to the PrintStream.
	 * 
	 * @param id identifyer for this subtree (if you don't want one, use the empty String "")
	 * @param out PrintStream
	 * @param outputProxies Outputting the proxy and scaffolding nodes?
	 * @param getProxySubtrees Function which returns subtrees if the id is given.
	 * 		If the function is null, then only the current subtree is transformed into XML.
	 */
	public void toXML(Object id, PrintStream out, boolean outputProxies, Function getProxySubtrees) {
		Iterator it;
		switch (getType()) {
		case Node.LITERAL_NODE:
			if (outputProxies)
				out.print("<LITERAL internalId=\""+internalId+"\" id=\""+id+"\" internalParentId=\""+
					internalParentId+"\" parentId=\""+getParentId()+"\">");
			Utils.writeCharactersXMLConform(((LiteralNode) this).getBytes(), out);
			if (outputProxies)
				out.print("</LITERAL>");
			return;
		case Node.MARKUP_NODE:
			String tagName = ((MarkupNode) this).getTagName();
			out.print("<"+tagName);

			if (outputProxies)
				out.print(" internalId=\""+internalId+"\" id=\""+id+"\" internalParentId=\""+
					internalParentId+"\" parentId=\""+getParentId()+"\"");

			Node node;

			// Traverse attribute nodes
			it = getChildNodes();
			while (it.hasNext()) {
				node= (Node) it.next();
				if (node.getType()==Node.MARKUP_NODE) {
					MarkupNode mn = (MarkupNode) node;
					if (mn.isAttribute()) {
						out.print(" ");
						out.print(mn.getTagName());
						out.print("=\"");
						Utils.writeCharactersXMLConform(((LiteralNode) getFirstChild()).getBytes(),out);
						out.print("\"");
					}
				}
			}

			out.print(">");
			
			// Traverse non attribute nodes
			it = getChildNodes();
			// Traverse attribute nodes
			while (it.hasNext()) {
				node= (Node) it.next();
				node.toXML(id, out, outputProxies, getProxySubtrees);
			}

			out.print("</"+tagName+">");
			return;
		case Node.PROXY_NODE:
			Object nextRecordId = ((ProxyNode)this).getChildId();
			if (outputProxies)
				out.print("<PROXY internalId=\""+internalId+"\" fromId=\""+id+"\" toId=\""+nextRecordId+"\" parentId=\""+getParentId()+"\">");
			if (getProxySubtrees!=null) {
				Node n = (Node) getProxySubtrees.invoke(nextRecordId);
				if (!id.equals(n.getParentId()))
				 	throw new RuntimeException("Tree is invalid, Next entry: type="+n.getType()+", parentId="+n.getParentId()+", toString: "+n);
				n.toXML(nextRecordId, out, outputProxies, getProxySubtrees);
			}
			if (outputProxies)
				out.print("</PROXY>");
			return;
		case Node.SCAFFOLD_NODE:
			if (outputProxies)
				out.print("<SCAFFOLD internalId=\""+internalId+"\" id=\""+id+"\" parentId=\""+getParentId()+"\">");
			it = getChildNodes();
			while (it.hasNext())
				((Node) it.next()).toXML(id, out, outputProxies, getProxySubtrees);
			if (outputProxies)
				out.print("</SCAFFOLD>");
			return;
		}
	}

	/**
	 * Writes just the subtree to the PrintStream.
	 * That is usefull to analyze i.e. the separator tree in the case of a split.
	 * @param id the id of the subtree, that contains the given node.
	 * @param out the PrintStream
	 */
	public void justSubtreeToXML(Object id, PrintStream out, boolean outputProxies) {
		out.println("<?xml version=\"1.0\"?>");
		toXML(id, out, outputProxies, null);
	}

	/**
	 * Writes just the subtree to the OutputStream.
	 * That is usefull to analyze i.e. the separator tree in the case of a split.
	 * @param id the id of the subtree, that contains the given node.
	 * @param out the OutputStream
	 */
	public void justSubtreeToXML(Object id, OutputStream out) {
		justSubtreeToXML(id, new DataOutputStream(out));
	}

	/**
	 * Returns the id of the parent record, if it exists.
	 * @return the id of the parent record, if it exists.
	 * @throws NoSuchElementException if no parent record exists.
	 */
	final public Object getParentId() {
		return parentId;
	}

	/**
	 * Sets the id of the parent record.
	 * @param tid the id of the parent record
	 */
	final public void setParentId(Object id) {
		parentId = id;
	}

	/**
	 * Checks whether this Node is a standalone-Object.
	 * @return true if this Node is a standalone Object, false otherwise.
	 */
	public boolean isStandalone() {
		return getParentId()!=null;
	}

	/**
	 * Simple get method for the internal-parent-id.
	 */
	final public short getInternalParentId() {
		return internalParentId;
	}

	/**
	 * Simple set method for the internal-parent-id.
	 */
	final public void setInternalParentId(short pid) {
		internalParentId = pid;
	}

	/**
	 * Returns the internal id from this node.
	 * @return the internal id from this node.
	 */
	final public short getInternalId() {
		return internalId;
	}

	/**
	 * Returns the internal id from this node.
	 * @param the internal id from this node.
	 */
	final public void setInternalId(short internalId) {
		this.internalId = internalId;
	}

	/**
	 * Returns the type of the node (a constant declared above).
	 * @return the type of the node.
	 */
	public abstract int getType();

	/**
	 * Returns the first child-node.
	 * @return the first child-node.
	 */
	public abstract Node getFirstChild();

	/**
	 * Returns an iterator over the child nodes of this node.
	 * @return an iterator over the child nodes of this node.
	 */
	public abstract Iterator getChildNodes();

	/**
	 * Adds a node to this Node.
	 * @param child the new Child Node
	 * @throws NoSuchMethodException if the node doesn't support child-nodes
	 */
	public abstract void addChildNode(Node child);

	public abstract void setChildList(List children);

	public abstract List getChildList();

	/**
	 * Makes a deep copy of the Node (subtree). The internalParentId is set to -1
	 * (must be set by the father node).
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		Node node = (Node) super.clone();
		node.internalParentId = -1;
		return node;
	}

	/**
	 * Compares two subtrees and returns true iff they are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		Node node = (Node) obj;

		if (node==null)
			return false;
		
		if (getType()!=node.getType())
			return false;
		
		if (parentId==null) {
			if (node.parentId!=null)
				return false;
		}
		else 
			if (!parentId.equals(node.parentId))
				return false;

		return true;
	}

	/**
	 * Returns a hash code for this Object.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (parentId==null)
			return 465345313;
		else
			return parentId.hashCode();
	}

	/**
	 * Returns true if the reference of Node n is part of the 
	 * tree starting with root.
	 */
	protected boolean containsNode(Node root, Node n) {
		
		if (root.equals(n))
			return true;
		else {
			boolean found = false;
			Iterator it = root.getChildNodes();
			while (it.hasNext() && !found)
				found = containsNode((Node) it.next(), n);
			return found;
		}
	}
}
