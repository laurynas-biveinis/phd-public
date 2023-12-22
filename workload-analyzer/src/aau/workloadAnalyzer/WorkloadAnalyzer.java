/*
     Copyright (C) 2011 Laurynas Biveinis

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

package aau.workloadAnalyzer;

import aau.workload.SpatialExtent;
import aau.workload.WorkloadOperation;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Reads the data file and outputs world boundaries and the maximum possible number of operations.
 */
public class WorkloadAnalyzer {

    private static final String INPUT_OPTION = "input";

    private WorkloadAnalyzer() { }

    public static void main(final String[] args) {
        final OptionParser optParser = new OptionParser();

        final OptionSpec<String> inputOption = optParser.accepts(INPUT_OPTION).withRequiredArg().ofType(String.class);

        final OptionSet options = optParser.parse(args);

        if (!options.has(inputOption)) {
            throw new IllegalArgumentException("Input file not specified!");
        }
        final String inputFileName = options.valueOf(inputOption);
        final String outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf('.')).concat(".info");

        System.out.println("Workload Analyzer:");
        System.out.println("    input: " + inputFileName);
        System.out.println("    output: " + outputFileName);
        System.out.print("    postprocessing... ");
        try {
            analyze(inputFileName, outputFileName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done!");
    }

    private static void analyze(final String inputFileName, final String outputFileName) throws IOException {
        PrintWriter output = null;
        try {
            final BufferedReader input = new BufferedReader(new FileReader(inputFileName));
            output = new PrintWriter(outputFileName);

            String s;
            int numUpdates = 0;
            SpatialExtent worldExtent = SpatialExtent.minimum();

            while ((s = input.readLine()) != null) {
                final WorkloadOperation op = new WorkloadOperation(s);
                if (op.isUpdate())
                    numUpdates++;

                worldExtent = worldExtent.include(op.getSpatialExtent());
                /* To help to clean the SPF dataset */
                if (worldExtent.x1() < 0 || worldExtent.y1() < 0 || worldExtent.x2() < 0 || worldExtent.y2() < 0) {
                    System.err.println("Became negative! numUpdates = " + numUpdates);
                    System.err.println("wordExtent = " + worldExtent.toString());
                    System.err.println("op = " + op.toString());
                    System.err.println("s = " + s);
                }
            }
            input.close();

            output.println(inputFileName.substring(inputFileName.lastIndexOf('/') + 1, inputFileName.length()));
            output.println("World extent (x1 y1 x2 y2): ");  
            output.println("" + worldExtent.x1() + ' ' + worldExtent.y1() + ' ' + worldExtent.x2()
                    + ' ' + worldExtent.y2());
            output.println("Number of updates: ");
            output.println(numUpdates);
        }
        finally {
            if (output != null)
                output.close();
        }
    }
}
