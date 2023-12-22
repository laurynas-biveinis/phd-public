/*
     Copyright (C) 2010, 2011, 2012 Laurynas Biveinis

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

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of UpdateTree.Entry that does not contain annihilating pairs and is sorted according to a given
 * Comparator.
 */
public class UpdateTreeEntryCollection<E extends Convertable> implements Iterable<UpdateTree.Entry<E>> {

    final private Function<UpdateTree.Entry<E>, UpdateTree.Entry<E>> identityUnwrapper
            = new Function<UpdateTree.Entry<E>, UpdateTree.Entry<E>>() {
        @Override
        public UpdateTree.Entry<E> invoke(UpdateTree.Entry<E> argument) {
            return argument;
        }
    };

    // TODO: ArrayList would offer better iteration performance, but we don't use it as list compacting becomes
    // expensive.
    final private List<UpdateTree.Entry<E>> loadedEntries = new LinkedList<>();

    final private Comparator<E> spatialComparator;

    final private ObjectTracer<E> objectTracer;

    private int peakSize = 0;

    public UpdateTreeEntryCollection(final Comparator<E> spatialComparator, final ObjectTracer<E> objectTracer) {
        this.spatialComparator = spatialComparator;
        this.objectTracer = objectTracer;
    }

    public void add(final UpdateTree.Entry<E> newEntry, final IRRDiskUpdateTree<E> tree) {
        assert UpdateTreeEntryAdder.isSorted(loadedEntries, spatialComparator);
        if (UpdateTreeEntryAdder.doAdd(loadedEntries.listIterator(), newEntry, tree, spatialComparator, objectTracer,
                                       identityUnwrapper) == null) {
            if (loadedEntries.size() > peakSize)
                peakSize = loadedEntries.size();
        }
        assert UpdateTreeEntryAdder.isSorted(loadedEntries, spatialComparator);
    }

    public void clear() {
        loadedEntries.clear();
    }

    public Iterator<UpdateTree.Entry<E>> iterator() {
        return loadedEntries.iterator();
    }

    public int size() {
        return loadedEntries.size();
    }

    public int memSize() {
        return size() * UpdateTree.KPE_OPERATION_SIZE;
    }

    public boolean isTransitive() {
        if (loadedEntries.size() < 2)
            return true;
        assert UpdateTreeEntryAdder.isSorted(loadedEntries, spatialComparator);
        int i = 0;
        for (UpdateTree.Entry<E> loadedEntry : loadedEntries) {
            i++;
            if (i == loadedEntries.size())
                i--;
            final Iterator<UpdateTree.Entry<E>> itr2 = loadedEntries.listIterator(i);
            while (itr2.hasNext()) {
                final UpdateTree.Entry<E> e2 = itr2.next();
                assert spatialComparator.compare(loadedEntry.getData(), e2.getData()) <= 0;
            }
        }
        return true;
    }
}
