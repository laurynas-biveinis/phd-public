/*
     Copyright (C) 2009, 2010, 2012 Laurynas Biveinis

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

import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import xxl.core.io.Convertable;

import java.util.*;

/**
 * Represents a mapping between node index entries and associated operations
 */
public class IndexEntryOpGroupMap<E extends Convertable>
        implements Iterable<Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>>> {

    // TODO: javadoc, replace null with something better
    public final IRRTreeIndexEntry<E> ORPHAN_GROUP_KEY = null;
    
    /**
     * The map itself
     */
    private final Map<IRRTreeIndexEntry<E>, OperationGroup<E>> map;

    /**
     * Creates a new empty mapping
     */
    public IndexEntryOpGroupMap() {
        map = new HashMap<>();
    }

    /**
     * Copies an existing mapping
     * @param other a mapping to copy
     */
    public IndexEntryOpGroupMap(final IndexEntryOpGroupMap<E> other) {
        map = new HashMap<>(other.map);
    }

    /**
     * Checks for a map for the specified index entry and returns it if it exists, otherwise creates a new map and
     * puts the specified operation in it. 
     * @param indexEntry the index entry to map from
     * @param op an operation to add if there is no index entry found, otherwise ignored
     * @return a new list of mapped operations or <code>null</code> if such list already existed
     */
    private OperationGroup<E> createOrGetMap(final IRRTreeIndexEntry<E> indexEntry,
                                             final UpdateTree.Entry<E> op) {
        OperationGroup<E> list = map.get(indexEntry);
        if (list != null)
            return list;
        list = new OperationGroup<>(); // TODO: list create and add
        list.add(op);
        map.put(indexEntry, list);
        return null;
    }

    /**
     * Adds a mapping from an index entry to an operation
     * @param indexEntry the index entry
     * @param op the operation
     */
    public void addEntry(final IRRTreeIndexEntry<E> indexEntry, final UpdateTree.Entry<E> op) {
        final OperationGroup<E> list = createOrGetMap(indexEntry, op);
        if (list != null)
            list.add(op);
    }

    /**
     * Adds a mapping from an index entry to an operation if it was not already in the mapping
     * @param indexEntry the index entry
     * @param op the operation
     */
    public void addIfNotExists(final IRRTreeIndexEntry<E> indexEntry, final UpdateTree.Entry<E> op) {
        final OperationGroup<E> list = createOrGetMap(indexEntry, op);
        if (list != null && !list.contains(op))
            list.add(op);  // TODO: list add if not exists
    }

    /**
     * Returns a collection of all operations in the mapping, ignoring the index entries.
     * @return collection of all operations in the mapping
     */
    public OperationGroup<E> flatten() {
        int totalSize = 0;
        int skippedDeletions = 0;
        final Collection<UpdateTree.Entry<E>> result = new HashSet<>();
        for (final OperationGroup<E> group : map.values()) {
            totalSize += group.size();
            for (final UpdateTree.Entry<E> entry : group) {
                if (entry.isDeletion()) {
                    if (result.contains(entry)) {
                        skippedDeletions++;
                        continue;
                    }
                }
                else
                    assert !result.contains(entry);
                result.add(entry);
            }
        }
        assert result.size() == totalSize - skippedDeletions;
        return new OperationGroup<>(result);
    }

    /**
     * Return a collection of all insertions in this mapping
     * @return an operation group with all insertions in this mapping
     */
    public OperationGroup<E> flattenOnlyInsertions() {
        final OperationGroup<E> result = new OperationGroup<>();
        for (final OperationGroup<E> group : map.values()) {
            for (final UpdateTree.Entry<E> entry : group) {
                if (entry.isInsertion()) {
                    assert !result.contains(entry);
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public int numOfDistinctOps() {
        return flatten().size();
    }

    /**
     * Removes a mapping from index entry to operations from one map and adds it to this one
     * @param otherMap    the map to remove from
     * @param entryKey    the key to move
     * @param entryValue  the value to move   
     */
    public void moveGroupFrom(final IndexEntryOpGroupMap<E> otherMap,
                              final IRRTreeIndexEntry<E> entryKey,
                              final OperationGroup<E> entryValue) {
        map.put(entryKey, entryValue);
        otherMap.remove(entryKey);
    }

    /**
     * Clears the current mapping and adds single mapping to it
     * @param entry mapping to add
     */
    public void clearAndSet(final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> entry) {
        map.clear();
        map.put(entry.getKey(), entry.getValue());
    }

    /**
     * Copies all mappings from another map.  Does not remove already existing mappings.
     * @param other the map to copy from
     */
    public void copy(final IndexEntryOpGroupMap<E> other) {
        if (other != null)
            map.putAll(other.map);
    }

    /**
     * Returns an iterator of all mappings
     * @return mapping iterator
     */
    public Iterator<Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>>> iterator() {
        return map.entrySet().iterator();
    }

    /**
     * Tests the mapping for emptiness
     * @return emptiness flag
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    // TODO: not a very nice thing to return a collection
    /**
     * Returns collection of lists of all operations, ignoring the index entries
     * @return collection of lists of operations
     */
    public Iterable<OperationGroup<E>> operations() {
        return map.values();
    }

    /**
     * Adds a mapping from index entry to the specified operations.  Overrides any old mapping from the index entry.
     * @param entry a mapping to add
     */
    public void put(final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> entry) {
        map.put(entry.getKey(), entry.getValue());
    }

    /**
     * Removes a mapping from index entry to the specified operations.
     * @param entry a mapping to remove
     */
    public void remove(final Map.Entry<IRRTreeIndexEntry<E>, OperationGroup<E>> entry) {
        map.remove(entry.getKey());
    }

    // TODO: javadoc, tests, merge these two?
    public void remove (final IRRTreeIndexEntry<E> key) {
        map.remove(key);
    }

    /**
     * Returns size of this map (number of different index entries)
     * @return size of this map
     */
    public int size() {
        return map.size();
    }

    // TODO: javadoc, test
    public OperationGroup<E> get (final IRRTreeIndexEntry<E> key) {
        return map.get(key);
    }
}
