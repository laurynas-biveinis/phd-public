/*
     Copyright (C) 2009 Laurynas Biveinis

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

import xxl.core.indexStructures.Descriptor;
import xxl.core.spatial.KPE;
import xxl.core.spatial.points.DoublePoint;
import xxl.core.spatial.rectangles.DoublePointRectangle;

/**         
 * Various things to facilitate unit testing
 */
@SuppressWarnings({"StaticMethodOnlyUsedInOneClass"})
class WorkloadTestUtils {
    private WorkloadTestUtils() { }

    public static Descriptor makeDescriptor(final double x1, final double y1, final double x2, final double y2) {
        return new DoublePointRectangle(new DoublePoint(new double[]{x1, y1}), new DoublePoint(new double[]{x2, y2}));
    }

    public static KPE makeDataRectangle(final double x1, final double y1, final double x2, final double y2) {
        return new KPE(makeDescriptor(x1, y1, x2, y2));
    }
}
