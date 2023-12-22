/*
     Copyright (C) 2007, 2008, 2009, 2010, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.operationGroupMakers;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import junit.framework.TestCase;
import xxl.core.indexStructures.Descriptor;
import xxl.core.spatial.KPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// TODO: split out AbstractOperationGroupMaker tests
// TODO: new unit tests (see DeletionsAsInsertionsGroupMakerTest)
// TODO: arrange these to be integration tests

@SuppressWarnings({"MagicNumber"})
public class TrivialOperationGroupMakerTest extends TestCase {
    private final AbstractOperationGroupMaker groupMaker = new TrivialOperationGroupMaker();

    public void testGroupOperationsDeletionSplits() {

        // No grouping yet
        final AbstractOperationGroupMaker groupMaker = new TrivialOperationGroupMaker();
        try {
            groupMaker.groupOperations(null, null);
            fail("Should rise IllegalArgumentException");
        }
        catch (IllegalArgumentException ignored) {}

        // Empty grouping
        checkGetGrouping(true);

        // Single entry, single insertion inside that entry
        checkGetGrouping(true, TestUtils.makeOperation(0, 0, 1, 1, OperationType.INSERTION),
                               TestUtils.makeDescriptor(0, 0, 1, 1));
        // Single entry, single insertion outside that entry
        checkGetGrouping(true, TestUtils.makeOperation(0, 0, 1, 1, OperationType.INSERTION),
                               TestUtils.makeDescriptor(2, 2, 3, 3));
        // Single entry, single deletion inside
        checkGetGrouping(true, TestUtils.makeOperation(0, 0, 1, 1, OperationType.DELETION),
                               TestUtils.makeDescriptor(0, 0, 1, 1));
        // Single entry, single deletion outside
        checkGetGrouping(true, TestUtils.makeOperation(4, 4, 5, 5, OperationType.DELETION),
                               TestUtils.makeDescriptor(0, 0, 1, 1));
        // Single entry, single insertion inside that entry and deletion
        checkGetGrouping(true, TestUtils.makeOperation(1, 1, 2, 2, OperationType.INSERTION),
                               TestUtils.makeOperation(3, 3, 4, 4, OperationType.DELETION),
                               TestUtils.makeDescriptor(0, 0, 5, 5));
        // Single entry, single insertion inside, single outside
        checkGetGrouping(true, TestUtils.makeOperation(1, 1, 2, 2, OperationType.INSERTION),
                               TestUtils.makeOperation(3, 3, 4, 4, OperationType.INSERTION),
                               TestUtils.makeDescriptor(0, 0, 2, 2));
        // Single entry, single insertion inside, single outside and deletion
        checkGetGrouping(true, TestUtils.makeOperation(1, 1, 2, 2, OperationType.INSERTION),
                               TestUtils.makeOperation(3, 3, 4, 4, OperationType.INSERTION),
                               TestUtils.makeOperation(0, 0, 1, 1, OperationType.DELETION),
                               TestUtils.makeDescriptor(0, 0, 2, 2));
        // Two entries, single insertion inside one of them
        checkGetGrouping(true, TestUtils.makeOperation(0, 0, 1, 1, OperationType.INSERTION),
                               TestUtils.makeDescriptor(0, 0, 1, 1), TestUtils.makeDescriptor(2, 2, 3, 3));
        // Two entries, single insertion outside them
        checkGetGrouping(true, TestUtils.makeOperation(0, 0, 1, 1, OperationType.INSERTION),
                               TestUtils.makeDescriptor(2, 2, 3, 3), TestUtils.makeDescriptor(4, 4, 4, 4));
        // Split deletion
        checkGetGrouping(true, TestUtils.makeOperation(3, 3, 4, 4, OperationType.DELETION),
                               TestUtils.makeDescriptor(0, 0, 4, 4), TestUtils.makeDescriptor(3, 3, 5, 5));
        // Two deletions, each split into two
        checkGetGrouping(true, TestUtils.makeOperation(0, 0, 1, 1, OperationType.DELETION),
                               TestUtils.makeOperation(10, 10, 11, 11, OperationType.DELETION),
                               TestUtils.makeDescriptor(0, 0, 8, 1), TestUtils.makeDescriptor(0, 0, 1, 8),
                               TestUtils.makeDescriptor(10, 10, 18, 11), TestUtils.makeDescriptor(10, 10, 11, 18));
        // Everything together
        checkGetGrouping(true, TestUtils.makeOperation(0, 0, 1, 1, OperationType.INSERTION), // i inside
                                    TestUtils.makeOperation(1.5, 1.5, 2, 2, OperationType.INSERTION), // i outside
                                    TestUtils.makeOperation(0, 10, 1, 11, OperationType.DELETION), // d outside
                                    TestUtils.makeOperation(2, 2, 3, 3, OperationType.DELETION), // d inside
                                    TestUtils.makeOperation(5, 5, 6, 6, OperationType.DELETION), // d split
                               TestUtils.makeDescriptor(0, 0, 1.1, 1.1),
                                    TestUtils.makeDescriptor(2, 2, 6, 6),
                                    TestUtils.makeDescriptor(5, 5, 7, 7),
                                    TestUtils.makeDescriptor(4, 5, 6, 7));

        // Two entries, single deletion partially overlapping both but contained in neither
        checkGetGrouping(true, TestUtils.makeOperation(1, 1, 3, 3, OperationType.DELETION),
                TestUtils.makeDescriptor(0, 0, 2, 2),
                TestUtils.makeDescriptor(2, 2, 4, 4));
    }

    public void testResetStatistics() {
        checkGetGrouping(false, TestUtils.makeOperation(3, 3, 4, 4, OperationType.DELETION),
                                TestUtils.makeDescriptor(0, 0, 4, 4), TestUtils.makeDescriptor(3, 3, 5, 5));
        checkGetGrouping(false, TestUtils.makeOperation(3, 3, 4, 4, OperationType.DELETION),
                                TestUtils.makeDescriptor(0, 0, 4, 4), TestUtils.makeDescriptor(3, 3, 5, 5));
        final StatisticalData deletionSplits = groupMaker.getDeletionSplits();
        assertEquals (1, deletionSplits.size());
        assertEquals (2, deletionSplits.get(2));
        groupMaker.resetStatistics();
        assertEquals (0, groupMaker.getDeletionSplits().size());
    }

    @SuppressWarnings({"ChainOfInstanceofChecks"})
    private void checkGetGrouping(final boolean resetStatistics, final Object... opsAndDescriptors) {
        final OperationGroup<KPE> ops = new OperationGroup<>();
        final Collection<Descriptor> descriptors = new ArrayList<>();
        for (final Object o : opsAndDescriptors) {
            if (o instanceof UpdateTree.Entry) {
                //noinspection unchecked
                ops.add((UpdateTree.Entry<KPE>)o);
            }
            else if (o instanceof Descriptor)
                descriptors.add((Descriptor)o);
            else
                fail("Buggy test: pass Entry or Descriptor...");
        }
        if (resetStatistics)
            groupMaker.resetStatistics();

        final IRRTreeDiskNode<KPE> node = makeNode(descriptors);
        final IndexEntryOpGroupMap<KPE> groupMap = groupMaker.groupOperations(node, ops);
        final StatisticalData recomputedSplits = new StatisticalData();
        int totalResultSize = 0;
        for (final UpdateTree.Entry<KPE> op : ops) {
            totalResultSize++;
            if (op.isDeletion()) {
                int deletionSplits = 0;
                for (final Map.Entry<IRRTreeIndexEntry<KPE>, OperationGroup<KPE>> gOp : groupMap) {
                    if (gOp.getValue().contains(op)) {
                        if (gOp.getKey() != groupMap.ORPHAN_GROUP_KEY) {
                            deletionSplits++;
                            assertTrue (TestUtils.getDescriptor(gOp.getKey()).contains(TestUtils.getDescriptor(op.getData())));                            
                        }
                    }
                    else {
                        if (gOp.getKey() != groupMap.ORPHAN_GROUP_KEY)
                            assertTrue (!TestUtils.getDescriptor(gOp.getKey()).contains(TestUtils.getDescriptor(op.getData())));
                    }
                }
                if (deletionSplits > 0)
                    totalResultSize += deletionSplits - 1;
                assertTrue (deletionSplits <= descriptors.size());
                recomputedSplits.update(deletionSplits, 1);
            }
            else {
                boolean found = false;
                for (final Map.Entry<IRRTreeIndexEntry<KPE>, OperationGroup<KPE>> gOp : groupMap) {
                    if (gOp.getValue().contains(op)) {
                        assertTrue (!found);
                        found = true;
                    }
                }
            }
        }
        int totalResultSize2 = 0;
        for (final Map.Entry<IRRTreeIndexEntry<KPE>, OperationGroup<KPE>> gOp : groupMap) {
            totalResultSize2 += gOp.getValue().size();
        }
        assertEquals (totalResultSize, totalResultSize2);
        if (resetStatistics)
            assertEquals (groupMaker.getDeletionSplits(), recomputedSplits);
        checkFlattenGroups(groupMap, ops, descriptors);
    }

    // TODO: move to IndexEntryOpGroupMap tests
    private static void checkFlattenGroups(final IndexEntryOpGroupMap<KPE> groupMap,
                                           final Iterable<UpdateTree.Entry<KPE>> ops,
                                           final Iterable<Descriptor> descriptors) {
        final OperationGroup<KPE> flattenedGroups = groupMap.flatten();
        for (final UpdateTree.Entry<KPE> op : ops) {
            if (!flattenedGroups.contains(op)) {
                assertTrue (op.isDeletion());
                for (final Descriptor desc : descriptors) {
                    assertTrue (!desc.contains(TestUtils.getDescriptor(op.getData())));
                }
            }
        }
    }

    private static IRRTreeDiskNode<KPE> makeNode(final Iterable<Descriptor> descriptors) {
        final IRRTreeDiskNode<KPE> node = TestUtils.makeNode(1);
        for (final Descriptor d : descriptors) {
            TestUtils.addEntry(node, TestUtils.makeIndexEntry(d, 0));
        }
        return node;
    }
}
