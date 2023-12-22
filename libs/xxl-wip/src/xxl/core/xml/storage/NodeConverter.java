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

import xxl.core.io.converters.SizeConverter;

/**
 * Converter for a single node of arbitrary node type.
 */
public class NodeConverter extends SizeConverter {

	/** Converters for each kind of node. */
	private SizeConverter converters[];
	
	/**
	 * Constructs a converter for all type of Nodes.
	 */
	public NodeConverter(SizeConverter converters[]) {
		this.converters = converters;
	}

	/**
	 * Reads one node and returns it.
	 * @param dataInput the dataInput with the serialized tree
	 * @return the node
	 * @see xxl.core.io.converters.Converter#read(java.io.DataInput, java.lang.Object)
	 */
	public Object read(DataInput dataInput, Object object) throws IOException {
		Node n;
		if (object==null)
			n = null;
		else
			n = (Node) object;

		// restore the node
		int type = dataInput.readByte();
		return (Node) converters[type-1].read(dataInput,n);
	}

	/**
	 * Serializes the node (only) to the given dataOutput stream.
	 * This means not the complete subtree, just a node.
	 * @param node the node to be serialized.
	 * @param dataOutput the outputStream where to store the node.
	 * @throws IOException thrown if an IOException occurs
	 * @see xxl.core.io.converters.Converter#write(java.io.DataOutput, java.lang.Object)
	 */
	public void write(DataOutput dataOutput, Object object)
		throws IOException {
		Node node = (Node) object;
		if (node!=null) {
			int type = node.getType();
			dataOutput.writeByte(type);
			converters[type-1].write(dataOutput, node);
		}
	}

	/**
	 * Calculates and return the size of the subtree with the given root-node.
	 * Note, that this doesn't mean the complete tree. Just the subtree, that ends at
	 * the leafs or at the proxy nodes.
	 * @param o the root node of the tree
	 */
	public int getSerializedSize(Object o) {
		Node node = (Node) o;
		if (node!=null) {
			int type = node.getType();
			return 1+converters[type-1].getSerializedSize(node);
		}
		else
			throw new RuntimeException("Cannot serialize a null node");
	}
}

