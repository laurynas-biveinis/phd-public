/*
     Copyright (C) 2007, 2008, 2009, 2010, 2012 Laurynas Biveinis

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
import aau.bufferedIndexes.OperationGroup;
import aau.bufferedIndexes.StatisticalData;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import xxl.core.io.Convertable;

/**
 * An abstract operation group maker class.  Defines routines that are independent of grouping strategy.
 */
public abstract class AbstractOperationGroupMaker {

    /**
     * How many times deletions have been split during grouping
     */
    final StatisticalData deletionSplits = new StatisticalData();

    /**
     * Creates a mapping between node entries and operations.  Should more or less follow R-tree rules for going down
     * the tree.
     * @param node the node whose entries will be mapped to operations
     * @param operations the operations to process
     * @param <T> payload data type
     * @return mapping between node entries and operations
     */
    @SuppressWarnings({"TypeMayBeWeakened"})
    public <T extends Convertable> IndexEntryOpGroupMap<T> groupOperations(final IRRTreeDiskNode<T> node,
                                                                           final OperationGroup<T> operations) {
        if (operations == null)
            throw new IllegalArgumentException("Argument operations cannot be null");

        final IndexEntryOpGroupMap<T> result = new IndexEntryOpGroupMap<>();
        for (final UpdateTree.Entry<T> operation : operations) {
            final T operatedData = operation.getData();
            groupOperation(operation, operatedData, node, result);
        }
        return result;

    }

    /**
     * Choose a node entry or entries for a given operation and update the result accordingly.
     *
     * @param operation     an operation to choose the entry for
     * @param operatedData  data inside the operation 
     * @param node          node among whose entries to choose
     * @param result        the result mapping to update
     * @param <T>           type of the data element in the tree
     */
    abstract protected <T extends Convertable> void groupOperation(final UpdateTree.Entry<T> operation,
                                                                   final T operatedData,
                                                                   final IRRTreeDiskNode<T> node,
                                                                   final IndexEntryOpGroupMap<T> result);

    /**
     * Return statistics for split deletions during grouping.
     * @return a mapping. Key denotes, into how many deletions a single deletion has been split, value - how many times
     *                    this has occurred.
     */
    public StatisticalData getDeletionSplits() {
        return deletionSplits;
    }

    /**
     * Resets deletion split statistics.
     */
    public void resetStatistics() {
        deletionSplits.reset();
    }
}
