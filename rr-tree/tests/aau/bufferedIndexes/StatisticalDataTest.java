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

/**
 * JUnit testsuite for the StatisticalData class.
 */

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public final class StatisticalDataTest {

    private static void fillIn(final StatisticalData data) {
        data.update(1, 5);
        data.update(1, 5);
        data.update(5, 10);
        data.update(5, -4);
        data.update(3, 7);
    }

    @Test
    public void add() {
        final StatisticalData data = new StatisticalData();
        fillIn(data);
        final StatisticalData dataToAdd = new StatisticalData();
        dataToAdd.update(1, 2);
        dataToAdd.update(3, -4);
        dataToAdd.update(7, 2);
        data.add(dataToAdd);
        assertEquals (4, data.size());
        assertEquals (12, data.get(1));
        assertEquals (3, data.get(3));
        assertEquals (6, data.get(5));
        assertEquals (2, data.get(7));
    }

    @Test
    public void containsKey() {
        final StatisticalData data = new StatisticalData();
        assertFalse (data.containsKey(1));
        fillIn(data);
        assertTrue (data.containsKey(1));
        assertTrue (data.containsKey(5));
        assertTrue (data.containsKey(3));
    }

    @Test
    public void statisticalData() {
        final StatisticalData data = new StatisticalData();
        assertEquals (0, data.size());
    }

    @Test
    public void entrySet() {
        final StatisticalData data = new StatisticalData();
        fillIn(data);
        final Set<Map.Entry<Integer, Integer>> entrySet = data.entrySet();
        assertEquals (3, entrySet.size());
        final Iterator<Map.Entry<Integer, Integer>> entrySetI = entrySet.iterator();
        Map.Entry<Integer, Integer> set = entrySetI.next();
        assertEquals (1, (int)set.getKey());
        assertEquals (10, (int)set.getValue());
        set = entrySetI.next();
        assertEquals (3, (int)set.getKey());
        assertEquals (7, (int)set.getValue());
        set = entrySetI.next();
        assertEquals (5, (int)set.getKey());
        assertEquals (6, (int)set.getValue());
        assertFalse (entrySetI.hasNext());
        try {
            entrySet.clear();
            fail ("Returned entry set should be read-only");
        }
        catch (UnsupportedOperationException ignored) { }
    }

    @Test
    public void testEquals() {
        final StatisticalData data = new StatisticalData();
        fillIn(data);
        final StatisticalData equalData = new StatisticalData();
        fillIn(equalData);
        assertEquals (data, equalData);

        final StatisticalData inequalData = new StatisticalData();
        inequalData.update(2, 5);
        assertFalse (data.equals(inequalData));

        final SortedMap<Integer, Integer> otherObj = new TreeMap<>();
        otherObj.put(2, 5);
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse (inequalData.equals(otherObj)); 
    }

    @Test
    public void get() {
        final StatisticalData data = new StatisticalData();
        data.update(1, 2);
        assertEquals (2, data.get(1));
        try {
            data.get(2);
            fail ("Should throw IllegalArgumentException!");
        }
        catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testHashCode() {
        final StatisticalData data = new StatisticalData();
        fillIn(data);
        final StatisticalData equalData = new StatisticalData();
        fillIn(equalData);
        assertEquals (data.hashCode(), equalData.hashCode());
        final StatisticalData inequalData = new StatisticalData();
        inequalData.update(2, 5);
        assertNotSame(data.hashCode(), inequalData.hashCode());
    }

    @Test
    public void isEmpty() {
        final StatisticalData data = new StatisticalData();
        assertTrue (data.isEmpty());
        fillIn(data);
        assertFalse (data.isEmpty());
        data.reset();
        assertTrue (data.isEmpty());
    }

    @Test
    public void reset() {
        final StatisticalData data = new StatisticalData();
        data.update(1, 5);
        assertEquals (1, data.size());
        data.reset();
        assertEquals (0, data.size());
        data.update(2, 4);
        assertEquals (1, data.size());
    }

    @Test
    public void update() {
        final StatisticalData data = new StatisticalData();
        fillIn(data);
        assertEquals (3, data.size());
        assertEquals (10, data.get(1));
        assertEquals (7, data.get(3));
        assertEquals (6, data.get(5));
    }
}
