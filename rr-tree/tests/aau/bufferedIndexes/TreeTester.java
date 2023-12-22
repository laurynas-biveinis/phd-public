/*
     Copyright (C) 2007, 2008, 2011, 2012 Laurynas Biveinis

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

import org.junit.After;
import org.junit.Before;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.Tree;
import xxl.core.spatial.KPE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * A collection of helper routines and data for testing tree data structures.
 */
public class TreeTester {

    Container mainMemoryContainer = null;

    static final int MIN_CAPACITY = 40;
    static final int MAX_CAPACITY = 100;

    @Before
    public void setUpContainer() {
        mainMemoryContainer = new MapContainer(false);
    }

    @After
    public void tearDownContainer() {
        mainMemoryContainer.close();
    }

    static void querySingleExisting(final Tree tree, final KPE o) {
        //noinspection unchecked
        final Cursor<KPE> queryResult = tree.query(o);
        checkForResults(queryResult, o);
    }

    static void queryNonexisting(final Tree tree, final KPE o) {
        //noinspection unchecked
        final Cursor<KPE> queryResult = tree.query(o);
        checkForResults(queryResult);
    }

    static void queryNonexisting(final Tree tree, final Descriptor o) {
        //noinspection unchecked
        final Cursor<KPE> queryResult = tree.query(o);
        checkForResults(queryResult);
    }

    static void checkedInsert(final Tree tree, final KPE o) {
        tree.insert(o);
        querySingleExisting(tree, o);
    }

    static void checkedDelete(final Tree tree, final KPE o) {
        assertEquals(o, tree.remove(o));
        queryNonexisting(tree, o);
    }

    static void checkedMissingDelete(final Tree tree, final KPE o) {
        assertNull("Delete operation should not find object in the tree (is it buffer?)", tree.remove(o));
        queryNonexisting(tree, o);        
    }

    static void checkForResults(final Cursor<KPE> queryResult, final KPE... results) {
        final Collection<KPE> remainingResults = new HashSet<>(Arrays.asList(results));
        queryResult.open();
        while (queryResult.hasNext()) {
            final boolean found = remainingResults.remove(queryResult.next());
            assertTrue (found);
        }
        try {
            queryResult.next();
            fail("Query returns more elements than it should");
        }
        catch (NoSuchElementException ignored) { }
        queryResult.close();
    }
}
