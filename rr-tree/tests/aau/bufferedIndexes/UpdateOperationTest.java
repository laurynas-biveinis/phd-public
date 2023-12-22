package aau.bufferedIndexes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for UpdateOperation
 */
public class UpdateOperationTest {

    @Test
    public void getOperationType () {
        UpdateOperation op = new UpdateOperation(OperationType.INSERTION);
        UpdateOperation op2 = new UpdateOperation(OperationType.DELETION);
        assertEquals (OperationType.INSERTION, op.getOperationType());
        assertEquals (OperationType.DELETION, op2.getOperationType());
    }

    @Test
    public void isDeletion () {
        UpdateOperation op = new UpdateOperation(OperationType.INSERTION);
        UpdateOperation op2 = new UpdateOperation(OperationType.DELETION);
        assertFalse (op.isDeletion());
        assertTrue (op2.isDeletion());
    }

    @Test
    public void isInsertion () {
        UpdateOperation op = new UpdateOperation(OperationType.INSERTION);
        UpdateOperation op2 = new UpdateOperation(OperationType.DELETION);
        assertTrue (op.isInsertion());
        assertFalse (op2.isInsertion());
    }

    @Test
    public void testEquals () {
        UpdateOperation op = new UpdateOperation(OperationType.INSERTION);
        UpdateOperation op2 = new UpdateOperation(OperationType.DELETION);
        UpdateOperation op3 = new UpdateOperation(OperationType.INSERTION);
        String other = "blah";
        assertFalse (op.equals(op2));
        assertTrue (op.equals(op3));
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse (op.equals(other));
        assertFalse (op2.equals(op));
        assertTrue (op3.equals(op));
    }

    @Test
    public void testHashCode () {
        UpdateOperation op = new UpdateOperation(OperationType.INSERTION);
        UpdateOperation op2 = new UpdateOperation(OperationType.INSERTION);
        assertEquals (op.hashCode(), op2.hashCode());
    }

    @Test
    public void testToString () {
        UpdateOperation op = new UpdateOperation(OperationType.INSERTION);
        UpdateOperation op2 = new UpdateOperation(OperationType.DELETION);
        assertTrue (op.toString().contains(OperationType.INSERTION.name()));
        assertTrue (op2.toString().contains(OperationType.DELETION.name()));
    }
}
