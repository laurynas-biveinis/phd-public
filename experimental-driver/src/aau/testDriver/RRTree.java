/*
     Copyright (C) 2007, 2008, 2009, 2010, 2011 Laurynas Biveinis

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
package aau.testDriver;

import aau.bufferedIndexes.AggregateStats;
import aau.bufferedIndexes.OpLifetimeStats;
import aau.bufferedIndexes.RRTreeStats;
import aau.bufferedIndexes.StatisticalData;
import aau.bufferedIndexes.diskTrees.*;
import aau.bufferedIndexes.diskTrees.visitors.RRDiskTreeInvariantChecker;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.bufferedIndexes.operationGroupMakers.AbstractOperationGroupMaker;
import aau.bufferedIndexes.pushDownStrategies.PushDownGroupsStrategy;
import aau.workload.DataID;
import xxl.core.collections.containers.Container;
import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.Tree;
import xxl.core.io.Convertable;
import xxl.core.io.converters.Converter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An R^R-tree for the experimental driver
 */
public final class RRTree<E extends Convertable> implements TreeDriver<E> {

    public enum TreeType { DATA_TREE, UPDATE_TREE }

    private final aau.bufferedIndexes.RRTree<E> tree;
    private boolean bufferEmptied;
    private final AggregateStats emptyingFreqStat = new AggregateStats();

    private int numOfEmptyings = 0;

    private int currentEBLineNumber = 0;
    private int lastEBLineNumber = 0;
    private int lastEBUpdateAccesses = 0;
    private int lastEBTotalCompletedUpdates = 0;

    private final AggregateStats updateIOStat = new AggregateStats();

    private final AggregateStats ebCostStat = new AggregateStats();

    /**
     * Creates a new RRTree for the driver
     *
     * @param treeType the disk tree type
     */
    RRTree(final TreeType treeType) {
        IRRDiskTree<E> diskTree;
        if (treeType == TreeType.DATA_TREE)
            diskTree = new RRDiskDataTree<>();
        else if (treeType == TreeType.UPDATE_TREE)
            diskTree = new RRDiskUpdateTree<>();
        else
            throw new IllegalArgumentException("Unknown tree type!");
        tree = new aau.bufferedIndexes.RRTree<>(diskTree);
        bufferEmptied = false;
    }

    /**
     * Returns the underlying tree object.
     *
     * @return the tree object.
     */
    public aau.bufferedIndexes.RRTree<E> asTree() {
        return tree;
    }

    public void checkTreeStructure() {
        try {
            tree.getDiskTree().visitTreeNodes(null, new RRDiskTreeInvariantChecker<E>());
        }
        catch (IOException e) {
            throw new IllegalStateException("Shouldn't have caused I/O exception", e);
        }
    }

    /**
     * Empties the buffer, even if it is not full yet.
     */
    public void emptyBuffer() {
        tree.forcedEmptyBuffer();
    }

    /**
     * Gets a descriptor of the specified object.
     *
     * @param datum the object whose descriptor to get.
     * @return the descriptor of the object.
     */
    public Descriptor getDescriptor(final E datum) {
        return tree.descriptor(datum);
    }

    /**
     * Gets a descriptor covering all objects in the tree.
     *
     * @return the descriptor covering all objects in the tree.
     */
    public Descriptor getGlobalDescriptor() {
        return tree.rootDescriptor();
    }

    /**
     * Returns global group size statistics
     *
     * @return statistical data with number of occurrences data for every group size encountered.
     */
    public StatisticalData getGlobalGroupSizes() {
        return tree.getStats().getGlobalGroupSizeStatistics();
    }

    /**
     * Returns root group size statistics
     * @return statistical data with number of occurrences data for every root group size encountered.
     */
    public StatisticalData getRootGroupSizes() {
        final List<StatisticalData> stats = tree.getStats().getGroupSizeStatistics();
        return stats.size() > 0 ? stats.get(stats.size() - 1) : new StatisticalData();
    }

    /**
     * Returns the height of the disk tree.
     *
     * @return height of the disk tree.
     */
    public int getHeight() {
        return tree.getDiskTree().height();
    }

    /**
     * Returns current setting of piggybacking.
     *
     * @return piggybacking state flag, or <code>false</code>, if the tree does not have piggybacking concept.
     */
    public boolean getPiggybackingState() {
        return tree.getQueryPiggybackingState();
    }

    /**
     * Prepares the tree for usage. 
     *
     * @param descriptorGetter                  function to get descriptor from a data object.
     * @param container                         container to store data in.
     * @param minNodeCapacity                   maximum fanout.
     * @param maxNodeCapacity                   minimum fanout.
     * @param bufferSize                        buffer size
     * @param operationGrouper                  operation grouping strategy
     * @param enableUpdateIndexNodePiggybacking update piggybacking at index nodes flag
     * @param enableUpdateLeafNodePiggybacking  update piggybacking at leaf nodes flag
     * @param piggybackingEpsilon               update piggybacking epsilon value
     * @param gcIndexCacheSize                  size of index node cache for garbage collection
     * @param gcInitialScratchMemSize           initial scratch memory size for GC
     * @param enableQueryPiggybacking           query piggybacking flag
     * @param pushDownStrategy                  push down strategy
     * @param objectTracer                      object tracer to use
     */
    public void initialize(final Function<E, DataID> idGetter, final Function<E, Descriptor> descriptorGetter,
                           final Container container, final int minNodeCapacity, final int maxNodeCapacity,
                           final int bufferSize, final AbstractOperationGroupMaker operationGrouper,
                           final boolean enableUpdateIndexNodePiggybacking,
                           final boolean enableUpdateLeafNodePiggybacking, final double piggybackingEpsilon,
                           final int gcIndexCacheSize, final int gcInitialScratchMemSize,
                           final boolean enableQueryPiggybacking, final PushDownGroupsStrategy<E> pushDownStrategy,
                           final ObjectTracer<E> objectTracer) {
        tree.initialize(idGetter, descriptorGetter, container, minNodeCapacity, maxNodeCapacity,
                bufferSize, operationGrouper, enableUpdateIndexNodePiggybacking,
                enableUpdateLeafNodePiggybacking, piggybackingEpsilon, enableQueryPiggybacking, gcIndexCacheSize,
                gcInitialScratchMemSize, pushDownStrategy, objectTracer);
    }

    /**
     * Returns a converter to serialize nodes of the tree
     *
     * @param objectConverter the converter for the data object
     * @param dimensions      number of dimensions
     * @return converter for the nodes of the tree
     */
    @SuppressWarnings({"unchecked"})
    public <T> Converter<T> nodeConverter(final Converter<T> objectConverter, final int dimensions) {
        return tree.getDiskTree().nodeConverter(objectConverter, dimensions);
    }

    /**
     * Returns an entry for the root node of the tree.
     *
     * @return an entry for the root node.
     */
    public Tree.IndexEntry getRootEntry() {
        return (Tree.IndexEntry)tree.getDiskTree().rootEntry();
    }

    /**
     * Inserts a data object into the tree.
     *
     * @param datum the object to insert.
     */
    public void insert(final E datum) {
        tree.insert(datum);
        bufferEmptied = tree.wasBufferEmptied();
    }

    /**
     * Prints line numbers of two last EmptyBuffer invoking operations to System.out.
     */
    public void printSpecificLineNumbers() {
        System.err.println("(Current EB line " + currentEBLineNumber + ", last EB line " + lastEBLineNumber + ')');
    }

    /**
     * Prints tree-specific statistics to System.out.
     */
    public void printSpecificStats() {
        computeStatistics();

        System.out.println("Avg tree size before GC: " + tree.getAvgIndexNodesBeforeGc() + "i + "
                + tree.getAvgLeafNodesBeforeGc() + "l = " + (tree.getAvgIndexNodesBeforeGc()
                + tree.getAvgLeafNodesBeforeGc()));
        System.out.println("Avg tree size after GC: " + tree.getAvgIndexNodesAfterGc() + "i + "
                + tree.getAvgLeafNodesAfterGc() + "l = " + (tree.getAvgIndexNodesAfterGc()
                + tree.getAvgLeafNodesAfterGc()));
        System.out.println("GC partial file I/O: " + tree.getAvgHilbertReadIOs() + "r + "
                + tree.getAvgHilbertWriteIOs() + "w = " + (tree.getAvgHilbertReadIOs() + tree.getAvgHilbertWriteIOs()));

        System.out.println("Mean I/O per update, by last EmptyBuffer: " + updateIOStat.average());
        System.out.println("I/O per update standard deviation: " + updateIOStat.deviation());
        System.out.println("EmptyBuffer count: " + ebCostStat.count());
        System.out.println("EmptyBuffer cost mean: " + ebCostStat.average());
        System.out.println("EmptyBuffer cost standard deviation: " + ebCostStat.deviation());

        System.out.println("I>D Annihilations: " + tree.getNumOfIDAnnihilations());
        System.out.println("D>I Annihilations: " + tree.getNumOfDIAnnihilations());
        final RRTreeStats stats = tree.getStats();
        System.out.println(stats.getLeafUpdatePiggybackings());
        System.out.println(stats.getNonleafUpdatePiggybackings());
        System.out.println(stats.getQueryPiggybackings());

        System.out.println("Physical to logical data ratio in the tree: " + tree.getPhysicalToLogicalDataRatio());

        System.out.println("Peak number of simultaneously loaded nodes during GC: "
                + tree.getPeakNodesLoadedAtOnceDuringGC());

        printGroupSizeStatistics();
        printGlobalGroupSizeStatistics();
        printOperationLifetimeStatistics();
        printBackToBufferGroupStatistics();
        printDeletionSplitStatistics();

        final int totalIntegrators = stats.getGroupUpdateInvocations() + numOfEmptyings;

        System.out.println("Average interval between emptying: " + emptyingFreqStat.average());
        System.out.println("Standard deviation of emptying intervals: " + emptyingFreqStat.deviation());
        printTimeBetweenEmptyingStatistics();
        System.out.println("Total and failed (%) buffer emptyings:" + numOfEmptyings + ", "
                + stats.getFailedEmptyings() + " (" + (float) stats.getFailedEmptyings() / numOfEmptyings + ')');
        System.out.println("Total, leaf (%), non-leaf (%), restarted (% of non-leaf) GroupUpdate invocations: "
                + stats.getGroupUpdateInvocations() + ", " + stats.getLeafNodeUpdates() + " ("
                + (float) stats.getLeafNodeUpdates() / stats.getGroupUpdateInvocations() + "), "
                + stats.getNonLeafNodeUpdates() + " ("
                + (float) stats.getNonLeafNodeUpdates() / stats.getGroupUpdateInvocations() + "), "
                + stats.getGroupUpdateRestarts() + " ("
                + (float) stats.getGroupUpdateRestarts() / stats.getNonLeafNodeUpdates() + ')');
        System.out.println("Number of single entry nodes upon GroupUpdate or EmptyBuffer exit (%): "
                + stats.getSingleEntryNodes() + " (" + (float) stats.getSingleEntryNodes() / totalIntegrators + ')');
        System.out.println("Times recursive GroupUpdate returning more than one child (% of non-leaf GroupUpdate): "
                + stats.getTrivialChildIntegrations() +
                " (" + (float) stats.getTrivialChildIntegrations() / stats.getNonLeafNodeUpdates() + ')');
        System.out.println("Times recursive GroupUpdate returning empty child (%): " + stats.getEmptyChildren()
                + " (" + (float) stats.getEmptyChildren() / stats.getNonLeafNodeUpdates() + ')');
        System.out.println("IntegrateChild invocations (% of non-leaf GroupUpdate): "
                + stats.getIntegrateChildInvocations() + " ("
                + (float) stats.getIntegrateChildInvocations() / stats.getNonLeafNodeUpdates() + ')');
        System.out.println("Times children replacing parent (% of IntegrateChild): "
                + stats.getChildrenReplacingParent() + " ("
                + (float) stats.getChildrenReplacingParent() / stats.getIntegrateChildInvocations() + ')');
        System.out.println("InsertSubtree (% of IntegrateChild), recursive (%) invocations: "
                + stats.getInsertSubtreeInvocations() + " (" +
                (float) stats.getInsertSubtreeInvocations() / stats.getIntegrateChildInvocations() + "), "
                + stats.getInsertSubtreeRecursion() + " ("
                + (float) stats.getInsertSubtreeRecursion() / stats.getInsertSubtreeInvocations() + ')');
        System.out.println("MergeSubtree (% of IntegrateChild), recursive (%) invocations: "
                + stats.getMergeSubtreeInvocations() + " (" +
                (float) stats.getMergeSubtreeInvocations() / stats.getIntegrateChildInvocations() + "), "
                + stats.getMergeSubtreeRecursion() + " ("
                + (float) stats.getMergeSubtreeRecursion() / stats.getMergeSubtreeInvocations() + ')');
        System.out.println("D and I that were not piggybacked because of node size constraints: "
                + stats.getTotalNonPiggybackedNodeSizeDecreasingOps() + ", " + stats.getTotalNonPiggybackedNodeSizeIncreasingOps());
        printGcTreeRebuildStats();
    }

    private void printGcTreeRebuildStats() {
        System.out.println("GC tree rebuild stat averages:");
        System.out.println("Nodes created by partial unloads: " + tree.getGcAvgPartialUnloadCreatedNodes());
        System.out.println("Ratio of nodes causing/created by partial unload: " + tree.getGcAvgPartialUnloadRatio());
        System.out.println("Part of nodes causing partial unload: " + tree.getGcAvgPartOfNodesCleanedWithUnload());
        System.out.println("Part of tree cleaned before last partial unload: " + tree.getGcAvgPartOfTreeCleanedWithUnload());
        System.out.println("Unloaded entries: " + tree.getGcAvgUnloadedEntries());
        System.out.println("Partial cleaning iterations: " + tree.getGcAvgPartialCleaningIterations());
    }

    private void printOperationLifetimeStatistics() {
        System.out.println("Operation lifetime statistics:");
        final Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> lifetimeStats = tree.getStats().getOpLifetimeStats();
        for (final Map.Entry<Integer, OpLifetimeStats.TotalsForEB> statItem : lifetimeStats) {
            final OpLifetimeStats.TotalsForEB stat = statItem.getValue();
            System.out.print("EBs survived: " + statItem.getKey() + ", num of ops: " + stat.getNumberOfOps());
            final List<Integer> timesReturnedToBuffer = stat.getTimesReturnedToBuffer();
            int level = 0;
            for (final Integer toBuffer : timesReturnedToBuffer) {
                System.out.print(", L: " + level + " returned: " + toBuffer);
                level++;
            }
            System.out.println();
        }
    }

    private void computeStatistics() {
        assert numOfEmptyings == 0;
                final StatisticalData emptyFreq = tree.getStats().getEmptyingFrequencies();
        for (final Map.Entry<Integer, Integer> freq : emptyFreq.entrySet()) {
            for (int i = 0; i < freq.getValue(); i++) {
                numOfEmptyings++;
                emptyingFreqStat.registerValue(freq.getKey());
            }
        }
        assert numOfEmptyings == ebCostStat.count();
    }

    /**
     * Performs a range query over the tree.
     *
     * @param queryRectangle the range query rectangle.
     * @return a cursor over the objects overlapping with the query rectangle.
     */
    @SuppressWarnings({"unchecked"})
    public Cursor<E> query(final Descriptor queryRectangle) {
        return tree.query(queryRectangle);
    }

    /**
     * Record operation lifetime stats for all the ops currently in the buffer. To be called at the end of test run.
     */
    public void registerBufferLifetimes() {
        tree.registerBufferLifetimes();
    }

    /**
     * Removes a data object from the tree.
     *
     * @param datum the object to remove.
     */
    public void remove(final E datum) {
        tree.remove(datum);
        bufferEmptied = tree.wasBufferEmptied();
    }

    /**
     * Enables or disables piggybacking, if applicable to the tree used.
     *
     * @param enablePiggybacking new value of piggybacking flag.
     */
    public void setPiggybackingState(final boolean enablePiggybacking) {
        tree.setQueryPiggybackingState(enablePiggybacking);
    }

    /**
     * Update RR-tree specific statistics after each update operation.
     * @param lineNumber line number of the operation
     * @param updateAccesses total I/Os performed for the update operations
     * @param insertions number of insertions performed
     * @param deletions number of deletions performed
     */
    public void updateSpecificStats(final int lineNumber, final int updateAccesses, final int insertions,
                                    final int deletions) {
        if (wasBufferEmptied()) {
            lastEBLineNumber = currentEBLineNumber;
            currentEBLineNumber = lineNumber;
            final int ebCost = updateAccesses - lastEBUpdateAccesses;
            lastEBUpdateAccesses = updateAccesses;
            final int currentEBCompletedOps = insertions + deletions - lastEBTotalCompletedUpdates;
            lastEBTotalCompletedUpdates = insertions + deletions;
            final double avgEBUpdateCost = (double)ebCost / currentEBCompletedOps;

            ebCostStat.registerValue(ebCost);
            for (int i = 0; i < currentEBCompletedOps; i++)
                updateIOStat.registerValue(avgEBUpdateCost);
        }
    }

    /**
     * Returns a flag of last update operation causing buffer emptying.
     *
     * @return <code>true</code>, if the buffer was emptied by the last update operation, <code>false</code> otherwise
     *         or if the tree does not have concept of buffer emptying.
     */
    public boolean wasBufferEmptied() {
        return bufferEmptied;
    }

    private void printDeletionSplitStatistics() {
        System.out.println("Deletion split statistics:");
        final StatisticalData deletionSplits = tree.getDeletionSplits();
        for (final Map.Entry<Integer, Integer> splits : deletionSplits.entrySet()) {
            System.out.println("Split size: " + splits.getKey() + ", number of splits: " + splits.getValue());
        }
    }

    private void printBackToBufferGroupStatistics() {
        System.out.println("Back to buffer group size statistics:");
        final Collection<StatisticalData> backToBufferStats = tree.getStats().getBackToBufferGroupSizes();
        System.out.println("Levels: " + backToBufferStats.size());
        int level = 0;
        for (final StatisticalData levelStats : backToBufferStats) {
            System.out.println("Level " + level);
            for (final Map.Entry<Integer, Integer> numOfGroups : levelStats.entrySet()) {
                System.out.println("Group size: " + numOfGroups.getKey() + ", num of groups: "
                        + numOfGroups.getValue());
            }
            level++;
        }
    }

    private void printGlobalGroupSizeStatistics() {
        System.out.println("Global group size statistics:");
        final StatisticalData globalStats = tree.getStats().getGlobalGroupSizeStatistics();
        int totals = 0;
        int totalGroups = 0;
        for (final Map.Entry<Integer, Integer> numOfGroups : globalStats.entrySet()) {
            System.out.println("Global: group size: " + numOfGroups.getKey() + " num of groups: " +
                    numOfGroups.getValue());
            totals += numOfGroups.getKey() * numOfGroups.getValue();
            totalGroups += numOfGroups.getValue();
        }
        System.out.println("Average group size: " + (double)totals / (double) totalGroups);
    }

    private void printGroupSizeStatistics() {
        System.out.println("Group size statistics:");
        final RRTreeStats<E> stats = tree.getStats();
        final Collection<StatisticalData> groupSizeStatistics = stats.getGroupSizeStatistics();
        final List<StatisticalData> insertionGroupSizeStatistics = stats.getInsertionOnlyGroupSizes();
        System.out.println("Levels: " + groupSizeStatistics.size());
        int level = 0;
        for (final StatisticalData statisticsForLevel : groupSizeStatistics) {
            System.out.println("Level " + level);
            int totals = 0;
            int totalGroups = 0;
            for (final Map.Entry<Integer, Integer> numOfGroups : statisticsForLevel.entrySet()) {
                totals += numOfGroups.getKey() * numOfGroups.getValue();
                totalGroups += numOfGroups.getValue();
                System.out.print("Group size: " + numOfGroups.getKey() + " Num of groups: " + numOfGroups.getValue());
                if ((insertionGroupSizeStatistics.size() >= (level + 1)) &&
                        (insertionGroupSizeStatistics.get(level).containsKey(numOfGroups.getKey()))) {
                    System.out.println(" Num of i-only groups: " +
                            insertionGroupSizeStatistics.get(level).get(numOfGroups.getKey()));
                }
                else
                    System.out.println();
            }
            System.out.println ("Average group size for level " + level + ": " +
                    (double)totals / (double) totalGroups);
            level++;
        }
    }

    private void printTimeBetweenEmptyingStatistics() {
        final StatisticalData emptyFreq = tree.getStats().getEmptyingFrequencies();
        System.out.println("Buffer emptying frequencies");
        for (final Map.Entry<Integer, Integer> freq : emptyFreq.entrySet()) {
            System.out.println("Time between emptyings: " + freq.getKey() + " Num of emptyings: " + freq.getValue());
        }
    }

    /**
     * Returns the physical to logical data ratio, i.e. how much the tree is bigger than if it only stored the logical
     * data items.
     *
     * @return the physical to logical data ratio
     */
    public float getPhysicalToLogicalDataRatio() {
        return tree.getPhysicalToLogicalDataRatio(); 
    }

    /**
     * Cleans the garbage in the disk tree, if applicable.
     * 
     * @param rebuildTree if <code>true</code>, a new disk tree is built
     * @return TreeClearIOState object with old disk tree disposal I/O counts
     */
    public TreeClearIOState cleanGarbage(boolean rebuildTree) {
        try {
            return tree.cleanGarbage(rebuildTree);
        }
        catch (IOException e) {
            throw new IllegalStateException("GC caused I/O error", e);
        }
    }

    /**
     * Makes the tree to do full buffer emptying the next time buffer becomes full, regardless of the default buffer
     * emptying strategy, if applicable.
     */
    public void onNextEbForceFullEmptying() {
        tree.onNextEbForceFullEmptying();
    }

    @Override
    public int getTmpGcReads() {
        return tree.getTotalHilbertReadIOs();
    }

    @Override
    public int getTmpGcWrites() {
        return tree.getTotalHilbertWriteIOs();
    }

    @Override
    public void registerPersistenceIo(TestIO ioSystem) {
        ioSystem.registerPersistenceIO();
    }
}
