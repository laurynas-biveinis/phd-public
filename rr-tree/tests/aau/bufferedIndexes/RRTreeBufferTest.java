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

import org.junit.Before;
import org.junit.Test;
import xxl.core.collections.containers.EmptyContainer;
import xxl.core.functions.Constant;
import xxl.core.indexStructures.Tree;
import xxl.core.spatial.KPE;

import static org.junit.Assert.*;

public class RRTreeBufferTest extends TreeTester {
    private RRTreeBuffer<KPE> rrTreeBuffer = null;
    private EmptyContainer emptyContainer = null;

    @Before
    public void setUp() {
        emptyContainer = new EmptyContainer();
        rrTreeBuffer = new RRTreeBuffer<>();
    }

    private void addNonfillingInsertions(int numOps) {
        for (int i = 0; i < numOps; i++) {
            rrTreeBuffer.insertWithAnnihilation(TestData.data[i]);
            assertTrue(!rrTreeBuffer.isFull());
        }
    }

    private void addNonfillingDeletions(int start, int numOps) {
        for (int i = 0; i < numOps; i++) {
            rrTreeBuffer.removeWithAnnihilation(TestData.data[i + start]);
            assertTrue(!rrTreeBuffer.isFull());
        }
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void initialize() {
        rrTreeBuffer.initialize(new Constant(null), emptyContainer, 0, 0, 0);
        assertTrue(true);

        rrTreeBuffer = new RRTreeBuffer<>();

        try {
            rrTreeBuffer.initialize(null, emptyContainer, 0, 0, 0);
            fail ("Expected IllegalArgumentException not thrown!");
        }
        catch (IllegalArgumentException ignored) {
            assertTrue(true);
        }

        // Regression check
        rrTreeBuffer = new RRTreeBuffer<>();
        rrTreeBuffer.initialize(new Constant(null), emptyContainer, 0, 0, 0);
        assertTrue(rrTreeBuffer.isFull());

        rrTreeBuffer = new RRTreeBuffer<>();
        rrTreeBuffer.initialize(new Constant(null), emptyContainer, 0, 0, 1);
        assertTrue(!rrTreeBuffer.isFull());

    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void isFull() {
        rrTreeBuffer.initialize(new Constant(null), emptyContainer, 50, 100, 0);
        assertTrue(rrTreeBuffer.isFull());

        rrTreeBuffer = new RRTreeBuffer<>();
        rrTreeBuffer.initialize(new Constant(null), emptyContainer, 50, 100, 1);
        assertTrue(!rrTreeBuffer.isFull());
    }

    @Test
    public void insert() {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY, 101);

        // 100 here because it's a test for error with DoublePointRectangle on the first split too.
        addNonfillingInsertions(100);
        int i = 100;
        rrTreeBuffer.insertWithAnnihilation(TestData.data[i]);
        assertTrue(rrTreeBuffer.isFull());
        try {
            rrTreeBuffer.insertWithAnnihilation(TestData.data[i + 1]);
            fail("Expected exception not thrown!");
        }
        catch (IllegalStateException ignored) {
            assertTrue(true);
        }
    }

    @Test
    public void remove() {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY, 101);
        addNonfillingDeletions(0, 100);
        int i = 100;
        rrTreeBuffer.removeWithAnnihilation(TestData.data[i]);
        assertTrue(rrTreeBuffer.isFull());
        try {
            rrTreeBuffer.removeWithAnnihilation(TestData.data[i + 1]);
            fail("Expected exception not thrown!");
        }
        catch (IllegalStateException ignored) {
            assertTrue(true);
        }
    }

    @Test
    public void flattenTree() {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY, 101);

        final int insNum = 50;
        addNonfillingInsertions(insNum);
        final int delNum = 50;
        addNonfillingDeletions(insNum, delNum);

        final OperationGroup<KPE> flatBuffer = rrTreeBuffer.flatten();
        assertEquals(insNum + delNum, flatBuffer.size());

        // 0(n^2) but oh well
        int i;
        for (i = 0; i < insNum; i++) {
            final UpdateTree.Entry<KPE> toCheck = new UpdateTree.Entry<>(TestData.data[i], OperationType.INSERTION);
            assertTrue(flatBuffer.contains(toCheck));
        }
        for (i = 0; i < delNum; i++) {
            assertTrue(flatBuffer.contains(new UpdateTree.Entry<>(TestData.data[i + insNum], OperationType.DELETION)));
        }
    }

    @Test
    public void nodeUnderflow () {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, 10, 50, 101);
        addNonfillingInsertions(100);
        addNonfillingDeletions(0, 100);
        assertEquals (0, rrTreeBuffer.getCurrentSize());
    }

    @Test
    public void createNode() {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, 10, 50, 101);
        Tree.Node node = rrTreeBuffer.createNode(1);
        assertEquals (1, node.level());
    }

    @Test
    public void clear() {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY, 200);
        addNonfillingInsertions(150);
        assertEquals (150, rrTreeBuffer.getCurrentSize());
        rrTreeBuffer.clear();
        assertEquals (0, rrTreeBuffer.getCurrentSize());
    }

    @Test
    public void removeGroup() {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY, 200);
        addNonfillingInsertions(100);
        addNonfillingDeletions(100, 50);

        OperationGroup<KPE> g = new OperationGroup<>();
        for (int i = 0; i < 70; i++) {
            g.add(new UpdateTree.Entry<>(TestData.data[i], OperationType.INSERTION));
        }
        for (int i = 100; i < 110; i++) {
            g.add(new UpdateTree.Entry<>(TestData.data[i], OperationType.DELETION));
        }

        rrTreeBuffer.removeGroup(g);
        assertEquals(70, rrTreeBuffer.getCurrentSize());
    }

    @Test
    public void addToBuffer() {
        rrTreeBuffer.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY, 200);
        addNonfillingDeletions(0, 50);

        final UpdateTree.Entry<KPE> iop = new UpdateTree.Entry<>(TestData.data[60], OperationType.INSERTION);
        final UpdateTree.Entry<KPE> dop = new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION);
        final UpdateTree.Entry<KPE> dop2 = new UpdateTree.Entry<>(TestData.data[100], OperationType.DELETION);

        rrTreeBuffer.addEntryIfNotExists(iop);
        rrTreeBuffer.addEntryIfNotExists(dop);
        rrTreeBuffer.addEntryIfNotExists(dop2);

        assertEquals(52, rrTreeBuffer.getCurrentSize());
    }
}
