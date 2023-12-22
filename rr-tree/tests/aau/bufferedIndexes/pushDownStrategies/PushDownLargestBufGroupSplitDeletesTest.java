/*
     Copyright (C) 2009, 2010, 2011, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.pushDownStrategies;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xxl.core.spatial.KPE;

import static org.junit.Assert.assertSame;

/**
 * Unit tests for PushDownLargestBufGroupSplitDeletes
 */
@RunWith(JMock.class)
public class PushDownLargestBufGroupSplitDeletesTest {
    
    final private Mockery mockery = new JUnit4Mockery();

    private IRRDiskTree<KPE> diskTree;

    private PushDownLargestBufGroupSplitDeletes<KPE> strategy = null;

    @Before
    public void setUp() {
        //noinspection unchecked
        diskTree = mockery.mock(IRRDiskTree.class);
    }

    @Test
    public void empty() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(0));
        }});
        strategy = new PushDownLargestBufGroupSplitDeletes<>(diskTree, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(null, 0, 0, false);
        TestUtils.checkPushDownResults(0, 0, result);
    }

    @Test
    public void noSplitDeletes() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new PushDownLargestBufGroupSplitDeletes<>(diskTree, false);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, false);
        TestUtils.checkPushDownResults(5, 1, result);
        assertSame(0, strategy.getEqualLargestGroups());
    }

    @Test
    public void splitDeletes() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new PushDownLargestBufGroupSplitDeletes<>(diskTree, false);
        final IndexEntryOpGroupMap<KPE> grouping = makeSplitDeleteGrouping();

        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, false);
        TestUtils.checkPushDownResults(1, 2, result);
        assertSame(0, strategy.getEqualLargestGroups());
    }

    private static IndexEntryOpGroupMap<KPE> makeSplitDeleteGrouping() {
        final IndexEntryOpGroupMap<KPE> result = new IndexEntryOpGroupMap<>();

        final IRRTreeIndexEntry<KPE> ie1
                = TestUtils.makeIndexEntry(TestUtils.makeDescriptor(0.0, 0.0, 2.0, 2.0), 0);
        result.addEntry(ie1, TestUtils.makeOperation(0.1, 0.1, 0.2, 0.2, OperationType.DELETION));
        final UpdateTree.Entry<KPE> sharedOp = TestUtils.makeOperation(1.1, 1.1, 1.2, 1.2,
                OperationType.DELETION);
        result.addEntry(ie1, sharedOp);

        final IRRTreeIndexEntry<KPE> ie2
                = TestUtils.makeIndexEntry(TestUtils.makeDescriptor(1.0, 1.0, 3.0, 3.0), 0);
        result.addEntry(ie2, sharedOp);
        result.addEntry(ie2, TestUtils.makeOperation(2.1, 2.1, 2.2, 2.2, OperationType.DELETION));
        result.addEntry(ie2, TestUtils.makeOperation(2.2, 2.2, 2.3, 2.3, OperationType.INSERTION));

        return result;
    }
}
