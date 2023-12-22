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
package aau.bufferedIndexes;

import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.RRDiskDataTree;
import aau.bufferedIndexes.diskTrees.RRDiskUpdateTree;
import aau.bufferedIndexes.objectTracers.NullObjectTracer;
import aau.bufferedIndexes.operationGroupMakers.AbstractOperationGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.DeletionsAsInsertionsGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.TrivialOperationGroupMaker;
import aau.bufferedIndexes.pushDownStrategies.PushDownAllGroups;
import aau.bufferedIndexes.pushDownStrategies.PushDownGroupsStrategy;
import org.junit.Before;
import org.junit.Test;
import xxl.core.cursors.Cursor;
import xxl.core.spatial.KPE;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Integration tests for the RRTree.
 */
public class RRTreeIntegrationTest extends TreeTester {

    private RRTree<KPE> tree = null;

    private IRRDiskTree<KPE> dataDiskTree = null;
    private IRRDiskTree<KPE> updateDiskTree = null;

    private AbstractOperationGroupMaker trivialGroupMaker = null;
    private AbstractOperationGroupMaker delsAsInsGroupMaker = null;
    private PushDownGroupsStrategy<KPE> emptyWholeBuffer = null;

    @Before
    public void setUp() {
        trivialGroupMaker = new TrivialOperationGroupMaker();
        delsAsInsGroupMaker = new DeletionsAsInsertionsGroupMaker();
        emptyWholeBuffer = new PushDownAllGroups<>();
        dataDiskTree = new RRDiskDataTree<>();
        updateDiskTree = new RRDiskUpdateTree<>();
    }

    private static final class PiggybackingState {
        final RRTree<KPE> tree;
        final int bufSize;

        PiggybackingState(final RRTree<KPE> tree) {
            this.tree = tree;
            bufSize = tree.getCurrentBufferSize();
        }

        void assertNoPiggybacking() {
            assertEquals (tree.getCurrentBufferSize(), bufSize);
        }

        void assertStateChange(final int bufSizeDelta) {
            assertEquals (bufSize + bufSizeDelta, tree.getCurrentBufferSize());
        }
    }

    @Test
    public void insertDataTree() throws IOException {
        testInsert(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void insertUpdateTree() throws IOException {
        testInsert(updateDiskTree, delsAsInsGroupMaker);
    }
    
    private void testInsert(final IRRDiskTree<KPE> diskTree,
                            final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 101, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());
        queryNonexisting(tree, TestData.data[4]);
        int i;
        for (i = 0; i <= 100; i++) {
            checkedInsert(tree, TestData.data[i]);
            queryNonexisting(tree, TestData.data[i + 1]);
        }
        // Empty the buffer
        tree.insert(TestData.data[i]);
        i++;
        // Fill the buffer again
        for (; i <= 201; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer again
        // Split the main tree root!
        checkedInsert(tree, TestData.data[i]);
        i++;

        // Go for it!
        for (; i < TestData.data.length; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        for (i = 0; i < TestData.data2.length; i++) {
            checkedInsert(tree, TestData.data2[i]);
        }
        tree.cleanGarbage(false); // TODO: here and everywhere else: test with true too
    }

    @Test
    public void dataDiskTreeBufferInsert() throws IOException {
        testBufferInsert(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void updateDiskTreeBufferInsert() throws IOException {
        testBufferInsert(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testBufferInsert(final IRRDiskTree<KPE> diskTree,
                                  final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 101, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());
        int i;
        // Almost fill the buffer with insertions
        for (i = 0; i < 100; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Verify that everything can be deleted from it
        for (i = 0; i < 100; i++) {
            checkedDelete(tree, TestData.data[i]);
        }
        tree.cleanGarbage(false);
    }

    @Test
    public void deleteDataTree() throws IOException {
        testDelete(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void deleteUpdateTree() throws IOException {
        testDelete(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testDelete(final IRRDiskTree<KPE> diskTree,
                            final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        // TODO: deletions behave differently for disk and update trees, the comments below apply for disk tree
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 101, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());
        int i;
        // Fill the buffer with insertions
        for (i = 0; i <= 100; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer
        checkedInsert(tree, TestData.data[101]);
        // Clear the buffer
        checkedDelete(tree, TestData.data[101]);
        // Fill the buffer with deletions
        for (i = 0; i <= 100; i++) {
            checkedMissingDelete(tree, TestData.data[i]);
        }
        // Empty the buffer and totally empty the main tree
        checkedInsert(tree, TestData.data[101]);
        tree.cleanGarbage(false);
    }

    @Test
    public void insertSubtreeExactLevelDataDiskTree() throws IOException {
        testInsertSubtreeExactLevel(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void insertSubtreeExactLevelUpdateDiskTree() throws IOException {
        testInsertSubtreeExactLevel(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testInsertSubtreeExactLevel(final IRRDiskTree<KPE> diskTree,
                                             final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        // TODO: deletions behave differently for disk and update trees, the comments below apply for disk tree
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 101, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());
        int i;
        // Fill the buffer
        for (i = 0; i <= 100; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer
        checkedInsert(tree, TestData.data[707]);
        // Now we have two entries at the main root node, clear them almost totally
        // Prepare
        for (i = 0; i < 100; i++) {
            checkedMissingDelete(tree, TestData.data[i]);
        }
        // Empty the buffer and trigger InsertSubtree() with exact level child nodes
        checkedMissingDelete(tree, TestData.data[100]);
        tree.cleanGarbage(false);
    }

    @Test
    public void mergeSubtreeDataDiskTree() throws IOException {
        testMergeSubtree(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void mergeSubtreeUpdateDiskTree() throws IOException {
        testMergeSubtree(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testMergeSubtree(final IRRDiskTree<KPE> diskTree,
                                  final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        // TODO: it does not touch mergeSubtree at all
        // TODO: deletions behave differently for disk and update trees, the comments below apply for disk tree
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, 30, MAX_CAPACITY,
                50, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer, new NullObjectTracer<KPE>());
        int i;
        // Fill the buffer, empty the buffer, fill the buffer
        for (i = 0; i < 100; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer
        checkedInsert(tree, TestData.data[i]);
        // Fill the second node
        for (i = 50; i < 99; i++) {
            checkedInsert(tree, TestData.data2[i]);
        }
        checkedInsert(tree, TestData.data2[i]);

        // Now there are two entries at the main root of the tree, clear one of them
        for (i = 0; i <= 48; i++) {
            checkedMissingDelete(tree, TestData.data[i]);
        }
        checkedMissingDelete(tree, TestData.data[i]);
        tree.cleanGarbage(false);
    }

    @Test
    public void shrinkTreeDataDiskTree() throws IOException {
        testShrinkTree(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void shrinkTreeUpdateDiskTree() throws IOException {
        testShrinkTree(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testShrinkTree(final IRRDiskTree<KPE> diskTree,
                                final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        // TODO: deletions behave differently for disk and update trees, the comments below apply for disk tree
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 50, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());
        int i;
        // Fill the buffer, empty the buffer, fill the buffer
        for (i = 0; i < 100; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer
        checkedInsert(tree, TestData.data[i]);
        // Fill the second node
        for (i = 50; i <= 98; i++) {
            checkedInsert(tree, TestData.data2[i]);
        }

        // Now there are two entries at the main root of the tree, clear one of them
        for (i = 0; i <= 49; i++) {
            checkedMissingDelete(tree, TestData.data[i]);
        }
        checkedMissingDelete(tree, TestData.data[i]);
        tree.cleanGarbage(false);
    }

    @Test
    public void queryDiskDataTree() throws IOException {
        testQuery(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void queryDiskUpdateTree() throws IOException {
        testQuery(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testQuery(final IRRDiskTree<KPE> diskTree,
                           final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 50, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());
        try {
            tree.query(tree.rootDescriptor(), 1);
            fail("Expected exception not thrown");
        }
        catch (IllegalArgumentException ignored) {
            // OK!
        }
        int i;
        // Fill the buffer
        for (i = 0; i <= 50; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer
        checkedInsert(tree, TestData.data[i]);
        // Test that the objects in the main tree can be found one by one
        for (i = 0; i <= 50; i++) {
            querySingleExisting(tree, TestData.data[i]);
        }
        querySingleExisting(tree, TestData.data[i]);
        // Test region queries
        //noinspection unchecked
        final Cursor<KPE> results = tree.query(tree.rootDescriptor());
        int resultCounter = 0;
        while (results.hasNext()) {
            final KPE o = results.next();
            resultCounter++;
            int j;
            for (j = 0; j <= 51; j++) {
                if (TestData.data[j].equals(o))
                    break;
            }
            assertTrue(j <= 51);
        }
        assertSame(52, resultCounter);
        tree.cleanGarbage(false);
    }

    @Test
    public void piggybackingEnabledDataDiskTree() throws IOException {
        testPiggybackingEnabled(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void piggybackingEnabledUpdateDiskTree() throws IOException {
        testPiggybackingEnabled(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testPiggybackingEnabled(final IRRDiskTree<KPE> diskTree,
                                         final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        // TODO: deletions behave differently for disk and update trees, the comments below apply for disk tree
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 50, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());

        int i;
        // Fill the buffer
        for (i = 0; i <= 50; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer
        tree.insert(TestData.data[i]);

        // Add a new element that is close to some existing one
        tree.insert(TestData.data2[0]);
        // Do a query that would cause that element to be piggybacked
        PiggybackingState state = new PiggybackingState(tree);
        state.assertNoPiggybacking();
        querySingleExisting(tree, TestData.data2[0]);
        state.assertStateChange(-1);
        // The same with deletions
        tree.remove(TestData.data2[0]);
        state = new PiggybackingState(tree);
        queryNonexisting(tree, TestData.data2[0]);
        state.assertStateChange(-1);
        tree.cleanGarbage(false);
    }

    @Test
    public void piggybackingDisabledDiskDataTree() throws IOException {
        testPiggybackingDisabled(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void piggybackingDisabledUpdateDataTree() throws IOException {
        testPiggybackingDisabled(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testPiggybackingDisabled(final IRRDiskTree<KPE> diskTree,
                                          final AbstractOperationGroupMaker operationGroupMaker) throws IOException {
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 50, operationGroupMaker, false, false, 0.0, false, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());

        int i;
        // Fill the buffer
        for (i = 0; i <= 50; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        // Empty the buffer
        tree.insert(TestData.data[i]);

        // Add a new element that is close to some existing one
        tree.insert(TestData.data2[0]);
        // Do a query that would cause that element to be piggybacked
        PiggybackingState state = new PiggybackingState(tree);
        querySingleExisting(tree, TestData.data2[0]);
        state.assertNoPiggybacking();
        // The same with deletions
        tree.remove(TestData.data2[0]);
        state = new PiggybackingState(tree);
        queryNonexisting(tree, TestData.data2[0]);
        state.assertNoPiggybacking();
        tree.cleanGarbage(false);
    }

    // Check that piggybacking is limited not to make node underful. Only makes sense with the data tree.
    @Test
    public void piggybackingUnderflowProtection() {
        tree = new RRTree<>(dataDiskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 101, trivialGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());

        int i;
        for (i = 0; i <= 100; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        checkedInsert(tree, TestData.data[101]);
        checkedDelete(tree, TestData.data[101]);
        for (i = 0; i <= 9; i++) {
            checkedMissingDelete(tree, TestData.data[i]);
        }
        final PiggybackingState state = new PiggybackingState(tree);
        checkedMissingDelete(tree, TestData.data[10]);
        state.assertStateChange(1);
    }

    // Check that piggybacking is limited not to make node overful. Only makes sense with the update tree.
    @Test
    public void piggybackingOverflowProtection() {
        tree = new RRTree<>(dataDiskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, 30, MAX_CAPACITY,
                50, trivialGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer, new NullObjectTracer<KPE>());
        int i;
        for (i = 0; i < 100; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        checkedInsert(tree, TestData.data[i]);
        final PiggybackingState state = new PiggybackingState(tree);
        checkedInsert(tree, TestData.data2[50]);
        state.assertStateChange(1);
    }

    @Test
    public void doNotPiggybackIntersectingOnlyOpDataTree() throws IOException {
        testDoNotPiggybackIntersectingOnlyData(dataDiskTree, trivialGroupMaker);
    }

    @Test
    public void doNotPiggybackIntersectingOnlyOpUpdateTree() throws IOException {
        testDoNotPiggybackIntersectingOnlyData(updateDiskTree, delsAsInsGroupMaker);
    }

    private void testDoNotPiggybackIntersectingOnlyData(final IRRDiskTree<KPE> diskTree,
                                                        final AbstractOperationGroupMaker operationGroupMaker)
            throws IOException {
        // TODO: deletions behave differently for disk and update trees, the comments below apply for disk tree
        tree = new RRTree<>(diskTree);
        tree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY,
                MAX_CAPACITY, 50, operationGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer,
                new NullObjectTracer<KPE>());
        // Fill and empty the buffer
        for (int i = 0; i < 50; i++) {
            checkedInsert(tree, TestData.data[i]);
        }
        tree.remove(TestData.data[50]);
        // Insert a data element that overlaps but is not contained in the disk tree node
        final KPE datum = TestUtils.makeKPE(4567, -2.0, -2.0, 1.0, 1.0);
        assertTrue (tree.rootDescriptor().overlaps(tree.descriptor(datum)));
        assertFalse (tree.rootDescriptor().contains(tree.descriptor(datum)));

        // Put it to the buffer
        tree.insert(datum);

        final PiggybackingState state = new PiggybackingState(tree);
        // Do a query on the disk tree
        queryNonexisting(tree, TestUtils.makeKPE(11111, 0.5, 0.5, 1.75, 1.25));
        // The overlapping but not contained in the node MBR operation should not be piggybacked!
        state.assertStateChange(0);
        tree.cleanGarbage(false);
    }

    @Test
    public void wasBufferEmptied() {
        // TODO: the closest thing to unit test in the whole RR-tree testsuite
        tree = new RRTree<>(new RRDiskDataTree<KPE>());
        tree.initialize(null, TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY,
                50, trivialGroupMaker, true, true, 0.0, true, 0, 0, emptyWholeBuffer, new NullObjectTracer<KPE>());

        int i;
        // Fill the buffer
        for (i = 0; i < 50; i++) {
            checkedInsert(tree, TestData.data[i]);
            assertFalse (tree.wasBufferEmptied());
        }
        // Empty the buffer
        tree.insert(TestData.data[i]);
        assertTrue (tree.wasBufferEmptied());
    }
}
