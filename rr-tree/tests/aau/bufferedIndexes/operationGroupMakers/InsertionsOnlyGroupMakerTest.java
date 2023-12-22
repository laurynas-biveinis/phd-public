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
package aau.bufferedIndexes.operationGroupMakers;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import xxl.core.spatial.KPE;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

/**
 * Tests for InsertionsOnlyGroupMaker
 */
public class InsertionsOnlyGroupMakerTest {

    private final Mockery context = new JUnit4Mockery();

    private IRRTreeIndexEntry<KPE> mockIndexEntry;

    private IRRTreeDiskNode<KPE> mockNode;

    @Before
    public void setUp() {
        //noinspection unchecked
        mockIndexEntry = context.mock(IRRTreeIndexEntry.class);
        //noinspection unchecked
        mockNode = context.mock(IRRTreeDiskNode.class);
    }

    @Test
    public void testGroupInsertion() {
        final AbstractOperationGroupMaker groupMaker = new InsertionsOnlyGroupMaker();

        final UpdateTree.Entry<KPE> op = TestUtils.makeOperation(1.0, 1.0, 2.0, 2.0, OperationType.INSERTION);

        final IndexEntryOpGroupMap<KPE> result = new IndexEntryOpGroupMap<>();

        context.checking (new Expectations() {{
            oneOf (mockNode).chooseSubtreeByObject(with(same(op.getData()))); will(returnValue(mockIndexEntry));
            allowing(mockIndexEntry).container(); will(returnValue(null));
        }});

        groupMaker.groupOperation(op, op.getData(), mockNode, result);

        checkSingleOpResult(mockIndexEntry, op, result);
    }

    @Test
    public void testGroupDeletion() {
        final AbstractOperationGroupMaker groupMaker = new InsertionsOnlyGroupMaker();

        final UpdateTree.Entry<KPE> op = TestUtils.makeOperation(1.0, 1.0, 2.0, 2.0, OperationType.DELETION);

        final IndexEntryOpGroupMap<KPE> result = new IndexEntryOpGroupMap<>();

        context.checking (new Expectations() {{
        }});

        groupMaker.groupOperation(op, op.getData(), mockNode, result);

        checkSingleOpResult(result.ORPHAN_GROUP_KEY, op, result);
    }

    private void checkSingleOpResult(final IRRTreeIndexEntry<KPE> groupKey, final UpdateTree.Entry<KPE> op,
                                     final IndexEntryOpGroupMap<KPE> result) {
        assertEquals(1, result.size());
        final Iterator<Map.Entry<IRRTreeIndexEntry<KPE>, OperationGroup<KPE>>> itr = result.iterator();
        final Map.Entry<IRRTreeIndexEntry<KPE>, OperationGroup<KPE>> group = itr.next();
        assertSame (groupKey, group.getKey());
        final OperationGroup<KPE> resultOps = group.getValue();
        assertEquals (1, resultOps.size());
        final Iterator<UpdateTree.Entry<KPE>> resultOpItr = resultOps.iterator();
        final UpdateTree.Entry<KPE> resultOp = resultOpItr.next();
        assertSame (op, resultOp);
        assertFalse (itr.hasNext());
    }

}
