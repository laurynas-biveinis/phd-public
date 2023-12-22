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
import xxl.core.io.Convertable;
import xxl.core.io.converters.Converter;

/**
 * Interface that the tested tree must implement
 */
interface TreeDriver<E extends Convertable> {

    /**
     * Returns the underlying tree object.
     * @return the tree object.
     */
    xxl.core.indexStructures.Tree asTree();

    /**
     * Checks the internal tree structure
     */
    void checkTreeStructure();

    /**
     * Flushes the buffer, if applicable.
     */
    void emptyBuffer();

    /**
     * Gets a descriptor of the specified object.
     * @param datum the object whose descriptor to get.
     * @return the descriptor of the object.
     */
    Descriptor getDescriptor(final E datum);

    /**
     * Gets a descriptor covering all objects in the tree.
     * @return the descriptor covering all objects in the tree.
     */
    Descriptor getGlobalDescriptor();

    /**
     * Returns global group size statistics
     * @return statistical data with number of occurences data for every group size encountered.
     */
    StatisticalData getGlobalGroupSizes();

    /**
     * Returns root group size statistics
     * @return statistical data with number of occurences data for every root group size encountered.
     */
    StatisticalData getRootGroupSizes();

    /**
     * Returns the height of the tree.
     * @return height of the tree.
     */
    int getHeight();

    /**
     * Returns current setting of piggybacking.
     * @return piggybacking state flag, or <code>false</code>, if the tree does not have piggybacking concept.
     */
    boolean getPiggybackingState();

    /**
     * Prepares the tree for usage. Some of the arguments below will be ignored by the tree types that do not
     * understand them.
     *
     * @param idGetter                           function to get id from a data object
     * @param descriptorGetter                   function to get descriptor from a data object
     * @param container                          container to store data in
     * @param minNodeCapacity                    maximum fanout
     * @param maxNodeCapacity                    minimum fanout
     * @param bufferSize                         buffer size
     * @param operationGroupMaker                operation groupping strategy
     * @param enableUpdateIndexNodePiggybacking  update piggybacking at index nodes flag
     * @param enableUpdateLeafNodePiggybacking   update piggybacking at leaf nodes flag
     * @param piggybackingEpsilon                update piggybacking epsilon value
     * @param gcIndexCacheSize                   size of index node cache for garbage collection
     * @param gcInitialScratchMemSize            initial scratch memory size for GC
     * @param enableQueryPiggybacking            query piggybacking flag
     * @param pushDownStrategy                   push down strategy
     * @param objectTracer                       object tracer to use
     */
    void initialize(final Function<E, DataID> idGetter, final Function<E, Descriptor> descriptorGetter,
                    final Container container, final int minNodeCapacity, final int maxNodeCapacity,
                    final int bufferSize, final AbstractOperationGroupMaker operationGroupMaker,
                    final boolean enableUpdateIndexNodePiggybacking, final boolean enableUpdateLeafNodePiggybacking,
                    final double piggybackingEpsilon, final int gcIndexCacheSize, final int gcInitialScratchMemSize,
                    final boolean enableQueryPiggybacking, final PushDownGroupsStrategy<E> pushDownStrategy,
                    final ObjectTracer<E> objectTracer);

    /**
     * Returns a converter to serialize nodes of the tree
     * @param objectConverter the converter for the data object
     * @param dimensions number of dimensions
     * @return converter for the nodes of the tree
     */
    <T> Converter<T> nodeConverter (final Converter<T> objectConverter, final int dimensions);

    /**
     * Returns an entry for the root node of the tree.
     * @return an entry for the root node.
     */
    xxl.core.indexStructures.Tree.IndexEntry getRootEntry();

    /**
     * Inserts a data object into the tree.
     * @param datum the object to insert.
     */
    void insert(final E datum);

    /**
     * Prints any tree-specific line numbers to System.err.
     */
    void printSpecificLineNumbers();
                                       
    /**
     * Prints any tree-specific statistics to System.out.  Gets info needed to compute tree-specific stats.
     */
    void printSpecificStats();

    /**
     * Performs a range query over the tree.
     * @param queryRectangle the range query rectangle.
     * @return a cursor over the objects overlapping with the query rectangle.
     */
    Cursor<E> query(final Descriptor queryRectangle);

    /**
     * Record operation lifetime stats for all the ops currently in the buffer. To be called at the end of test run.
     */
    void registerBufferLifetimes();

    /**
     * Removes a data object from the tree.
     * @param datum the object to remove.
     */
    void remove(final E datum);

    /**
     * Enables or disables piggybacking, if applicable to the tree used.
     * @param enablePiggybacking new value of piggybacking flag.
     */
    void setPiggybackingState(final boolean enablePiggybacking);

    /**
     * Update any tree specific stats after each update operation.
     * @param lineNumber line number of the operation
     * @param updateAccesses total I/Os performed for the update operations
     * @param insertions number of insertions performed
     * @param deletions number of deletions performed
     */
    void updateSpecificStats(final int lineNumber, final int updateAccesses, final int insertions,
                             final int deletions);

    /**
     * Returns a flag of last update operation causing buffer emptying.
     * @return <code>true</code>, if the buffer was emptied by the last update operation, <code>false</code> otherwise
     * or if the tree does not have concept of buffer emptying.
     */
    boolean wasBufferEmptied();

    /**
     * Returns the physical to logical data ratio, i.e. how much the tree is bigger than if it only stored the logical
     * data items.
     *
     * @return the physical to logical data ratio
     */
    float getPhysicalToLogicalDataRatio();
    
    /**
     * Cleans the garbage in the disk tree, if applicable.
     *
     * @param rebuildTree if <code>true</code>, a new disk tree is built
     * @return TreeClearIOState object with old disk tree disposal I/O counts
     */
    TreeClearIOState cleanGarbage(boolean rebuildTree);

    /**
     * Makes the tree to do full buffer emptying the next time buffer becomes full, regardless of the default buffer
     * emptying strategy, if applicable.
     */
    void onNextEbForceFullEmptying();

    int getTmpGcReads();

    int getTmpGcWrites();

    /**
     * Register additional I/Os required for persistent operation of the tree after an update operation.
     *
     * @param ioSystem the I/O system to register I/Os in
     */
    void registerPersistenceIo(final TestIO ioSystem);
}
