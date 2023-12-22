/*
     Copyright (C) 2007, 2008, 2009, 2012 Laurynas Biveinis

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
import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.spatial.KPE;

import static org.junit.Assert.*;

/**
 * Unit tests for UpdateTree
 */
public class UpdateTreeTest extends TreeTester {

    private UpdateTree<KPE> tree = null;

    @Before
    public void setUp() {
        tree = new UpdateTree<>();
    }

    @Test
    public void entryEqualsAndHashCode() {
        final UpdateTree.Entry<KPE> e1 = new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION);
        assertEquals(e1, e1);
        assertEquals (e1.hashCode(), e1.hashCode());
        //noinspection ObjectEqualsNull
        assertFalse (e1.equals(null));

        final UpdateTree.Entry<KPE> e2 = new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION);
        assertFalse (e1.equals(e2));
        assertNotSame(e1.hashCode(), e2.hashCode());

        final UpdateTree.Entry<KPE> e3 = new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION);
        assertEquals(e1, e3);
        assertEquals (e1.hashCode(), e3.hashCode());
    }

    @Test
    public void initialize() {
        initTree();

        tree = new UpdateTree<>();
        try {
            tree.initialize(null, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY);
            fail ("Expected IllegalArgumentException not thrown!");
        }
        catch (IllegalArgumentException ignored) {
            assertTrue(true);
        }
    }

    private void initTree() {
        tree = new UpdateTree<>();
        tree.initialize(TestUtils.GET_DESCRIPTOR, mainMemoryContainer, MIN_CAPACITY, MAX_CAPACITY);
    }

    @Test
    public void insertWithAnnihilation() {
        initTree();

        tree.insertWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0], OperationType.INSERTION);
        assertEquals (0, tree.getNumOfIDAnnihilations());
        assertEquals (0, tree.getNumOfDIAnnihilations());
        assertTrue (true);

        // Check that deletions annihilate with insertions
        tree.removeWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0]);
        assertEquals (0, tree.getNumOfIDAnnihilations());
        assertEquals (1, tree.getNumOfDIAnnihilations());

        tree.removeWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0], OperationType.DELETION);
        assertEquals (0, tree.getNumOfIDAnnihilations());
        assertEquals (1, tree.getNumOfDIAnnihilations());

        // Check that insertions annihilate with deletions
        tree.insertWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0]);
        assertEquals (1, tree.getNumOfIDAnnihilations());
        assertEquals (1, tree.getNumOfDIAnnihilations());
    }

    @Test
    public void queryEmptyDescriptor() {
        initTree();
        queryNonexisting(tree, (Descriptor)null);
        // Add enough data so that root node overflows
        for (int i = 0; i <= 100; i++) {
            tree.insert(TestData.data[i]);
        }
        queryNonexisting(tree, (Descriptor)null);
    }

    @Test
    public void query() {
        initTree();

        queryNonexisting(tree, tree.descriptor(new UpdateTree.Entry<>(TestData.data[0],
                OperationType.INSERTION)));

        tree.insertWithAnnihilation(TestData.data[0]);
        queryNonexisting(tree, tree.descriptor(new UpdateTree.Entry<>(TestData.data[1], OperationType.INSERTION)));
        Cursor<KPE> q = tree.query(tree.descriptor(new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION)));
        checkForResults(q, TestData.data[0]);
        q = tree.query(tree.descriptor(new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION)));
        checkForResults(q, TestData.data[0]);

        tree.removeWithAnnihilation(TestData.data[1]);
        q = tree.query(tree.descriptor(new UpdateTree.Entry<>(TestData.data[1], OperationType.DELETION)));
        checkForResults(q);

        tree.insertWithAnnihilation(TestData.data[2]);
        q = tree.query(TestUtils.makeDescriptor(0, 0, 5, 5)); // MBR for TestData.data[0] and TestData.data[2]
        checkForResults(q, TestData.data[0], TestData.data[2]);

        tree.removeWithAnnihilation(TestData.data[0]);
        q = tree.query(TestUtils.makeDescriptor(0, 0, 5, 5)); // MBR for TestData.data[0] and TestData.data[2]
        checkForResults(q, TestData.data[2]);
    }

    @Test
    public void queryEntry() {
        initTree();

        final KPE datum = new KPE(1, TestUtils.makeDescriptor(0.0, 0.0, 1.0, 1.0),
                ConvertableConverter.DEFAULT_INSTANCE);
        final UpdateTree.Entry<KPE> datumI = new UpdateTree.Entry<>(datum, OperationType.INSERTION);
        final UpdateTree.Entry<KPE> datumD = new UpdateTree.Entry<>(datum, OperationType.DELETION);

        Cursor<UpdateTree.Entry<KPE>> c = tree.queryEntry(datumI);
        assertFalse (c.hasNext());
        
        c = tree.queryEntry(datumD);
        assertFalse (c.hasNext());

        tree.insertWithAnnihilation(datum);

        c = tree.queryEntry(datumI);
        assertTrue (c.hasNext());
        assertEquals (datumI, c.next());
        
        c = tree.queryEntry(datumD);
        assertFalse (c.hasNext());

        // Same position, different ID
        final KPE datum2 = new KPE(2, TestUtils.makeDescriptor(0.0, 0.0, 1.0, 1.0),
                ConvertableConverter.DEFAULT_INSTANCE);
        final UpdateTree.Entry<KPE> datum2I = new UpdateTree.Entry<>(datum2, OperationType.INSERTION);

        c = tree.queryEntry(datum2I);
        assertFalse (c.hasNext());
    }

    @Test
    public void removeWithAnnihilation() {
        initTree();
        tree.removeWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0], OperationType.DELETION);
        assertEquals (0, tree.getNumOfIDAnnihilations());
        assertEquals (0, tree.getNumOfDIAnnihilations());

        // Ensure that insertions annihilate with deletions
        initTree();
        tree.insertWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0], OperationType.INSERTION);
        assertEquals (0, tree.getNumOfIDAnnihilations());
        assertEquals (0, tree.getNumOfDIAnnihilations());

        tree.removeWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0]);
        assertEquals (0, tree.getNumOfIDAnnihilations());
        assertEquals (1, tree.getNumOfDIAnnihilations());
    }

    private void checkForEntrySequence(final KPE data, final OperationType... delInsSeq) {
        final Descriptor dataDescriptor = TestUtils.GET_DESCRIPTOR.invoke(data);
        final Cursor<UpdateTree.Entry<KPE>> queryResult = tree.queryEntryOfAnyType(dataDescriptor);
        queryResult.open();

        for (final OperationType operationType: delInsSeq) {
            final UpdateTree.Entry<KPE> entry = queryResult.next();
            assertEquals(operationType, entry.getOperationType());
        }
        assertTrue(!queryResult.hasNext());
        queryResult.close();
    }

    @Test
    public void getCurrentTreeSize() {
        initTree();
        assertEquals(0, tree.getCurrentSize());
        tree.insertWithAnnihilation(TestData.data[0]);
        assertEquals(1, tree.getCurrentSize());
        tree.removeWithAnnihilation(TestData.data[1]);
        assertEquals(2, tree.getCurrentSize());
        tree.insertWithAnnihilation(TestData.data[2]);
        assertEquals(3, tree.getCurrentSize());
        tree.removeWithAnnihilation(TestData.data[0]);
        assertEquals(2, tree.getCurrentSize());
        tree.insertWithAnnihilation(TestData.data[1]);
        assertEquals(1, tree.getCurrentSize());
    }

    @Test
    public void clear()  {
        initTree();
        tree.clear();
        assertEquals(0, tree.getCurrentSize());
        tree.insertWithAnnihilation(TestData.data[0]);
        assertEquals(1, tree.getCurrentSize());
        tree.clear();
        assertEquals(0, tree.getCurrentSize());
        assertNull(tree.rootEntry());
    }

    @Test
    public void removeExactEntry() {
        initTree();
        assertEquals(0, tree.getCurrentSize());
        tree.insertWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0], OperationType.INSERTION);
        assertEquals(1, tree.getCurrentSize());
        assertNull(tree.removeExactEntry(new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION)));
        UpdateTree.Entry<KPE> exactEntry = new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION);
        assertEquals(exactEntry, tree.removeExactEntry(exactEntry));
        assertEquals(0, tree.getCurrentSize());

        tree.removeWithAnnihilation(TestData.data[0]);
        checkForEntrySequence(TestData.data[0], OperationType.DELETION);
        assertEquals(1, tree.getCurrentSize());
        assertNull(tree.removeExactEntry(new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION)));
        exactEntry = new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION);
        assertEquals(exactEntry, tree.removeExactEntry(exactEntry));
        assertEquals(0, tree.getCurrentSize());
    }

    @Test
    public void testToString() {
        final UpdateTree.Entry<KPE> e1 = new UpdateTree.Entry<>(TestData.data[0], OperationType.INSERTION);
        assertTrue (e1.toString().contains(TestData.data[0].toString()));
        assertTrue (e1.toString().contains(OperationType.INSERTION.toString()));
    }

    @Test
    public void addDeletionIfNotExists() {
        initTree();
        UpdateTree.Entry<KPE> e = new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION);
        tree.addEntryIfNotExists(e);
        assertEquals(1, tree.getCurrentSize());
        checkForEntrySequence(TestData.data[0], OperationType.DELETION);
        tree.addEntryIfNotExists(e);
        assertEquals(1, tree.getCurrentSize());
        checkForEntrySequence(TestData.data[0], OperationType.DELETION);
    }

    @Test
    public void copyEntry() {
        UpdateTree.Entry<KPE> e = new UpdateTree.Entry<>(TestData.data[0], OperationType.DELETION);
        UpdateTree.Entry<KPE> e2 = UpdateTree.Entry.copyEntry(e);
        assertEquals (e, e2);
    }

    @Test
    public void removeAllDeletions() {
        initTree();
        tree.insertWithAnnihilation(TestData.data[0]);
        tree.removeWithAnnihilation(TestData.data[1]);
        tree.removeAllDeletions();
        assertEquals (1, tree.getCurrentSize());
        checkForEntrySequence(TestData.data[0], OperationType.INSERTION);
    }
}
