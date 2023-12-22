/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xxl.core.collections.containers.Container;

/**
 * A very basic example of a split algorithm for XML-Subtrees.
 * It splits the subtrees into left and right partitions, trying
 * to reach a configurable ratio of left to right partition size.
 * Running MatrixSplit with an empty split matrix results in exactly
 * the same behaviour, but this implementation just looks so much nicer.
 */
public class SimpleSplit extends Split {

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
	public SimpleSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, float ratio, int tolerance) {
		super (container, subtreeConverter, maxSubtreeSize, ratio, tolerance);
	}

	/**
	 * Creates a new instance of SimpleSplit, using
	 * the given ratio between left and right parition when spliting.
	 * @param ratio The split ratio, left/right.
	 * @param container The Container to use when storing new subtrees.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
	 */
	public SimpleSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, float ratio) {
		super(container, subtreeConverter, maxSubtreeSize, ratio);
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
	public SimpleSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, int tolerance) {
		super(container, subtreeConverter, maxSubtreeSize, tolerance);
	}

	/**
	 * Creates a new instance of SimpleSplit, using
	 * a ratio of 1.0 between the left and right parition when spliting.
	 * @param container The Container to use when storing new subtrees.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
	 */
	public SimpleSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize) {
		super(container, subtreeConverter, maxSubtreeSize);
	}

	/** 
	 * Splits an EXTree Subtree, defined by its root node.
	 * Inserts the newly created subtrees into the record manager
	 * and returns the root node of the separator which	has	to be
	 * inserted into the father subtree by the inserting function.
	 * @param root The root node of the subtree
	 * @param parentIdForNewRecords the parentTid for the new Records
	 */
	public Node split(Node root, Object parentIdForNewRecords) {
		int subtreeSize = subtreeConverter.getSerializedSizeWithoutParentId(root);
		ArrayList pathList = new ArrayList();
		Node splitNode = getSplitNode(root, subtreeSize, 0, 0, pathList);
		if (splitNode==null)
			throw new RuntimeException("Split error: getSplitNode returned null");
		partitionSubTree(splitNode, pathList, parentIdForNewRecords);
		return root;
	}

	/**
	 * Determines the Node that gives us left and right partitions
	 * of the subtree with the configured ration
	 * @param root the root of the subtree
	 * @param sizeOfSubtree number of bytes in the whole subtree starting from the
	 * 	root of the whole subtree.
	 * @param sizeOfLeftTree number of bytes in the left splitting tree so far.
	 * @param sizeOfSeparatorTree number of bytes used by the separator tree.
	 * @return Node the SplitNode
	 */
	protected Node getSplitNode(Node root, int sizeOfSubtree, int sizeOfLeftTree, int sizeOfSeparatorTree, List pathList) {

		pathList.add(root);
		
		if (root.getType()==Node.PROXY_NODE || root.getType()==Node.LITERAL_NODE)
			return root;

		List l = root.getChildList();
		int numberOfChildren = l.size();

		// If we have reached a leaf, this is the one we want.
		if (numberOfChildren==0)
			return root;

		int index=0;
		int newSubtreeSize=0;
		Node currentNode = null;

		// Find the "middle" of the child nodes, i.e. the position which
		// paritions the subtrees most closely to our desired ratio
		while (true) {
			if (index==numberOfChildren) {
				return root;
				// Optimization: look for more nodes...
				// throw new RuntimeException("No!!!");
			}
			currentNode = (Node) l.get(index);
			int nodeSize = nodeConverter.getSerializedSize(root); 
			newSubtreeSize = subtreeConverter.getSerializedSizeWithoutParentId(currentNode);
			if (sizeOfLeftTree+newSubtreeSize >= (sizeOfSubtree-(sizeOfLeftTree+newSubtreeSize))*ratio) {
				// System.out.println("Maximum possible: " + (left+newSubtreeSize));
				if (newSubtreeSize<=tolerance) {
					pathList.add(root);
					return currentNode;
				}
				else
					return getSplitNode(currentNode, sizeOfSubtree, sizeOfLeftTree, sizeOfSeparatorTree+nodeSize+nodeSize, pathList);			
			}
			sizeOfLeftTree += newSubtreeSize;
			index++;
		}
	}

	/**
	 * Splits the XML-Tree given by the root Node in
	 * a number of subtrees, storing the subtrees in the
	 * container. The splitNode determines the structure of
	 * the partition used.
	 * @param splitNode The given splitnode.
	 * @param pathList The path from the root of a subtree to
	 * 	the splitNode.
	 * @param parentIdForNewRecords Identifyer for new records which
	 * 	are allocated.
	 */
	protected void partitionSubTree(Node splitNode, List pathList, Object parentIdForNewRecords) {

		Iterator plIterator = pathList.iterator();
		Iterator it;
		Node currentRoot;
		Node splitNodeOnLevel;
		Node tmp;
		LinkedList subTree = new LinkedList();
		ProxyNode leftProxy;
		ProxyNode rightProxy;
		Node middle;

		if (plIterator.hasNext())
			currentRoot = (Node) plIterator.next();
		else
			return;

		while (plIterator.hasNext()) {
			leftProxy = null;
			rightProxy = null;
			middle = null;
			splitNodeOnLevel = (Node) plIterator.next();
			
			// Make a new subtree consisting of the childnodes to the left of the
			// path to d:
			it = currentRoot.getChildNodes();
			// The list subTree is empty here
	
			while (true) {
				tmp = (Node) it.next();
				if (tmp==splitNodeOnLevel) {
					// Create a record from the left subtree and create a proxy for the root node
					if (!subTree.isEmpty()) {
						leftProxy = storeSubTree(subTree, parentIdForNewRecords);
						subTree = new LinkedList();
					}
					break;
				}
	
				subTree.add(tmp);
				it.remove();
			}
		
			// The subTree-list is empty here!
			
			// When we have reached the splitNode, put it into the right subtree,
			
			if (tmp==splitNode)
				subTree.add(tmp);
			else
				middle = tmp;
			it.remove();
	
			// store the nodes to the right
			while (it.hasNext()) {
				subTree.add(it.next());
				it.remove();
			}
	
			// Create a record from the right subtree and create a proxy for the root node
			if (!subTree.isEmpty())
				rightProxy = storeSubTree(subTree, parentIdForNewRecords);
			subTree.clear();
			
			if (leftProxy != null)
				currentRoot.addChildNode(leftProxy);
			if (middle != null)
				currentRoot.addChildNode(middle);
			if (rightProxy != null)
				currentRoot.addChildNode(rightProxy);
			
			currentRoot = splitNodeOnLevel;
		}
	}
}
