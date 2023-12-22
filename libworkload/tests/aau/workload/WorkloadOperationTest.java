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

import org.junit.Test;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.spatial.KPE;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * Testsuite for the WorkloadOperation class
 */
public class WorkloadOperationTest {

    private static KPE makeObject(final int id, final double x1, final double y1, final double x2, final double y2) {
        return new KPE(new DataID(id), WorkloadTestUtils.makeDescriptor(x1, y1, x2, y2),
                ConvertableConverter.DEFAULT_INSTANCE);
    }

    private static final String deleteOpStr = "delete 6 5.0 6.0 7.0 8.0";
    private static final String finalDeleteOpStr = "finaldelete 7 10.0 11.0 12.0 13.0";
    private static final String emptyOpStr = "empty";

    private static final SpatialExtent insertOpExtent = new SpatialExtent(1.0, 2.0, 3.0, 4.0);
    private static final String insertOpStr = "insert 5 1.0 2.0 3.0 4.0";
    private static final KPE insertOpObj = makeObject(5, 1.0, 2.0, 3.0, 4.0);

    private static final String insertOpStr2 = "insert 6 1.0 2.0 3.0 4.0";
    private static final String insertOpStr3 = "insert 5 1.1 2.0 3.0 4.0";

    private static final String queryOpStr = "query 4.0 3.0 2.0 1.0";
    private static final KPE queryOpRange = WorkloadTestUtils.makeDataRectangle(4.0, 3.0, 2.0, 1.0);

    private static final String deletingInsertionOpStr = "deletinginsert 7 1.0 2.0 3.0 4.0";

    private static final String invalidOpStr = "invalidop blah blah 1.0";

    @Test
    public void equals() {
        final WorkloadOperation op = new WorkloadOperation(insertOpStr);
        assertEquals(op, new WorkloadOperation(insertOpStr));
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse (op.equals (insertOpStr));
        assertFalse (op.equals (new WorkloadOperation(insertOpStr2)));
        assertFalse (op.equals (new WorkloadOperation(insertOpStr3)));
        assertFalse (op.equals (new WorkloadOperation(queryOpStr)));
    }

    @Test
    public void testHashCode() {
        final WorkloadOperation op = new WorkloadOperation(insertOpStr);
        assertEquals(op.hashCode(), op.hashCode());

        final WorkloadOperation op2 = new WorkloadOperation(insertOpStr);
        assertEquals(op.hashCode(), op2.hashCode());
    }

    @Test
    public void getOpId() {
        final WorkloadOperation op = new WorkloadOperation(insertOpStr);
        assertEquals (5, op.getId());
    }

    @Test
    public void getNotUpdateOpId() {
        final WorkloadOperation op = new WorkloadOperation(queryOpStr);
        try {
            op.getId();
            fail();
        }
        catch (AssertionError ignored) { }
    }

    @Test
    public void getObject() {
        final WorkloadOperation op = new WorkloadOperation(insertOpStr);
        assertEquals (insertOpObj, op.getObject());
    }
    
    @Test
    public void getNotUpdateObject() {
        final WorkloadOperation op = new WorkloadOperation(queryOpStr);
        try {
            op.getObject();
            fail();
        }
        catch (AssertionError ignored) { }
    }

    @Test
    public void getQueryRectangle() {
        final WorkloadOperation op = new WorkloadOperation(queryOpStr);
        assertEquals (queryOpRange, op.getQueryRectangle());
    }

    @Test
    public void getUpdateQueryRectangle() {
        final WorkloadOperation op = new WorkloadOperation(queryOpStr);
        try {
            op.getQueryRectangle();
            fail();
        }
        catch (AssertionError ignored) { }        
    }

    @Test
    public void isKindOfDelete() {
        WorkloadOperation op = new WorkloadOperation(deleteOpStr);
        assertTrue (op.isKindOfDelete());

        op = new WorkloadOperation(finalDeleteOpStr);
        assertTrue (op.isKindOfDelete());

        op = new WorkloadOperation(insertOpStr);
        assertFalse (op.isKindOfDelete());
    }

    @Test
    public void isEmptyBuffer() {
        WorkloadOperation op = new WorkloadOperation(emptyOpStr);
        assertTrue (op.isEmptyBuffer());

        op = new WorkloadOperation(queryOpStr);
        assertFalse (op.isEmptyBuffer());
    }

    @Test
    public void isInsert() {
        WorkloadOperation op = new WorkloadOperation(insertOpStr);
        assertTrue (op.isInsert());

        op = new WorkloadOperation(deleteOpStr);
        assertFalse (op.isInsert());
    }

    @Test
    public void isQuery() {
        WorkloadOperation op = new WorkloadOperation(queryOpStr);
        assertTrue (op.isQuery());

        op = new WorkloadOperation(deleteOpStr);
        assertFalse (op.isQuery());
    }

    @Test
    public void isDeletingInsertion() {
        WorkloadOperation op = new WorkloadOperation(deletingInsertionOpStr);
        assertTrue (op.isDeletingInsert());
        op = new WorkloadOperation(insertOpStr);
        assertFalse (op.isDeletingInsert());
    }

    private static void checkWrite(final String opStr) {
        checkWrite(new WorkloadOperation(opStr), opStr);
    }

    private static void checkWrite(final WorkloadOperation op, final String opStr) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        op.write(pw);
        assertEquals (op, new WorkloadOperation(opStr));
    }

    @Test
    public void write() {
        checkWrite (deleteOpStr);
        checkWrite (finalDeleteOpStr);
        checkWrite (emptyOpStr);
        checkWrite (insertOpStr);
        checkWrite (queryOpStr);
        checkWrite (deletingInsertionOpStr);
    }

    @Test
    public void constructorWrite() {
        checkWrite (new WorkloadOperation(WorkloadOperation.OperationType.DELETE, 6,
                new SpatialExtent(5.0, 6.0, 7.0, 8.0)), deleteOpStr);
        checkWrite (new WorkloadOperation(WorkloadOperation.OperationType.FINAL_DELETE, 7,
                new SpatialExtent(10.0, 11.0, 12.0, 13.0)), finalDeleteOpStr);
        checkWrite (new WorkloadOperation(WorkloadOperation.OperationType.EMPTY, 7,
                new SpatialExtent(11.0, 12.0, 13.0, 14.0)), emptyOpStr);
        checkWrite (new WorkloadOperation(WorkloadOperation.OperationType.INSERT, 5,
                new SpatialExtent(1.0, 2.0, 3.0, 4.0)), insertOpStr);
        checkWrite (new WorkloadOperation(WorkloadOperation.OperationType.QUERY, 50,
                new SpatialExtent(4.0, 3.0, 2.0, 1.0)), queryOpStr);
        checkWrite (new WorkloadOperation(WorkloadOperation.OperationType.DELETING_INSERT, 7,
                new SpatialExtent(1.0, 2.0, 3.0, 4.0)), deletingInsertionOpStr);
    }

    @Test
    public void getSpatialExtent() {
        final WorkloadOperation op = new WorkloadOperation(insertOpStr);
        assertEquals (insertOpExtent, op.getSpatialExtent());
    }

    @Test
    public void distance() {
        final WorkloadOperation op1 = new WorkloadOperation(insertOpStr);
        final WorkloadOperation op2 = new WorkloadOperation(deleteOpStr);
        assertEquals (op1.distance(op2),
                      op1.getSpatialExtent().center().distance(op2.getSpatialExtent().center()), 0.0001);
    }

    @Test
    public void invalidOp() {
        try {
            final WorkloadOperation op = new WorkloadOperation(invalidOpStr);
            fail();
            op.getId();
        }
        catch (IllegalArgumentException ignored) { }
    }
}
