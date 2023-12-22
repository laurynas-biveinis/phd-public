/*
     Copyright (C) 2007, 2008, 2010, 2012 Laurynas Biveinis

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

import xxl.core.io.Convertable;

/**
 * Encapsulates information about groups that should be pushed down and groups which should be returned back to
 * buffer.  Returned by PushDownGroupsStrategy.
 */
public final class PushDownAndBufferGroups<E extends Convertable> {
    private final IndexEntryOpGroupMap<E> groupsForPushDown;
    private final IndexEntryOpGroupMap<E> groupsForBuffer;

    public PushDownAndBufferGroups() {
        this.groupsForBuffer = new IndexEntryOpGroupMap<>();
        this.groupsForPushDown = new IndexEntryOpGroupMap<>();
    }

    public PushDownAndBufferGroups(final IndexEntryOpGroupMap<E> groupsForPushDown,
                                   final IndexEntryOpGroupMap<E> groupsForBuffer) {
        this.groupsForPushDown = groupsForPushDown;
        this.groupsForBuffer = groupsForBuffer;
    }

    public IndexEntryOpGroupMap<E> getPushDownGroups() {
        return groupsForPushDown;
    }

    public IndexEntryOpGroupMap<E> getBufferGroups() {
        return groupsForBuffer;
    }
}
