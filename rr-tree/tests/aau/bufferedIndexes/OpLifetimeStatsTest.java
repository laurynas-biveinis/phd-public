/*
     Copyright (C) 2009, 2012 Laurynas Biveinis

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

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for OpLifeTimeStats
 */
public class OpLifetimeStatsTest {

    @Test
    public void entrySet() {
        final OpLifetimeStats stats = new OpLifetimeStats();
        final Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> entrySet = stats.entrySet();
        assertEquals (0, entrySet.size());
        try {
            entrySet.clear();
            fail ("Returned entry set should be read-only");
        }
        catch (UnsupportedOperationException ignored) { }
    }

    @Test
    public void updateLifetimeSimple() {
        final OpLifetimeStats stats = new OpLifetimeStats();
        assertEquals (0, stats.entrySet().size());

        stats.updateLifetime(0);
        checkLifetimeStats(stats, 1, 0, 1, 0);

        stats.updateLifetime(1);
        checkLifetimeStats(stats, 2, 1, 1, 0);

        stats.updateLifetime(0);
        checkLifetimeStats(stats, 2, 0, 2, 0);
    }

    @Test
    public void updateLifettimeWithStats() {
        final OpLifetimeStats stats = new OpLifetimeStats();
        LifetimeStatEntry entry = new LifetimeStatEntry();
        entry.returnToBuffer(1);
        stats.updateLifetime(1, entry);
        checkLifetimeStats(stats, 1, 1, 1, 2, 0, 1);

        entry = new LifetimeStatEntry();
        entry.returnToBuffer(2);
        stats.updateLifetime(2, entry);
        checkLifetimeStats(stats, 2, 1, 1, 2, 0, 1);
        checkLifetimeStats(stats, 2, 2, 1, 3, 0, 0, 1);

        entry = new LifetimeStatEntry();
        entry.returnToBuffer(2);
        stats.updateLifetime(1, entry);
        checkLifetimeStats(stats, 2, 1, 2, 3, 0, 1, 1);
        checkLifetimeStats(stats, 2, 2, 1, 3, 0, 0, 1);
    }

    private static void checkLifetimeStats(final OpLifetimeStats stats, final int totalSize, final int numOfEbs,
                                           final int numOfOps, final int returnedToBufSize,
                                           final Integer... levelsReached) {
        final Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> entrySet = stats.entrySet();
        final List<Integer> expectedLevelsReached = new LinkedList<>(Arrays.asList(levelsReached));
        assertEquals (totalSize, entrySet.size());
        for (final Map.Entry<Integer, OpLifetimeStats.TotalsForEB> setItem : entrySet) {
            if (setItem.getKey() == numOfEbs) {
                final OpLifetimeStats.TotalsForEB totalsForEB = setItem.getValue();
                assertEquals (numOfOps, totalsForEB.getNumberOfOps());
                assertEquals (returnedToBufSize, totalsForEB.getTimesReturnedToBuffer().size());
                assertEquals (expectedLevelsReached, totalsForEB.getTimesReturnedToBuffer());
                break;
            }
        }
    }
}
