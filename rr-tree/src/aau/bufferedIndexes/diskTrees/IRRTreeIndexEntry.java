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
package aau.bufferedIndexes.diskTrees;

import aau.bufferedIndexes.HasLogicalSize;
import xxl.core.collections.containers.Container;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.Tree;
import xxl.core.io.Convertable;

/**
 * An interface that RRDiskTree.IndexEntry should implement.
 */
public interface IRRTreeIndexEntry<E extends Convertable> extends HasLogicalSize {

    /* Initializers */

    /**
     * Initializes an index entry with a descriptor.
     *
     * @param descriptor the descriptor of the index entry
     * @return the initialized index entry itself
     */
    public Tree.IndexEntry initialize (Descriptor descriptor);

    /**
     * Initializes an index entry with a node ID.
     *
     * @param id an ID of the node referred to by this index entry
     * @return the initialized index entry itself
     */
    public Tree.IndexEntry initialize (Object id);

    /**
     * Initializes an index entry with a container and node ID.
     *
     * @param container a container to store the node referred to by this index entry
     * @param id a node ID in the container
     * @return the initialized index entry itself
     */
    public Tree.IndexEntry initialize (Container container, Object id);

    /* Getters */

    /**
     * Returns the container where the node referred to by this index entry should be stored.
     * @return the container of the node pointed to by this index entry
     */
    public Container container ();

    /**
     * Returns the descriptor of this index entry.
     * @return the descriptor of this index entry
     */
    public Descriptor descriptor ();

    /**
     * Returns the id of this index entry.
     * @return the id of this index entry
     */
    public Object id();

    /**
     * Returns the level of the node pointed to by this index entry.
     * @return the level of the node pointed to by this index entry
     */
    public int level();

    /* Disk I/O */

    /**
     * Retrieves from the storage the node associated with this index entry.
     * @return the node associated with this index entry
     */
    public IRRTreeDiskNode<E> get ();

    /**
     * Overwrites the node referred to by the current index entry by a new node.
	 *
	 * @param node the new node
	 */
    public void update (IRRTreeDiskNode<E> node);

    /** 
     * Overwrites the node referred to by the current index entry by a new node.
	 *
	 * @param node the new node
	 * @param unfix signals whether the node can be removed from the underlying buffer
	 */
    public void update (IRRTreeDiskNode<E> node, boolean unfix);

    /**
     * Removes the node referred to by this index entry from the container.
     */
	public void remove();

    // TODO: javadoc
    boolean spatiallyContains(E data);
}
