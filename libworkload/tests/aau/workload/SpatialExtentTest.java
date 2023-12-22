/*
     Copyright (C) 2011 Laurynas Biveinis

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
import xxl.core.spatial.rectangles.DoublePointRectangle;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for SpatialExtent class
 */
public class SpatialExtentTest {

    private static final double EPSILON = 0.00001D;
    
    @Test
    public void fourDoublesConstructor() {
        final SpatialExtent e = new SpatialExtent(1.0, 2.0, 3.0, 4.0);
        assertExtentCoords(e, 1.0, 2.0, 3.0, 4.0);
    }
    
    @Test
    public void twoCoordsConstructor() {
        final SpatialExtent e = new SpatialExtent(new Coords(4.0, 3.0), new Coords(2.0, 1.0));
        assertExtentCoords(e, 4.0, 3.0, 2.0, 1.0);
    }
    
    @Test
    public void stringConstructor() {
        final SpatialExtent e = new SpatialExtent("10.0 11.0 12.0 13.0");
        assertExtentCoords(e, 10.0, 11.0, 12.0, 13.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void stringConstructorTooManyValues() {
        new SpatialExtent("10.0 11.0 12.0 13.0 14.0");
    }

    @Test(expected = NumberFormatException.class)
    public void stringConstructorNotNumber() {
        new SpatialExtent("1.0 foo bar 2.0");
    }
    
    @Test
    public void testMinimum() {
        final SpatialExtent min = SpatialExtent.minimum();
        final SpatialExtent extent = new SpatialExtent(0.0, 0.0, 1.0, 1.0);

        final SpatialExtent result = min.include(extent);
        assertEquals(result, extent);
    }

    @Test
    public void center() {
        final SpatialExtent e = new SpatialExtent(1.0, 2.0, 2.0, 3.0);
        final Coords c = e.center();
        assertEquals(c.x, 1.5, EPSILON);
        assertEquals(c.y, 2.5, EPSILON);
    }
    
    @Test
    public void xxlDoublePointRectangle() {
        final SpatialExtent e = new SpatialExtent(1.0, 2.0, 3.0, 4.0);
        DoublePointRectangle dpr = e.getDoublePointRectangle();
        assertEquals(dpr.getCorner(false).getValue(0), 1.0, EPSILON);
        assertEquals(dpr.getCorner(false).getValue(1), 2.0, EPSILON);
        assertEquals(dpr.getCorner(true).getValue(0), 3.0, EPSILON);
        assertEquals(dpr.getCorner(true).getValue(1), 4.0, EPSILON);
    }

    @Test
    public void testPrintCoords() {
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        final PrintWriter printWriter = new PrintWriter(byteOutputStream);

        final SpatialExtent e = new SpatialExtent(1.1, 2.2, 3.3, 4.4);
        e.print(printWriter);
        printWriter.close();

        assertEquals("1.1\t2.2 3.3\t4.4", byteOutputStream.toString());
    }

    @Test
    public void testToString() {
        final SpatialExtent e = new SpatialExtent(1, -2, 3.3, 4.4);
        assertEquals("Lower left: x = 1.0, y = -2.0, upper right: x = 3.3, y = 4.4", e.toString());
    }

    @Test
    public void testEquals() {
        final SpatialExtent e = new SpatialExtent(1.0, 1.0, 2.0, 2.0);
        assertTrue(e.equals(e));
        //noinspection ObjectEqualsNull
        assertFalse(e.equals(null));

        final SpatialExtent e2 = new SpatialExtent(2.0, 2.0, 3.0, 3.0);
        assertFalse(e.equals(e2));
        assertFalse(e2.equals(e));

        final SpatialExtent e3 = new SpatialExtent(1.0, 1.0, 2.0, 2.0);
        assertTrue(e.equals(e3));
        assertTrue(e3.equals(e));

        final SpatialExtent e4 = new SpatialExtent(1.0 + EPSILON / 2, 1.0 - EPSILON / 2, 2.0 + EPSILON / 2, 2.5);
        assertFalse(e.equals(e4));
        assertFalse(e4.equals(e));

        final SpatialExtent e5 = new SpatialExtent(1.0 + EPSILON / 2, 1.0 - EPSILON / 2,
                2.0 + EPSILON / 2, 2.0 - EPSILON / 2);
        assertTrue(e.equals(e5));
        assertTrue(e5.equals(e));
    }

    @Test
    public void testHashCode() {
        final SpatialExtent e = new SpatialExtent(1.0, 1.0, 2.0, 2.0);
        assertEquals(e.hashCode(), e.hashCode());

        final SpatialExtent e2 = new SpatialExtent(1.0, 1.0, 2.0, 2.0);
        assertEquals(e.hashCode(), e2.hashCode());
    }

    private static void assertExtentCoords(final SpatialExtent e,
                                           final double x1, final double y1, final double x2, final double y2) {
        assertEquals(e.x1(), x1, EPSILON);
        assertEquals(e.y1(), y1, EPSILON);
        assertEquals(e.x2(), x2, EPSILON);
        assertEquals(e.y2(), y2, EPSILON);
    }
}
