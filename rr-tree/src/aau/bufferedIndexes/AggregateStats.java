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
package aau.bufferedIndexes;

/**
 * Class for calculating aggregate statistics such as arithmetic mean
 */
public class AggregateStats {

    private int numOfValues = 0;

    private int totalInt = 0;

    private double average;

    private double variance;

    private double maximum = Double.NEGATIVE_INFINITY;

    public void registerValue(int value) {
        totalInt += value;
        registerValue((double)value);
    }

    public void registerValue(double value) {
        numOfValues++;
        if (value > maximum)
            maximum = value;
        if (numOfValues == 1) {
            average = value;
            variance = 0.0D;
        }
        else {
            final double oldAvg = average;
            average = oldAvg + (value - oldAvg) / numOfValues;
            variance += (value - oldAvg) * (value - average);
        }
    }

    public double average() {
        if (numOfValues == 0)
            return Double.NEGATIVE_INFINITY;
        return average;
    }

    public int count() {
        return numOfValues;
    }

    public double deviation() {
        return Math.sqrt(variance / (numOfValues - 1));
    }

    public double maximum() {
        return maximum;
    }

    public int totalInt() {
        return totalInt;
    }
}
