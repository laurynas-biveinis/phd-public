/*
     Copyright (C) 2009, 2010, 2011, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.diskTrees;

import aau.bufferedIndexes.TreeTester;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xxl.core.spatial.KPE;

import static org.junit.Assert.assertTrue;

@RunWith(JMock.class)
public class CachingStrategyTest extends TreeTester {

    private final Mockery context = new JUnit4Mockery();

    private IRRDiskTree<KPE> tree;
    private IRRTreeDiskNode<KPE> node;
    private IRRTreeIndexEntry<KPE> indexEntry;

    @Before
    public void setup() {
        //noinspection unchecked
        tree = context.mock(IRRDiskTree.class);
        //noinspection unchecked
        node = context.mock(IRRTreeDiskNode.class);
        //noinspection unchecked
        indexEntry = context.mock(IRRTreeIndexEntry.class);

        context.checking(new Expectations(){{
            atLeast(1).of(tree).height(); will(returnValue(2));
        }});
    }

    @Test
    public void doNotCacheNonRootNode() {
        context.checking(new Expectations(){{
            oneOf(node).level(); will(returnValue(0));
        }});

        final CachingStrategy s = new CachingStrategy(tree);
        assertTrue (!s.shouldBeFixed(node));
    }

    @Test
    public void doNotCacheNonRootNodeByIndexEntry()  {
        context.checking(new Expectations(){{
            oneOf(indexEntry).level(); will(returnValue(0));
        }});

        final CachingStrategy s = new CachingStrategy(tree);
        assertTrue (!s.shouldBeFixed(indexEntry));
    }

    @Test
    public void cacheRootNode() {
        context.checking(new Expectations(){{
            oneOf(node).level(); will(returnValue(1));
        }});

        final CachingStrategy s = new CachingStrategy(tree);
        assertTrue (s.shouldBeFixed(node));
    }

    @Test
    public void cacheRootNodeByIndexEntry() {
        context.checking(new Expectations(){{
            oneOf(indexEntry).level(); will(returnValue(1));
        }});

        final CachingStrategy s = new CachingStrategy(tree);
        assertTrue (s.shouldBeFixed(indexEntry));

    }
}