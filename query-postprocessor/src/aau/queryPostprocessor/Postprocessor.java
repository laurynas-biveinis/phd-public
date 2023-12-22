/*
     Copyright (C) 2007, 2010, 2011 Laurynas Biveinis

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

package aau.queryPostprocessor;

import aau.workload.RandomQueryGenerator;
import aau.workload.SpatialExtent;
import aau.workload.WorkloadOperation;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

/**
 * Postprocesses workloads to include queries
 */
final class Postprocessor {

    private static final String INPUT_OPTION = "input";
    private static final String OUTPUT_OPTION = "output";
    private static final String QUERY_RATIO_OPTION = "queryratio";
    private static final String UPDATE_RATIO_OPTION = "updateratio";
    private static final String QUERY_REGION_SIZE = "queryregionsize";
    private static final String OUTPUT_SIZE_OPTION = "outputsize";
    private static final String INITIAL_INSERTIONS_OPTION = "initialinsertions";

    private Postprocessor() {
    }

    /**
     * Invokes a postprocessor-converter for Brinkhoff's generated data.
     * @param args "--input input_file --output output_file [--query-ratio ratio]"
     */
    public static void main(final String[] args) {

        final OptionParser optParser = new OptionParser();
        final OptionSpec<String> inputOption = optParser.accepts(INPUT_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<String> outputOption = optParser.accepts(OUTPUT_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<Integer> queryRatioOption
                = optParser.accepts(QUERY_RATIO_OPTION).withRequiredArg().ofType(Integer.class);
        final OptionSpec<Integer> updateRatioOption
                = optParser.accepts(UPDATE_RATIO_OPTION).withRequiredArg().ofType(Integer.class);
        final OptionSpec<Double> queryRegionSizeOption
                = optParser.accepts(QUERY_REGION_SIZE).withRequiredArg().ofType(Double.class);
        //noinspection unchecked
        final OptionSpec<Integer> outputSizeOption
                = optParser.accepts(OUTPUT_SIZE_OPTION).withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        //noinspection unchecked
        final OptionSpec<Integer> initialInsertionsOption
                = optParser.accepts(INITIAL_INSERTIONS_OPTION).withRequiredArg().ofType(Integer.class).defaultsTo(0);
        final OptionSet options = optParser.parse(args);

        if (!options.has(inputOption)) {
            throw new IllegalArgumentException("Input file not specified!");
        }
        final String inputFileName = options.valueOf(inputOption);

        if (!options.has(OUTPUT_OPTION)) {
            throw new IllegalArgumentException("Output file not specified!");
        }

        final String outputFileName = options.valueOf(outputOption);
        final int numOperations = options.valueOf(outputSizeOption);
        final int initialInsertions = options.valueOf(initialInsertionsOption);

        int queryRatio = 0;
        int updateRatio = 1;
        double queryRegionSize = 0.0;
        if (options.has(queryRatioOption) && options.has(queryRegionSizeOption)) {
            queryRatio = options.valueOf(queryRatioOption);
            queryRegionSize = options.valueOf(queryRegionSizeOption);
            if (options.has(updateRatioOption))
            {
                updateRatio = options.valueOf(updateRatioOption);
                if ((updateRatio != 1) && (queryRatio != 1))
                    {
                        System.err.println ("Query ratio: " + queryRatio);
                        throw new IllegalArgumentException("If update ratio is specified, query ratio must be 1");
                    }
            }
        }
        else if (options.has(queryRatioOption)) {
            throw new IllegalArgumentException("Query ratio specified without query region size!");
        }
        else if (options.has(queryRegionSizeOption)) {
            throw new IllegalArgumentException("Query region size specified without query ratio!");
        }

        System.out.println("Query postprocessor:");
        System.out.println("    Input: "+ inputFileName);
        System.out.println("    Output: " + outputFileName);
        System.out.println("    Query ratio: " + queryRatio);
        System.out.println("    Update ratio: " + updateRatio);
        System.out.println("    Query region size: " + queryRegionSize);
        System.out.println("    Initial insertions: " + initialInsertions);
        System.out.print(  "    Total number of operations: ");
        if (numOperations == -1) {
            System.out.println("maximum possible");
        }
        else {
            System.out.println(numOperations);
        }
        System.out.print(  "    Postprocessing... ");

        try {
            doPostprocessing(inputFileName, outputFileName, queryRatio, updateRatio, queryRegionSize, numOperations,
                    initialInsertions);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("done.");
    }

    private static void doPostprocessing(final String inputFileName, final String outputFileName,
                                         final int queryRatio, final int updateRatio, final double queryRegionSize,
                                         final int numOperations, final int initialInsertions) throws IOException {
        PrintWriter output = null;
        try {
            BufferedReader input = new BufferedReader(new FileReader(inputFileName));

            output = new PrintWriter(outputFileName);

            // TODO: copy paste with workload-analyzer
            int maxUpdates = 0;
            SpatialExtent worldExtent = SpatialExtent.minimum();
            if (queryRatio > 0) {
                // First pass: calculate world boundaries
                String s;
                while ((s = input.readLine()) != null) {
                    final WorkloadOperation op = new WorkloadOperation(s);
                    if (op.isUpdate())
                        maxUpdates++;

                    worldExtent = worldExtent.include(op.getSpatialExtent());
                    /* To help to clean the SPF dataset */
                    if (worldExtent.x1() < 0 || worldExtent.y1() < 0 || worldExtent.x2() < 0 || worldExtent.y2() < 0) {
                        System.err.println("Became negative! maxUpdates = " + maxUpdates);
                        System.err.println("wordExtent = " + worldExtent.toString());
                        System.err.println("op = " + op.toString());
                        System.err.println("s = " + s);
                    }
                }
                input.close();
                input = new BufferedReader(new FileReader(inputFileName));
            }

            System.out.println(" maximum possible operations: " + maxUpdates);
            if (maxUpdates + maxUpdates % queryRatio < numOperations) {
                throw new IllegalArgumentException("Requested more operations than possible with this dataset!");
            }
            System.out.println("World boundaries: " + worldExtent.toString());

            if (queryRatio == 0)
                return;

            // TODO: copy-paste with QueryGenerator
            final RandomQueryGenerator rndQGen = new RandomQueryGenerator(queryRegionSize, worldExtent);

            final Collection<Integer> activeIds = new HashSet<Integer>(initialInsertions);

            int lineNumber = 0;
            int seenInsertions = 0;
            int seenOthers = 0;
            String s;
            System.out.print("Initial insertions... ");
            // Initial insertions do not count towards the total operations
            while (((s = input.readLine()) != null) && (seenInsertions < initialInsertions))
            {
                lineNumber++;
                final WorkloadOperation op = new WorkloadOperation(s);
                checkObjectId(activeIds, lineNumber, op);

                if (op.isInsert())
                    seenInsertions++;
                else
                    seenOthers++;
                op.write(output);
                output.println();
            }
            System.out.println("insertions: " + seenInsertions + ", others: " + seenOthers);

            int generatedOps = 0;
            int queries = 0;

            System.out.print("Updates and queries... ");
            while ((s != null) && ((numOperations == -1) || (generatedOps < numOperations))) {
                lineNumber++;
                final WorkloadOperation op = new WorkloadOperation(s);
                checkObjectId(activeIds, lineNumber, op);
                op.write(output);
                output.println();
                generatedOps++;

                if (lineNumber % queryRatio == 0) {
                    int k = 0;
                    while (k < updateRatio)
                    {
                        queries++;
                        generatedOps++;
                        final WorkloadOperation queryOp = rndQGen.generateQuery();
                        queryOp.write(output);
                        output.println();
                        k++;
                    }
                }
                s = input.readLine();
            }
            input.close();
            System.out.println("queries generated: " + queries);
        }
        finally {
            if (output != null)
                output.close();
        }
    }

    private static void checkObjectId(Collection<Integer> activeIds, int lineNumber, WorkloadOperation op) {
        if (op.isInsert()) {
            if (!activeIds.add(op.getId())) {
                throw new IllegalStateException("Error in input file line = " + lineNumber
                        + ": object id = " + op.getId() +" already inserted!");
            }
        }
        else if (op.isKindOfDelete()) {
            if (!activeIds.remove(op.getId())) {
                throw new IllegalStateException("Error in input file line = " + lineNumber
                        + ": object id = " + op.getId() + " not inserted!");
            }
        }
    }
}
