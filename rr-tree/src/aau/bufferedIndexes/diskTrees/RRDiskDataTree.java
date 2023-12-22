/*
     Copyright (C) 2010, 2011, 2012 Laurynas Biveinis

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

import aau.bufferedIndexes.OperationTypeStat;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.leafNodeModifiers.IRRTreeDiskNodeOnQueryModifier;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.wrappers.IteratorCursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

import java.util.*;

/**
 * The disk tree part of the RR-tree.
 * @param <E> the data element type
 */
public class RRDiskDataTree<E extends Convertable> extends AbstractRRDiskTree<E> {

    /**
     * The RR-Tree node
     */
    public class Node extends AbstractRRDiskTree<E>.Node implements Cloneable, IRRTreeDiskDataNode<E> {

        /**
         * Creates a new node with a given level and node contents.
         *
         * @param level the level of the node
         * @param newEntries the contents of the node
         */
        Node (final int level, final Collection<?> newEntries) {
            super(level, newEntries);
        }

        /**
         * Returns a collection of node entries, assuming that the node is a leaf node.
         * @return a collection of data entries in the node
         */
        public Collection<E> getLeafNodeEntries() {
            assert level == 0;
            //noinspection unchecked
            return entries;
        }

        /**
         * Determines if a given operation may be performed on a given node, disregarding if it will overflow or
         * underflow as a result.  A deletion may be performed if there is a matching entry in the node.  An insertion
         * may be performed if it is fully enclosed (i.e. not overlapping) in the MBR of the node if outsideMBRAllowed
         * is false, and always otherwise.
         *
         * @param descriptor        descriptor of the node
         * @param operation         the operation to consider
         * @param outsideMBRAllowed if <code>false</code>, only operations that will not enlarge the node MBR will be
         *                          considered.
         * @return <code>true</code> if operation may be performed on a given node, <code>false</code> otherwise
         */
        protected boolean doesOperationFit(Descriptor descriptor, UpdateTree.Entry<E> operation, boolean outsideMBRAllowed) {
            // TODO: test outsideMBRAllowed == true case
            return (operation.isDeletion() && getLeafNodeEntries().contains(operation.getData()))
                    || (operation.isInsertion()
                        && (descriptor.contains(RRDiskDataTree.this.descriptor(operation.getData()))
                            || outsideMBRAllowed));
        }

        /**
         * Checks if a given operation will actually increase the node size when executed.  For the data tree
         * tree, the deletions always decrease the node size and insertions always increase it.
         *
         * @param operation an operation to check for
         * @return <code>true</code> if this operation will increase the node size, <code>false</code> otherwise.
         */
        public boolean operationWillIncreaseNodeSize(final UpdateTree.Entry<E> operation) {
            return operation.isInsertion();
        }

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
                final Collection<UpdateTree.Entry<E>> candidateSet, int maxInsertions, int maxDeletions) {
            final Collection<UpdateTree.Entry<E>> executedOps = new ArrayList<>(candidateSet.size());
            for (UpdateTree.Entry<E> entry : candidateSet) {
                if (entry.isDeletion() && (maxDeletions > 0)) {
                    boolean result = getLeafNodeEntries().remove(entry.getData());
                    assert result;
                    executedOps.add(entry);
                    maxDeletions--;
                    RRDiskDataTree.this.dataItems--;
                }
                else if (entry.isInsertion() && (maxInsertions > 0)
                        && !this.getLeafNodeEntries().contains(entry.getData())) { // TODO: is last condition necessary?
                    this.grow(entry.getData());
                    RRDiskDataTree.this.dataItems++;
                    executedOps.add(entry);
                    maxInsertions--;
                }
                if ((maxDeletions == 0) && (maxInsertions == 0))
                    break;
            }
            return executedOps;
        }

        protected boolean executeOp(final UpdateTree.Entry<E> entry, final boolean insertionRemovesOldInsertion) {
            if (entry.isDeletion()) {
                if (getLeafNodeEntries().remove(entry.getData())) {
                    RRDiskDataTree.this.dataItems--;
                    return true;
                }
            }
            else {
                grow(entry.getData());
                RRDiskDataTree.this.dataItems++;
                if (insertionRemovesOldInsertion) {
                    final E toRemove = getLeafNodeEntries().iterator().next();
                    getLeafNodeEntries().remove(toRemove);
                    RRDiskDataTree.this.dataItems--;
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Creates a new node of this tree with a given level and node contents.
     *
     * @param level the level of the new node
     * @param nodeContents the contents of the new node
     * @return the new node created with a given level and contents
     */
    public IRRTreeDiskNode<E> createNode(final int level, final List<?> nodeContents) {
        //noinspection unchecked
        assert (level == 0) || (nodeContents == null) || (nodeContents.size() == 0)
                || (level == getHeight((List<IRRTreeIndexEntry<E>>)nodeContents) + 1);
        return new Node(level, nodeContents);
    }

    /**
     * Creates a new empty node of this tree with a given level.
     * @param level the level of the new node
     * @return the new node created with a given level
     */
    public Node createNode(final int level) {
        return (Node)createNode (level, null);
    }

    // TODO: javadoc, tests
    public boolean deletionsLikeInsertions() {
        return false;
    }

    public TreeClearIOState cleanGarbage() {
        return new TreeClearIOState(0, 0);
    }

    /**
     * Returns the MBR descriptor of an operation.
     *
     * @param entry the operation
     * @return the MBR descriptor of operation
     */
    public Descriptor descriptor(final UpdateTree.Entry<E> entry) {
        return super.descriptor(entry.getData());
    }

    /**
     * Processes initial query results as necessary to get the final query results.  In the case of data tree, the
     * initial results are the final results, thus no processing is done.
     *
     * @param initialResult              cursor over initial query results
     * @param leafNodeModifier           the modifier for any accessed leaf nodes
     * @param leafNodeModificationStats  the leaf node modification statistics
     * @return cursor over final query results
     */
    protected <T> Cursor<E> rrQueryProcessResults(
            final Cursor<T> initialResult,
            final Cursor<UpdateTree.Entry<E>> externalResults,
            final IRRTreeDiskNodeOnQueryModifier<E> leafNodeModifier,
            final OperationTypeStat leafNodeModificationStats) {
        // TODO: unit-test!

        final Map<Object, ArrayList<E>> diskEntries = new HashMap<>();
        while (initialResult.hasNext()) {
            //noinspection unchecked
            final E answerCandidate = (E)initialResult.next();
            final Object entryId = id(answerCandidate);
            objectTracer.traceObject(answerCandidate, ObjectTracer.Operation.DATA_TREE_QUERY_1ST_LOOP);
            final ArrayList<E> sameIdList = diskEntries.get(entryId);
            if (sameIdList == null) {
                final ArrayList<E> newSameIdList = new ArrayList<>();
                newSameIdList.add(answerCandidate);
                diskEntries.put(entryId, newSameIdList);
            }
            else {
                sameIdList.add(answerCandidate);
            }
        }

        leafNodeModifier.finalizeModifications(leafNodeModificationStats);

        final List<E> finalResults = new ArrayList<>();

        while (externalResults.hasNext()) {
            final UpdateTree.Entry<E> externalEntry = externalResults.next();
            objectTracer.traceUpdateTreeEntry(externalEntry, ObjectTracer.Operation.DATA_TREE_QUERY_BUFFER_LOOP, null);
            if (externalEntry.isInsertion())
                finalResults.add(externalEntry.getData());
            else {
                final Object entryId = id(externalEntry.getData());
                final ArrayList<E> sameIdList = diskEntries.get(entryId);
                final Iterator<E> sameIdListItr = sameIdList.iterator();
                boolean foundMatch = false;
                while (sameIdListItr.hasNext()) {
                    final E sameIdEntry = sameIdListItr.next();
                    if (externalEntry.getData().equals(sameIdEntry)) {
                        sameIdListItr.remove();
                        foundMatch = true;
                        break;
                    }
                }
                assert foundMatch;
            }
        }

        for (ArrayList<E> sameIdEntries : diskEntries.values()) {
            assert (sameIdEntries.size() <= 1);
            if (sameIdEntries.size() == 1) {
                final E externalEntry = sameIdEntries.get(0);
                objectTracer.traceObject(externalEntry, ObjectTracer.Operation.DATA_TREE_QUERY_FINAL_LOOP);
                finalResults.add(externalEntry);
            }
        }

        return new IteratorCursor<>(finalResults.iterator());
    }
}
