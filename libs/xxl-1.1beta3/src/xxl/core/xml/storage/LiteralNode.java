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
import xxl.core.io.converters.SizeConverter;
import xxl.core.util.Arrays;

/**
 * This class is an extention of the abstract class Node. It has the role
 * of an literal (simple String) as we know from XML documents.
 *
 * An literal node cannot have child nodes, but can standalone.
 */
public class LiteralNode extends Node {

	/**
	 * Constructs a converter for LiteralNodes.
	 */
	public static SizeConverter getConverter() {
		return new SizeConverter() {
			/**
			 * Writes this node into the dataOutput.<br>
			 * The first byte == Node.LITERALNODE == 2<br>
			 * The second byte = internal Id<br>
			 * The third byte == internal parent Id<br>
			 * The fourth byte == type of the content<br>
			 * The fifth byte == size of the content<br>
			 * Then the content.
			 * @throws IOException IOException if an io-error occurs.
			 */
			public void write(DataOutput dataOutput, Object o) throws IOException {
				LiteralNode n = (LiteralNode) o;
				
				dataOutput.writeShort(n.internalId); //the internal ID
				dataOutput.writeShort(n.internalParentId); //the internal parent ID
				if (n.content==null)
					throw new RuntimeException ("content is null, cannot write");
				dataOutput.writeByte(n.lnType);				//the type
				dataOutput.writeShort((int) n.content.length);	//the length of the content
				dataOutput.write(n.content);			//the content
			}
			/**
			 * Reconstructs the node from the DataInput.<br>
			 * The first byte == internal id<br>
			 * The second byte == internal parent id<br>
			 * standalone flag and then if true the parent TID<br>
			 * The third byte == the type of the content<br>
			 * The fourth byte == the size of the content<br>
			 * And the content.
			 * @throws IOException if an io-error occurs.
			 */
			public Object read(DataInput dataInput, Object o) throws IOException {
				LiteralNode n;
				if (o==null)
					n = new LiteralNode();
				else
					n = (LiteralNode) o;
				
				n.internalId = dataInput.readShort();
				n.internalParentId = dataInput.readShort();
				n.lnType = dataInput.readByte();
				//read the lengt-informations
				n.content = new byte[(int) dataInput.readUnsignedShort()];
				//read the two strings ....
				dataInput.readFully(n.content);
				
				return n;
			}
			/**
			 * Return the size (in bytes) of this literal-node.
			 * Including one byte that indicates that this node is a
			 * literal-node.
			 */
			public int getSerializedSize(Object o) {
				LiteralNode n = (LiteralNode) o;
				// extra bytes: length of content string, type of node (1byte), 
				// internalId (1Byte), internalParentId
				return 7+n.content.length; 
			}
		};
	}

	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int UNDEFINED = 0;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int STRING = 1;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int URI = 2;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int BYTE = 3;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int SHORT = 4;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int INTEGER = 5;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int LONG = 6;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int FLOAT = 7;
	/**
	 * Constant determining the type of content of the node.
	 */
	public static final int DOUBLE = 8;

	/**
	 * Content of the node.
	 */
	protected byte[] content;

	/**
	 * Type of the node.
	 */
	protected int lnType=STRING;

	/**
	 * Constructs a new LiteralNode. Use read(dataInput) to fill the attributes.
	 */
	public LiteralNode() {
	}

	/**
	 * Constructs a new LiteralNode with the given content.
	 */
	public LiteralNode(byte[] content, int lnType) {
		this.lnType = lnType;
		this.content = content;
	}

	/**
	 * Constructs a new LiteralNode with the given String content.
	 */
	public LiteralNode(String contentString) {
		this.lnType = STRING;
		this.content = contentString.getBytes();
	}

	/**
	 * Clones the LiteralNode.
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		LiteralNode node = (LiteralNode) super.clone();
		byte[] contentOrig = node.content;
		node.content = new byte[contentOrig.length];
		System.arraycopy(contentOrig,0,node.content,0,contentOrig.length);
		return node;
	}

	/**
	 * Returns the value of this literal-node according to
	 * the type. If the type is i.e. INTEGER_8_BIT then
	 * the return value will be a Byte. if type=FLOAT then the
	 * return value will be a Float.......
	 * @return the value of this node converted into a java-Object
	 * according to the type of this node.
	 */
	public Object getObject() {
		switch(lnType) {
			case STRING:	return new String(content);
			case URI:		return new String(content);
			case BYTE:		return new Byte(content[0]);
			case SHORT:	return new Short(new String(content));
			case INTEGER:	return new Integer(new String(content));
			case LONG:		return new Long(new String(content));
			default:		return new String(content);
		}
	}

	/**
	 * Returns Node.LITERAL_NODE, because this is a MarkupNode.
	 * Returns Node.LITERAL_NODE.
	 */
	public int getType() {
		return Node.LITERAL_NODE;
	}

	/**
	 * Returns the value of this Node in a byte-Array.
	 * @return the value of this Node in a byte-Array.
	 */
	public byte[] getBytes() {
		return content;
	}

	/**
	 * Returns the byte array of the content.
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * Sets the byte array of the content.
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

	/**
	 * Returns null
	 * @return null
	 */
	public Node getFirstChild() {
		return null;
	}

	/**
	 * Returns an empty iterator.
	 * @return an empty iterator.
	 */
	public Iterator getChildNodes() {
		return new EmptyCursor();
	}

	/**
	 * Return the list which contains the child nodes.
	 */
	public void setChildList(List children) {
		throw new RuntimeException("an literal node isn't able to store child-nodes");
	}

	/**
	 * Return an empty list, because an Literal-Node isn't able to contain childs.
	 * @return null.
	 */
	public List getChildList() {
		return null;
		// throw new RuntimeException("an literal node isn't able to store child-nodes");
		// return new java.util.ArrayList();
	}

	/**
	 * Throws a NoSuchMethodException because literal-nodes aren't able to store child-nodes.
	 * @throws NoSuchMethodException always
	 */
	public void addChildNode(Node child) {
		throw new RuntimeException("an literal node isn't able to store child-nodes");
	}

	/**
	 * Outputs the content of the node.
	 * @return the String representation of the Node.
	 */
	public String toString() {
		return getObject().toString();
	}

	/**
	 * Compares two subtrees and returns true iff they are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			LiteralNode node = (LiteralNode) obj;
			if (lnType!=node.lnType)
				return false;
			if (content==null)
				return node.content==null;
			else {
				if (content.length!=node.content.length)
					return false;
				for (int i=0; i<content.length; i++)
					if (content[i]!=node.content[i])
						return false;
				return true;
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
		return super.hashCode() ^ Arrays.getHashCodeForByteArray(content, 0, content.length);
	}
}
