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
package aau.bufferedIndexes.objectTracers;

import aau.bufferedIndexes.OperationType;
import aau.bufferedIndexes.TestData;
import aau.bufferedIndexes.TestUtils;
import org.junit.Test;
import xxl.core.spatial.KPE;

/**
 * Tests for NullObjectTracer
 */
public class NullObjectTracerTest {

    @Test
    public void testNullObjecTracer() {
        final ObjectTracer<KPE> nullObjectTracer = new NullObjectTracer<>();

        nullObjectTracer.registerObject(1);
        nullObjectTracer.traceObject(TestData.data[0], ObjectTracer.Operation.INDEX_NODE_PIGGYBACKING);
        nullObjectTracer.traceUpdateTreeEntry(TestUtils.makeOperation(0.0, 0.0, 1.0, 1.0, OperationType.INSERTION),
                ObjectTracer.Operation.BULK_RELOADER_2ND_PASS_WRITE, null);
    }
}
