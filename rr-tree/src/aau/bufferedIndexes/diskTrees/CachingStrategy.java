/*
     Copyright (C) 2009, 2010 Laurynas Biveinis

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
package aau.bufferedIndexes.diskTrees;

/**
 * Caching strategy for the disk tree nodes of the RR-Tree
 */
public class CachingStrategy {

    private final IRRDiskTree<?> treeToCache;

    public CachingStrategy(final IRRDiskTree<?> treeToCache) {
        this.treeToCache = treeToCache;
    }

    public boolean shouldBeFixed(final IRRTreeDiskNode<?> accessedNode) {
        return treeToCache.height() == accessedNode.level() + 1;
    }

    public boolean shouldBeFixed(final IRRTreeIndexEntry<?> entryToAccessedNode) {
        return treeToCache.height() == entryToAccessedNode.level() + 1;
    }
}
