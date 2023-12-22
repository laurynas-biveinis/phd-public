/*
     Copyright (C) 2007, 2009, 2011 Laurynas Biveinis

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

import xxl.core.spatial.points.DoublePoint;
import xxl.core.spatial.rectangles.DoublePointRectangle;

import java.io.PrintWriter;

/**
 * A class for specifying 2D spatial rectangle
 */
public final class SpatialExtent {
    private final Coords lowerLeft;
    private final Coords upperRight;

    static public SpatialExtent minimum() {
        return new SpatialExtent(Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
    }

    public SpatialExtent(final double x1, final double y1, final double x2, final double y2) {
        lowerLeft = new Coords(x1, y1);
        upperRight = new Coords(x2, y2);
    }

    public SpatialExtent(final Coords lowerLeft, final Coords upperRight) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    public SpatialExtent(final String extentString) {
        final String[] tokens = extentString.split("\\s+");
        if (tokens.length != 4)
            throw new IllegalArgumentException("SpatialExtent extentString must be x1 y1 x2 y2");
        final double x1 = Double.parseDouble(tokens[0]);
        final double y1 = Double.parseDouble(tokens[1]);
        final double x2 = Double.parseDouble(tokens[2]);
        final double y2 = Double.parseDouble(tokens[3]);
        lowerLeft = new Coords(x1, y1);
        upperRight = new Coords(x2, y2);
    }

    public Coords center() {
        return new Coords((upperRight.x + lowerLeft.x) / 2, (upperRight.y + lowerLeft.y) / 2); 
    }

    public DoublePointRectangle getDoublePointRectangle() {
        return new DoublePointRectangle(new DoublePoint(new double[]{lowerLeft.x, lowerLeft.y}),
                                        new DoublePoint(new double[]{upperRight.x, upperRight.y}));
    }

    public void print(final PrintWriter output) {
        lowerLeft.printCoords(output);
        output.print(' ');
        upperRight.printCoords(output);
    }

    public String toString() {
        return "Lower left: " + lowerLeft + ", upper right: " + upperRight;
    }

    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SpatialExtent))
            return false;
        final SpatialExtent e = (SpatialExtent)o;
        return lowerLeft.equals(e.lowerLeft) && upperRight.equals(e.upperRight);
    }

    public int hashCode() {
        int result = 17;
        result = result * 37 + lowerLeft.hashCode();
        result = result * 37 + upperRight.hashCode();
        return result;
    }

    SpatialExtent include(final Coords point) {
        final double x1 = Math.min(lowerLeft.x, point.x);
        final double y1 = Math.min(lowerLeft.y, point.y);
        final double x2 = Math.max(upperRight.x, point.x);
        final double y2 = Math.max(upperRight.y, point.y);
        return new SpatialExtent(x1, y1, x2, y2);
    }

    public SpatialExtent include(final SpatialExtent extent) {
        return include(extent.lowerLeft).include(extent.upperRight);
    }

    public double x1() {
        return lowerLeft.x;
    }

    public double y1() {
        return lowerLeft.y;
    }

    public double x2() {
        return upperRight.x;
    }

    public double y2() {
        return upperRight.y;
    }

    public double xLength() {
        return Math.abs(lowerLeft.x - upperRight.x);
    }

    public double yLength() {
        return Math.abs(lowerLeft.y - upperRight.y);
    }
}
