/*
     Copyright (C) 2009, 2012 Laurynas Biveinis

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
package aau.spfPostprocessor;

import com.csvreader.CsvReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.*;
import java.util.*;

/**
 * Postprocessor for SPF data files
 */
public class Postprocessor {

    /**
     * Roughly the number of different objects in the original SPF dataset
     */
    private static final int ORIGINAL_OBJECTS = 119;

    private static final int YEAR_MULT = 11;
    private static final int YEAR_BASE = 2000;
    private static final int MONTH_MULT = 13;
    private static final int DAY_MULT = 32;
    private static final int HOUR_MULT = 25;

    private static final double LOWER_X_BOUND = 400000.0;
    private static final double LOWER_Y_BOUND = 6000000.0;
    private static final double UPPER_X_BOUND = 700000.0;
    private static final double UPPER_Y_BOUND = 7000000.0;

    private static final class OutputUpdate implements Comparable<OutputUpdate> {
        private final int id;
        private final int x;
        private final int y;
        private final int h;
        private final int m;
        private final int s;
        private final int seqId;

        public static OutputUpdate read(final BufferedReader r) throws IOException {
            final String record = r.readLine();
            if (record == null)
                return null;

            int i = 0;
            int h = 0;
            while (record.charAt(i) != ' ') {
                h = h * 10 + (record.charAt(i) - '0');
                i++;
            }
            i++;
            int m = 0;
            while (record.charAt(i) != ' ') {
                m = m * 10 + (record.charAt(i) - '0');
                i++;
            }
            i++;
            int s = 0;
            while (record.charAt(i) != ' ') {
                s = s * 10 + (record.charAt(i) - '0');
                i++;
            }
            i++;
            int id = 0;
            while (record.charAt(i) != ' ') {
                id = id * 10 + (record.charAt(i) - '0');
                i++;
            }
            i++;
            int x = 0;
            while (record.charAt(i) != ' ') {
                x = x * 10 + (record.charAt(i) - '0');
                i++;
            }
            i++;
            int y = 0;
            while (i < record.length()) {
                y = y * 10 + (record.charAt(i) - '0');
                i++;
            }

            return new OutputUpdate(id, x, y, h, m, s, 0);
        }

        OutputUpdate(final int id, final int x, final int y, final int h, final int m, final int s, final int seqId) {
            this.x = x;
            this.y = y;
            this.id = id;
            this.h = h;
            this.m = m;
            this.s = s;
            this.seqId = seqId; 
        }

        public int getId() {
            return id;
        }

        public int compareTo(final OutputUpdate o) {
            int result = h - o.h;
            if (result == 0)
                result = m - o.m;
            if (result == 0)
                result = s - o.s;
            if (result == 0)
                result = seqId - o.seqId;
            return result;
        }

        public boolean equals(final Object o) {
            if (!(o instanceof OutputUpdate))
                return false;
            final OutputUpdate u = (OutputUpdate)o;
            return (id == u.id) && (h == u.h) && (m == u.m) && (s == u.s) && (x == u.x) && (y == u.y)
                    && (seqId == u.seqId);
        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + id;
            result = 37 * result + h;
            result = 37 * result + m;
            result = 37 * result + s;
            result = 37 * result + x;
            result = 37 * result + y;
            return result;
        }

        public String toString() {
            return id + " @ " + h + ':' + m + ':' + s + " x: " + x + " y: " + y + " (seqId = " + seqId + ')';
        }

        public void print(final PrintWriter output) {
            output.print(h);
            output.print(' ');
            output.print(m);
            output.print(' ');
            output.print(s);
            output.print(' ');
            output.print(id);
            output.print(' ');
            output.print(x);
            output.print(' ');
            //noinspection SuspiciousNameCombination
            output.println(y);
        }
    }

    private static final class SimpleDate {
        final int y;
        final int m;
        final int d;
        final int h;
        final int min;
        final int sec;

        @SuppressWarnings({"MagicNumber"})
        SimpleDate(final CharSequence s) {
            // yyyy-MM-dd HH:mm:ss
            y = (s.charAt(0) - '0') * 1000 + (s.charAt(1) - '0') * 100 + (s.charAt(2) - '0') * 10 + s.charAt(3) - '0';
            m = (s.charAt(5) - '0') * 10 + s.charAt(6) - '0';
            d = (s.charAt(8) - '0') * 10 + s.charAt(9) - '0';
            h = (s.charAt(11) - '0') * 10 + s.charAt(12) - '0';
            min = (s.charAt(14) - '0') * 10 + s.charAt(15) - '0';
            sec = (s.charAt(17) - '0') * 10 + s.charAt(18) - '0';
        }
    }
    
    private static final String INPUT_OPTION = "input";
    private static final String OUTPUT_OPTION = "output";
    private static final String HOUR_PERIOD_OPTION = "hourperiod";
    private static final String STATS_OPTION = "stats";

    public static void main(final String[] args) {

        final OptionParser optParser = new OptionParser();
        final OptionSpec<String> inputOption = optParser.accepts(INPUT_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<String> outputOption = optParser.accepts(OUTPUT_OPTION).withRequiredArg().ofType(String.class);
        final OptionSpec<Integer> hourPeriodOption
                = optParser.accepts(HOUR_PERIOD_OPTION).withRequiredArg().ofType(Integer.class);
        final OptionSpec<Void> statsOption = optParser.accepts(STATS_OPTION);
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

        if (!options.has(hourPeriodOption)) {
            System.err.println("Hour period not specified!");
            System.exit(3);
        }
        final int hourPeriod = options.valueOf(hourPeriodOption); 

        System.out.println("SPF postprocessor:");
        System.out.println("    Input list: "+ inputFileName);
        System.out.println("    Output: " + outputFileName);
        System.out.println("    Hour period: " + hourPeriod);

        System.out.println(  "    Postprocessing... ");
        final Postprocessor postproc = new Postprocessor(hourPeriod, options.has(statsOption));
        try {
            postproc.doPostprocessing(inputFileName, outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done.");
    }

    /**
     * Number of updates for each original ID encountered
     */
    private final Map<Integer, Integer> originalIDUpdates;
    private final Collection<Integer> newIDs;
    private final int hourPeriod;

    private Postprocessor(final int hourPeriod, final boolean enableStats) {
        this.hourPeriod = hourPeriod;
        originalIDUpdates = enableStats ? new HashMap<Integer, Integer>(ORIGINAL_OBJECTS) : null;
        newIDs = enableStats ? new HashSet<Integer>() : null;
    }

    private int totalNumOfRecords = 0;
    private int currentNumOfRecords = 0;
    private int numOfSkippedRecords = 0;
    private long totalmiliseconds = 0;

    @SuppressWarnings({"TypeMayBeWeakened"})
    private final SortedSet<OutputUpdate> updatesToOutput = new TreeSet<>();

    private void doPostprocessing(final String inputFileListFileName, final String outputFileName)
            throws IOException {
        final BufferedReader input = new BufferedReader(new FileReader(inputFileListFileName));
        String inputFileName;
        while ((inputFileName = input.readLine()) != null ) {
            if (inputFileName.equals("savetemp"))
                savePartialOutput();
            else
                readInSingleFile(inputFileName);
        }
        printReadingStats();
        writePostprocessedFile(outputFileName);
    }

    private void readInSingleFile(final String inputFileName)
            throws IOException {
        final long msBefore = System.currentTimeMillis();
        final long numRecordsBefore = totalNumOfRecords;
        System.out.print("Reading " + inputFileName + "... ");
        final CsvReader reader = new CsvReader(inputFileName);
        int lineNumber = 0;
        String currentRecordStr = "";
        try {
            while (reader.readRecord()) {
                currentRecordStr = reader.getRawRecord();
                if (currentRecordStr.startsWith("Elapsed:")) {
                    lineNumber++;
                    continue;
                }
                totalNumOfRecords++;
                currentNumOfRecords++;

                final int x = Integer.parseInt(reader.get(2));
                final int y = Integer.parseInt(reader.get(3));
                if ((x < LOWER_X_BOUND) || (y < LOWER_Y_BOUND) || (x > UPPER_X_BOUND) || (y > UPPER_Y_BOUND)) {
                    numOfSkippedRecords++;
                    continue;
                }

                final int id = Integer.parseInt(reader.get(0));

                if (originalIDUpdates != null) {
                    final Integer oldOrigIdUpdates = originalIDUpdates.get(id);
                    final int newOrigIdUpdates = oldOrigIdUpdates != null ? oldOrigIdUpdates + 1 : 1;
                    originalIDUpdates.put(id, newOrigIdUpdates);
                }

                final SimpleDate d = new SimpleDate(reader.get(1));

                int newRID = id;
                newRID = newRID * YEAR_MULT + d.y  - YEAR_BASE;
                newRID = newRID * MONTH_MULT + d.m;
                newRID = newRID * DAY_MULT + d.d;
                newRID = newRID * HOUR_MULT + d.h / hourPeriod; // + adjustedHour;

                if (newIDs != null)
                    newIDs.add(newRID);

                final int adjustedHour = d.h % hourPeriod;
                final OutputUpdate update = new OutputUpdate(newRID, x, y, adjustedHour,
                        d.min, d.sec, totalNumOfRecords);
                updatesToOutput.add(update);

                assert currentNumOfRecords == updatesToOutput.size();
                lineNumber++;
            }
        }
        catch (NumberFormatException ignored)
        {
            throw new NumberFormatException("Line = \"" + currentRecordStr + "\", Bad number at line number = " + lineNumber);
        }
        reader.close();
        final long duration = System.currentTimeMillis() - msBefore;
        System.out.println("in " + duration + "ms, "
                + ((totalNumOfRecords - numRecordsBefore) / duration * 1000) + " records/s");
        totalmiliseconds += duration;
    }

    private final Collection<File> tmpOutputFiles = new ArrayList<>();
    private final Collection<Integer> seenOutputIDs = new HashSet<>();
    private static final File currentDir = new File(".");

    private void savePartialOutput() throws IOException {
        System.out.print("Saving partial output to temp file... ");
        final long msBefore = System.currentTimeMillis();

        final File tmpFile = File.createTempFile("RRTree", String.valueOf(tmpOutputFiles.size()), currentDir);
        tmpOutputFiles.add(tmpFile);
        final PrintWriter output = new PrintWriter(tmpFile);

        int outputRows = 0;
        for (final OutputUpdate u : updatesToOutput) {
            u.print(output);
            outputRows++;
        }
        output.close();
        final long duration = System.currentTimeMillis() - msBefore;
        System.out.println("in " + duration + "ms, " + (outputRows / duration * 1000) + " records/s");

        updatesToOutput.clear();
        currentNumOfRecords = 0;
    }

    private void printReadingStats() {
        System.out.println("Records read: " + totalNumOfRecords);
        System.out.println("Throughput: " + (totalNumOfRecords / totalmiliseconds * 1000) + " records/s");

        if (originalIDUpdates != null) {
            System.out.println("Unique original objects seen: " + originalIDUpdates.size());
            int total = 0;
            for (final Map.Entry<Integer, Integer> x : originalIDUpdates.entrySet()) {
                total += x.getValue();
            }
            System.out.println("Average updates per one original object: " + ((double)total / originalIDUpdates.size()));
        }
        if (newIDs != null)
            System.out.println("Converted IDs seen: " + newIDs.size());
    }

    static class FinalUpdate {
        final int id;
        final int x;
        final int y;

        FinalUpdate(final int id, final int x, final int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        @SuppressWarnings({"SuspiciousNameCombination"})
        private void writeDelete(final PrintWriter out) {
            out.print("delete ");
            out.print(id);
            out.print(' ');
            out.print(x);
            out.print(' ');
            out.print(y);
            out.print(' ');
            out.print(x);
            out.print(' ');
            out.println(y);
        }

        @SuppressWarnings({"SuspiciousNameCombination"})
        private void writeInsert(final PrintWriter out) {
            out.print("insert ");
            out.print(id);
            out.print(' ');
            out.print(x);
            out.print(' ');
            out.print(y);
            out.print(' ');
            out.print(x);
            out.print(' ');
            out.println(y);
        }
    }


    private final Map<Integer, FinalUpdate> pendingDeletes = new HashMap<>();

    private static final int BUF_SIZE = 100 * 1024 * 1024;

    private void writePostprocessedFile(final String outputFileName) throws IOException {

        final BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName)),
                BUF_SIZE);
        final PrintWriter output = new PrintWriter(buf);
        System.out.print("Externally sorting and outputing updates... ");
        final long msBefore = System.currentTimeMillis();
        final List<BufferedReader> tmpReaders = new ArrayList<>(tmpOutputFiles.size());
        final List<OutputUpdate> updatesToMerge = new ArrayList<>(tmpOutputFiles.size());
        for (final File tmpFile : tmpOutputFiles) {
            final BufferedReader r = new BufferedReader(new FileReader(tmpFile)); 
            final OutputUpdate u = OutputUpdate.read(r);
            if (u != null) {
                tmpReaders.add(r);
                updatesToMerge.add(u);
            }
            else
                r.close();
        }
        int outputRows = 0;
        while (!updatesToMerge.isEmpty()) {
            OutputUpdate earliestUpdate = updatesToMerge.get(0);
            int earliestUpdateI = 0;
            int i = 0;
            for (final OutputUpdate u : updatesToMerge) {
                if (u.compareTo(earliestUpdate) < 0) {
                    earliestUpdate = u;
                    earliestUpdateI = i;
                }
                i++;
            }
            final OutputUpdate replacement = OutputUpdate.read(tmpReaders.get(earliestUpdateI));
            if (replacement != null) {
                updatesToMerge.set(earliestUpdateI, replacement);
            }
            else {
                tmpReaders.get(earliestUpdateI).close();
                tmpReaders.remove(earliestUpdateI);
                updatesToMerge.remove(earliestUpdateI);
            }

            seenOutputIDs.add(earliestUpdate.getId());
            final FinalUpdate previousUpdate = pendingDeletes.get(earliestUpdate.getId());
            if (previousUpdate != null) {
                previousUpdate.writeDelete(output);
            }
            final FinalUpdate op = new FinalUpdate(earliestUpdate.getId(), earliestUpdate.x, earliestUpdate.y);
            if ((op.x < 0) || (op.y < 0)) {
                System.err.println("Negative FinalUpdate, outputRows = " + outputRows);
                System.err.println("op.x = " + op.x + ", op.y = " + op.y);
            }
            op.writeInsert(output);
            pendingDeletes.put(earliestUpdate.getId(), op);
            outputRows++;
        }

        final long duration = System.currentTimeMillis() - msBefore;
        System.out.println("in " + duration + "ms");
        System.out.println("Throughput: " + ((double)outputRows / duration * 1000) + " records/s");
        System.out.println("Different objects in output: " + seenOutputIDs.size());
        System.out.println("Skipped nonpositive record: " + numOfSkippedRecords);
        output.close();        
    }
}
