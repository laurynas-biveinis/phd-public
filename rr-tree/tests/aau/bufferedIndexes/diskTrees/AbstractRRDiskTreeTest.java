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
import aau.bufferedIndexes.leafNodeModifiers.NullModeModifier;
import aau.workload.DataID;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.unions.Sequentializer;
import xxl.core.cursors.wrappers.IteratorCursor;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.Tree;
import xxl.core.io.Convertable;
import xxl.core.io.LRUBuffer;
import xxl.core.spatial.KPE;

import java.io.IOException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Tests for AbstractRRDiskTree class
 */
@RunWith(JMock.class)
public class AbstractRRDiskTreeTest {

    class AbstractRRDiskTreeStub extends AbstractRRDiskTree<KPE> {

        class Node extends AbstractRRDiskTree<KPE>.Node {
            public Node(int level, Collection<?> newEntries) { super(level, newEntries); }
            protected boolean doesOperationFit(Descriptor descriptor, UpdateTree.Entry<KPE> operation, boolean outsideMBRAllowed) { return false; }
            protected boolean executeOp(UpdateTree.Entry<KPE> kpeEntry, boolean insertionRemovesOldInsertion) {
                return false; }
            public boolean operationWillIncreaseNodeSize(UpdateTree.Entry<KPE> deletion) { return false; }
            public Collection<UpdateTree.Entry<KPE>> executeConstrainedSubsetOfOps(
                    Collection<UpdateTree.Entry<KPE>> candidateSet, int maxInsertions, int maxDeletions) {
                return null; }
        }

        public IRRTreeDiskNode<KPE> createNode(int level, List<?> nodeContents) {
            return new Node(level, nodeContents);
        }

        public AbstractRRDiskTree<KPE>.Node createNode(int level) {
            return new Node(level, null);
        }

        public boolean deletionsLikeInsertions() {
            return false;
        }

        public Descriptor descriptor (final UpdateTree.Entry<KPE> entry) {
            return super.descriptor(entry.getData());
        }

        public TreeClearIOState cleanGarbage() {
            return null;
        }

        protected <T> Cursor<KPE> rrQueryProcessResults(
                final Cursor<T> initialResult,
                final Cursor<UpdateTree.Entry<KPE>> externalResults,
                final IRRTreeDiskNodeOnQueryModifier<KPE> leafNodeModifier,
                final OperationTypeStat leafNodeModificationStats) {
            //noinspection unchecked
            return new Sequentializer<>((Iterator<KPE>)initialResult,
                    new Mapper<>(
                            new Function<UpdateTree.Entry<KPE>, KPE>() {
                                public KPE invoke(final UpdateTree.Entry<KPE> object) {
                                    return object.getData();        
                                }
                            }, externalResults)
                    );
        }
    }

    final private Mockery context = new JUnit4Mockery();

    private IRRTreeIndexEntry<KPE> mockIndexEntry;

    private IRRTreeIndexEntry<KPE> mockIndexEntry2;

    private AbstractRRDiskTreeStub diskTree;

    private AbstractRRDiskTreeStub.Node emptyNode;

    private Object[] entries;

    private Container container;

    @Before
    public void setUp() {
        container = new MapContainer();
        diskTree = new AbstractRRDiskTreeStub();
        emptyNode = diskTree.new Node(0, null);
        entries = new Object[100];
        System.arraycopy(TestData.data, 0, entries, 0, 100);
        //noinspection unchecked
        mockIndexEntry = context.mock(IRRTreeIndexEntry.class, "mock index entry");
        //noinspection unchecked
        mockIndexEntry2 = context.mock(IRRTreeIndexEntry.class, "mock index entry 2");
    }

    @Test
    public void nodeGrowAndAddEntriesFrom() {
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(0, new ArrayList());
        n.grow(1);
        n.grow(2);
        assertEquals (2, n.number());
        final IRRTreeDiskNode<KPE> otherNode = diskTree.new Node(0, new ArrayList());
        otherNode.grow(3);
        otherNode.grow(4);
        n.addEntriesFrom(otherNode);
        assertEquals (4, n.number());
        assertTrue (n.getEntries().contains(1));
        assertTrue (n.getEntries().contains(2));
        assertTrue (n.getEntries().contains(3));
        assertTrue (n.getEntries().contains(4));
    }

    @Test
    public void createNodeAndGetEntries() {
        final List<Integer> nodeContents = new ArrayList<>();
        nodeContents.add(5);
        nodeContents.add(10);
        final AbstractRRDiskTree.Node n = diskTree.new Node(3, nodeContents);
        assertEquals(3, n.level());
        assertEquals(2, n.number());
        assertEquals (n.number(), n.getEntries().size());
        assertTrue (n.getEntries().contains(5));
        assertTrue (n.getEntries().contains(10));
    }

    @Test
    public void createNodeEmptyContents() {
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(2, null);
        assertEquals(2, n.level());
        assertEquals(0, n.number());
    }

    @Test
    public void nodeGetNonLeafNodeEntries() {
        final List<IRRTreeIndexEntry<KPE>> nodeContents = new ArrayList<>();
        nodeContents.add(mockIndexEntry);
        nodeContents.add(mockIndexEntry2);
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(3, nodeContents);

        assertEquals (n.getEntries(), n.getNonLeafNodeEntries());
    }

    @Test
    public void nodeRemove() {
        final AbstractRRDiskTree.Node n = diskTree.new Node(0, new ArrayList());
        n.grow(1);
        n.grow(2);
        n.remove(1);
        assertEquals (1, n.number());
        assertTrue (n.getEntries().contains(2));
    }

    @Test
    public void nodeSetLevel() {
        emptyNode.setLevel(15);
        assertEquals(15, emptyNode.level());
    }

    @Test
    public void nodeSplitMinNumber() {
        diskTree.initialize(null, null, 50, 100);
        final List<KPE> data = new ArrayList<>(100);
        for (Object object : entries) {
            data.add((KPE)object);
        }
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(0, data);
        assertEquals (50, n.splitMinNumber());
    }

    @Test
    public void nodeOverflows() {
        diskTree.initialize(null, null, 5, 10);
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(0, new ArrayList<KPE>());
        for (int i = 0; i < 10; i++) {
            n.grow(entries[i]);
            assertFalse (n.overflows());
        }
            n.grow(entries[10]);
        assertTrue (n.overflows());
    }

    @Test
    public void nodeUnderflows() {
        diskTree.initialize(null, null, 5, 10);
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(0, new ArrayList<KPE>());
        for (int i = 0; i < 4; i++) {
            n.grow(entries[i]);
            assertTrue (n.underflows());
        }
        n.grow(entries[5]);
        assertFalse (n.underflows());
    }

    @Test
    public void nodeQuery() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, null, 50, 100);
        final List<IRRTreeIndexEntry<KPE>> nodeContents = new ArrayList<>();
        // TODO: mock below
        final RRDiskDataTree<KPE>.IndexEntry i1 = diskTree.createIndexEntry(3);
        i1.initialize(TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0));
        final RRDiskDataTree<KPE>.IndexEntry i2 = diskTree.createIndexEntry(3);
        i2.initialize(TestUtils.makeRectangle(0.0, 0.0, 4.0, 4.0));
        final RRDiskDataTree<KPE>.IndexEntry i3 = diskTree.createIndexEntry(3);
        i3.initialize(TestUtils.makeRectangle(10.0, 10.0, 12.0, 12.0));
        nodeContents.add(i1);
        nodeContents.add(i2);
        nodeContents.add(i3);
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(3, nodeContents);

        Iterator<IRRTreeIndexEntry<KPE>> results = n.query(TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        IRRTreeIndexEntry<KPE> result = results.next();
        assertSame (i1, result);
        result = results.next();
        assertSame (i2, result);
        assertFalse (results.hasNext());
    }

    @Test
    public void emptyNodeDescriptor() {
        emptyNode.initialize(0, new ArrayList());
        final Descriptor result = emptyNode.computeDescriptor();
        assertNull(result);
    }

    // TODO: copy paste!
    private IRRTreeDiskNode<KPE> makeNodeWithContents(final int level, final KPE... contents) {
        final IRRTreeDiskNode<KPE> n = diskTree.createNode(level);
        for (KPE entry : contents) {
            n.grow(entry);
        }
        return n;
    }

    @Test
    public void leafNodeDescriptor() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, null, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        final Descriptor result = n.computeDescriptor();
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0), result);
    }

    @Test
    public void nodeLimitNumberOfOperationsNoLimitLow() {
        diskTree.initialize(null, TestUtils.GET_DESCRIPTOR, container, 1, 10, null);
        IRRTreeDiskNode<KPE> node = makeNodeWithContents(0, TestData.data[0]);
        final IRRTreeLeafPiggybackingInfo i = new RRTreeLeafPiggybackingInfo();
        addPotentialOps(i, OperationType.INSERTION, OperationType.DELETION, OperationType.INSERTION,
                           OperationType.DELETION, OperationType.INSERTION, OperationType.DELETION,
                           OperationType.INSERTION, OperationType.DELETION);
        node.limitNumberOfOperations(i);

        assertEquals(0, i.getUnpiggybackableSizeDecreasingOps());
        assertEquals(0, i.getUnpiggybackableSizeIncreasingOps());
    }

    @Test
    public void nodeLimitNumberOfOperationsNoLimitHigh() {
        diskTree.initialize(null, TestUtils.GET_DESCRIPTOR, container, 1, 5, null);
        IRRTreeDiskNode<KPE> node = makeNodeWithContents(0, TestData.data[0]);
        final IRRTreeLeafPiggybackingInfo i = new RRTreeLeafPiggybackingInfo();
        addPotentialOps(i, OperationType.INSERTION, OperationType.INSERTION, OperationType.INSERTION,
                           OperationType.INSERTION);
        node.limitNumberOfOperations(i);

        assertEquals(0, i.getUnpiggybackableSizeDecreasingOps());
        assertEquals(0, i.getUnpiggybackableSizeIncreasingOps());
    }

    @Test
    public void nodeLimitNumberOfOperationsUnderflow() {
        diskTree.initialize(null, TestUtils.GET_DESCRIPTOR, container, 1, 10, null);
        IRRTreeDiskNode<KPE> node = makeNodeWithContents(0, TestData.data[0]);
        final IRRTreeLeafPiggybackingInfo i = new RRTreeLeafPiggybackingInfo();
        addPotentialOps(i, OperationType.INSERTION, OperationType.DELETION, OperationType.DELETION,
                OperationType.DELETION);
        node.limitNumberOfOperations(i);

        assertEquals(2, i.getUnpiggybackableSizeDecreasingOps());
        assertEquals(0, i.getUnpiggybackableSizeIncreasingOps());
    }

    @Test
    public void nodeLimitNumberOfOperationsOverflow() {
        diskTree.initialize(null, TestUtils.GET_DESCRIPTOR, container, 1, 3, null);
        IRRTreeDiskNode<KPE> node = makeNodeWithContents(0, TestData.data[0]);
        final IRRTreeLeafPiggybackingInfo i = new RRTreeLeafPiggybackingInfo();
        addPotentialOps(i, OperationType.DELETION, OperationType.INSERTION, OperationType.INSERTION,
                OperationType.INSERTION, OperationType.INSERTION, OperationType.INSERTION, OperationType.INSERTION);
        node.limitNumberOfOperations(i);

        assertEquals(0, i.getUnpiggybackableSizeDecreasingOps());
        assertEquals(3, i.getUnpiggybackableSizeIncreasingOps());
    }

    private void addPotentialOps(final IRRTreeLeafPiggybackingInfo info, OperationType... ops) {
        for (final OperationType op : ops) {
            if (op.isDeletion())
                info.addPotentialSizeDecreasingOp();
            else
                info.addPotentialSizeIncreasingOp();
        }
    }

    @Test
    public void indexNodeDescriptor() {
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(1, new ArrayList<IRRTreeIndexEntry<KPE>>());

        final RRDiskDataTree<KPE>.IndexEntry i1 = diskTree.createIndexEntry(3);
        i1.initialize(TestUtils.makeRectangle(0.0, 0.0, 1.0, 1.0));
        n.grow(i1);
        final RRDiskDataTree<KPE>.IndexEntry i2 = diskTree.createIndexEntry(3);
        i2.initialize(TestUtils.makeRectangle(1.0, 0.0, 2.0, 1.0));
        n.grow(i2);
        final RRDiskDataTree<KPE>.IndexEntry i3 = diskTree.createIndexEntry(3);
        i3.initialize(TestUtils.makeRectangle(0.0, 5.0, 12.0, 11.0));
        n.grow(i3);

        final Descriptor result = n.computeDescriptor();
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 12.0, 11.0), result);
    }

    class FittingOperationTestTree extends AbstractRRDiskTreeStub {
        class Node extends AbstractRRDiskTreeStub.Node {
            final private UpdateTree.Entry<KPE> unfitOp;

            public Node (final UpdateTree.Entry<KPE> unfitOp) {
                super (0, null);
                this.unfitOp = unfitOp;
            }

            protected boolean doesOperationFit(Descriptor descriptor, UpdateTree.Entry<KPE> operation, boolean outsideMBRAllowed) {
                return operation != unfitOp;
            }
        }
    }

    @Test
    public void nodeSelectFittingOperations() {
        FittingOperationTestTree fitTestTree = new FittingOperationTestTree();
        fitTestTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);

        final UpdateTree.Entry<KPE> unfitOp = new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION);
        final IRRTreeDiskNode<KPE> node = fitTestTree.new Node(unfitOp);
        node.grow (TestData.data[0]);

        final Collection<UpdateTree.Entry<KPE>> candidateOps = new ArrayList<>();
        candidateOps.add(unfitOp);
        candidateOps.add(new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));
        Cursor<UpdateTree.Entry<KPE>> cursor = new IteratorCursor<>(candidateOps.iterator());

        final Collection<UpdateTree.Entry<KPE>> resultOps = new ArrayList<>();
        resultOps.add(new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> fittingOps = node.selectFittingOperations(cursor, false);
        assertArrayEquals (resultOps.toArray(), fittingOps.toArray());
    }

    class ExecuteOpTestTree extends AbstractRRDiskTreeStub {
        class Node extends AbstractRRDiskTreeStub.Node {
            final private UpdateTree.Entry<KPE> unexecutableOp;

            public Node (final UpdateTree.Entry<KPE> unexecutableOp) {
                super (0, null);
                this.unexecutableOp = unexecutableOp;
            }

            protected boolean executeOp(UpdateTree.Entry<KPE> entry, boolean insertionRemovesOldInsertion) {
                return entry != unexecutableOp;
            }
        }
    }

    @Test
    public void nodeExecuteOps() {
        ExecuteOpTestTree executeOpTestTree = new ExecuteOpTestTree();
        executeOpTestTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);

        final UpdateTree.Entry<KPE> unexecutableOp
                = new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION);
        final IRRTreeDiskNode<KPE> node = executeOpTestTree.new Node(unexecutableOp);
        node.grow (TestData.data[0]);

        final Collection<UpdateTree.Entry<KPE>> candidateOps = new ArrayList<>();
        candidateOps.add(unexecutableOp);
        candidateOps.add(new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> shouldBe = new ArrayList<>();
        shouldBe.add (new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> executedSet = node.executeOps(candidateOps, false);
        assertArrayEquals (shouldBe.toArray(), executedSet.toArray());
    }

    @Test
    public void indexEntryConstructor() {
        final IRRTreeIndexEntry<KPE> e = diskTree.new IndexEntry(2);
        assertEquals (1, e.level());
    }

    @Test
    public void indexEntryGet() {
        diskTree.initialize(null, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(54, null);
        final RRDiskDataTree<KPE>.IndexEntry e = diskTree.new IndexEntry(55);
        final Object nodeID = container.insert(n);
        e.initialize(container, nodeID);

        final AbstractRRDiskTree.Node retrievedNode = e.get();
        assertEquals (n, retrievedNode);
    }

    @Test
    public void indexEntryGetUnfixed() {
        final Container cache = new BufferedContainer(container, new LRUBuffer(1));

        diskTree.initialize(null, cache, 50, 100);
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(54, null);
        final RRDiskDataTree<KPE>.IndexEntry e = diskTree.new IndexEntry(55);
        final Object nodeID = cache.insert(n);
        e.initialize(cache, nodeID);

        final AbstractRRDiskTree.Node retrievedNode = e.get(true);
        assertEquals (n, retrievedNode);
    }

    @Test(expected=IllegalStateException.class)
    public void indexEntryGetFixed() {
        final Container cache = new BufferedContainer(container, new LRUBuffer(1));

        diskTree.initialize(null, cache, 50, 100);
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(54, null);
        final RRDiskDataTree<KPE>.IndexEntry e = diskTree.new IndexEntry(55);
        final Object nodeID = cache.insert(n);
        e.initialize(cache, nodeID);

        AbstractRRDiskTree.Node retrievedNode = e.get(false);
        assertEquals (n, retrievedNode);

        final IRRTreeDiskNode<KPE> n2 = diskTree.new Node(24, null);
        final RRDiskDataTree<KPE>.IndexEntry e2 = diskTree.new IndexEntry(55);
        final Object node2ID = cache.insert(n2);
        e.initialize(cache, node2ID);

        retrievedNode = e2.get(false);
        // Unreachable
        assertSame (retrievedNode, n2);
    }

    private RRDiskDataTree<KPE>.IndexEntry makeAndStoreNode(final Container container, final int level) {
        final IRRTreeDiskNode<KPE> n = diskTree.new Node(level, null);
        final RRDiskDataTree<KPE>.IndexEntry e = diskTree.new IndexEntry(level + 1);
        final Object nodeID = container.insert(n);
        e.initialize(container, nodeID);
        return e;
    }
    
    @Test
    public void indexEntryUpdate() {
        diskTree.initialize(null, container, 50, 100);
        final RRDiskDataTree<KPE>.IndexEntry e = makeAndStoreNode(container, 54);

        e.update((IRRTreeDiskNode<KPE>)emptyNode); // TODO: remove cast

        final IRRTreeDiskNode<KPE> retrievedNode = e.get();
        assertEquals (emptyNode, retrievedNode);

    }

    @Test
    public void indexEntryUpdateUnfixed() {
        diskTree.initialize(null, container, 50, 100);
        final RRDiskDataTree<KPE>.IndexEntry e = makeAndStoreNode(container, 54);

        e.update((IRRTreeDiskNode<KPE>)emptyNode, true); // TODO: fix ambiguous call

        final IRRTreeDiskNode<KPE> retrievedNode = e.get();
        assertEquals (emptyNode, retrievedNode);
    }

    @Test
    public void indexEntryUpdateFixed() {
        final Container cache = new BufferedContainer(container, new LRUBuffer(1));

        diskTree.initialize(null, cache, 50, 100);

        final RRDiskDataTree<KPE>.IndexEntry e = makeAndStoreNode(cache, 54);

        e.update((IRRTreeDiskNode<KPE>)emptyNode, true); // TODO: fix ambiguous call

        final AbstractRRDiskTree.Node retrievedNode = e.get();
        assertEquals (emptyNode, retrievedNode);
    }

    @Test
    public void initializeMinMaxNodeCapacity() {
        diskTree.initialize(null, null, null, 50, 100, null);

        assertEquals (50, diskTree.getMinNodeCapacity());
        assertEquals (100, diskTree.getMaxNodeCapacity());
    }

    @Test
    public void createIndexEntry() {
        final IRRTreeIndexEntry<KPE> e = diskTree.createIndexEntry(10);
        assertEquals (9, e.level());
    }

    @Test
    public void computeIndexEntry() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, null, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        final IRRTreeIndexEntry<KPE> entry = diskTree.computeIndexEntry(n);

        assertEquals (0, entry.level());
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0), entry.descriptor());
    }

    @Test
    public void takeOverNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));

        final IRRTreeIndexEntry<KPE> entry = diskTree.computeIndexEntry(n);
        final Object nodeID = container.insert(n);
        entry.initialize(nodeID);

        n.grow(TestUtils.makeKPE(2.0, 2.0, 3.0, 3.0));
        final IRRTreeIndexEntry<KPE> newEntry = diskTree.takeOverNode(n, entry, false);

        assertEquals (0, newEntry.level());
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 3.0, 3.0), newEntry.descriptor());

        final IRRTreeDiskNode<KPE> retrievedNode = newEntry.get();
        assertEquals (n.level(), retrievedNode.level());
    }

    @Test
    public void takeOverEmptyNode() {
        final IRRTreeDiskNode<KPE> n = diskTree.createNode(0);
        final IRRTreeIndexEntry<KPE> entry = diskTree.takeOverNode(n, null, false);
        assertNull (entry);
    }

    @Test
    public void getHeight() {
        final RRDiskDataTree<KPE>.IndexEntry entry = diskTree.createIndexEntry(3);
        final List<IRRTreeIndexEntry<KPE>> il = new ArrayList<>();
        il.add(entry);
        assertEquals (2, RRDiskDataTree.getHeight(il));
    }

    @Test
    public void storeNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));

        IRRTreeIndexEntry<KPE> entry = diskTree.storeNode(n, true);
        assertEquals (0, entry.level());
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0), entry.descriptor());

        final IRRTreeDiskNode<KPE> retrievedNode = entry.get();
        assertEquals (n.level(), retrievedNode.level());
    }

    @Test
    public void storeEmptyNode() {
        final IRRTreeDiskNode<KPE> n = diskTree.createNode(0);
        final IRRTreeIndexEntry<KPE> entry = diskTree.storeNode(n, false);
        assertNull (entry);
    }

    @Test
    public void setEmptyRootNode() {
        diskTree.initialize(null, null, 50, 100);
        //noinspection unchecked
        final IRRTreeDiskNode<KPE> node = context.mock(IRRTreeDiskNode.class);
        context.checking(new Expectations(){{
            allowing(node).number(); will(returnValue(0));
        }});

        diskTree.setNewRootNode(node);

        assertNull (diskTree.rootEntry());
        assertNull (diskTree.rootDescriptor());
    }

    @Test
    public void setNewRootNodeNoPreviousRootNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));

        diskTree.setNewRootNode(n);

        final Tree.IndexEntry rootEntry = diskTree.rootEntry();
        final Tree.Node rootNode = rootEntry.get();

        assertEquals (n.level(), rootNode.level());
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0), diskTree.rootDescriptor());
    }

    @Test
    public void overridePreviousRootNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0));

        diskTree.setNewRootNode(n);

        final IRRTreeDiskNode<KPE> n2 = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));

        diskTree.setNewRootNode(n2);
        final Tree.IndexEntry rootEntry = diskTree.rootEntry();
        final Tree.Node rootNode = rootEntry.get();

        assertEquals (n2.number(), rootNode.number());
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0), diskTree.rootDescriptor());

    }

    @Test
    public void getRootNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));

        diskTree.setNewRootNode(n);

        final Tree.IndexEntry rootEntry = diskTree.rootEntry();
        final Tree.Node rootNode = rootEntry.get();

        final IRRTreeDiskNode<KPE> rootNode2 = diskTree.getRootNode();

        // TODO: here and elsewhere: compare nodes exactly
        assertEquals (rootNode.level(), rootNode2.level());
        assertEquals (rootNode.number(), rootNode2.number());
    }

    @Test
    public void getRootNodeNonexisting() {
        final IRRTreeDiskNode<KPE> root = diskTree.getRootNode();
        assertEquals (0, root.level());
        assertEquals (0, root.number());
    }

    @Test
    public void storeNodesNoOriginalNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        final IRRTreeDiskNode<KPE> n2 = makeNodeWithContents(0,
                TestUtils.makeKPE(2.0, 2.0, 3.0, 3.0),
                TestUtils.makeKPE(3.0, 4.0, 4.0, 4.0));
        final Collection<IRRTreeDiskNode<KPE>> nodes = new ArrayList<>();
        nodes.add(n);
        nodes.add(n2);

        List<IRRTreeIndexEntry<KPE>> result = diskTree.storeNodes(nodes, null, null);

        for (IRRTreeIndexEntry<KPE> entry : result) {
            assertEquals (0, entry.level());
            if (entry.descriptor().equals(TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0)))
                assertTrue(true);
            else
                assertEquals (TestUtils.makeRectangle(2.0, 2.0, 4.0, 4.0), entry.descriptor());
        }
    }

    @Test
    public void storeNodesWithOriginalNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        final IRRTreeDiskNode<KPE> n2 = makeNodeWithContents(0,
                TestUtils.makeKPE(2.0, 2.0, 3.0, 3.0),
                TestUtils.makeKPE(3.0, 4.0, 4.0, 4.0));
        final IRRTreeIndexEntry<KPE> oldEntry = diskTree.storeNode(n2, true);
        final Collection<IRRTreeDiskNode<KPE>> nodes = new ArrayList<>();
        nodes.add(n);
        nodes.add(n2);

        List<IRRTreeIndexEntry<KPE>> result = diskTree.storeNodes(nodes, oldEntry, n2);

        for (IRRTreeIndexEntry<KPE> entry : result) {
            assertEquals (0, entry.level());
            if (entry.descriptor().equals(TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0)))
                assertTrue(true);
            else
                assertEquals (TestUtils.makeRectangle(2.0, 2.0, 4.0, 4.0), entry.descriptor());
        }
    }

    @Test
    public void growTreeSingleSibling() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0),
                TestUtils.makeKPE(10.0, 10.0, 12.0, 12.0));

        diskTree.setNewRootNode(n);

        final IRRTreeDiskNode<KPE> n2 = makeNodeWithContents(0,
                TestUtils.makeKPE(2.0, 2.0, 3.0, 3.0),
                TestUtils.makeKPE(3.0, 3.0, 4.0, 4.0));

        List<IRRTreeDiskNode<KPE>> siblings = new ArrayList<>();
        siblings.add(n2);

        // TODO: here and everywhere: mock the splitter
        diskTree.growTree(n, siblings, new RecursiveTwoWaySplitter());

        final Tree.IndexEntry rootEntry = diskTree.rootEntry();
        final Tree.Node rootNode = rootEntry.get();

        assertEquals (n2.number(), rootNode.number());
        assertEquals (TestUtils.makeRectangle(2.0, 2.0, 4.0, 4.0), diskTree.rootDescriptor());
    }

    @Test
    public void growTreeSingleSiblingOldNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final KPE toRemove = TestUtils.makeKPE(10.0, 10.0, 12.0, 12.0);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0),
                toRemove);

        diskTree.setNewRootNode(n);

        n.remove(toRemove);

        List<IRRTreeDiskNode<KPE>> siblings = new ArrayList<>();
        siblings.add(n);

        diskTree.growTree(n, siblings, new RecursiveTwoWaySplitter());

        final Tree.IndexEntry rootEntry = diskTree.rootEntry();
        final Tree.Node rootNode = rootEntry.get();

        assertEquals (n.number(), rootNode.number());
        assertEquals (TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0), diskTree.rootDescriptor());

    }

    @Test
    public void growTreeTwoSiblings() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 50, 100);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0),
                TestUtils.makeKPE(10.0, 10.0, 12.0, 12.0));

        diskTree.setNewRootNode(n);

        final IRRTreeDiskNode<KPE> n2 = makeNodeWithContents(0,
                TestUtils.makeKPE(2.0, 2.0, 3.0, 3.0),
                TestUtils.makeKPE(3.0, 3.0, 4.0, 4.0));

        final IRRTreeDiskNode<KPE> n3 = makeNodeWithContents(0,
                TestUtils.makeKPE(4.0, 4.0, 5.0, 5.0),
                TestUtils.makeKPE(5.0, 5.0, 6.0, 6.0));

        List<IRRTreeDiskNode<KPE>> siblings = new ArrayList<>();
        siblings.add(n2);
        siblings.add(n3);

        diskTree.growTree(n, siblings, new RecursiveTwoWaySplitter());

        final Tree.IndexEntry rootEntry = diskTree.rootEntry();
        final Tree.Node rootNode = rootEntry.get();

        assertEquals (2, rootNode.number());
        assertEquals (1, rootNode.level());
        assertEquals (TestUtils.makeRectangle(2.0, 2.0, 6.0, 6.0), diskTree.rootDescriptor());
    }

    class TestVisitor<E extends Convertable> implements IRRDiskTreeVisitor<E> {
        int visitedIndexNodes = 0;
        int visitedLeafNodes = 0;
        boolean finishVisitingCalled = false;

        public void visitIndexNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> indexNodeEntry,
                                   final IRRTreeDiskNode<E> indexNode) {
            visitedIndexNodes++;
        }

        public void visitLeafNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> leafNodeEntry) {
            visitedLeafNodes++;
        }

        public void finishVisiting() {
            finishVisitingCalled = true;
        }
    }

    // TODO: copy paste!
    private void makeThreeLevelTree() {
        for (int i = 0; i < 401; i++)
            diskTree.insert(TestData.data[i]);
        assertEquals (3, diskTree.height());
    }

    @Test
    public void visitTree() throws IOException {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 10, 20);

        makeThreeLevelTree();

        TestVisitor<KPE> visitor = new TestVisitor<>();
        diskTree.visitTreeNodes(null, visitor);
        assertEquals (4, visitor.visitedIndexNodes);
        assertEquals (40, visitor.visitedLeafNodes);
        assertTrue (visitor.finishVisitingCalled);
    }

    @Test
    public void visitEmptyTree() throws IOException {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 10, 20);
        TestVisitor<KPE> visitor = new TestVisitor<>();
        diskTree.visitTreeNodes(null, visitor);
        Assert.assertEquals (0, visitor.visitedIndexNodes);
        Assert.assertEquals (0, visitor.visitedLeafNodes);        
    }

    @Test
    public void fetchIntersectingNodeIDs() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 2, 3);

        // Completely non-overlapping node
        final IRRTreeDiskNode<KPE> n1 = makeNodeWithContents(0,
                TestUtils.makeKPE(2.0, 2.0, 3.0, 3.0),
                TestUtils.makeKPE(0.0, 4.0, 1.0, 5.0));
        // Partially overlapping node
        final IRRTreeDiskNode<KPE> n2 = makeNodeWithContents(0,
                TestUtils.makeKPE(4.0, 0.0, 5.0, 1.0),
                TestUtils.makeKPE(6.0, 2.0, 8.0, 3.0));
        // Fully enclosed node
        final IRRTreeDiskNode<KPE> n3 = makeNodeWithContents(0,
                TestUtils.makeKPE(13.0, 3.0, 14.0, 4.0),
                TestUtils.makeKPE(15.0, 1.0, 16.0, 2.0));

        final Collection<IRRTreeDiskNode<KPE>> nodes = new ArrayList<>();
        nodes.add(n1);
        nodes.add(n2);
        nodes.add(n3);

        List<IRRTreeIndexEntry<KPE>> indexEntries1 = diskTree.storeNodes(nodes, null, null);
        @SuppressWarnings({"TypeMayBeWeakened"}) 
        Set<Object> expectedNodeIDs = new HashSet<>();
        for (IRRTreeIndexEntry<KPE> indexEntry : indexEntries1) {
            if (!indexEntry.descriptor().equals(TestUtils.makeRectangle(0.0, 2.0, 3.0, 5.0)))
                expectedNodeIDs.add(indexEntry.id());
        }

        final IRRTreeDiskNode<KPE> in1 = diskTree.createNode(1, indexEntries1);

        // The same shifted far away for the second index node
        final IRRTreeDiskNode<KPE> n4 = makeNodeWithContents(0,
                TestUtils.makeKPE(102.0, 102.0, 103.0, 103.0),
                TestUtils.makeKPE(100.0, 104.0, 101.0, 105.0));
        // Partially overlapping node
        final IRRTreeDiskNode<KPE> n5 = makeNodeWithContents(0,
                TestUtils.makeKPE(104.0, 100.0, 105.0, 101.0),
                TestUtils.makeKPE(106.0, 102.0, 108.0, 103.0));
        // Fully enclosed node
        final IRRTreeDiskNode<KPE> n6 = makeNodeWithContents(0,
                TestUtils.makeKPE(113.0, 103.0, 114.0, 104.0),
                TestUtils.makeKPE(115.0, 101.0, 116.0, 102.0));

        final Collection<IRRTreeDiskNode<KPE>> nodes2 = new ArrayList<>();
        nodes2.add(n4);
        nodes2.add(n5);
        nodes2.add(n6);

        List<IRRTreeIndexEntry<KPE>> indexEntries2 = diskTree.storeNodes(nodes2, null, null);
        final IRRTreeDiskNode<KPE> in2 = diskTree.createNode(1, indexEntries2);

        final Collection<IRRTreeDiskNode<KPE>> nodes3 = new ArrayList<>();
        nodes3.add(in1);
        nodes3.add(in2);

        List<IRRTreeIndexEntry<KPE>> rootNodeIndexEntries = diskTree.storeNodes(nodes3, null, null);
        final IRRTreeDiskNode<KPE> rootNode = diskTree.createNode(2, rootNodeIndexEntries);
        diskTree.setNewRootNode(rootNode);

        final Set<Object> intersectingNodeIDs = new HashSet<>(
                diskTree.fetchIntersectingLeafNodeIDs(null, TestUtils.makeRectangle(7.0, 0.0, 17.0, 5.0)));
        assertTrue (expectedNodeIDs.equals(intersectingNodeIDs));
    }

    @Test
    public void initialQueryNoModificationNoFilter() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 10, 20);

        makeThreeLevelTree();

        Descriptor queryDescriptor = (Descriptor)diskTree.descriptor(TestData.data[0]).clone();
        for (int i = 1; i < 401; i++)
            queryDescriptor.union(diskTree.descriptor(TestData.data[i]));

        final Cursor<KPE> results = diskTree.rrQuery(queryDescriptor,
                new EmptyCursor<UpdateTree.Entry<KPE>>(), new NullModeModifier<KPE>(),
                null);

        final Collection<KPE> resultSet = new HashSet<>();
        while (results.hasNext()) {
            resultSet.add(results.next());
        }

        assertEquals (401, resultSet.size());
    }

    @Test
    public void initialQuerySelectiveQuery() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 10, 20);

        makeThreeLevelTree();

        Descriptor queryDescriptor = (Descriptor)diskTree.descriptor(TestData.data[0]).clone();

        final Cursor<KPE> results = diskTree.rrQuery(queryDescriptor,
                new EmptyCursor<UpdateTree.Entry<KPE>>(), new NullModeModifier<KPE>(), null);

        assertTrue (results.hasNext());
        assertEquals (TestData.data[0], results.next());
        assertFalse (results.hasNext());
    }

    class TestModifier implements IRRTreeDiskNodeOnQueryModifier<KPE> {

        public boolean modify(IRRTreeDiskNode<KPE> irrTreeDiskNode, boolean allowReorganization,
                              double updatePiggybackingEpsilon, IRRTreeLeafPiggybackingInfo piggybackingInfo) {
            irrTreeDiskNode.remove(TestData.data[0]);
            return true;
        }

        public void finalizeModifications(OperationTypeStat stats) {  }
    }

    @Test
    public void initialQueryUpdateNode() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 10, 20);
        diskTree.insert(TestData.data[0]);
        diskTree.insert(TestData.data[1]);
        Descriptor queryDescriptor = (Descriptor)diskTree.descriptor(TestData.data[1]).clone();

        Cursor<KPE> results = diskTree.rrQuery(queryDescriptor,
                new EmptyCursor<UpdateTree.Entry<KPE>>(), new TestModifier(), null);
        results.next();
        results.close();

        queryDescriptor = (Descriptor)diskTree.descriptor(TestData.data[0]).clone();
        results = diskTree.rrQuery(queryDescriptor,
                new EmptyCursor<UpdateTree.Entry<KPE>>(), new NullModeModifier<KPE>(), null);
        assertFalse (results.hasNext());
    }

    // TODO: make these RRTree unit tests
/*
    private Predicate<KPE> filterData1 = new Predicate<KPE>() {
        public boolean invoke(final KPE object) {
            return (!object.equals(TestData.data[1]));
        }
    };

    @Test
    public void initialQueryFilterResults() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 10, 20);
        diskTree.insert(TestData.data[0]);
        diskTree.insert(TestData.data[1]);

        Descriptor queryDescriptor = (Descriptor)diskTree.descriptor(TestData.data[0]).clone();
        queryDescriptor.union(diskTree.descriptor(TestData.data[1]));

        Cursor<KPE> results = diskTree.rrQuery(queryDescriptor, new NullModeModifier<KPE>(), filterData1);
        assertTrue (results.hasNext());
        assertEquals (TestData.data[0], results.next());
        assertFalse (results.hasNext());
    }
  */
    @Test
    public void id() {
        diskTree.initialize(TestUtils.GET_ID, null, null, -1, -1, null);
        final KPE data = TestUtils.makeKPE(16, 1.0, 1.0, 2.0, 2.0);
        assertEquals (new DataID(16), diskTree.id(data));
    }

    @Test
    public void container() {
        diskTree.initialize(null, null, container, -1, -1, null);
        assertSame (container, diskTree.container());
    }
}
