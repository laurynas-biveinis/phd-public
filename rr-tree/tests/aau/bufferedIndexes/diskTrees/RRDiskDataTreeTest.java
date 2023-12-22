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
package aau.bufferedIndexes.diskTrees;

import aau.bufferedIndexes.OperationType;
import aau.bufferedIndexes.TestData;
import aau.bufferedIndexes.TestUtils;
import aau.bufferedIndexes.UpdateTree;
import org.junit.Before;
import org.junit.Test;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.spatial.KPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Tests (unit?) for the RRDiskDataTree
 */
public class RRDiskDataTreeTest {

    private Container container;

    private RRDiskDataTree<KPE> diskTree;

    @Before
    public void setUp() {
        container = new MapContainer();
        diskTree = new RRDiskDataTree<>();
    }

    @Test
    public void nodeGetLeafNodeEntries() {
        final RRDiskDataTree<KPE>.Node n = makeNodeWithContents(0,
                TestUtils.makeKPE(0.0, 0.0, 1.0, 1.0),
                TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));

        assertEquals (n.getEntries(), n.getLeafNodeEntries());
    }

    private RRDiskDataTree<KPE>.Node makeNodeWithContents(final int level,
                                                          final KPE... contents) {
        final RRDiskDataTree<KPE>.Node n = diskTree.createNode(level); 
        for (KPE entry : contents) {
            n.grow(entry);
        }
        return n;
    }

    @Test
    public void nodeDoesOperationFitInsertionFits() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[1], OperationType.INSERTION), true);
    }

    @Test
    public void nodeDoesOperationFitDeletionFits() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION), true);
    }

    @Test
    public void nodeDoesOperationFitInsertionDoesNotFit() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION), false);
    }

    @Test
    public void nodeDoesOperationFitDeletionDoesNotFit() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION), false);
    }

    private void testNodeDoesOperationFit(final UpdateTree.Entry<KPE> op, final boolean expectedResult) {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        RRDiskDataTree<KPE>.Node node = makeNodeWithContents(0, TestData.data[0], TestData.data[2]);

        assertEquals (expectedResult, node.doesOperationFit(node.computeDescriptor(), op, false));
    }

    @Test
    public void testNodeDeletionWillIncreaseNodeSize() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        RRDiskDataTree<KPE>.Node node = makeNodeWithContents(0, TestData.data[0], TestData.data[2]);
        assertFalse (node.operationWillIncreaseNodeSize(new UpdateTree.Entry<>(TestData.data[0],
                OperationType.DELETION)));
    }

    @Test
    public void testNodeInsertionWillIncreaseNodeSize() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        RRDiskDataTree<KPE>.Node node = makeNodeWithContents(0, TestData.data[0], TestData.data[2]);
        assertTrue (node.operationWillIncreaseNodeSize(new UpdateTree.Entry<>(TestData.data[1],
                OperationType.INSERTION)));
    }

    @Test
    public void nodeExecuteConstrainedSetOfOpsFullSet() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestData.data[0], TestData.data[1], TestData.data[2]);

        final Collection<UpdateTree.Entry<KPE>> candidateSet = new ArrayList<>();
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[2], OperationType.DELETION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[4], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> executedSet = n.executeConstrainedSubsetOfOps(candidateSet, 10, 10);
        assertArrayEquals (candidateSet.toArray(), executedSet.toArray());
        // TODO: here and below: the tests do not check the resulting node contents!
    }

    @Test
    public void nodeExecuteConstrainedSetOfOpsSubset() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0,
                TestData.data[0], TestData.data[1], TestData.data[2]);

        final Collection<UpdateTree.Entry<KPE>> candidateSet = new ArrayList<>();
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[2], OperationType.DELETION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[4], OperationType.INSERTION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[5], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> shouldBe = new ArrayList<>();
        shouldBe.add (new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION));
        shouldBe.add (new UpdateTree.Entry<>(TestData.data[4], OperationType.INSERTION));
        
        final Collection<UpdateTree.Entry<KPE>> executedSet = n.executeConstrainedSubsetOfOps(candidateSet, 1, 1);
        assertArrayEquals (shouldBe.toArray(), executedSet.toArray());
    }

    private void testNodeExecute(final KPE existingData, final KPE dataToExecute,
                                 final OperationType opToExecute, final boolean insertionDeletesOldInsertion,
                                 final boolean expectedResult, final int expectedSize) {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        final RRDiskDataTree<KPE>.Node n = makeNodeWithContents(0, existingData);

        assertEquals (expectedResult,
                n.executeOp(new UpdateTree.Entry<>(dataToExecute, opToExecute), insertionDeletesOldInsertion));
        assertEquals (expectedSize, n.number());        
    }

    @Test
    public void nodeExecuteOpDeletionFindsEntry() {
        testNodeExecute (TestData.data[0], TestData.data[0], OperationType.DELETION, false, true, 0);
    }

    @Test
    public void nodeExecuteOpDeletionDoesNotFindEntry() {
        testNodeExecute (TestData.data[0], TestData.data[1], OperationType.DELETION, false, false, 1);
    }

    @Test
    public void nodeExecuteOpInsertion() {
        testNodeExecute (TestData.data[0], TestData.data[1], OperationType.INSERTION, false, true, 2);
    }

    @Test
    public void nodeExecuteOpInsertionRemovesOldInsertion() {
        testNodeExecute (TestData.data[0], TestData.data[1], OperationType.INSERTION, true, true, 1);
    }

    @Test
    public void createNodeWithContents() {
        diskTree.initialize(null, null, 50, 100);

        final List<KPE> nodeContents = new ArrayList<>();
        nodeContents.add(TestData.data[0]);
        nodeContents.add(TestData.data[1]);
        final IRRTreeDiskNode<KPE> n = diskTree.createNode(0, nodeContents);
        assertEquals (0, n.level());
        assertEquals (2, n.getEntries().size());
        assertTrue (n.getEntries().contains(TestData.data[0]));
        assertTrue (n.getEntries().contains(TestData.data[1]));
    }

    @Test
    public void createEmptyNode() {
        diskTree.initialize(null, null, 50, 100);

        final IRRTreeDiskNode<KPE> n = diskTree.createNode(12);
        assertEquals (12, n.level());
        assertEquals (0, n.getEntries().size());
    }

    @Test
    public void descriptorOfOperation() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, null, -1, -1);

        final UpdateTree.Entry<KPE> op = new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION);
        assertEquals (diskTree.descriptor(TestData.data[0]), diskTree.descriptor(op));
    }
}
