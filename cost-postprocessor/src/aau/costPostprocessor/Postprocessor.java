/*
     Copyright (C) 2007, 2009 Laurynas Biveinis

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
package aau.costPostprocessor;

import aau.workload.Coords;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Postprocesses COST generated workloads into driver format
 */
final class Postprocessor {
    private static final String INPUT_OPTION = "input";
    private static final String OUTPUT_OPTION = "output";

    private static final Pattern LINE_PATTERN = Pattern.compile(".*\r?\n");

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final CharsetDecoder CHARSET_DECODER = CHARSET.newDecoder();

    private static final double OBJECT_EXTENT = 0.2D;

    private Postprocessor() {
    }

    public static void main(final String[] args) {
        final OptionParser optParser = new OptionParser();
        final OptionSpec<String> inputOption = optParser.accepts(INPUT_OPTION).withRequiredArg();
        final OptionSpec<String> outputOption = optParser.accepts(OUTPUT_OPTION).withRequiredArg();
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

        System.out.println("Brinkhoff postprocessor:");
        System.out.println("    Input: "+ inputFileName);
        System.out.println("    Output: " + outputFileName);

        System.out.print(  "    Postprocessing... ");
        try {
            doPostprocessing(inputFileName, outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done.");
    }

    private static void doPostprocessing(final String inputFileName, final String outputFileName) throws IOException {
        PrintWriter output = null;
        FileInputStream inputStream = null;
        FileChannel inputChannel = null;
        try {
            output = new PrintWriter(outputFileName);
            inputStream = new FileInputStream(inputFileName);
            inputChannel = inputStream.getChannel();
            final MappedByteBuffer input = inputChannel.map(FileChannel.MapMode.READ_ONLY, 0L, inputChannel.size());
            final CharBuffer charInput = CHARSET_DECODER.decode(input);
            final Matcher lineMatcher = LINE_PATTERN.matcher(charInput);
            int lineNumber = 0;
            while (lineMatcher.find()) {
                lineNumber++;
                final String s = lineMatcher.group();
                final char command = s.charAt(0);
                switch (command) {
                    case 't':
                        break; // Ignore time set command
                    case 'a':
                        break; // Ignore time advance command
                    case 'i':  // Insertion
                    case 'd': {  // Deletion
                        doUpdate(output, s, command);
                        break;
                    }
                    case 's': { // Query
                        doQuery(output, s);
                        break; }
                    default: throw new IllegalArgumentException("Unrecognized command in input file, lineNumber = "
                            + lineNumber);
                }
            }
        }
        finally {
            if (output != null)
                output.close();
            if (inputChannel != null)
                inputChannel.close();
            if (inputStream != null)
                inputStream.close();
        }
    }

    private static void doQuery(PrintWriter output, String s) {
        int start = s.indexOf('(') + 1;
        int end = s.indexOf(',');
        final double x1 = Double.parseDouble(s.substring(start, end));
        start = end + 1;
        end = s.indexOf(',', start);
        final double y1 = Double.parseDouble(s.substring(start, end));
        final Coords coords1 = new Coords(x1, y1);
        start = end + 1;
        end = s.indexOf(',', start);
        final Double x2 = Double.parseDouble(s.substring(start, end));
        start = end + 1;
        end = s.indexOf(',', start);
        final Double y2 = Double.parseDouble(s.substring(start, end));
        final Coords coords2 = new Coords(x2, y2);
        writeQuery(output, coords1, coords2);
    }

    private static void doUpdate(PrintWriter output, String s, char command) {
        int start = s.indexOf('(') + 1;
        int end = s.indexOf(',');
        final double x = Double.parseDouble(s.substring(start, end));
        start = end + 1;
        end = s.indexOf(',', start);
        final double y = Double.parseDouble(s.substring(start, end));
        final Coords coords = new Coords(x, y);
        start = s.indexOf(')', end) + 3;
        end = s.indexOf(',', start);
        final int id = Integer.parseInt(s.substring(start, end)) + 1;
        if (command == 'i')
            writeInsert(output, id, coords);
        else
            writeDelete(output, id, coords);
    }

    private static void writeQuery(final PrintWriter output, final Coords queryBottomLeft, final Coords queryTopRight) {
        output.print("query \t");
        queryBottomLeft.printCoords(output);
        output.print(' ');
        queryTopRight.printCoords(output);
        output.println();
    }

    private static void writeDelete(final PrintWriter output, final int objectId, final Coords coords) {
        output.print("delete\t");
        output.print(objectId);
        output.print(" \t");
        coords.printCoords(output);
        output.print(' ');
        final Coords otherCorner = new Coords(coords.x + OBJECT_EXTENT, coords.y + OBJECT_EXTENT);
        otherCorner.printCoords(output);
        output.println();
    }

    private static void writeInsert(final PrintWriter output, final int objectId, final Coords coords) {
        output.print("insert\t");
        output.print(objectId);
        output.print(" \t");
        coords.printCoords(output);
        output.print(' ');
        final Coords otherCorner = new Coords(coords.x + OBJECT_EXTENT, coords.y + OBJECT_EXTENT);
        otherCorner.printCoords(output);
        output.println();
    }
}
