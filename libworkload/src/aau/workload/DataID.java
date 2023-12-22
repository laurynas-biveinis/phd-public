/*
     Copyright (C) 2007, 2008, 2009 Laurynas Biveinis

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
package aau.workload;

import xxl.core.io.Convertable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * An object id class, basically a wrapped integer.  Differs from @see{java.lang.Integer} in that the objects
 * of this class are mutable.  Defined to have an usable ID for @see{xxl.core.spatial.KPE} objects. 
 */
public final class DataID implements Convertable, Cloneable {    
    private int ID;

    public DataID(final int newID) {
        ID = newID;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public DataID() {
        ID = -1;
    }

    public int getID() {
        return ID;
    }

    public boolean equals (final Object other) {
        return (other instanceof DataID) && ((DataID) other).ID == ID;
    }

    public int hashCode () {
        return ID;
    }

    public String toString() {
        return Integer.toString(ID);
    }

    public void read(final DataInput dataInput) throws IOException {
        ID = dataInput.readInt();
    }

    public void write(final DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(ID);
    }
}
