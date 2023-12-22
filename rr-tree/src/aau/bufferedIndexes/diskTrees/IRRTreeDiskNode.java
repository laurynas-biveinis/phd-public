/*
     Copyright (C) 2010, 2012 Laurynas Biveinis

     This file is part of RR-Tree.

     RR-Tree is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     RR-Tree is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with RR-Tree.  If not, see <http://www.gnu.org/licenses/>.
*/
package aau.bufferedIndexes.diskTrees;

import aau.bufferedIndexes.IRRTreeLeafPiggybackingInfo;
import aau.bufferedIndexes.UpdateTree;
import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

import java.util.Collection;
import java.util.Iterator;

/**
 * An interface that RRDiskTree.Node should implement.
 */
public interface IRRTreeDiskNode<E extends Convertable> {

    /* Entry data */

    /**
     * Adds a new data element to the node.
     * @param data an element to add
     */
    public void grow(final Object data);

    /**
     * Removes a data element from the node.
     * @param data an element to remove
     */
    public void remove(final Object data);

    /**
     * Copies all entries from another node.
     * @param n node to copy from
     */
    public void addEntriesFrom(final IRRTreeDiskNode<E> n);

    /**
     * Returns a collection of node entries, type unknown.
     * @return a collection of node entries
     */
    public Collection<?> getEntries();

    /**
     * Returns a collection of node entries, assuming that the node is an index node.
     * @return a collection of index entries in the node
     */
    public Collection<IRRTreeIndexEntry<E>> getNonLeafNodeEntries();

    /**
     * Returns an iterator over node entries, type unknown.
     * @return an iterator over node entries, type unknown
     */
    public Iterator<?> entries();

    /* Queries */

    /**
     * Returns a subset of children, whose descriptors overlap with a given object descriptor
     *
     * @param object an object
     * @return an iterator over subset of child entries
     */
    public Iterator<IRRTreeIndexEntry<E>> query(final E object);

    /**
     * Returns a subset of children, whose descriptors overlap with a given descriptor
     *
     * @param queryDescriptor a descriptor
     * @return an iterator over subset of child entries
     */
    public Iterator<IRRTreeIndexEntry<E>> query (final Descriptor queryDescriptor);

    /* Getters */

    /**
     * Returns <code>true</code> if this node overflows, <code>false</code> otherwise.
     * @return <code>true</code> if this node underflows, <code>false</code> otherwise
     */
    boolean overflows ();

    /**
     * Returns <code>true</code> if this node underflows, <code>false</code> otherwise.
     * @return <code>true</code> if this node underflows, <code>false</code> otherwise
     */
    boolean underflows();

    /**
     * Returns the tree level of this node.
     * @return the tree level of this node
     */
    int level ();

    /**
     * Returns a number of entries in this node.
     * @return a number of entries in this node
     */
    int number();

    /**
     * Computes a descriptor for the node
     * @return union of all node entry descriptors
     */
    public Descriptor computeDescriptor();

    /* Update support */

    /**
     * Returns the minimum allowed number of entries in the node after a split.
     * @return the minimum allowed number of entries in the node after a split
     */
    int splitMinNumber ();

    /**
     * Changes the level of the node in the tree
     * @param newLevel the new level of the node
     */
    public void setLevel(final int newLevel);

    /**
     * Chooses the subtree to follow for a new data insertion.
     *
     * @param object  the object to be inserted. Its descriptor is used to choose the subtree.
     * @return the index entry referring to the root of the chosen subtree
     */
     public IRRTreeIndexEntry<E> chooseSubtreeByObject(final Object object);

    /**
     * Loop through a given cursor and collect operations that can be performed on a node with or without enlarging its
     * MBR, disregarding if it will overflow or underflow as a result.  A deletion may be performed if there is a
     * corresponding data entry.  An insertion may be performed if it is fully contained in the current MBR of the
     * node if outside MBR operations are disallowed or always if they are allowed.  Closes cursor in the end.
     *
     * @param candidateSet      a cursor of candidate operations
     * @param outsideMBRAllowed if <code>true</code>, then operations falling outside the current node MBR are allowed
     * @return a collection of operations that may be performed on this node
     */
    public Collection<UpdateTree.Entry<E>> selectFittingOperations(
            final Cursor<UpdateTree.Entry<E>> candidateSet,
            final boolean outsideMBRAllowed);

    /**
     * Checks if an operation will increase the node size when executed.
     *
     * @param operation an operation to check for
     * @return <code>true</code> if the operation will increase the node size when executed, <code>false</code>
     * otherwise.
     */
    public boolean operationWillIncreaseNodeSize(final UpdateTree.Entry<E> operation);

    /**
     * Check the number of insertions and deletions that can be executed on this node and limit some of them so
     * that after executing the operations the node size constraints are not violated.
     *
     * @param piggybackingInfo the piggybacking info that holds numbers of candidate operations
     */
    public void limitNumberOfOperations(final IRRTreeLeafPiggybackingInfo piggybackingInfo);

    /**
     * Execute a certain subset of a given operation set on this node.  The subset is constrained by given values
     * of maximum number of insertion and deletions that may be executed.  This method returns a collection of
     * actually executed operations.
     *
     * @param candidateSet   a set of operations whose subset should be executed on this node
     * @param maxInsertions  a maximum number of insertions in the set that may be executed
     * @param maxDeletions   a minimum number of deletions in the set that may be executed
     * @return a collection of actually executed operations
     */
    public Collection<UpdateTree.Entry<E>> executeConstrainedSubsetOfOps(
            final Collection<UpdateTree.Entry<E>> candidateSet, int maxInsertions, int maxDeletions);

    /**
     * Executes given operations on the node.  In some cases not all the operations will be actually executed, for
     * example, if a deletion did not find its corresponding data entry in the node.
     *
     * @param operations                   operations to execute
     * @param insertionRemovesOldInsertion if <code>true</code>, then each completed insertion will remove an old entry
     * from the tree.  This is used to simulate execution of deletions as if they were insertions.
     * @return a set of operations actually executed.
     */
    public Collection<UpdateTree.Entry<E>> executeOps(final Iterable<UpdateTree.Entry<E>> operations,
                                                      final boolean insertionRemovesOldInsertion);

    /**
     * Prepare an efficient index for following executeConstrainedSubsetOfOps and willOperationIncreaseNodeSize
     * operations.  The index is not updated when node is updated.  The caller must call deleteEntryIndex after
     * finishing with the operations above.
     */
    void indexEntries();

    /**
     * Delete the index made by indexEntries call.
     */
    void deleteEntryIndex();
}
