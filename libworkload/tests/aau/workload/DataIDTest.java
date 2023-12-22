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

/**
 *  Testsuite for DataID class.
 **/

import org.junit.Test;
import xxl.core.functions.Function;
import xxl.core.io.converters.ConvertableConverter;

import java.io.*;

import static org.junit.Assert.*;

public class DataIDTest {

    private static final int ID_VAL = 453;

    @Test
    public void dataID() {
        assertEquals (new DataID(289), new DataID(289));
    }

    @Test
    public void defaultDataID() {
        assertEquals (new DataID(), new DataID());
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        final DataID first = new DataID(459);
        assertEquals (first, first.clone());
    }

    @Test
    public void getID() {
        final DataID dataid = new DataID(ID_VAL);
        assertEquals (ID_VAL, dataid.getID());
    }

    @Test
    public void testEquals() {
        final DataID obj = new DataID(ID_VAL);
        assertEquals (obj, new DataID(ID_VAL));
        assertFalse (obj.equals(new DataID(ID_VAL - 1)));
        assertFalse (obj.equals(new DataID()));
        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse (obj.equals(ID_VAL));
    }

    @Test
    public void testHashCode() {
        final DataID obj = new DataID(ID_VAL);
        final DataID equalObj = new DataID(ID_VAL);
        final DataID nonEqualObj = new DataID(ID_VAL + 5);
        assertEquals (obj.hashCode(), equalObj.hashCode());
        assertNotSame(nonEqualObj.hashCode(), obj.hashCode());
    }

    @Test
    public void testToString() {
        final DataID obj = new DataID(ID_VAL);
        assertEquals (obj.toString(), Integer.toString(ID_VAL));
    }

    private static final Function<Object, DataID> dataIdFactory = new Function<Object, DataID>() {
        public DataID invoke() {
            return new DataID();
        }
    };

    @Test
    public void readAndWrite() throws IOException {
        final ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        final ConvertableConverter<DataID> converter = new ConvertableConverter<>(dataIdFactory);

        final DataID before = new DataID(ID_VAL);
        converter.write(new DataOutputStream(outputBytes), before);

        final ByteArrayInputStream inputBytes = new ByteArrayInputStream(outputBytes.toByteArray());
        final DataID after = converter.read(new DataInputStream(inputBytes));
        assertEquals (before, after);
    }
}
