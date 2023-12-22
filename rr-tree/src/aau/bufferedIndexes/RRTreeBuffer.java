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

import xxl.core.collections.containers.Container;
import xxl.core.cursors.Cursor;
import xxl.core.functions.Constant;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.Tree;
import xxl.core.io.Convertable;
import xxl.core.predicates.Predicate;

import java.util.ArrayList;

/**
 * A buffer for the buffered R-tree.  Implemented as R-tree internally.
 */
public class RRTreeBuffer<E extends Convertable> extends UpdateTree<E> implements IRRTreeBuffer<E> {

    /**
     * Maximum size of the buffer in the terms of number of objects.
     */
    private int maxSize = -1;

    private int maxNodeCapacity;

    private Container bufferContainer = null;

    /**
     * Initialize the RR-Tree buffer
     *
     * @param getDescriptor the new {@link xxl.core.indexStructures.Tree#getDescriptor}
     * @param container     the new {@link xxl.core.indexStructures.Tree#determineContainer}
     * @param minCapacity   is used to define {@link xxl.core.indexStructures.Tree#underflows}, {@link xxl.core.indexStructures.Tree#getSplitMinRatio}
     *                      and {@link xxl.core.indexStructures.Tree#getSplitMaxRatio}.
     * @param maxCapacity   is used to define {@link xxl.core.indexStructures.Tree#overflows}
     * @param maxBufferSize the maximum size of the buffer in the number of objects
     */
    public void initialize(final Function<E, Descriptor> getDescriptor, final Container container,
                           final int minCapacity, final int maxCapacity, final int maxBufferSize) {
        bufferContainer = container;
        maxNodeCapacity = maxCapacity;
        super.initialize(
                null,
                null,
                getDescriptor,
                new Constant<>(container),
                new Constant<>(container),
                new Predicate() {
                    public boolean invoke(final Object node) {
                        return ((Tree.Node) node).number() < minCapacity;
                    }
                },
                new Predicate() {
                    public boolean invoke(final Object node) {
                        return ((Tree.Node) node).number() > maxCapacity;
                    }
                },
                new Function() {
                    public Object invoke(final Object node) {
                        return minCapacity / (double) ((Tree.Node) node).number();
                    }
                },
                new Function() {
                    public Object invoke(final Object node) {
                        return 1.0 - minCapacity / (double) ((Tree.Node) node).number();
                    }
                }
        );
        maxSize = maxBufferSize;
    }

    /**
     * Create a new buffer node
     * @param level a level of the new node
     * @return the new node
     */
    public Tree.Node createNode (int level) {
        return new Node().initialize(level, new ArrayList(maxNodeCapacity));
    }

    /**
     * TODO Inserts an object into the buffer
     *
     * @param data the object that is to be inserted
     */
    public void insertWithAnnihilation(final E data) {
        if (isFull())
            throw new IllegalStateException("Buffer overflow!");
        super.insertWithAnnihilation(data);
    }

    /**
     * Removes an insertion entry from the buffer. If there is no insertion entry, inserts a deletion entry.
     *
     * @param data   object to remove
     * @return the removed object or <tt>null</tt> if no object was removed
     * @see xxl.core.indexStructures.Tree#remove(xxl.core.indexStructures.Descriptor, int, xxl.core.predicates.Predicate)
     */
    public Object removeWithAnnihilation(final E data) {
        if (isFull())
            throw new IllegalStateException("Buffer overflow!");
        return super.removeWithAnnihilation(data);
    }

    /**
     * Checks if buffer is at its maximum capacity
     * @return true if yes, false otherwise
     */
    public boolean isFull() {
        return getCurrentSize() == maxSize;
    }

    /**
     * Removes all data from the buffer
     */
    public void clear() {
        super.clear();
        bufferContainer.clear();
    }

    /**
     * Returns entries currently stored in the buffer as a list
     * @return buffer contents
     */
    public OperationGroup<E> flatten() {
        final OperationGroup<E> result = new OperationGroup<>(getCurrentSize());
        //noinspection unchecked
        final Cursor<Entry<E>> cursor = query();
        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        cursor.close();
        return result;
    }

    /**
     * Removes a group of operations from the buffer
     * @param pushDownGroup a group of operations to be removed
     */
    @SuppressWarnings({"TypeMayBeWeakened"})
    public void removeGroup(final OperationGroup<E> pushDownGroup) {
        for (final UpdateTree.Entry<E> entry : pushDownGroup) {
            final Entry<E> result = removeExactEntry(entry);
            assert (result != null) || (entry.isDeletion());
        }
    }
}
