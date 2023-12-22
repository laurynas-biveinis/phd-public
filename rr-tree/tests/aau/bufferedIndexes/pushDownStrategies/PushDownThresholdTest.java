/*
     Copyright (C) 2009, 2010, 2012 Laurynas Biveinis

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
import org.junit.Test;
import xxl.core.spatial.KPE;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"MagicNumber"})
public class PushDownThresholdTest {

    private PushDownThreshold<KPE> strategy = null;

    @Test
    public void trivial() {
        strategy = new PushDownThreshold<>(0, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(null, 0, 0, false);
        TestUtils.checkPushDownResults(0, 0, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);
    }

    @Test
    public void allAboveThreshold() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        strategy = new PushDownThreshold<>(10, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, false);
        TestUtils.checkPushDownResults(0, 6, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);
    }

    @Test
    public void testSomeAboveThreshold() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        strategy = new PushDownThreshold<>(25, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, false);
        TestUtils.checkPushDownResults(4, 2, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);
        assertSame (0, strategy.getEqualLargestGroups());
    }

    @Test
    public void testNoneAboveThreshold() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        strategy = new PushDownThreshold<>(200, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, false);
        TestUtils.checkPushDownResults(5, 1, result);
        TestUtils.checkPushDownThresholds(0, 1, strategy);
    }

    @Test
    public void testCalledFromRestart() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        strategy = new PushDownThreshold<>(20, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, true);
        TestUtils.checkPushDownResults(2, 4, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);
    }

    @Test
    public void testAnotherLevel() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        strategy = new PushDownThreshold<>(20, false);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, false);
        TestUtils.checkPushDownResults(2, 4, result);
        TestUtils.checkPushDownThresholds(1, 0, strategy);
    }

    @Test
    public void testWillEmptyLargePartOfBuffer() {
        strategy = new PushDownThreshold<>(1, false);
        assertTrue (strategy.willEmptyBigPartOfBuffer());
    }

    @Test
    public void testZeroSizeGroupsByInsertions() {
        strategy = new PushDownThreshold<>(10, true);
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeDeleteGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, false);
        TestUtils.checkPushDownResults(5, 1, result);
        TestUtils.checkPushDownThresholds(0, 1, strategy);
    }
}
