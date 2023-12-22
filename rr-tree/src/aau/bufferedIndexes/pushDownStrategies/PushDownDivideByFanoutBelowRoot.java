/*
     Copyright (C) 2009, 2010, 2012 Laurynas Biveinis

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
 * A push down strategy that divides group size by the actual traversed node fanout below root
 */
public class PushDownDivideByFanoutBelowRoot<E extends Convertable> extends AbstractPushDownThreshold<E> {

    private final PushDownGroupsStrategy<E> pushDownAtRoot;

    private final IRRDiskTree<E> tree;

    private final double coefficient;

    public PushDownDivideByFanoutBelowRoot(final IRRDiskTree<E> tree, final PushDownGroupsStrategy<E> pushDownAtRoot,
                                           final double coefficient, final boolean groupSizeByInsertions) {
        super(-1, groupSizeByInsertions);
        this.tree = tree;
        this.pushDownAtRoot = pushDownAtRoot;
        this.coefficient = coefficient;
    }

    /**
     * From a set of mappings between node index entries and operations, chooses the operations that should be further
     * processed down the tree and the ones that should be returned back to buffer.
     *
     * @param groupsToFilter     set of mappings between node index entries and operations
     * @param childNodeLevel     the tree level of the receiving nodes
     * @param calledFromRestart  is it asked to do regrouping from GroupUpdate restart
     * @return operations to be processed down and operations to be returned to buffer
     */
    public PushDownAndBufferGroups<E> choosePushDownGroups(final IndexEntryOpGroupMap<E> groupsToFilter,
                                                           final int childNodeLevel, final int childNodeSize,
                                                           final boolean calledFromRestart) {
        if (groupsToFilter == null)
            return new PushDownAndBufferGroups<>();
        if (tree.height() == childNodeLevel + 1)
            return pushDownAtRoot.choosePushDownGroups(groupsToFilter, childNodeLevel, childNodeSize,
                    calledFromRestart);
        //noinspection NumericCastThatLosesPrecision
        return applyThreshold((int)Math.floor((double)groupsToFilter.numOfDistinctOps() / childNodeSize * coefficient),
                groupsToFilter);
    }

    /**
     * Tells if this strategy is likely to empty significant part of the buffer.
     *
     * @return <code>true</code> if the strategy is likely to empty significant part of the buffer
     */
    public boolean willEmptyBigPartOfBuffer() {
        return false;
    }

    /**
     * Returns the number of equal largest groups encountered
     *
     * @return number of equal largest groups
     */
    public int getEqualLargestGroups() {
        return pushDownAtRoot.getEqualLargestGroups();
    }
}
