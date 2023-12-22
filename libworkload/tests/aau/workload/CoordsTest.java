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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.Assert.*;

/**
 * Unit tests for Coords class
 */
public class CoordsTest {

    final private static double EPSILON = 0.00001;

    @Test
    public void testDistance() {
        final Coords c1 = new Coords(-1.0, -1.0);
        final Coords c2 = new Coords(2.5, 3.4);
        final double dist = Math.sqrt((c1.x - c2.x) * (c1.x - c2.x)
                + (c1.y - c2.y) * (c1.y - c2.y));

        assertEquals(c1.distance(c2), c2.distance(c1), EPSILON);
        assertEquals(dist, c1.distance(c2), EPSILON);
    }

    @Test
    public void testPrintCoords() {
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        final PrintWriter printWriter = new PrintWriter(byteOutputStream);

        final Coords c = new Coords(1.5, 2.7);
        c.printCoords(printWriter);
        printWriter.close();

        assertEquals("1.5\t2.7", byteOutputStream.toString());
    }

    @Test
    public void testToString() {
        final Coords c = new Coords(1.9, -2.3);
        assertEquals("x = 1.9, y = -2.3", c.toString());
    }

    @Test
    public void testEquals() {
        final Coords c = new Coords(1.0, 2.0);
        assertTrue(c.equals(c));
        //noinspection ObjectEqualsNull
        assertFalse(c.equals(null));

        final Coords c2 = new Coords(-1.0, 2.0);
        assertFalse(c.equals(c2));
        assertFalse(c2.equals(c));

        final Coords c3 = new Coords(1.0 + EPSILON / 2, 2.0 - EPSILON / 2);
        assertTrue(c.equals(c3));
        assertTrue(c3.equals(c));

        final Coords c4 = new Coords(1.0 + EPSILON / 2, 4.0);
        assertFalse(c.equals(c4));
        assertFalse(c4.equals(c));
    }

    @Test
    public void testHashCode() {
        final Coords c = new Coords(1.0, 2.0);
        assertEquals(c.hashCode(), c.hashCode());

        final Coords c2 = new Coords(1.0, 2.0);
        assertEquals(c.hashCode(), c2.hashCode());
    }
}
