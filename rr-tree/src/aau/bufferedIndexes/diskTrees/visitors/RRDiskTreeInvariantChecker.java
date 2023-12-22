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

import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.diskTrees.*;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

import java.util.Collection;
import java.util.HashSet;

public class RRDiskTreeInvariantChecker<E extends Convertable> implements IRRDiskTreeVisitor<E> {

    private final Collection<Object> usedContainerIds = new HashSet<>();

    /**
     * A method to be called when an index node is visited.
     *
     * @param tree            the tree whose nodes are being visited
     * @param indexNodeEntry  the index entry pointing to the visited node
     * @param indexNode       the visited node
     */
    public void visitIndexNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> indexNodeEntry,
                               final IRRTreeDiskNode<E> indexNode) {
        // Check that nodes are not shared
        boolean result = usedContainerIds.add(indexNodeEntry.id());
        checkInvariant (result, "Same container ID used twice!");

        checkInvariant (indexNodeEntry.level() == indexNode.level(),
                "indexNodeEntry and its node should be at the same level");

        if (indexNodeEntry == tree.rootEntry()) {
            checkInvariant (indexNode.number() > 1, "Index root node should contain at least two entries");
        }
        else {
            checkInvariant (!indexNode.underflows(), "node should not be underflowing");
        }
        checkInvariant (!indexNode.overflows(), "node should not be overflowing");

        final IRRTreeIndexEntry<E> firstEntry = indexNode.getNonLeafNodeEntries().iterator().next();
        final Descriptor commonDescriptor = (Descriptor)tree.descriptor(firstEntry).clone();
        for (final IRRTreeIndexEntry<E> entry : indexNode.getNonLeafNodeEntries()) {
            checkInvariant (entry.level() == indexNode.level() - 1,
                    "indexNodeEntry must be one level below its parent node");
            checkInvariant (indexNodeEntry.descriptor().contains(entry.descriptor()),
                    "Subtree MBR should be contained in parent MBR");
            commonDescriptor.union(tree.descriptor(entry));
        }
        checkInvariant (indexNodeEntry.descriptor().equals(commonDescriptor),
                "MBR of node differs from the union of all node entry MBRs");
    }

    /**
     * A method to be called when a leaf node is visited.
     *
     * @param tree           the tree whose nodes are being visited
     * @param leafNodeEntry  the index entry pointing to the visited node
     */
    public void visitLeafNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> leafNodeEntry) {
        final IRRTreeDiskNode<E> leafNode = leafNodeEntry.get();
        checkCommonLeafInvariants(leafNodeEntry, leafNode);
        if (leafNode instanceof IRRTreeDiskDataNode) {
            if (leafNodeEntry == tree.rootEntry())
                checkInvariant (leafNode.number() > 1, "Root-level leaf node should contain at least one element!");
            else {
                checkInvariant (!leafNode.underflows(), "Leaf node should not be underflowing!");
            }
            for (final E entry : ((IRRTreeDiskDataNode<E>)leafNode).getLeafNodeEntries()) {
                checkInvariant (leafNodeEntry.descriptor().contains(tree.descriptor(entry)),
                        "Leaf node element descriptor should be contained in the node descriptor");
            }
        }
        else if (leafNode instanceof IRRTreeDiskUpdateNode) {
            // TODO: unit-test this
            final IRRDiskUpdateTree<E> t = (IRRDiskUpdateTree<E>)tree;
            for (final UpdateTree.Entry<E> entry : ((IRRTreeDiskUpdateNode<E>)leafNode).getLeafNodeEntries()) {
                for (final UpdateTree.Entry<E> e2 : ((IRRTreeDiskUpdateNode<E>)leafNode).getLeafNodeEntries()) {
                    checkInvariant (!t.doEntriesAnnihilate(entry, e2),
                            "Two entries in the node that should have been annihilated!");
                }
                checkInvariant (leafNodeEntry.descriptor().contains(tree.descriptor(entry)),
                        "Leaf node element descriptor should be contained in the node descriptor");
            }
        }
        else {
            throw new IllegalStateException("Unknown disk node type!");
        }
    }

    /**
     * A method to be called after finishing visiting the tree.
     */    
    public void finishVisiting() {
        usedContainerIds.clear();
    }

    /**
     * Checks the invariants common to all leaf node types.
     *
     * @param leafNodeEntry  the index entry pointing to the visited node
     * @param leafNode       the visited node
     */
    private void checkCommonLeafInvariants(final IRRTreeIndexEntry<E> leafNodeEntry,
                                           final IRRTreeDiskNode<E> leafNode) {
        boolean result = usedContainerIds.add(leafNodeEntry.id());
        checkInvariant (result, "Same container ID used twice!");
        checkInvariant (!leafNode.overflows(), "node should not be overflowing");
    }

    private static void checkInvariant(final boolean condition, final String message) throws IllegalStateException {
        if (!condition)
            throw new IllegalStateException(message);
    }
}
