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

import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.RRDiskDataTree;
import org.junit.Before;
import org.junit.Test;
import xxl.core.spatial.KPE;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RecursiveTwoWaySplitter
 */
public class RecursiveTwoWaySplitterTest extends TreeTester {

    private final RRDiskDataTree<KPE> tree = new RRDiskDataTree<>();
    private final RRTreeGroupSplitter splitter = new RecursiveTwoWaySplitter();

    private static final int MIN_CAPACITY = 10;

    @Before
    public void setUp() {
        tree.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY);
    }

    @Test
    public void groupSplit() {

        // Try to split empty node
        testSplitToSingle(0);

        // Try to split underflowing node
        testSplitToSingle(MIN_CAPACITY - 1);

        // Try to split normal nodes
        testSplitToSingle(MIN_CAPACITY);
        testSplitToSingle((MIN_CAPACITY + MAX_CAPACITY) / 2);
        testSplitToSingle(MAX_CAPACITY);

        // Split overflowing nodes
        testSplit(MAX_CAPACITY + 1);
        testSplit(2 * MAX_CAPACITY - 1);
        testSplit(2 * MAX_CAPACITY);
        testSplit(2 * MAX_CAPACITY + 1);
        testSplit(3 * MAX_CAPACITY);
        testSplit(5 * MAX_CAPACITY);
        testSplit(10 * MAX_CAPACITY + 1);
    }

    private List<KPE> makeList(final int size) {
        final List<KPE> result = new LinkedList<>();
        result.addAll(Arrays.asList(TestData.data).subList(0, size));
        return result;
    }

    private void testSplitToSingle(final int nodeSize) {
        final List<IRRTreeDiskNode<KPE>> splits = testSplit(nodeSize);

        final List<KPE> nodeContents = makeList(nodeSize);
        final IRRTreeDiskNode<KPE> n = tree.createNode(0, nodeContents);
        assertTrue((splits.size() == 1) && splits.get(0).getEntries().equals(n.getEntries()));
    }

    private List<IRRTreeDiskNode<KPE>> testSplit(final int nodeSize) {
        List<KPE> nodeContents = makeList(nodeSize);
        final IRRTreeDiskNode<KPE> toSplit = tree.createNode(0, nodeContents);
        final List<IRRTreeDiskNode<KPE>> splits = splitter.groupSplit(toSplit, tree);
        nodeContents = makeList(nodeSize);
        final IRRTreeDiskNode<KPE> n = tree.createNode(0, nodeContents);
        checkSplits(n, splits, 0);
        return splits;
    }

    private void checkSplits(final IRRTreeDiskNode<KPE> n, final Iterable<IRRTreeDiskNode<KPE>> splits,
                             final int nodeLevel) {
        int sumOfSplitEntries = 0;
        for (final IRRTreeDiskNode<KPE> s : splits) {
            sumOfSplitEntries += s.number();
            assertTrue("Splitted nodes should not be overflowing", !s.overflows());
            assertTrue("Every entry in the splitted node should be found in the original node", n.getEntries().containsAll(s.getEntries()));
            assertEquals("Splitted node should be on the same level as the original node", nodeLevel, s.level());
        }

        for (final Object entry : n.getEntries()) {
            boolean found = false;
            for (final IRRTreeDiskNode<KPE> s : splits) {
                if (s.getEntries().contains(entry)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Every entry in the original node should be found in one of the splitted nodes", found);
        }

        assertEquals("Sum of entries in all splitted nodes should be equal to the number of entries in original node",
                n.number(), sumOfSplitEntries);
    }
}
