/*
     Copyright (C) 2009, 2010, 2011, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.pushDownStrategies;

import aau.bufferedIndexes.IndexEntryOpGroupMap;
import aau.bufferedIndexes.OperationGroup;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.io.Convertable;

import java.util.Iterator;
import java.util.Map;

/**
 * A pushdown strategy that chooses the single largest group in the buffer.  Additional groups to contain all
 * split deletes are pushed down too. 
 */
public class PushDownLargestBufGroupSplitDeletes<E extends Convertable> extends PushDownLargestBufGroup<E> {

    /**
     * Creates new strategy object for the specified tree
     * @param tree the tree where this strategy will be used
     * @param groupSizeByInsertions flag if insertions or all ops should be considered for group size calculations
     */
    public PushDownLargestBufGroupSplitDeletes(final IRRDiskTree<E> tree, final boolean groupSizeByInsertions) {
        super(tree, groupSizeByInsertions);
    }

    /**
     * Decides what to do with deletes in the largest group that should also go down other subtrees.  This class
     * creates additional groups so that deletes reach the leaf level in any case.
     * @param groupsForPushDown groups to be pushed down the tree
     * @param groupsForBuffer groups to be returned to the buffer
     */
    protected void handleSplitDeletes(final IndexEntryOpGroupMap<E> groupsForPushDown,
                                      final IndexEntryOpGroupMap<E> groupsForBuffer) {
        // Damn deletes. Look at big-O complexity. As long as we only measure I/Os, should be fine...
        final IndexEntryOpGroupMap<E> deletionPushDownGroups = new IndexEntryOpGroupMap<>();
        for (final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> groupForBuffer : groupsForBuffer) {
            final Iterator<UpdateTree.Entry<E>> opForBufferI = groupForBuffer.getValue().iterator();
            while (opForBufferI.hasNext()) {
                final UpdateTree.Entry<E> opForBuffer = opForBufferI.next();
                boolean deleteThisOpForBuf = false;
                for (final OperationGroup<E> pushDownGroup : groupsForPushDown.operations()) {
                    if (pushDownGroup.contains(opForBuffer)) { // If deletion split has happened
                        // No matter the group size, we must push the split deletion down.
                        assert opForBuffer.isDeletion();
                        deletionPushDownGroups.addIfNotExists(groupForBuffer.getKey(), opForBuffer);
                        deleteThisOpForBuf = true;
                    }
                }
                if (deleteThisOpForBuf)
                    opForBufferI.remove();
            }
        }
        groupsForPushDown.copy(deletionPushDownGroups);
    }
}
