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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for RRTreeLeafPiggybackingInfo class
 */
public class RRTreeLeafPiggybackingInfoTest {

    private RRTreeLeafPiggybackingInfo i;

    @Before
    public void setUp() {
        i = new RRTreeLeafPiggybackingInfo();
    }

    @Test
    public void deletions() {
        assertEquals (0, i.getNumOfSizeDecreasingOps());
        i.addPotentialSizeDecreasingOp();
        assertEquals (1, i.getNumOfSizeDecreasingOps());
        i.addPotentialSizeDecreasingOp();
        assertEquals (2, i.getNumOfSizeDecreasingOps());
    }

    @Test
    public void insertions() {
        assertEquals (0, i.getNumOfSizeIncreasingOps());
        i.addPotentialSizeIncreasingOp();
        assertEquals (1, i.getNumOfSizeIncreasingOps());
        i.addPotentialSizeIncreasingOp();
        assertEquals (2, i.getNumOfSizeIncreasingOps());
    }

    @Test
    public void limitDeletions() {
        i.addPotentialSizeDecreasingOp();
        i.addPotentialSizeDecreasingOp();
        i.addPotentialSizeDecreasingOp();
        i.limitSizeDecreasingOps(2);
        assertEquals (1, i.getNumOfSizeDecreasingOps());
        assertEquals (2, i.getUnpiggybackableSizeDecreasingOps());
    }

    @Test
    public void limitInsertions() {
        i.addPotentialSizeIncreasingOp();
        i.addPotentialSizeIncreasingOp();
        i.addPotentialSizeIncreasingOp();
        i.limitSizeIncreasingOps(2);
        assertEquals (1, i.getNumOfSizeIncreasingOps());
        assertEquals (2, i.getUnpiggybackableSizeIncreasingOps());
    }
    
    @Test
    public void nodeSizeChange() {
        i.addPotentialSizeIncreasingOp();
        i.addPotentialSizeDecreasingOp();
        i.addPotentialSizeIncreasingOp();
        assertEquals(1, i.nodeSizeChange());
        i.addPotentialSizeDecreasingOp();
        assertEquals(0, i.nodeSizeChange());
        i.addPotentialSizeDecreasingOp();
        assertEquals(-1, i.nodeSizeChange());
    }

    @Test
    public void isNodeChanged() {
        assertFalse (i.isNodeChanged());
        i.addPotentialSizeIncreasingOp();
        i.addPotentialSizeDecreasingOp();
        i.addPotentialSizeIncreasingOp();
        assertTrue (i.isNodeChanged());
        i.limitSizeIncreasingOps(2);
        i.limitSizeDecreasingOps(1);
        assertFalse(i.isNodeChanged());
    }
}
