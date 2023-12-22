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
package aau.bufferedIndexes.objectTracers;

import aau.bufferedIndexes.UpdateTree;
import xxl.core.io.Convertable;

/**
 * An object tracer that silently eats all trace requests.
 * @param <E> the object data type
 */
public class NullObjectTracer<E extends Convertable> implements ObjectTracer<E> {
    /**
     * Adds a new object id to the set of ids being traced. Does nothing for the null tracer.
     *
     * @param objectIdToTrace object id to trace
     */
    @Override
    public void registerObject(final int objectIdToTrace) { }

    /**
     * Trace an object. Does nothing for the null tracer.
     *
     * @param object the object for possible tracing
     * @param op the operation (context) of the trace
     */
    @Override
    public void traceObject(final E object, final Operation op) { }

    /**
     * Trace an operation. Does nothing for the null tracer.
     *
     * @param entry the object for possible tracing
     * @param op the operation (context) of the trace
     * @param extraInfo the info, if any, for the operation
     */
    @Override
    public void traceUpdateTreeEntry(final UpdateTree.Entry<E> entry, final Operation op, final Object extraInfo) { }
}
