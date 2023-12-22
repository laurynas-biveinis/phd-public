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
 * The mode modifier that does nothing.
 */
public class NullModeModifier<E extends Convertable> implements IRRTreeDiskNodeOnQueryModifier<E> {

    /**
     * Modifies a given node at query time, which in this case does nothing.
     *
     * @param node                 a node to modify (ignored)
     * @param allowReorganization  ignored
     * @param epsilon              ignored
     * @param piggybackingInfo     a piggybacking info object to carry info about this particular piggybacking (ignored)
     * @return always <code>false</code>
     */
    public boolean modify(final IRRTreeDiskNode<E> node, final boolean allowReorganization,
                          final double epsilon, final IRRTreeLeafPiggybackingInfo piggybackingInfo) {
        return false;
    }

    /**
     * Finalizes any made modifications, which in this case does nothing.
     *
     * @param stats the statistics to register operations at (ignored)
     */
    public void finalizeModifications(OperationTypeStat stats) {
    }
}
