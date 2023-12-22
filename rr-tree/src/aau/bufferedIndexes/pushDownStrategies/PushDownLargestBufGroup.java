/*
     Copyright (C) 2007, 2008, 2009, 2010, 2011, 2012 Laurynas Biveinis

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
import aau.bufferedIndexes.PushDownAndBufferGroups;
import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.io.Convertable;

import java.util.Map;

/**
 * A pushdown strategy that chooses the single largest group in the buffer.  Any split deletes are ignored.
 */
public class PushDownLargestBufGroup<E extends Convertable> implements PushDownGroupsStrategy<E> {

    /**
     * The tree which uses this strategy
     */
    private final IRRDiskTree<E> tree;

    /**
     * Number of equal largest groups found
     */
    private int equalLargestGroupsFound = 0;

    private final boolean groupSizeByInsertions;

    /**
     * Creates new strategy object for the specified tree
     * @param tree the tree where this strategy will be used
     * @param groupSizeByInsertions flag if insertions or all ops should be considered for group size calculations
     */
    public PushDownLargestBufGroup(final IRRDiskTree<E> tree, final boolean groupSizeByInsertions) {
        this.tree = tree;
        this.groupSizeByInsertions = groupSizeByInsertions;
    }
    
    /**
     * From a set of mappings between node index entries and operations, chooses the operations that should be further
     * processed down the tree and the ones that should be returned back to buffer.
     * @param groupsToFilter     set of mappings between node index entries and operations
     * @param childNodeLevel     the tree level of the receiving nodes
     * @param childNodeSize      the size of the receiving node
     * @param calledFromRestart  is it asked to do regrouping from GroupUpdate restart
     * @return operations to be processed down and operations to be returned to buffer
     */
    public PushDownAndBufferGroups<E> choosePushDownGroups(final IndexEntryOpGroupMap<E> groupsToFilter,
                                                           final int childNodeLevel, final int childNodeSize,
                                                           final boolean calledFromRestart) {
        if (calledFromRestart) {
            return new PushDownAndBufferGroups<>(new IndexEntryOpGroupMap<>(groupsToFilter),
                    new IndexEntryOpGroupMap<E>());
        }
        final IndexEntryOpGroupMap<E> groupsForPushDown = new IndexEntryOpGroupMap<>();
        final IndexEntryOpGroupMap<E> groupsForBuffer = new IndexEntryOpGroupMap<>();
        if (tree.height() == childNodeLevel + 1) {
            groupsForBuffer.copy(groupsToFilter);
            int biggestGroupSize = 0;
            for (final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> group : groupsToFilter) {
                if (group.getValue().sizeInSignificantOps(groupSizeByInsertions) > biggestGroupSize) {
                    groupsForBuffer.copy(groupsForPushDown);
                    groupsForPushDown.clearAndSet(group);
                    groupsForBuffer.remove(group);
                    biggestGroupSize = group.getValue().size();
                }
                else if (group.getValue().sizeInSignificantOps(groupSizeByInsertions) == biggestGroupSize)
                    groupsForPushDown.moveGroupFrom(groupsForBuffer, group.getKey(), group.getValue());
            }
            equalLargestGroupsFound += groupsForPushDown.size() - 1;
            handleSplitDeletes(groupsForPushDown, groupsForBuffer);
        }
        else {
            groupsForPushDown.copy(groupsToFilter);
        }
        return new PushDownAndBufferGroups<>(groupsForPushDown, groupsForBuffer);
    }

    /**
     * Decides what to do with deletes in the largest group that should also go down other subtrees.  This class
     * ignores them.
     * @param groupsForPushDown groups to be pushed down the tree
     * @param groupsForBuffer groups to be returned to the buffer
     */
    void handleSplitDeletes(final IndexEntryOpGroupMap<E> groupsForPushDown,
                            final IndexEntryOpGroupMap<E> groupsForBuffer)   { }

    /**
     * Tells if this strategy is likely to empty significant part of the buffer.  Emptying only the largest group is
     * not likely to do that. 
     *
     * @return <code>true</code> if the strategy is likely to empty significant part of the buffer
     */
    public boolean willEmptyBigPartOfBuffer() {
        return false;
    }

    /**
     * Returns the number of equal largest groups encountered
     * @return number of equal largest groups
     */
    public int getEqualLargestGroups() {
        return equalLargestGroupsFound;
    }
}
