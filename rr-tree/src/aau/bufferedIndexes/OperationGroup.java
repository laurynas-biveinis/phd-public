/*
     Copyright (C) 2010, 2012 Laurynas Biveinis

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

import xxl.core.io.Convertable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Operation group class
 * @param <E> type of entry of operations
 */
public class OperationGroup<E extends Convertable> implements Iterable<UpdateTree.Entry<E>> {

    private final List<UpdateTree.Entry<E>> ops;

    public OperationGroup() {
        ops = new ArrayList<>();
    }

    public OperationGroup(final int capacity) {
        ops = new ArrayList<>(capacity);
    }

    public OperationGroup(final Collection<UpdateTree.Entry<E>> other) {
        ops = new ArrayList<>(other);
    }

    // TODO: javadoc, test
    public void clear() {
        ops.clear();
    }

    public void add(final UpdateTree.Entry<E> op) {
        ops.add(op);
    }

    public boolean contains(final UpdateTree.Entry<E> op) {
        return ops.contains(op);
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    public Iterator<UpdateTree.Entry<E>> iterator() {
        return ops.iterator();
    }

    public int size() {
        return ops.size();
    }

    /**
     * Returns number of insertion operations in this group
     * @return number of insertion operations in this group
     */
    public int sizeInInsertions() {
        // TODO: if shows up in performance profile, eagerly calculate this on add
        int result = 0;
        for (final UpdateTree.Entry<E> e : ops) {
            if (e.isInsertion())
                result++;
        }
        return result;
    }

    public int sizeInSignificantOps(final boolean onlyInsertionsSignificant) {
        return onlyInsertionsSignificant ? sizeInInsertions() : size();
    }

    /**
     * Returns <code>true</code> if this group contains only insertions or is empty
     * @return <code>true</code> if this group contains only insertions or is empty
     */
    public boolean isInsertionOnly() {
        boolean insertionOnlyGroup = true;
        for (final UpdateOperation entry: ops) {
            if (entry.isDeletion()) {
                insertionOnlyGroup = false;
                break;
            }
        }
        return insertionOnlyGroup;
    }

    // TODO: javadoc, test
    public boolean remove(final UpdateTree.Entry<E> op) {
        return ops.remove(op);
    }
}
