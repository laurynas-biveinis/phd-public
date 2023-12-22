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
package aau.bufferedIndexes;

import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import aau.bufferedIndexes.diskTrees.RRDiskDataTree;
import org.junit.Test;
import xxl.core.spatial.KPE;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for IndexEntryOpGroupMap
 */
public class IndexEntryOpGroupMapTest {

    @Test
    public void testCopyFromEmpty() {
        final IndexEntryOpGroupMap<KPE> to = new IndexEntryOpGroupMap<>();
        to.copy(null);
        assertEquals (0, to.size());
    }

    @Test
    public void flattenOnlyInsertions() {
        IRRDiskTree<KPE> indexEntryMaker = new RRDiskDataTree<>();
        final IndexEntryOpGroupMap<KPE> map = new IndexEntryOpGroupMap<>();
        IRRTreeIndexEntry<KPE> e1 = indexEntryMaker.createIndexEntry(0);
        e1.initialize(1);
        IRRTreeIndexEntry<KPE> e2 = indexEntryMaker.createIndexEntry(0);
        e2.initialize(2);
        IRRTreeIndexEntry<KPE> e3 = indexEntryMaker.createIndexEntry(0);
        e3.initialize(3);
        UpdateTree.Entry<KPE> op1 = TestUtils.makeOperation(1.0, 1.0, 2.0, 2.0, OperationType.INSERTION);
        UpdateTree.Entry<KPE> op2 = TestUtils.makeOperation(2.0, 2.0, 3.0, 3.0, OperationType.DELETION);
        UpdateTree.Entry<KPE> op3 = TestUtils.makeOperation(3.0, 3.0, 4.0, 4.0, OperationType.DELETION);
        UpdateTree.Entry<KPE> op4 = TestUtils.makeOperation(4.0, 4.0, 5.0, 5.0, OperationType.INSERTION);
        UpdateTree.Entry<KPE> op5 = TestUtils.makeOperation(5.0, 5.0, 6.0, 6.0, OperationType.INSERTION);
        UpdateTree.Entry<KPE> op6 = TestUtils.makeOperation(6.0, 6.0, 7.0, 7.0, OperationType.DELETION);
        UpdateTree.Entry<KPE> op7 = TestUtils.makeOperation(7.0, 7.0, 8.0, 8.0, OperationType.DELETION);
        map.addEntry(e1, op1);
        map.addEntry(e1, op2);
        map.addEntry(e1, op3);
        map.addEntry(e2, op4);
        map.addEntry(e2, op5);
        map.addEntry(e3, op6);
        map.addEntry(e3, op7);

        OperationGroup<KPE> g = map.flattenOnlyInsertions();
        assertEquals (3, g.size());
        assertTrue (g.contains(op1));
        assertTrue (g.contains(op4));
        assertTrue (g.contains(op5));
    }
}
