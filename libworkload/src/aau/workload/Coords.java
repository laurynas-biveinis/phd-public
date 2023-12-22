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

import java.io.PrintWriter;

public final class Coords {

    private static final Double EPSILON = 0.00001;

    public final double x;
    public final double y;

    public Coords(final double xx, final double yy) {
        x = xx;
        y = yy;
    }

    public double distance(final Coords other) {
        return Math.sqrt((other.x - x) * (other.x - x) + (other.y - y) * (other.y - y));
    }

    public void printCoords(final PrintWriter output) {
        output.print(String.valueOf(x));
        output.print('\t');
        output.print(String.valueOf(y));
    }

    public String toString() {
        return "x = " + x + ", y = " + y;
    }

    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Coords))
            return false;
        final Coords c = (Coords)o;
        return (Math.abs(x - c.x) < EPSILON) && (Math.abs(y - c.y) < EPSILON);
    }

    /**
     * Returns the hash value for this object.
     *
     * @return the hash value of this object
     */
    public int hashCode() {
        int result = 17;
        result = result * 37 + hashDouble(x);
        result = result * 37 + hashDouble(y);
        return result;
    }

    /**
     * Returns a hash value of a double value.  Implementation follows Effective Java by Bloch.
     *
     * @param value a value to hash
     * @return the hash value
     */
    private static int hashDouble(final double value) {
        final long bits = Double.doubleToLongBits(value);
        //noinspection NumericCastThatLosesPrecision
        return (int)(bits ^ (bits >>> 32));
    }
}
