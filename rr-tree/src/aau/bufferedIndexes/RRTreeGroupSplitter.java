/*
     Copyright (C) 2007, 2008, 2009, 2010 Laurynas Biveinis

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

import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import xxl.core.io.Convertable;

import java.util.List;

/**
 * An interface that should be implemented by various GroupSplit strategies.
 */
public interface RRTreeGroupSplitter {
    <T extends Convertable> List<IRRTreeDiskNode<T>> groupSplit(IRRTreeDiskNode<T> node, final IRRDiskTree<T> tree);
}
