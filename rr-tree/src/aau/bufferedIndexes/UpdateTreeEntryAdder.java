/*
     Copyright (C) 2011 Laurynas Biveinis

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

import aau.bufferedIndexes.diskTrees.IRRDiskUpdateTree;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import xxl.core.functions.Function;
import xxl.core.io.Convertable;

import java.util.*;

public class UpdateTreeEntryAdder {
    public static <E extends Convertable, T> T doAdd(final ListIterator<T> oldDataItr, final T newData,
                                                     final IRRDiskUpdateTree<E> tree, final Comparator<E> comparator,
                                                     final ObjectTracer<E> tracer,
                                                     final Function<T, UpdateTree.Entry<E>> unwrapper) {
        final UpdateTree.Entry<E> newEntry = unwrapper.invoke(newData);
        tracer.traceUpdateTreeEntry(newEntry, ObjectTracer.Operation.ENTRY_COLLECTION_ADD, null);
        boolean entriesAnnihilate = false;
        boolean reachedEnd = true;
        T oldData = null;
        while (oldDataItr.hasNext()) {
            oldData = oldDataItr.next();
            final UpdateTree.Entry<E> oldEntry = unwrapper.invoke(oldData);
            final int compResult = comparator.compare(newEntry.getData(), oldEntry.getData());
            if (compResult < 0) {
                reachedEnd = false;
                break;
            }
            if (compResult == 0 && tree.doEntriesAnnihilate(newEntry, oldEntry)) {
                entriesAnnihilate = true;
                break;
            }
        }
        if (entriesAnnihilate) {
            tracer.traceUpdateTreeEntry(newEntry, ObjectTracer.Operation.ENTRY_COLLECTION_ADD_ANNIHILATE, null);
            oldDataItr.remove();
            return oldData;
        }
        tracer.traceUpdateTreeEntry(newEntry, ObjectTracer.Operation.ENTRY_COLLECTION_ADD_INCREASE, null);
        if (!reachedEnd) {
            assert oldDataItr.hasPrevious();
            oldDataItr.previous();
        }
        oldDataItr.add(newData);
        return null;
    }

    public static <E extends Convertable> boolean isSorted (final Collection<UpdateTree.Entry<E>> loadedEntries,
                                                            final Comparator<E> comparator) {
        if (loadedEntries.size() < 2)
            return true;
        final Iterator<UpdateTree.Entry<E>> itr = loadedEntries.iterator();
        UpdateTree.Entry<E> lesser = itr.next();
        while (itr.hasNext()) {
            final UpdateTree.Entry<E> greater = itr.next();
            if (comparator.compare(lesser.getData(), greater.getData()) > 0) {
                System.err.println("lesser = " + lesser.toString());
                System.err.println("greater = " + greater.toString());
                return false;
            }
            lesser = greater;
        }
        return true;
    }
}
