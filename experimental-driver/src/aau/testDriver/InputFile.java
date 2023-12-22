/*
     Copyright (C) 2007, 2008, 2009, 2011 Laurynas Biveinis

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

import aau.workload.WorkloadOperation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads the file and returns it line by line
 */
class InputFile {
    
    final private String name;
    
    private String line = null;

    private int lineNumber = 0;
    
    private final BufferedReader input;

    private static final int BUFFER_SIZE = 1024 * 1024;

    InputFile(final String name) throws IOException {
        this.name = name;
        input = new BufferedReader(new FileReader(name), BUFFER_SIZE);
    }

    boolean hasNextOperation() throws IOException {
        line = input.readLine();
        return line != null;
    }

    int getLineNumber() {
        return lineNumber;
    }
    
    String getName() {
        return name;
    }
    
    WorkloadOperation getNextOperation() {
        lineNumber++;
        return new WorkloadOperation(line);
    }

    void close() throws IOException {
        input.close();
    }

    protected void finalize() throws Throwable {
        try {
            close();
        }
        catch (IOException ignored) { }
        finally {
            super.finalize();
        }
    }
}
