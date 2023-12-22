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

package xxl.core.xml.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import xxl.core.io.converters.FixedSizeConverter;
import xxl.core.io.converters.SizeConverter;

/**
 * Converter which converts a whole subtree (usually to store it inside a Record).
 */
public class SubtreeConverter extends SizeConverter {

	/**
	 * Converter for an arbitrary Node.
	 */
	NodeConverter nodeConverter;

	/**
	 * Converter for the identifyer.
	 */
	FixedSizeConverter idConverter;
	
	/**
	 * Constructs a converter for a whole subtree.
	 * @param nodeConverter Converter which is used for a single node.
	 * @param idConverter Converter which is used to convert the identifyer.
	 */
	public SubtreeConverter(NodeConverter nodeConverter, FixedSizeConverter idConverter) {
		this.nodeConverter = nodeConverter;
		this.idConverter = idConverter;
	}

	/**
	 * Reads the whole subtree and returns the root node.
	 * @param dataInput the dataInput with the serialized tree
	 * @return the root-node
	 * @see xxl.core.io.converters.Converter#read(java.io.DataInput, java.lang.Object)
	 */
	public Object read(DataInput dataInput, Object object) throws IOException {
		Node rootNode = null;

		Stack stack = new Stack();
		Node currentNode;
		Node parentNode;
		
		Object parentId = null;
		
		if (dataInput.readByte()==1)
			parentId = idConverter.read(dataInput);
		
		while (true) {
			// restore the nodes and set parent node
			try {
				currentNode = (Node) nodeConverter.read(dataInput,null);
			}
			catch (EOFException eof) {
				// end of the dataInput, no node Object has been created
				rootNode.setParentId(parentId);
				return rootNode;
			}
			if (rootNode==null)
				rootNode = currentNode;
			else {
				//search the parent node of this childNode
				while (true) {
					Node stackNode = (Node) stack.peek();
					if (stackNode.internalId == currentNode.internalParentId) {
						parentNode = stackNode;
						break;
					}
					else
						stack.pop();
				}
				
				if (parentNode==null)
					throw new RuntimeException("Father node was not found during deserialization of subtree");
				
				parentNode.addChildNode(currentNode);
			}
			
			// put it into the map if there might be a child node.
			if (currentNode.getType()!=Node.LITERAL_NODE)
				stack.push(currentNode);
		}
	}

	/** 
	 * Stores the next id which can be given.
	 */
	private static short nextInternalId;

	/**
	 * Serializes the subtree and renews the internal ids which are
	 * necessary for the reconstruction process of the tree.
	 * The nodes are stored in pre-order (this is very important for
	 * the read operation).
	 * @param dataOutput the outputStream where to store the serialized tree
	 * @param node the root node of the tree
	 * @param internalFatherId internal identifyer of the father node used for
	 * 		reconstruction of the tree.
	 * @throws IOException if an IO Exception occurs
	 * @see xxl.core.io.converters.Converter#write(java.io.DataOutput, java.lang.Object)
	 */
	public void write(DataOutput dataOutput, Node node, short internalFatherId)
		throws IOException {

		short myId = nextInternalId++;
		node.internalId = myId;
		node.internalParentId = internalFatherId;
		nodeConverter.write(dataOutput, node);
		Iterator it = node.getChildNodes();
		while (it.hasNext()) 
			write(dataOutput, (Node) it.next(), myId);
	}

	/**
	 * Serializes the subtree to the given dataOutput stream.
	 * This means not the complete tree, its just a subtree, which ends at the
	 * proxy nodes. This subtree usually ist a subttree that will be stored in one record.
	 * @param dataOutput the outputStream where to store the serialized tree
	 * @param object the root node of the tree
	 * @throws IOException if an IO Exception occurs
	 * @see xxl.core.io.converters.Converter#write(java.io.DataOutput, java.lang.Object)
	 */
	public void write(DataOutput dataOutput, Object object)
		throws IOException {
		nextInternalId = 0;
		Node node = (Node) object;

		dataOutput.writeByte(node.parentId==null?0:1);
		if (node.parentId!=null)
			idConverter.write(dataOutput,node.parentId);
		
		write(dataOutput, node, (short)-1);
	}

	/**
	 * Calculates and return the size of the subtree with the given root-node
	 * disregarding the size of the parentId of the node itself.
	 * Note, that this doesn't mean the complete tree. Just the subtree, that ends at
	 * the leaf nodes or at the proxy nodes.
	 * @param o the root node of the tree
	 * @return the size in bytes.
	 */
	public int getSerializedSizeWithoutParentId(Object o) {
		Node rootNode = (Node) o;
		if (rootNode!=null) {
			int sum = nodeConverter.getSerializedSize(rootNode);
			Iterator it = rootNode.getChildNodes();
			while (it.hasNext())
				sum += getSerializedSizeWithoutParentId(it.next());
			return sum;
		}
		else
			throw new RuntimeException("Cannot serialize null-subtrees");
	}

	/**
	 * Calculates and return the size of the subtree with the given root-node.
	 * Note, that this doesn't mean the complete tree. Just the subtree, that ends at
	 * the leafnodes or at the proxy nodes.
	 * @param o the root node of the tree
	 * @return the size in bytes.
	 */
	public int getSerializedSize(Object o) {
		Node rootNode = (Node) o;
		if (rootNode!=null)
			return 
				1 + (rootNode.parentId==null?0:idConverter.getSerializedSize()) +
				getSerializedSizeWithoutParentId(o);
		else
			throw new RuntimeException("Cannot serialize null-subtrees");
	}

	/**
	 * Return the converter for a single node of arbitrary type used inside.
	 * @return NodeConverter
	 */
	public NodeConverter getNodeConverter() {
		return nodeConverter;
	}
}
