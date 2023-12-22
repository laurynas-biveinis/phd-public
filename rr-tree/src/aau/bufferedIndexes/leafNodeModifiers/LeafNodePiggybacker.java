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
package aau.bufferedIndexes.leafNodeModifiers;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

import java.util.Collection;
import java.util.HashSet;

/**
 * The RR-tree query-time leaf node modifier that performs piggybacking.
 */
public class LeafNodePiggybacker<E extends Convertable> implements IRRTreeDiskNodeOnQueryModifier<E> {

    /**
     * The tree where everything happens
     */
    final private IRRTree<E> tree;

    /**
     * The buffer of the tree
     */
    final private IRRTreeBuffer<E> buffer;

    /**
     * The tree statistics object
     */
    final private RRTreeStats<E> rrTreeStats;

    /**
     * The object tracer to use for piggybacked operations
     */
    final private ObjectTracer<E> objectTracer;

    /**
     * All operations that were piggybacked by an instance
     */
    final private Collection<UpdateTree.Entry<E>> piggybackedOps = new HashSet<>();

    /**
     * Creates a new leaf node piggybacker.
     *
     * @param tree         the tree where we are going to piggyback
     * @param buffer       the buffer of the tree
     * @param rrTreeStats  the statistics of the tree
     * @param objectTracer the object tracer to use during piggybacking
     */
    public LeafNodePiggybacker(final IRRTree<E> tree, final IRRTreeBuffer<E> buffer, final RRTreeStats<E> rrTreeStats,
                               final ObjectTracer<E> objectTracer) {
        this.tree = tree;
        this.buffer = buffer;
        this.rrTreeStats = rrTreeStats;
        this.objectTracer = objectTracer;
    }

    /**
     * Perform the applicable operations from the buffer on a leaf-level node.  If used at update time, it is
     * best to run after normal operations have been processed, to ensure proper statistics.
     *
     * @param node node on which the operations should be performed
     * @param allowReorganization <code>true</code> if the node is allowed to under- or overflow as a result of
     * performing the operations
     * @param epsilon how much expand the buffer query MBR in each direction
     * @param piggybackingInfo     a piggybacking info object to carry info about this particular piggybacking
     * @return <code>true</code> if any operations were piggybacked.
     */
    public boolean modify(final IRRTreeDiskNode<E> node, final boolean allowReorganization,
                          final double epsilon, final IRRTreeLeafPiggybackingInfo piggybackingInfo) {
        final Descriptor descriptor = node.computeDescriptor();
        if (descriptor == null)
            return false;    

        final Descriptor expandedDescriptor = (allowReorganization && epsilon > 0.0)
                ? RRTree.expandDescriptor(descriptor, epsilon) : descriptor;
        final Cursor<UpdateTree.Entry<E>> piggybackers = buffer.queryEntryOfAnyType(expandedDescriptor);
        final Collection<UpdateTree.Entry<E>> refinedCandidateSet
                = node.selectFittingOperations(piggybackers, epsilon > 0.0);

        refinedCandidateSet.removeAll(piggybackedOps);

        // TODO: assert here that no operations in the refinedCandidateSet annihilate with each other

        node.indexEntries();

        for (UpdateTree.Entry<E> op : refinedCandidateSet) {
            objectTracer.traceUpdateTreeEntry(op, ObjectTracer.Operation.LEAF_NODE_PIGGYBACKING, null);
            if (node.operationWillIncreaseNodeSize(op))
                piggybackingInfo.addPotentialSizeIncreasingOp();
            else
                piggybackingInfo.addPotentialSizeDecreasingOp();
        }

        if (!allowReorganization) {
            node.limitNumberOfOperations(piggybackingInfo);
            rrTreeStats.registerNonPiggybackedNodeSizeDecreasingOps(piggybackingInfo.getUnpiggybackableSizeDecreasingOps());
            rrTreeStats.registerNonPiggybackedNodeSizeIncreasingOps(piggybackingInfo.getUnpiggybackableSizeIncreasingOps());
        }

        if (!piggybackingInfo.isNodeChanged()) {
            node.deleteEntryIndex();
            return false;
        }

        final Collection<UpdateTree.Entry<E>> newlyExecutedOps = node.executeConstrainedSubsetOfOps(refinedCandidateSet,
                piggybackingInfo.getNumOfSizeIncreasingOps(), piggybackingInfo.getNumOfSizeDecreasingOps());
        node.deleteEntryIndex();
        piggybackedOps.addAll(newlyExecutedOps);
        return true;
    }

    /**
     * Finalizes statistics about all the operations that were piggybacked by the piggybacker instance.
     *
     * @param stats stats where to register these operations
     */
    public void finalizeModifications(final OperationTypeStat stats) {
        for (final UpdateTree.Entry<E> e : piggybackedOps) {
            stats.register(e);
            tree.completeOperation(e);
            buffer.removeExactEntry(e);
        }
        piggybackedOps.clear();
    }
}
