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

package xxl.core.relational.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.functions.Switch;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataProvider;

/**
 * This class provides the implementation of a node inside a query graph. This
 * node provides metadata information describing itself and is able to store an
 * possibly unlimited number of child and parent nodes. Child and parent nodes
 * can be accessed and modified directly via the methods of this node.
 * 
 * <p>The metadata information describing the node can be separated into two
 * classes of information:
 * <ol>
 *   <li>there is static information that is simply inserted into the node's
 *   global metadata during its initialisation time.</li>
 *   <li>there is information that must be calculated from the node's static
 *   information during its lifetime. In this case, let us call it dependent
 *   information. To calculate the dependent information, an arbitrary number
 *   of {@link xxl.core.functions.Function factories} can be registered to the
 *   the global metadata factory, whereas every factory must return the
 *   calculated dependent information it represents.</li> 
 * </ol>
 * Actually there is a third class of metadata information called dynamic
 * information, i.e., information that is calculated at the time when it is
 * requested. This class of metadata information can be provided by inserting
 * {@link xxl.core.functions.Function factories} to the node's global metadata
 * that can be invoked whenever dynamic information is needed.</p>
 * 
 * <p>Whenever metadata information has changed by putting something in it, the
 * affected node must be notified by a call to its <code>updateMetaData</code>
 * method. This call will cause a recomputation of the node's dependent
 * information. Whensoever at least one piece of dependent information is
 * changed by this recomputation, all child and parent nodes of the affected
 * node are informed about it and urged to recompute their dependent
 * information.</p>
 * 
 * <p>Because of the fact that a node is described by its metadata information
 * and this information can nearly contain everything, a node must specify
 * which part of its metadata information is defining itself. For this reason
 * a node offers a signature. A signature is a map that maps identifiers of
 * metadata fragments to its class. The purpose of such a signature is to
 * collect the identifiers of the metadata fragments defining a node and
 * additionally store its class information without using reflection. This
 * information can be used for deciding in which way two metadata fragments can
 * be checked for equality, e.g., two nodes will be tested by checking their
 * signatures and the signatures of child and parent nodes returned by a
 * strategy stored in metadata information while strings will be tested in a
 * case insensitive way.</p>
 * 
 * @see Nodes
 */
public class Node implements MetaDataProvider<CompositeMetaData<Object, Object>> {
	
	/**
	 * A composite metadata containing this node's different metadata
	 * information.
	 */
	protected CompositeMetaData<Object, Object> globalMetaData;
	
	/**
	 * A factory that is used to update the global metadata of this node.
	 */
	protected Switch<Object, Object> globalMetaDataFactory;
	
	/**
	 * The signature of this node. It contains the identifiers of the metadata
	 * fragments defining the node and maps the information whether these
	 * metadata informations themselves are nodes again.
	 */
	protected Map<Object, Class<?>> signature;
	
	/**
	 * An array-list that stores this node's child nodes.
	 */
	protected ArrayList<Node> children;
	
	/**
	 * An array-list that stores this node's parent nodes.
	 */
	protected ArrayList<Node> parents;
	
	/**
	 * A boolean flag that determines whether this node has already been opened
	 * or not. The metadata of this node has reached its final state when the
	 * node is open, because it is produced during the open-phase.
	 */
	protected boolean open;
	
	/**
	 * A boolean flag that determines whether the <code>toString</code> method
	 * is running at this moment. This is necessary to prevent recursive calls
	 * of the <code>toString</code> method when a local metadata fragment of a
	 * child node points to this node.
	 */
	private boolean semaphore = false;
	
	/**
	 * Creates a new node providing the given global metadata using the
	 * spefified factory to update the node's global metadata.
	 * 
	 * @param globalMetaData the global metadata of the new node.
	 * @param globalMetaDataFactory a factory method that is used to update the
	 *        node's global metadata.
	 * @param signature the signature of the new node.
	 */
	public Node(CompositeMetaData<Object, Object> globalMetaData, Switch<Object, Object> globalMetaDataFactory, Map<Object, Class<?>> signature) {
		this.globalMetaData = globalMetaData;
		this.globalMetaDataFactory = globalMetaDataFactory;
		this.signature = signature;
		parents = new ArrayList<Node>();
		children = new ArrayList<Node>();
		open = false;
	}
	
	/**
	 * Returns the number of children that are actually stored by this node.
	 * 
	 * @return the number of children that are actually stored by this node.
	 */
	public int getActualNumberOfChildren() {
		return children.size();
	}
	
	/**
	 * Adds the given node to this node's children and this node to the given
	 * node's parents and returns the inex of the new child.
	 *  
	 * @param child the node which should be a new child of this node.
	 * @return the index of the new child.
	 * @throws IllegalStateException if the maximum number of this node's
	 *         children reached.
	 */
	public int addChild(Node child) throws IllegalStateException {
		if (Nodes.getNumberOfChildren(this) == getActualNumberOfChildren())
			throw new IllegalStateException("node has reached its maximum number of children (number of allowed children: " + Nodes.getNumberOfChildren(this) + ")");
		if (Nodes.getNumberOfParents(child) == child.getActualNumberOfParents())
			throw new IllegalStateException("new child has reached its maximum number of parents (number of allowed parents: " + Nodes.getNumberOfParents(child) + ")");
		children.add(child);
		child.parents.add(this);
		updateMetaData();
		return children.size() - 1;
	}
	
	/**
	 * Returns <code>true</code> if the given child is a child node of this
	 * node otherwise <code>false</code>.
	 * 
	 * @param child the node that should be tested for being a child of this
	 *        node.
	 * @return <code>true</code> if the given child is a child node of this
	 *         node otherwise <code>false</code>.
	 */
	public boolean containsChild(Node child) {
		return children.contains(child);
	}
	
	/**
	 * Returns the <code>index</code>'th child of this node.
	 * 
	 * @param index the index of the child node to be returned.
	 * @return the <code>index</code>'th child of this node.
	 */
	public Node getChild(int index) {
		return children.get(index);
	}
	
	/**
	 * Returns an iteration over all children of this node. The returned
	 * cursor only supports the <code>peek</code> and the <code>remove</code>
	 * method.
	 * 
	 * @return an iteration over this node's child nodes.
	 */
	public Cursor<Node> getChildren() {
		return new AbstractCursor<Node>() {
			protected int index = 0;
			
			@Override
			protected boolean hasNextObject() {
				return index < children.size();
			}
			
			@Override
			protected Node nextObject() {
				return children.get(index++);
			}
			
			@Override
			public void remove() throws IllegalStateException {
				super.remove();
				removeChild(index--);
			}
			
			@Override
			public boolean supportsRemove() {
				return true;
			}
		};
	}
	
	/**
	 * Removes the <code>index</code>'th child of this node and returns it.
	 * This node is also removed from the <code>index</code>'th node's parent
	 * nodes.
	 * 
	 * @param index the index of this node's child to be removed.
	 * @return the removed child node.
	 * @throws IllegalArgumentException if the given index is smaller than 0 or
	 *         greater than or equal to the maximum number of children.
	 * @throws IllegalStateException if the actual number of children is
	 *         smaller or equal to the given index.
	 */
	public Node removeChild(int index) throws IllegalArgumentException, IllegalStateException {
		if (index < 0 || Nodes.getNumberOfChildren(this) != Nodes.VARIABLE && index >= Nodes.getNumberOfChildren(this))
			throw new IllegalArgumentException("index " + index + " cannot be accessed (" + Nodes.getNumberOfChildren(this) + " children are allowed)");
		if (index >= getActualNumberOfChildren())
			throw new IllegalStateException("child at index " + index + " cannot be accessed because the given index is not assigned so far");
		Node oldChild = children.remove(index);
		oldChild.parents.remove(this);
		updateMetaData();
		return oldChild;
	}
	
	/**
	 * Removes the given child node from this node and returns if the method
	 * succeeds. This node is also removed from the given node's parent nodes.
	 * 
	 * @param oldChild the node to be removed from this node's children.
	 * @return <code>true</code> if this method succeeds, otherwise
	 *         <code>false</code>.
	 */
	public boolean removeChild(Node oldChild) {
		if (children.remove(oldChild)) {
			oldChild.parents.remove(this);
			updateMetaData();
			return true;
		}
		return false;
	}
	
	/**
	 * Replaces the <code>index</code>'th child of this node by the given node
	 * <code>newChild</code> and returns the prior child node. This node is
	 * also removed from the <code>index</code>'th node's parent nodes and
	 * added to the new child's parent nodes.
	 * 
	 * @param index the index of this node's child to be replaced.
	 * @param newChild the node to replace the <code>index</code>'th child of
	 *        this node.
	 * @return the replaced child node.
	 * @throws IllegalArgumentException if the given index is smaller than 0 or
	 *         greater than or equal to the maximum number of children.
	 * @throws IllegalStateException if the actual number of children is
	 *         smaller or equal to the given index.
	 */
	public Node replaceChild(int index, Node newChild) throws IllegalArgumentException, IllegalStateException {
		if (index < 0 || Nodes.getNumberOfChildren(this) != Nodes.VARIABLE && index >= Nodes.getNumberOfChildren(this))
			throw new IllegalArgumentException("index " + index + " cannot be accessed (" + Nodes.getNumberOfChildren(this) + " children are allowed)");
		if (index >= getActualNumberOfChildren())
			throw new IllegalStateException("child at index " + index + " cannot be accessed because the given index is not assigned so far");
		Node oldChild = children.set(index, newChild);
		oldChild.parents.remove(this);
		newChild.parents.add(this);
		updateMetaData();
		return oldChild;
	}
	
	/**
	 * Replaces the child node <code>oldChild</code> of this node by the given
	 * node <code>newChild</code> and returns if the method succeeds. This node
	 * is also removed from the old child's parent nodes and added to the new
	 * child's parent nodes.
	 * 
	 * @param oldChild the child node of this node to be replaced by the given
	 *        new child node.
	 * @param newChild the node to replace the child node <code>oldChild</code>
	 *        of this node.
	 * @return <code>true</code> if this method succeeds, otherwise
	 *         <code>false</code>.
	 */
	public boolean replaceChild(Node oldChild, Node newChild) {
		int index = children.indexOf(oldChild);
		if (index != -1) {
			children.set(index, newChild).parents.remove(this);
			newChild.parents.add(this);
			updateMetaData();
			return true;
		}
		return false;
	}
	
	/**
	 * Removes all children except the <code>index</code>'th one from this node
	 * and returns the remaining child node. Note that this method can only be
	 * applied to nodes that can have an unlimited number of child nodes.
	 *  
	 * @param index the index of the child to be kept.
	 * @return the kept child node of this node, i.e., its only child node when
	 *         this method is applied.
	 * @throws IllegalArgumentException if the given index is smaller than 0 or
	 *         greater than or equal to the maximum number of children.
	 * @throws IllegalStateException if the actual number of children is
	 *         smaller or equal to the given index or this node cannot have an
	 *         unlimited number of child nodes.
	 */
	public Node keepChild(int index) throws IllegalArgumentException, IllegalStateException {
		if (Nodes.getNumberOfChildren(this) != Nodes.VARIABLE)
			throw new IllegalStateException("only use this method on nodes without a fixed number of children");
		if (index < 0)
			throw new IllegalArgumentException("index " + index + " cannot be accessed (" + Nodes.getNumberOfChildren(this) + " children are allowed)");
		if (index >= getActualNumberOfChildren())
			throw new IllegalStateException("child at index " + index + " cannot be accessed because the given index is not assigned so far");
		for (int i = getActualNumberOfChildren()-1; i >= 0; i--)
			if (i != index)
				children.remove(i).parents.remove(this);
		updateMetaData();
		return getChild(0);
	}
	
	/**
	 * Returnes the number of parents that are actually stored by this node.
	 * 
	 * @return the number of parents that are actually stored by this node.
	 */
	public int getActualNumberOfParents() {
		return parents.size();
	}
	
	/**
	 * Adds the given node to this node's parents and this node to the given
	 * node's children and returns the inex of the new parent.
	 *  
	 * @param parent the node which should be a new parent of this node.
	 * @return the index of the new parent.
	 * @throws IllegalStateException if the maximum number of this node's
	 *         parents reached.
	 */
	public int addParent(Node parent) throws IllegalStateException {
		if (Nodes.getNumberOfParents(this) == getActualNumberOfParents())
			throw new IllegalStateException("node has reached its maximum number of parents (number of allowed parents: " + Nodes.getNumberOfParents(this) + ")");
		if (Nodes.getNumberOfChildren(parent) == parent.getActualNumberOfChildren())
			throw new IllegalStateException("new parent has reached its maximum number of children (number of allowed children: " + Nodes.getNumberOfChildren(parent) + ")");
		parents.add(parent);
		parent.children.add(this);
		updateMetaData();
		return parents.size() - 1;
	}
	
	/**
	 * Returns <code>true</code> if the given child is a parent node of this
	 * node otherwise <code>false</code>.
	 * 
	 * @param parent the node that should be tested for being a parent of this
	 *        node.
	 * @return <code>true</code> if the given child is a parent node of this
	 *         node otherwise <code>false</code>.
	 */
	public boolean containsParent(Node parent) {
		return parents.contains(parent);
	}
	
	/**
	 * Returns the <code>index</code>'th parent of this node.
	 * 
	 * @param index the index of the parent node to be returned.
	 * @return the <code>index</code>'th parent of this node.
	 */
	public Node getParent(int index) {
		return parents.get(index);
	}
	
	/**
	 * Returns an iteration over all parents of this node. The returned cursor
	 * only supports the <code>peek</code> and the <code>remove</code> method.
	 * 
	 * @return an iteration over this node's parent nodes.
	 */
	public Cursor<Node> getParents() {
		return new AbstractCursor<Node>() {
			protected int index = 0;
			
			@Override
			protected boolean hasNextObject() {
				return index < parents.size();
			}
			
			@Override
			protected Node nextObject() {
				return parents.get(index++);
			}
			
			@Override
			public void remove() throws IllegalStateException {
				super.remove();
				removeParent(index--);
			}
			
			@Override
			public boolean supportsRemove() {
				return true;
			}
		};
	}
	
	/**
	 * Removes the <code>index</code>'th parent of this node and returns it.
	 * This node is also removed from the <code>index</code>'th node's child
	 * nodes.
	 * 
	 * @param index the index of this node's parent to be removed.
	 * @return the removed parent node.
	 * @throws IllegalArgumentException if the given index is smaller than 0 or
	 *         greater than or equal to the maximum number of parents.
	 * @throws IllegalStateException if the actual number of parents is smaller
	 *         or equal to the given index.
	 */
	public Node removeParent(int index) throws IllegalArgumentException, IllegalStateException {
		if (index < 0 || Nodes.getNumberOfParents(this) != Nodes.VARIABLE && index >= Nodes.getNumberOfParents(this))
			throw new IllegalArgumentException("index " + index + " cannot be accessed (" + Nodes.getNumberOfParents(this) + " parents are allowed)");
		if (index >= getActualNumberOfParents())
			throw new IllegalStateException("parent at index " + index + " cannot be accessed because the given index is not assigned so far");
		Node oldParent = parents.remove(index);
		oldParent.children.remove(this);
		updateMetaData();
		return oldParent;
	}
	
	/**
	 * Removes the given parent node from this node and returns if the method
	 * succeeds. This node is also removed from the given node's child nodes.
	 * 
	 * @param oldParent the node to be removed from this node's parents.
	 * @return <code>true</code> if this method succeeds, otherwise
	 *         <code>false</code>.
	 */
	public boolean removeParent(Node oldParent) {
		if (parents.remove(oldParent)) {
			oldParent.children.remove(this);
			updateMetaData();
			return true;
		}
		return false;
	}
	
	/**
	 * Replaces the <code>index</code>'th parent of this node by the given node
	 * <code>newParent</code> and returns the prior parent node. This node is
	 * also removed from the <code>index</code>'th node's child nodes and added
	 * to the new parent's child nodes.
	 * 
	 * @param index the index of this node's parent to be replaced.
	 * @param newParent the node to replace the <code>index</code>'th parent of
	 *        this node.
	 * @return the replaced parent node.
	 * @throws IllegalArgumentException if the given index is smaller than 0 or
	 *         greater than or equal to the maximum number of parents.
	 * @throws IllegalStateException if the actual number of parents is smaller
	 *         or equal to the given index.
	 */
	public Node replaceParent(int index, Node newParent) throws IllegalArgumentException, IllegalStateException {
		if (index < 0 || Nodes.getNumberOfParents(this) != Nodes.VARIABLE && index >= Nodes.getNumberOfParents(this))
			throw new IllegalArgumentException("index " + index + " cannot be accessed (" + Nodes.getNumberOfParents(this) + " parents are allowed)");
		if (index >= getActualNumberOfParents())
			throw new IllegalStateException("parent at index " + index + " cannot be accessed because the given index is not assigned so far");
		Node oldParent = parents.set(index, newParent);
		oldParent.children.remove(this);
		newParent.children.add(this);
		updateMetaData();
		return oldParent;
	}
	
	/**
	 * Replaces the parent node <code>oldParent</code> of this node by the
	 * given node <code>newParent</code> and returns if the method succeeds.
	 * This node is also removed from the old parent's child nodes and added to
	 * the new parent's child nodes.
	 * 
	 * @param oldParent the parent node of this node to be replaced by the
	 *        given new parent node.
	 * @param newParent the node to replace the parent node
	 *        <code>oldParent</code> of this node.
	 * @return <code>true</code> if this method succeeds, otherwise
	 *         <code>false</code>.
	 */
	public boolean replaceParent(Node oldParent, Node newParent) {
		int index = parents.indexOf(oldParent);
		if (index != -1) {
			parents.set(index, newParent).children.remove(this);
			newParent.children.add(this);
			updateMetaData();
			return true;
		}
		return false;
	}
	
	/**
	 * Removes all parents except the <code>index</code>'th one from this node
	 * and returns the remaining parent node. Note that this method can only be
	 * applied to nodes that can have an unlimited number of parent nodes.
	 *  
	 * @param index the index of the parent to be kept.
	 * @return the kept parent node of this node, i.e., its only parent node
	 *         when this method is applied.
	 * @throws IllegalArgumentException if the given index is smaller than 0 or
	 *         greater than or equal to the maximum number of parents.
	 * @throws IllegalStateException if the actual number of parents is smaller
	 *         or equal to the given index or this node cannot have an
	 *         unlimited number of parent nodes.
	 */
	public Node keepParent(int index) throws IllegalArgumentException, IllegalStateException {
		if (Nodes.getNumberOfParents(this) != Nodes.VARIABLE)
			throw new IllegalStateException("only use this method on nodes without a fixed number of parents");
		if (index < 0)
			throw new IllegalArgumentException("index " + index + " cannot be accessed (" + Nodes.getNumberOfParents(this) + " parents are allowed)");
		if (index >= getActualNumberOfParents())
			throw new IllegalStateException("parent at index " + index + " cannot be accessed because the given index is not assigned so far");
		for (int i = getActualNumberOfParents()-1; i >= 0; i--)
			if (i != index)
				parents.remove(i).children.remove(this);
		updateMetaData();
		return getParent(0);
	}
	
	/**
	 * Returns the factory that is used to create this node's global metadata.
	 * 
	 * @return the factory that is used to create this node's global metadata.
	 */
	public Switch<Object, Object> getGlobalMetaDataFactory() {
		return globalMetaDataFactory;
	}
	
	/**
	 * Returns <code>true</code> if this node is already opened otherwise
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if this node is already opened otherwise
	 * <code>false</code>.
	 */
	public boolean isOpen() {
		return open;
	}
	
	/**
	 * Returns the metadata information for this node. Note that a node can
	 * only return its entire metadata if it is already opened.
	 * 
	 * @return the metadata information for this node.
	 */
	public CompositeMetaData<Object, Object> getMetaData() {
		return globalMetaData;
	}
	
	/**
	 * (Re-)Computes the dependent metadata information of this node. The
	 * metadata information is (re-)computed if and only if the affected node
	 * has the required number of child and parent nodes. In this case the
	 * node's global metadata factory is invoked with the node itself.
	 */
	public void updateMetaData() {
		if (Nodes.getNumberOfChildren(this) <= getActualNumberOfChildren() && Nodes.getNumberOfParents(this) <= getActualNumberOfParents())
			globalMetaDataFactory.invoke(this);
	}
	
	/**
	 * Notifies the child and parent nodes of this node about a change in this
	 * node's metadata information and urges them to (re-)compute their
	 * dependent metadata information.
	 */
	public void propagateMetaData() {
		for (Node node : children)
			node.updateMetaData();
		for (Node node : parents)
			node.updateMetaData();
	}
	
	/**
	 * Returns a string representation of this node.
	 * 
	 * @return a string representation of this node.
	 */
	@Override
	public String toString() {
		String string;
		if (semaphore)
			string = "ALREADY PRINTING " + super.toString();
		else {
			semaphore = true;
			string = globalMetaData.toString();
			if (children.size() > 0)
				string += children.toString();
			semaphore = false;
		}
		return string;
	}
	
	/**
	 * Returns the signature of this node. The signature contains the
	 * identifiers of the metadata fragments defining the node and maps the
	 * class information of this metadata fragments.
	 * 
	 * @return the signature of this node.
	 */
	public Map<Object, Class<?>> getSignature() {
		return signature;
	}
	
	/**
	 * Checks whether the given node fits this node's signature. The method
	 * returns <code>true</code> if the given node's metadata information
	 * contains every metadata fragment that is part of this node's signature
	 * and if these metadata fragments are equal. The equality of two metadata
	 * fragments is checked by calling the <code>checkAllSignatures</code>
	 * method if the signature maps <code>Node.class</code> to the according
	 * identifier or by calling the <code>equalsIgnoreCase</code> method if
	 * <code>String.class</code> is mapped or by calling the
	 * <code>equals</code> method otherwise, respectively.
	 * 
	 * @param node the node that should be checked whether it fits this node's
	 *        signature.
	 * @return <code>true</code> if the given node's metadata information
	 *         contains every metadata fragment that is part of this node's
	 *         signature and if these metadata fragments are equal otherwise
	 *         <code>false</code>.
	 */
	public boolean checkSignature(Node node) {
		if (this == node)
			return true;
		if (node == null)
			return false;
		for (Object identifier : signature.keySet())
			if (globalMetaData.contains(identifier) && (!node.getMetaData().contains(identifier) || signature.get(identifier) == Node.class && !((Node)globalMetaData.get(identifier)).checkAllSignatures((Node)node.getMetaData().get(identifier)) || signature.get(identifier) == String.class && !((String)globalMetaData.get(identifier)).equalsIgnoreCase((String)node.getMetaData().get(identifier)) || !globalMetaData.get(identifier).equals(node.getMetaData().get(identifier))))
				return false;
		return true;
	}
	
	/**
	 * Checks whether the given node's signature fits this node's signature and
	 * whether the signatures of the nodes returned by the given node's
	 * <i>all-signatures</i> strategy fit the signatures of the nodes returned
	 * by this node's <i>all-signatures</i> strategy.
	 * 
	 * @param node the node that should be checked whether it fits this node's
	 *        signature.
	 * @return <code>true</code> if the given node's metadata information
	 *         contains every metadata fragment that is part of this node's
	 *         signature and if these metadata fragments are equal otherwise
	 *         <code>false</code>. This condition is also checked for the nodes
	 *         returned by the <i>all-signatures</i> strategy of this node and
	 *         the given node.
	 */
	public boolean checkAllSignatures(Node node) {
		if (this == node)
			return true;
		if (!checkSignature(node))
			return false;
		Iterator<Node> nodes = Nodes.getAllSignaturesStrategy(this).invoke(this);
		Iterator<Node> nodesNodes = Nodes.getAllSignaturesStrategy(node).invoke(node);
		while (nodes.hasNext() && nodesNodes.hasNext())
			if (!nodes.next().checkAllSignatures(nodesNodes.next()))
				return false;
		if (nodes.hasNext() || nodesNodes.hasNext())
			return false;
		return true;
	}
	
}
