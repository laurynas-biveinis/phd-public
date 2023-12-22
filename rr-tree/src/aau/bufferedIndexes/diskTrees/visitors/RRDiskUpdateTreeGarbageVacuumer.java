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
package aau.bufferedIndexes.diskTrees.visitors;

import aau.bufferedIndexes.IRRTreeBuffer;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRDiskUpdateTree;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskUpdateNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.collections.containers.Container;
import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

import java.util.*;

/**
 * A garbage vacuumer for the RRDiskUpdateTree.
 */
public class RRDiskUpdateTreeGarbageVacuumer<E extends Convertable> extends RRDiskUpdateTreeGarbageCleaner<E> {

    /**
     * The disk nodes that have been loaded but are not fully cleaned yet.
     */
    private final Map<Object, IRRTreeDiskUpdateNode<E>> leavesInProgress
            = new HashMap<>();
    /**
     * The peak number of nodes loaded at once.
     */
    private int peakNodesLoadedAtOnce = 0;

    /**
     * Creates a new disk update tree garbage vacuumer.
     *
     * @param tree                the tree to clean
     * @param buffer              the buffer to piggyback operations from as we clean
     * @param indexNodeContainer  a intermediate buffering container for the index nodes  
     */
    public RRDiskUpdateTreeGarbageVacuumer(final IRRDiskUpdateTree<E> tree, final IRRTreeBuffer<E> buffer,
                                           final Container indexNodeContainer) {
        super(tree, buffer, indexNodeContainer);
    }

    protected void cleanNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> leafNodeEntry,
                             final Object leafID, final Iterable<Object> intersectingNodeIDs,
                             final Cursor<UpdateTree.Entry<E>> matchingBufferOps, final Descriptor cleaningArea) {
        final IRRTreeDiskUpdateNode<E> nodeToClean = ensureNodeIsLoaded(leafID);
        for (Object intersectingNodeID : intersectingNodeIDs) {
            if (intersectingNodeID.equals(leafID)) // TODO: a failing test with ==
                continue;
            final IRRTreeDiskUpdateNode<E> intersectingNode = ensureNodeIsLoaded(intersectingNodeID);
            assert intersectingNode != nodeToClean;
            final Iterator<UpdateTree.Entry<E>> it = intersectingNode.getLeafNodeEntries().iterator();
            while (it.hasNext()) {
                final UpdateTree.Entry<E> entry = it.next();
                final UpdateTree.Entry<E> dupEntry = getLeafNodeDataById(nodeToClean, tree.id(entry.getData()));
                if (dupEntry != null && entry.getOperationType() == dupEntry.getOperationType().opposite()
                        && entry.getData().equals(dupEntry.getData())) {
                    nodeToClean.getLeafNodeEntries().remove(dupEntry);
                    nodeToClean.dataItemRemoved();
                    it.remove();
                    intersectingNode.dataItemRemoved();
                }
            }
        }
        performBufferOps(nodeToClean, matchingBufferOps);
        leafNodeEntry.update(nodeToClean);
        leavesInProgress.remove(leafID);        
    }

    @SuppressWarnings({"TypeMayBeWeakened"})
    private void performBufferOps(final IRRTreeDiskUpdateNode<E> node,
                                  final Cursor<UpdateTree.Entry<E>> matchingBufferOpCursor) {
        if (node.number() == 0)
            return;
        final Collection<UpdateTree.Entry<E>> matchingBufferOps = new ArrayList<>();
        while (matchingBufferOpCursor.hasNext()) {
            matchingBufferOps.add(matchingBufferOpCursor.next());
        }
        matchingBufferOpCursor.close();

        final ArrayList<UpdateTree.Entry<E>> insertionsToExecute
                = new ArrayList<>(matchingBufferOps.size());
        node.indexEntries();
        for (final UpdateTree.Entry<E> matchingBufferOp : matchingBufferOps) {
            if (!node.operationWillIncreaseNodeSize(matchingBufferOp)) {
                node.getLeafNodeEntries().remove(matchingBufferOp.makeOpposite());
                node.dataItemRemoved();
                buffer.removeExactEntry(matchingBufferOp);
            }
            else if (matchingBufferOp.isInsertion())
                insertionsToExecute.add(matchingBufferOp);
        }
        node.deleteEntryIndex();

        for (final UpdateTree.Entry<E> insertion : insertionsToExecute) {
            if (node.number() == tree.getMaxNodeCapacity())
                break;
            node.grow(insertion);
            node.dataItemAdded();
            buffer.removeExactEntry(insertion);
        }
    }

    /**
     * Returns a leaf node data element with a specified id.
     *
     * @param node a node to search in
     * @param id the id of the data element to find
     * @return the data entry with the same id, or <code>null</code> if no entry with such id exists in the node
     */
    private UpdateTree.Entry<E> getLeafNodeDataById(final IRRTreeDiskUpdateNode<E> node, final Object id) {
        // TODO: probably belongs to the Node class itself
        // TODO: bad big-O
        for (UpdateTree.Entry<E> entry : node.getLeafNodeEntries()) {
            if (tree.id(entry.getData()).equals(id))
                return entry;
        }
        return null;
    }

    /**
     * Loads and caches or returns from the cache the requested node.
     *
     * @param nodeId the node id to load
     * @return the loaded node
     */
    IRRTreeDiskUpdateNode<E> ensureNodeIsLoaded(final Object nodeId) {
        if (leavesInProgress.containsKey(nodeId))
            return leavesInProgress.get(nodeId);
        //noinspection unchecked
        final IRRTreeDiskUpdateNode<E> result = (IRRTreeDiskUpdateNode<E>)(tree.container()).get(nodeId);
        leavesInProgress.put(nodeId, result);
        if (leavesInProgress.size() > peakNodesLoadedAtOnce)
            peakNodesLoadedAtOnce = leavesInProgress.size();
        return result;
    }

    /**
     * Clears the auxiliary data structures after garbage cleaning is complete.
     */
    public void finishVisiting() {
        leavesInProgress.clear();
        super.finishVisiting();
    }

    /**
     * Returns the peak number of simultaneously loaded disk tree nodes.
     *
     * @return the peak number of simultaneously loaded disk tree nodes
     */
    public int getPeakNodesLoadedAtOnce() {
        return peakNodesLoadedAtOnce;
    }

}
