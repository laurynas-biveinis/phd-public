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

import xxl.core.collections.containers.Container;

/**
 * This class provides a split algorithm for the EXTree which uses a SplitMatrix
 * to control which types of Nodes schould be clustered.
 */
public class MatrixSplit extends Split {
    
	/**
	 * The split matrix used for splitting.
	 */
    protected SplitMatrix splitMatrix;

    /**
     * If this flag is set, the split algorithm changes the order
     * of subtrees when splitting so that a minimal number of new
     * records is created.
     */
    protected boolean orderUnimportant = false;
    
	/** 
	 * Creates a new instance of MatrixSplit. 
	 * @param container The Container to use when storing new subtrees.
	 * @param matrix The Splitmatrix to use.
	 * @param ratio The ratio between left and right partitionsize.
	 * @param tolerance The size in bytes left and right partion can differ from the desired ratio.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
	 */
	public MatrixSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, SplitMatrix matrix, float ratio, int tolerance) {
		super(container, subtreeConverter, maxSubtreeSize, ratio, tolerance);
		splitMatrix = matrix;
	}

    /** 
     * Creates a new instance of MatrixSplit,
     * ratio is set to 1.0 and tolerance to 0.
	 * @param container The Container to use when storing new subtrees.
     * @param matrix The Splitmatrix to use.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
     */
    public MatrixSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, SplitMatrix matrix) {
        super(container, subtreeConverter, maxSubtreeSize);
        splitMatrix = matrix;
    }
   
    /**
     * Creates a new instance of MatrixSplit,
     * tolerance is set to 0.
	 * @param container The Container to use when storing new subtrees.
     * @param matrix The Splitmatrix to use.
     * @param ratio The ratio between left and right partitionsize.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
     */
    public MatrixSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, SplitMatrix matrix, float ratio) {
        super(container, subtreeConverter, maxSubtreeSize, ratio);
        splitMatrix = matrix;
    }

    /**
     * Creates a new instance of MatrixSplit,
     * ratio is set to 1.0. 
	 * @param container The Container to use when storing new subtrees.
     * @param matrix The Splitmatrix to use.
     * @param tolerance The size in bytes left and right partion can differ from the desired ratio.
	 * @param maxSubtreeSize The maximum size which the container allows.
	 * @param subtreeConverter Converts a subtree into a byte representation.
     */
    public MatrixSplit(Container container, SubtreeConverter subtreeConverter, int maxSubtreeSize, SplitMatrix matrix, int tolerance) {
        super(container, subtreeConverter, maxSubtreeSize, tolerance);
        splitMatrix = matrix;
    }

    /**
     * Splits an EXTree Subtree, defined by its root node.
     * Inserts the newly created subtrees into the record manager
     * and returns the root node of the separator which has to be
     * inserted into the father subtree by the inserting function.
     * @param root The root node of the subtree
     * @param fatherTIdForNewRecords the parentTid for the new Records
     */
    public Node split(Node root, Object fatherTIdForNewRecords) {
        // System.out.println("Splitting Node");
        if (!splitLevel(root, fatherTIdForNewRecords, false)) {
            // The node wasn't split, probably because the splitmatrix was to
            // restrictive
            // System.out.println("Node has to be splitted disregarding splitmatrix");
            splitLevel(root, fatherTIdForNewRecords, true);
        }
        return root;
    }

    /**
     * Sets the value of orderUnimportant,
     * orderUnimportant == true means the split algorithm can change the order
     * of the subtrees of a node, when creating new records.
     * orderUnimportant == false means we preserve the order of the subtrees
     * even if the split produces a high number of small new records. This
     * is the default setting which should be the sensible thing for most
     * applications. 
     * @param flag The new value.
     */
    public void setOrderUnimportant(boolean flag) {
        orderUnimportant = flag;
    }

    /**
     * Splits on level of the subtree, descending into the next level choosing the
     * path which results in comming as close as possible to the desired ratio
     * When force == true the split matrix is not consulted when choosing which
     * subtrees to store separately
     */
    protected boolean splitLevel(Node root, Object father, boolean force) {
        
        Iterator it;
        ArrayList keep = new ArrayList(); //The Nodes to keep with root
        LinkedList leftSubtree = new LinkedList();
        LinkedList rightSubtree = new LinkedList();
        Node tmp=null;
        it = root.getChildNodes();
        int right;
        int left = 0;
        boolean splitOccured = false;
        
        right = calculateSize(root);
        
        //Find the "middle" of the child nodes, i.e. the position which
        //paritions the subtrees most closely to our desired ratio
        
        while (left < right*ratio && it.hasNext()) {
            tmp = (Node) it.next();
            if (force) {
                int s = subtreeConverter.getSerializedSize(tmp);
                right -= s;
                left += s;
                if (left < right*ratio)
                    leftSubtree.add(tmp);
            }
            else {
                switch (splitMatrix.getMatrixEntry(root, tmp)) {
                    case SplitMatrix.STANDALONE:
                        //This one has to be separated
                        if (!orderUnimportant) { //When the order of the childnodes is important
                            if (leftSubtree.size() != 0) {
                                keep.add(storeSubTree(leftSubtree, father));
                            }
                            leftSubtree.clear();
                        }
                        keep.add(storeSubTree(tmp, father));
                        splitOccured = true;
                        break;
                    case SplitMatrix.OTHER:
                        
                        int s = subtreeConverter.getSerializedSize(tmp);
                        right -= s;
                        left += s;
                        if (left < right*ratio)
                            leftSubtree.add(tmp);
                        break;
                    case SplitMatrix.CLUSTER:
                        keep.add(tmp);
                        if (!orderUnimportant) { //When the order of the childnodes is important
                            if (leftSubtree.size() != 0) {
                                keep.add(storeSubTree(leftSubtree, father));
                                splitOccured = true;
                            }
                            leftSubtree.clear();
                        }
                }
            }
        }
        if (leftSubtree.size() != 0) {
            keep.add(storeSubTree(leftSubtree, father));
            splitOccured = true;
        }
        if (tmp.getFirstChild() == null || subtreeConverter.getSerializedSize(tmp) <= tolerance)
            rightSubtree.add(tmp);
        else {
            keep.add(tmp);
            splitOccured = splitLevel(tmp, father, force) || splitOccured;
        }
        while (it.hasNext()) {
            tmp = (Node) it.next();
            if (force) {
                rightSubtree.add(tmp);
            }
            else {
                switch (splitMatrix.getMatrixEntry(root, tmp)) {
                    case SplitMatrix.STANDALONE:
                        //This one has to be separated
                        if (!orderUnimportant) { //When the order of the childnodes is important
                            if (rightSubtree.size() != 0) {
                                keep.add(storeSubTree(rightSubtree, father));
                            }
                            rightSubtree.clear();
                        }
                        keep.add(storeSubTree(tmp, father));
                        splitOccured = true;
                        break;
                    case SplitMatrix.OTHER:
                        rightSubtree.add(tmp);
                        break;
                    case SplitMatrix.CLUSTER:
                        if (!orderUnimportant) { //When the order of the childnodes is important
                            if (rightSubtree.size() != 0) {
                                keep.add(storeSubTree(rightSubtree, father));
                                splitOccured = true;
                            }
                            rightSubtree.clear();
                        }
                        keep.add(tmp);
                }
            }
        }
        if (rightSubtree.size() != 0) {
            keep.add(storeSubTree(rightSubtree,father));
            splitOccured = true;
        }
        root.setChildList(keep);
        return splitOccured;
    }

    /**
     * Returns the size of the subtree in bytes.
     * @param root root of the subtree.
     * @return size in bytes.
     */
    protected int calculateSize(Node root) {
        if (root.getType() == Node.MARKUP_NODE) {
			Iterator it = root.getChildNodes();
			Node tmp;
			int size = 0;
			
            while (it.hasNext()) {
                tmp = (Node) it.next();
                if (splitMatrix.getMatrixEntry(root, tmp) == SplitMatrix.OTHER)
                    size += subtreeConverter.getSerializedSize(tmp);
            }
            return size;
        }
        else
        	return subtreeConverter.getSerializedSize(root);
    }
}
