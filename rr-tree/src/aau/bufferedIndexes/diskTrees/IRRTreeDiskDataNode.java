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

import xxl.core.io.Convertable;

import java.util.Collection;

/**
 * An interface that RRDiskDataTree.Node should implement.
 */
public interface IRRTreeDiskDataNode<E extends Convertable> extends IRRTreeDiskNode<E> {
    /**
     * Returns a collection of node entries, assuming that the node is a leaf node.
     *
     * @return a collection of data entries in the node
     */
    Collection<E> getLeafNodeEntries();
}
