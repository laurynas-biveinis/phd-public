/*
     Copyright (C) 2007, 2008, 2009, 2010, 2011, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes;

import aau.bufferedIndexes.diskTrees.*;
import aau.bufferedIndexes.diskTrees.visitors.RRDiskTreeInvariantChecker;
import aau.bufferedIndexes.diskTrees.visitors.RRDiskUpdateTreeBulkReloader;
import aau.bufferedIndexes.diskTrees.visitors.RRDiskUpdateTreeGarbageVacuumer;
import aau.bufferedIndexes.leafNodeModifiers.IRRTreeDiskNodeOnQueryModifier;
import aau.bufferedIndexes.leafNodeModifiers.LeafNodePiggybacker;
import aau.bufferedIndexes.leafNodeModifiers.NullModeModifier;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.bufferedIndexes.operationGroupMakers.AbstractOperationGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.DeletionsAsInsertionsGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.InsertionsOnlyGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.TrivialOperationGroupMaker;
import aau.bufferedIndexes.pushDownStrategies.PushDownAllGroups;
import aau.bufferedIndexes.pushDownStrategies.PushDownGroupsStrategy;
import aau.workload.DataID;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.RTree;
import xxl.core.io.Convertable;
import xxl.core.io.LRUBuffer;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

import java.io.IOException;
import java.util.*;

/**
 * A buffered R-tree implementation.  There is a buffer (internally organized as a R-tree also) All the operations
 * are buffered through it. When the operations have to go down on the tree itself, they are modified to minimize I/O
 * by operating on chunks of data.
 *
 * @see xxl.core.indexStructures.RTree
 */
public class RRTree<E extends Convertable> extends RTree implements IRRTree<E> {

    /* Various tree components and strategies of RR-tree algorithms */

    /**
     * The buffer for this tree
     */
    private RRTreeBuffer<E> buffer = null;

    /**
     * The disk tree
     */
    final private IRRDiskTree<E> diskTree;

    /**
     * A GroupSplit strategy
     */
    private final RRTreeGroupSplitter groupSplitter = new RecursiveTwoWaySplitter();

    /**
     * A GroupOperations strategy
     */
    private AbstractOperationGroupMaker operationGroupMaker = null;

    /**
     * Buffer emptying strategy
     */
    private PushDownGroupsStrategy<E> pushDownGroupsStrategy = null;

    /* Tree parameters */

    /**
     * A value of how much expand update piggybacking buffer query rectangle to each direction.
     */
    private double updatePiggybackingEpsilon = 0.0;

    /**
     * Update piggybacking above the leaf level flag
     */
    private boolean updateIndexPiggybacking = true;

    /**
     * Update piggybacking at the leaf level flag
     */
    private boolean updateLeafPiggybacking = true;

    /**
     * Query piggybacking flag
     */
    private boolean queryPiggybacking = true;

    /**
     * If <code>true</code>, then every completed insertion removes an arbitrary older entry from the leaf node
     * where it was completed
     */
    private boolean insertionRemovesOldInsertion = false;

    /**
     * Number of data items in the tree
     */
    private int dataItems = 0;

    /**
     * Size of the GC index node cache
     */
    private int gcIndexCacheSize = 0;

    private int gcInitialScratchMemSize = 0;

    private ObjectTracer<E> objectTracer = null;

    /* Internal state */

    /**
     * Flag indicating if last update operation caused buffer emptying
     */
    private boolean lastUpdateEmptiedBuffer = false;

    /**
     * Deletions completed during the current EmptyBuffer.  Used only if deletions may split.
     * TODO: rename to something like "completed operations that may multiply during EmptyBuffer"
     */
    private Set<E> completedDeletions = null;

    /**
     * If @code{true}, force the next buffer emptying to be a full one
     */
    private boolean forceFullNextEb = false;

    /* Initialization */

    /**
     * Creates a new uninitialized tree.
     *
     * @param diskTree the disk tree part of the tree to use
     */
    public RRTree(final IRRDiskTree<E> diskTree) {
        this.diskTree = diskTree;
    }

    /**
     * Initializes the RR-Tree.
     *
     * @param getId                    the function that returns the ID of a given object
     * @param getDescriptor            the new {@link xxl.core.indexStructures.Tree#getDescriptor}
     * @param container                the new {@link xxl.core.indexStructures.Tree#determineContainer}
     * @param minNodeCapacity              is used to define {@link xxl.core.indexStructures.Tree#underflows},
*                                 {@link xxl.core.indexStructures.Tree#getSplitMinRatio} and
*                                 {@link xxl.core.indexStructures.Tree#getSplitMaxRatio}.
     * @param maxNodeCapacity              is used to define {@link xxl.core.indexStructures.Tree#overflows}
     * @param maxBufferSize            the maximum size of the buffer in the number of objects
     * @param operationGroupMaker      operation grouping strategy object
     * @param updateIndexPiggybacking  flag indicating if piggybacking should be performed on updates, index nodes
     * @param updateLeafPiggybacking   flag indicating if piggybacking should be performed on updates, leaf nodes
     * @param piggybackingEpsilon      update piggybacking epsilon value
     * @param queryPiggybacking        flag indicating if piggybacking should be performed on queries
     * @param gcIndexCacheSize         size of index node cache for garbage collection
     * @param gcInitialScratchMemSize  initial scratch memory size for GC
     * @param pushDownGroupsStrategy   buffer emptying strategy object
     * @param objectTracer             the object tracer to use for the tree operations
     */
    public void initialize(final Function<E, DataID> getId, final Function<E, Descriptor> getDescriptor,
                           final Container container, final int minNodeCapacity, final int maxNodeCapacity,
                           final int maxBufferSize,
                           final AbstractOperationGroupMaker operationGroupMaker, final boolean updateIndexPiggybacking,
                           final boolean updateLeafPiggybacking, final double piggybackingEpsilon,
                           final boolean queryPiggybacking, final int gcIndexCacheSize,
                           final int gcInitialScratchMemSize, final PushDownGroupsStrategy<E> pushDownGroupsStrategy,
                           final ObjectTracer<E> objectTracer) {

        super.initialize(getDescriptor, null, 0, 0);

        this.operationGroupMaker = operationGroupMaker;
        this.updateIndexPiggybacking = updateIndexPiggybacking;
        this.updateLeafPiggybacking = updateLeafPiggybacking;
        this.updatePiggybackingEpsilon = piggybackingEpsilon;
        this.queryPiggybacking = queryPiggybacking;
        this.gcIndexCacheSize = gcIndexCacheSize;
        this.gcInitialScratchMemSize = gcInitialScratchMemSize;
        this.pushDownGroupsStrategy = pushDownGroupsStrategy;
        this.objectTracer = objectTracer;

        /* Initialize the buffer */
        buffer = new RRTreeBuffer<>();
        final Container bufferContainer = new MapContainer();
        buffer.initialize(getDescriptor, bufferContainer, minNodeCapacity, maxNodeCapacity, maxBufferSize);

        /* Initialize the disk tree */
        diskTree.initialize(getId, getDescriptor, container, minNodeCapacity, maxNodeCapacity, objectTracer);
    }

    /* Update operations */

    /**
     * Inserts an object into the buffered R-tree.
     *
     * @param data the object that is to be inserted
     * @see xxl.core.indexStructures.Tree#insert(Object,xxl.core.indexStructures.Descriptor,int)
     */
    public void insert(final Object data) {
        final int oldBufSize = maybeEmptyBuffer();
        //noinspection unchecked
        final E eData = (E)data;
        objectTracer.traceObject(eData, ObjectTracer.Operation.INSERT_TO_BUFFER);
        buffer.insertWithAnnihilation(eData);
        rrTreeStats.registerOpLifetime(eData, oldBufSize, buffer.getCurrentSize(), OperationType.INSERTION);
        dataItems++;
    }

    /**
     * Removes an object from the buffered R-tree.
     * @param data the object, equal to which an object from the tree will be removed.
     * @return the removed object or null for delayed delete
     */
    public Object remove(final Object data) {
        final int oldBufSize = maybeEmptyBuffer();
        //noinspection unchecked
        final E eData = (E)data;
        objectTracer.traceObject(eData, ObjectTracer.Operation.REMOVE_FROM_BUFFER);
        final Object result = buffer.removeWithAnnihilation(eData);
        rrTreeStats.registerOpLifetime(eData, oldBufSize, buffer.getCurrentSize(), OperationType.DELETION);
        dataItems--;
        return result;
    }

    /* Buffer operations */

    /**
     * Empty the buffer if necessary and set the last emptying flag.  Returns buffer size after emptying if the buffer
     * was emptied or just buffer size if emptying did not happen.
     * @return buffer size
     */
    private int maybeEmptyBuffer() {
        lastUpdateEmptiedBuffer = buffer.isFull();
        if (lastUpdateEmptiedBuffer) {
            emptyBuffer();
        }
        rrTreeStats.registerUpdate();
        return buffer.getCurrentSize();
    }

    /**
     * Unconditionally empty the buffer
     */
    public void forcedEmptyBuffer() {
        emptyBuffer();
    }

    /**
     * EmptyBuffer implementation
     */
    private void emptyBuffer() {
        rrTreeStats.registerEmptyBuffer();
        // TODO: cleaner!!!
        if (!diskTree.deletionsLikeInsertions() && !(operationGroupMaker instanceof InsertionsOnlyGroupMaker))
            completedDeletions = new HashSet<>();

        IRRTreeDiskNode<E> rootNode = diskTree.getRootNode();
        List<IRRTreeDiskNode<E>> siblings;
        final OperationGroup<E> bufferList = buffer.flatten();

        if (forceFullNextEb) {
            siblings = emptyEverything(rootNode, bufferList);
            forceFullNextEb = false;
        }
        else if (rootNode.level() == 0)
            // If the disk tree does not exist or is a single leaf node at this point,
            // empty the whole buffer regardless of chosen push down strategy
            siblings = emptyEverything(rootNode, bufferList);
        else {
            final IndexEntryOpGroupMap<E> bufferGroupping = operationGroupMaker.groupOperations(rootNode, bufferList);
            final OperationGroup<E> orphanGroup = bufferGroupping.get(bufferGroupping.ORPHAN_GROUP_KEY);
            if ((orphanGroup != null) && (orphanGroup.size() != 0)) {
                // TODO: test this branch
                if (orphanGroup.size() == bufferList.size()) {
                    // We got ourselves into a situation where the buffer is filled with deletions only! Just empty
                    // everything.  Might be possible to do something more optimal though.
                    // Count this as a failed emptying
                    rrTreeStats.registerFailedEmptying();
                    siblings = emptyEverything(rootNode, bufferList);
                }
                else {
                    siblings = selectAndExecuteBufferGroups(rootNode, bufferGroupping);
                }
            }
            else
                siblings = selectAndExecuteBufferGroups(rootNode, bufferGroupping);
            if (buffer.isFull()) {
                // Uh-oh. We failed to empty with the chosen strategy. Just empty everything.
                rrTreeStats.registerFailedEmptying();
                siblings = emptyEverything(rootNode, buffer.flatten());
            }
        }

        diskTree.growTree(rootNode, siblings, groupSplitter);
        if (completedDeletions != null)
            completedDeletions.clear();
        rrTreeStats.registerEndOfEmptyBuffer();

        // assert diskTreeInvariantsOK(); // TODO: bulk-load produces underful index nodes
    }

    /**
     * Selects the operation groups to execute based on the pushdown strategy, removes them from the buffer (or clears
     * the buffer and puts back the operations that will not be executed if the pushdown strategy is likely to empty a
     * big part of the buffer).  Then executes those groups on the disk tree.
     *
     * @param rootNode        the root node of the disk tree
     * @param bufferGroupping the operation groups for the root node of the disk tree
     * @return a set of new root-level nodes after executing the groups
     */
    private List<IRRTreeDiskNode<E>> selectAndExecuteBufferGroups(final IRRTreeDiskNode<E> rootNode,
                                                                  final IndexEntryOpGroupMap<E> bufferGroupping) {
        final PushDownAndBufferGroups<E> pushDownAndBufferGroups
            = pushDownGroupsStrategy.choosePushDownGroups(bufferGroupping, rootNode.level(), rootNode.number(), false);

        final IndexEntryOpGroupMap<E> pushDownGroups = pushDownAndBufferGroups.getPushDownGroups();
        final IndexEntryOpGroupMap<E> bufferGroups = pushDownAndBufferGroups.getBufferGroups();
        // The orphan group always goes to the buffer
        final OperationGroup<E> orphanGroup = pushDownGroups.get(pushDownGroups.ORPHAN_GROUP_KEY);
        if ((orphanGroup != null) && (orphanGroup.size() > 0)) {
            bufferGroups.moveGroupFrom(pushDownGroups, pushDownGroups.ORPHAN_GROUP_KEY, orphanGroup);
        }
        if (pushDownGroupsStrategy.willEmptyBigPartOfBuffer()) {
            buffer.clear();
            putOpGroupsBackToBuffer(bufferGroups, rootNode.level());
        }
        else {
            final OperationGroup<E> toBeRemoved = pushDownGroups.flatten();
            buffer.removeGroup(toBeRemoved);
        }
        return groupUpdate(rootNode, pushDownGroups);
    }

    /**
     * Performs the whole buffer emptying.
     *
     * @param rootNode       the root node of the disk tree
     * @param wholeBufferOps must be all operations in the buffer
     * @return a set of new root-level nodes after performing the operations
     */
    private List<IRRTreeDiskNode<E>> emptyEverything(final IRRTreeDiskNode<E> rootNode,
                                                     final OperationGroup<E> wholeBufferOps) {
        assert buffer.getCurrentSize() == wholeBufferOps.size();

        final AbstractOperationGroupMaker oldOperationGroupMaker = operationGroupMaker;
        final PushDownGroupsStrategy<E> oldPushDownGroupsStrategy = pushDownGroupsStrategy;
        pushDownGroupsStrategy = new PushDownAllGroups<>();
        // TODO: redesign!!!
        if (!(operationGroupMaker instanceof DeletionsAsInsertionsGroupMaker)) {
            operationGroupMaker = new TrivialOperationGroupMaker();
            if (completedDeletions != null)
                completedDeletions.clear();
            completedDeletions = new HashSet<>();
        }
        buffer.clear();
        final List<IRRTreeDiskNode<E>> results = groupUpdate(rootNode, wholeBufferOps, false, false);
        pushDownGroupsStrategy = oldPushDownGroupsStrategy;
        operationGroupMaker = oldOperationGroupMaker;
        if (completedDeletions != null) {
            completedDeletions.clear();
            completedDeletions = null;
        }
        return results;
    }

    /**
     * Puts the non-performed operations back to the buffer.
     * 
     * @param backToBufferGroups list of groups to be put back
     * @param treeLevel the disk tree level the groups have reached
     */
    @SuppressWarnings({"TypeMayBeWeakened"})
    private void putOpGroupsBackToBuffer(final IndexEntryOpGroupMap<E> backToBufferGroups, final int treeLevel) {
        for (final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> opGroup: backToBufferGroups) {
            putOpGroupBackToBuffer(treeLevel, opGroup.getValue());
        }
    }

    // TODO: javadoc
    private void putOpGroupBackToBuffer(final int treeLevel, final OperationGroup<E> opGroup) {
        rrTreeStats.registerBackToBufferGroup (treeLevel, opGroup.size());
        for (final UpdateTree.Entry<E> op : opGroup) {
            putOpBackToBuffer(treeLevel, op);
        }
    }

    // TODO: javadoc
    private void putOpBackToBuffer(final int treeLevel, final UpdateTree.Entry<E> op) {
        objectTracer.traceUpdateTreeEntry(op, ObjectTracer.Operation.PUT_OP_BACK_TO_BUFFER, null);

        assert (completedDeletions == null) || op.isInsertion() || !completedDeletions.contains(op.getData());
        assert rrTreeStats.hasUpdateLifetime(op);
        rrTreeStats.registerBackToBufferOperation (treeLevel, op);

        if ((completedDeletions != null) && op.isDeletion()) {
            buffer.addEntryIfNotExists(op);
        }
        else {
            assert !buffer.queryEntry(op).hasNext();
            buffer.insertEntry(op);
        }
    }

    /* Disk tree update operations */

    /**
     * A version of groupUpdate that is only called from EmptyBuffer.  Does not update group size statistics as groups
     * between the buffer and the root node do not cause I/O.
     *
     * @param node the disk tree root node
     * @param operationMap the operation mapping between disk tree root node entries and operations to execute
     * @return list of updated nodes
     */
    private List<IRRTreeDiskNode<E>> groupUpdate(final IRRTreeDiskNode<E> node, final IndexEntryOpGroupMap<E> operationMap) {
        rrTreeStats.registerGroupUpdate();
        if (node.level() == 0) {
            updateLeafNode(operationMap.flatten(), node);
        }
        else if (updateNonLeafNode(operationMap, node)) {
            rrTreeStats.registerGroupUpdateRestart();
            return groupUpdate(node, operationMap.flatten(), false, true); // ??? updateGroupStats was true
        }
        return finishGroupUpdate(node);
    }

    /**
     * The GroupUpdate algorithm implementation.
     *
     * @param node the disk tree node to execute operations on
     * @param operations the collection of operations to execute on this node.
     * @param updateGroupStats flag if group size statistics should be updated.  Should be <code>false</code> when the
     * groups do not cause real I/O, i.e. between the buffer and the disk tree root node.
     * @param restarted flag if this a restarted invocation
     * @return list of updated nodes
     */
    private List<IRRTreeDiskNode<E>> groupUpdate(final IRRTreeDiskNode<E> node, final OperationGroup<E> operations,
                                                 final boolean updateGroupStats, final boolean restarted) {
        rrTreeStats.registerGroupUpdate();
        final int originalOpListSize = operations.size();
        final boolean originalOpListIsInsertionOnly = operations.isInsertionOnly();

        for (final UpdateTree.Entry<E> op : operations)
            objectTracer.traceUpdateTreeEntry(op, ObjectTracer.Operation.GROUP_UPDATE_START, null);

        // Remove already-completed deletions if applicable
        if (completedDeletions != null) {
            final Iterator<UpdateTree.Entry<E>> opItr = operations.iterator();
            while (opItr.hasNext()) {
                final UpdateTree.Entry<E> op = opItr.next();
                if (op.isDeletion() && completedDeletions.contains(op.getData()))
                    opItr.remove();
            }
        }

        if (node.level() == 0) {
            updateLeafNode(operations, node);
        }
        else {
            final IndexEntryOpGroupMap<E> groups = operationGroupMaker.groupOperations(node, operations);

            // Put back to buffer operations that cannot proceed further
            final OperationGroup<E> orphans = groups.get(groups.ORPHAN_GROUP_KEY);
            if (orphans != null)
                putOpGroupBackToBuffer(node.level(), orphans);
            groups.remove(groups.ORPHAN_GROUP_KEY);
                       
            final PushDownAndBufferGroups<E> pushDownAndBufferGroups
                    = pushDownGroupsStrategy.choosePushDownGroups(groups, node.level(), node.number(), restarted);
            final IndexEntryOpGroupMap<E> pushDownGroups = pushDownAndBufferGroups.getPushDownGroups();
            final IndexEntryOpGroupMap<E> backToBufferGroups = pushDownAndBufferGroups.getBufferGroups();
            putOpGroupsBackToBuffer(backToBufferGroups, node.level());

            if (updateNonLeafNode(pushDownGroups, node)) {
                rrTreeStats.registerGroupUpdateRestart();
                return groupUpdate(node, pushDownGroups.flatten(), false, true);
            }
        }
        if (updateGroupStats)
            rrTreeStats.updateGroupUpdateStatistics(node.level(), originalOpListSize, originalOpListIsInsertionOnly);
        return finishGroupUpdate(node);
    }

    private List<IRRTreeDiskNode<E>> finishGroupUpdate(final IRRTreeDiskNode<E> node) {
        IRRTreeDiskNode<E> nodeToSplit = node;
        if ((node.number() == 1) && (node.level() > 0)) {
            rrTreeStats.registerSingleEntryNode();
            final IRRTreeIndexEntry<E> childEntry = node.getNonLeafNodeEntries().iterator().next();
            nodeToSplit = childEntry.get();
        }
        return groupSplitter.groupSplit(nodeToSplit, diskTree);
    }

    /**
     * Dispatches down groups of operations among node entries. Handles result integration
     * @param pushDownGroups the groups of operations to execute
     * @param node the node to execute operations on
     * @return <code>true</code> if parent GroupUpdate restart is required
     */
    private boolean updateNonLeafNode(final IndexEntryOpGroupMap<E> pushDownGroups, final IRRTreeDiskNode<E> node) {
        rrTreeStats.registerNonLeafNodeUpdate();
        boolean regroupRemaining = false;
        final Iterator<Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>>> groupIterator = pushDownGroups.iterator();
        while (groupIterator.hasNext()) {
            if (regroupRemaining)
                return true;

            final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> opGroup = groupIterator.next();
            groupIterator.remove();

            final IRRTreeIndexEntry<E> child = opGroup.getKey();
            final IRRTreeDiskNode<E> childNode = child.get();
            node.remove(child);

            final OperationGroup<E> opGroupEntries = opGroup.getValue();

            for (UpdateTree.Entry<E> op : opGroupEntries)
                objectTracer.traceUpdateTreeEntry(op, ObjectTracer.Operation.GROUP_UPDATE_BEFORE_INDEX_PIGGYBACKING, null);

            if (updateIndexPiggybacking)
                performIndexNodePiggybacking(opGroupEntries);

            for (UpdateTree.Entry<E> op : opGroupEntries)
                objectTracer.traceUpdateTreeEntry(op, ObjectTracer.Operation.GROUP_UPDATE_AFTER_INDEX_PIGGYBACKING, null);

            final List<IRRTreeDiskNode<E>> newChildren = groupUpdate(childNode, opGroupEntries, true, false);
            if (newChildren.size() > 1) {
                rrTreeStats.registerTrivialChildIntegration();
                addNewChildren(node, newChildren, child, childNode);
            }
            else {
                regroupRemaining = integrateChild(newChildren.get(0), node, child);
            }
        }
        return false;
    }

    private void performIndexNodePiggybacking(OperationGroup<E> opGroupEntries) {
        final Descriptor opGroupMBR = computeMBR (opGroupEntries);
        if (opGroupMBR == null)
            return;
        final Descriptor expandedOpGroupMBR = updatePiggybackingEpsilon > 0.0 ?
                expandDescriptor(opGroupMBR, updatePiggybackingEpsilon) : opGroupMBR;
        final Cursor<UpdateTree.Entry<E>> piggybackers = diskTree.deletionsLikeInsertions() ?
                buffer.copyQueryAllOps(expandedOpGroupMBR) : buffer.copyQueryInsertions(expandedOpGroupMBR);
        while (piggybackers.hasNext()) {
            final UpdateTree.Entry<E> entry = piggybackers.next();
            objectTracer.traceUpdateTreeEntry(entry, ObjectTracer.Operation.INDEX_NODE_PIGGYBACKING, null);
            assert !opGroupEntries.contains(entry);
            rrTreeStats.getNonleafUpdatePiggybackings().register(entry);
            opGroupEntries.add(entry);
            final UpdateTree.Entry<E> removedEntry = buffer.removeExactEntry(entry);
            assert removedEntry != null;
        }
    }

    private boolean integrateChild(final IRRTreeDiskNode<E> newChild, final IRRTreeDiskNode<E> node,
                                   final IRRTreeIndexEntry<E> child) {
        rrTreeStats.registerIntegrateChildInvocation();
        if (newChild.number() == 0) {
            rrTreeStats.registerEmptyChildrenSet();
            return false;
        }
        if (node.number() == 0) {
            rrTreeStats.registerChildReplacingParent();
            child.remove();
            node.setLevel(newChild.level());
            node.addEntriesFrom(newChild);
            return false;
        }
        if (newChild.underflows()) {
            child.remove();
            return mergeSubtree(node, newChild);
        }
        final IRRTreeIndexEntry<E> newChildEntry = diskTree.takeOverNode(newChild, child, false);
        return insertSubtree(node, newChildEntry);
    }

    public void setInsertionRemovesOldInsertion() {
        insertionRemovesOldInsertion = true;
    }

    private void updateLeafNode(final OperationGroup<E> operations, final IRRTreeDiskNode<E> node) {
        assert node.level() == 0;
        rrTreeStats.registerLeafNodeUpdate ();

        for (final UpdateTree.Entry<E> op : operations)
            objectTracer.traceUpdateTreeEntry(op, ObjectTracer.Operation.UPDATE_LEAF_NODE, null);

        final Collection<UpdateTree.Entry<E>> completedOperations
                = node.executeOps(operations, insertionRemovesOldInsertion);

        for (final UpdateTree.Entry<E> op : completedOperations) {
            completeOperation(op);
            boolean result = operations.remove(op);
            assert result;
        }

        for (final UpdateTree.Entry<E> op : operations) {
            putOpBackToBuffer(0, op);
        }

        operations.clear();

        if (updateLeafPiggybacking && (node.number() > 0)) { // TODO: can use index entry MBR if node.number() == 0
            final IRRTreeDiskNodeOnQueryModifier<E> piggybacker
                    = new LeafNodePiggybacker<>(this, buffer, rrTreeStats, objectTracer);
            piggybacker.modify(node, true, updatePiggybackingEpsilon, new RRTreeLeafPiggybackingInfo());
            piggybacker.finalizeModifications(rrTreeStats.getLeafUpdatePiggybackings());
        }
    }

    /**
     * Inserts a subtree to a main tree node.  Goes down the main tree (using R-tree ChooseSubtree algorithm) until it
     * finds the right level in the main tree to insert the subtree.  The right level is the one where distance to the
     * leaf level is equal to the subtree height.  There, it adds the entry for subtree to the main tree node.
     *
     * @param node  the main tree node, to which (or below which) a subtree should be inserted.
     * @param subtree  an entry for the subtree.
     * @return a flag indicating that remaining operations should be regrouped
     */
    private boolean insertSubtree(final IRRTreeDiskNode<E> node, final IRRTreeIndexEntry<E> subtree) {
        assert(subtree.level() + 1 <= node.level());
        rrTreeStats.registerInsertSubtreeInvocation();

        if (subtree.level() + 1 == node.level()) {
            node.grow(subtree);
        }
        else {
            rrTreeStats.registerInsertSubtreeRecursion();
            final IRRTreeIndexEntry<E> childForEntry = node.chooseSubtreeByObject(subtree);
            final IRRTreeDiskNode<E> childNode = childForEntry.get();
            node.remove(childForEntry);
            insertSubtree(childNode, subtree);
            final List <IRRTreeDiskNode<E>> resultNodes = groupSplitter.groupSplit(childNode, diskTree);
            addNewChildren(node, resultNodes, childForEntry, childNode);
            if (resultNodes.size() > 1)
                return true;
        }
        return false;
    }

    /**
     * Merges a subtree with a underfull root node to a main tree node.  Goes down the main tree (using R-tree
     * ChooseSubtree algorithm) until it finds the right level in the main tree to merge the subtree.  The right level
     * is the one where distance to the leaf level is equal to the subtree height.  Since an entry for the underfull
     * subtree root cannot be inserted into the main tree, ChooseSubtree is called once again for the main tree and all
     * the entries from the subtree root are inserted into the returned main tree node.  The resulting child node is
     * split if overflowing and resulting nodes are inserted to the parent node.  Overflow of the parent node is
     * handled by the caller.
     *
     * @param node node the main tree node, below which a subtree should be merged.
     * @param subtree an entry for the subtree.
     * @return a flag indicating that remaining operations should be regrouped
     */
    private boolean mergeSubtree(final IRRTreeDiskNode<E> node, final IRRTreeDiskNode<E> subtree) {
        assert(subtree.level() + 1 <= node.level());
        assert((subtree.number() > 1) || (subtree.level() == 0));
        rrTreeStats.registerMergeSubtreeInvocation();

        final Descriptor subtreeDescriptor = node.computeDescriptor();
        final IRRTreeIndexEntry<E> childForEntry = node.chooseSubtreeByObject(subtreeDescriptor);
        final IRRTreeDiskNode<E> childForEntryNode = childForEntry.get();

        // Cannot happen? Well it did happen!
        assert(!childForEntryNode.equals(subtree));

        node.remove(childForEntry);

        if (subtree.level() + 1 == node.level()) {
            childForEntryNode.addEntriesFrom(subtree);
        }
        else {
            rrTreeStats.registerMergeSubtreeRecursion();
            mergeSubtree(childForEntryNode, subtree);
        }

        final List<IRRTreeDiskNode<E>> resultNodes = groupSplitter.groupSplit(childForEntryNode, diskTree);
        addNewChildren(node, resultNodes, childForEntry, childForEntryNode);
        return resultNodes.size() > 1;
    }

    /* Queries */

    public boolean getQueryPiggybackingState() {
        return queryPiggybacking;
    }

    public void setQueryPiggybackingState(final boolean newPiggybackingState) {
        queryPiggybacking = newPiggybackingState;
    }

    /**
     * This is the implementation of query algorithm.
     *
     * @param queryDescriptor describes the query in terms of a descriptor
     * @param targetLevel     the tree-level to provide the answer-objects
     * @return a lazy cursor pointing to all response objects
     */
    public Cursor<E> query(final Descriptor queryDescriptor, final int targetLevel) {
        if (targetLevel != 0)
            throw new IllegalArgumentException("Only leaf level searching is supported");

        final IRRTreeDiskNodeOnQueryModifier<E> piggybacker
                = queryPiggybacking
                    ? new LeafNodePiggybacker<>(this, buffer, rrTreeStats, objectTracer)
                    : new NullModeModifier<E>();

        final Cursor<UpdateTree.Entry<E>> bufferCursor
                = buffer.queryEntryOfAnyType(queryDescriptor);
        // TODO: split rrQuery into two, do the piggybacking stat accounting here

        return diskTree.rrQuery(queryDescriptor, bufferCursor,
                piggybacker, rrTreeStats.getQueryPiggybackings());
    }

    /**
     * Completes an operation.  Finalizes its statistics and removes it from buffer if it is still there.
     *
     * @param op an operation that has been completed
     */
    public void completeOperation(final UpdateTree.Entry<E> op) {
        objectTracer.traceUpdateTreeEntry(op, ObjectTracer.Operation.COMPLETE_OPERATION, null);
        // TODO: refactor to make it testable
        rrTreeStats.completeUpdateLifetime(op);
        if ((completedDeletions != null) && op.isDeletion()) {
            final boolean result = completedDeletions.add(op.getData());
            assert result;
            buffer.removeExactEntry(op);
        }
    }

    /* Disk I/O operations */

    /* Tree parameter getters */

    /**
     * Returns flag of last update operation causing buffer emptying
     * @return <code>true</code> if the last operation caused the buffer to be emptied, <code>false</code> otherwise.
     */
    public boolean wasBufferEmptied() {
        return lastUpdateEmptiedBuffer;
    }

    /* Various public methods */

    private final AggregateStats partialUnloadCreatedNodeStat = new AggregateStats();

    private final AggregateStats partialUnloadRatioStat = new AggregateStats();

    private final AggregateStats partOfNodesCleanedWithUnloadStat = new AggregateStats();

    private final AggregateStats partOfTreeCleanedWithUnloadStat = new AggregateStats();

    private final AggregateStats unloadedEntriesStat = new AggregateStats();

    private final AggregateStats partialCleaningIterationsStat = new AggregateStats();

    private final AggregateStats indexNodesBeforeGCStat = new AggregateStats();

    private final AggregateStats leafNodesBeforeGCStat = new AggregateStats();

    private final AggregateStats indexNodesAfterGCStat = new AggregateStats();

    private final AggregateStats leafNodesAfterGCStat = new AggregateStats();

    private final AggregateStats hilbertFileWriteStat = new AggregateStats();

    private final AggregateStats hilbertFileReadStat = new AggregateStats();

    /**
     * Cleans garbage from the tree if applicable.
     */
    public TreeClearIOState cleanGarbage(boolean rebuildTree) throws IOException {
        // TODO: any TODOs here, eh?
        if (!(getDiskTree() instanceof IRRDiskUpdateTree))
            return getDiskTree().cleanGarbage();
        final IRRDiskUpdateTree<E> diskUpdateTree = (IRRDiskUpdateTree<E>)getDiskTree();
        // TODO: calculate this value instead of passing it
        final LRUBuffer indexNodeBuffer = new LRUBuffer(gcIndexCacheSize);
        final Container oldDiskContainer = diskUpdateTree.container();
        final BufferedContainer indexNodeContainer
                = gcIndexCacheSize > 0 ? new BufferedContainer(oldDiskContainer, indexNodeBuffer) : null;
        // TODO: the strategy should be passed here
        if (rebuildTree) {
            final RRDiskUpdateTreeBulkReloader<E> treeRebuilder
                    = new RRDiskUpdateTreeBulkReloader<>(diskUpdateTree, gcInitialScratchMemSize, objectTracer);
            diskUpdateTree.visitTreeNodes(indexNodeContainer, treeRebuilder);
            indexNodesBeforeGCStat.registerValue(treeRebuilder.oldTreeIndexCount());
            leafNodesBeforeGCStat.registerValue(treeRebuilder.oldTreeLeafCount());
            hilbertFileReadStat.registerValue(treeRebuilder.getHilbertReadIO());
            hilbertFileWriteStat.registerValue(treeRebuilder.getHilbertWriteIO());
            indexNodesAfterGCStat.registerValue(treeRebuilder.newTreeIndexCount());
            leafNodesAfterGCStat.registerValue(treeRebuilder.newTreeLeafCount());
            return treeRebuilder.getOldTreeClearingIO();
        }
        else {
            final RRDiskUpdateTreeGarbageVacuumer<E> garbageVacuumer
                    = new RRDiskUpdateTreeGarbageVacuumer<>(diskUpdateTree, buffer, indexNodeContainer);
            diskUpdateTree.visitTreeNodes(indexNodeContainer, garbageVacuumer);
            if (garbageVacuumer.getPeakNodesLoadedAtOnce() > peakNodesLoadedAtOnce)
                peakNodesLoadedAtOnce = garbageVacuumer.getPeakNodesLoadedAtOnce();
            return new TreeClearIOState(0, 0);
        }
    }
    
    public double getGcAvgPartialUnloadCreatedNodes() {
        return partialUnloadCreatedNodeStat.average();
    }

    public double getGcAvgPartialUnloadRatio() {
        return partialUnloadRatioStat.average();
    }

    public double getGcAvgPartOfNodesCleanedWithUnload() {
        return partOfNodesCleanedWithUnloadStat.average();
    }

    public double getGcAvgPartOfTreeCleanedWithUnload() {
        return partOfTreeCleanedWithUnloadStat.average();
    }

    public double getGcAvgUnloadedEntries() {
        return unloadedEntriesStat.average();
    }

    public double getGcAvgPartialCleaningIterations() {
        return partialCleaningIterationsStat.average();
    }

    public double getAvgIndexNodesBeforeGc() {
        return indexNodesBeforeGCStat.average();
    }

    public double getAvgLeafNodesBeforeGc() {
        return leafNodesBeforeGCStat.average();
    }

    public double getAvgIndexNodesAfterGc() {
        return indexNodesAfterGCStat.average();
    }

    public double getAvgLeafNodesAfterGc() {
        return leafNodesAfterGCStat.average();
    }

    public double getAvgHilbertWriteIOs() {
        return hilbertFileWriteStat.average();
    }

    public double getAvgHilbertReadIOs() {
        return hilbertFileReadStat.average();
    }

    public int getTotalHilbertWriteIOs() {
        return hilbertFileWriteStat.totalInt();
    }

    public int getTotalHilbertReadIOs() {
        return hilbertFileReadStat.totalInt();
    }

    /**
     * Return MBR of the whole tree
     * @return the MBR of the whole tree
     */
    public Descriptor rootDescriptor() {
        if (diskTree.rootDescriptor() != null) {
            final Descriptor result = (Descriptor)diskTree.rootDescriptor().clone();
            if (buffer.rootDescriptor() != null)
                result.union(buffer.rootDescriptor());
            return result;
        }
        if (buffer.rootDescriptor() != null)
            return (Descriptor)buffer.rootDescriptor().clone();
        return null;
    }

    /**
     * Makes the next EmptyBuffer to do the full buffer emptying regardless of the default emptying strategy.
     */
    public void onNextEbForceFullEmptying() {
        forceFullNextEb = true;
    }

    /**
     * Computes MBR of an operation group
     * @param opGroup the operation group
     * @return the MBR
     */
    private Descriptor computeMBR (final OperationGroup<E> opGroup) {
        if (opGroup.size() == 0)
            return null;
        final Iterator<UpdateTree.Entry<E>> entries = opGroup.iterator();
        final Descriptor result = (Descriptor)diskTree.descriptor(entries.next()).clone();
        while (entries.hasNext()) {
            result.union(diskTree.descriptor(entries.next()));
        }
        return result;
    }

    private void addNewChildren(final IRRTreeDiskNode<E> node, final Iterable<IRRTreeDiskNode<E>> newChildren,
                                final IRRTreeIndexEntry<E> originalEntry, final IRRTreeDiskNode<E> originalNode) {
        final List<IRRTreeIndexEntry<E>> newEntries = diskTree.storeNodes(newChildren, originalEntry, originalNode);
        for (final IRRTreeIndexEntry<E> newChild: newEntries) {
            assert(node.level() == newChild.level() + 1);
            node.grow(newChild);
        }
    }

    /* Statistics */

    /**
     * The main statistics object
     */
    private final RRTreeStats<E> rrTreeStats = new RRTreeStats<>();

    /**
     * Record operation lifetime stats for all the ops currently in the buffer. To be called at the end of test run.
     */
    public void registerBufferLifetimes() {
        rrTreeStats.registerBufferLifetimes();
    }

    /**
     * Returns number of operations currently in the buffer.
     *
     * @return number of operations
     */
    public int getCurrentBufferSize() {
        return buffer.getCurrentSize();
    }

    /**
     * Returns copy of most RR-tree statistics
     * @return RR-tree statistics
     */
    public RRTreeStats<E> getStats() {
        try {
            //noinspection unchecked
            return (RRTreeStats<E>)rrTreeStats.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException (e);
        }
    }

    /**
     * Returns statistics on deletion splits happenned during buffer emptying
     * @return statistics on deletion splits happenned during buffer emptying
     */
    public StatisticalData getDeletionSplits() {
        return operationGroupMaker.getDeletionSplits();
    }

    /**
     * Returns number of times insertions annihilated deletions
     * @return number of times insertions annihilated deletions
     */
    public int getNumOfIDAnnihilations() {
        return buffer.getNumOfIDAnnihilations();
    }

    /**
     * Returns number of times deletions annhihilated insertions
     * @return number of times deletions annihilated insertions
     */
    public int getNumOfDIAnnihilations() {
        return buffer.getNumOfDIAnnihilations();
    }

    /**
     * The peak number of leaf nodes that were loaded at the same time during the garbage collection.
     */
    private int peakNodesLoadedAtOnce = 0;

    /**
     * Gets the peak number of leaf nodes that were loaded at the same time during the garbage collection.
     *
     * @return the peak number of leaf nodes that were loaded at the same time during the garbage collection
     */
    public int getPeakNodesLoadedAtOnceDuringGC() {
        return peakNodesLoadedAtOnce;
    }

    /**
     * Returns the physical to logical data item ratio for this tree.  Physical data items are the ones stored on the
     * disk, including non-annihilated deletions and insertions.  Thus this function returns the value of how much this
     * tree is larger than a "perfect" compact tree, disregarding data stored in the buffer.
     *
     * @return the physical to logical data item ratio for this tree, or <code>Float.MAX_VALUE</code> if number of
     * logical data items is zero.
     */
    public float getPhysicalToLogicalDataRatio() {
        if (dataItems == 0)
            return Float.MAX_VALUE;
        return (float)diskTree.getDataItems() / dataItems;
    }

    public IRRDiskTree<E> getDiskTree() {
        // TODO: minimize its use
        return diskTree;
    }

    /* Misc */

    @SuppressWarnings({"UnusedDeclaration"})
    private boolean diskTreeInvariantsOK() throws IOException {
        diskTree.visitTreeNodes(null, new RRDiskTreeInvariantChecker<E>());
        return true;
    }

    public static Descriptor expandDescriptor(final Descriptor descriptor, final double epsilon) {
        // TODO: Descriptor interface does not contain enough methods to expand it.
        assert descriptor instanceof DoublePointRectangle;
        final Rectangle rectangle = (Rectangle)descriptor;
        double[] lengths = rectangle.deltas();
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] += lengths[i] * epsilon;
        }
        double center[] = new double[lengths.length];
        for (int i = 0; i < center.length; i++) {
            center[i] = Math.abs(rectangle.getCorner(true).getValue(i) + rectangle.getCorner(false).getValue(i)) / 2;
        }
        double c1[] = new double[lengths.length];
        double c2[] = new double[lengths.length];
        for (int i = 0; i < c1.length; i++) {
            c1[i] = center[i] - lengths[i] / 2;
            c2[i] = center[i] + lengths[i] / 2;
        }
        return new DoublePointRectangle(c1, c2);
    }
}
