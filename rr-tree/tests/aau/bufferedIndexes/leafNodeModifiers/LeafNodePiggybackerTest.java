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
package aau.bufferedIndexes.leafNodeModifiers;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.wrappers.IteratorCursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.spatial.KPE;
import xxl.core.spatial.rectangles.DoublePointRectangle;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for LeafNodePiggybacker class
 */
@RunWith(JMock.class)
public class LeafNodePiggybackerTest {

    private final Mockery context = new JUnit4Mockery();

    private IRRTree<KPE> mockTree;
    private IRRTreeBuffer<KPE> mockBuffer;
    private IRRTreeDiskNode<KPE> mockNode;
    private IRRTreeLeafPiggybackingInfo mockInfo;
    private ObjectTracer<KPE> mockTracer;

    private RRTreeStats<KPE> treeStats;

    private IRRTreeDiskNodeOnQueryModifier<KPE> piggybacker;

    private Descriptor nodeDescriptor;

    @Before
    public void setUp() {
        nodeDescriptor = TestUtils.makeDescriptor(1.0, 1.0, 4.0, 4.0);

        //noinspection unchecked
        mockTree = context.mock(IRRTree.class);
        //noinspection unchecked
        mockBuffer = context.mock(IRRTreeBuffer.class);
        //noinspection unchecked
        mockNode = context.mock(IRRTreeDiskNode.class);
        mockInfo = context.mock(IRRTreeLeafPiggybackingInfo.class);
        //noinspection unchecked
        mockTracer = context.mock(ObjectTracer.class);

        treeStats = new RRTreeStats<>();
        piggybacker = new LeafNodePiggybacker<>(mockTree, mockBuffer, treeStats, mockTracer);
    }

    @Test
    public void noModifications() {
        final Cursor<UpdateTree.Entry<KPE>> emptyCursor = new EmptyCursor<>();
        final Descriptor emptyDescriptor = new DoublePointRectangle(2);

        context.checking(new Expectations() {{
            ignoring(mockInfo);
            ignoring(mockNode).indexEntries();
            ignoring(mockNode).deleteEntryIndex();
            oneOf (mockNode).computeDescriptor(); will(returnValue(emptyDescriptor));
            oneOf (mockBuffer).queryEntryOfAnyType(with(same(emptyDescriptor))); will(returnValue(emptyCursor));
            oneOf (mockNode).selectFittingOperations(with(same(emptyCursor)), with(equal(false)));
            oneOf (mockNode).limitNumberOfOperations(with(aNonNull(RRTreeLeafPiggybackingInfo.class)));
        }});

        boolean result = piggybacker.modify(mockNode, false, 0.0, mockInfo);
        assertFalse (result);
    }

    private void expectPrepareForModification(final Collection<UpdateTree.Entry<KPE>> piggybackedOps) {
        final Cursor<UpdateTree.Entry<KPE>> bufferCursor
                = new IteratorCursor<>(piggybackedOps.iterator());
        context.checking(new Expectations() {{
            oneOf (mockNode).computeDescriptor(); will(returnValue(nodeDescriptor));
            oneOf (mockBuffer).queryEntryOfAnyType(nodeDescriptor); will(returnValue(bufferCursor));
            oneOf (mockNode).selectFittingOperations(with(same(bufferCursor)), with(equal(false)));
                will(returnValue(piggybackedOps));
        }});
    }

    private void expectNodeModification(final Collection<UpdateTree.Entry<KPE>> piggybackedOps,
                                        final int i, final int d) {
        expectPrepareForModification(piggybackedOps);
        context.checking(new Expectations() {{
            // TODO: exact args
            //noinspection unchecked
            exactly(i + d).of(mockTracer).traceUpdateTreeEntry(with(aNonNull(UpdateTree.Entry.class)),
                    with(same(ObjectTracer.Operation.LEAF_NODE_PIGGYBACKING)), with(same(null)));
            // TODO: exact counts in two expectations below
            allowing(mockInfo).addPotentialSizeIncreasingOp();
            allowing(mockInfo).addPotentialSizeDecreasingOp();
            ignoring(mockNode).indexEntries();
            ignoring(mockNode).deleteEntryIndex();
            //noinspection unchecked
            exactly(i + d).of(mockNode).operationWillIncreaseNodeSize(with(aNonNull(UpdateTree.Entry.class)));
                will(returnValue(false));
            oneOf (mockNode).executeConstrainedSubsetOfOps(with(same(piggybackedOps)),
                    with(any(Integer.class)), with(any(Integer.class))); will(returnValue(piggybackedOps));
            allowing(mockInfo).getUnpiggybackableSizeDecreasingOps(); will(returnValue(0));
            allowing(mockInfo).getUnpiggybackableSizeIncreasingOps(); will(returnValue(0));
            allowing(mockInfo).isNodeChanged(); will(returnValue(true));
            oneOf (mockInfo).getNumOfSizeIncreasingOps(); will(returnValue(i));
            oneOf (mockInfo).getNumOfSizeDecreasingOps(); will(returnValue(d));
        }});
    }

    @Test
    public void piggybackAllMatchingOps() {
        testPiggybacking(2, 1,
                new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION),
                new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION),
                new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));
    }

    @SuppressWarnings("FinalPrivateMethod")
    @SafeVarargs
    final private void testPiggybacking(final int i, final int d, final UpdateTree.Entry<KPE>... ops) {
        final Collection<UpdateTree.Entry<KPE>> piggybackedOps = TestUtils.makeOpList(ops);

        expectNodeModification(piggybackedOps, i, d);

        boolean result = piggybacker.modify(mockNode, true, 0.0, mockInfo);
        assertTrue (result);
    }

    @Test
    public void doNotPiggybackPreviouslyPiggybackedOps() {
        final Collection<UpdateTree.Entry<KPE>> piggybackedOps = TestUtils.makeOpList(
                new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION));

        expectNodeModification(piggybackedOps, 1, 0);

        boolean result = piggybacker.modify(mockNode, true, 0.0, mockInfo);
        assertTrue (result);

        final Collection<UpdateTree.Entry<KPE>> secondPiggybackedOps = TestUtils.makeOpList(
                new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION),
                new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION));

        final Collection<UpdateTree.Entry<KPE>> shouldBePiggybacked = TestUtils.makeOpList(
                new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION));

        expectPrepareForModification(secondPiggybackedOps);
        context.checking(new Expectations() {{
            ignoring(mockInfo);
            // TODO: exact args
            //noinspection unchecked
            atMost(secondPiggybackedOps.size()).of(mockTracer).traceUpdateTreeEntry(with(aNonNull(UpdateTree.Entry.class)),
                    with(same(ObjectTracer.Operation.LEAF_NODE_PIGGYBACKING)), with(same(null)));
            //noinspection unchecked
            atMost(secondPiggybackedOps.size()).of(mockNode).
                    operationWillIncreaseNodeSize(with(aNonNull(UpdateTree.Entry.class)));
                will(returnValue(false));
            oneOf (mockNode).executeConstrainedSubsetOfOps(with(equal(shouldBePiggybacked)),
                    with(any(Integer.class)), with(any(Integer.class))); will(returnValue(shouldBePiggybacked));
        }});
        
        result = piggybacker.modify(mockNode, true, 0.0, mockInfo);
        assertTrue (result);
    }

    @Test
    public void piggybackOnlySomeOpsAvoidNodeReorganization() {
        final Collection<UpdateTree.Entry<KPE>> piggybackedOps = TestUtils.makeOpList(
                new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION),
                new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION),
                new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));

        final Collection<UpdateTree.Entry<KPE>> actuallyPiggybackedOps = TestUtils.makeOpList(
                new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION),
                new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION));

        expectPrepareForModification(piggybackedOps);
        context.checking(new Expectations() {{
            // TODO: exact args
            //noinspection unchecked
            atMost(piggybackedOps.size()).of(mockTracer).traceUpdateTreeEntry(with(aNonNull(UpdateTree.Entry.class)),
                    with(same(ObjectTracer.Operation.LEAF_NODE_PIGGYBACKING)), with(same(null)));
            // TODO: exact counts below
            allowing(mockInfo).addPotentialSizeIncreasingOp();
            allowing(mockInfo).addPotentialSizeDecreasingOp();
            ignoring(mockNode).indexEntries();
            ignoring(mockNode).deleteEntryIndex();            
            //noinspection unchecked
            exactly(3).of(mockNode).operationWillIncreaseNodeSize(with(aNonNull(UpdateTree.Entry.class)));
                will(returnValue(false));             
            oneOf (mockNode).limitNumberOfOperations(with(same(mockInfo)));
            allowing(mockInfo).getUnpiggybackableSizeDecreasingOps(); will(returnValue(0));
            allowing(mockInfo).getUnpiggybackableSizeIncreasingOps(); will(returnValue(1));
            allowing(mockInfo).isNodeChanged(); will(returnValue(true));
            oneOf (mockInfo).getNumOfSizeIncreasingOps(); will(returnValue(1));
            oneOf (mockInfo).getNumOfSizeDecreasingOps(); will(returnValue(1));
                        
            oneOf (mockNode).executeConstrainedSubsetOfOps(with(same(piggybackedOps)),
                    with(1), with(1)); will(returnValue(actuallyPiggybackedOps));
        }});

        boolean result = piggybacker.modify(mockNode, false, 0.0, mockInfo);
        assertTrue (result);

        assertEquals (0, treeStats.getTotalNonPiggybackedNodeSizeDecreasingOps());
        assertEquals (1, treeStats.getTotalNonPiggybackedNodeSizeIncreasingOps());
    }

    @Test
    public void modifyNodeNullDescriptor() {
        context.checking(new Expectations() {{
            oneOf (mockNode).computeDescriptor(); will(returnValue(null));
        }});

        boolean result = piggybacker.modify(mockNode, false, 0.0, mockInfo);
        assertFalse(result);
        assertEquals(0, treeStats.getTotalNonPiggybackedNodeSizeDecreasingOps());
        assertEquals(0, treeStats.getTotalNonPiggybackedNodeSizeIncreasingOps());
    }

    @Test
    public void piggybackWithEpsilon() {
        final double epsilon = 0.5;

        final Collection<UpdateTree.Entry<KPE>> piggybackedOps = TestUtils.makeOpList(
                new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION),
                new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION),
                new UpdateTree.Entry<>(TestData.data[2], OperationType.INSERTION));

        final Descriptor expandedDescriptor = RRTree.expandDescriptor(nodeDescriptor, epsilon);

        final Cursor<UpdateTree.Entry<KPE>> bufferCursor
                = new IteratorCursor<>(piggybackedOps.iterator());
        context.checking(new Expectations() {{
            oneOf (mockNode).computeDescriptor(); will(returnValue(nodeDescriptor));
            oneOf (mockBuffer).queryEntryOfAnyType(with(equal(expandedDescriptor))); will(returnValue(bufferCursor));
            oneOf (mockNode).selectFittingOperations(with(same(bufferCursor)), with(equal(true)));
                will(returnValue(piggybackedOps));
            // TODO: exact args
            //noinspection unchecked
            exactly(2 + 1).of(mockTracer).traceUpdateTreeEntry(with(aNonNull(UpdateTree.Entry.class)),
                    with(same(ObjectTracer.Operation.LEAF_NODE_PIGGYBACKING)), with(same(null)));
            // TODO: exact counts in two expectations below
            allowing(mockInfo).addPotentialSizeIncreasingOp();
            allowing(mockInfo).addPotentialSizeDecreasingOp();
            ignoring(mockNode).indexEntries();
            ignoring(mockNode).deleteEntryIndex();
            //noinspection unchecked
            exactly(2 + 1).of(mockNode).operationWillIncreaseNodeSize(with(aNonNull(UpdateTree.Entry.class)));
                will(returnValue(false));
            oneOf (mockNode).executeConstrainedSubsetOfOps(with(same(piggybackedOps)),
                    with(any(Integer.class)), with(any(Integer.class))); will(returnValue(piggybackedOps));
            allowing(mockInfo).getUnpiggybackableSizeDecreasingOps(); will(returnValue(0));
            allowing(mockInfo).getUnpiggybackableSizeIncreasingOps(); will(returnValue(0));
            allowing(mockInfo).isNodeChanged(); will(returnValue(true));
            oneOf (mockInfo).getNumOfSizeIncreasingOps(); will(returnValue(2));
            oneOf (mockInfo).getNumOfSizeDecreasingOps(); will(returnValue(1));
        }});

        boolean result = piggybacker.modify(mockNode, true, 0.5, mockInfo);
        assertTrue(result);
    }

    @Test
    public void finalizeModifications() {
        final UpdateTree.Entry<KPE> op1 = new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION);
        final UpdateTree.Entry<KPE> op2 = new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION);

        testPiggybacking(1, 0, op1);
        testPiggybacking(0, 1, op2);

        context.checking (new Expectations() {{
            exactly(1).of(mockTree).completeOperation(with(same(op1)));
            exactly(1).of(mockBuffer).removeExactEntry(with(same(op1)));
            exactly(1).of(mockTree).completeOperation(with(same(op2)));
            exactly(1).of(mockBuffer).removeExactEntry(with(same(op2)));
        }});

        final OperationTypeStat stats = new OperationTypeStat("test");
        piggybacker.finalizeModifications(stats);
        
        assertEquals (1, stats.getDeletions());
        assertEquals (1, stats.getInsertions());
    }
}
