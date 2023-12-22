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
package aau.bufferedIndexes.operationGroupMakers;

import aau.bufferedIndexes.IndexEntryOpGroupMap;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.io.Convertable;

/**
 * An operation group maker that treats both deletions and insertions as insertions, i.e. uses only R-tree
 * ChooseSubtree algorithm.
 */
public class DeletionsAsInsertionsGroupMaker extends AbstractOperationGroupMaker {

    /**
     * Chooses a node entry for a given operation and updates result accordingly.  For both insertions and deletions,
     * chooses a child entry using the standard R-tree ChooseSubtree algorithm.
     *
     * @param operation     an operation to choose the entry for
     * @param operatedData  data inside the operation
     * @param node          node among whose entries to choose
     * @param result        the result mapping to update
     * @param <T>           the type of the data elements in the tree 
     */
    protected <T extends Convertable> void groupOperation(final UpdateTree.Entry<T> operation, final T operatedData,
                                                          final IRRTreeDiskNode<T> node,
                                                          final IndexEntryOpGroupMap<T> result) {
        final IRRTreeIndexEntry<T> childForEntry = node.chooseSubtreeByObject(operation);
        result.addEntry(childForEntry, operation);
    }
}
