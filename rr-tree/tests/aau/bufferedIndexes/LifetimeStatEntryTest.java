/*
     Copyright (C) 2009 Laurynas Biveinis

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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the LifetimeStatEntry class
 */
public class LifetimeStatEntryTest {

    @Test
    public void returnToBuffer() {
        final LifetimeStatEntry entry = new LifetimeStatEntry();
        assertEquals (0, entry.getTimesReturnedToBuffer().size());
        entry.returnToBuffer(0);
        final List<Integer> result = entry.getTimesReturnedToBuffer();
        assertEquals (1, result.size());
        assertEquals (1, result.get(0).intValue());
        
        entry.returnToBuffer(5);
        checkList(entry, 1);

        entry.returnToBuffer(0);
        checkList(entry, 2);
    }

    @Test
    public void add() {
        final LifetimeStatEntry entry = new LifetimeStatEntry();
        entry.returnToBuffer(1);
        entry.add(entry);
        List<Integer> result = entry.getTimesReturnedToBuffer();
        assertEquals (2, result.size());
        assertEquals (2, result.get(1).intValue());

        final LifetimeStatEntry e2 = new LifetimeStatEntry();
        e2.returnToBuffer(10);
        e2.returnToBuffer(10);
        entry.add(e2);
        result = entry.getTimesReturnedToBuffer();
        assertEquals (11, result.size());
        assertEquals (2, result.get(10).intValue());

        final LifetimeStatEntry e3 = new LifetimeStatEntry();
        e3.returnToBuffer(2);
        entry.add(e3);
        result = entry.getTimesReturnedToBuffer();
        assertEquals (11, result.size());
        assertEquals (1, result.get(2).intValue());
    }

    private static void checkList(final LifetimeStatEntry entry, final int valueAtZero) {
        final List<Integer> result = entry.getTimesReturnedToBuffer();
        assertEquals (6, result.size());
        assertEquals (valueAtZero, result.get(0).intValue());
        assertEquals (1, result.get(5).intValue());
        for (int i = 1; i < 5; i++)
            assertEquals (0, result.get(i).intValue());
    }
}
