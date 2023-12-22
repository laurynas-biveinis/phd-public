/*
     Copyright (C) 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.diskTrees.visitors;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.IRRDiskUpdateTree;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskUpdateNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import xxl.core.collections.containers.Container;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.spatial.KPE;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for RRDiskUpdateTreeGarbageVacuumer
 */
public class RRDiskUpdateTreeGarbageCleanerTest {

    private final Mockery context = new JUnit4Mockery();

    private IRRDiskUpdateTree<KPE> tree;
    private IRRTreeBuffer<KPE> buffer;
    private IRRTreeIndexEntry<KPE> indexEntry;
    private IRRTreeIndexEntry<KPE> indexEntry2;
    private Container container;
    private IRRTreeDiskUpdateNode<KPE> node;
    private IRRTreeDiskUpdateNode<KPE> node2;
    private Descriptor nodeDescriptor;

    private RRDiskUpdateTreeGarbageVacuumer<KPE> vacuumer;

    private final KPE id100Pos1 = TestUtils.makeKPE(100, 0.0, 0.0, 1.0, 1.0);
    private final KPE id100Pos2 = TestUtils.makeKPE(100, 1.0, 1.0, 2.0, 2.0);

    private final UpdateTree.Entry<KPE> id100insPos1 = new UpdateTree.Entry<>(id100Pos1, OperationType.INSERTION);
    private final UpdateTree.Entry<KPE> id100delPos2 = new UpdateTree.Entry<>(id100Pos2, OperationType.DELETION);

    @Before
    public void setUp() {
        //noinspection unchecked
        tree = context.mock(IRRDiskUpdateTree.class);
        //noinspection unchecked
        buffer = context.mock(IRRTreeBuffer.class);
        //noinspection unchecked
        indexEntry = context.mock(IRRTreeIndexEntry.class, "1st index entry");
        //noinspection unchecked
        indexEntry2 = context.mock(IRRTreeIndexEntry.class, "2nd index entry");
        container = context.mock(Container.class);
        //noinspection unchecked
        node = context.mock(IRRTreeDiskUpdateNode.class, "1st node");
        //noinspection unchecked
        node2 = context.mock(IRRTreeDiskUpdateNode.class, "2nd  node");
        nodeDescriptor = context.mock(Descriptor.class);

        vacuumer = new RRDiskUpdateTreeGarbageVacuumer<>(tree, buffer, null);
    }

    @Test
    public void visitIndexNode() {
        final RRDiskUpdateTreeGarbageVacuumer<KPE> vacuumer = new RRDiskUpdateTreeGarbageVacuumer<>(null, null, null);
        vacuumer.visitIndexNode(null, null, null);
        assertEquals (0, vacuumer.getPeakNodesLoadedAtOnce());
    }

    @Test
    public void visitLeafNodeNoIntersections() {
        visitNonIntersectingLeafNode();
        vacuumer.finishVisiting();
        assertEquals (1, vacuumer.getPeakNodesLoadedAtOnce());
    }

    private void setUpNode(final Object nodeID, final Collection<Object> intersectingNodeIDs,
                           final IRRTreeIndexEntry<KPE> indexEntry, final IRRTreeDiskUpdateNode<KPE> node) {
        context.checking(new Expectations() {{
            ignoring(buffer).queryEntryOfAnyType(with(any(Descriptor.class)));
                will(returnValue(new EmptyCursor<UpdateTree.Entry<KPE>>()));
            ignoring(node).number(); will(returnValue(0)); // The return value is ignored in these tests
            ignoring(node2).number(); will(returnValue(0)); // Likewise
            oneOf (indexEntry).id(); will(returnValue(nodeID));
            atLeast(1).of(tree).container(); will(returnValue(container));
            atLeast(1).of(container).get(with(equal(nodeID))); will(returnValue(node));
            oneOf(indexEntry).descriptor(); will(returnValue(nodeDescriptor));
            atLeast(1).of(node).computeDescriptor(); will(returnValue(nodeDescriptor));
            atLeast(1).of(node2).computeDescriptor(); will(returnValue(nodeDescriptor)); // The return value is ignored
            oneOf (tree).fetchIntersectingLeafNodeIDs(with(any(Container.class)), with(equal(nodeDescriptor)));
                will(returnValue(intersectingNodeIDs));
            oneOf (indexEntry).update(with(same(node)));
            ignoring(indexEntry).level(); will(returnValue(0));
            ignoring(node).dataItemRemoved();
            ignoring(node2).dataItemRemoved();
            ignoring(node).dataItemAdded();
            ignoring(node2).dataItemAdded();
        }});
    }

    private void visitNonIntersectingLeafNode() {
        final Object nodeID = 1;
        final Collection<Object> intersectingNodeIDs = new ArrayList<>();
        intersectingNodeIDs.add(nodeID);
        setUpNode (nodeID, intersectingNodeIDs, indexEntry, node);
        vacuumer.visitLeafNode(tree, indexEntry);
    }

    @Test
    public void visitTwoIntersectingNodesUniqueData() {
        final Collection<UpdateTree.Entry<KPE>> nodeEntries = new ArrayList<>();
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION));
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[1], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNodeEntries = new ArrayList<>(nodeEntries);

        final Collection<UpdateTree.Entry<KPE>> node2Entries = new ArrayList<>();
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNode2Entries
                = new ArrayList<>(node2Entries);

        testTwoNodes(nodeEntries, node2Entries, expectedNodeEntries, expectedNode2Entries);
    }

    @Test
    public void visitTwoIntersectingNodesDeletionEatsInsertion() {
        final Collection<UpdateTree.Entry<KPE>> nodeEntries = new ArrayList<>();
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNodeEntries = new ArrayList<>(nodeEntries);
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[1], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> node2Entries = new ArrayList<>();
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNode2Entries
                = new ArrayList<>(node2Entries);
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION));

        testTwoNodes(nodeEntries, node2Entries, expectedNodeEntries, expectedNode2Entries);
    }

    @Test
    public void visitTwoIntersectingNodesInsertionEatsDeletion() {
        final Collection<UpdateTree.Entry<KPE>> nodeEntries = new ArrayList<>();
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNodeEntries = new ArrayList<>(nodeEntries);
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION));

        final Collection<UpdateTree.Entry<KPE>> node2Entries = new ArrayList<>();
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNode2Entries
                = new ArrayList<>(node2Entries);
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[1], OperationType.INSERTION));

        testTwoNodes(nodeEntries, node2Entries, expectedNodeEntries, expectedNode2Entries);
    }

    @Test
    public void visitTwoIntersectingNodesSameIdDifferentPosShouldNotAnnihilate() {
        final Collection<UpdateTree.Entry<KPE>> nodeEntries = new ArrayList<>();
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION));
        nodeEntries.add(id100insPos1);
        final Collection<UpdateTree.Entry<KPE>> expectedNodeEntries = new ArrayList<>(nodeEntries);

        final Collection<UpdateTree.Entry<KPE>> node2Entries = new ArrayList<>();
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));
        node2Entries.add(id100delPos2);
        final Collection<UpdateTree.Entry<KPE>> expectedNode2Entries
                = new ArrayList<>(node2Entries);

        testTwoNodes(nodeEntries, node2Entries, expectedNodeEntries, expectedNode2Entries);
    }

    @Test
    public void loadAlreadyLoadedNode() {
        final Collection<UpdateTree.Entry<KPE>> nodeEntries = new ArrayList<>();
        nodeEntries.add(new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNodeEntries = new ArrayList<>(nodeEntries);

        final Collection<UpdateTree.Entry<KPE>> node2Entries = new ArrayList<>();
        node2Entries.add(new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));
        final Collection<UpdateTree.Entry<KPE>> expectedNode2Entries
                = new ArrayList<>(node2Entries);

        testTwoNodes(nodeEntries, node2Entries, expectedNodeEntries, expectedNode2Entries);

        final Object nodeID = 1;
        final Object secondNodeID = 2;
        final Collection<Object> intersectingNodeIDs = new ArrayList<>();
        intersectingNodeIDs.add(nodeID);
            intersectingNodeIDs.add(secondNodeID);

        setUpNode(secondNodeID, intersectingNodeIDs, indexEntry2, node2);

        vacuumer.visitLeafNode(tree, indexEntry2);
        assertEquals (2, vacuumer.getPeakNodesLoadedAtOnce());
    }

    private void testTwoNodes(final Collection<UpdateTree.Entry<KPE>> n1Entries,
                              final Collection<UpdateTree.Entry<KPE>> n2Entries,
                              final Collection<UpdateTree.Entry<KPE>> n1ExpectedEntries,
                              final Collection<UpdateTree.Entry<KPE>> n2ExpectedEntries) {
        final Object nodeID = 1;
        final Object secondNodeID = 2;
        final Collection<Object> intersectingNodeIDs = new ArrayList<>();
        intersectingNodeIDs.add(nodeID);
        intersectingNodeIDs.add(secondNodeID);

        setUpNode(nodeID, intersectingNodeIDs, indexEntry, node);

        context.checking(new Expectations() {{
            atLeast(1).of(node).getLeafNodeEntries(); will(returnValue(n1Entries));
        }});

        context.checking(new Expectations() {{
            oneOf (container).get(with(equal(secondNodeID))); will(returnValue(node2));
            atLeast(1).of(node2).getLeafNodeEntries(); will(returnValue(n2Entries));
        }});

        context.checking(new Expectations(){{
            atLeast(1).of(tree).id(with(same(TestData.data[0]))); will(returnValue(0));
            atLeast(1).of(tree).id(with(same(TestData.data[1]))); will(returnValue(1));
            atLeast(1).of(tree).id(with(same(TestData.data[2]))); will(returnValue(2));
            atLeast(1).of(tree).id(with(same(TestData.data[3]))); will(returnValue(3));
            atLeast(1).of(tree).id(with(same(id100Pos1))); will(returnValue(100));
            atLeast(1).of(tree).id(with(same(id100Pos2))); will(returnValue(100));
        }});

        vacuumer.visitLeafNode(tree, indexEntry);

        assertArrayEquals(n1ExpectedEntries.toArray(), n1Entries.toArray());
        assertArrayEquals(n2ExpectedEntries.toArray(), n2Entries.toArray());
        assertEquals (2, vacuumer.getPeakNodesLoadedAtOnce());
    }
}

