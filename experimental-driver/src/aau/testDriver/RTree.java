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

import aau.bufferedIndexes.StatisticalData;
import aau.bufferedIndexes.diskTrees.TreeClearIOState;
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

/**
 * An R-tree for the experimental driver
 */
public final class RTree<E extends Convertable> implements TreeDriver<E> {

    private final xxl.core.indexStructures.RTree tree;

    RTree() {
        tree = new xxl.core.indexStructures.RTree();
    }

    public void checkTreeStructure() {
        // No checking for the R-tree
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
        return tree.nodeConverter(objectConverter, dimensions);
    }

    /**
     * Returns the underlying tree object.
     *
     * @return the tree object.
     */
    public Tree asTree() {
        return tree;
    }

    /**
     * Does nothing for the R-tree.
     */
    public void emptyBuffer() {  }

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
     * @return an empty statistics
     */
    public StatisticalData getGlobalGroupSizes() {
        return new StatisticalData();
    }

    /**
     * Returns root group size statistics
     * @return an empty statistics
     */
    public StatisticalData getRootGroupSizes() {
        return new StatisticalData();
    }

    /**
     * Returns the height of the tree.
     *
     * @return height of the tree.
     */
    public int getHeight() {
        return tree.height();
    }

    /**
     * Returns current setting of piggybacking.
     *
     * @return Always <code>false</code> for the R-tree.
     */
    public boolean getPiggybackingState() {
        return false;
    }

    /**
     * Prepares the tree for usage.
     *
     * @param idGetter                           ignored
     * @param descriptorGetter                   function to get descriptor from a data object.
     * @param container                          container to store data in.
     * @param minNodeCapacity                    maximum fanout.
     * @param maxNodeCapacity                    minimum fanout.
     * @param bufferSize                         ignored.
     * @param operationGroupMaker                ignored.
     * @param enableUpdateIndexNodePiggybacking  ignored.
     * @param enableUpdateLeafNodePiggybacking   ignored.
     * @param piggybackingEpsilon                ignored.
     * @param gcIndexCacheSize                   ignored.
     * @param gcInitialScratchMemSize            ignored
     * @param enableQueryPiggybacking            ignored.
     * @param pushDownStrategy                   ignored.
     * @param objectTracer                       ignored
     */
    public void initialize(final Function<E, DataID> idGetter, final Function<E, Descriptor> descriptorGetter,
                           final Container container, final int minNodeCapacity, final int maxNodeCapacity,
                           final int bufferSize, final AbstractOperationGroupMaker operationGroupMaker,
                           final boolean enableUpdateIndexNodePiggybacking,
                           final boolean enableUpdateLeafNodePiggybacking, final double piggybackingEpsilon,
                           final int gcIndexCacheSize, final int gcInitialScratchMemSize,
                           final boolean enableQueryPiggybacking, final PushDownGroupsStrategy<E> pushDownStrategy,
                           final ObjectTracer<E> objectTracer) {
        tree.initialize(descriptorGetter, container, minNodeCapacity, maxNodeCapacity);
    }

    /**
     * Returns an entry for the root node of the tree.
     *
     * @return an entry for the root node.
     */
    public xxl.core.indexStructures.Tree.IndexEntry getRootEntry() {
        return tree.rootEntry();
    }

    /**
     * Inserts a data object into the tree.
     *
     * @param datum the object to insert.
     */
    public void insert(final E datum) {
        tree.insert(datum);
    }

    /**
     * Prints any tree-specific line numbers to System.out.  Nothing for the R-tree.
     */
    public void printSpecificLineNumbers() { }

    /**
     * Prints any tree-specific statistics to System.out.  Does nothing for the R-tree.
     */
    public void printSpecificStats() { }

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
     * Does nothing for the R-tree.
     */
    public void registerBufferLifetimes() { }

    /**
     * Removes a data object from the tree.
     *
     * @param datum the object to remove.
     */
    public void remove(final E datum) {
        tree.remove(datum);
    }

    /**
     * Enables or disables piggybacking, if applicable to the tree used.
     *
     * @param enablePiggybacking new value of piggybacking flag.
     */
    public void setPiggybackingState(final boolean enablePiggybacking) {  }

    /**
     * Update any tree specific stats after each update operation. Does nothing for the R-tree
     * @param lineNumber line number of the operation
     * @param updateAccesses total I/Os performed for the update operations
     * @param insertions number of insertions performed
     * @param deletions number of deletions performed
     */
    public void updateSpecificStats(final int lineNumber, final int updateAccesses, final int insertions,
                             final int deletions) { }

    /**
     * Returns a flag of last update operation causing buffer emptying.
     *
     * @return <code>false</code>.
     */
    public boolean wasBufferEmptied() {
        return false;
    }

    /**
     * Returns the physical to logical data ratio, i.e. how much the tree is bigger than if it only stored the logical 
     * data items.  For the R-tree it is always 1.0.
     *
     * @return the physical to logical data ratio
     */
    public float getPhysicalToLogicalDataRatio() {
        return 1.0F;
    }

    /**
     * Cleans the garbage in the disk tree, if applicable.
     *
     * @param rebuildTree ignored
     * @return TreeClearIOState object with old disk tree disposal I/O counts of zero 
     */
    public TreeClearIOState cleanGarbage(boolean rebuildTree) {
        return new TreeClearIOState(0, 0);
    }

    @Override
    public int getTmpGcReads() {
        return 0;
    }

    @Override
    public int getTmpGcWrites() {
        return 0;
    }

    /**
     * Ignored.
     */
    public void onNextEbForceFullEmptying() {
    }

    @Override
    public void registerPersistenceIo(TestIO ioSystem) {
        // The R-tree is persistent by default
    }
}
