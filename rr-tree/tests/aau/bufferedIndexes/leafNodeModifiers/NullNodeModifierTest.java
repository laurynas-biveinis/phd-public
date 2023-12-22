/*
     Copyright (C) 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.leafNodeModifiers;

import aau.bufferedIndexes.RRTreeLeafPiggybackingInfo;
import org.junit.Before;
import org.junit.Test;
import xxl.core.spatial.KPE;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for NullModeModifier class
 */
public class NullNodeModifierTest {

    private NullModeModifier<KPE> m;

    @Before
    public void setUp() {
        m = new NullModeModifier<>();
    }

    @Test
    public void modify() {
        boolean result = m.modify(null, true, 0.0, new RRTreeLeafPiggybackingInfo());
        assertFalse (result);
    }

    @Test
    public void finalizeMods() {
        m.finalizeModifications(null);
        assertTrue (true);
    }
}
