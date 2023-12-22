/*
     Copyright (C) 2007, 2008, 2009, 2010, 2011 Laurynas Biveinis

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
package aau.uniformDataGenerator;

import aau.workload.SpatialExtent;
import aau.workload.WorkloadOperation;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Simple uniform dataset generator
 **/
public class UniformDataGenerator {
    private static final String OUTPUT_OPTION = "output";
    private static final String NUM_OBJECTS = "numobjects";
    private static final String NUM_OPS = "numops";
    private static final String NUM_DELETINGINSERTIONS = "numdelins";

    private static final double OBJECT_EXTENT = 0.2D;
    private static final int WORLD_SIZE = 100000; // - OBJECT_EXTENT
    private static final Random rng = new Random();

    private UniformDataGenerator() {
    }

    public static void main(final String[] args) {
        final OptionParser optParser = new OptionParser();
        final OptionSpec<String> outputOption = optParser.accepts(OUTPUT_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<Integer> numObjectsOption
                = optParser.accepts(NUM_OBJECTS).withRequiredArg().ofType(Integer.class);
        final OptionSpec<Integer> numOpsOption = optParser.accepts(NUM_OPS).withRequiredArg().ofType(Integer.class);
        //noinspection unchecked
        final OptionSpec<Integer> numDeletingInsertionsOption
                = optParser.accepts(NUM_DELETINGINSERTIONS).withRequiredArg().ofType(Integer.class).defaultsTo(0);

        final OptionSet options = optParser.parse(args);

        if (!options.has(outputOption)) {
            System.err.println("Output file not specified!");
            System.exit(1);
        }
        final String outputFileName = options.valueOf(outputOption);

        if (!options.has(numObjectsOption)) {
            System.err.println("Number of objects not specified!");
            System.exit(1);
        }
        final int numObjs = options.valueOf(numObjectsOption);

        if (!options.has(numOpsOption)) {
            System.err.println("Number of operations not specified!");
            System.exit(1);
        }
        final int numOps = options.valueOf(numOpsOption);

        final int numDeletingInsertions = options.valueOf(numDeletingInsertionsOption);

        System.out.println("Uniform Data Generator:");
        System.out.println("    Output: " + outputFileName);
        System.out.println("    Number of objects: " + numObjs);
        System.out.println("    Number of operations: " + numOps);
        System.out.println("    Number of deleting insertions: " + numDeletingInsertions);
        System.out.print(  "    Generating... ");
        try {
            generate(outputFileName, numObjs, numOps, numDeletingInsertions);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("done.");
    }

    private static void generate(final String outputFileName, final int numObjs, final int numOps, final int numDelIns)
            throws FileNotFoundException {
        final Map<Integer, SpatialExtent> objects = new HashMap<Integer, SpatialExtent>(numObjs);
        final PrintWriter output = new PrintWriter(outputFileName);
        int i;
        for (i = 0; i < numObjs; i++) {
            final SpatialExtent datum = makeRandomExtent();
            objects.put(i, datum);
            final WorkloadOperation op = new WorkloadOperation(WorkloadOperation.OperationType.INSERT, i, datum);
            op.write(output);
            output.println();
        }
        WorkloadOperation op;
        for (i = 0; i < numOps; i++) {
            final int id = rng.nextInt(numObjs);
            final SpatialExtent oldDatum = objects.get(id);
            op = new WorkloadOperation(WorkloadOperation.OperationType.DELETE, id, oldDatum);
            op.write(output);
            output.println();
            final SpatialExtent newDatum = makeRandomExtent();
            op = new WorkloadOperation(WorkloadOperation.OperationType.INSERT, id, newDatum);
            op.write(output);
            output.println();
            objects.put(id, newDatum);
        }
        for (i = 0; i < numDelIns; i++) {
            final int id = rng.nextInt(numObjs);
            final SpatialExtent newDatum = makeRandomExtent();
            op = new WorkloadOperation(WorkloadOperation.OperationType.DELETING_INSERT, id, newDatum);
            op.write(output);
            output.println();
        }
        output.close();
    }

    private static SpatialExtent makeRandomExtent() {
        final double x1 = rng.nextInt(WORLD_SIZE);
        final double y1 = rng.nextInt(WORLD_SIZE);
        final double x2 = x1 + OBJECT_EXTENT;
        final double y2 = y1 + OBJECT_EXTENT;
        return new SpatialExtent(x1, y1, x2, y2);
    }
}
