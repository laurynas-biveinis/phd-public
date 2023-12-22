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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.io.converters.FixedSizeConverter;
import xxl.core.io.converters.SizeConverter;

/**
 * This class is an extention of the abstract class Node. It is used to distribute 
 * large trees over different records.
 *
 * An proxy node can have child nodes, and can standalone.
 */
public class ProxyNode extends Node {

	/**
	 * Constructs a converter for ProxyNodes.
	 */
	public static SizeConverter getConverter(final FixedSizeConverter idConverter) {
		return new SizeConverter() {
			/**
			 * Serializes the ProxyNode to the dataOutput Stream.
			 * type, internalId, internalParentId
			 * standaloneFlag, case standalone: write parent tid, else writes no parent tid
			 * then writes 0 or 1 if this node has a link to the childTID
			 * @param dataOutput the output where to write the node
			 */
			public void write(DataOutput dataOutput, Object o) throws IOException {
				ProxyNode n = (ProxyNode) o;
				dataOutput.writeShort(n.internalId); //the internal ID
				dataOutput.writeShort(n.internalParentId); //the internal parent ID
				dataOutput.writeByte(n.childId==null?0:1);
				if (n.childId!=null)
					idConverter.write(dataOutput,n.childId); //and now the tid of the child
			}
			public Object read(DataInput dataInput, Object o) throws IOException {
				ProxyNode n;
				if (o==null)
					n = new ProxyNode();
				else
					n = (ProxyNode) o;

				n.internalId = dataInput.readShort();
				n.internalParentId = dataInput.readShort();
				if (dataInput.readByte()==1)
					n.childId = idConverter.read(dataInput, null);
				
				return n;
			}
			/**
			 * Calculates and returns the size of this Node.
			 * Including the size of the required header-informations (i.e. size, ...).
			 * That is the size the serialized Object would require.
			 * @return the size of this Node in bytes.
			 */
			public int getSerializedSize(Object o) {
				ProxyNode n = (ProxyNode) o;
				// extra Bytes: type, internalId, internal Parent Id, hasChild and if 
				// necessary the identifier (10 Bytes) for the child
				return 5+(n.childId!=null?idConverter.getSerializedSize():0);
			}
		};
	}

	/**
	 * Identifyer which is used to find the subtree below.
	 */
	protected Object childId;

	/**
	 * Constructs a new proxy node.
	 */
	public ProxyNode() {
	}

	/**
	 * Clones the ProxyNode.
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone(); 
		// Nothing to do. The childId is not clonable (without reflection)! But
		// identifyer are normally not updatable, so it should not be a problem.
	}

	/**
	 * Returns Node.PROXY_NODE, because this is a ScaffoldNode.
	 * @return Node.PROXY_NODE
	 */
	public int getType() {
		return Node.PROXY_NODE;
	}

	/**
	 * Returns the id of the child record, null if no id exists.
	 * @return the id of the child record, null if no id exists.
	 */
	public Object getChildId() {
		return childId;
	}

	/**
	 * Sets the id of the child record, null if no id exists.
	 * @param childTId the id of the child record, null if no id exists.
	 */
	public void setChildId(Object childId) {
		this.childId = childId;
	}

	/**
	 * Returns the first child-node.
	 * @return the first child-node.
	 */	
	public Node getFirstChild() {
		return null;
	}

	/**
	 * Returns an iterator over the child nodes of this node.
	 * @return an iterator over the child nodes of this node.
	 */	
	public Iterator getChildNodes() {
		return new EmptyCursor();
	}
	
	/**
	 * Return the list which contains the child nodes.
	 */
	public void setChildList(List children) {
		throw new RuntimeException("an proxy node isn't able to store child-nodes");
	}	

	/**
	 * Return an empty list, because a ProxyNode isn't able to contain childs.
	 * @return null.
	 */
	public List getChildList() {
		return null;
		// throw new RuntimeException("an proxy node isn't able to store child-nodes");
		// return new java.util.ArrayList();
	}

	/**
	 * Throws a NoSuchMethodException because proxy-nodes aren't able to store child-nodes. 
	 * @throws NoSuchMethodException always
	 */
	public void addChildNode(Node child) {
		throw new RuntimeException("an proxy node isn't able to store child-nodes");
	}

	/**
	 * Outputs the content of the node.
	 * @return the String representation of the Node.
	 */
	public String toString() {
		return "Proxy node pointing to id: "+childId.toString();
	}

	/**
	 * Compares two subtrees and returns true iff they are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			ProxyNode node = (ProxyNode) obj;
			
			if (childId==null)
				return childId==node.childId;
			else
				return childId.equals(node.childId);
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
		if (childId!=null)
			ret = ret ^ childId.hashCode();
		return ret;
	}
}
