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
package aau.bufferedIndexes.pushDownStrategies;

import aau.bufferedIndexes.IndexEntryOpGroupMap;
import aau.bufferedIndexes.PushDownAndBufferGroups;
import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import xxl.core.io.Convertable;

/**
 * A push down strategy that performs threshold-based filtering only at the root level
 */
public class RootLevelThreshold<E extends Convertable> extends AbstractPushDownThreshold<E> {

    /**
     * The tree which is using this strategy
     */
    private final IRRDiskTree<E> tree;

    /**
     * Creates new strategy object.
     * @param tree the tree to use this strategy on
     * @param threshold the threshold value to use at the root level
     * @param groupSizeByInsertions flag if insertions or all ops should be considered for group size calculations
     */
    public RootLevelThreshold(final IRRDiskTree<E> tree, final int threshold, final boolean groupSizeByInsertions) {
        super(threshold, groupSizeByInsertions);
        this.tree = tree;
    }

    /**
     * From a set of mappings between node index entries and operations, chooses the operations that should be further
     * processed down the tree and the ones that should be returned back to buffer.
     * @param groupsToFilter set of mappings between node index entries and operations
     * @param childNodeLevel the tree level of the receiving nodes
     * @param childNodeSize     the size of the receiving node
     * @param calledFromRestart is it asked to do regrouping from GroupUpdate restart
     * @return operations to be processed down and operations to be returned to buffer
     */    
    public PushDownAndBufferGroups<E> choosePushDownGroups(final IndexEntryOpGroupMap<E> groupsToFilter,
                                                           final int childNodeLevel, final int childNodeSize,
                                                           final boolean calledFromRestart) {
        final IndexEntryOpGroupMap<E> groupsForPushDown = new IndexEntryOpGroupMap<>();
        final IndexEntryOpGroupMap<E> groupsForBuffer = new IndexEntryOpGroupMap<>();
        if (tree.height() == childNodeLevel + 1)
            selectGroupsAboveThreshold(groupsToFilter, groupsForPushDown, groupsForBuffer);
        else
            groupsForPushDown.copy(groupsToFilter);
        return new PushDownAndBufferGroups<>(groupsForPushDown, groupsForBuffer);
    }

//    public PushDownAndBufferGroups choosePushDownGroups(IndexEntryOpGroupMap groupsToFilter, int childNodeLevel, int childNodeSize, boolean calledFromRestart) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    /**
     * Tells if this strategy is likely to empty significant part of the buffer.  Thresholding at root level will not
     * do this with sane thresholds.
     *
     * @return <code>true</code> if the strategy is likely to empty significant part of the buffer
     */
    public boolean willEmptyBigPartOfBuffer() {
        return false;
    }

    /**
     * Returns the number of equal largest groups encountered.  Always zero for this strategy.
     *
     * @return number of equal largest groups
     */
    public int getEqualLargestGroups() {
        return 0;
    }
}
