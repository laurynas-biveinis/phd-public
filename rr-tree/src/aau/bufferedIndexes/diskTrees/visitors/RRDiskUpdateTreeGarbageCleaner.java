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
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.collections.containers.Container;
import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Common parent class for different disk update tree garbage removal implementations
 */
public abstract class RRDiskUpdateTreeGarbageCleaner<E extends Convertable>
        implements IRRDiskUpdateTreeGarbageCleaner<E> {

    /**
     * The tree which the garbage cleaner operates on.
     */
    final IRRDiskUpdateTree<E> tree;

    /**
     * The buffer of the tree to piggyback operations from as we clean.
     */
    final IRRTreeBuffer<E> buffer;

    /**
     * The disk nodes that have been fully cleaned.
     */
    private final Collection<Object> cleanedLeafNodeIDs = new HashSet<>();

    /**
     * Additional container that separately caches index nodes
     */
    private final Container indexNodeContainer;

    RRDiskUpdateTreeGarbageCleaner(final IRRDiskUpdateTree<E> tree, final IRRTreeBuffer<E> buffer,
                                   final Container indexNodeContainer) {
        this.tree = tree;
        this.buffer = buffer;
        this.indexNodeContainer = indexNodeContainer;
    }

    /**
     * A method to be called when an index node is visited.  This implementation does nothing as there is no garbage
     * above the leaf level.
     *
     * @param tree           the tree whose nodes are being visited
     * @param indexNodeEntry the index entry pointing to the visited node
     * @param indexNode      the visited node
     */
    public void visitIndexNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> indexNodeEntry,
                               final IRRTreeDiskNode<E> indexNode) {
        assert (tree == this.tree);
    }

    /**
     * Cleans a disk update tree leaf node from garbage.
     *
     * @param tree          the tree whose nodes are being visited
     * @param leafNodeEntry the index entry pointing to the visited node
     */
    public void visitLeafNode(IRRDiskTree<E> tree, IRRTreeIndexEntry<E> leafNodeEntry) {
        assert (tree == this.tree);
        assert (leafNodeEntry.level() == 0);
        final Object leafID = leafNodeEntry.id();
        assert !cleanedLeafNodeIDs.contains(leafID);
        final Descriptor cleaningArea = leafNodeEntry.descriptor();
        final Collection<Object> intersectingNodeIDs = tree.fetchIntersectingLeafNodeIDs(indexNodeContainer,
                cleaningArea);
        intersectingNodeIDs.removeAll(cleanedLeafNodeIDs); // TODO: a failing test with leavesInProgress.values()
        final Cursor<UpdateTree.Entry<E>> matchingBufferOps = buffer.queryEntryOfAnyType(cleaningArea);
        cleanNode(tree, leafNodeEntry, leafID, intersectingNodeIDs, matchingBufferOps, cleaningArea);
        matchingBufferOps.close();
        cleanedLeafNodeIDs.add(leafID);
    }

    protected abstract void cleanNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> leafNodeEntry,
                                      final Object leafID, final Iterable<Object> intersectingNodeIDs,
                                      final Cursor<UpdateTree.Entry<E>> matchingBufferOps,
                                      final Descriptor cleaningArea);

    public void finishVisiting() {
        cleanedLeafNodeIDs.clear();
    }
}
