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

import aau.bufferedIndexes.diskTrees.IRRDiskTree;
import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import xxl.core.indexStructures.ORTree;
import xxl.core.indexStructures.RTree;
import xxl.core.io.Convertable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * An implementation of the GroupSplit algorithm that works by
 * recursively invoking standard R*-tree two way Split algorithm until
 * resulting nodes do not overflow.  This implementation is adapted
 * from XXL library.
 */
public class RecursiveTwoWaySplitter implements RRTreeGroupSplitter {

    /**
     * An adaptation of standard R*-tree split algorithm.  The main difference is that this split is allowed to return
     * overflowing split nodes that may have up to (number of elements in the node being split - 1).
     * @param node a node to split, on return contains one of the split nodes
     * @param tree a tree containing the node
     * @return another one of the split nodes
     */
    private static <E extends Convertable> IRRTreeDiskNode<E> twoWaySplit(final IRRTreeDiskNode<E> node,
                                                                          final IRRDiskTree<E> tree) {
        assert (node.overflows());

        // Copy-pasted straight from the RTree.split().
        final int minEntries = node.splitMinNumber();
        final int maxEntries = node.number() - node.splitMinNumber(); // The split should split away at least m nodes

        final IRRTreeDiskNode<E> newNode = tree.createNode(node.level()); // TODO: size it

        // TODO: generify R-tree
        //noinspection unchecked
        ((RTree)tree).splitNode((ORTree.Node)node, (Collection<Object>)newNode.getEntries(), minEntries,  maxEntries);

        assert(newNode.number() <= maxEntries);
        assert(node.number() <= maxEntries);

        return newNode;
    }

    public <E extends Convertable> List<IRRTreeDiskNode<E>> groupSplit(final IRRTreeDiskNode<E> node,
                                                                       final IRRDiskTree<E> tree) {
        final List<IRRTreeDiskNode<E>> result = new LinkedList<>();
        final Queue<IRRTreeDiskNode<E>> toProcess = new LinkedList<>();
        toProcess.add(node);
        while (!toProcess.isEmpty()) {
            final IRRTreeDiskNode<E> element = toProcess.remove();
            if (element.overflows()) {
                final IRRTreeDiskNode<E> splitElement = twoWaySplit(element, tree);
                toProcess.add(splitElement);
                toProcess.add(element);
            } else {
                result.add(element);
            }
        }
        return result;
    }
}
