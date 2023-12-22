package aau.bufferedIndexes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit testsuite for OperationTypeStat class
 */
public class OperationTypeStatTest {

    private static final String name1 = "Counter Name";
    private static final String name2 = "Another Counter Name";

    @Test
    public void counterName() {
        OperationTypeStat opTypeStat1 = new OperationTypeStat(name1);
        OperationTypeStat opTypeStat2 = new OperationTypeStat(name2);

        assertTrue (opTypeStat1.toString().contains(name1));
        assertTrue (opTypeStat2.toString().contains(name2));
    }

    @Test
    public void zeroCounters() {
        OperationTypeStat s = new OperationTypeStat(name1);
        assertEquals(s.getInsertions(), 0);
        assertEquals(s.getDeletions(), 0);
    }

    @Test
    public void simpleCounters() {
        OperationTypeStat s = new OperationTypeStat(name1);

        UpdateOperation op = new UpdateOperation(OperationType.INSERTION);
        UpdateOperation op2 = new UpdateOperation(OperationType.DELETION);

        s.register(op);
        s.register(op2);
        s.register(op);
        s.register(op2);
        s.register(op);

        assertEquals(s.getInsertions(), 3);
        assertEquals(s.getDeletions(), 2);
    }
}
