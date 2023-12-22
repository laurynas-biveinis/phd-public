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

import aau.bufferedIndexes.diskTrees.TreeClearIOState;
import xxl.core.io.Convertable;

import java.io.IOException;

/**
 * An interface that the RRTree should implement.
 */
public interface IRRTree<E extends Convertable> {

    /**
     * Completes an operation.  Finalizes its statistics and removes it from buffer if it is still there.
     *
     * @param op an operation that has been completed
     */
    public void completeOperation(final UpdateTree.Entry<E> op);

    /**
     * Cleans garbage from the tree, if applicable.
     *
     * @param rebuildTree if <code>true</code>, a new disk tree is built
     *
     * @return TreeClearIOState object with old disk tree disposal I/O counts
     * @throws IOException if I/O error while working with temporary files occurs
     */
    public TreeClearIOState cleanGarbage(boolean rebuildTree) throws IOException;
}
