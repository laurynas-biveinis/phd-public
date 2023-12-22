/*
     Copyright (C) 2010, 2012 Laurynas Biveinis

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

import xxl.core.spatial.KPE;

/**
 * Sample KPE test data for the tests
 */
public class TestData {

    private static final int DATA_SIZE = 2000;

    // (0, 0, 1, 1), (2, 2, 3, 3), (4, 4, 5, 5) ...
    public static KPE[] data;

    // (0, 2, 3, 1), (2, 0, 1, 3), (4, 6, 5, 7) ...
    public static KPE[] data2;

    static {
        data = new KPE[DATA_SIZE];
        data2 = new KPE[DATA_SIZE];
        for (int i = 0; i < data.length; i++) {
            data[i] = TestUtils.makeKPE(i, i * 2, i * 2, (i * 2) + 1, (i * 2)+ 1);
            if ((i % 2) == 0)
                data2[i] = TestUtils.makeKPE(i + DATA_SIZE, i * 2 + 2, i * 2, (i * 2) + 3, (i * 2)+ 1);
            else
                data2[i] = TestUtils.makeKPE(i + DATA_SIZE, i * 2 - 2, i * 2, (i * 2) - 1, (i * 2)+ 1);
        }
    }
}
