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

import aau.bufferedIndexes.IndexEntryOpGroupMap;
import aau.bufferedIndexes.PushDownAndBufferGroups;
import aau.bufferedIndexes.TestUtils;
import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xxl.core.spatial.KPE;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for RootLevelThreshold class
 */
@RunWith(JMock.class)
public class RootLevelThresholdTest {

    final private Mockery mockery = new JUnit4Mockery();

    private IRRDiskTree<KPE> diskTree;

    @Before
    public void setUp() {
        //noinspection unchecked
        diskTree = mockery.mock(IRRDiskTree.class);
    }

    private RootLevelThreshold<KPE> strategy = null;

    @Test
    public void empty() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(0));
        }});
        strategy = new RootLevelThreshold<>(diskTree, 10, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(null, 0, 0, false);
        TestUtils.checkPushDownResults(0, 0, result);
    }

    @Test
    public void rootAllAboveThreshold() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new RootLevelThreshold<>(diskTree, 10, false);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, false);
        TestUtils.checkPushDownResults(0, 6, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);
    }

    @Test
    public void rootSomeAboveThreshold() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new RootLevelThreshold<>(diskTree, 25, false);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, false);
        TestUtils.checkPushDownResults(4, 2, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);
        assertSame(0, strategy.getEqualLargestGroups());
    }

    @Test
    public void rootNoneAboveThreshold() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new RootLevelThreshold<>(diskTree, 250, false);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, false);
        TestUtils.checkPushDownResults(5, 1, result);
        TestUtils.checkPushDownThresholds(0, 1, strategy);
    }

    @Test
    public void nonRootLevel() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new RootLevelThreshold<>(diskTree, 25, false);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, false);
        TestUtils.checkPushDownResults(0, 6, result);
        TestUtils.checkPushDownThresholds(0, 0, strategy);
    }

    @Test
    public void restartGroupUpdateRoot() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new RootLevelThreshold<>(diskTree, 25, false);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, true);
        TestUtils.checkPushDownResults(4, 2, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);        
    }

    @Test
    public void restartGroupUpdateNonRoot() {
        mockery.checking(new Expectations(){{
            allowing(diskTree).height(); will(returnValue(2));
        }});
        strategy = new RootLevelThreshold<>(diskTree, 25, false);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, true);
        TestUtils.checkPushDownResults(0, 6, result);
        TestUtils.checkPushDownThresholds(0, 0, strategy);
    }

    @Test
    public void willEmptyBigPartOfBuffer() {
        strategy = new RootLevelThreshold<>(diskTree, 10, false);
        assertFalse(strategy.willEmptyBigPartOfBuffer());
    }
}
