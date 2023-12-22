/*
     Copyright (C) 2011, 2012 Laurynas Biveinis

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

import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.CounterContainer;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.io.LRUBuffer;
import xxl.core.io.converters.Converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles the I/O for the experimental driver, including the query scheduling at specified update I/O intervals
 */
public class TestIO implements IOIntervalEvent {

    final private BlockFileContainer fileContainer;

    final private CallbackCounterContainer statContainer;

    final private CounterContainer logicalStatContainer;

    final private boolean persistent;

    private int persistenceIO = 0;

    /**
     * Update I/O stats at the points of query issuance.
     */
    private List<IOStatsState> iosAtQueryIssueTime;

    public TestIO(final int containerBlockSize, final int cacheSize, final boolean persistent,
                  final boolean cacheClonesObjects, final Converter<?> converter, final int updateIOQueryRatio) 
            throws IOException {
        fileContainer = prepareFileContainer(containerBlockSize);
        statContainer = new CallbackCounterContainer(new ConverterContainer(fileContainer, converter),
                updateIOQueryRatio, this);
        final Container cacheContainer = addCache(cacheSize, persistent, cacheClonesObjects, statContainer);
        logicalStatContainer = new CounterContainer(cacheContainer);
        iosAtQueryIssueTime = new ArrayList<>();
        this.persistent = persistent;
    }

    public void closeAndDelete() {
        if (fileContainer != null)
            fileContainer.delete();
    }

    public Container get() {
        return logicalStatContainer;
    }

    public IOStatsState logicalStatsSnapshot() {
        return new IOStatsState(logicalStatContainer);
    }

    public IOStatsState statsSnapshot() {
        return new IOStatsState(statContainer);
    }

    public void registerPersistenceIO() {
        if (persistent)
            persistenceIO++;
    }

    public void addReads(final int r) {
        statContainer.gets += r;
    }

    public void addInserts(final int i) {
        statContainer.inserts += i;
    }

    public void revertReads(final int r) {
        statContainer.gets -= r;
        for (IOStatsState qStatsState : iosAtQueryIssueTime) {
            qStatsState.revertReads(r);
        }
    }

    public void revertReads(final IOStatsState ioStatsState) {
        final int r = ioStatsState.getReadDelta();
        revertReads(r);
    }

    public void revert(final IOStatsState ioStatsState) {
        final int r = ioStatsState.getReadDelta();
        final int u = ioStatsState.getUpdateDelta();
        final int i = ioStatsState.getInsertDelta();
        final int d = ioStatsState.getRemoveDelta();
        revertReads(r);
        statContainer.updates -= u;
        statContainer.inserts -= i;
        statContainer.removes -= d;
        for (IOStatsState qStatsState : iosAtQueryIssueTime) {
            qStatsState.revertUpdates(u);
            qStatsState.revertInserts(i);
            qStatsState.revertRemoves(d);
        }
    }
    
    public void revertRemoves(final int r) {
        statContainer.removes -= r;
        for (IOStatsState qStatsState : iosAtQueryIssueTime) {
            qStatsState.revertRemoves(r);
        }
    }

    public int sizeOnDiskInPages() {
        return fileContainer.size();
    }

    public int reads() {
        return statContainer.gets;
    }

    public int writes() {
        return persistenceIO + statContainer.inserts + statContainer.updates + statContainer.removes;
    }

    public int total() {
        return reads() + writes();
    }

    public String writesToString() {
        return "(" + statContainer.inserts + "i, " + statContainer.updates + "u, "
                   + statContainer.removes + "r, " + persistenceIO + "p)";
    }

    public void disableIoIntervalNotifications() {
        statContainer.disableIoIntervalNotifications();
    }
    
    public void enableIoIntervalNotifications() {
        statContainer.enableIoIntervalNotifications();
    }

    /**
     * Takes and stores a new update I/O stats snapshot at a specified interval.
     */
    @Override
    public void notifyIoInterval() {
        iosAtQueryIssueTime.add(statsSnapshot());
    }

    public boolean wasQueryIssued() {
        return iosAtQueryIssueTime.size() > 0;
    }

    /**
     * Returns an iterator over all I/O snapshots at the times queries have occurred and starts registering them anew.
     * @return an iterator over all I/O snapshots at the times queries have occurred
     */
    public Iterator<IOStatsState> getIoSnapshotsAtQueryTime() {
        final Iterator<IOStatsState> result = new ArrayList<>(iosAtQueryIssueTime).iterator();
        iosAtQueryIssueTime = new ArrayList<>();
        return result;
    }
    
    private static BlockFileContainer prepareFileContainer(final int containerBlockSize) throws IOException {
        // We want to be reentrant. Work around lack of temp file support in XXL.
        final File tmpFile = File.createTempFile("RRTreeStor", "", new File("."));
        tmpFile.deleteOnExit();
        final String tmpFilePath = tmpFile.getPath();
        final String tmpFileName = tmpFilePath.substring(tmpFilePath.lastIndexOf(File.separatorChar) + 1,
                tmpFilePath.length());
        return new BlockFileContainer(tmpFileName, containerBlockSize);
    }

    private static Container addCache(final int cacheSize, final boolean persistent, final boolean cacheClonesObjects,
                                      final Container storage) {
        return cacheSize == 0
                ? storage
                : new BufferedContainer(storage, new LRUBuffer(cacheSize), !persistent, cacheClonesObjects);
    }
}
