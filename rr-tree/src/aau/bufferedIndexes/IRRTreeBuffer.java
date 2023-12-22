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

import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

/**
 * An interface that RRTreeBuffer should implement.
 */
public interface IRRTreeBuffer<E extends Convertable> {

    /**
     * Perform a query, i.e. return a lazy cursor pointing to all leaf entries whose descriptors
     * overlap with the given <tt>queryDescriptor</tt>.
     *
     * @param queryDescriptor describes the query in terms of a descriptor
     * @return a lazy <tt>Cursor</tt> pointing to all response objects
     * @see xxl.core.indexStructures.Tree#query(xxl.core.indexStructures.Descriptor, int)
     */
    public Cursor<UpdateTree.Entry<E>> queryEntryOfAnyType(final Descriptor queryDescriptor);

    // TODO: javadoc
    public UpdateTree.Entry<E> removeExactEntry(final UpdateTree.Entry<E> object);
}
