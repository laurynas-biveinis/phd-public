/*
     Copyright (C) 2007, 2008, 2010, 2011 Laurynas Biveinis

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

import java.util.Iterator;

/**
 * An implementation of the GroupOperations algorithm that groups operations trivially, i.e. insert operations are
 * grouped according to the R-tree ChooseSubtree for insertion algorithm; deletion operations are grouped according to
 * the R-tree Query algorithm.
 */
public class TrivialOperationGroupMaker extends AbstractOperationGroupMaker {

    /**
     * Chooses a node entry or entries for a given operation and update the result accordingly.  For insertions,
     * chooses a child entry using standard R-tree ChooseSubtree algorithm.  For deletions, chooses all child entries
     * whose MBR overlap the MBR of the deletion.
     *
     * @param operation     an operation to choose the entry for
     * @param operatedData  data inside the operation
     * @param node          node among whose entries to choose
     * @param result        the result mapping to update
     * @param <T>           type of the data element in the tree
     */    
    protected <T extends Convertable> void groupOperation(final UpdateTree.Entry<T> operation, final T operatedData,
                                                          final IRRTreeDiskNode<T> node,
                                                          final IndexEntryOpGroupMap<T> result) {
        if (operation.isDeletion()) {
            int splitBranching = 0;
            final Iterator<IRRTreeIndexEntry<T>> children = node.query(operatedData);
            while (children.hasNext()) {
                final IRRTreeIndexEntry<T> childForEntry = children.next();
                if (!childForEntry.spatiallyContains(operatedData))
                    continue;
                result.addEntry(childForEntry, operation);
                splitBranching++;
            }
            if (splitBranching == 0) {
                result.addEntry(result.ORPHAN_GROUP_KEY, operation);
            }
            deletionSplits.update(splitBranching, 1);
        }
        else {
            final IRRTreeIndexEntry<T> childForEntry = node.chooseSubtreeByObject(operatedData);
            result.addEntry(childForEntry, operation);
        }
    }
}
    