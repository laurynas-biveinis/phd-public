/*
     Copyright (C) 2011 Laurynas Biveinis

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

/**
 * A callback interface to be notified once a specified number of I/Os happen in the container.
 */
public interface IOIntervalEvent {

    /**
     * Is called once a specified number of I/Os happens in the container.
     */
    public void notifyIoInterval();
}
