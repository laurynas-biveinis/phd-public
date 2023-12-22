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
package aau.testDriver;

import aau.bufferedIndexes.*;
import aau.bufferedIndexes.RRTree;
import aau.bufferedIndexes.diskTrees.TreeClearIOState;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.bufferedIndexes.operationGroupMakers.AbstractOperationGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.DeletionsAsInsertionsGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.InsertionsOnlyGroupMaker;
import aau.bufferedIndexes.operationGroupMakers.TrivialOperationGroupMaker;
import aau.bufferedIndexes.pushDownStrategies.*;
import aau.workload.DataID;
import aau.workload.WorkloadOperation;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import xxl.core.cursors.Cursor;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.Tree;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.io.converters.Converter;
import xxl.core.spatial.KPE;
import xxl.core.spatial.rectangles.DoublePointRectangle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * The command-line driven test driver
 */
final class TestDriver {

    // Various sizes of data structures in memory and other constants
    private static final int DEFAULT_NODE_SIZE = 4096;
    private static final double DEFAULT_MIN_FANOUT = 0.4D;

    // Index node entry: 8 bytes (long) id + 4 * 8 bytes (double) Rectangle = 40 bytes
    private static final int INDEX_ENTRY_SIZE = 40;
    // Leaf node entry: 1 byte (bool) null flag + 4 * 8 bytes (double) Rectangle
    //      + 1 byte (bool) null flag + 4 bytes (int) id = 38 bytes
    private static final int DATA_LEAF_ENTRY_SIZE = 38;
    private static final int UPDATE_LEAF_ENTRY_SIZE = DATA_LEAF_ENTRY_SIZE + 1;
    private static final int LEAF_ENTRY_SIZE = Math.max(DATA_LEAF_ENTRY_SIZE, UPDATE_LEAF_ENTRY_SIZE);
    // We use an unified entry size (40 bytes currently)
    private static final int ENTRY_SIZE = Math.max(INDEX_ENTRY_SIZE, LEAF_ENTRY_SIZE);
    // Node overhead: 2 bytes (short) level + 4 bytes (size) = 6 bytes
    private static final int NODE_OVERHEAD = 6;

    private static final Descriptor sampleQuery = new DoublePointRectangle(
            new double[]{27194.0, 64799.0},
            new double[]{27195.0, 54801.0});

    // Command line options and their allowed values
    private static final OptionParser optParser = new OptionParser();

    @SuppressWarnings("unchecked")
    private static final OptionSpec<TreeType> treeOption
            = optParser.accepts("tree").withRequiredArg().ofType(TreeType.class).defaultsTo(TreeType.R_TREE);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> bufSizeOption
            = optParser.accepts("bufsize").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> cacheOption
            = optParser.accepts("cache").withRequiredArg().ofType(Integer.class).defaultsTo(0);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<PushDownStrategyType> pushDownStrategyOption
            = optParser.accepts("pushdown").withRequiredArg().ofType(PushDownStrategyType.class)
                .defaultsTo(PushDownStrategyType.EVERYTHING);
    private static final OptionSpec<String> groupSizesOutOption
            = optParser.accepts("outputgsizes").withRequiredArg().ofType(String.class);
    private static final OptionSpec<String> rootGroupSizesOutOption
            = optParser.accepts("outputrootgsizes").withRequiredArg().ofType(String.class);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> minGroupSizeOption
            = optParser.accepts("mingroupsize").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Double> groupSizeCoefficientOption
            = optParser.accepts("groupsizecoeff").withRequiredArg().ofType(Double.class).defaultsTo(1.0D);
    private static final OptionSpec<Void> disableQueryPiggybackingOption
            = optParser.accepts("disablequerypiggybacking");
    private static final OptionSpec<Void> disableUpdateIndexPiggybackingOption
            = optParser.accepts("disableupdateindexpiggybacking");
    private static final OptionSpec<Void> disableUpdateLeafPiggybackingOption
            = optParser.accepts("disableupdateleafpiggybacking");
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Boolean> groupSizeByOption
            = optParser.accepts("sizebyins").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> nodeSizeOption
            = optParser.accepts("nodesize").withRequiredArg().ofType(Integer.class).defaultsTo(DEFAULT_NODE_SIZE);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Boolean> persistentOption
            = optParser.accepts("persistent").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
    private static final OptionSpec<Void> countObjectsOption
            = optParser.accepts("countobjects");
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Double> piggybackingEpsilonOption
            = optParser.accepts("piggybackingepsilon").withRequiredArg().ofType(Double.class).defaultsTo(0.0D);

    /* Input options */
    private static final OptionSpec<String> inputOption
            = optParser.accepts("input").withRequiredArg().ofType(String.class).required();
    private static final OptionSpec<String> inputQueriesOption
            = optParser.accepts("inputqueries").withRequiredArg().ofType(String.class);
    
    private static final OptionSpec<Integer> updateIOQueryRatioOption
            = optParser.accepts("queryratio").withRequiredArg().ofType(Integer.class);
    
    /* Garbage collection options */
    @SuppressWarnings("unchecked")
    private static final OptionSpec<GcStrategyType> gcOption
            = optParser.accepts("gc").withRequiredArg().ofType(GcStrategyType.class).defaultsTo(GcStrategyType.NONE);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<GcInvocationType> gcInvocationOption
            = optParser.accepts("gcinvocation").withRequiredArg().ofType(GcInvocationType.class)
            .defaultsTo(GcInvocationType.NONE);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> gcUpdateIntervalOption
            = optParser.accepts("gcinterval").withRequiredArg().ofType(Integer.class).defaultsTo(0);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Float> gcDiskSizeOption
            = optParser.accepts("gcdisksize").withRequiredArg().ofType(Float.class).defaultsTo(0.0F);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> gcIndexCacheSizeOption
            = optParser.accepts("gcindexcache").withRequiredArg().ofType(Integer.class).defaultsTo(0);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> gcInitialScratchMemOption
            = optParser.accepts("gcinitscratchmem").withRequiredArg().ofType(Integer.class).defaultsTo(0);
    private static final OptionSpec<Void> gcFullEbOption = optParser.accepts("gcfulleb");

    /* Operation group maker option */
    @SuppressWarnings("unchecked")
    private static final OptionSpec<OperationGroupMakerType> operationGroupMakerOption
            = optParser.accepts("opgroupmaker").withRequiredArg().ofType(OperationGroupMakerType.class)
                .defaultsTo(OperationGroupMakerType.DEFAULT);

    /* Tracing and debugging options */
    private static final OptionSpec<Void> verifyAlwaysOption
            = optParser.accepts("verifyalways");
    private static final OptionSpec<Void> verifyOnIOOption
            = optParser.accepts("verifyonio");
    private static final OptionSpec<Void> verifyOnFinishOption
            = optParser.accepts("verifyonfinish");
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> verifyOnLineOption
            = optParser.accepts("verifyonline").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
    private static final OptionSpec<Void> assertInvariantsOption
            = optParser.accepts("checktreeinvariants");
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> trackObjectOption
            = optParser.accepts("trackobject").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
    @SuppressWarnings("unchecked")
    private static final OptionSpec<Integer> notifyOnLineOption
            = optParser.accepts("notifyonline").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
    private static final OptionSpec<ObjectTracer.TraceClass> tracingOption
            = optParser.accepts("trace").withOptionalArg().ofType(ObjectTracer.TraceClass.class)
                .withValuesSeparatedBy(',');

    // Enums for command line options
    public enum TreeType {
        RR_TREE {
            TreeDriver<KPE> makeTreeDriver() {
                return new aau.testDriver.RRTree<>(aau.testDriver.RRTree.TreeType.DATA_TREE);
            }

            OperationGroupMakerType defaultOperationGroupMakerType() { return OperationGroupMakerType.INSONLY; }

            Converter<?> getLeafConverter() { return new ConvertableConverter<>(DATA_LEAFENTRY_FACTORY); }
        },
        UPDATE_RR_TREE {
            TreeDriver<KPE> makeTreeDriver() {
                return new aau.testDriver.RRTree<>(aau.testDriver.RRTree.TreeType.UPDATE_TREE);
            }

            OperationGroupMakerType defaultOperationGroupMakerType() { return OperationGroupMakerType.DELSASINS; }

            Converter<?> getLeafConverter() {
                return new ConvertableConverter<>(UPDATE_LEAFENTRY_FACTORY);
            }
        },
        R_TREE {
            TreeDriver<KPE> makeTreeDriver() {
                return new RTree<>();
            }

            OperationGroupMakerType defaultOperationGroupMakerType() { throw new IllegalStateException(); }

            Converter<?> getLeafConverter() { return new ConvertableConverter<>(DATA_LEAFENTRY_FACTORY); }
        };

        abstract TreeDriver<KPE> makeTreeDriver();
        abstract OperationGroupMakerType defaultOperationGroupMakerType();
        abstract Converter<?> getLeafConverter();

        private static final Function<Object, KPE> DATA_LEAFENTRY_FACTORY = new Function<Object, KPE> () {
            public KPE invoke () {
                return new KPE(new DataID(), new DoublePointRectangle(2), WorkloadOperation.getConverter());
            }
        };

        private static final Function<Object, UpdateTree.Entry<KPE>> UPDATE_LEAFENTRY_FACTORY
                = new Function<Object, UpdateTree.Entry<KPE>>() {
            public UpdateTree.Entry<KPE> invoke() {
                return new UpdateTree.Entry<>(new KPE(new DataID(), new DoublePointRectangle(2),
                        WorkloadOperation.getConverter()), OperationType.INSERTION);
            }
        };
    }

    public enum PushDownStrategyType {
        EVERYTHING                  ("whole buffer"),
        ROOT_THRESHOLD              ("threshold at the root level"),
        BUF_LARGEST                 ("largest groups in the buffer, ignoring split deletes"),
        BUF_LARGEST_SPLIT_DELS      ("largest groups in the buffer, pushing down split deletes"),
        THRESHOLD                   ("global threshold"),
        BUF_LARGEST_THRESHOLD       ("largest groups in the buffer, threshold below root"),
        BUF_LARGEST_DIV_BY_CONST    ("largest groups in the buffer, divided by global fanout below root"),
        BUF_LARGEST_DIV_BY_FANOUT   ("largest groups in the buffer, divided by node fanout below root"),
        BUF_THRESHOLD_DIV_BY_CONST  ("threshold at the root level, divided by global fanout below root"),
        BUF_THRESHOLD_DIV_BY_FANOUT ("threshold at the root level, divided by node fanout below root"),
        LEAF_THRESHOLD_TIMES_FANOUT ("threshold at one above leaf level, multiplied by global fanout above");

        private final String description;

        PushDownStrategyType(final String description) {
            this.description = description;
        }

        String getDescription() {
            return description;
        }
    }

    public enum GcStrategyType { NONE, VACUUM, REBUILD }

    public enum GcInvocationType { NONE, INTERVAL, DISK_SIZE }

    public enum OperationGroupMakerType {
        DEFAULT   ("default for the tree type"),
        TRIVIAL   ("trivial (i by ChooseSubtree, d by splitting into all overlaps)"),
        DELSASINS ("deletions as insertions (i and d by ChooseSubtree)"),
        INSONLY   ("insertions only (i by ChooseSubtree, d never happens)");

        private final String description;

        OperationGroupMakerType(final String description) {
            this.description = description;
        }

        String getDescription() {
            return description;
        }
    }

    // Command line arg values
    private static int trackObjId;
    private static int verifyLineNumber;
    private static OperationGroupMakerType operationGroupMakerType;
    private static TreeType treeType;

    /**
     * The ratio between update-I/Os and queries.  I.e. each query is processed every "updateIOQueryRatio" I/Os spent on
     * updates.
     */
    private static int updateIOQueryRatio = 0;

    /**
     * Disk page length, equal to the maximum node size
     */
    private static int containerBlockSize;

    private static String groupSizesFileName = "";
    private static String rootGroupSizesFileName = "";
    private static int cacheSize;
    private static PushDownStrategyType pushDownStrategyType;
    private static boolean groupSizeByInsertions;
    private static int groupSizeThreshold;
    private static double groupSizeCoefficient;
    private static int bufferSize;
    private static boolean doQueryPiggybacking = false;
    private static boolean doUpdateIndexPiggybacking = false;
    private static boolean doUpdateLeafPiggybacking = false;
    private static boolean verifyOnFinish = false;
    private static boolean verifyAlways = false;
    private static boolean verifyOnIO = false;
    private static boolean assertInvariants = false;
    private static boolean persistent;
    private static boolean countObjects = false;
    private static GcStrategyType gcStrategyType;

    /**
     * The type of the chosen garbage collection invocation option.
     */
    private static GcInvocationType gcInvocationType;

    /**
     * If applicable, the interval between the garbage collections, measured in
     * EmptyBuffer invocation.
     */
    private static int gcEmptyBufferInterval;

    /**
     * If applicable, the maximum ratio that the disk tree is allowed to exceed the logical size of the tree or the
     * garbage collecton happens.
     */
    private static float gcMaximumDiskSizeRatio;

    /**
     * Size of index node LRU cache for garbage collection
     */
    private static int gcIndexCacheSize;

    private static int gcInitialScratchMemSize = 0;

    /**
     * A relative amount of query MBR expansion in each direction for piggybacking.
     */
    private static double piggybackingEpsilon;

    /**
     * A line number to have some code executed for debugging breakpoint.
     */
    private static int notifyOnLine;

    private static boolean gcFullEb = false;

    private static EnumSet<ObjectTracer.TraceClass> traceMask;

    // Main data variables
    private static TreeDriver<KPE> tree = null;
    private static TreeVerifier<KPE> treeVerifier = null;

    /**
     * The main workload input file
     */
    private static InputFile inputFile;

    /**
     * The parallel queries input file
     */
    private static InputFile queryInputFile = null;

    private static PushDownGroupsStrategy<KPE> pushDownStrategy = null;

    // Statistical counters
    private static int queriedObjects = 0;
    private static int insertions = 0;
    private static int deletions = 0;
    private static int queries = 0;
    private static int verificationReads = 0;
    private static int queryReads = 0;

    /**
     * I/O writes caused by queries.  Only piggybacking should cause this
     */
    private static int queryWrites = 0;

    /**
     * Reads caused by garbage collection.
     */
    private static int gcReads = 0;

    /**
     * Writes caused by garbage collection.
     */
    private static int gcWrites = 0;

    private static int garbageCleanedCount = 0;
    private static final AggregateStats diskDataRatioBeforeGCStat = new AggregateStats();
    private static final AggregateStats diskDataRatioAfterGCStat = new AggregateStats();

    private static TestIO testIO = null;

    private TestDriver() { }

    public static void main(final String[] args) {
        printRawOptions(args);

        try {
            processOptions(optParser.parse(args));
            printArgs();

            final ObjectTracer<KPE> objectTracer = new ConsoleTracer(traceMask);
            objectTracer.registerObject(trackObjId);

            tree = treeType.makeTreeDriver();
            switch (treeType) {
                case RR_TREE:
                case UPDATE_RR_TREE:
                    if (bufferSize == -1)
                        throw new IllegalArgumentException("Unspecified buffer size for the RR-tree!");
                    pushDownStrategy = preparePushDownStrategy();
                    if (operationGroupMakerType == OperationGroupMakerType.DEFAULT)
                        operationGroupMakerType = treeType.defaultOperationGroupMakerType();
                    break;
                case R_TREE:
                    break;
                default:
                    throw new IllegalStateException();
            }

            AbstractOperationGroupMaker operationGroupMaker;
            switch (operationGroupMakerType) {
                case TRIVIAL:   operationGroupMaker = new TrivialOperationGroupMaker(); break;
                case DELSASINS: operationGroupMaker = new DeletionsAsInsertionsGroupMaker(); break;
                case INSONLY:   operationGroupMaker = new InsertionsOnlyGroupMaker(); break;
                default: throw new IllegalStateException();
            }

            final int maxCapacity = (containerBlockSize - NODE_OVERHEAD) / ENTRY_SIZE;
            @SuppressWarnings({"NumericCastThatLosesPrecision"})
            final int minCapacity = (int)Math.floor((double) maxCapacity * DEFAULT_MIN_FANOUT);

            final Converter<?> c = treeType.getLeafConverter();
            testIO = new TestIO (containerBlockSize, cacheSize, persistent,
                    (tree.asTree() instanceof aau.bufferedIndexes.RRTree), tree.nodeConverter(c, 2),
                    updateIOQueryRatio);

            // Function that returns the ID of a given KPE data object.
            final Function<KPE, DataID> GET_ID = new Function<KPE, DataID>() {
                public DataID invoke(final KPE o) {
                    return (DataID)o.getID();
                }
            };

            // Function that returns the descriptor of a given KPE data object.
            final Function<KPE, Descriptor> GET_DESCRIPTOR = new Function<KPE, Descriptor>() {
                public Descriptor invoke(final KPE o) {
                    return (Descriptor)(o.getData());
                }
            };

            tree.initialize(GET_ID, GET_DESCRIPTOR, testIO.get(), minCapacity, maxCapacity, bufferSize,
                    operationGroupMaker, doUpdateIndexPiggybacking, doUpdateLeafPiggybacking, piggybackingEpsilon,
                    gcIndexCacheSize, gcInitialScratchMemSize, doQueryPiggybacking, pushDownStrategy, objectTracer);

            treeVerifier = new TreeVerifier<>(tree, verifyOnFinish || verifyAlways || verifyOnIO
                    || (verifyLineNumber != -1));

            WorkloadOperation query = getNextQuery();
            final Set<Integer> seenIDs = countObjects ? new HashSet<Integer>() : null;
            int ebCount = 0;
            boolean performGcAfterNextEb = false;
             // Statistics of query latency, i.e. number of update I/Os passed from query issue time until the query
             // is answered
            final AggregateStats ioLatencyStat = new AggregateStats();

            while (inputFile.hasNextOperation()) {

                if (inputFile.getLineNumber() == notifyOnLine)
                    System.err.println("Input line = " + notifyOnLine + " reached");

                if (testIO.wasQueryIssued())
                    throw new IllegalStateException("Unhandled query!");
                
                final WorkloadOperation operation = inputFile.getNextOperation();

                if (countObjects && operation.isUpdate())
                    seenIDs.add(operation.getId());

                if (operation.isEmptyBuffer())
                    tree.emptyBuffer();
                else if (operation.isInsert() || operation.isDeletingInsert()) {
                    doInsert(operation);
                    tree.registerPersistenceIo(testIO);
                } else if (operation.isKindOfDelete()) {
                    doDelete(operation);
                    tree.registerPersistenceIo(testIO);
                } else if (operation.isQuery()) {
                    if (queryInputFile == null)
                        doQuery(operation);
                    else {
                        // TODO: currently let's ignore queries in the main query input file, because we use
                        // previously-generated datasets where they exist
                        //throw new IllegalStateException("With query input file used, a query found in the main input "
                        //        + inputFile.getLineNumber());
                    }
                } else
                    System.err.println("Unknown operation, line " + inputFile.getLineNumber());

                if (inputFile.getLineNumber() == notifyOnLine) {
                    System.err.println("Operation on input line = " + notifyOnLine + " executed");
                    performSampleQuery();
                }

                if (tree.wasBufferEmptied()) {
                    ebCount++;
                    if (performGcAfterNextEb) {
                        performGcAfterNextEb = false;
                        cleanGarbage();
                    }
                    else if (shouldDoGC(ebCount)) {
                        if (gcFullEb) {
                            tree.onNextEbForceFullEmptying();
                            performGcAfterNextEb = true;
                        }
                        else
                            cleanGarbage();
                    }
                }

                if (testIO.wasQueryIssued()) {
                    final Iterator<IOStatsState> iosAtQueryTimeItr = testIO.getIoSnapshotsAtQueryTime();
                    testIO.disableIoIntervalNotifications();
                    while (iosAtQueryTimeItr.hasNext() && (query != null)) {
                        final IOStatsState iosAtQueryTime = iosAtQueryTimeItr.next();
                        final IOStatsState iosPreQuery = testIO.statsSnapshot();
                        doQuery(query);
                        ioLatencyStat.registerValue(iosAtQueryTime.getDelta());
                        testIO.revert(iosPreQuery);
                        query = getNextQuery();
                    }
                    testIO.enableIoIntervalNotifications();
                }

                if (assertInvariants)
                    tree.checkTreeStructure();
            }
            inputFile.close();
            tree.registerBufferLifetimes();
            printStats(seenIDs, ioLatencyStat);
            outputStatisticalData(groupSizesFileName, tree.getGlobalGroupSizes());
            outputStatisticalData(rootGroupSizesFileName, tree.getRootGroupSizes());
            if (verifyOnFinish) {
                System.out.print("Performing final verification...");
                verify();
                System.out.println("done");
            }
        }
        catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(1);
        }
        catch (Throwable e) {
            printLineNumbers();
            System.err.println(e);
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (testIO != null)
                testIO.closeAndDelete();
        }
    }

    private static void printStats(final Set<Integer> seenIDs, final AggregateStats ioLatencyStat) {
        final int containerWrites = testIO.writes();
        final int containerAccesses = testIO.total();
        final int containerUpdateAccesses = getUpdateAccesses();
        final double avgIOPerUpdate = (double)containerUpdateAccesses / (insertions + deletions);
        final double avgIOPerOp = (double)containerAccesses / (double) (insertions + deletions + queries);
        final int equalLargestGroups = pushDownStrategy != null ? pushDownStrategy.getEqualLargestGroups() : 0;
        System.out.println();
        System.out.print("Insertions: " + insertions + ' ');
        System.out.print("Deletions: " + deletions + ' ');
        System.out.println("Queries: " + queries + ' ');
        System.out.print("Container writes: " + containerWrites + ' ');
        System.out.println(testIO.writesToString());
        System.out.println("Total container reads w/o verification and garbage accounting: " + testIO.reads());
        System.out.println("Container reads for verification: " + verificationReads);
        System.out.println("Times GC performed: " + garbageCleanedCount);
        System.out.println("Average physical to logical data ratio before GC: "
                + diskDataRatioBeforeGCStat.average());
        System.out.println("Average physical to logical data ratio after GC: "
                + diskDataRatioAfterGCStat.average());
        System.out.println("Absolute total reads: " + (testIO.reads() + verificationReads));
        System.out.println("Container reads by queries: " + queryReads);
        System.out.println("Container updates by queries: " + queryWrites);
        System.out.println("Container reads by GC: " + gcReads);
        System.out.println("Container writes by GC: " + gcWrites);
        System.out.println("Average query latency in I/Os: " + ioLatencyStat.average());
        System.out.println("Maximum query latency in I/Os: " + ioLatencyStat.maximum());
        System.out.println("Standard deviation of query latency in I/Os: " + ioLatencyStat.deviation());
        System.out.println("Final tree height: " + tree.getHeight());
        System.out.println("Final disk tree size in pages: " + testIO.sizeOnDiskInPages());
        System.out.println("Mean I/O per update, workload total: " + avgIOPerUpdate);
        System.out.println("Average I/O per op: " + avgIOPerOp);
        printThresholdStats();
        System.out.println("Number of equal largest groups: " + equalLargestGroups);
        printTreeFanoutStatistics();
        System.out.println("Objects returned by queries: " + queriedObjects);
        if (countObjects)
            System.out.println("Different objects seen: " + seenIDs.size());
        tree.printSpecificStats();
    }

    private static void printRawOptions(final String[] args) {
        System.out.println("Command line arguments: ");
        for (final String arg : args) {
            System.out.print(arg);
            System.out.print(' ');
        }
        System.out.println();
    }

    private static void cleanGarbage() throws TreeVerifier.FailedVerificationException {
        diskDataRatioBeforeGCStat.registerValue(tree.getPhysicalToLogicalDataRatio());
        doCleanGarbage();
        diskDataRatioAfterGCStat.registerValue(tree.getPhysicalToLogicalDataRatio());
        if (verifyAlways || verifyOnIO)
            verify();
    }

    private static void doCleanGarbage() {

        if (gcStrategyType == GcStrategyType.NONE)
            return;

        final IOStatsState ioStatsState = testIO.statsSnapshot();
        final int oldTmpGcReads = tree.getTmpGcReads();
        final int oldTmpGcWrites = tree.getTmpGcWrites();
        TreeClearIOState oldDiskClearCosts;
        switch (gcStrategyType) {
            case VACUUM:
                oldDiskClearCosts = tree.cleanGarbage(false);
                if (ioStatsState.insertsOrRemovesHappened()) {
                    System.err.println("GC vacuum caused an insert or delete!");
                    printLineNumbers();
                }
                break;
            case REBUILD:
                oldDiskClearCosts = tree.cleanGarbage(true);
                break;
            default: throw new IllegalStateException();
        }
        final int tmpGcReads = tree.getTmpGcReads() - oldTmpGcReads;
        final int tmpGcWrites = tree.getTmpGcWrites() - oldTmpGcWrites;
        garbageCleanedCount++;
        gcReads += ioStatsState.getReadDelta() - oldDiskClearCosts.gets + tmpGcReads;
        gcWrites += ioStatsState.getWriteDelta() - oldDiskClearCosts.removes + tmpGcWrites;
        testIO.revertReads(oldDiskClearCosts.gets);
        testIO.revertRemoves(oldDiskClearCosts.removes);
        testIO.addReads(tmpGcReads);
        testIO.addInserts(tmpGcWrites);
    }

    private static boolean shouldDoGC(int ebCount) {
        return ((gcMaximumDiskSizeRatio != 0.0F) && (tree.getPhysicalToLogicalDataRatio() > gcMaximumDiskSizeRatio))
                || ((gcEmptyBufferInterval > 0) && (ebCount % gcEmptyBufferInterval == 0));
    }

    private static WorkloadOperation getNextQuery() throws IOException {
        WorkloadOperation query = null;
        if ((queryInputFile != null) && (queryInputFile.hasNextOperation())) {
            query = queryInputFile.getNextOperation();
            if (!query.isQuery())
                throw new IllegalStateException("Non-query found in the query file, line = "
                        + queryInputFile.getLineNumber());
        }
        return query;
    }

    private static void performSampleQuery() {
        final boolean oldPiggybackingState = tree.getPiggybackingState();
        tree.setPiggybackingState(false);
        //noinspection unchecked
        final Cursor<KPE> results = tree.asTree().query(sampleQuery);
        System.err.println("Results:");
        while (results.hasNext()) {
            KPE result = results.next();
            System.err.println("Next result = " + result);
        }
        System.err.println("End of results!");
        tree.setPiggybackingState(oldPiggybackingState);
    }

    private static void verify() throws TreeVerifier.FailedVerificationException {
        final IOStatsState writesChecker = testIO.logicalStatsSnapshot();
        final IOStatsState ioStatsState = testIO.statsSnapshot();

        treeVerifier.verify();
        assert !writesChecker.writesHappened();

        verificationReads += ioStatsState.getReadDelta();
        testIO.revertReads(ioStatsState);
    }

    private static int getUpdateAccesses() {
        return testIO.total() - queryReads;
    }

    private static void printThresholdStats() {
        if (pushDownStrategy instanceof AbstractPushDownThreshold) {
            final AbstractPushDownThreshold thresholdStrategy = (AbstractPushDownThreshold)pushDownStrategy;
            final int unsatisfactions = thresholdStrategy.getThresholdUnsatisfactions();
            final int satisfactions = thresholdStrategy.getThresholdSatisfactions();
            final float unsatRatio = (float)unsatisfactions / (unsatisfactions + satisfactions);
            final float satRatio = (float)satisfactions / (unsatisfactions + satisfactions);
            System.out.println("Number of threshold satisfactions (%), unsatisfactions (%), total: "
                    + satisfactions + " (" + satRatio + "), " + unsatisfactions + " (" + unsatRatio + "), "
                    + (satisfactions + unsatisfactions));
        }
    }

    private static PushDownGroupsStrategy<KPE> preparePushDownStrategy() {
        assert tree instanceof aau.testDriver.RRTree;
        final RRTree<KPE> rrtree = ((aau.testDriver.RRTree<KPE>)tree).asTree();

        switch (pushDownStrategyType) {
            case EVERYTHING:
                return new PushDownAllGroups<>();
            case ROOT_THRESHOLD:
                if (groupSizeThreshold == -1)
                    throw new IllegalArgumentException("Unspecified group size threshold!");
                return new RootLevelThreshold<>(rrtree.getDiskTree(), groupSizeThreshold, groupSizeByInsertions);
            case THRESHOLD:
                if (groupSizeThreshold == -1)
                    throw new IllegalArgumentException("Unspecified group size threshold!");
                return new PushDownThreshold<>(groupSizeThreshold, groupSizeByInsertions);
            case BUF_LARGEST:
                return new PushDownLargestBufGroup<>(rrtree.getDiskTree(), groupSizeByInsertions);
            case BUF_LARGEST_SPLIT_DELS:
                return new PushDownLargestBufGroupSplitDeletes<>(rrtree.getDiskTree(), groupSizeByInsertions);
            case BUF_LARGEST_THRESHOLD:
                if (groupSizeThreshold == -1)
                    throw new IllegalArgumentException("Unspecified group size threshold!");
                return new PushDownThresholdBelowRoot<>(rrtree.getDiskTree(),
                    new PushDownLargestBufGroup<>(rrtree.getDiskTree(), groupSizeByInsertions), groupSizeThreshold,
                        groupSizeByInsertions);
            case BUF_LARGEST_DIV_BY_CONST:
                return new PushDownDivideByConstantBelowRoot<>(rrtree.getDiskTree(),
                        new PushDownLargestBufGroup<>(rrtree.getDiskTree(), groupSizeByInsertions),
                            groupSizeCoefficient, groupSizeByInsertions);
            case BUF_THRESHOLD_DIV_BY_CONST:
                return new PushDownDivideByConstantBelowRoot<>(rrtree.getDiskTree(),
                        new RootLevelThreshold<>(rrtree.getDiskTree(), groupSizeThreshold, groupSizeByInsertions),
                            groupSizeCoefficient, groupSizeByInsertions);
            case BUF_LARGEST_DIV_BY_FANOUT:
                return new PushDownDivideByFanoutBelowRoot<>(rrtree.getDiskTree(),
                        new PushDownLargestBufGroup<>(rrtree.getDiskTree(), groupSizeByInsertions),
                            groupSizeCoefficient, groupSizeByInsertions);
            case BUF_THRESHOLD_DIV_BY_FANOUT:
                return new PushDownDivideByFanoutBelowRoot<>(rrtree.getDiskTree(),
                        new RootLevelThreshold<>(rrtree.getDiskTree(), groupSizeThreshold, groupSizeByInsertions),
                            groupSizeCoefficient, groupSizeByInsertions);
            case LEAF_THRESHOLD_TIMES_FANOUT:
                if (groupSizeThreshold == -1)
                    throw new IllegalArgumentException("Unspecified group size threshold!");
                return new PushDownThresholdTimesFanout<>(rrtree.getDiskTree(), groupSizeThreshold,
                        groupSizeByInsertions);
            default: throw new IllegalStateException();
        }
    }

    private static boolean assertionsEnabled() {
        System.out.println("Assertions enabled");
        return true;
    }

    private static void printArgs() {
        assert assertionsEnabled();
        System.out.println("Tree: "+treeType+' ');
        System.out.println("Operation group maker: " + operationGroupMakerType.getDescription());
        System.out.println("Buffer emptying strategy: " + pushDownStrategyType.getDescription());
        if (groupSizeThreshold != -1)
            System.out.println("Group size threshold: " + groupSizeThreshold);
        System.out.println("Group size coefficient: " + groupSizeCoefficient);
        if (groupSizeByInsertions)
            System.out.println("Considering only insertions for group size threshold");
        else
            System.out.println("Considering all ops for group size threshold");
        if (bufferSize != -1)
            System.out.println("Buffer size: "+ bufferSize);
        if (cacheSize > 0)
            System.out.println("LRU cache: " + cacheSize);
        System.out.println("Input: " + inputFile.getName());
        if (queryInputFile != null) {
            System.out.println("Query input file: " + queryInputFile.getName());
            System.out.println("Update-IO/Query ratio: " + updateIOQueryRatio);
        }
        if (persistent)
            System.out.println("Persistent setting assumed");
        if (countObjects)
            System.out.println("Counting objects");
        System.out.println("Piggybacking epsilon: " + piggybackingEpsilon);
        if (doUpdateIndexPiggybacking)
            System.out.println("Performing piggybacking on updates, index nodes");
        else
            System.out.println("Not performing piggybacking on updates, index nodes ");
        if (doUpdateLeafPiggybacking)
            System.out.println("Performing piggybacking on updates, leaf nodes");
        else
            System.out.println("Not performing piggybacking on updates, leaf nodes");
        if (doQueryPiggybacking)
            System.out.println("Performing piggybacking on queries");
        else
            System.out.println("Not performing piggybacking on queries");
        System.out.println("GC strategy: " + gcStrategyType);
        System.out.print("GC invocation: ");
        switch (gcInvocationType) {
            case INTERVAL:
                System.out.println("interval between EmptyBuffer calls = " + gcEmptyBufferInterval);
                break;
            case DISK_SIZE:
                System.out.println("maximum physical-to-logical data ratio = " + gcMaximumDiskSizeRatio);
                break;
            case NONE:
                System.out.println("none");
                break;
            default:
                throw new IllegalStateException();
        }
        System.out.println("GC index node cache size = " + gcIndexCacheSize);
        System.out.println("GC initial scratch memory size = " + gcInitialScratchMemSize);
        if (gcFullEb)
            System.out.println("Performing full EmptyBuffer before each GC");
        if (assertInvariants)
            System.out.println("Performing tree structure checks after every operation");
        if (verifyAlways)
            System.out.println("Verifying tree contents after every operation");
        if (verifyOnFinish)
            System.out.println("Verifying tree contents on workload finish");
        if (verifyOnIO)
            System.out.println("Verifying tree contents on disk I/O");
        if (verifyLineNumber != -1)
            System.out.println("Verifying on line number = " + verifyLineNumber);
        if (!(verifyAlways || verifyOnFinish || verifyOnIO || (verifyLineNumber != -1)))
            System.out.println("Not verifying anything");
        if (notifyOnLine != -1)
            System.out.println("Notifying on line = " + notifyOnLine);
        System.out.println("Tracing: " + traceMask.toString());
    }

    private static void processOptions(final OptionSet options) throws IOException {
        treeType = options.valueOf(treeOption);
        operationGroupMakerType = options.valueOf(operationGroupMakerOption);
        if (options.has(groupSizesOutOption)) groupSizesFileName = options.valueOf(groupSizesOutOption);
        if (options.has(rootGroupSizesOutOption)) rootGroupSizesFileName = options.valueOf(rootGroupSizesOutOption);
        containerBlockSize = options.valueOf(nodeSizeOption);
        cacheSize = options.valueOf(cacheOption);
        pushDownStrategyType = options.valueOf(pushDownStrategyOption);
        groupSizeThreshold = options.valueOf(minGroupSizeOption);
        groupSizeCoefficient = options.valueOf(groupSizeCoefficientOption);
        bufferSize = options.valueOf(bufSizeOption);
        groupSizeByInsertions = options.valueOf(groupSizeByOption);
        doQueryPiggybacking = !options.has(disableQueryPiggybackingOption);
        doUpdateIndexPiggybacking = !options.has(disableUpdateIndexPiggybackingOption);
        doUpdateLeafPiggybacking = !options.has(disableUpdateLeafPiggybackingOption);
        piggybackingEpsilon = options.valueOf(piggybackingEpsilonOption);
        verifyOnFinish = options.has(verifyOnFinishOption);
        verifyAlways = options.has(verifyAlwaysOption);
        verifyOnIO = options.has(verifyOnIOOption);
        verifyLineNumber = options.valueOf(verifyOnLineOption);
        assertInvariants = options.has(assertInvariantsOption);
        notifyOnLine = options.valueOf(notifyOnLineOption);
        countObjects = options.has(countObjectsOption);
        inputFile = new InputFile(options.valueOf(inputOption));
        if (options.has(inputQueriesOption)) {
            queryInputFile = new InputFile(options.valueOf(inputQueriesOption));
            if (options.has(updateIOQueryRatioOption))
                updateIOQueryRatio = options.valueOf(updateIOQueryRatioOption);
            else
                throw new IllegalArgumentException("Specified query input file but not the update I/O:query ratio!");
        } else if (options.has(updateIOQueryRatioOption))
            throw new IllegalArgumentException("Specified update I/O:query ratio but not the query input file!");
        trackObjId = options.valueOf(trackObjectOption);
        persistent = options.valueOf(persistentOption);
        gcStrategyType = options.valueOf(gcOption);
        gcInvocationType = options.valueOf(gcInvocationOption);
        gcEmptyBufferInterval = options.valueOf(gcUpdateIntervalOption);
        gcMaximumDiskSizeRatio = options.valueOf(gcDiskSizeOption);
        gcIndexCacheSize = options.valueOf(gcIndexCacheSizeOption);
        gcInitialScratchMemSize = options.valueOf(gcInitialScratchMemOption);
        gcFullEb = options.has(gcFullEbOption);
        if (options.has(tracingOption)) {
            if (options.hasArgument(tracingOption))
                traceMask = EnumSet.copyOf(options.valuesOf(tracingOption));
            else
                traceMask = EnumSet.allOf(ObjectTracer.TraceClass.class);
        }
        else {
            traceMask = EnumSet.noneOf(ObjectTracer.TraceClass.class);
        }
    }

    /**
     * Check object id against ids of traced objects and in the case of match print where in the workload the object
     * was encountered
     * @param id object id
     * @param datum the object itself
     * @param message message to print (ussually the type of operation performed)
     */
    private static void maybeTraceObject(final int id, final KPE datum, final String message) {
        if (id == trackObjId) {
            System.err.print(message);
            printLineNumbers();
            System.err.println(datum.toString());
        }
    }

    private static void doInsert(final WorkloadOperation operation) throws TreeVerifier.FailedVerificationException {
        if (operation.isDeletingInsert()) {
            ((RRTree)tree.asTree()).setInsertionRemovesOldInsertion();
        }
        final KPE datum = operation.getObject();
        maybeTraceObject(operation.getId(), datum, "insert: ");
        insertions++;
        tree.insert(datum);
        tree.updateSpecificStats(inputFile.getLineNumber(), getUpdateAccesses(), insertions, deletions);
        treeVerifier.insert(datum);
        if ((inputFile.getLineNumber() == verifyLineNumber) || (operation.getId() == trackObjId) || verifyAlways
                || (verifyOnIO && (tree.wasBufferEmptied())))
            verify();
    }

    private static void doDelete(final WorkloadOperation operation) throws TreeVerifier.FailedVerificationException {
        final KPE datum = operation.getObject();
        maybeTraceObject(operation.getId(), datum, "delete: ");
        deletions++;
        tree.remove(datum);
        tree.updateSpecificStats(inputFile.getLineNumber(), getUpdateAccesses(), insertions, deletions);
        treeVerifier.remove(datum);
        if ((inputFile.getLineNumber() == verifyLineNumber) || (operation.getId() == trackObjId) || verifyAlways
                || (verifyOnIO && tree.wasBufferEmptied()))
            verify();
    }

    private static void doQuery(final WorkloadOperation operation) throws TreeVerifier.FailedVerificationException {
        final IOStatsState ioStatsState = testIO.statsSnapshot();
        final KPE queryDatum = operation.getQueryRectangle();
        queries++;
        final Descriptor queryRectangle = tree.getDescriptor(queryDatum);
        final Cursor<KPE> queryResults = tree.query(queryRectangle);
        final Set<KPE> queryResultColl = new HashSet<>();
        while (queryResults.hasNext()) {
            queriedObjects++;
            queryResultColl.add(queryResults.next());
        }
        queryResults.close();
        if (ioStatsState.insertsOrRemovesHappened())
        {
            System.err.println("Query caused an insert or delete!");
            printLineNumbers();
        }
        if (verifyAlways || verifyOnIO || verifyOnFinish)
            treeVerifier.verifyQuery(queryRectangle, queryResultColl);
        queryReads += ioStatsState.getReadDelta();
        queryWrites += ioStatsState.getUpdateDelta();
        if (verifyAlways || (doQueryPiggybacking && verifyOnIO))
            verify();

    }

    private static void printLineNumbers() {
        if (inputFile != null)
            System.err.println(" (input line " + inputFile.getLineNumber() + ')');
        if (tree != null)
            tree.printSpecificLineNumbers();
    }

    private static void printTreeFanoutStatistics() {
        final List<AggregateStats> entryAndNodeStats = new ArrayList<>(tree.getHeight());
        for (int i = 0; i < tree.getHeight(); i++)
            entryAndNodeStats.add(new AggregateStats());
        final Stack<Tree.IndexEntry> toProcess = new Stack<>();
        if (tree.getRootEntry() != null)
            toProcess.push(tree.getRootEntry());
        while (!toProcess.isEmpty()) {
            final Tree.IndexEntry entry = toProcess.pop();
            final Tree.Node node = entry.get();
            entryAndNodeStats.get(node.level()).registerValue(node.number());
            if (node.level() > 0) {
                //noinspection unchecked
                final Iterator<Tree.IndexEntry> i = node.entries();
                while (i.hasNext())
                    toProcess.push(i.next());
            }
        }
        for (int i = 0; i < entryAndNodeStats.size(); i++) {
            System.out.println ("Level: " + i + ", nodes: " + entryAndNodeStats.get(i).count()
                    + ", entries: " + entryAndNodeStats.get(i).totalInt()
                    + ", avg entries per node: " + entryAndNodeStats.get(i).average());
        }
    }

    private static void outputStatisticalData(final String fileName, final StatisticalData data) 
            throws FileNotFoundException {
        if (fileName.length() == 0)
            return;
        final PrintWriter output = new PrintWriter(fileName);
        for (final Map.Entry<Integer, Integer> datum : data.entrySet()) {
            output.println(datum.getKey() + " " + datum.getValue());
        }
        output.close();
    }
}
