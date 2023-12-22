package aau.bufferedIndexes;

/**
 * An either insertion or deletion
 */
public enum OperationType {
    INSERTION,
    DELETION;

    /**
     * Returns the opposite operation type
     * @return the opposite operation type
     */
    public OperationType opposite() {
        return this == DELETION ? INSERTION : DELETION;
    }

    /**
     * Checks if this is a deletion
     * @return <code>true</code> if deletion.
     */
    public boolean isDeletion() {
        return this == DELETION;
    }

    /**
     * Checks if this is a deletion
     * @return <code>true</code> if insertion.
     */
    public boolean isInsertion() {
        return this == INSERTION;
    }
}
