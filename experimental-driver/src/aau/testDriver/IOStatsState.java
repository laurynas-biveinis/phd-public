/*
     Copyright (C) 2010, 2011, 2012 Laurynas Biveinis

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
package aau.testDriver;

import xxl.core.collections.containers.CounterContainer;

/**
 * Takes an I/O statistics snapshot of a specified counter container and compares it with changes at a later time.
 */
public class IOStatsState {

    /**
     * The counter container that this object is tracking
     */
    private final CounterContainer container;

    /**
     * Reads at the object construction time
     */
    private int r;

    /**
     * Inserts at the object construction time
     */
    private int i;

    /**
     * Updates at the object construction time
     */
    private int u;

    /**
     * Deletes at the object construction time
     */
    private int d;

    /**
     * Creates a new I/O statistics snapshot of a specified counter container.
     *
     * @param container the container to take I/O statistics snapshot of
     */
    public IOStatsState (final CounterContainer container) {
        this.container = container;
        r = container.gets;
        i = container.inserts;
        u = container.updates;
        d = container.removes;
    }

    /**
     * Gets an additional number of reads that have happened since this object was constructed.
     *
     * @return an additional number of reads that have happened since this object was constructed
     */
    public int getReadDelta() {
        checkState();
        return container.gets - r;
    }

    /**
     * Gets an additional number of updates that have happened since this object was constructed.
     *
     * @return an additional number of updates that have happened since this object was constructed
     */
    public int getUpdateDelta() {
        checkState();
        return container.updates - u;
    }

    public int getInsertDelta() {
        checkState();
        return container.inserts - i;
    }
    
    public int getRemoveDelta() {
        checkState();
        return container.removes - d;
    }
    
    /**
     * Gets an additional number of writes (updates, inserts and removes) that have happened since this object was
     * constructed.
     *
     * @return an additional number of writes (updates, inserts and removes) that have happened since this object was
     *         constructed
     */
    public int getWriteDelta() {
        checkState();
        return getUpdateDelta() + getInsertDelta() + getRemoveDelta();
    }

    public int getDelta() {
        checkState();
        return getReadDelta() + getWriteDelta();
    }

    /**
     * Checks if inserts or removes have happened since this object was constructed.
     *
     * @return <code>true</code> if inserts or removes have happened since this object was constructed,
     *         <code>false</code> otherwise
     */
    public boolean insertsOrRemovesHappened() {
        checkState();
        return (container.inserts != i) || (container.removes != d);
    }

    /**
     * Checks if writes (updates, inserts or removes) have happened since this object was constructed.
     *
     * @return <code>true</code> if writes (updates, inserts or removes) have happened since this object was
     *         constructed, <code>false</code> otherwise
     */
    public boolean writesHappened() {
        checkState();
        return insertsOrRemovesHappened() || (container.updates != u);
    }

    /**
     * Registers a read revert event in the parent container by reverting the read value at the snapshot time.
     * @param r reads to revert
     */
    public void revertReads(final int r) {
        this.r -= r;
    }

    /**
     * Registers a remove revert event in the parent container by reverting the remove value at the snapshot time.
     *
     * @param d removes to revert
     */
    public void revertRemoves(final int d) {
        this.d -= d;
    }

    /**
     * Registers an update revert event in the parent container by reverting the update value at the snapshot time.
     *
     * @param u updates to revert
     */
    public void revertUpdates(final int u) {
        this.u -= u;
    }

    /**
     * Registers an insert revert event in the parent container by reverting the insert value at the snapshot time.
     *
     * @param i inserts to revert
     */
    public void revertInserts(final int i) {
        this.i -= i;
    }

    /**
     * Checks the consistency of statistics by not allowing negative changes in the counters.
     */
    private void checkState() {
        if (container.gets < r)
            throw new IllegalStateException("Negative gets since IOStatsState object construction (current = " +
                container.gets + ", previous = " + r);
        if (container.inserts < i)
            throw new IllegalStateException("Negative inserts since IOStatsState object construction (current = " +
                container.inserts + ", previous = " + i);
        if (container.updates < u)
            throw new IllegalStateException("Negative updates since IOStatsState object construction (current = " +
                container.updates + ", previous = " + u);
        if (container.removes < d)
            throw new IllegalStateException("Negative removes since IOStatsState object construction (current = " +
                container.removes + ", previous = " + d);
    }

    /**
     * Tests given object for equality, which is defined as equal counter values.
     * @param other an object to compare with
     * @return @code{true} if equal, @code{false} otherwise
     */
    public boolean equals(final Object other) {
        if (!(other instanceof IOStatsState))
            return false;
        if (other == this)
            return true;
        final IOStatsState o = (IOStatsState)other;
        return (r == o.r) && (i == o.i) && (u == o.u) && (d == o.d);
    }
}
