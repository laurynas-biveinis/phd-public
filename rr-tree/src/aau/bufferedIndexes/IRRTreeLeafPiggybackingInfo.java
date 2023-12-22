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

/**
 * An interface that RRTreeLeafPiggybackingInfo must implement.
 */
public interface IRRTreeLeafPiggybackingInfo {

    /**
     * Registers an operation that may be piggybacked which will decrease the node size.
     */
    public void addPotentialSizeDecreasingOp();

    /**
     * Registers an operation that may be piggybacked which will increase the node size.
     */
    public void addPotentialSizeIncreasingOp();

    /**
     * Return the number of node size decreasing operations that may be piggybacked.
     *
     * @return the number of node size decreasing operations that may be piggybacked
     */
    public int getNumOfSizeDecreasingOps();

    /**
     * Return the number of node size increasing operations that may be piggybacked.
     *
     * @return the number of node size increasing operations that may be piggybacked
     */
    public int getNumOfSizeIncreasingOps();

    /**
     * Register additional number of node-size-decreasing operations that may not be piggybacked.
     *
     * @param limit how many additional node-size-decreasing operations may not be piggybacked
     */
    public void limitSizeDecreasingOps(int limit);

    /**
     * Register additional number of node-size-increasing operations that may not be piggybacked.
     *
     * @param limit how many additional node-size-increasing operations may not be piggybacked
     */
    public void limitSizeIncreasingOps(int limit);

    /**
     * Return the total number of node-size-decreasing operations that may not be piggybacked.
     *
     * @return the total number of deletion operations that may not be piggybacked
     */
    public int getUnpiggybackableSizeDecreasingOps();

    /**
     * Return the total number of deletion operations that may not be piggybacked.
     *
     * @return the total number of deletion operations that may not be piggybacked
     */
    public int getUnpiggybackableSizeIncreasingOps();

    /**
     * Return the net node size change if all possible operations were piggybacked.
     *
     * @return the net node size change if all possible operations were piggybacked
     */
    public int nodeSizeChange();

    /**
     * Returns <code>true</code> if node is changed at all, that is, there are no insertions nor deletions that may be
     * piggybacked on this node.
     *
     * @return node change flag
     */
    public boolean isNodeChanged();
}
