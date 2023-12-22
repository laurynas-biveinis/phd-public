/*
     Copyright (C) 2007, 2009 Laurynas Biveinis

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
package aau.workload;

import java.util.Random;

/**
 * Generates random position and random shape queries.  They are positioned in the universe uniformly.  The shape
 * is random rectangle, with edge length a Gaussian random around square length. 
 */
public class RandomQueryGenerator {
    private final SpatialExtent world;

    private final double queryArea;
    private final double avgEdge;
    private final Random rng = new Random();

    private static final double EPSILON = 0.000001;

    /**
     * Creates a new random query generator object.
     * @param queryRegionSize query region size given as part of world size
     * @param world world dimensions
     */
    public RandomQueryGenerator(final double queryRegionSize, final SpatialExtent world) {
        if (queryRegionSize <= EPSILON)
            throw new IllegalArgumentException("queryRegionSize must be larger than " + EPSILON);

        this.world = world;
        queryArea = world.xLength() * world.yLength() * queryRegionSize;
        avgEdge = Math.sqrt(queryArea);
    }

    public WorkloadOperation generateQuery() {
        double xEdgeLength;
        double yEdgeLength;
        do {
            xEdgeLength = avgEdge + rng.nextGaussian() * avgEdge;
            yEdgeLength = queryArea / xEdgeLength;
        } while ((xEdgeLength < EPSILON) || (yEdgeLength < EPSILON)
                || (xEdgeLength > world.xLength()) || (yEdgeLength > world.yLength()));
        final Coords queryLowerLeft = new Coords(constrainedRandomPoint(world.x1(), world.xLength() - xEdgeLength),
                                                 constrainedRandomPoint(world.y1(), world.yLength() - yEdgeLength));
        final Coords queryUpperRight = new Coords(queryLowerLeft.x + xEdgeLength,
                                                  queryLowerLeft.y + yEdgeLength);

        return new WorkloadOperation(WorkloadOperation.OperationType.QUERY, 0,
                new SpatialExtent(queryLowerLeft, queryUpperRight));
    }

    /**
     * Returns a random double value that is between lowerBound and lowerBound + length.
     *
     * @param lowerBound the lower bound for the returned random value
     * @param length the length of the interval for the returned random value
     * @return the random value in the interval
     */
    private double constrainedRandomPoint(final double lowerBound, final double length) {
        return rng.nextDouble() * length + lowerBound;
    }
}
