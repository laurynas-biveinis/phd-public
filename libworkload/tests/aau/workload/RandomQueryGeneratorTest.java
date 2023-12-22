/*
     Copyright (C) 2012 Laurynas Biveinis

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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testsuite for RandomQueryGenerator class
 */
public class RandomQueryGeneratorTest {

    final static private SpatialExtent worldExtent = new SpatialExtent(10, 20, 100, 200);
    final static private double QUERY_REGION_SIZE = 0.1;
    final static private double EPSILON = 0.0001;

    @Test
    public void testRandomQueryGenerator() {
        final RandomQueryGenerator randomQueryGenerator = new RandomQueryGenerator(QUERY_REGION_SIZE, worldExtent);

        WorkloadOperation op = randomQueryGenerator.generateQuery();
        assertTrue(op.isQuery());
        SpatialExtent qExtent = op.getSpatialExtent();
        assertTrue(worldExtent.x1() <= qExtent.x1());
        assertTrue(worldExtent.x2() >= qExtent.x2());
        assertTrue(worldExtent.y1() <= qExtent.y1());
        assertTrue(worldExtent.y2() >= qExtent.y2());
        assertEquals(worldExtent.xLength() * worldExtent.yLength() * QUERY_REGION_SIZE,
                qExtent.xLength() * qExtent.yLength(), EPSILON);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testTooSmallQueryRegionSize() {
        final RandomQueryGenerator rqG = new RandomQueryGenerator(0.0000001, worldExtent);
        rqG.generateQuery();
    }
}
