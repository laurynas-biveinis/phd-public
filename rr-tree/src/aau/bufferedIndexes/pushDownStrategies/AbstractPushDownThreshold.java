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
import aau.bufferedIndexes.OperationGroup;
import aau.bufferedIndexes.PushDownAndBufferGroups;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.io.Convertable;

import java.util.Map;

/**
 * A common ancestor for all threshold-based pushing down strategies
 */
public abstract class AbstractPushDownThreshold<E extends Convertable> implements PushDownGroupsStrategy<E> {
    final int threshold;
    private final boolean groupSizeByInsertions;
    private int thresholdUnsatisfied = 0;
    private int thresholdSatisfied = 0;

    AbstractPushDownThreshold(final int threshold, final boolean groupSizeByInsertions) {
        this.threshold = threshold;
        this.groupSizeByInsertions = groupSizeByInsertions;
    }

    public int getThresholdSatisfactions() {
        return thresholdSatisfied;
    }

    public int getThresholdUnsatisfactions() {
        return thresholdUnsatisfied;
    }

    void selectGroupsAboveThreshold(final IndexEntryOpGroupMap<E> groupsToFilter,
                                    final IndexEntryOpGroupMap<E> groupsForPushDown,
                                    final IndexEntryOpGroupMap<E> groupsForBuffer)
    {
        selectGroupsAboveThreshold(threshold, groupsToFilter, groupsForPushDown, groupsForBuffer,
                false);
    }

    @SuppressWarnings({"TypeMayBeWeakened"})
    void selectGroupsAboveThreshold(final int threshold,
                                    final IndexEntryOpGroupMap<E> groupsToFilter,
                                    final IndexEntryOpGroupMap<E> groupsForPushDown,
                                    final IndexEntryOpGroupMap<E> groupsForBuffer,
                                    final boolean emptyPushDownAcceptable) {
        if (groupsToFilter == null) {
            thresholdSatisfied++;
            return;
        }
        Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> biggestGroup = null;
        int biggestGroupSize = 0;
        for (final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> group : groupsToFilter) {
            if (!emptyPushDownAcceptable
                    && (group.getValue().sizeInSignificantOps(groupSizeByInsertions) > biggestGroupSize)) {
                biggestGroupSize = group.getValue().size();
                biggestGroup = group;
            }
            if (group.getValue().sizeInSignificantOps(groupSizeByInsertions) >= threshold)
                groupsForPushDown.put(group);
            else
                groupsForBuffer.put(group);
        }
        if (groupsForPushDown.isEmpty() && !groupsForBuffer.isEmpty()) {
            thresholdUnsatisfied++;
            if (!emptyPushDownAcceptable) {
                if (biggestGroup == null) {
                    // Can happen if we have groups from deletions only
                    // Probably, we will have to restart EmptyBuffer, so just continue with any group for now
                    biggestGroup = groupsForBuffer.iterator().next();
                }
                groupsForPushDown.moveGroupFrom(groupsForBuffer, biggestGroup.getKey(), biggestGroup.getValue());
            }
        }
        else
            thresholdSatisfied++;
    }

    PushDownAndBufferGroups<E> applyThreshold(final int threshold,
                                              final IndexEntryOpGroupMap<E> groupsToFilter) {
        final IndexEntryOpGroupMap<E> groupsForPushDown = new IndexEntryOpGroupMap<>();
        final IndexEntryOpGroupMap<E> groupsForBuffer = new IndexEntryOpGroupMap<>();
        selectGroupsAboveThreshold(threshold, groupsToFilter, groupsForPushDown, groupsForBuffer, true);
        return new PushDownAndBufferGroups<>(groupsForPushDown, groupsForBuffer);
    }
}
