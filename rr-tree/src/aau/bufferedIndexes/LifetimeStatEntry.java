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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * A single of entry of operation lifetime (number of EmptyBuffers survived) statistics 
 */
public final class LifetimeStatEntry {

    /**
     * Number of times the operations were returned back to buffer from each disk tree level.
     */
    private final ArrayList<Integer> timesReturnedToBuffer = new ArrayList<>();

    /**
     * Returns numbers of times the operations were returned back to buffer from each disk tree level
     * @return list of numbers of times per level
     */
    public List<Integer> getTimesReturnedToBuffer() {
        return Collections.unmodifiableList(timesReturnedToBuffer);
    }

    /**
     * Adds to stat data items
     * @param toAdd another stat data item, whose information should be merged with the current one
     */
    public void add (final LifetimeStatEntry toAdd) {
        final ListIterator<Integer> l1 = timesReturnedToBuffer.listIterator();
        final ListIterator<Integer> l2 = toAdd.timesReturnedToBuffer.listIterator();
        while (l1.hasNext() && l2.hasNext()) {
            l1.set(l1.next() + l2.next());
        }
        if (l2.hasNext()) {
            timesReturnedToBuffer.addAll(timesReturnedToBuffer.size(),
                    toAdd.getTimesReturnedToBuffer().subList(timesReturnedToBuffer.size(),
                            toAdd.getTimesReturnedToBuffer().size()));
        }
    }

    /**
     * Register return to the buffer of the operation tracked by this stat item
     * @param level level of the disk tree the operation has reached
     */
    public void returnToBuffer(final int level) {
        for (int i = timesReturnedToBuffer.size(); i <= level; i++)
            timesReturnedToBuffer.add(0);
        timesReturnedToBuffer.set(level, timesReturnedToBuffer.get(level) + 1);
    }

}
