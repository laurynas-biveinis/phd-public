/*
     Copyright (C) 2012 Laurynas Biveinis

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
import xxl.core.spatial.KPE;

import static org.junit.Assert.*;

/**
 * Tests for OperationGroup class
 */
public class OperationGroupTest {

    private OperationGroup<KPE> g;

    private final KPE d = TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0);
    private final UpdateTree.Entry<KPE> opI = new UpdateTree.Entry<>(d, OperationType.INSERTION);
    private final UpdateTree.Entry<KPE> opD = new UpdateTree.Entry<>(d, OperationType.DELETION);

    @Before
    public void setUp() {
        g = new OperationGroup<>();
    }

    @Test
    public void sizeInInsertions() {
        assertEquals (0, g.sizeInInsertions());
        g.add(opI);
        assertEquals (1, g.sizeInInsertions());
        g.add(opD);
        assertEquals (1, g.sizeInInsertions());
        g = new OperationGroup<>();
        g.add(opD);
        assertEquals (0, g.sizeInInsertions());
    }

    @Test
    public void isInsertionOnly() {
        g.add(opI);
        assertTrue (g.isInsertionOnly());
        g.add(opD);
        assertFalse (g.isInsertionOnly());
        g = new OperationGroup<>();
        g.add(opD);
        assertFalse (g.isInsertionOnly());
    }
}
