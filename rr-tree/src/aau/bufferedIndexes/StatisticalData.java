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

import java.util.*;

/**
 * Two-dimensional statistical data counter.
 */
public final class StatisticalData {

    private final SortedMap<Integer, Integer> data = new TreeMap<>();

    /**
     * Adds (merges) other statistical data object to this one.  The resulting object will have keys from both
     * of the objects, with their values summed.
     * @param toAdd other statistical data object to add to
     */
    public void add(final StatisticalData toAdd) {
        for (final Map.Entry<Integer, Integer> counter : toAdd.entrySet()) {
            update(counter.getKey(), counter.getValue());
        }
    }

    /**
     * Returns <code>true</code>, if there is a mapping from the specified key.
     * @param key key to check for
     * @return <code>true</code>, if data for such key exists
     */
    public boolean containsKey(final int key) {
        return data.containsKey(key);
    }

    /**
     * Returns the read-only set of all statistical data mappings.
     * @return the read-only set of mappings between keys and their values.
     */
    public Set<Map.Entry<Integer, Integer>> entrySet() {
        return Collections.unmodifiableSet(data.entrySet());
    }

    /**
     * Compares with specified object for equality.
     * @param other the object to compare with.
     * @return <code>true</code>is of StatisticalData class and has exactly the same statistical data,
     * <code>false</code>otherwise.
     */
    public boolean equals (final Object other) {
        return (other instanceof StatisticalData) && ((StatisticalData)other).data.equals(data);
    }

    /**
     * Returns the counter value for the specified key. Throws IllegalArgumentException, if such key does not exist
     * in the data.
     * @param key the statistics key
     * @return the counter value.
     */
    public int get(final int key) {
        final Object result = data.get(key);
        if (result == null)
            throw new IllegalArgumentException("Non-existent key");
        return (Integer)result;
    }

    /**
     * Returns the hash code for this object.
     * @return the hash code
     */
    public int hashCode() {
        return data.hashCode();
    }

    /**
     * Returns <code>true</code> if there is no data in this object.  
     * @return <code>true</code> if there is no data in this object.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Resets the counter.
     */
    public void reset() {
        data.clear();
    }

    /**
     * Returns the number of mappings in the object
     * @return number of mappings.
     */
    public int size() {
        return data.size();
    }

    /**
     * Adjusts the specified key of the statistics by the specified delta.
     * @param key the statistics key to update
     * @param delta change in counter of the key
     */
    public void update(final int key, final int delta) {
        final int oldValue = data.containsKey(key) ? data.get(key) : 0;
        data.put(key, oldValue + delta);
    }
}
