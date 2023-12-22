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
package aau.bufferedIndexes.diskTrees;

import aau.bufferedIndexes.HasLogicalSize;
import aau.bufferedIndexes.UpdateTree;
import xxl.core.io.Convertable;

import java.util.Collection;

/**
 * An interface that RRDiskUpdateTree.Node must implement.
 */
public interface IRRTreeDiskUpdateNode<E extends Convertable> extends IRRTreeDiskNode<E>, HasLogicalSize {
    /**
     * Returns a collection of node entries, assuming that the node is a leaf node.
     * @return a collection of data entries in the node
     */
    Collection<UpdateTree.Entry<E>> getLeafNodeEntries();

    /**
     * A stop-gap measure before nodes implement Iterable properly.  Signals to a node that its element was just
     * removed through an iterator.
     */
    void dataItemRemoved();

    /**
     * A stop-gap measure before nodes implement Iterable properly.  Signals to a node that its element was just
     * added.
     */
    void dataItemAdded();
}
