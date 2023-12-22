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

import aau.bufferedIndexes.OperationType;
import aau.bufferedIndexes.TestData;
import aau.bufferedIndexes.TestUtils;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.leafNodeModifiers.NullModeModifier;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.workload.DataID;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.Function;
import xxl.core.spatial.KPE;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Unit tests for the RRDiskUpdateTree class.
 */
@RunWith(JMock.class)
public class RRDiskUpdateTreeTest {

    private final Mockery context = new JUnit4Mockery();

    private Container container;

    private RRDiskUpdateTree<KPE> diskTree;

    @Before
    public void setUp() {
        container = new MapContainer();
        diskTree = new RRDiskUpdateTree<>();
    }

    @Test
    public void nodeDoesOperationFitFittingInsertion() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[1], OperationType.INSERTION), true);
    }

    @Test
    public void nodeDoesOperationFitFittingDeletion() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION), true);
    }
    
    @Test
    public void nodeDoesOperationFitUnfittingInsertion() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[4], OperationType.INSERTION), false);
    }

    @Test
    public void nodeDoesOperationFitUnfittingDeletion() {
        testNodeDoesOperationFit(new UpdateTree.Entry<>(TestData.data[4], OperationType.DELETION), false);
    }

    // TODO: almost copy paste
    private void testNodeDoesOperationFit(final UpdateTree.Entry<KPE> op, final boolean expectedResult) {
        diskTree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, container, 1, 10, null);
        RRDiskUpdateTree<KPE>.Node node = makeNodeWithContents(0, OperationType.INSERTION, TestData.data[0],
                TestData.data[2]);

        assertEquals (expectedResult, node.doesOperationFit(node.computeDescriptor(), op, false));
    }

    // TODO: four tests below: copy-paste
    @Test
    public void nodeOperationDWillIncreaseNodeSize() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        RRDiskUpdateTree<KPE>.Node node = makeNodeWithContents(0, OperationType.INSERTION, TestData.data[0],
                TestData.data[2]);
        node.indexEntries();
        assertTrue (node.operationWillIncreaseNodeSize(new UpdateTree.Entry<>(TestData.data[1],
                OperationType.DELETION)));
        node.deleteEntryIndex();
    }

    @Test
    public void nodeOperationDWillNotIncreaseNodeSize() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        RRDiskUpdateTree<KPE>.Node node = makeNodeWithContents(0, OperationType.INSERTION, TestData.data[0],
                TestData.data[2]);
        node.indexEntries();
        assertFalse (node.operationWillIncreaseNodeSize(new UpdateTree.Entry<>(TestData.data[0],
                OperationType.DELETION)));
        node.deleteEntryIndex();
    }

    @Test
    public void nodeOperationIWillIncreaseNodeSize() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        RRDiskUpdateTree<KPE>.Node node = makeNodeWithContents(0, OperationType.INSERTION, TestData.data[0],
                TestData.data[2]);
        node.indexEntries();
        assertTrue (node.operationWillIncreaseNodeSize(new UpdateTree.Entry<>(TestData.data[1],
                OperationType.INSERTION)));
        node.deleteEntryIndex();
    }

    @Test
    public void nodeOperationIWillDecreaseNodeSize() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        RRDiskUpdateTree<KPE>.Node node = makeNodeWithContents(0, OperationType.DELETION, TestData.data[0],
                TestData.data[2]);
        node.indexEntries();
        assertFalse (node.operationWillIncreaseNodeSize(new UpdateTree.Entry<>(TestData.data[0],
                OperationType.INSERTION)));
        node.deleteEntryIndex();
    }

    @Test
    public void nodeExecuteConstrainedSubsetOfOpsFullSetUpToLimits () {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0, OperationType.INSERTION,
                TestData.data[0], TestData.data[1], TestData.data[2]);

        final Collection<UpdateTree.Entry<KPE>> correctSet = new ArrayList<>();
        correctSet.add (new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION));
        correctSet.add (new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));
        correctSet.add (new UpdateTree.Entry<>(TestData.data[4], OperationType.DELETION));

        final Collection<UpdateTree.Entry<KPE>> candidateSet = new ArrayList<>();
        candidateSet.addAll(correctSet);
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[5], OperationType.INSERTION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[6], OperationType.DELETION));

        n.indexEntries();
        final Collection<UpdateTree.Entry<KPE>> executedSet = n.executeConstrainedSubsetOfOps(candidateSet, 2, 1);
        n.deleteEntryIndex();
        assertArrayEquals (correctSet.toArray(), executedSet.toArray());
        // TODO: here and below: the tests do not check the resulting node contents!
    }

    @Test
    public void nodeExecuteConstrainedSubsetOfOpsSubsetOfIncreasingOps() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0, OperationType.INSERTION,
                TestData.data[0], TestData.data[1], TestData.data[2]);

        final Collection<UpdateTree.Entry<KPE>> candidateSet = new ArrayList<>();
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[4], OperationType.DELETION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[3], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> shouldBe = new ArrayList<>();
        shouldBe.add (new UpdateTree.Entry<>(TestData.data[4], OperationType.DELETION));

        n.indexEntries();
        final Collection<UpdateTree.Entry<KPE>> executedSet = n.executeConstrainedSubsetOfOps(candidateSet, 1, 1);
        n.deleteEntryIndex();
        assertArrayEquals (shouldBe.toArray(), executedSet.toArray());
    }

    @Test
    public void nodeExecuteConstrainedSubsetOfOpsSubsetOfDecreasingOps() {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        final IRRTreeDiskNode<KPE> n = makeNodeWithContents(0, OperationType.INSERTION,
                TestData.data[0], TestData.data[1], TestData.data[2]);

        final Collection<UpdateTree.Entry<KPE>> candidateSet = new ArrayList<>();
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION));
        candidateSet.add (new UpdateTree.Entry<>(TestData.data[2], OperationType.DELETION));

        final Collection<UpdateTree.Entry<KPE>> shouldBe = new ArrayList<>();
        shouldBe.add (new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION));

        n.indexEntries();
        final Collection<UpdateTree.Entry<KPE>> executedSet = n.executeConstrainedSubsetOfOps(candidateSet, 1, 1);
        n.deleteEntryIndex();
        assertArrayEquals (shouldBe.toArray(), executedSet.toArray());
    }

    // TODO: unit test with node size decreasing deletion

    @Test
    public void nodeExecuteOpInsertion() {
        testNodeExecute (TestData.data[0], OperationType.INSERTION, TestData.data[1], OperationType.INSERTION,
                false, true, 2);
    }

    @Test
    public void nodeExecuteOpDeletionFindsInsertion() {
        testNodeExecute (TestData.data[0], OperationType.INSERTION, TestData.data[0], OperationType.DELETION,
                false, true, 0);
    }

    @Test
    public void nodeExecuteOpDeletionDoesNotFindInsertion() {
        testNodeExecute (TestData.data[0], OperationType.INSERTION, TestData.data[1], OperationType.DELETION, false,
                true, 2);
    }

    @Test
    public void nodeExecuteOpInsertionFindsDeletion() {
        testNodeExecute (TestData.data[0], OperationType.DELETION, TestData.data[0], OperationType.INSERTION, false,
                true, 0);
    }
    
    private void testNodeExecute(final KPE existingData, final OperationType existingDataOp, final KPE dataToExecute,
                                 final OperationType opToExecute, final boolean insertionDeletesOldInsertion,
                                 final boolean expectedResult, final int expectedSize) {
        diskTree.initialize(TestUtils.GET_DESCRIPTOR, container, 1, 10);
        final RRDiskUpdateTree<KPE>.Node n = makeNodeWithContents(0, existingDataOp, existingData);

        Assert.assertEquals (expectedResult,
                n.executeOp(new UpdateTree.Entry<>(dataToExecute, opToExecute), insertionDeletesOldInsertion));
        Assert.assertEquals (expectedSize, n.number());
    }

    private RRDiskUpdateTree<KPE>.Node makeNodeWithContents(final int level, final OperationType typeOfEntries,
                                                            final KPE... contents) {
        final RRDiskUpdateTree<KPE>.Node n = diskTree.createNode(level);
        for (KPE entry : contents) {
            n.grow(new UpdateTree.Entry<>(entry, typeOfEntries));
        }
        return n;
    }

    @Test
    public void createNodeWithContents() {
        diskTree.initialize(Function.IDENTITY, null, 50, 100);

        final UpdateTree.Entry<KPE> op1 = new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION);
        final UpdateTree.Entry<KPE> op2 = new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION);

        final List<UpdateTree.Entry<KPE>> nodeContents = new ArrayList<>();
        nodeContents.add(op1);
        nodeContents.add(op2);
        final IRRTreeDiskNode<KPE> n = diskTree.createNode(12, nodeContents);
        assertEquals (12, n.level());
        assertEquals (2, n.getEntries().size());
        assertTrue (n.getEntries().contains(op1));
        assertTrue (n.getEntries().contains(op2));
    }

    @Test
    public void descriptorOfOperation() {
        diskTree.initialize(null, TestUtils.GET_DESCRIPTOR, null, -1, -1, null);

        final UpdateTree.Entry<KPE> op = new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION);
        assertEquals (TestData.data[0].getData(), diskTree.descriptor(op));
    }

    private final Function<KPE, DataID> dataIdGetter = new Function<KPE, DataID>() {
        public DataID invoke (final KPE arg) {
            return (DataID)arg.getID();
        }
    };

    // TODO: unit tests for rrQueryProcessResults that take into account buffer

    @Test
    public void rrQueryProcessResultsEmptyResults() {
        diskTree.initialize(dataIdGetter, null, 0, 0);
        final Cursor<UpdateTree.Entry<KPE>> initialResults = new EmptyCursor<>();
        final Cursor<KPE> processedResults = diskTree.rrQueryProcessResults(initialResults,
                initialResults, new NullModeModifier<KPE>(), null);
        assertFalse (processedResults.hasNext());
    }

    @Test
    public void rrQueryProcessResultsAllCandidatesUnique() {
        //noinspection unchecked
        testRrQueryProcessResults(new UpdateTree.Entry[]{
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 2, 1.0, 1.0, 2.0, 2.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 3, 2.0, 2.0, 3.0, 3.0)}, 
                new KPE[]{
                    TestUtils.makeKPE(1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeKPE(2, 1.0, 1.0, 2.0, 2.0),
                    TestUtils.makeKPE(3, 2.0, 2.0, 3.0, 3.0)});
    }

    @Test
    public void rrQueryProcessResultsDeletionCancelsInsertion() {
        //noinspection unchecked
        testRrQueryProcessResults(new UpdateTree.Entry[]{
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 2, 1.0, 1.0, 2.0, 2.0),
                    TestUtils.makeOperation(OperationType.DELETION, 2, 1.0, 1.0, 2.0, 2.0)},
                new KPE[]{
                    TestUtils.makeKPE(1, 0.0, 0.0, 1.0, 1.0)});
    }

    @Test
    public void rrQueryProcessResultsInsertionCancelsDeletion() {
        //noinspection unchecked
        testRrQueryProcessResults(new UpdateTree.Entry[]{
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 2, 1.0, 1.0, 2.0, 2.0),
                    TestUtils.makeOperation(OperationType.DELETION, 2, 1.0, 1.0, 2.0, 2.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 2, 1.0, 1.0, 2.0, 2.0)},
                new KPE[]{
                    TestUtils.makeKPE(1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeKPE(2, 1.0, 1.0, 2.0, 2.0)});
    }

    @Test
    public void rrQueryProcessResultsSameIDInsertionsBeforeDeletions() {
        //noinspection unchecked
        testRrQueryProcessResults(new UpdateTree.Entry[]{
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 1.0, 1.0, 2.0, 2.0),
                    TestUtils.makeOperation(OperationType.DELETION, 1, 0.0, 0.0, 1.0, 1.0)},
                new KPE[]{
                    TestUtils.makeKPE(1, 1.0, 1.0, 2.0, 2.0)
                }
        );
    }

    @Test
    public void rrQueryProcessResultsTwoInsertionsToSamePlace() {
        //noinspection unchecked
        testRrQueryProcessResults(new UpdateTree.Entry[]{
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeOperation(OperationType.DELETION, 1, 0.0, 0.0, 1.0, 1.0)},
                new KPE[]{
                    TestUtils.makeKPE(1, 0.0, 0.0, 1.0, 1.0)
                }
        );
    }

    @Test
    public void rrQueryProcessResultsInsertionsDeletionsMixedOrder() {
        //noinspection unchecked
        testRrQueryProcessResults(new UpdateTree.Entry[]{
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 1.0, 1.0, 2.0, 2.0),
                    TestUtils.makeOperation(OperationType.DELETION, 1, 0.0, 0.0, 1.0, 1.0),
                    TestUtils.makeOperation(OperationType.INSERTION, 1, 0.0, 0.0, 1.0, 1.0)},
                new KPE[]{
                    TestUtils.makeKPE(1, 1.0, 1.0, 2.0, 2.0)
                }
        );          
    }

    private void testRrQueryProcessResults(final UpdateTree.Entry<KPE>[] initialResults, final KPE[] expectedResults) {
        //noinspection unchecked
        final ObjectTracer<KPE> objectTracer = context.mock(ObjectTracer.class);

        context.checking(new Expectations() {{
            //noinspection unchecked
            exactly(initialResults.length).of(objectTracer).traceUpdateTreeEntry(with(aNonNull(UpdateTree.Entry.class)),
                    with(same(ObjectTracer.Operation.UPDATE_TREE_QUERY_INITIAL_RESULT)), with(same(null)));
        }});

        diskTree.initialize(dataIdGetter, TestUtils.NULL_GET_DESCRIPTOR, null, 0, 0, objectTracer);
        final Cursor<UpdateTree.Entry<KPE>> initialResultCursor
                = new ArrayCursor<>(initialResults);
        final Cursor<KPE> processedResults
                = diskTree.rrQueryProcessResults(initialResultCursor,
                new EmptyCursor<UpdateTree.Entry<KPE>>(), new NullModeModifier<KPE>(), null);
        final Collection<KPE> expectedResultColl = new HashSet<>(Arrays.asList(expectedResults));
        while (processedResults.hasNext()) {
            final KPE processedEntry = processedResults.next();
            final boolean found = expectedResultColl.remove(processedEntry);
            assertTrue (found);
        }
        assertEquals (0, expectedResultColl.size());
    }
}
