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
package aau.bufferedIndexes;

import aau.bufferedIndexes.diskTrees.AbstractRRDiskTreeTest;
import aau.bufferedIndexes.diskTrees.RRDiskDataTreeTest;
import aau.bufferedIndexes.diskTrees.RRDiskUpdateTreeTest;
import aau.bufferedIndexes.diskTrees.visitors.RRDiskUpdateTreeGarbageCleanerTest;
import aau.bufferedIndexes.diskTrees.visitors.RRTreeInvariantCheckerTest;
import aau.bufferedIndexes.leafNodeModifiers.LeafNodePiggybackerTest;
import aau.bufferedIndexes.leafNodeModifiers.NullNodeModifierTest;
import aau.bufferedIndexes.objectTracers.NullObjectTracerTest;
import aau.bufferedIndexes.operationGroupMakers.DeletionsAsInsertionsGroupMakerTest;
import aau.bufferedIndexes.operationGroupMakers.InsertionsOnlyGroupMakerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@SuppressWarnings({"ClassMayBeInterface", "EmptyClass"})
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RRTreeBufferTest.class,
        StatisticalDataTest.class,
        OpLifetimeStatsTest.class,
        LifetimeStatEntryTest.class,
        OperationTypeStatTest.class,
        UpdateOperationTest.class,
        OperationTypeTest.class,
        RRTreeStatsTest.class,
        OperationGroupTest.class,
        RRTreeInvariantCheckerTest.class,
        RRDiskDataTreeTest.class,
        RRTreeLeafPiggybackingInfoTest.class,
        NullNodeModifierTest.class,
        LeafNodePiggybackerTest.class,
        AbstractRRDiskTreeTest.class,
        DeletionsAsInsertionsGroupMakerTest.class,
        RRDiskUpdateTreeTest.class,
        RRDiskUpdateTreeGarbageCleanerTest.class,
        AggregateStatsTest.class,
        HilbertPointComparatorTest.class,
        NullObjectTracerTest.class,
        InsertionsOnlyGroupMakerTest.class
})
public class AllUnitTests { }