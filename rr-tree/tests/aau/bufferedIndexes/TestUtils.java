/*
     Copyright (C) 2007, 2008, 2009, 2010, 2011, 2012 Laurynas Biveinis

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

import aau.bufferedIndexes.diskTrees.IRRTreeDiskNode;
import aau.bufferedIndexes.diskTrees.IRRTreeIndexEntry;
import aau.bufferedIndexes.diskTrees.RRDiskDataTree;
import aau.bufferedIndexes.objectTracers.NullObjectTracer;
import aau.bufferedIndexes.pushDownStrategies.AbstractPushDownThreshold;
import aau.workload.DataID;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;
import xxl.core.spatial.KPE;
import xxl.core.spatial.points.DoublePoint;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Various things to facilitate unit testing
 */
public class TestUtils {

    private TestUtils() { }

    /**
     * Standard function to get descriptor from the data object (KPE assumed)
     */
    public static final Function<KPE, Descriptor> GET_DESCRIPTOR = new Function<KPE, Descriptor>() {
        public Descriptor invoke(final KPE o) {
            return (Descriptor)(o).getData();
        }
    };

    public static final Function<KPE, Descriptor> NULL_GET_DESCRIPTOR = new Function<KPE, Descriptor>() {
        public Descriptor invoke(final KPE o) {
            return null;
        }
    };

    /**
     * Standard function to get ID from the data object (KPE assumed)
     */
    public static final Function<KPE, DataID> GET_ID = new Function<KPE, DataID>() {
        public DataID invoke (final KPE o) {
            return (DataID)o.getID();
        }
    };

    private static RRDiskDataTree<KPE> dummyDataTree = null;

    public static UpdateTree.Entry<KPE> makeOperation(final double x1, final double y1, final double x2,
                                                      final double y2, final OperationType operationType) {
        return new UpdateTree.Entry<>(TestUtils.makeKPE(x1, y1, x2, y2), operationType);
    }

    public static UpdateTree.Entry<KPE> makeOperation(final OperationType operationType, final int id, final double x1,
                                                      final double y1, final double x2, final double y2) {
        return new UpdateTree.Entry<>(TestUtils.makeKPE(id, x1, y1, x2, y2), operationType);
    }

    @SafeVarargs
    public static Collection<UpdateTree.Entry<KPE>> makeOpList(final UpdateTree.Entry<KPE>... ops) {
        final Collection<UpdateTree.Entry<KPE>> result = new ArrayList<>(ops.length);
        result.addAll(Arrays.asList(ops));
        return result;
    }

    private static int nextId = 1;

    public static IRRTreeIndexEntry<KPE> makeIndexEntry(final Descriptor descriptor, final int parentLevel) {
        initDummyTree();
        final IRRTreeIndexEntry<KPE> result = dummyDataTree.createIndexEntry(parentLevel);
        result.initialize(descriptor);
        result.initialize(nextId);
        nextId++;
        return result;
    }

    public static IRRTreeDiskNode<KPE> makeNode(final int level) {
        initDummyTree();
        return dummyDataTree.createNode(level);
    }

    public static <T extends Convertable> void addEntry(final IRRTreeDiskNode<T> node,
                                                        final IRRTreeIndexEntry<T> entry) {
        node.getNonLeafNodeEntries().add(entry);
    }

    public static Descriptor getDescriptor(final Object entry) {
        initDummyTree();
        return dummyDataTree.descriptor(entry);
    }

    private static void initDummyTree() {
        if (dummyDataTree == null) {
            dummyDataTree = new RRDiskDataTree<>();
            dummyDataTree.initialize(TestUtils.GET_ID, TestUtils.GET_DESCRIPTOR, null, 50, 100,
                    new NullObjectTracer<KPE>());
        }
    }

    public static void checkPushDownResults(final int bgsize, final int pdgsize,
                                     final PushDownAndBufferGroups<KPE> result) {
        org.junit.Assert.assertEquals(bgsize, result.getBufferGroups().size());
        org.junit.Assert.assertEquals(pdgsize, result.getPushDownGroups().size());
    }

    public static void checkPushDownThresholds(final int sats, final int unsats,
                                               final AbstractPushDownThreshold strategy) {
        org.junit.Assert.assertEquals(sats, strategy.getThresholdSatisfactions());
        org.junit.Assert.assertEquals(unsats, strategy.getThresholdUnsatisfactions());
    }

    private static IndexEntryOpGroupMap<KPE> makeGrouping(final OperationType operationType, final int... groupSizes) {
        final IndexEntryOpGroupMap<KPE> result = new IndexEntryOpGroupMap<>();
        int uniqPos = 0;
        for (final int groupSize: groupSizes) {
            final IRRTreeIndexEntry<KPE> indexEntry
                    = makeIndexEntry(makeDescriptor(0.0, 0.0, 1.0, 1.0), 0);
            for (int i = 0; i < groupSize; i++) {
                result.addEntry(indexEntry, makeOperation(uniqPos, uniqPos, uniqPos + 1.0, uniqPos + 1.0,
                        operationType));
                uniqPos++;
            }
        }
        return result;
    }

    public static IndexEntryOpGroupMap<KPE> makeDeleteGrouping(final int... groupSizes) {
        return makeGrouping(OperationType.DELETION, groupSizes);
    }

    public static IndexEntryOpGroupMap<KPE> makeGrouping(final int... groupSizes) {
        return makeGrouping(OperationType.INSERTION, groupSizes);
    }

    public static Rectangle makeRectangle(final double x1, final double y1, final double x2, final double y2) {
        return new DoublePointRectangle(new DoublePoint(new double[]{x1, y1}), new DoublePoint(new double[]{x2, y2}));
    }

    public static Descriptor makeDescriptor(final double x1, final double y1, final double x2, final double y2) {
        return makeRectangle(x1, y1, x2, y2);
    }

    public static KPE makeKPE(final double x1, final double y1, final double x2, final double y2) {
        return new KPE(makeRectangle(x1, y1, x2, y2));
    }

    public static KPE makeKPE(final int id, final double x1, final double y1, final double x2, final double y2) {
        final KPE result = makeKPE (x1, y1, x2, y2);
        result.setID(new DataID(id));
        return result;
    }
}
