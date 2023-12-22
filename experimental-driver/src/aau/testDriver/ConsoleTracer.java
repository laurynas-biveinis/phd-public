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
package aau.testDriver;

import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.objectTracers.ObjectTracer;
import aau.workload.DataID;
import xxl.core.spatial.KPE;

import java.util.EnumSet;

/**
 * An object tracer that prints to console
 */
public final class ConsoleTracer implements ObjectTracer<KPE> {

    private final EnumSet<TraceClass> traceMask;

    private int tracedObjectId = -1;

    public ConsoleTracer(final EnumSet<TraceClass> traceMask) {
        this.traceMask = traceMask;
    }

    /**
     * Adds a new object id to the set of ids being traced.
     *
     * @param objectIdToTrace object id to trace
     */
    @Override
    final public void registerObject(final int objectIdToTrace) {
        if (tracedObjectId != -1)
            throw new IllegalStateException("ConsoleTracer.registerObject called second time!");
        tracedObjectId = objectIdToTrace;
    }

    private boolean shouldTrace(final KPE object, final Operation op)  {
        return traceMask.contains(operationToClassMap.get(op)) && tracedObjectId == ((DataID)object.getID()).getID();
    }

    /**
     * Trace an object. Checks if the object id is registered to be traced, then performs a tracing based on the
     * context.
     *
     * @param object the object for possible tracing
     * @param op     the operation (context) of the trace
     */
    @Override
    final public void traceObject(KPE object, Operation op) {
        if (shouldTrace(object, op)) {
            System.err.println("Tracer: operation = " + op.name() + ", object = " + object.toString());
        }
    }

    /**
     * Trace an operation. Checks if the object id is registered to be traced, then performs a tracing based on the
     * context.
     *
     * @param entry     the object for possible tracing
     * @param op        the operation (context) of the trace
     * @param extraInfo the info, if any, for the operation
     */
    @Override
    final public void traceUpdateTreeEntry(UpdateTree.Entry<KPE> entry, Operation op, Object extraInfo) {
        if (shouldTrace(entry.getData(), op)) {
            System.err.println("Tracer: operation = " + op.name() + ", entry = " + entry.toString()
                    + ", extra info = " + ((extraInfo == null) ? "null" : extraInfo.toString()));
        }
    }
}
