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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.collections.queues.Queues;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.functions.Switch;
import xxl.core.predicates.Predicate;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * This class provides static methods for creating <i>ready to use</i>
 * instances of a {@link Node node} in a directed acyclic graph. Beside this
 * methods, it contains constants for identifying local metadata fragments
 * inside a node's global metadata, methods for accessing them and local
 * metadata factories for updating them.
 * 
 * @see Node
 */
public class Nodes {
	
	// global metadata factory
	
	/**
	 * A local metadata factory that builds up a node's global metadata. The
	 * factory must be put to the node's global metadata factory (the
	 * {@link Switch switch} function that is used for storing the node' local
	 * factories) at first, thereafter the local metadata factories used for
	 * creating the local metadata fragments building up the global composite
	 * metadata can be put to the global metadata factory. The
	 * <code>invoke</code> method if this function builds up the global
	 * metadata of a specified node, i.e., the local metadata fragments are
	 * created by calling the local factories on this node in the same order as
	 * they are inserted into the global factory (switch function) and their
	 * results are put into the global metadata identified by the identifier of
	 * the local factory.
	 * 
	 * <p>Whensoever at least one local metadata fragment changes, the affected
	 * node is urged to propagate this change to its child and parent
	 * nodes.</p>
	 * 
	 * @see xxl.core.functions.Function
	 * @see xxl.core.functions.Switch
	 * @see xxl.core.util.metaData.CompositeMetaData
	 */
	public static final Function<Object, CompositeMetaData<Object, Object>> GLOBAL_METADATA_FACTORY = new Function<Object, CompositeMetaData<Object, Object>>() {
		@Override
		public CompositeMetaData<Object, Object> invoke(Object node) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)node).getMetaData();
			Switch<Object, Object> globalMetaDataFactory = ((Node)node).getGlobalMetaDataFactory();
			// try to create the local metadata fragments by calling the local
			// factories
			boolean changed = false;
			for (Iterator<Object> identifiers = globalMetaDataFactory.identifiers(); identifiers.hasNext(); ) {
				Object identifier = identifiers.next(), localMetaData = null;
				if (identifier != node) {
					try {
						localMetaData = globalMetaDataFactory.invoke(identifier, node);
					}
					catch (Exception e) {
						if (globalMetaData.contains(identifier)) {
							globalMetaData.remove(identifier);
							changed = true;
						}
						continue;
					}
					Object formerLocalMetaData = globalMetaData.put(identifier, localMetaData);
					changed |= (formerLocalMetaData == null ? localMetaData != null : !formerLocalMetaData.equals(localMetaData));
				}
			}
			// when at least one local metadata fragment has changed progagate
			// the changes in global metadata
			if (changed)
				((Node)node).propagateMetaData();
			return globalMetaData;
		}
	};
	
	// metadata identifier and metadata fragment constants
	
	/**
	 * This constant is used to identify a node's type inside its global
	 * metadata.
	 */
	public static final String TYPE = "NODE->TYPE";
	
	/**
	 * This constant is used to identify a node's number of children inside its
	 * global metadata.
	 * 
	 * @see #VARIABLE
	 */
	public static final String NUMBER_OF_CHILDREN = "NODE->NUMBER_OF_CHILDREN";
	
	/**
	 * This constant is used to identify a node's number of parents inside its
	 * global metadata.
	 * 
	 * @see #VARIABLE
	 */
	public static final String NUMBER_OF_PARENTS = "NODE->NUMBER_OF_PARENTS";
	
	/**
	 * This constant can be used to denote that a node does not have a fixed
	 * number of child or parent nodes.
	 */
	public static final int VARIABLE = -1;
	
	/**
	 * This constant is used to identify a node's <i>all-signatures</i>
	 * strategy inside its global metadata. The <i>all-signatures</i> strategy
	 * is represented by a function that is called with the affected node and
	 * returns an iteration over the nodes whose signature should be checked in
	 * the course of calls to the <code>checkAllSignatures</code> method.
	 */
	public static final String ALL_SIGNATURES_STRATEGY = "NODE->ALL_SIGNATURES_STRATEGY";
	
	/**
	 * A <i>all-signatures</i> strategy that simple returns the affected node's
	 * child nodes.
	 */
	public static final Function<Node, Iterator<Node>> SIGNATURES_OF_CHILDREN = new Function<Node, Iterator<Node>>() {
		@Override
		public Iterator<Node> invoke(Node node) {
			return node.getChildren();
		}
	};
	
	/**
	 * A <i>all-signatures</i> strategy that simple returns the affected node's
	 * parent nodes.
	 */
	public static final Function<Node, Iterator<Node>> SIGNATURES_OF_PARENTS = new Function<Node, Iterator<Node>>() {
		@Override
		public Iterator<Node> invoke(Node node) {
			return node.getParents();
		}
	};
	
	/**
	 * This constant is used to identify a node's anchor inside its global
	 * metadata. Whenever a node's metadata depends on another node's metadata
	 * that is not a child of the first node, an anchor is specified inside the
	 * node's global metadata that references the required node.
	 */
	public static final String ANCHOR = "NODE->ANCHOR";
	
	/**
	 * This constant is used to identify a node's anchor placement strategy
	 * inside its global metadata. Whenever a node needs another anchor
	 * placement strategy than passing it to its child nodes, it can be
	 * specified in the node's global metadata.
	 */
	public static final String ANCHOR_PLACEMENT_STRATEGY = "NODE->ANCHOR_PLACEMENT_STRATEGY";
	
	// metadata fragment factory
	
	/**
	 * A local metadata factory that can be used to reset different metadata
	 * fragments simply by removing the existing metadata fragment stored in
	 * the node's global metadata and then causing an exception in such a way
	 * as to disable a new metadata fragment to be set.
	 */
	public static final Function<Object, Object> RESETTING_METADATA_FACTORY = new Function<Object, Object>() {
		@Override
		public Object invoke(Object identifier, Object node) {
			CompositeMetaData<Object, Object> globalMetaData = ((Node)node).getMetaData();
			if (globalMetaData.contains(identifier))
				globalMetaData.remove(identifier);
			throw new MetaDataException("this exception is thrown to avoid the creation of a new metadata fragment after resetting");
		}
	};
	
	/**
	 * A local metadata factory that can be used to update the metadata
	 * fragment holding a node in a node's metadata. It places an anchor to
	 * the node, opens it and returns it.
	 */
	public static final Function<Object, Node> NODE_WITH_ANCHOR_METADATA_FACTORY = new Function<Object, Node>() {
		@Override
		public Node invoke(Object identifier, Object node) {
			Node containedNode = Nodes.getNode((Node)node, identifier);
			Nodes.placeAnchor(containedNode, (Node)node);
			return containedNode;
		}
	};
	
	// static 'constructors'
	
	/**
	 * Creates a new node providing the given local metadata fragments and
	 * registers factory methods for this metadata fragments.
	 * 
	 * @param type a string identifying the type of the node to be returned.
	 * @param numberOfChildren the number of children required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of child nodes.
	 * @param numberOfParents the number of parents required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of parent nodes.
	 * @param allSignaturesStrategy the function that is called with the
	 *        created node and returns an iteration over the nodes whose
	 *        signature should be checked in the course of calls to the node's
	 *        <code>checkAllSignatures</code> method.
	 * @param globalMetadataFactory a factory for the node's global metadata,
	 *        i.e., a factory that is used to update the global metadata of the
	 *        node to be returned.
	 * @return a new node providing the given local metadata fragments.
	 */
	public static final Node newNode(String type, int numberOfChildren, int numberOfParents, Function<? super Node, ? extends Iterator<Node>> allSignaturesStrategy, Function<Object, ?> globalMetadataFactory) {
		Node node = new Node(new CompositeMetaData<Object, Object>(), new Switch<Object, Object>(), new HashMap<Object, Class<?>>());
		CompositeMetaData<Object, Object> globalMetaData = node.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = node.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = node.getSignature();
		
		globalMetaDataFactory.put(node, globalMetadataFactory);
		globalMetaData.add(TYPE, type);
		globalMetaData.add(NUMBER_OF_CHILDREN, numberOfChildren);
		globalMetaData.add(NUMBER_OF_PARENTS, numberOfParents);
		globalMetaData.add(ALL_SIGNATURES_STRATEGY, allSignaturesStrategy);
		
		signature.put(TYPE, String.class);
		signature.put(NUMBER_OF_CHILDREN, Integer.class);
		signature.put(NUMBER_OF_PARENTS, Integer.class);
	
		return node;
	}
	
	/**
	 * Creates a new node providing the given local metadata fragments and
	 * registers factory methods for this metadata fragments.
	 * 
	 * @param type a string identifying the type of the node to be returned.
	 * @param numberOfChildren the number of children required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of child nodes.
	 * @param numberOfParents the number of parents required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of parent nodes.
	 * @param allSignaturesStrategy the function that is called with the
	 *        created node and returns an iteration over the nodes whose
	 *        signature should be checked in the course of calls to the node's
	 *        <code>checkAllSignatures</code> method.
	 * @return a new node providing the given local metadata fragments.
	 */
	public static final Node newNode(String type, int numberOfChildren, int numberOfParents, Function<? super Node, ? extends Iterator<Node>> allSignaturesStrategy) {
		return newNode(type, numberOfChildren, numberOfParents, allSignaturesStrategy, GLOBAL_METADATA_FACTORY);
	}
	
	/**
	 * Creates a new node providing the given local metadata fragments and
	 * registers factory methods for its metadata fragments. The node's
	 * <code>checkAllSignatures</code> method checks the node's signature and
	 * its child nodes' signatures.
	 * 
	 * @param type a string identifying the type of the node to be returned.
	 * @param numberOfChildren the number of children required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of child nodes.
	 * @param numberOfParents the number of parents required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of parent nodes.
	 * @return a new node providing the given local metadata fragments.
	 */
	public static final Node newNode(String type, int numberOfChildren, int numberOfParents) {
		return newNode(type, numberOfChildren, numberOfParents, SIGNATURES_OF_CHILDREN);
	}
	
	/**
	 * Creates a new node providing the given local metadata fragments and
	 * registers factory methods for its metadata fragments. The returned node
	 * does not need a fixed number of parent nodes.
	 * 
	 * @param type a string identifying the type of the node to be returned.
	 * @param numberOfChildren the number of children required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of child nodes.
	 * @param allSignaturesStrategy the function that is called with the
	 *        created node and returns an iteration over the nodes whose
	 *        signature should be checked in the course of calls to the node's
	 *        <code>checkAllSignatures</code> method.
	 * @return a new node providing the given local metadata fragments.
	 */
	public static final Node newNode(String type, int numberOfChildren, Function<? super Node, ? extends Iterator<Node>> allSignaturesStrategy) {
		return newNode(type, numberOfChildren, VARIABLE, allSignaturesStrategy);
	}
	
	/**
	 * Creates a new node providing the given local metadata fragments and
	 * registers factory methods for its metadata fragments. The returned node
	 * does not need a fixed number of parent nodes and its
	 * <code>checkAllSignatures</code> method checks the node's signature and
	 * its child nodes' signatures.
	 * 
	 * @param type a string identifying the type of the node to be returned.
	 * @param numberOfChildren the number of children required by the returned
	 *        node. It can be set to {@link #VARIABLE} for indicating that the
	 *        node does not need a fixed number of child nodes.
	 * @return a new node providing the given local metadata fragments.
	 */
	public static final Node newNode(String type, int numberOfChildren) {
		return newNode(type, numberOfChildren, VARIABLE, SIGNATURES_OF_CHILDREN);
	}
	
	/**
	 * Creates a new node providing the given local metadata fragments and
	 * registers factory methods for its metadata fragments. The returned node
	 * does not need a fixed number of child nodes and parent nodes.
	 * 
	 * @param type a string identifying the type of the node to be returned.
	 * @param allSignaturesStrategy the function that is called with the
	 *        created node and returns an iteration over the nodes whose
	 *        signature should be checked in the course of calls to the node's
	 *        <code>checkAllSignatures</code> method.
	 * @return a new node providing the given local metadata fragments.
	 */
	public static final Node newNode(String type, Function<? super Node, ? extends Iterator<Node>> allSignaturesStrategy) {
		return newNode(type, VARIABLE, VARIABLE, allSignaturesStrategy);
	}
	
	/**
	 * Creates a new node providing the given local metadata fragment and
	 * registers factory methods for its metadata fragments. The returned node
	 * does not need a fixed number of child nodes and parent nodes and it's
	 * <code>checkAllSignatures</code> method checks the node's signature and
	 * its child nodes' signatures.
	 * 
	 * @param type a string identifying the type of the node to be returned.
	 * @return a new node providing the given local metadata fragment.
	 */
	public static final Node newNode(String type) {
		return newNode(type, VARIABLE, VARIABLE, SIGNATURES_OF_CHILDREN);
	}
	
	// metadata fragment accessors
	
	/**
	 * Returns the type of the given node.
	 * 
	 * @param node the node whose type should be returned.
	 * @return the type of the given node.
	 */
	public static final String getType(Node node) {
		return (String)node.getMetaData().get(TYPE);
	}
	
	/**
	 * Returns the number of children the given node should be able to store.
	 * 
	 * @param node the node whose required number of children should be
	 *        returned.
	 * @return the number of children the given node should be able to store.
	 */
	public static final int getNumberOfChildren(Node node) {
		return (Integer)node.getMetaData().get(NUMBER_OF_CHILDREN);
	}
	
	/**
	 * Returns the number of parents the given node should be able to store.
	 * 
	 * @param node the node whose required number of parents should be
	 *        returned.
	 * @return the number of parents the given node should be able to store.
	 */
	public static final int getNumberOfParents(Node node) {
		return (Integer)node.getMetaData().get(NUMBER_OF_PARENTS);
	}
	
	/**
	 * Returns the <i>all-signatures</i> strategy of the given node, that
	 * determines the nodes whose signature should be checked in the course of
	 * calls to the node's <code>checkAllSignatures</code> method.
	 * 
	 * @param node the node whose <i>all-signatures</i> strategy should be
	 *        returned.
	 * @return the <i>all-signatures</i> strategy of the given node, that
	 *         determines the nodes whose signature should be checked in the
	 *         course of calls to the node's <code>checkAllSignatures</code>
	 *         method.
	 */
	public static final Function<? super Node, ? extends Iterator<Node>> getAllSignaturesStrategy(Node node) {
		return (Function<? super Node, ? extends Iterator<Node>>)node.getMetaData().get(ALL_SIGNATURES_STRATEGY);
	}
	
	/**
	 * Returns the anchor of the given node that references a node providing
	 * required metadata informations.
	 * 
	 * @param node the node whose anchor should be returned.
	 * @return the anchor of the given node that references a node providing
	 *         required metadata informations.
	 */
	public static final Node getAnchor(Node node) {
		return (Node)node.getMetaData().get(ANCHOR);
	}

	/**
	 * Returns the anchor of the given node that references a node providing
	 * required metadata informations.
	 * 
	 * @param node the node whose anchor should be returned.
	 * @return the anchor of the given node that references a node providing
	 *         required metadata informations.
	 */
	public static final Function<Node, ?> getAnchorPlacementStrategy(Node node) {
		return (Function<Node, ?>)node.getMetaData().get(ANCHOR_PLACEMENT_STRATEGY);
	}

	/**
	 * Places the specified anchor to the given node and its child nodes.
	 * Whenever a node's global metadata provides an anchor placement strategy
	 * it is invoked with the node and the specified anchor to be placed.
	 * 
	 * @param node the node for which the specified anchor should be placed.
	 * @param anchor the anchor to be placed.
	 */
	public static final void placeAnchor(Node node, Node anchor) {
		Queue<Node> queue = new ListQueue<Node>();
		queue.enqueue(node);
		while (!queue.isEmpty()) {
			node = queue.dequeue();
			if (Nodes.getNumberOfChildren(node) != 0)
				Queues.enqueueAll(queue, node.getChildren());
			if (node.getMetaData().contains(ANCHOR_PLACEMENT_STRATEGY))
				Nodes.getAnchorPlacementStrategy(node).invoke(node, anchor);
		}
	}
	
	/**
	 * Returns the node from of the given node's metadata associated with the
	 * specified identifier.
	 * 
	 * @param node the node whose metadata should be revised for a node
	 *        associated with the specified identifier.
	 * @param identifier the identifier the node to be returned is associated
	 *        with.
	 * @return the node from of the given node's metadata associated with the
	 *         specified identifier.
	 */
	public static final Node getNode(Node node, Object identifier) {
		return (Node)node.getMetaData().get(identifier);
	}
	
	/**
	 * Returns an iteration over the nodes from of the given node's metadata
	 * associated with the specified identifier prefix followed by a
	 * consecutive number.
	 * 
	 * @param node the node whose metadata should be revised for nodes
	 *        associated with the specified identifier prefix followed by
	 *        consecutive numbers.
	 * @param identifierPrefix the prefix of the identifier the nodes to be
	 *        returned are associated with.
	 * @return an iteration over the nodes from of the given node's metadata
	 *         associated with the specified identifier prefix followed by a
	 *         consecutive number.
	 */
	public static final Cursor<Node> getNodes(Node node, final String identifierPrefix) {
		final CompositeMetaData<Object, Object> globalMetaData = node.getMetaData();
		return new AbstractCursor<Node>() {
			protected int i = 0;
			
			@Override
			public boolean hasNextObject() {
				return globalMetaData.contains(identifierPrefix + i);
			}
			
			@Override
			public Node nextObject() {
				return (Node)globalMetaData.get(identifierPrefix + i++);
			}
			
			@Override
			public void reset() {
				super.reset();
				i = 0;
			}
			
			@Override
			public boolean supportsReset() {
				return true;
			}
		};
	}
	
	/**
	 * Counts the nodes that are registered to the given node's global metadata
	 * using the given identifier prefix followed by a number and returns the
	 * number.
	 * 
	 * @param node the node whose metadata should be revised for nodes
	 *        associated with the specified identifier prefix followed by
	 *        consecutive numbers.
	 * @param identifierPrefix the prefix of the identifier the nodes to be
	 *        returned are associated with.
	 * @return the number of nodes that are registered to the given node's
	 *         global metadata using the given identifier prefix followed by a
	 *         number and returns the number.
	 */
	public static final int countNodes(Node node, String identifierPrefix) {
		CompositeMetaData<Object, Object> globalMetaData = node.getMetaData();
		for (int i = 0; true; i++)
			if (!globalMetaData.contains(identifierPrefix + i))
				return i;
	}
	
	/**
	 * Puts the nodes contained by the given iteration to the global metadata
	 * of the given node using the specified identifier prefix followed by a
	 * consectuive number associated with their positions (indices) inside the
	 * iteration for generating unique identifier.
	 *  
	 * @param node the node whose global metadata should be extended by the
	 *        specified nodes.
	 * @param identifierPrefix a string that is used in combination with a
	 *        consectuive number associated with their positions (indices)
	 *        inside the given iteration for generating unique identifiers.
	 * @param nodes an iteration holding the nodes that should be put to the
	 *        global metadata of the given node.
	 * @param precondition a predicate that is invoked before a node of the
	 *        given iteration is put to the global metadata of the given node.
	 *        If the precondition does not hold, the execution of this method
	 *        is interrupted by an
	 *        {@link IllegalArgumentException exception}.
	 * @param localMetaDataFactory a local metadata factory that should be used
	 *        to create/update the metadata represented by the put nodes.
	 * @param addToSignature decides whether the put nodes should be considered
	 *        when checking the given nodes signature.
	 * @throws IllegalArgumentException if the specified precondition does not
	 *         hold for a node of the given iteration.
	 */
	public static final void putNodes(Node node, String identifierPrefix, Iterator<? extends Node> nodes, Predicate<? super Node> precondition, Function<Object, ? extends Node> localMetaDataFactory, boolean addToSignature) throws IllegalArgumentException {
		CompositeMetaData<Object, Object> globalMetaData = node.getMetaData();
		Switch<Object, Object> globalMetaDataFactory = node.getGlobalMetaDataFactory();
		Map<Object, Class<?>> signature = node.getSignature();
		
		for (int i = 0; nodes.hasNext(); i++) {
			if (!precondition.invoke(node = nodes.next()))
				throw new IllegalArgumentException("a node of the specified iteration cannot be added to the given node's global metadata because it does not hold the specified precondition");
			globalMetaData.put(identifierPrefix + i, node);
			if (localMetaDataFactory != null)
				globalMetaDataFactory.put(identifierPrefix + i, localMetaDataFactory);
			
			if (addToSignature)
				signature.put(identifierPrefix + i, Node.class);
		}
	}
	
	/**
	 * Puts the nodes contained by the given iteration to the global metadata
	 * of the given node using the specified identifier prefix followed by a
	 * consectuive number associated with their positions (indices) inside the
	 * iteration for generating unique identifier.
	 *  
	 * @param node the node whose global metadata should be extended by the
	 *        specified nodes.
	 * @param identifierPrefix a string that is used in combination with a
	 *        consectuive number associated with their positions (indices)
	 *        inside the given iteration for generating unique identifiers.
	 * @param nodes an iteration holding the nodes that should be put to the
	 *        global metadata of the given node.
	 * @param precondition a predicate that is invoked before a node of the
	 *        given iteration is put to the global metadata of the given node.
	 *        If the precondition does not hold, the execution of this method
	 *        is interrupted by an
	 *        {@link IllegalArgumentException exception}.
	 * @param addToSignature decides whether the put nodes should be considered
	 *        when checking the given nodes signature.
	 * @throws IllegalArgumentException if the specified precondition does not
	 *         hold for a node of the given iteration.
	 */
	public static final void putNodes(Node node, String identifierPrefix, Iterator<? extends Node> nodes, Predicate<? super Node> precondition, boolean addToSignature) {
		putNodes(node, identifierPrefix, nodes, precondition, null, addToSignature);
	}
	
	// private constructor
	
	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Nodes() {
		// private access in order to ensure non-instantiability
	}
	
}
