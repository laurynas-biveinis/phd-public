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

import java.util.*;

/**
 * Statistics for operation entry lifetimes in the buffer.
 */
public class OpLifetimeStats {

    /**
     * Data for a single set of operations that have survived the same number of buffer emptyings.
     */
    public static final class TotalsForEB {
        /**
         * Total number of operations sharing these values.
         */
        private int numberOfOps = 0;

        /**
         * The stat values for this set of operations
         */
        private final LifetimeStatEntry stats = new LifetimeStatEntry();

        /**
         * Add one more operation sharing this stat entry.
         */
        public void addOperation() {
            numberOfOps++;
        }

        public void addOperation(final LifetimeStatEntry toAdd) {
            addOperation();
            stats.add(toAdd);
        }

        /**
         * Returns number of operations sharing this statistic data item object
         * @return number of operations sharing this statistic data item object
         */
        public int getNumberOfOps() {
            return numberOfOps;
        }

        /**
         * Returns numbers of times the operations were returned back to buffer from each disk tree level
         * @return list of numbers of times per level
         */
        public List<Integer> getTimesReturnedToBuffer() {
            return stats.getTimesReturnedToBuffer();
        }
    }

    /**
     * Statistics for operations entry lifetimes.  Map key is the number of buffer emptyings on which the operation was
     * completed, 0 meaning annihilation.
     */
    private final SortedMap<Integer, TotalsForEB> stats = new TreeMap<>();

    /**
     * Returns the read-only set of all statistical data mappings.
     * @return the read-only set of mappings between keys-numbers of buffer emptyings required to complete the
     * operations-and their values, the TotalsForEB objects
     */
    public Set<Map.Entry<Integer, TotalsForEB>> entrySet() {
        return Collections.unmodifiableSet(stats.entrySet());
    }

    /**
     * Add one more operation to the statistics upon its completion
     * @param ebOnWhichEmptied number of EB, on which the operation was completed
     */
    public void updateLifetime(final int ebOnWhichEmptied) {
        final TotalsForEB totalsForEB
                = stats.containsKey(ebOnWhichEmptied) ? stats.get(ebOnWhichEmptied) : new TotalsForEB();
        totalsForEB.addOperation();
        stats.put(ebOnWhichEmptied, totalsForEB);
    }

    /**
     * Add one more operation with EB info to the statistics upon its completion
     * @param ebOnWhichEmptied number of EB, on which the operation was completed
     * @param entry stats for the operation
     */
    public void updateLifetime(final int ebOnWhichEmptied, final LifetimeStatEntry entry) {
        final TotalsForEB totalsForEB
                = stats.containsKey(ebOnWhichEmptied) ? stats.get(ebOnWhichEmptied) : new TotalsForEB();
        totalsForEB.addOperation(entry);
        stats.put(ebOnWhichEmptied, totalsForEB);
    }
}
