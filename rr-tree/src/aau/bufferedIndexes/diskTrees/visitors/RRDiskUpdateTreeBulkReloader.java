/*
     Copyright (C) 2010, 2011, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.diskTrees.visitors;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.diskTrees.*;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.workload.DataID;
import aau.workload.WorkloadOperation;
import xxl.core.functions.Function;
import xxl.core.io.Convertable;
import xxl.core.spatial.KPE;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

import java.io.*;
import java.util.*;

/**
 * Disk update tree garbage collector that rebuilds the tree using the Hilbert bulk-load.
 */
public class RRDiskUpdateTreeBulkReloader<E extends Convertable> implements IRRDiskUpdateTreeGarbageCleaner<E> {

    static class EntryTmpFileIdxPair<E extends Convertable> {
        private final UpdateTree.Entry<E> entry;
        private final int tmpFileIdx;

        EntryTmpFileIdxPair(final UpdateTree.Entry<E> entry, int tmpFileIdx) {
            this.entry = entry;
            this.tmpFileIdx = tmpFileIdx;
        }

        UpdateTree.Entry<E> getEntry() {
            return entry;
        }

        int getTmpFileIdx() {
            return tmpFileIdx;
        }
    }

    static class GCEntryCollection<E extends Convertable> {

        private final Comparator<E> hilbertComparator = new Comparator<E>() {
            public int compare (final E e1, final E e2) {
                return HilbertRectangleComparator.INSTANCE.compare((Rectangle)(((KPE)e1).getData()),
                        (Rectangle)(((KPE)e2).getData()));
            }
        };

        final Function<EntryTmpFileIdxPair<E>, UpdateTree.Entry<E>> pairUnwrapper
                = new Function<EntryTmpFileIdxPair<E>, UpdateTree.Entry<E>>() {
            @Override
            public UpdateTree.Entry<E> invoke(EntryTmpFileIdxPair<E> argument) {
                return argument.getEntry();
            }
        };

        private final ObjectTracer<E> objectTracer;

        private final List<EntryTmpFileIdxPair<E>> entriesToWrite = new LinkedList<>();

        GCEntryCollection(final ObjectTracer<E> objectTracer) {
            this.objectTracer = objectTracer;
        }

        int size() {
            return entriesToWrite.size();
        }

        Integer add(final UpdateTree.Entry<E> entry, final int tmpFileIdx, final IRRDiskUpdateTree<E> tree) {
            final EntryTmpFileIdxPair<E> annihilatedPair
                    = UpdateTreeEntryAdder.doAdd(entriesToWrite.listIterator(),
                                                 new EntryTmpFileIdxPair<>(entry, tmpFileIdx), tree, hilbertComparator,
                                                 objectTracer, pairUnwrapper);
            if (annihilatedPair == null) {
                objectTracer.traceUpdateTreeEntry(entry, ObjectTracer.Operation.BULK_RELOADER_2ND_PASS_READ_INCREASE,
                        null);
                return null;
            }
            objectTracer.traceUpdateTreeEntry(entry, ObjectTracer.Operation.BULK_RELOADER_2ND_PASS_READ_ANNIHILATION,
                    annihilatedPair.getTmpFileIdx());
            return annihilatedPair.getTmpFileIdx();
        }

        EntryTmpFileIdxPair<E> pop() {
            return entriesToWrite.remove(0);
        }
    }

    final private static int OUTPUT_BUF_SIZE = 4096; // TODO! size properly

    final private static int INPUT_BUF_SIZE = 4096; // TODO size properly

    final private static double BULK_LOAD_FILL_FACTOR = 0.8D;

    private final Collection<Object> leafIDs = new ArrayList<>();

    private TreeClearIOState oldTreeClearingIO;

    private final Comparator<E> hilbertComparator = new Comparator<E>() {
        public int compare (final E e1, final E e2) {
            return HilbertRectangleComparator.INSTANCE.compare((Rectangle)(((KPE)e1).getData()),
                    (Rectangle)(((KPE)e2).getData()));
        }
    };

    private final int memLimit;

    private final IRRDiskUpdateTree<E> tree;

    /**
     * The object tracer to use during the bulk reloading
     */
    private final ObjectTracer<E> objectTracer;

    private int oldIndexNodeCount = 0;

    private int oldLeafNodeCount = 0;

    private int newIndexNodeCount = 0;

    private int newLeafNodeCount = 0;

    private boolean countsValid = false;

    private int hilbertReadIOs = 0;

    private int hilbertWriteIOs = 0;

    public RRDiskUpdateTreeBulkReloader(final IRRDiskUpdateTree<E> tree, final int memLimit,
                                        final ObjectTracer<E> objectTracer) {
        this.tree = tree;
        this.memLimit = memLimit;
        this.objectTracer = objectTracer;
    }

    public void visitIndexNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> entry,
                               final IRRTreeDiskNode<E> node) {
        oldIndexNodeCount++;
    }

    public void visitLeafNode(final IRRDiskTree<E> tree, final IRRTreeIndexEntry<E> entry) {
        leafIDs.add(entry.id());
    }

    public void finishVisiting() throws IOException {
        final UpdateTreeEntryCollection<E> loadedEntries
                = new UpdateTreeEntryCollection<>(hilbertComparator, objectTracer);
        final Collection<File> partialHilbertFiles = new ArrayList<>();
        final Iterator<Object> leafIdItr = leafIDs.iterator();

        while (leafIdItr.hasNext()) {
            while ((loadedEntries.memSize() < memLimit) && (leafIdItr.hasNext())) {
                final Object leafId = leafIdItr.next();
                //noinspection unchecked
                final IRRTreeDiskUpdateNode<E> node = (IRRTreeDiskUpdateNode<E>)tree.container().get(leafId);
                for (final UpdateTree.Entry<E> newEntry : node.getLeafNodeEntries()) {
                    objectTracer.traceUpdateTreeEntry(newEntry, ObjectTracer.Operation.BULK_RELOADER_GC_1ST_PASS, null);
                    loadedEntries.add(newEntry, tree);
                }
            }
            writePartialHilbertFile(loadedEntries, partialHilbertFiles);
        }
        if (loadedEntries.size() > 0)
            writePartialHilbertFile(loadedEntries, partialHilbertFiles);

        oldLeafNodeCount = leafIDs.size();
        leafIDs.clear();
        oldTreeClearingIO = tree.clearWithIOCount();

        hilbertReadIOs = hilbertWriteIOs; // TODO: wouldn't hurt to verify...

        final Collection<FileInputStream> partialInputStreams = new ArrayList<>(partialHilbertFiles.size());
        for (final File tmpFile : partialHilbertFiles) {
            FileInputStream fileStream = new FileInputStream(tmpFile);
            partialInputStreams.add(fileStream);
        }
        final List<DataInput> partialInputs = new ArrayList<>(partialInputStreams.size());
        for (final FileInputStream tmpStream : partialInputStreams) {
            final DataInput input = new DataInputStream(new BufferedInputStream(tmpStream, INPUT_BUF_SIZE));
            partialInputs.add(input);
        }


        final GCEntryCollection<E> gcEntryCollection = new GCEntryCollection<>(objectTracer);

        for (int i = 0; i < partialInputs.size(); i++)
            loadFromPartialFile(gcEntryCollection, i, partialInputs, null);
        //noinspection NumericCastThatLosesPrecision
        int bulkLoadNodeSize = (int)Math.ceil(tree.getMaxNodeCapacity() * BULK_LOAD_FILL_FACTOR);
        final List<UpdateTree.Entry<E>> newLeafNodeContents = new ArrayList<>(bulkLoadNodeSize);
        final List<IRRTreeIndexEntry<E>> newIndexEntries = new ArrayList<>();
        int dataItemsInNewTree = 0;
        final Collection<Object> seenIds = new HashSet<>();
        while (gcEntryCollection.size() > 0) {
            final EntryTmpFileIdxPair<E> entryTmpFileIdxPair = gcEntryCollection.pop();
            final UpdateTree.Entry<E> entryToWrite = entryTmpFileIdxPair.getEntry();
            if (entryToWrite.isDeletion()) {
                throw new IllegalStateException("D seen at GC bulk reloader: " + entryToWrite.toString());
            }
            boolean result = seenIds.add(((KPE)entryToWrite.getData()).getID());
            if (!result) {
                throw new IllegalStateException("ID already seen at GC bulk reloader: "
                        + ((KPE)entryToWrite.getData()).getID());
            }
            final int replacementReader = entryTmpFileIdxPair.getTmpFileIdx();
            objectTracer.traceUpdateTreeEntry(entryToWrite,
                                              ObjectTracer.Operation.BULK_RELOADER_REMOVING_FROM_ENTRY_TO_TMP_MAP,
                                              replacementReader);
            loadFromPartialFile(gcEntryCollection, replacementReader, partialInputs, entryToWrite);
            newLeafNodeContents.add(entryToWrite);
            dataItemsInNewTree++;
            if (newLeafNodeContents.size() == bulkLoadNodeSize)
                makeNode(newLeafNodeContents, 0, newIndexEntries);
        }
        if (newLeafNodeContents.size() > 0)
            makeNode(newLeafNodeContents, 0, newIndexEntries);
        for (final InputStream inputStream : partialInputStreams) {
            inputStream.close();
        }
        for (final File tmpFile : partialHilbertFiles) {
            boolean result = tmpFile.delete();
            assert result;
        }

        List<IRRTreeIndexEntry<E>> lowerIndexEntries = newIndexEntries;
        int level = 1;
        while (lowerIndexEntries.size() > 1) {
            final List<IRRTreeIndexEntry<E>> upperIndexEntries = new ArrayList<>();
            final List<IRRTreeIndexEntry<E>> newIndexNodeContents = new ArrayList<>();
            for (final IRRTreeIndexEntry<E> indexEntry : lowerIndexEntries) {
                newIndexNodeContents.add(indexEntry);
                if (newIndexNodeContents.size() == bulkLoadNodeSize)
                    makeNode(newIndexNodeContents, level, upperIndexEntries);
            }
            if (newIndexNodeContents.size() > 0)
                makeNode(newIndexNodeContents, level, upperIndexEntries);
            level++;
            lowerIndexEntries = upperIndexEntries;
        }
        tree.setNewRootNode(lowerIndexEntries.get(0));
        tree.setNumberOfDataItems(dataItemsInNewTree);
        countsValid = true;
    }

    private void writePartialHilbertFile(final UpdateTreeEntryCollection<E> loadedEntries,
                                         final Collection<File> partialHilbertFiles) throws IOException {
        assert loadedEntries.isTransitive();
        final File tmpFile = File.createTempFile("RRTreeBulkLoad", "", new File("."));
        partialHilbertFiles.add(tmpFile);
        try (BufferedOutputStream bufStream = new BufferedOutputStream(new FileOutputStream(tmpFile), OUTPUT_BUF_SIZE)) {
            final DataOutputStream output = new DataOutputStream(bufStream);
            UpdateTree.Entry<E> prev = null;
            for (final UpdateTree.Entry<E> entry : loadedEntries) {
                assert (prev == null) || !prev.getData().equals(entry.getData());
                assert (prev == null) || (hilbertComparator.compare(prev.getData(), entry.getData()) != 1);
                objectTracer.traceUpdateTreeEntry(entry, ObjectTracer.Operation.BULK_RELOADER_1ST_PASS_WRITE, null);
                entry.write(output);
                prev = entry;
            }
            hilbertWriteIOs += Math.ceil((double) output.size() / OUTPUT_BUF_SIZE);
        }

        loadedEntries.clear();
    }

    public TreeClearIOState getOldTreeClearingIO() {
        if (oldTreeClearingIO == null) {
            throw new IllegalStateException("RRDiskUpdateTreeBulkReloader.getOldTreeClearingIO"
                    + " called before finishVisiting");
        }
        return oldTreeClearingIO;
    }

    private void makeNode(final List<?> nodeContents, final int level,
                          final Collection<IRRTreeIndexEntry<E>> newIndexEntries) {
        if (level == 0) {
            //noinspection unchecked
            for (final UpdateTree.Entry<E> e : (List<UpdateTree.Entry<E>>)nodeContents)
                objectTracer.traceUpdateTreeEntry(e, ObjectTracer.Operation.BULK_RELOADER_2ND_PASS_WRITE, null);
        }
        final IRRTreeDiskNode<E> newLeafNode = tree.createNode(level, nodeContents);
        nodeContents.clear();
        final IRRTreeIndexEntry<E> newIndexEntry = tree.storeNode(newLeafNode, false);
        newIndexEntries.add(newIndexEntry);
        if (level == 0)
            newLeafNodeCount++;
        else
            newIndexNodeCount++;
    }

    private void loadFromPartialFile(final GCEntryCollection<E> gcEntryCollection, final int i,
                                     final List<DataInput> inputs,
                                     final UpdateTree.Entry<E> previousEntry) throws IOException {
        try {
            UpdateTree.Entry<E> prevReadEntry = null;
            UpdateTree.Entry<E> entry = null;
            do {
                int readIterations = 1;
                while (readIterations > 0) {
                    prevReadEntry = entry;
                    // TODO: please not.
                    //noinspection unchecked
                    final E dummyData
                            = (E)new KPE(new DataID(), new DoublePointRectangle(2), WorkloadOperation.getConverter());
                    entry = new UpdateTree.Entry<>(dummyData, OperationType.DELETION);
                    entry.read(inputs.get(i));
                    assert prevReadEntry == null || !entry.getData().equals(prevReadEntry.getData());
                    objectTracer.traceUpdateTreeEntry(entry, ObjectTracer.Operation.BULK_RELOADER_2ND_PASS_READ, i);
                    readIterations--;
                    Integer prevTmpFileIdx = gcEntryCollection.add(entry, i, tree);
                    if (prevTmpFileIdx == null) {
                        assert previousEntry == null ||
                                hilbertComparator.compare(previousEntry.getData(), entry.getData()) != 1;
                    }
                    else {
                        readIterations++;
                        loadFromPartialFile(gcEntryCollection, prevTmpFileIdx, inputs, previousEntry);
                    }
                }
                assert prevReadEntry == null
                        || hilbertComparator.compare(prevReadEntry.getData(), entry.getData()) <= 0;
            } while (prevReadEntry == null || hilbertComparator.compare(prevReadEntry.getData(), entry.getData()) == 0);
        }
        catch (EOFException ignored) {
            // On EOF no more entries to load from this partial run
        }
    }

    public int oldTreeIndexCount() {
        if (!countsValid)
            throw new IllegalStateException("oldTreeIndexCount must be called after finishVisiting");
        return oldIndexNodeCount;
    }

    public int oldTreeLeafCount() {
        if (!countsValid)
            throw new IllegalStateException("oldTreeLeafCount must be called after finishVisiting");
        return oldLeafNodeCount;
    }

    public int newTreeIndexCount() {
        if (!countsValid)
            throw new IllegalStateException("newTreeIndexCount must be called after finishVisiting");
        return newIndexNodeCount;
    }

    public int newTreeLeafCount() {
        if (!countsValid)
            throw new IllegalStateException("newTreeLeafCount must be called after finishVisiting");
        return newLeafNodeCount;
    }

    public int getHilbertWriteIO() {
        if (!countsValid)
            throw new IllegalStateException("finishVisiting must be called before");
        return hilbertWriteIOs;
    }

    public int getHilbertReadIO() {
        if (!countsValid)
            throw new IllegalStateException("finishVisiting must be called before");
        return hilbertReadIOs;
    }

}
