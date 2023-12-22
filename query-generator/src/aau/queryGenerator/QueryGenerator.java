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
package aau.queryGenerator;

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

/**
 * Generates queries with given parameters for a given data files
 */
public class QueryGenerator {

    private static final String INFO_OPTION = "info";
    private static final String QUERY_REGION_SIZE_OPTION = "queryregionsize";
    private static final String QUERY_NUMBER_OPTION = "querynumber";
    private static final String OUTPUT_OPTION = "output";

    private QueryGenerator() { }

    public static void main(final String[] args) {
        final OptionParser optParser = new OptionParser();

        final OptionSpec<String> infoOption = optParser.accepts(INFO_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<Double> queryRegionSizeOption
                = optParser.accepts(QUERY_REGION_SIZE_OPTION).withRequiredArg().ofType(Double.class);
        final OptionSpec<Integer> queryNumberOption
                = optParser.accepts(QUERY_NUMBER_OPTION).withRequiredArg().ofType(Integer.class);
        final OptionSpec<String> outputOption = optParser.accepts(OUTPUT_OPTION).withRequiredArg().ofType(String.class);

        final OptionSet options = optParser.parse(args);

        if (!options.has(infoOption)) {
            throw new IllegalArgumentException("Info file name not specified!");
        }

        if (!options.has(queryRegionSizeOption)) {
            throw new IllegalArgumentException("Query region size not specified!");
        }

        if (!options.has(queryNumberOption)) {
            throw new IllegalArgumentException("Number of queries not specified!");
        }

        if (!options.has(outputOption)) {
            throw new IllegalArgumentException("Output file name not specified!");
        }

        final String infoFileName = options.valueOf(infoOption);
        final double queryRegionSize = options.valueOf(queryRegionSizeOption);
        final int queryNumber = options.valueOf(queryNumberOption);
        final String outputFileName = options.valueOf(outputOption);

        System.out.println("Query Generator:");
        System.out.println("    info file: " + infoFileName);
        System.out.println("    query region size: " + queryRegionSize);
        System.out.println("    query number: " + queryNumber);
        System.out.println("    output file: " + outputFileName);

        System.out.print("    generating... ");
        try {
            generate(infoFileName, queryRegionSize, queryNumber, outputFileName);
            System.out.println("done!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void generate (final String infoFileName, final double queryRegionSize, final int queryNumber,
                                  final String outputFileName) throws IOException {
        // Read the world size
        String s;
        final BufferedReader infoInput = new BufferedReader(new FileReader(infoFileName));
        // Eat the 1st line: file name
        infoInput.readLine();
        // Eat the 2nd line: "World extent (x1 y1 x2 y2):"
        infoInput.readLine();
        // Read the actual extent on the 3rd line
        s = infoInput.readLine();
        final SpatialExtent worldExtent = new SpatialExtent(s);
        infoInput.close();

        final RandomQueryGenerator rndQGen = new RandomQueryGenerator(queryRegionSize, worldExtent);
        final PrintWriter output = new PrintWriter(outputFileName);
        for (int i = 0; i < queryNumber; i++) {
            final WorkloadOperation query = rndQGen.generateQuery();
            query.write(output);
            output.println();
        }
        output.close();
    }

}
