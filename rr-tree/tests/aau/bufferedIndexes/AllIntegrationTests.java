/*
     Copyright (C) 2010 Laurynas Biveinis

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

import aau.bufferedIndexes.diskTrees.CachingStrategyTest;
import aau.bufferedIndexes.operationGroupMakers.TrivialOperationGroupMakerTest;
import aau.bufferedIndexes.pushDownStrategies.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@SuppressWarnings({"ClassMayBeInterface", "EmptyClass"})
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TrivialOperationGroupMakerTest.class,
        RecursiveTwoWaySplitterTest.class,
        PushDownLargestBufGroupSplitDeletesTest.class,
        PushDownThresholdBelowRootTest.class,
        PushDownDivideByConstantBelowRootTest.class,
        PushDownDivideByFanoutBelowRootTest.class,
        PushDownThresholdTimesFanoutTest.class,
        CachingStrategyTest.class,
        IndexEntryOpGroupMapTest.class,
        RRTreeIntegrationTest.class        
})
public class AllIntegrationTests { }
