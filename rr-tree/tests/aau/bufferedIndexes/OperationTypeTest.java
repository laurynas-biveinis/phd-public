package aau.bufferedIndexes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for OperationType
 */
public class OperationTypeTest {

    @Test
    public void opposite ()
    {
        assertEquals (OperationType.DELETION, OperationType.INSERTION.opposite());
        assertEquals (OperationType.INSERTION, OperationType.DELETION.opposite());
    }

    @Test
    public void isInsertion ()
    {
        assertEquals (true, OperationType.INSERTION.isInsertion());
        assertEquals (false, OperationType.DELETION.isInsertion());
    }

    @Test
    public void isDeletion ()
    {
        assertEquals (true, OperationType.DELETION.isDeletion());
        assertEquals (false, OperationType.INSERTION.isDeletion());
    }
}
