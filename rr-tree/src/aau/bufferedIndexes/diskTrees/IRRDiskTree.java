/*
     Copyright (C) 2010, 2011, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.diskTrees;

import aau.bufferedIndexes.OperationTypeStat;
import aau.bufferedIndexes.RRTreeGroupSplitter;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.diskTrees.visitors.IRRDiskTreeVisitor;
import aau.bufferedIndexes.leafNodeModifiers.IRRTreeDiskNodeOnQueryModifier;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
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

/**
 * An interface that RRDiskTree should implement
 */
public interface IRRDiskTree<E extends Convertable> {

    /**
     * Initializes this tree.
     *
     * @param getId          the function that returns the ID of a given object
     * @param getDescriptor  the function that returns the descriptor of a given object
     * @param container      the container which should store tree nodes
     * @param minCapacity    the minimum capacity of a tree node
     * @param maxCapacity    the maximum capacity of a tree node
     * @param objectTracer   the object tracer for this tree
     * @return this tree initialized
     */
    public Tree initialize(final Function<E, DataID> getId, final Function<E, Descriptor> getDescriptor,
                           final Container container, final int minCapacity, final int maxCapacity,
                           final ObjectTracer<E> objectTracer);

    // TODO: javadoc
    public TreeClearIOState clearWithIOCount ();

    /**
     * Returns the MBR descriptor of an index entry or node.
     *
     * @param entry the index entry or node
     * @return the MBR descriptor of entry
     */
    public Descriptor descriptor (Object entry);

    /**
     * Returns the MBR descriptor of an operation.
     *
     * @param entry the operation
     * @return the MBR descriptor of operation
     */
    public Descriptor descriptor(final UpdateTree.Entry<E> entry);

    /**
     * Returns the id of a data item.
     *
     * @param data the data to return the id of
     * @return the id of the data
     */
    public Object id (E data);

    public Container container ();

    /**
     * Returns the height of the tree
     * @return the height of the tree.
	 */
	public int height ();

    /**
     * Returns the entry of the root node of tree.
     * @return the entry of the root node of tree
     */
    // TODO: the proper return type is IRRTreeIndexEntry<E> or at least Tree.IndexEntry but the hell breaks loose with
    // TODO: mock classes in TestUtils
    public Object rootEntry ();

    /**
     * Returns the minimum allowed number of entries in a node.
     * @return the minimum allowed number of entries in a node
     */
    public int getMinNodeCapacity();

    /**
     * Returns the maximum allowed number of entries in a node.
     * @return the maximum allowed number of entries in a node
     */
    public int getMaxNodeCapacity();

    /**
     * Creates a new node of this tree with a given level and node contents.
     * @param level the level of the new node
     * @param nodeContents the contents of the new node
     * @return the new node created with a given level and contents
     */
    public IRRTreeDiskNode<E> createNode(final int level, final List<?> nodeContents);

    /**
     * Creates a new empty node of this tree with a given level.
     * @param level the level of the new node
     * @return the new node created with a given level
     */
    public IRRTreeDiskNode<E> createNode(final int level);

    /**
     * Creates a new index entry of this tree one level below given level.
     * @param parentLevel the parent level of this entry
     * @return the newly created index entry
     */
    public IRRTreeIndexEntry<E> createIndexEntry (int parentLevel);

    /**
     * Retrieves from disk and returns the root node of the tree.
     * @return the root node of the tree
     */
    public IRRTreeDiskNode<E> getRootNode();

    /**
     * Stores node in the container and computes its index entry.
     * @param newNode node to store and compute index entry for
     * @param alwaysUnfix if <code>true</code>, node should be unfixed in the buffer regardless of caching strategy.
     * Used for temporary root nodes as caching strategy does not know that they are temporary.
     * @return new index entry
     */
    public IRRTreeIndexEntry<E> storeNode(final IRRTreeDiskNode<E> newNode, final boolean alwaysUnfix);

    /**
     * For a list of nodes, writes them to the tree container and creates the corresponding IndexEntry list.  One of
     * the nodes in the list might be original "old" node (usually the case after split).  It is specified by the
     * parameters <code>originalEntry</code> and <code>originalNode</code> and is updated in the container in-place.
     * @param nodes nodes to write to the container and to create IndexEntry list for.
     * @param originalEntry index entry for the original old node.
     * @param originalNode original old node
     * @return list of index entries for the nodes
     */
    public List<IRRTreeIndexEntry<E>> storeNodes(final Iterable<IRRTreeDiskNode<E>> nodes,
                                                 final IRRTreeIndexEntry<E> originalEntry,
                                                 final IRRTreeDiskNode<E> originalNode);

    /**
     * Returns the descriptor of the whole tree (i.e. the MBR of everything).
     * @return the descriptor of the whole tree
     */
    public Descriptor rootDescriptor ();

    /**
     * Updates node in the container and recomputes its index entry which is given the id of the old entry.
     *
     * @param node node to update and recompute index entry for
     * @param oldEntry old index entry for the node
     * @param alwaysUnfix if <code>true</code>, node should be unfixed in the buffer regardless of caching strategy.
     * Used for temporary root nodes as caching strategy does not know that they are temporary.
     * @return new index entry
     */
    public IRRTreeIndexEntry<E> takeOverNode(final IRRTreeDiskNode<E> node, final IRRTreeIndexEntry<E> oldEntry,
                                             final boolean alwaysUnfix);

    // TODO: javadoc
    public void setNewRootNode(final IRRTreeIndexEntry<E> newRootEntry);

    /**
     * Create new upper-level nodes and grow the tree as necessary to put all of the new nodes to the disk.
     *
     * @param rootNode       the root node of the tree
     * @param siblings       the list of the nodes to put on the disk
     * @param groupSplitter  the node splitting algorithm to use
     */
    public void growTree(IRRTreeDiskNode<E> rootNode,
                         List<IRRTreeDiskNode<E>> siblings,
                         final RRTreeGroupSplitter groupSplitter);

    /**
     * Gets a suitable converter to deserialize the nodes of this tree.
     *
     * @param objectConverter a converter to convert the data objects stored in the tree
     * @param dimensions number of data dimensions
     * @return a converter for deserializing the nodes of this tree
     */
    public Converter nodeConverter (final Converter objectConverter, final int dimensions);

    /**
     * Visits the tree nodes, both index and leaf.
     *
     * @param indexNodeContainer  an intermediate buffering container for the index nodes
     * @param visitor             the visitor for node visiting
     * @throws IOException if I/O error occurs during processing.
     */
    public void visitTreeNodes(final Container indexNodeContainer, final IRRDiskTreeVisitor<E> visitor)
            throws IOException;

    /**
     * Gets all ids of all the leaf nodes that intersect a given rectangle.
     *
     * @param indexNodeContainer  a intermediate buffering container for the index nodes
     * @param descriptor          a rectangle to check intersection with
     * @return a collection of intersecting leaf node ids.
     */
    public Collection<Object> fetchIntersectingLeafNodeIDs(final Container indexNodeContainer,
                                                           final Descriptor descriptor);

    /**
     * Performs a spatial query on the tree with optional leaf node modifications
     *
     * @param queryDescriptor            the query descriptor
     * @param externalResults            cursor over partial query results coming from
     *                                   outside (i.e. from the buffer)
     * @param leafNodeModifier           the modifier for any accessed leaf nodes
     * @param leafNodeModificationStats  the leaf node modification statistics
     * @return cursor over query results
     */
    public Cursor<E> rrQuery(final Descriptor queryDescriptor,
                             final Cursor<UpdateTree.Entry<E>> externalResults,
                             final IRRTreeDiskNodeOnQueryModifier<E> leafNodeModifier,
                             final OperationTypeStat leafNodeModificationStats);

    // TODO: javadoc
    boolean deletionsLikeInsertions();

    /**
     * Returns the number of data items in the tree.
     * @return the number of data items in the tree
     */
    public int getDataItems();

    public TreeClearIOState cleanGarbage();
}
