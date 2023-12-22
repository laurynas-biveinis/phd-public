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
package aau.bufferedIndexes.operationGroupMakers;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import xxl.core.spatial.KPE;

import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for DeletionsAsInsertionsGroupMaker class
 */
public class DeletionsAsInsertionsGroupMakerTest {

    private final Mockery context = new JUnit4Mockery();

    @Test
    public void groupDeletion() {
        testSingleOpGrouping(TestUtils.makeOperation(0.0, 0.0, 1.0, 1.0, OperationType.DELETION));
    }

    @Test
    public void groupInsertion() {
        testSingleOpGrouping(TestUtils.makeOperation(0.0, 0.0, 1.0, 1.0, OperationType.INSERTION));
    }

    private void testSingleOpGrouping(final UpdateTree.Entry<KPE> op) {
        final AbstractOperationGroupMaker groupMaker = new DeletionsAsInsertionsGroupMaker();

        final IndexEntryOpGroupMap<KPE> result = new IndexEntryOpGroupMap<>();

        //noinspection unchecked
        final IRRTreeDiskNode<KPE> mockNode = context.mock(IRRTreeDiskNode.class);

        //noinspection unchecked
        final IRRTreeIndexEntry<KPE> mockIndexEntry = context.mock(IRRTreeIndexEntry.class);

        context.checking (new Expectations() {{
            oneOf (mockNode).chooseSubtreeByObject(with(same(op))); will(returnValue(mockIndexEntry));
            allowing(mockIndexEntry).container(); will(returnValue(null));
        }});

        groupMaker.groupOperation(op, op.getData(), mockNode, result);

        assertEquals (1, result.size());
        final Iterator<Map.Entry<IRRTreeIndexEntry<KPE>, OperationGroup<KPE>>> itr = result.iterator();
        final Map.Entry<IRRTreeIndexEntry<KPE>, OperationGroup<KPE>> group = itr.next();
        assertSame (mockIndexEntry, group.getKey());
        final OperationGroup<KPE> resultOps = group.getValue();
        assertEquals (1, resultOps.size());
        final Iterator<UpdateTree.Entry<KPE>> resultOpItr = resultOps.iterator();
        final UpdateTree.Entry<KPE> resultOp = resultOpItr.next();
        assertSame (op, resultOp);
        assertFalse (itr.hasNext());
    }

}
