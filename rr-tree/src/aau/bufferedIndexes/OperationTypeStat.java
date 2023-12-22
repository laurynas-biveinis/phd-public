package aau.bufferedIndexes;

/**
 * Statistical counter that registers types of incoming operations.
 */
public class OperationTypeStat {

    /**
     * A description of this counter
     */
    private final String statName;

    private int deletions = 0;
    private int insertions = 0;

    /**
     * Creates new counter
     * @param statName description of this counter
     */
    public OperationTypeStat (final String statName) {
        this.statName = statName;
    }

    public int getDeletions () {
        return deletions;
    }

    public int getInsertions () {
        return insertions;
    }

    /**
     * Registers a new operation for this statistics
     * @param op operation to register
     */
    public void register (final UpdateOperation op) {
        if (op.isDeletion())
            deletions++;
        else
            insertions++;
    }

    /**
     * Returns a string representation of this counter
     * @return string with counter summary
     */
    public String toString () {
        return statName + ": i = " + insertions + " d = " + deletions;
    }
}
