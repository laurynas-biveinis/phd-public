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
package aau.bufferedIndexes;

import org.junit.Before;
import org.junit.Test;
import xxl.core.spatial.KPE;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for RRTreeStats class
 */
public class RRTreeStatsTest {

    private RRTreeStats<KPE> stats;

    private final KPE d = TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0);
    private final UpdateTree.Entry<KPE> op = new UpdateTree.Entry<>(d, OperationType.INSERTION);

    @Before
    public void setUp() {
        stats = new RRTreeStats<>();
    }

    @Test
    public void groupUpdate() {
        assertEquals (0, stats.getGroupUpdateInvocations());
        stats.registerGroupUpdate();
        assertEquals (1, stats.getGroupUpdateInvocations());
        stats.registerGroupUpdate();
        assertEquals (2, stats.getGroupUpdateInvocations());
    }

    @Test
    public void failedEmptying() {
        assertEquals (0, stats.getFailedEmptyings());
        stats.registerFailedEmptying();
        assertEquals (1, stats.getFailedEmptyings());
        stats.registerFailedEmptying();
        assertEquals (2, stats.getFailedEmptyings());
    }
    
    @Test
    public void groupUpdateRestart() {
        assertEquals (0, stats.getGroupUpdateRestarts());
        stats.registerGroupUpdateRestart();
        assertEquals (1, stats.getGroupUpdateRestarts());
        stats.registerGroupUpdateRestart();
        assertEquals (2, stats.getGroupUpdateRestarts());
    }

    @Test
    public void singleEntryNode() {
        assertEquals (0, stats.getSingleEntryNodes());
        stats.registerSingleEntryNode();
        assertEquals (1, stats.getSingleEntryNodes());
        stats.registerSingleEntryNode();
        assertEquals (2, stats.getSingleEntryNodes());
    }

    @Test
    public void nonLeafUpdate() {
        assertEquals (0, stats.getNonLeafNodeUpdates());
        stats.registerNonLeafNodeUpdate();
        assertEquals (1, stats.getNonLeafNodeUpdates());
        stats.registerNonLeafNodeUpdate();
        assertEquals (2, stats.getNonLeafNodeUpdates());
    }

    @Test
    public void integrateChild() {
        assertEquals (0, stats.getIntegrateChildInvocations());
        stats.registerIntegrateChildInvocation();
        assertEquals (1, stats.getIntegrateChildInvocations());
        stats.registerIntegrateChildInvocation();
        assertEquals (2, stats.getIntegrateChildInvocations());
    }

    @Test
    public void emptyChild() {
        assertEquals (0, stats.getEmptyChildren());
        stats.registerEmptyChildrenSet();
        assertEquals (1, stats.getEmptyChildren());
        stats.registerEmptyChildrenSet();
        assertEquals (2, stats.getEmptyChildren());
    }
    
    @Test
    public void childReplacingParent() {
        assertEquals (0, stats.getChildrenReplacingParent());
        stats.registerChildReplacingParent();
        assertEquals (1, stats.getChildrenReplacingParent());
        stats.registerChildReplacingParent();
        assertEquals (2, stats.getChildrenReplacingParent());
    }
    
    @Test
    public void trivialChildIntegrations() {
        assertEquals (0, stats.getTrivialChildIntegrations());
        stats.registerTrivialChildIntegration();
        assertEquals (1, stats.getTrivialChildIntegrations());
        stats.registerTrivialChildIntegration();
        assertEquals (2, stats.getTrivialChildIntegrations());
    }
    
    @Test
    public void leafUpdate() {
        assertEquals (0, stats.getLeafNodeUpdates());
        stats.registerLeafNodeUpdate();        
        assertEquals (1, stats.getLeafNodeUpdates());
        stats.registerLeafNodeUpdate();        
        assertEquals (2, stats.getLeafNodeUpdates());
    }
    
    @Test
    public void insertSubtree() {
        assertEquals (0, stats.getInsertSubtreeInvocations());
        stats.registerInsertSubtreeInvocation();        
        assertEquals (1, stats.getInsertSubtreeInvocations());
        stats.registerInsertSubtreeInvocation();        
        assertEquals (2, stats.getInsertSubtreeInvocations());
    }
    
    @Test
    public void insertSubtreeRecursion() {
        assertEquals (0, stats.getInsertSubtreeRecursion());
        stats.registerInsertSubtreeRecursion();                
        assertEquals (1, stats.getInsertSubtreeRecursion());
        stats.registerInsertSubtreeRecursion();                
        assertEquals (2, stats.getInsertSubtreeRecursion());
    }

    @Test
    public void mergeSubtree() {
        assertEquals (0, stats.getMergeSubtreeInvocations());
        stats.registerMergeSubtreeInvocation();        
        assertEquals (1, stats.getMergeSubtreeInvocations());
        stats.registerMergeSubtreeInvocation();        
        assertEquals (2, stats.getMergeSubtreeInvocations());
    }
    
    @Test
    public void mergeSubtreeRecursion() {
        assertEquals (0, stats.getMergeSubtreeRecursion());
        stats.registerMergeSubtreeRecursion();                
        assertEquals (1, stats.getMergeSubtreeRecursion());
        stats.registerMergeSubtreeRecursion();                
        assertEquals (2, stats.getMergeSubtreeRecursion());
    }

    @Test
    public void nonPiggybackedDeletions() {
        assertEquals (0, stats.getTotalNonPiggybackedNodeSizeDecreasingOps());
        stats.registerNonPiggybackedNodeSizeDecreasingOps(5);
        assertEquals (5, stats.getTotalNonPiggybackedNodeSizeDecreasingOps());
        stats.registerNonPiggybackedNodeSizeDecreasingOps(1);
        assertEquals (6, stats.getTotalNonPiggybackedNodeSizeDecreasingOps());
    }

    @Test
    public void nonPiggybackedInsertions() {
        assertEquals (0, stats.getTotalNonPiggybackedNodeSizeIncreasingOps());
        stats.registerNonPiggybackedNodeSizeIncreasingOps(5);
        assertEquals (5, stats.getTotalNonPiggybackedNodeSizeIncreasingOps());
        stats.registerNonPiggybackedNodeSizeIncreasingOps(1);
        assertEquals (6, stats.getTotalNonPiggybackedNodeSizeIncreasingOps());
    }

    @Test
    public void lifeTimeStatsRemovedFromBufferDuringEB() {
        assertEquals (0, stats.getOpLifetimeStats().size());
        stats.registerOpLifetime(d, 0, 1, OperationType.INSERTION);
        stats.registerEmptyBuffer();
        stats.completeUpdateLifetime(op);
        stats.registerEndOfEmptyBuffer();
        checkForLifetime(stats.getOpLifetimeStats(), 0, 1, 0);
    }

    @Test
    public void lifeTimeStatsRemovedFromBufferAfterEB() {
        assertEquals (0, stats.getOpLifetimeStats().size());
        stats.registerOpLifetime(d, 0, 1, OperationType.INSERTION);
        stats.registerEmptyBuffer();
        stats.registerEndOfEmptyBuffer();
        stats.completeUpdateLifetime(op);
        checkForLifetime(stats.getOpLifetimeStats(), 1, 1, 0);
    }

    @Test
    public void lifeTimeStatsStayedInBuffer() {
        assertEquals (0, stats.getOpLifetimeStats().size());
        stats.registerOpLifetime(d, 4, 5, OperationType.INSERTION);
        stats.registerBufferLifetimes();
        final Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> lifeTimeStats = stats.getOpLifetimeStats();
        assertEquals (1, lifeTimeStats.size());
        checkForLifetime(lifeTimeStats, -1, 1, 0);
    }

    @Test
    public void lifeTimeStatsAnnihilation() {
        assertEquals (0, stats.getOpLifetimeStats().size());
        stats.registerOpLifetime(d, 2, 3, OperationType.DELETION);
        stats.registerOpLifetime(d, 3, 2, OperationType.INSERTION);
        final Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> lifeTimeStats = stats.getOpLifetimeStats();
        assertEquals (1, lifeTimeStats.size());
        checkForLifetime(lifeTimeStats, 0, 2, 0);
    }

    @Test
    public void lifeTimeStatsBackToBuffer() {
        assertEquals (0, stats.getOpLifetimeStats().size());
        stats.registerOpLifetime(d, 4, 5, OperationType.INSERTION);
        stats.registerEmptyBuffer();
        stats.registerBackToBufferOperation(0, op);
        stats.registerEndOfEmptyBuffer();
        stats.registerEmptyBuffer();
        stats.completeUpdateLifetime(op);
        stats.registerEndOfEmptyBuffer();
        final Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> lifeTimeStats = stats.getOpLifetimeStats();
        assertEquals (1, lifeTimeStats.size());
        checkForLifetime(lifeTimeStats, 1, 1, 1);
    }

    @Test
    public void lifeTimeStatsMultipleDeletionsBackToBuffer() {
        assertEquals (0, stats.getOpLifetimeStats().size());
        final UpdateTree.Entry<KPE> op = new UpdateTree.Entry<>(d, OperationType.DELETION);
        stats.registerOpLifetime(d, 4, 5, OperationType.DELETION);
        stats.registerEmptyBuffer();
        stats.registerBackToBufferOperation(0, op);
        stats.completeUpdateLifetime(op);
        stats.registerBackToBufferOperation(0, op);
        stats.registerEndOfEmptyBuffer();
        final Set<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> lifeTimeStats = stats.getOpLifetimeStats();
        assertEquals (1, lifeTimeStats.size());
        checkForLifetime(lifeTimeStats, 0, 1, 1);
    }

    private void checkForLifetime(final Iterable<Map.Entry<Integer, OpLifetimeStats.TotalsForEB>> lifeTimeStats,
                                  final int ebNum, final int numOps, final int backToBufSize) {
        boolean seenKey = false;
        for (Map.Entry<Integer, OpLifetimeStats.TotalsForEB> lifeTimeStat : lifeTimeStats) {
            if (lifeTimeStat.getKey() == ebNum) {
                seenKey = true;
                final OpLifetimeStats.TotalsForEB ebTotals = lifeTimeStat.getValue();
                assertEquals (numOps, ebTotals.getNumberOfOps());
                assertEquals (backToBufSize, ebTotals.getTimesReturnedToBuffer().size());
            }
        }
        assertTrue (seenKey);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        //noinspection unchecked
        RRTreeStats<KPE> statsCopy = (RRTreeStats<KPE>)stats.clone();
        assertEquals (0, stats.getGroupUpdateInvocations());
        assertEquals (0, statsCopy.getGroupUpdateInvocations());
        stats.registerGroupUpdate();
        assertEquals (1, stats.getGroupUpdateInvocations());
        assertEquals (0, statsCopy.getGroupUpdateInvocations());
    }

    @Test
    public void emptyingFrequency () {
        StatisticalData f = stats.getEmptyingFrequencies();
        assertEquals (0, f.size());
        stats.registerUpdate();
        stats.registerUpdate();
        stats.registerEmptyBuffer();
        stats.registerUpdate();
        stats.registerEmptyBuffer();
        stats.registerUpdate();
        stats.registerUpdate();
        stats.registerEmptyBuffer();
        f = stats.getEmptyingFrequencies();
        assertEquals (2, f.size());
        assertEquals (1, f.get(1));
        assertEquals (2, f.get(2));
    }

    @Test
    public void backToBufferGroupSizes () {
        Collection<StatisticalData> sizes = stats.getBackToBufferGroupSizes();
        assertEquals(0, sizes.size());
        stats.registerBackToBufferGroup(1, 10);
        stats.registerBackToBufferGroup(2, 20);
        stats.registerBackToBufferGroup(1, 30);
        stats.registerBackToBufferGroup(2, 30);
        stats.registerBackToBufferGroup(1, 10);
        stats.registerBackToBufferGroup(0, 5);
        sizes = stats.getBackToBufferGroupSizes();
        assertEquals(3, sizes.size());
        Iterator<StatisticalData> itr = sizes.iterator();
        StatisticalData s = itr.next();
        assertEquals(1, s.size());
        assertEquals(1, s.get(5));
        s = itr.next();
        assertEquals(2, s.size());
        assertEquals(2, s.get(10));
        assertEquals(1, s.get(30));
        s = itr.next();
        assertEquals(2, s.size());
        assertEquals(1, s.get(20));
        assertEquals(1, s.get(30));
    }

    @Test
    public void groupSizes () {
        StatisticalData globalGroupSizes = stats.getGlobalGroupSizeStatistics();
        List<StatisticalData> groupSizes = stats.getGroupSizeStatistics();
        List<StatisticalData> iGroupSizes = stats.getInsertionOnlyGroupSizes();
        assertEquals (0, globalGroupSizes.size());
        assertEquals (0, groupSizes.size());
        assertEquals (0, iGroupSizes.size());

        final UpdateTree.Entry<KPE> e1 = TestUtils.makeOperation(1.0, 1.0, 2.0, 2.0, OperationType.INSERTION);
        final UpdateTree.Entry<KPE> e2 = TestUtils.makeOperation(2.0, 2.0, 3.0, 3.0, OperationType.DELETION);
        final UpdateTree.Entry<KPE> e3 = TestUtils.makeOperation(3.0, 3.0, 4.0, 4.0, OperationType.INSERTION);

        OperationGroup<KPE> g1 = new OperationGroup<>();
        g1.add(e1);
        g1.add(e2);
        g1.add(e3);

        OperationGroup<KPE> g2 = new OperationGroup<>();
        g2.add(e1);
        g2.add(e3);

        OperationGroup<KPE> g3 = new OperationGroup<>();
        g3.add(e1);
        g3.add(e2);

        OperationGroup<KPE> g4 = new OperationGroup<>();
        g4.add(e2);

        stats.updateGroupUpdateStatistics(0, g1.size(), g1.isInsertionOnly());
        stats.updateGroupUpdateStatistics(0, g2.size(), g2.isInsertionOnly());
        stats.updateGroupUpdateStatistics(0, g3.size(), g3.isInsertionOnly());
        stats.updateGroupUpdateStatistics(1, g2.size(), g2.isInsertionOnly());
        stats.updateGroupUpdateStatistics(2, g4.size(), g4.isInsertionOnly());

        globalGroupSizes = stats.getGlobalGroupSizeStatistics();
        assertEquals (3, globalGroupSizes.size());
        Iterator<Map.Entry<Integer, Integer>> gi = globalGroupSizes.entrySet().iterator();
        Map.Entry<Integer, Integer> gie = gi.next();
        assertEquals(1, (int)gie.getKey());
        assertEquals(1, (int)gie.getValue());
        gie = gi.next();
        assertEquals(2, (int)gie.getKey());
        assertEquals(3, (int)gie.getValue());
        gie = gi.next();
        assertEquals(3, (int)gie.getKey());
        assertEquals(1, (int)gie.getValue());

        groupSizes = stats.getGroupSizeStatistics();
        assertEquals(3, groupSizes.size());
        Iterator<StatisticalData> i = groupSizes.iterator();
        StatisticalData d = i.next();
        assertEquals(2, d.entrySet().size());
        gi = d.entrySet().iterator();
        gie = gi.next();
        assertEquals(2, (int)gie.getKey());
        assertEquals(2, (int)gie.getValue());
        gie = gi.next();
        assertEquals(3, (int)gie.getKey());
        assertEquals(1, (int)gie.getValue());
        d = i.next();
        assertEquals(1, d.entrySet().size());
        gi = d.entrySet().iterator();
        gie = gi.next();
        assertEquals(2, (int)gie.getKey());
        assertEquals(1, (int)gie.getValue());
        d = i.next();
        assertEquals(1, d.entrySet().size());
        gi = d.entrySet().iterator();
        gie = gi.next();
        assertEquals(1, (int)gie.getKey());
        assertEquals(1, (int)gie.getValue());

        iGroupSizes = stats.getInsertionOnlyGroupSizes();
        assertEquals(2, iGroupSizes.size());
        i = iGroupSizes.iterator();
        d = i.next();
        assertEquals(1, d.entrySet().size());
        gi = d.entrySet().iterator();
        gie = gi.next();
        assertEquals(2, (int)gie.getKey());
        assertEquals(1, (int)gie.getValue());
        d = i.next();
        assertEquals(1, d.entrySet().size());
        gi = d.entrySet().iterator();
        gie = gi.next();
        assertEquals(2, (int)gie.getKey());
        assertEquals(1, (int)gie.getValue());
    }

    @Test
    public void piggybackings() {
        OperationTypeStat piggybackings = stats.getQueryPiggybackings();
        assertTrue (piggybackings != null);
        piggybackings = stats.getLeafUpdatePiggybackings();
        assertTrue (piggybackings != null);
        piggybackings = stats.getNonleafUpdatePiggybackings();
        assertTrue (piggybackings != null);
    }
}
