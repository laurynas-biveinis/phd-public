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

import java.util.HashMap;
import java.util.Map;

/**
 * An interface for tracing data objects during various RR-tree operations.
 */
public interface ObjectTracer<E extends Convertable> {

    public static enum TraceClass {
        RR_TREE,
        PIGGYBACKING,
        ENTRY_COLLECTION,
        QUERY_PROCESSING,
        GC
    }

    public static enum Operation {

        /* RR_TREE */

        /**
         * Object is inserted to the RR-tree buffer
         */
        INSERT_TO_BUFFER,

        /**
         * Object is removed from the RR-tree buffer (inserting deletion to the buffer)
         */
        REMOVE_FROM_BUFFER,

        /**
         * Object removed from the buffer at the current emptying is returned back to it
         */
        PUT_OP_BACK_TO_BUFFER,

        /**
         * Object is passed to GroupUpdate
         */
        GROUP_UPDATE_START,

        /**
         * Object in GroupUpdate before index node piggybacking
         */
        GROUP_UPDATE_BEFORE_INDEX_PIGGYBACKING,

        /**
         * Object in GroupUpdate after index node piggybacking
         */
        GROUP_UPDATE_AFTER_INDEX_PIGGYBACKING,

        /**
         * Object is at the leaf node in EmptyBuffer
         */
        UPDATE_LEAF_NODE,

        /**
         * The RR-tree buffer operation is performed on the disk and completed
         */
        COMPLETE_OPERATION,

        /* PIGGYBACKING */

        /**
         * Object is being piggybacked at the index node
         */
        INDEX_NODE_PIGGYBACKING,

        /**
         * Operation was selected for leaf node piggybacking
         */
        LEAF_NODE_PIGGYBACKING,

        /* ENTRY_COLLECTION */

        /**
         *  Object is added to UpdateTreeEntryCollection
         */
        ENTRY_COLLECTION_ADD,

        /**
         * Object is added to UpdateTreeEntryCollection, annihilates an existing object
         */
        ENTRY_COLLECTION_ADD_ANNIHILATE,

        /**
         * Object is added to UpdateTreeEntryCollection, increases the collection
         */
        ENTRY_COLLECTION_ADD_INCREASE,

        /* QUERY_PROCESSING */

        /**
         * Object in the data tree query result processing 1st loop
         */
        DATA_TREE_QUERY_1ST_LOOP,

        /**
         * Object in the data tree query result processing buffer loop
         */
        DATA_TREE_QUERY_BUFFER_LOOP,

        /**
         * Object in the data tree query result processing final loop
         */
        DATA_TREE_QUERY_FINAL_LOOP,

        /**
         * Object in the update tree query processing initial set
         */
        UPDATE_TREE_QUERY_INITIAL_RESULT,

        /**
         * Object in the update tree query processing external result set
         */
        UPDATE_TREE_QUERY_EXTERNAL_RESULT,

        /* GC */

        /**
         * Object is being read in the bulk reloader GC, 1st pass
         */
        BULK_RELOADER_GC_1ST_PASS,

        /**
         * Operation is being removed from the bulk reloader GC entry to tmp file mapping. extraInfo is Integer of
         * tmp file id.
         */
        BULK_RELOADER_REMOVING_FROM_ENTRY_TO_TMP_MAP,

        /**
         * Operation is being written to a tmp file in the bulk reloading GC 1st pass
         */
        BULK_RELOADER_1ST_PASS_WRITE,

        /**
         * Operation is being written to a final leaf node in the bulk reloading GC 2nd pass
         */
        BULK_RELOADER_2ND_PASS_WRITE,

        /**
         * Operation is being read from a tmp file in the bulk reloading GC 2nd pass. extraInfo is Integer of tmp file
         * id.
         */
        BULK_RELOADER_2ND_PASS_READ,

        /**
         * Operation after reading from a tmp file in the bulk reloading GC 2nd pass increased the working data set
         */
        BULK_RELOADER_2ND_PASS_READ_INCREASE,

        /**
         * Operation after reading from a tmp file in the bulk reloading GC 2nd pass annihilated with an already
         * existing operation. extraInfo is Integer of tmp file id of the already-existing operation that was
         * annihilated with.
         */
        BULK_RELOADER_2ND_PASS_READ_ANNIHILATION
    }

    final static Map<Operation, TraceClass> operationToClassMap = new HashMap<Operation, TraceClass>() {{
        put(Operation.INSERT_TO_BUFFER,                       TraceClass.RR_TREE);
        put(Operation.REMOVE_FROM_BUFFER,                     TraceClass.RR_TREE);
        put(Operation.PUT_OP_BACK_TO_BUFFER,                  TraceClass.RR_TREE);
        put(Operation.GROUP_UPDATE_START,                     TraceClass.RR_TREE);
        put(Operation.GROUP_UPDATE_BEFORE_INDEX_PIGGYBACKING, TraceClass.RR_TREE);
        put(Operation.GROUP_UPDATE_AFTER_INDEX_PIGGYBACKING,  TraceClass.RR_TREE);
        put(Operation.UPDATE_LEAF_NODE,                       TraceClass.RR_TREE);
        put(Operation.COMPLETE_OPERATION,                     TraceClass.RR_TREE);

        put(Operation.INDEX_NODE_PIGGYBACKING, TraceClass.PIGGYBACKING);
        put(Operation.LEAF_NODE_PIGGYBACKING,  TraceClass.PIGGYBACKING);

        put(Operation.ENTRY_COLLECTION_ADD,            TraceClass.ENTRY_COLLECTION);
        put(Operation.ENTRY_COLLECTION_ADD_ANNIHILATE, TraceClass.ENTRY_COLLECTION);
        put(Operation.ENTRY_COLLECTION_ADD_INCREASE,   TraceClass.ENTRY_COLLECTION);

        put(Operation.DATA_TREE_QUERY_1ST_LOOP,          TraceClass.QUERY_PROCESSING);
        put(Operation.DATA_TREE_QUERY_BUFFER_LOOP,       TraceClass.QUERY_PROCESSING);
        put(Operation.DATA_TREE_QUERY_FINAL_LOOP,        TraceClass.QUERY_PROCESSING);
        put(Operation.UPDATE_TREE_QUERY_INITIAL_RESULT,  TraceClass.QUERY_PROCESSING);
        put(Operation.UPDATE_TREE_QUERY_EXTERNAL_RESULT, TraceClass.QUERY_PROCESSING);

        put(Operation.BULK_RELOADER_GC_1ST_PASS,                    TraceClass.GC);
        put(Operation.BULK_RELOADER_REMOVING_FROM_ENTRY_TO_TMP_MAP, TraceClass.GC);
        put(Operation.BULK_RELOADER_1ST_PASS_WRITE,                 TraceClass.GC);
        put(Operation.BULK_RELOADER_2ND_PASS_WRITE,                 TraceClass.GC);
        put(Operation.BULK_RELOADER_2ND_PASS_READ,                  TraceClass.GC);
        put(Operation.BULK_RELOADER_2ND_PASS_READ_INCREASE,         TraceClass.GC);
        put(Operation.BULK_RELOADER_2ND_PASS_READ_ANNIHILATION,     TraceClass.GC);
    }};

    /**
     * Adds a new object id to the set of ids being traced.
     *
     * @param objectIdToTrace object id to trace
     */
    public void registerObject(final int objectIdToTrace);

    /**
     * Trace an object. Checks if the object id is registered to be traced, then performs a tracing based on the
     * context.
     *
     * @param object the object for possible tracing
     * @param op the operation (context) of the trace
     */
    public void traceObject(final E object, final Operation op);

    /**
     * Trace an operation. Checks if the object id is registered to be traced, then performs a tracing based on the
     * context.
     *
     * @param entry the object for possible tracing
     * @param op the operation (context) of the trace
     * @param extraInfo the info, if any, for the operation
     */
    public void traceUpdateTreeEntry(final UpdateTree.Entry<E> entry, final Operation op, final Object extraInfo);
}
