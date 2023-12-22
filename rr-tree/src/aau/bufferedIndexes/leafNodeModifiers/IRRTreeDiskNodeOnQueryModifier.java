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
package aau.bufferedIndexes.leafNodeModifiers;

import aau.bufferedIndexes.IRRTreeLeafPiggybackingInfo;
import aau.bufferedIndexes.OperationTypeStat;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import xxl.core.io.Convertable;

/**
 * TODO: rename: drop "Query" part
 * An interface of a leaf-level RR-tree node modifier during queries (i.e. piggybacking)
 * @param <E> the type of data elements in the RR-tree
 */
public interface IRRTreeDiskNodeOnQueryModifier<E extends Convertable> {

    /**
     * Possibly modify a given node when it is read for a query.
     *
     * @param node                 a node to modify
     * @param allowReorganization  flag if it is allowed to make the node under- or overflowing
     * @param epsilon              how much expand buffer query rectangle in each direction
     * @param piggybackingInfo     a piggybacking info object to carry info about this particular piggybacking
     * @return <code>true</code> if node has been changed, <code>false</code> otherwise
     */
    public boolean modify(final IRRTreeDiskNode<E> node, final boolean allowReorganization,
                          final double epsilon, final IRRTreeLeafPiggybackingInfo piggybackingInfo);

    /**
     * Finalize any made modifications so far.  Usually involves registering statistics and marking the operations as
     * completed as necessary.
     *
     * @param stats the statistics to register operations at
     */
    public void finalizeModifications(final OperationTypeStat stats);
}
