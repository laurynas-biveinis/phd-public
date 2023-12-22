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
import aau.workload.DataID;
import xxl.core.collections.containers.Container;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.wrappers.IteratorCursor;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.Tree;
import xxl.core.io.Convertable;

import java.util.*;

/**
 * The disk tree part of the RR-tree that stores I/D flag on the disk
 */
public class RRDiskUpdateTree<E extends Convertable> extends AbstractRRDiskTree<E> implements IRRDiskUpdateTree<E> {

    /**
     * The RR-tree disk node that stores I/D flag on the disk.
     */
    public class Node extends AbstractRRDiskTree<E>.Node implements IRRTreeDiskUpdateNode<E> {

        /**
         * Creates a new node with a given level and node contents.
         *
         * @param level the level of the node
         * @param newEntries the contents of the node.  They must have previously been in the tree.
         */
        Node (final int level, final Collection<?> newEntries) {
            super(level, newEntries);
        }

        /**
         * Returns a collection of node entries, assuming that the node is a leaf node.
         *
         * @return a collection of data entries in the node
         */
        public Collection<UpdateTree.Entry<E>> getLeafNodeEntries() {
            //noinspection unchecked
            return entries;
        }

        /**
         * A stop-gap measure before nodes implement Iterable properly.  Signals to a node that its element was just
         * removed through an iterator.
         */
        public void dataItemRemoved() {
            RRDiskUpdateTree.this.dataItems--;
        }

        /**
         * A stop-gap measure before nodes implement Iterable properly.  Signals to a node that its element was just
         * added.
         */
        public void dataItemAdded() {
            RRDiskUpdateTree.this.dataItems++;
        }

        /**
         * Determines if a given operation may be performed on a given node, disregarding if it will overflow or
         * underflow as a result.  For the update tree, both insertions and deletions can be
         * performed on the node as long as their MBR is fully contained in the node if outsideMBRAllowed is not set,
         * and always otherwise.
         *
         * @param descriptor descriptor of the node
         * @param operation  the operation to consider
         * @param outsideMBRAllowed if <code>false</code>, only operations that will not enlarge the node MBR will be
         *                          considered.
         * @return <code>true</code> if operation may be performed on a given node, <code>false</code> otherwise
         */
        protected boolean doesOperationFit(final Descriptor descriptor, final UpdateTree.Entry<E> operation,
                                           final boolean outsideMBRAllowed) {
            // TODO: test outsideMBRAllowed == true case
            return descriptor.contains(RRDiskUpdateTree.this.descriptor(operation)) || outsideMBRAllowed;
        }

        /**
         * Executes the operation on the node.  First checks if there is an corresponding opposite operation in the
         * node.  If there is, removes it, otherwise adds the current operation to the node.  
         *
         * @param entry                        an operation to execute
         * @param insertionRemovesOldInsertion if <code>true</code>, then each completed insertion will remove an old
         *                                     entry from the tree.  This is used to simulate execution of deletions as
         *                                     if they were insertions.  This parameter is ignored for the update tree.
         * @return <code>true</code> if this operation was executed, <code>false</code> otherwise.
         */
        protected boolean executeOp(final UpdateTree.Entry<E> entry, final boolean insertionRemovesOldInsertion) {
            final boolean oppositeRemoved = getLeafNodeEntries().remove(entry.makeOpposite());
            if (!oppositeRemoved) {
                RRDiskUpdateTree.this.dataItems++;
                grow(entry);
            }
            else {
                RRDiskUpdateTree.this.dataItems--;
            }
            return true;
        }

        /**
         * Checks if a given operation will actually increase the node size when executed.  An operation will decrease
         * the node size if there is a corresponding opposite operation, otherwise it will increase it.
         *
         * @param operation an operation to check for
         * @return <code>true</code> if the operation will increase the node size when executed, <code>false</code>
         *         otherwise.
         */
        public boolean operationWillIncreaseNodeSize(UpdateTree.Entry<E> operation) {
            assert haveHashedEntries;
            return !hashedEntries.contains(operation.makeOpposite());
        }

        /**
         * Execute a certain subset of a given operation set on this node.  The subset is constrained by given values
         * of maximum number of insertion and deletions that may be executed.  This method returns a collection of
         * actually executed operations.
         *
         * @param candidateSet  a set of operations whose subset should be executed on this node
         * @param maxInsertions a maximum number of insertions in the set that may be executed
         * @param maxDeletions  a minimum number of deletions in the set that may be executed
         * @return a collection of actually executed operations
         */
        public Collection<UpdateTree.Entry<E>> executeConstrainedSubsetOfOps(Collection<UpdateTree.Entry<E>> candidateSet, int maxInsertions, int maxDeletions) {
            final Collection<UpdateTree.Entry<E>> executedOps = new ArrayList<>(candidateSet.size());
            for (UpdateTree.Entry<E> entry : candidateSet) {
                if (operationWillIncreaseNodeSize(entry)) {
                    if (maxInsertions > 0) {
                        grow(entry);
                        RRDiskUpdateTree.this.dataItems++;
                        maxInsertions--;
                        executedOps.add(entry);
                    }
                }
                else if (maxDeletions > 0) {
                    boolean result = getLeafNodeEntries().remove(entry.makeOpposite());
                    assert result;
                    RRDiskUpdateTree.this.dataItems--;
                    maxDeletions--;
                    executedOps.add(entry);
                }
                if ((maxInsertions == 0) && (maxDeletions == 0))
                    break;
            }
            return executedOps;
        }

        /**
         * Copies all entries from another node.
         *
         * @param n node to copy from
         */
        public void addEntriesFrom(final IRRTreeDiskNode<E> n) {
            // TODO: test!
            if (n.level() > 0) {
                assert level() > 0;
                super.addEntriesFrom(n);
                return;
            }
            assert level() == 0;
            final IRRTreeDiskUpdateNode<E> node = (IRRTreeDiskUpdateNode<E>)n;
            // TODO: fix O(n^2)
            for (final UpdateTree.Entry<E> e : node.getLeafNodeEntries()) {
                boolean shouldAnnihilate = false;
                final Iterator<UpdateTree.Entry<E>> thisNodeEntriesItr
                        = getLeafNodeEntries().iterator();
                while (thisNodeEntriesItr.hasNext()) {
                    final UpdateTree.Entry<E> e2 = thisNodeEntriesItr.next();
                    if (doEntriesAnnihilate(e, e2)) {
                        shouldAnnihilate = true;
                        thisNodeEntriesItr.remove();
                        RRDiskUpdateTree.this.dataItems--;
                        RRDiskUpdateTree.this.dataItems--;
                        break;
                    }
                }
                if (!shouldAnnihilate)
                    grow(e);
            }
        }

    }

    // TODO: javadoc, unit test, use it more
    public boolean doEntriesAnnihilate(final UpdateTree.Entry<E> e1,
                                       final UpdateTree.Entry<E> e2) {
        return (e1.getOperationType() == e2.getOperationType().opposite())
                && (id(e1.getData()).equals(id(e2.getData())))
                && (e1.getData().equals(e2.getData()));
    }

    /**
     * Initializes this tree.
     *
     * @param getId            the function that returns the ID of a given object
     * @param getDescriptor    the function that returns the descriptor of a given object
     * @param container        the container which should store tree nodes
     * @param minNodeCapacity  the minimum capacity of a tree node
     * @param maxNodeCapacity  the maximum capacity of a tree node
     * @param objectTracer     the object tracer to use
     * @return this tree initialized
     */
    public Tree initialize(final Function<E, DataID> getId, final Function<E, Descriptor> getDescriptor,
                           final Container container, final int minNodeCapacity, final int maxNodeCapacity,
                           final ObjectTracer<E> objectTracer) {
        // TODO: oh the generics how I don't understand you
        //noinspection unchecked
        return super.initialize(getId, getDescriptor.composeUnary((Function)UpdateTree.getObjectUnwrapper()), container,
                minNodeCapacity, maxNodeCapacity, objectTracer);
    }

    /**
     * Creates a new node of this tree with a given level and node contents.
     *
     * @param level        the level of the new node
     * @param nodeContents the contents of the new node.  They must have previously been in the tree.
     * @return the new node created with a given level and contents
     */
    public IRRTreeDiskNode<E> createNode(int level, List<?> nodeContents) {
        //noinspection unchecked
        return new Node(level, nodeContents);
    }

    /**
     * Creates a new empty node of this tree with a given level.
     *
     * @param level the level of the new node
     * @return the new node created with a given level
     */
    public Node createNode(int level) {
        return new Node(level, null);
    }

    // TODO: javadoc, test
    public boolean deletionsLikeInsertions() {
        return true;
    }

    public TreeClearIOState cleanGarbage() {
        return null;
    }

    public void setNumberOfDataItems(final int dataItemsInTree) {
        dataItems = dataItemsInTree;
    }

    /**
     * Returns the MBR descriptor of an operation.
     *
     * @param entry the operation
     * @return the MBR descriptor of operation
     */
    public Descriptor descriptor (final UpdateTree.Entry<E> entry) {
        return super.descriptor((Object)entry);
    }

    /**
     * Processes initial query results as necessary to get the final query results.  For the update tree, matches
     * deletions with insertions and vice versa to remove such pairs from the result set. 
     *
     * @param initialResult  cursor over initial query results
     * @param leafNodeModifier           the modifier for any accessed leaf nodes
     * @param leafNodeModificationStats  the leaf node modification statistics
     * @return cursor over final query results
     */
    protected <T> Cursor<E> rrQueryProcessResults(final Cursor<T> initialResult,
                                                  final Cursor<UpdateTree.Entry<E>> externalResults,
                                                  final IRRTreeDiskNodeOnQueryModifier<E> leafNodeModifier,
                                                  final OperationTypeStat leafNodeModificationStats) {

        final Map<Object, ArrayList<UpdateTree.Entry<E>>> resultsInProgress
                = new HashMap<>();

        while (initialResult.hasNext()) {
            //noinspection unchecked
            final UpdateTree.Entry<E> resultCandidate = (UpdateTree.Entry<E>)initialResult.next();
            objectTracer.traceUpdateTreeEntry(resultCandidate, ObjectTracer.Operation.UPDATE_TREE_QUERY_INITIAL_RESULT, null);
            final Object dataKey = id(resultCandidate.getData());
            if (resultsInProgress.containsKey(dataKey)) {
                boolean handled = false;
                final Collection<UpdateTree.Entry<E>> sameIdOps = resultsInProgress.get(dataKey);
                for (final UpdateTree.Entry<E> sameIdOp : sameIdOps) {
                    if (resultCandidate.getOperationType() == sameIdOp.getOperationType().opposite()) {
                        if (resultCandidate.getData().equals(sameIdOp.getData())) {
                            sameIdOps.remove(sameIdOp);
                            handled = true;
                            break;
                        }
                    }
                }
                if (!handled)
                    sameIdOps.add(resultCandidate);
            }
            else {
                final ArrayList<UpdateTree.Entry<E>> listForKey = new ArrayList<>();
                listForKey.add(resultCandidate);
                resultsInProgress.put(dataKey, listForKey);
            }
        }

        leafNodeModifier.finalizeModifications(leafNodeModificationStats);

        while (externalResults.hasNext()) {
            final UpdateTree.Entry<E> resultCandidate = externalResults.next();
            objectTracer.traceUpdateTreeEntry(resultCandidate, ObjectTracer.Operation.UPDATE_TREE_QUERY_EXTERNAL_RESULT,
                    null);
            final Object dataKey = id(resultCandidate.getData());
            final Collection<UpdateTree.Entry<E>> sameIdOps = resultsInProgress.get(dataKey);
            if (sameIdOps == null) {
                final ArrayList<UpdateTree.Entry<E>> newIdOpList = new ArrayList<>();
                newIdOpList.add(resultCandidate);
                resultsInProgress.put(dataKey, newIdOpList);
            }
            else {
                boolean foundOpposite = false;
                final Iterator<UpdateTree.Entry<E>> sameIdOpsItr = sameIdOps.iterator();
                while (sameIdOpsItr.hasNext()) {
                    final UpdateTree.Entry<E> sameIdOp = sameIdOpsItr.next();
                    if ((resultCandidate.getOperationType().opposite() == sameIdOp.getOperationType())
                            && (resultCandidate.getData().equals(sameIdOp.getData()))) {
                        sameIdOpsItr.remove();
                        foundOpposite = true;
                        break;
                    }
                }
                if (!foundOpposite)
                    sameIdOps.add(resultCandidate);
            }
        }

        final Collection<E> finalResults = new ArrayList<>(resultsInProgress.size());
        for (final ArrayList<UpdateTree.Entry<E>> entryList : resultsInProgress.values()) {
            if (entryList.size() > 1)
                throw new IllegalStateException("Unannihilated query results: " + entryList.toString());
            for (final UpdateTree.Entry<E> entry : entryList) {
                assert entry.isInsertion();
                finalResults.add(entry.getData());
            }
        }
        return new IteratorCursor<>(finalResults.iterator());
    }
}
