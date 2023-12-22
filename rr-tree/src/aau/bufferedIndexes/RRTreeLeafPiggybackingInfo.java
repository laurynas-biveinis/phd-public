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
 * Information about currently-happening leaf node piggybacking: how many operations of each kind may and may not be
 * piggybacked.
 */
public class RRTreeLeafPiggybackingInfo implements IRRTreeLeafPiggybackingInfo {

    /**
     * Number of node-size-decreasing operations that may be piggybacked
     */
    private int nodeSizeDecreasingOps = 0;

    /**
     * Number of node-size-increasing operations that may be piggybacked
     */
    private int nodeSizeIncreasingOps = 0;

    /**
     * Number of node-size-decreasing operations that may not be piggybacked
     */
    private int unPiggybackableNodeSizeDecreasingOps = 0;

    /**
     * Number of node-size-increasing operations that may not be piggybacked
     */
    private int unPiggybackableNodeSizeIncreasingOps = 0;

    /**
     * Register a new node-size-decreasing operation that may be piggybacked.
     */
    public void addPotentialSizeDecreasingOp() {
        nodeSizeDecreasingOps++;
    }

    /**
     * Registers a new node-size-increasing operation that may be piggybacked.
     */
    public void addPotentialSizeIncreasingOp() {
        nodeSizeIncreasingOps++;
    }

    /**
     * Returns the number of node-size-decreasing operations that may be piggybacked.
     *
     * @return the number of node-size-decreasing operations that may be piggybacked
     */
    public int getNumOfSizeDecreasingOps() {
        return nodeSizeDecreasingOps;
    }

    /**
     * Returns the number of node-size-increasing operations that may be piggybacked.
     *
     * @return the number of node-size-increasing operations that may be piggybacked
     */
    public int getNumOfSizeIncreasingOps() {
        return nodeSizeIncreasingOps;
    }

    /**
     * Registers additional number of node-size-decreasing operations that may not be piggybacked.
     *
     * @param limit how many additional node-size-decreasing operations may not be piggybacked
     */
    public void limitSizeDecreasingOps(int limit) {
        unPiggybackableNodeSizeDecreasingOps += limit;
        nodeSizeDecreasingOps -= limit;
    }

    /**
     * Registers additional number of node-size-increasing operations that may not be piggybacked.
     *
     * @param limit how many additional node-size-increasing operations may not be piggybacked
     */
    public void limitSizeIncreasingOps(int limit) {
        unPiggybackableNodeSizeIncreasingOps += limit;
        nodeSizeIncreasingOps -= limit;
    }

    /**
     * Returns the total number of node-size-decreasing operations that may not be piggybacked.
     *
     * @return the total number of node-size-decreasing operations that may not be piggybacked
     */
    public int getUnpiggybackableSizeDecreasingOps() {
        return unPiggybackableNodeSizeDecreasingOps;
    }

    /**
     * Return the total number of node-size-increasing operations that may not be piggybacked.
     *
     * @return the total number of node-size-increasing operations that may not be piggybacked
     */
    public int getUnpiggybackableSizeIncreasingOps() {
        return unPiggybackableNodeSizeIncreasingOps;
    }

    /**
     * Return the net node size change if all possible operations were piggybacked.
     *
     * @return the net node size change if all possible operations were piggybacked
     */
    public int nodeSizeChange() {
        return nodeSizeIncreasingOps - nodeSizeDecreasingOps;
    }

    /**
     * Returns <code>true</code> if node is changed at all, that is, there are no insertions nor deletions that may be
     * piggybacked on this node.
     *
     * @return node change flag
     */
    public boolean isNodeChanged() {
        return (nodeSizeIncreasingOps != 0) || (nodeSizeDecreasingOps != 0);
    }
}
