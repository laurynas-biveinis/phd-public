/*
     Copyright (C) 2010 Laurynas Biveinis

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

import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.io.Convertable;

import java.io.IOException;

/**
 * An interface that a RR-Tree node visitor should implement.
 */
public interface IRRDiskTreeVisitor<E extends Convertable> {

    /**
     * A method to be called when an index node is visited.
     *
     * @param tree           the tree whose nodes are being visited
     * @param indexNodeEntry the index entry pointing to the visited node
     * @param indexNode      the visited node
     */
    public void visitIndexNode (IRRDiskTree<E> tree, IRRTreeIndexEntry<E> indexNodeEntry,
                                IRRTreeDiskNode<E> indexNode);

    /**
     * A method to be called when a leaf node is visited.  The implementation is responsible for loading the node
     * itself.
     *
     * @param tree           the tree whose nodes are being visited
     * @param leafNodeEntry  the index entry pointing to the visited node
     */
    public void visitLeafNode (IRRDiskTree<E> tree, IRRTreeIndexEntry<E> leafNodeEntry);

    /**
     * A method to be called after finishing visiting the tree.  May perform any arbitrary final processing, including
     * I/O.
     *
     * @throws IOException if final processing I/O fails
     */
    public void finishVisiting() throws IOException;
}
