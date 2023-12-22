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
package aau.bufferedIndexes;

import xxl.core.io.Convertable;

import java.util.*;

/**
 * The RR-Tree statistics class
 * @param <E> type of RR-Tree data item
 */
public class RRTreeStats<E extends Convertable> implements Cloneable {

    /**
     * Class for tracking unfinalized EB survival stats
     */
    private static final class RunningLifetimeStat {

        /**
         * Number of completed EmptyBuffer invocations at the time this was created
         */
        private final int ebNum;

        /**
         * The statistics data associated with this unfinalized entry
         */
        public final LifetimeStatEntry statEntry = new LifetimeStatEntry();

        /**
         * Create new unfinalized EB survival stat
         * @param ebNum number of completed EmptyBuffer invocations so far
         */
        private RunningLifetimeStat(final int ebNum) {
            this.ebNum = ebNum;
        }

        /**
         * Get number of completed EmptyBuffer invocations when this was created
         * @return number of completed EmptyBuffer invocations when this was created
         */
        public int getEBNum() {
            return ebNum;
        }

        /**
         * Return the statistics data associated with this unfinalized entry
         * @return the statistics data associated with this unfinalized entry
         */
        public LifetimeStatEntry getStatEntry() {
            return statEntry;
        }
    }

    /**
     * Sizes of groups encountered during buffer emptying so far
     */
    private final List<StatisticalData> groupSizes = new ArrayList<>();

    /**
     * Sizes of insertion-only group sizes encountered during buffer emptying so far
     */
    private final List<StatisticalData> insertionOnlyGroupSizes = new ArrayList<>();

    /**
     * Sizes of the groups put back to the buffer at each tree level
     */
    private final List<StatisticalData> backToBufferGroupSizes = new ArrayList<>();

    /**
     * How often buffer is emptied
     */
    private final StatisticalData emptyingFrequency = new StatisticalData();

    /**
     * Operation lifetime in the buffer statistics
     */
    private final OpLifetimeStats opLifetimeStats = new OpLifetimeStats();

    /**
     * Number of EBs completed so far
     */
    private int completedEmptyBufNum = 0;

    /**
     * Unfinalized EB survival stats for the entries still in buffer
     */
    private final Map<UpdateTree.Entry<E>, RunningLifetimeStat> ebsSurvived = new HashMap<>();

    /**
     * Number of updates since last buffer emptying
     */
    private int updatesSinceLastEmptying = 0;

    /**
     * Number of times when the emptying strategy failed to do anything and whole buffer had to be emptied.
     */
    private int failedEmptying = 0;

    /**
     * Number of times a leaf level node was updated.
     */
    private int leafNodeUpdates = 0;

    /**
     * Number of times a non-leaf node was updated.
     */
    private int nonLeafNodeUpdates = 0;

    /**
     * Number of GroupUpdate algorithm restarts because of mapping invalidation.
     */
    private int groupUpdateRestarts = 0;

    /**
     * Total number of GroupUpdate algorithm invocations.
     */
    private int groupUpdateInvocations = 0;

    /**
     * Number of times when updated non-leaf node had only one entry.
     */
    private int singleEntryNodes = 0;

    /**
     * Number of times when recursive GroupUpdate call returned non-underfull or more than one child.
     */
    private int trivialChildIntegrations = 0;

    /**
     * Number of IntegrateChild algorithm invocations.
     */
    private int integrateChildInvocations = 0;

    /**
     * Number of times when recursive GroupUpdate call returned empty child.
     */
    private int emptyChildren = 0;

    /**
     * Number of times in GroupUpdate when node contained a single child and was replaced by it.
     */
    private int childrenReplacingParent = 0;

    /**
     * Number of InsertSubtree invocations.
     */
    private int insertSubtreeInvocations = 0;

    /**
     * Number of recursive InsertSubtree invocations.
     */
    private int insertSubtreeRecursion = 0;

    /**
     * Number of MergeSubtree invocations.
     */
    private int mergeSubtreeInvocations = 0;

    /**
     * Number of recursive MergeSubtree invocations.
     */
    private int mergeSubtreeRecursion = 0;

    /**
     * Number of node-size-decreasing operations that could be but were not piggybacked.
     */
    private int totalNonPiggybackedNodeSizeDecreasingOps = 0;

    /**
     * Number of node-size-increasing operations that could be but were not piggybacked.
     */
    private int totalNonPiggybackedNodeSizeIncreasingOps = 0;

    /**
     * Statistics for operations completed during query piggybacking
     */
    private final OperationTypeStat queryPiggybackings
            = new OperationTypeStat("Query piggybacking");

    /**
     * Statistics for operations completed during update piggybacking at the leaf level
     */
    private final OperationTypeStat leafUpdatePiggybackings
            = new OperationTypeStat("Leaf update piggybacking");

    /**
     * Statistics for additional operations inserted to pushdown groups above the leaf level
     */
    private final OperationTypeStat nonleafUpdatePiggybackings
            = new OperationTypeStat("Non-leaf update piggybacking");

    /**
     * Makes a copy of statistics object
     * @return a copy of this object
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        // TODO: clone mutable subobjects here too
        return super.clone();
    }

    // Various statistical events

    /**
     * Registers an incoming update
     */
    public void registerUpdate() {
        updatesSinceLastEmptying++;
    }

    /**
     * Registers start of EmptyBuffer
     */
    public void registerEmptyBuffer() {
        emptyingFrequency.update(updatesSinceLastEmptying, 1);
        updatesSinceLastEmptying = 0;
    }

    /**
     * Registers finish of EmptyBuffer
     */
    public void registerEndOfEmptyBuffer() {
        completedEmptyBufNum++;
    }

    /**
     * Registers a failed completion of EmptyBuffer
     */
    public void registerFailedEmptying() {
        failedEmptying++;
    }

    /**
     * Registers a GroupUpdate invocation
     */
    public void registerGroupUpdate() {
        groupUpdateInvocations++;
    }

    /**
     * Registers a GroupUpdate restart
     */
    public void registerGroupUpdateRestart() {
        groupUpdateRestarts++;
    }

    /**
     * Registers an operation group that is being returned to the buffer
     * @param treeLevel from which disk tree level it is being returned
     * @param groupSize the size of this group
     */
    public void registerBackToBufferGroup(final int treeLevel, final int groupSize) {
        incHistogramInList(backToBufferGroupSizes, treeLevel, groupSize);
    }

    /**
     * Registers an operation that is being returned to the buffer
     * @param treeLevel from which disk tree level it is being returned
     * @param op the operation
     */
    public void registerBackToBufferOperation(final int treeLevel, final UpdateTree.Entry<E> op) {
        final RunningLifetimeStat runningStat = ebsSurvived.get(op);
        if (runningStat != null)
            runningStat.getStatEntry().returnToBuffer(treeLevel);
        else
            assert op.isDeletion();
    }

    /**
     * Registers a node with only one entry resulting from GroupUpdate
     */
    public void registerSingleEntryNode() {
        singleEntryNodes++;
    }

    /**
     * Registers a non-leaf node update
     */
    public void registerNonLeafNodeUpdate() {
        nonLeafNodeUpdates++;
    }

    /**
     * Registers IntegrateChild call
     */
    public void registerIntegrateChildInvocation() {
        integrateChildInvocations++;
    }

    /**
     * Registers an empty children set as returned by GroupUpdate
     */
    public void registerEmptyChildrenSet() {
        emptyChildren++;
    }

    /**
     * Registers an event of child replacing parent
     */
    public void registerChildReplacingParent() {
        childrenReplacingParent++;
    }

    /**
     * Registers a trivial child integration
     */
    public void registerTrivialChildIntegration() {
        trivialChildIntegrations++;
    }

    /**
     * Registers a leaf node update
     */
    public void registerLeafNodeUpdate() {
        leafNodeUpdates++;
    }

    /**
     * Registers an InsertSubtree call
     */
    public void registerInsertSubtreeInvocation() {
        insertSubtreeInvocations++;
    }

    /**
     * Registers an InsertSubtree recursion
     */
    public void registerInsertSubtreeRecursion() {
        insertSubtreeRecursion++;
    }

    /**
     * Registers a MergeSubtree call
     */
    public void registerMergeSubtreeInvocation() {
        mergeSubtreeInvocations++;
    }

    /**
     * Registers a MergeSubtree recursion
     */
    public void registerMergeSubtreeRecursion() {
        mergeSubtreeRecursion++;
    }

    /**
     * Registers node-size-decreasing operations that could not be piggybacked.
     *
     * @param nonPiggybackedNodeSizeDecreasingOps number of node-size-decreasing operations that could not be
     * piggybacked
     */
    public void registerNonPiggybackedNodeSizeDecreasingOps(final int nonPiggybackedNodeSizeDecreasingOps) {
        totalNonPiggybackedNodeSizeDecreasingOps += nonPiggybackedNodeSizeDecreasingOps;
    }

    /**
     * Registers node-size-increasing operations that could not be piggybacked.
     *
     * @param nonPiggybackedNodeSizeIncreasingOps number of node-size-increasing operations that could not be
     * piggybacked
     */
    public void registerNonPiggybackedNodeSizeIncreasingOps(final int nonPiggybackedNodeSizeIncreasingOps) {
        totalNonPiggybackedNodeSizeIncreasingOps += nonPiggybackedNodeSizeIncreasingOps;
    }

    /**
     * Registers statistics about current operation.  If it caused annihilation, registers the final statistics for the
     * annihilated operation, otherwise opens statistics for the newly inserted to the buffer operation.
     * @param data data of the current operation
     * @param oldBufSize buffer size before attempting to insert current operation to the buffer
     * @param newBufSize buffer size after inserting the current operation to the buffer
     * @param operationType operation type (insertion or deletion)
     */
    public void registerOpLifetime(final E data, final int oldBufSize, final int newBufSize,
                                   final OperationType operationType) {
        if (oldBufSize - newBufSize == 1) {
            opLifetimeStats.updateLifetime(0);
            completeUpdateLifetime(new UpdateTree.Entry<>(data, operationType.opposite()));
        }
        else {
            ebsSurvived.put(new UpdateTree.Entry<>(data, operationType), new RunningLifetimeStat(completedEmptyBufNum));
        }
    }

    /**
     * Marks end of life in buffer for the specified entry and updates its buffer emptying survival statistics
     * @param entry an operation which has been removed from buffer
     */
    public void completeUpdateLifetime(final UpdateTree.Entry<E> entry) {
        final RunningLifetimeStat runningStat = ebsSurvived.get(entry);
        final int ebDiff = completedEmptyBufNum - runningStat.getEBNum();
        assert ebDiff >= 0;
        opLifetimeStats.updateLifetime(ebDiff, runningStat.getStatEntry());
        ebsSurvived.remove(entry);
    }

    /**
     * Registers GroupUpdate group size statistics
     * @param nodeLevel the disk tree level of the node that is receiving these operations
     * @param groupSize size of the group of operations
     * @param insertionOnly flag if the group is made of only insertions
     */
    public void updateGroupUpdateStatistics(final int nodeLevel, final int groupSize, final boolean insertionOnly) {
        incHistogramInList(groupSizes, nodeLevel, groupSize);
        if (insertionOnly) {
            incHistogramInList(insertionOnlyGroupSizes, nodeLevel, groupSize);
        }
    }

    // TODO: javadoc, unit test
    public boolean hasUpdateLifetime (final UpdateTree.Entry<E> entry) {
        return ebsSurvived.containsKey(entry);
    }

    /**
     * Record operation lifetime stats for all the ops currently in the buffer. To be called at the end of test run.
     */
    public void registerBufferLifetimes() {
        for (final Map.Entry<UpdateTree.Entry<E>, RunningLifetimeStat> bufEntry : ebsSurvived.entrySet()) {
            final RunningLifetimeStat runningStat = bufEntry.getValue();
            int ebDiff = completedEmptyBufNum - runningStat.getEBNum();
            assert ebDiff >= 0;
            // Make a special EBs survived number of operations that just arrived to the buffer and didn't have a
            // chance yet
            if (ebDiff == 0)
                ebDiff = -1;
            opLifetimeStats.updateLifetime(ebDiff, runningStat.getStatEntry());
        }
    }

    // Statistic queries and getters

    /**
     * Get number of times GroupUpdate was called
     * @return number of times GroupUpdate was called
     */
    public int getGroupUpdateInvocations() {
        return groupUpdateInvocations;
    }

    /**
     * Return operation lifetime statistics in a set of map entries with key being number of EmptyBuffer calls and
     * value of OpLifetimeStats.TotalsForEB class.
     * @return operation lifetime statistics
     */
    public Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> getOpLifetimeStats() {
        return opLifetimeStats.entrySet();
    }

    /**
     * Get number of failed EmptyBuffer calls
     * @return number of failed EmptyBuffer calls
     */
    public int getFailedEmptyings() {
        return failedEmptying;
    }

    /**
     * Returns statistics, how often buffer is emptied.
     * @return A mapping from intervals between buffer emptying operations (counted by number of update operations)
     * to number of such intervals
     */
    public StatisticalData getEmptyingFrequencies() {
        return emptyingFrequency;
    }

    /**
     * Returns statistics of group sizes for the groups that are returned back to the buffer during emptying.
     * @return put back to buffer group statistics.  An array index specifies level, map key specifies group size and
     * map values specifies number of occurences of groups of this size.
     */
    public Collection<StatisticalData> getBackToBufferGroupSizes() {
        return backToBufferGroupSizes;
    }

    /**
     * Returns statistics, how many groups containing only insertions of what size to which level have been pushed so
     * far.
     * @return insertion-only group statistics.  An array index specifies level, map key specifies group size and map
     *         value specifies how many insertion-only groups of what size have been formed so far.
     */
    public List<StatisticalData> getInsertionOnlyGroupSizes() {
        return insertionOnlyGroupSizes;
    }

    /**
     * Return statistics for frequency of group sizes, regardless of their level.
     * @return a sorted mapping, whose key is a group size, value - how many groups of that size were encountered.
     */
    public StatisticalData getGlobalGroupSizeStatistics() {
        final StatisticalData globalSizeStats = new StatisticalData();
        for (final StatisticalData statisticsForLevel : groupSizes) {
            globalSizeStats.add(statisticsForLevel);
        }
        return globalSizeStats;
    }

    /**
     * Returns statistics, how many groups of what size to which level have been pushed so far.
     * @return group size statistics.  An array index specifies level, map key specifies group size and map value
     *         specifies how many groups of that size have been formed so far.
     */
    public List<StatisticalData> getGroupSizeStatistics() {
        return groupSizes;
    }

    /**
     * Return query piggybacking statistics
     * @return query piggybacking statistics
     */
    public OperationTypeStat getQueryPiggybackings() {
        return queryPiggybackings;
    }

    /**
     * Return non-leaf level update piggybacking statistics
     * @return non-leaf level update piggybacking statistics
     */
    public OperationTypeStat getNonleafUpdatePiggybackings() {
        return nonleafUpdatePiggybackings;
    }

    /**
     * Return update piggybacking statistics
     * @return update piggybacking statistics
     */
    public OperationTypeStat getLeafUpdatePiggybackings() {
        return leafUpdatePiggybackings;
    }

    /**
     * Get number of leaf node updates
     * @return number of leaf node updates
     */
    public int getLeafNodeUpdates() {
        return leafNodeUpdates;
    }

    /**
     * Get number of non-leaf node updates
     * @return number of non-leaf updates
     */
    public int getNonLeafNodeUpdates() {
        return nonLeafNodeUpdates;
    }

    /**
     * Get number of times GroupUpdate has been restarted
     * @return number of times GroupUpdate has been restarted
     */
    public int getGroupUpdateRestarts() {
        return groupUpdateRestarts;
    }

    /**
     * Get number of times GroupUpdate has produced a single entry node
     * @return number of times GroupUpdate has produced a single entry node
     */
    public int getSingleEntryNodes() {
        return singleEntryNodes;
    }

    /**
     * Get number of times GroupUpdate has trivially integrated children entries
     * @return number of times GroupUpdate has trivially integrated children entries
     */
    public int getTrivialChildIntegrations() {
        return trivialChildIntegrations;
    }

    /**
     * Get number of times GroupUpdate has produced an empty children set
     * @return number of times GroupUpdate has produced an empty children set
     */
    public int getEmptyChildren() {
        return emptyChildren;
    }

    /**
     * Get number of times IntegrateChild was called
     * @return number of times IntegrateChild was called
     */
    public int getIntegrateChildInvocations() {
        return integrateChildInvocations;
    }

    /**
     * Get number of times children replaced parent in GroupUpdate
     * @return number of times children replaced parent in GroupUpdate
     */
    public int getChildrenReplacingParent() {
        return childrenReplacingParent;
    }

    /**
     * Get number of times InsertSubtree was called
     * @return number of times InsertSubtree was called
     */
    public int getInsertSubtreeInvocations() {
        return insertSubtreeInvocations;
    }

    /**
     * Get number of times InsertSubtree has called itself recursively
     * @return number of times InsertSubtree has called itself recursively
     */
    public int getInsertSubtreeRecursion() {
        return insertSubtreeRecursion;
    }

    /**
     * Get number of times MergeSubtree was called
     * @return number of times MergeSubtree was called
     */
    public int getMergeSubtreeInvocations() {
        return mergeSubtreeInvocations;
    }

    /**
     * Get number of times MergeSubtree has called itself recursively
     * @return number of times MergeSubtree has called itself recursively
     */
    public int getMergeSubtreeRecursion() {
        return mergeSubtreeRecursion;
    }

    /**
     * Gets the total number of node-size-decreasing operations that could have been piggybacked but were not in order
     * not to make the node underflowing.
     *  
     * @return total number of non-piggybacked node-size-decreasing operations
     */
    public int getTotalNonPiggybackedNodeSizeDecreasingOps() {
        return totalNonPiggybackedNodeSizeDecreasingOps;
    }

    /**
     * Gets the total number of node-size-increasing operations that could have been piggybacked but were not in order
     * not to make the node overflowing.
     *
     * @return total number of non-piggybacked node-size-increasing operations
     */
    public int getTotalNonPiggybackedNodeSizeIncreasingOps() {
        return totalNonPiggybackedNodeSizeIncreasingOps;
    }

    /**
     * In a list of StatisticalData objects, bump the counter in one of them.  If the specified object is not existing
     * in the list, create it and all missing objects with smaller indexes.
     * @param statistics list of StatisticalData objects
     * @param index index in the list which StatisticalData object to use
     * @param key key in the StatisticalData object
     */
    private static void incHistogramInList(final List<StatisticalData> statistics, final int index, final int key) {
        while (statistics.size() < index + 1) {
            statistics.add(new StatisticalData());
        }
        statistics.get(index).update(key, 1);
    }
}
