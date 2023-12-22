/*
     Copyright (C) 2009 Laurynas Biveinis

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
package aau.thresholdFilter;

import aau.workload.SpatialExtent;
import aau.workload.WorkloadOperation;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter a given workload by distance threshold criteria
 */
class ThresholdFilter {

    private static final String INPUT_OPTION = "input";
    private static final String OUTPUT_OPTION = "output";
    private static final String THRESHOLD_OPTION = "threshold";
    private static final String VERBOSE_OPTION = "verbose";

    private ThresholdFilter() {
    }

    public static void main(final String[] args) {
        final OptionParser optParser = new OptionParser();
        final OptionSpec<String> inputOption = optParser.accepts(INPUT_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<String> outputOption = optParser.accepts(OUTPUT_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<Integer> thresholdOption
                = optParser.accepts(THRESHOLD_OPTION).withRequiredArg().ofType(Integer.class);
        final OptionSpec<Void> verboseOption = optParser.accepts(VERBOSE_OPTION);

        final OptionSet options = optParser.parse(args);

        if (!options.has(inputOption)) {
            System.err.println("Input file not specified!");
            System.exit(1);
        }
        final String inputFileName = options.valueOf(inputOption);

        if (!options.has(outputOption)) {
            System.err.println("Output file not specified!");
            System.exit(2);
        }
        final String outputFileName = options.valueOf(outputOption);

        if (!options.has(thresholdOption)) {
            System.err.println("Threshold not specified!");
            System.exit(3);
        }
        final int threshold = options.valueOf(thresholdOption);

        final boolean verbose = options.has(verboseOption);

        System.out.println("Threshold filter:");
        System.out.println("    Input: " + inputFileName);
        System.out.println("    Output: " + outputFileName);
        System.out.println("    Threshold: " + threshold + " meters");
        System.out.println("    Verbose: " + verbose);

        try {
            doFiltering(inputFileName, outputFileName, threshold, verbose);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class LastUpdateInfo {
        final WorkloadOperation op;

        LastUpdateInfo (final WorkloadOperation op) {
            this.op = op;
        }

        public boolean equals(final Object o) {
            return o instanceof LastUpdateInfo && op.equals(((LastUpdateInfo) o).op);
        }

        public int hashCode() {
            return op.hashCode();
        }
    }

    private static void doFiltering(final String inputFileName, final String outputFileName, final int threshold,
                            final boolean verbose)
            throws IOException {
        final BufferedReader input = new BufferedReader(new FileReader(inputFileName));
        final PrintWriter output = new PrintWriter(outputFileName);
        String inputLine;

        final Map<Integer, LastUpdateInfo> lastUpdates = new HashMap<Integer, LastUpdateInfo>();

        int totalOps = 0;
        int writtenOps = 0;

        while ((inputLine = input.readLine()) != null) {
            final WorkloadOperation op = new WorkloadOperation(inputLine);
            totalOps++;
            if (op.isInsert()) {
                final LastUpdateInfo lastOp = lastUpdates.get(op.getId());
                if (lastOp != null) {
                    if (op.distance(lastOp.op) > threshold) {
                        final WorkloadOperation delOp = new WorkloadOperation(WorkloadOperation.OperationType.DELETE,
                                lastOp.op.getId(), lastOp.op.getSpatialExtent());
                        writtenOps = printOp(delOp, output, writtenOps);
                        lastUpdates.put(op.getId(), new LastUpdateInfo(op));
                        writtenOps = printOp(op, output, writtenOps);
                    }
                }
                else {
                    lastUpdates.put(op.getId(), new LastUpdateInfo(op));
                    writtenOps = printOp(op, output, writtenOps);
                }
            }
            else if (!op.isKindOfDelete())
                writtenOps = printOp(op, output, writtenOps);
            if (op.isUpdate()) {
                final SpatialExtent s = op.getSpatialExtent();
                if (s.x1() < 0 || s.y1() < 0 || s.x2() < 0 || s.y2() < 0) {
                    System.err.println("Became negative! totalOps = " + totalOps);
                    System.err.println("op = " + op.toString());
                    System.err.println("inputLine = " + inputLine);
                }
            }
        }
        input.close();
        output.close();

        if (verbose) {
            final int filteredOps = totalOps - writtenOps;
            System.out.println("Total operations: " + totalOps);
            System.out.println("Written operations: " + writtenOps + ", " + (float)writtenOps / totalOps * 100 + '%');
            System.out.println("Filtered operations: " + filteredOps + ", "
                    + (float)filteredOps / totalOps * 100 + '%');
        }
    }

    private static int printOp(final WorkloadOperation op, final PrintWriter output, final int writtenOps) {
        op.write(output);
        output.println();
        return writtenOps + 1;
    }
}
