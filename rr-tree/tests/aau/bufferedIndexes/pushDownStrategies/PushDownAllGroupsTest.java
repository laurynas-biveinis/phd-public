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
package aau.bufferedIndexes.pushDownStrategies;

import aau.bufferedIndexes.IndexEntryOpGroupMap;
import aau.bufferedIndexes.PushDownAndBufferGroups;
import aau.bufferedIndexes.TestUtils;
import org.junit.Before;
import org.junit.Test;
import xxl.core.spatial.KPE;

import static org.junit.Assert.assertSame;

/**
 * Unit tests for PushDownAllGroups class.
 */
public class PushDownAllGroupsTest {

    private PushDownAllGroups<KPE> strategy = null;

    @Before
    public void setUp() {
        strategy = new PushDownAllGroups<>();
    }

    @Test
    public void emptyBuffer() {
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(null, 0, 0, false);
        TestUtils.checkPushDownResults(0, 0, result);
    }

    @Test
    public void testPushDownAllGroups() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(10, 15, 20, 25, 100, 20);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, false);
        TestUtils.checkPushDownResults(0, 6, result);
        assertSame (0, strategy.getEqualLargestGroups());
    }

    @Test
    public void testAnotherLevel() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(1, 2, 3, 4, 5);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 1, 0, false);
        TestUtils.checkPushDownResults(0, 5, result);
    }

    @Test
    public void testRestart() {
        final IndexEntryOpGroupMap<KPE> grouping = TestUtils.makeGrouping(14, 21, 63, 24, 45);
        final PushDownAndBufferGroups<KPE> result = strategy.choosePushDownGroups(grouping, 0, 0, true);
        TestUtils.checkPushDownResults(0, 5, result);
    }
}
