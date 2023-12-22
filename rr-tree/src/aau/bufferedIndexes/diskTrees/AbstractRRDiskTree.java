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

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.visitors.IRRDiskTreeVisitor;
import aau.bufferedIndexes.leafNodeModifiers.IRRTreeDiskNodeOnQueryModifier;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.workload.DataID;
import xxl.core.collections.MapEntry;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.CounterContainer;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.sources.SingleObjectCursor;
import xxl.core.cursors.unions.Sequentializer;
import xxl.core.functions.Constant;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.RTree;
import xxl.core.indexStructures.Tree;
import xxl.core.io.Convertable;
import xxl.core.predicates.Predicate;

import java.io.IOException;
import java.util.*;

/**
 * AbstractRRDiskTree is a common base class for the various RR-tree disk tree part implementations.
 */
public abstract class AbstractRRDiskTree<E extends Convertable> extends RTree implements IRRDiskTree<E> {

    /**
     * The common ancestor for various RR-tree disk tree nodes
     */
    public abstract class Node extends RTree.Node implements IRRTreeDiskNode<E>, Cloneable {

        /* Constructors */

        /**
         * Creates a new node with a given level and node contents.
         *
         * @param level the level of the node
         * @param newEntries the contents of the node
         */
        public Node (final int level, final Collection<?> newEntries) {
            super.initialize(level);

            entries = new ArrayList(newEntries == null ? 1 : newEntries.size());
            if (newEntries != null) {
                //noinspection unchecked
                entries.addAll(newEntries);
            }
        }

        // TODO: push down to RRDiskUpdateTree?
        Set<?> hashedEntries = null;

        boolean haveHashedEntries = false;

        public void indexEntries() {
            // TODO: generify properly
            //noinspection unchecked
            hashedEntries = new HashSet(getEntries());
            haveHashedEntries = true;
        }

        public void deleteEntryIndex() {
            hashedEntries.clear();
            hashedEntries = null;
            haveHashedEntries = false;
        }

        /* Entry data */

        /**
         * Adds a new data element to the node, both index and leaf level.
         * @param data an element to add
         */
        public void grow(final Object data) {
            grow(data, null);
        }

        /**
         * Removes a data element from the node, both index and leaf level.
         * @param data an element to remove
         */
        public void remove(final Object data) {
            entries.remove(data);
        }

        /**
         * Copies all entries from another node.
         *
         * @param n node to copy from
         */
        public void addEntriesFrom(final IRRTreeDiskNode<E> n) {
            // TODO: mixing node types should be forbidden
            ((ArrayList<?>)entries).ensureCapacity(number() + n.number());
            for (final Object entry : n.getEntries()) {
                grow(entry);
            }
        }

        /**
         * Returns a collection of this node entries.  It should not be a part of a public interface, it's
         * intended for use by GroupSplit algorithms only.
         * @return a collection of entries.
         */
        public final Collection<?> getEntries() {
            return entries;
        }

        /**
         * Returns a collection of this (non-leaf) node entries.
         * @return a collection of IndexEntry entries
         */
        public Collection<IRRTreeIndexEntry<E>> getNonLeafNodeEntries() {
            assert level > 0;
            //noinspection unchecked
            return entries;
        }

        /* Queries */

        /**
         * Returns a subset of children, whose descriptors overlap with a given object descriptor
         * @param object an object
         * @return an iterator over subset of child entries
         */
        public Iterator<IRRTreeIndexEntry<E>> query(final E object) {
            assert level > 0;
            //noinspection unchecked
            return query(descriptor(object));
        }

        public IRRTreeIndexEntry<E> chooseSubtreeByObject(final Object e) {
            //noinspection unchecked
            return (IRRTreeIndexEntry<E>)super.chooseSubtreeByDescriptor(descriptor(e));
        }

        /* Getters */

        /**
         * Returns <code>true</code> if the node contains more than the maximum allowed number of elements.
         * @return overflow value
         */
        public boolean overflows() {
            return super.overflows();
        }

        /**
         * Returns <code>true</code> if the node contains less than the minimum allowed number of elements.
         * @return underflow value
         */
        public boolean underflows() {
            return super.underflows();
        }

        /**
         * Computes a descriptor for the node
         * @return union of all node entry descriptors
         */
        public Descriptor computeDescriptor() {
            // Cache the result on a in-memory node if this shows up in profiles
            Descriptor result = null;
            //noinspection unchecked
            final Iterator<Descriptor> descriptor = descriptors();
            if (descriptor.hasNext()) {
                result = (Descriptor)descriptor.next().clone();
                while (descriptor.hasNext()) {
                    result.union(descriptor.next());
                }
            }
            return result;
        }

        /* Update support */

        /**
         * Returns the minimum allowed number of entries in a node after a split.
         *
         * @return the minimum allowed number of entries in a node after a split
         */
        public int splitMinNumber() {
            return super.splitMinNumber();
        }

        /**
         * Check the number of insertions and deletions that can be executed on this node and limit some of them so
         * that after executing the operations the node size constraints are not violated.
         *
         * @param piggybackingInfo the piggybacking info that holds numbers of candidate operations
         */
        public void limitNumberOfOperations(final IRRTreeLeafPiggybackingInfo piggybackingInfo) {
            int potentialNodeSize = number() + piggybackingInfo.nodeSizeChange();
            if (potentialNodeSize < getMinNodeCapacity())
                piggybackingInfo.limitSizeDecreasingOps(getMinNodeCapacity() - potentialNodeSize);
            else if (potentialNodeSize > getMaxNodeCapacity())
                piggybackingInfo.limitSizeIncreasingOps(potentialNodeSize - getMaxNodeCapacity());
        }
        
        /**
         * Loop through a given cursor and collect operations that can be performed on a node without enlarging its MBR,
         * disregarding if it will overflow or underflow as a result.  Closes cursor in the end.
         *
         * @param candidateSet a cursor of candidate operations
         * @param outsideMBRAllowed if <code>true</code>, then operations falling outside the current node MBR are
         * allowed 
         * @return a collection of operations that may be performed on this node
         */
        public Collection<UpdateTree.Entry<E>> selectFittingOperations(final Cursor<UpdateTree.Entry<E>> candidateSet,
                                                                       final boolean outsideMBRAllowed) {
            final Descriptor descriptor = computeDescriptor();
            final Collection<UpdateTree.Entry<E>> refinedCandidateSet = new ArrayList<>();
            while (candidateSet.hasNext()) {
                final UpdateTree.Entry<E> operation = candidateSet.next();
                if (doesOperationFit(descriptor, operation, outsideMBRAllowed))
                    refinedCandidateSet.add(operation);
                }
            candidateSet.close();
            return refinedCandidateSet;
        }

        /**
         * Determines if a given operation may be performed on a given node without, disregarding if it will overflow or
         * underflow as a result.
         *
         * @param descriptor        descriptor of the node
         * @param operation         the operation to consider
         * @param outsideMBRAllowed if <code>false</code>, only operations that will not enlarge the node MBR will be
         *                          considered.
         * @return <code>true</code> if operation may be performed on a given node, <code>false</code> otherwise
         */
        protected abstract boolean doesOperationFit(final Descriptor descriptor, final UpdateTree.Entry<E> operation,
                                                    final boolean outsideMBRAllowed);

        /**
         * Executes given operations on the node.  In some cases not all the operations will be actually executed, for
         * example, if a deletion did not find its corresponding data entry in the node.
         *
         * @param operations                   operations to execute
         * @param insertionRemovesOldInsertion if <code>true</code>, then each completed insertion will remove an old
         * entry from the tree.  This is used to simulate execution of deletions as if they were insertions.
         * @return a set of operations actually executed.
         */
        public Collection<UpdateTree.Entry<E>> executeOps(final Iterable<UpdateTree.Entry<E>> operations,
                                                          final boolean insertionRemovesOldInsertion) {
            final Collection<UpdateTree.Entry<E>> executedOps = new ArrayList<>();
            for (final UpdateTree.Entry<E> entry: operations) {
                boolean wasOpExecuted = executeOp(entry, insertionRemovesOldInsertion);
                if (wasOpExecuted)
                    executedOps.add(entry);
            }
            return executedOps;
        }

        /**
         * Try to execute the operation on the node.
         *
         * @param entry                         an operation to execute
         * @param insertionRemovesOldInsertion  if <code>true</code>, then each completed insertion will remove an old
         * entry from the tree.  This is used to simulate execution of deletions as if they were insertions.
         * @return <code>true</code> if this operation was executed, <code>false</code> otherwise.
         */
        protected abstract boolean executeOp(final UpdateTree.Entry<E> entry,
                                             final boolean insertionRemovesOldInsertion);

        /**
         * Changes the level of the node in the tree
         * @param newLevel the new level of the node
         */
        public void setLevel(final int newLevel) {
            level = newLevel;
        }
    }

    /**
     * The index entry class for the various RR-tree disk tree implementations
     */
    public class IndexEntry extends RTree.IndexEntry implements IRRTreeIndexEntry<E> {

        /**
         * Creates a new RR-Tree IndexEntry on a specified parent level
         *
         * @param parentLevel the parent level of the new index entry
         */
        public IndexEntry (int parentLevel) {
            super(parentLevel);
        }

        /**
         * Retrieves the node pointed to by this index entry from container.
         *
         * @return the node pointed to by this index entry
         */
        public Node get () {
            //noinspection unchecked
            return (Node)super.get();
        }

        /**
         * Retrieves the node pointed to by this index entry from container.  Additionally tell if this node can be
         * removed from the cache, if any, if required.
         *
         * @param unfix if <code>true</code>, then this node can be removed from cache if needed
         * @return the node pointed to by this index entry
         */
        public Node get(boolean unfix) {
            //noinspection unchecked
            return (Node)super.get(unfix);
        }

        /**
         * Overwrites the node referred to by the current index entry by a new node.
         *
         * @param node the new node
         */
        public void update (final IRRTreeDiskNode<E> node) {
            super.update ((Tree.Node)node);
        }

        /**
         * Overwrites the node referred to by the current index entry by a new node.
	     *
         * @param node the new node
	     * @param unfix signals whether the node can be removed from the underlying buffer
	     */
        public void update (final IRRTreeDiskNode<E> node, final boolean unfix) {
            super.update ((Tree.Node) node, unfix);
        }

        // TODO: javadoc, unit tests
        public boolean spatiallyContains (final E data) {
            return descriptor().contains(AbstractRRDiskTree.this.descriptor(data));
        }

    }

    /* Various tree parameters, functions and strategies */

    /**
     * Function that gets an ID of a data object.
     */
    private Function<E, DataID> getId;

    /**
     * Minimum allowed number of entries in a node
     */
    private int minNodeCapacity = -1;

    /**
     * Maximum allowed number of entries in a node
     */
    private int maxNodeCapacity = -1;

    /**
     * LRU caching strategy for the disk tree nodes
     */
    private final CachingStrategy cachingStrategy = new CachingStrategy(this);

    /**
     * Number of items in the disk tree
     */
    int dataItems = 0;

    /**
     * An object tracer for this tree
     */
    ObjectTracer<E> objectTracer = null;

    /* Initialization */

    /**
     * Initializes this tree.
     *
     * @param getId            the function that returns the ID of a given object
     * @param getDescriptor    the function that returns the descriptor of a given object
     * @param container        the container which should store tree nodes
     * @param minNodeCapacity  the minimum capacity of a tree node
     * @param maxNodeCapacity  the maximum capacity of a tree node
     * @param objectTracer     the object tracer for this tree
     * @return this tree initialized
     */
    public Tree initialize(final Function<E, DataID> getId, final Function<E, Descriptor> getDescriptor,
                           final Container container, final int minNodeCapacity, final int maxNodeCapacity,
                           final ObjectTracer<E> objectTracer) {
        super.initialize(getDescriptor, container, minNodeCapacity, maxNodeCapacity);
        this.getId = getId;
        this.minNodeCapacity = minNodeCapacity;
        this.maxNodeCapacity = maxNodeCapacity;
        this.objectTracer = objectTracer;
        return this;
    }

    /* Factories */

    /**
     * Creates a new empty node of this tree with a given level.
     * @param level the level of the new node
     * @return the new node created with a given level
     */
    abstract public Node createNode(final int level);

    // TODO: used only in tests. Inline in computeIndexEntry and use it for tests too
    /**
     * Creates a new index entry of this tree one level below given level.
     * @param parentLevel the parent level of this entry
     * @return the newly created index entry
     */
    public IndexEntry createIndexEntry (int parentLevel) {
        return new IndexEntry(parentLevel);
    }

    /**
     * Creates a new index entry for a given node and initializes the descriptor of the index entry.
     *
     * @param newNode a node to compute the index entry for
     * @return the newly created index entry
     */
    public IRRTreeIndexEntry<E> computeIndexEntry(final IRRTreeDiskNode<E> newNode) {
        final Descriptor nodeDescriptor = newNode.computeDescriptor();
        final IRRTreeIndexEntry<E> nodeEntry = createIndexEntry(newNode.level() + 1);
        nodeEntry.initialize(nodeDescriptor);
        return nodeEntry;
    }

    /* Tree parameter getters */

    /**
     * Returns the minimum allowed number of entries in a node.
     *
     * @return the minimum allowed number of entries in a node
     */
    public int getMinNodeCapacity() {
        return minNodeCapacity;
    }

    /**
     * Returns the maximum allowed number of entries in a node.
     *
     * @return the maximum allowed number of entries in a node
     */
    public int getMaxNodeCapacity() {
        return maxNodeCapacity;
    }

    /**
     * Returns the number of data items in the tree.
     * @return the number of data items in the tree
     */
    public int getDataItems() {
        return dataItems;
    }

    /* Root node management */

    /**
     * Retrieves from disk and returns the root node of the tree.
     * @return the root node of the tree
     */
    public IRRTreeDiskNode<E> getRootNode() {
        //noinspection unchecked
        return (rootEntry == null) ? createNode(0)
                : (IRRTreeDiskNode<E>)rootEntry.get(!cachingStrategy.shouldBeFixed((IRRTreeIndexEntry<E>)rootEntry));
    }

    // TODO: the only usage outside testing is growTree, make private, change tests
    /**
     * Updates tree root to point to the new root node
     * @param newRootNode the new disk tree root node
     */
    public void setNewRootNode(final IRRTreeDiskNode<E> newRootNode) {
        IRRTreeIndexEntry<E> newRootEntry;
        if (newRootNode.number() == 0) {
            rootEntry = null;
        }
        //noinspection unchecked
        newRootEntry = ((rootEntry == null) ? storeNode(newRootNode, false)
                                            : takeOverNode(newRootNode, (IRRTreeIndexEntry<E>)rootEntry, false));
        setNewRootNode(newRootEntry);
    }

    public void setNewRootNode(final IRRTreeIndexEntry<E> newRootEntry) {
        rootEntry = (Tree.IndexEntry)newRootEntry;
        //noinspection AssignmentToNull
        rootDescriptor = (rootEntry == null) ? null : descriptor(rootEntry);
    }

    /**
     * Create new upper-level nodes and grow the tree as necessary to put all of the new nodes to the disk.
     *
     * @param rootNode       the root node of the tree
     * @param siblings       the list of the nodes to put on the disk
     * @param groupSplitter  the node splitting algorithm to use
     */
    public void growTree(IRRTreeDiskNode<E> rootNode,
                         List<IRRTreeDiskNode<E>> siblings,
                         final RRTreeGroupSplitter groupSplitter) {
        if (siblings.size() == 1) {
            if (rootNode != siblings.get(0)) {
                rootEntry.remove();
                rootEntry = null;
                rootNode = siblings.get(0);
            }
        }
        while (siblings.size() > 1) {
            // Create a new root and add siblings to it
            //noinspection unchecked
            final List<IRRTreeIndexEntry<E>> siblingEntries
                    = storeNodes(siblings, (IRRTreeIndexEntry<E>)rootEntry, rootNode);
            rootNode = createNode(getHeight(siblingEntries) + 1, siblingEntries);
            rootEntry = null;
            siblings = groupSplitter.groupSplit(rootNode, this);
        }
        assert (rootNode == siblings.get(0));
        setNewRootNode(rootNode);
    }

    /* Disk I/O */

    /**
     * Stores node in the container and computes its index entry.
     * @param newNode node to store and compute index entry for
     * @param alwaysUnfix if <code>true</code>, node should be unfixed in the buffer regardless of caching strategy.
     * Used for temporary root nodes as caching strategy does not know that they are temporary.
     * @return new index entry
     */
    public IRRTreeIndexEntry<E> storeNode(final IRRTreeDiskNode<E> newNode, final boolean alwaysUnfix) {
        if (newNode.number() == 0) return null;
        if (newNode.overflows())
            throw new IllegalStateException("AbstractRRDiskTree.storeNode: overflow, entries = " + newNode.number());

        final IRRTreeIndexEntry<E> nodeEntry = computeIndexEntry(newNode);
        final Container container = (Container)determineContainer.invoke();
        final Object id = container.insert(newNode, alwaysUnfix || !cachingStrategy.shouldBeFixed(newNode));
        nodeEntry.initialize(container, id);

        return nodeEntry;
    }

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
                                                 final IRRTreeDiskNode<E> originalNode) {
        final List<IRRTreeIndexEntry<E>> indexEntries = new ArrayList<>();
        for (final IRRTreeDiskNode<E> node : nodes) {
            if (node == originalNode && (originalEntry != null)) {
                final IRRTreeIndexEntry<E> newEntry = takeOverNode(node, originalEntry, true);
                indexEntries.add(newEntry);
            }
            else {
                final IRRTreeIndexEntry<E> newEntry = storeNode(node, true);
                if (newEntry != null)
                    indexEntries.add(newEntry);
            }
        }
        return indexEntries;
    }

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
                                             final boolean alwaysUnfix) {
        if (node.number() == 0) return null;
        assert (!node.overflows());

        final IRRTreeIndexEntry<E> nodeEntry = computeIndexEntry(node);
        final Container container = (Container)determineContainer.invoke();
        nodeEntry.initialize(container, oldEntry.id());
        nodeEntry.update(node, alwaysUnfix || !cachingStrategy.shouldBeFixed(node));

        return nodeEntry;
    }

    /* Queries */

    /**
     * Performs a spatial query on the tree with optional leaf node modifications.
     *
     * @param queryDescriptor            the query descriptor
     * @param leafNodeModifier           the modifier for any accessed leaf nodes
     * @param leafNodeModificationStats  the leaf node modification statistics
     * @return cursor over query results
     */
    public Cursor<E> rrQuery(final Descriptor queryDescriptor,
                             final Cursor<UpdateTree.Entry<E>> externalResults,
                             final IRRTreeDiskNodeOnQueryModifier<E> leafNodeModifier,
                             final OperationTypeStat leafNodeModificationStats) {
        final Cursor<?> initialResults = initialQuery(queryDescriptor, leafNodeModifier);
        return rrQueryProcessResults(initialResults, externalResults, leafNodeModifier,
                leafNodeModificationStats);
    }

    /**
     * Performs an initial query on the tree.  Returns all leaf node items that intersect with a given query rectangle.
     * Depending on the leaf node data type, further processing may be necessary on these results.  Optionally performs
     * leaf node modifications.
     *
     * @param queryDescriptor        the query descriptor
     * @param leafNodeModifier       a node modifier
     * @param <T> data type of the leaf node items
     * @return cursor over initial query results.
     */
    private <T> Cursor<T> initialQuery(final Descriptor queryDescriptor,
                                       final IRRTreeDiskNodeOnQueryModifier<E> leafNodeModifier) {
        // TODO: prime candidate for refactoring
        final Iterator[] iterators = new Iterator[this.height()+1];

        Arrays.fill(iterators, EmptyCursor.DEFAULT_INSTANCE);
        if (this.height()>0 && queryDescriptor.overlaps(this.rootDescriptor())) {
            //noinspection unchecked
            iterators[this.height()] = new SingleObjectCursor<>(((IRRTreeIndexEntry<E>)this.rootEntry()));
        }

        return new AbstractCursor<T>() {
            int queryAllLevel = -1;
            final Stack<MapEntry<IRRTreeIndexEntry<E>, IRRTreeDiskNode<E>>> path = new Stack<>();

            @SuppressWarnings({"AssignmentToForLoopParameter"})
            public boolean hasNextObject() {
                for (int parentLevel = 0;;)
                    if (iterators[parentLevel].hasNext())
                        if (parentLevel==0)
                            return true;
                        else {
                            //noinspection unchecked
                            final IRRTreeIndexEntry<E> indexEntry
                                    = (IRRTreeIndexEntry<E>) iterators[parentLevel].next();

                            final IRRTreeDiskNode<E> node = indexEntry.get();

                            if (node.level() == 0) {
                                final boolean nodeModified = leafNodeModifier.modify (node, false, 0.0,
                                        new RRTreeLeafPiggybackingInfo());
                                if (nodeModified) {
                                    indexEntry.update(node);
                                }
                            }

                            Iterator<?> queryIterator;

                            if (parentLevel<=queryAllLevel || queryDescriptor.contains(indexEntry.descriptor())) {
                                queryIterator = node.entries();
                                if (parentLevel>queryAllLevel && !iterators[node.level()].hasNext())
                                    queryAllLevel = node.level();
                            }
                            else
                                queryIterator = node.query(queryDescriptor);

                            //noinspection unchecked
                            iterators[parentLevel = node.level()] =
                                       iterators[parentLevel].hasNext()
                                            ? new Sequentializer<>((Iterator<Object>)queryIterator,
                                               iterators[parentLevel])
                                            : queryIterator;
                            path.push(new MapEntry<>(indexEntry, node));
                        }
                    else
                        if (parentLevel==height())
                            return false;
                        else {
                            if (parentLevel==queryAllLevel)
                                queryAllLevel = 0;
                            if (level(path)==parentLevel)
                                path.pop();
                            iterators[parentLevel++] = EmptyCursor.DEFAULT_INSTANCE;
                        }
            }

            public T nextObject() {
                //noinspection unchecked
                return (T)iterators[0].next();
            }
        };
    }

    /**
     * Processes initial query results as necessary to get the final query results.
     *
     * @param initialResult              cursor over initial query results
     * @param externalResults            cursor over external query results (i.e. the
     *                                   buffer)
     * @param leafNodeModifier           modifier for any leaf nodes accessed
     * @param leafNodeModificationStats  statistics for any leaf node modifications done
     * @return cursor over final query results
     */
    abstract protected <T> Cursor<E> rrQueryProcessResults(
            final Cursor<T> initialResult,
            final Cursor<UpdateTree.Entry<E>> externalResults,
            final IRRTreeDiskNodeOnQueryModifier<E> leafNodeModifier,
            final OperationTypeStat leafNodeModificationStats);

    /* Tree visiting */

    /**
     * Visits the tree nodes, both index and leaf, using a visitor object.
     *
     * @param indexNodeContainer  an intermediate buffering container for the index nodes
     * @param visitor             the visitor for node visiting
     */
    public void visitTreeNodes(final Container indexNodeContainer, final IRRDiskTreeVisitor<E> visitor)
            throws IOException {
        final AbstractRRDiskTree<E> thisTree = this;
        visitTree (indexNodeContainer, new Predicate<Object>() {
            public boolean invoke(Object argument0, Object argument1) {
                //noinspection unchecked
                final IRRTreeIndexEntry<E> entryToProcess = (IRRTreeIndexEntry<E>)argument0;
                //noinspection unchecked
                final IRRTreeDiskNode<E> indexNode = (IRRTreeDiskNode<E>)argument1;
                visitor.visitIndexNode(thisTree, entryToProcess, indexNode);
                return true;
            }
        }, new Function<Object, Void>() {
            public Void invoke(Object arg) {
                //noinspection unchecked
                final IRRTreeIndexEntry<E> entryToProcess = (IRRTreeIndexEntry<E>)arg;
                visitor.visitLeafNode(thisTree, entryToProcess);
                return null;
            }
        });
        visitor.finishVisiting();
    }

    /**
     * Visits the tree nodes, both index and leaf, using functions with a possible cut-off at a subtree level.
     *
     * @param indexNodeContainer  an intermediate buffering container for the index nodes
     * @param indexVisitFunction  a function to use for index node visiting.  If returns <code>false</code>, the
     * subtree pointed to by this function is excluded from further visiting.
     * @param leafVisitFunction   a function to use for leaf node visiting.
     */
    private void visitTree(Container indexNodeContainer, final Predicate<Object> indexVisitFunction,
                           final Function<Object, Void> leafVisitFunction) {
        if (rootEntry == null)
            return;

        if (indexNodeContainer == null)
            indexNodeContainer = container();

        //noinspection unchecked
        final Function<Void, Container> getIndexNodeCachingContainer = new Constant(indexNodeContainer);
        final Function oldGetContainer = getContainer;
        getContainer = getIndexNodeCachingContainer;

        final Deque<Iterator<IRRTreeIndexEntry<E>>> indexNodes = new ArrayDeque<>(height());
        final Collection<IRRTreeIndexEntry<E>> rootEntryColl = new ArrayList<>(1);
        //noinspection unchecked
        rootEntryColl.add((IRRTreeIndexEntry<E>)rootEntry);
        indexNodes.addFirst(rootEntryColl.iterator());
        while (indexNodes.size() > 0) {
            final Iterator<IRRTreeIndexEntry<E>> topIterator = indexNodes.getFirst();
            if (!topIterator.hasNext())
                indexNodes.removeFirst();
            else {
                final IRRTreeIndexEntry<E> entryToProcess = topIterator.next();
                if (entryToProcess.level() > 0) {
                    final IRRTreeDiskNode<E> indexNode = entryToProcess.get();
                    if (indexVisitFunction.invoke(entryToProcess, indexNode))
                        indexNodes.addFirst(indexNode.getNonLeafNodeEntries().iterator());
                }
                else {
                    getContainer = oldGetContainer;
                    leafVisitFunction.invoke(entryToProcess);
                    getContainer = getIndexNodeCachingContainer;
                }
            }
        }
        getContainer = oldGetContainer;
    }

    /**
     * Gets all ids of all the leaf nodes that intersect a given rectangle.
     *
     * @param indexNodeContainer  an intermediate buffering container for the index nodes
     * @param descriptor          a rectangle to check intersection with
     * @return a collection of intersecting leaf node ids.
     */
    public Collection<Object> fetchIntersectingLeafNodeIDs(final Container indexNodeContainer,
                                                           final Descriptor descriptor) {
        final Collection<Object> result = new ArrayList<>();
        if (descriptor == null)
            return result;
        visitTree (indexNodeContainer, new Predicate<Object>() {
            public boolean invoke(Object argument0, Object argument1) {
                //noinspection unchecked
                final IRRTreeIndexEntry<E> entryToProcess = (IRRTreeIndexEntry<E>)argument0;
                return entryToProcess.descriptor().overlaps(descriptor);
            }
        }, new Function<Object, Void>() {
            public Void invoke(Object arg) {
                //noinspection unchecked
                final IRRTreeIndexEntry<E> entryToProcess = (IRRTreeIndexEntry<E>)arg;
                if (entryToProcess.descriptor().overlaps(descriptor))
                    result.add(entryToProcess.id());
                return null;
            }
        });
        return result;
    }

    /* Helpers */

    /**
     * Returns the id of a data item.
     *
     * @param data the data to return the id of
     * @return the id of the data
     */
    public Object id (E data) {
        return getId.invoke(data);
    }

    /**
     * Determines the level of the tree where the index entries should be put.  Assumes all the entries are on the same
     * level.
     *
     * @param nodeContents the list of the index entries
     * @return the level of the tree for these index entries
     */
    static public <T extends Convertable> int getHeight(final List<IRRTreeIndexEntry<T>> nodeContents) {
        assert nodeContents.size() > 0;
        return nodeContents.get(0).level();
    }

    public Container container () {
        return (Container)getContainer.invoke();
    }

    public TreeClearIOState clearWithIOCount () {
        //noinspection unchecked
        final CounterContainer counterContainer = new CounterContainer((Container)getContainer.invoke());
        //noinspection unchecked
        final Function<Void, Container> getCounterContainer = new Constant(counterContainer);
        final Function oldGetContainer = getContainer;
        getContainer = getCounterContainer;
        super.clear();
        final TreeClearIOState result = new TreeClearIOState(counterContainer.gets, counterContainer.removes);
        if (counterContainer.inserts != 0 || counterContainer.updates != 0)
            throw new IllegalStateException("Tree cleaning caused container insert or update");
        getContainer = oldGetContainer;
        return result;
    }
}
