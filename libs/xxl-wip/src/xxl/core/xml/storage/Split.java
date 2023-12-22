/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage;

import java.util.Iterator;
import java.util.List;

import xxl.core.collections.containers.Container;

/**
 * The Split classes provide methods to split an XML subtree when
 * it gets to big for the blocksize while inserting an new node.
 */
public abstract class Split {
	
	/** The container used inside */
	protected Container container;
	
	/**
	 * Converter for a subtree used to determine the sizes of the subtrees. 
	 */
	protected SubtreeConverter subtreeConverter;
	
	/**
	 * Converter used for single nodes of arbitrary type.
	 */
	protected NodeConverter nodeConverter;
	
	/**
	 * The maximal possible size of a subtree. 
	 */
	protected int maxSubtreeSize;
	
	/** 
	 * The ration between left and right partitions when chosing a split.
	 */
	protected float ratio;
	
	/**
	 * How many bytes are the partitions allowed to differ from the ratio 
	 */
	protected int tolerance;

	/**
	 * Creates a new instance of SimpleSplit, using
	 * a the given ratio between left and right parition when spliting
	 * and setting the split tolerance.
	 * @param ratio The split ratio, left/right.
	 * @param tolerance The amount in bytes left and right partition can deviate from the desired ratio.
	 * @param container The Container to use when storing new subtrees.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
	 */
	public Split(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, float ratio, int tolerance) {
		this.ratio = ratio;
		this.container = container;
		this.tolerance = tolerance;
		this.maxSubtreeSize = maxSubtreeSize;
		this.subtreeConverter = subtreeConverter;
		this.nodeConverter = subtreeConverter.getNodeConverter();
	}

	/**
	 * Creates a new instance of SimpleSplit, using
	 * the given ratio between left and right parition when spliting.
	 * @param ratio The split ratio, left/right.
	 * @param container The Container to use when storing new subtrees.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
	 */
	public Split(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, float ratio) {
		this(container, subtreeConverter, maxSubtreeSize, ratio, 0);
	}

	/**
	 * Creates a new instance of SimpleSplit, using
	 * 1.0 as ratio between left and right parition when spliting
	 * and setting the split tolerance.
	 * @param tolerance The amount in bytes left and right partition can deviate from the desired ratio.
	 * @param container The Container to use when storing new subtrees.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
	 */
	public Split(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, int tolerance) {
		this(container,subtreeConverter, maxSubtreeSize, 1f, tolerance);
	}

	/** 
	 * Creates a new instance of SimpleSplit, using
	 * a ratia of 1.0 between the left and right parition when spliting.
	 * @param container The Container to use when storing new subtrees.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
	 */
	public Split(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize) {
		this(container, subtreeConverter, maxSubtreeSize, 1f, 0);
	}

	/**
	 * Splits an EXTree Subtree, defined by its root node.
	 * Inserts the newly created subtrees into the record manager
	 * and returns the root node of the separator which has to be
	 * inserted into the father subtree by the inserting function.
	 * @param root The root node of the subtree
	 */
	public Node split(Node root) {
		return split(root, root.getParentId());
	}
	
	/** 
	 * Splits an EXTree Subtree, defined by its root node.
	 * Inserts the newly created subtrees into the record manager
	 * and returns the root node of the separator which has to be
	 * inserted into the father subtree by the inserting function.
	 * @param root The root node of the subtree
	 * @param fatherTIdForNewRecords the parentTid for the new Records
	 */
	public abstract Node split(Node root, Object fatherTIdForNewRecords);

	/**
	 * Stores the subtree in a new node and returns a proxy node to
	 * the new container entry.
	 * @param root The root of the subtree to be stored.
	 * @param parentId the parentId for the new container entry.
	 * @return ProxyNode pointing to the new entry.
	 */
	protected ProxyNode storeSubTree(Node root, Object parentId) {
		
		if (root.getType() == Node.PROXY_NODE) {
			// Set parent TId
			root.setParentId(null);
			
			Object id = ((ProxyNode)root).getChildId();
			Node node = (Node) container.get(id);
			node.setParentId(parentId);
			container.update(id, node);
			
			return (ProxyNode) root;
		}
		else {
			root.setParentId(parentId);
			Object id = container.insert(root);

			EXTree.updateParentIdsInChildSubtrees(root, id, container);

			ProxyNode proxy;
			proxy = new ProxyNode();
			proxy.setChildId(id);

			
			return proxy;
		}
	}

	/**
	 * Stores the subtree containing all nodes of the given list
	 * in a new node and returns a proxy node to
	 * the new container entry.
	 * @param forest a list of nodes which should be stored in a 
	 * 	new node inside the container.
	 * @param parentId the parentId for the new container entry.
	 * @return ProxyNode pointing to the new entry.
	 */
	protected ProxyNode storeSubTree(List forest, Object parentId) {

		if (forest.size()==1)
			// We have only one subtree, if it consists only of
			// a proxy node, we return this proxy unchanged
			return storeSubTree((Node) forest.get(0), parentId);
		else {
			// more than one node
			Node newRoot = new ScaffoldNode();
			newRoot.setParentId(parentId);

			ProxyNode proxy = new ProxyNode();
			
			Iterator it = forest.iterator();
			
			while (it.hasNext()) {
				Node node = (Node) it.next();
				node.setParentId(null);
				newRoot.addChildNode(node); // (Node) it.next());
			}
			
			int size = subtreeConverter.getSerializedSize(newRoot);
			if (size>maxSubtreeSize) {
				System.out.println("Split has not been very well. One side was above maximum size ("+size+")");
				int proxySize = subtreeConverter.getSerializedSize(proxy);
				
				// Store each non-proxy-node inside a new container entry.
				// Each entry must fit - hopefully newRoot, too
				
				List children = newRoot.getChildList();
				
				for (int i=0; i<children.size(); i++) {
					Node node = (Node) children.get(i);
					if (node.getType()!=Node.PROXY_NODE) {
						int stSize = subtreeConverter.getSerializedSize(node); 
						if (stSize>proxySize) {
							// node.setParentTId() = ...
							Object id = container.insert(node);
							
							EXTree.updateParentIdsInChildSubtrees(node, id, container);
							
							ProxyNode newProxy = new ProxyNode();
							newProxy.setChildId(id);
							children.set(i,newProxy);
							size = size+proxySize-stSize;
							if (size<=maxSubtreeSize)
								break;
						}
					}
				}
				if (size>maxSubtreeSize)
					throw new RuntimeException("Severe problem with split");
				System.out.println("Splits performed");
			}
			
			Object id = container.insert(newRoot);
			EXTree.updateParentIdsInChildSubtrees(newRoot, id, container);
			
			proxy.setChildId(id);
			
			// if (newRoot.containsProxyTId(id)) throw new RuntimeException("Slope detected");
			
			return proxy;
		}
	}
}
