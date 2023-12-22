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
package aau.workload;

import xxl.core.io.converters.ConvertableConverter;
import xxl.core.io.converters.Converter;
import xxl.core.math.Maths;
import xxl.core.relational.tuples.TupleConverter;
import xxl.core.spatial.KPE;

import java.io.PrintWriter;

/**
 * Represents a single workload operation with all information as found in workload file
 */
public final class WorkloadOperation {

    // First word in a string representation: operation type
    private static final String EMPTY = "empty";
    private static final String QUERY = "query";
    private static final String INSERT = "insert";
    private static final String DELETE = "delete";
    private static final String FINAL_DELETE = "finaldelete";
    private static final String DELETING_INSERT = "deletinginsert";

    /**
     * Possible operation types
     */
    public static enum OperationType { EMPTY, QUERY, INSERT, DELETE, FINAL_DELETE, DELETING_INSERT }

    private final OperationType operationType;

    private static final int UNDEFINED_OBJECT_ID = -1;

    /**
     * If this operation represents an update operation, then object id of the object involved, otherwise undefined.
     */
    private final int objectId;

    /**
     * Spatial extent of this command
     */
    private final SpatialExtent spatialExtent;
    
    /**
     * Creates a new workload operation from a single workload file string
     * @param workloadFileLine workload file string representing this operation
     */
    public WorkloadOperation(final String workloadFileLine) {
        final String[] tokens = workloadFileLine.split("\\s+");
        final String operation = tokens[0];

        double newX1 = -1.0, newY1 = -1.0, newX2 = -1.0, newY2 = -1.0;
        final OperationType newOpType;

        boolean isUpdate = false;

        switch (operation) {
            case EMPTY:
                newOpType = OperationType.EMPTY;
                break;
            case QUERY:
                newOpType = OperationType.QUERY;
                newX1 = Double.parseDouble(tokens[1]);
                newY1 = Double.parseDouble(tokens[2]);
                newX2 = Double.parseDouble(tokens[3]);
                newY2 = Double.parseDouble(tokens[4]);
                break;
            case INSERT:
                newOpType = OperationType.INSERT;
                isUpdate = true;
                break;
            case DELETE:
                newOpType = OperationType.DELETE;
                isUpdate = true;
                break;
            case FINAL_DELETE:
                newOpType = OperationType.FINAL_DELETE;
                isUpdate = true;
                break;
            case DELETING_INSERT:
                newOpType = OperationType.DELETING_INSERT;
                isUpdate = true;
                break;
            default:
                throw new IllegalArgumentException("Invalid operation type");
        }

        int newObjId = UNDEFINED_OBJECT_ID;
        if (isUpdate) {
            newObjId = Integer.parseInt(tokens[1]);
            assert newObjId != UNDEFINED_OBJECT_ID;
            newX1 = Double.parseDouble(tokens[2]);
            newY1 = Double.parseDouble(tokens[3]);
            newX2 = Double.parseDouble(tokens[4]);
            newY2 = Double.parseDouble(tokens[5]);
        }

        operationType = newOpType;
        objectId = newObjId;
        spatialExtent = new SpatialExtent(newX1, newY1, newX2, newY2);
    }

    /**
     * Creates a new workload operation from parameters
     * @param operationType the command type
     * @param objectId the object id if command is an update, otherwise ignored
     * @param spatialExtent extent of an object or a range of a query
     */
    public WorkloadOperation(final OperationType operationType, final int objectId, final SpatialExtent spatialExtent) {
        this.operationType = operationType;

        this.objectId = isUpdate() ? objectId : UNDEFINED_OBJECT_ID;

        this.spatialExtent = operationType == OperationType.EMPTY
                ? new SpatialExtent(-1.0, -1.0, -1.0, -1.0) : spatialExtent;

    }

    public double distance(final WorkloadOperation other) {
        return spatialExtent.center().distance(other.spatialExtent.center());
    }

    /**
     * Checks for equality.
     * @param other the object to compare with
     * @return equality flag
     */
    public boolean equals(final Object other) {
        if (!(other instanceof WorkloadOperation))
            return false;
        final WorkloadOperation wo = (WorkloadOperation)other;
        return (objectId == wo.objectId) && (operationType == wo.operationType)
                && (spatialExtent.equals(wo.spatialExtent));
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + operationType.hashCode();
        result = 31 * result + objectId;
        result = 31 * result + spatialExtent.hashCode();
        return result;
    }

    /**
     * For an update operation, returns the object id.
     *
     * @return the object id of operation
     */
    public int getId() {
        assert isInsert() || isKindOfDelete();
        return objectId;
    }

    /**
     * The converter for I/O of KPE, which is part of WorkloadOperation.
     */
    @SuppressWarnings({"unchecked"})
    private static final TupleConverter kpeTupleConverter
            = new TupleConverter(false, new Converter[]{ConvertableConverter.DEFAULT_INSTANCE,
                                 ConvertableConverter.DEFAULT_INSTANCE});

    /**
     * Returns a suitable converter for the KPE part of WorkloadOperation.
     *
     * @return the suitable converter for KPE
     */
    public static TupleConverter getConverter() {
        return kpeTupleConverter;
    }

    /**
     * For an update operation, returns the data object (of XXL library KPE type) involved
     * @return the data object of operation
     */
    public KPE getObject() {
        assert objectId != UNDEFINED_OBJECT_ID;
        return new KPE(new DataID(objectId), spatialExtent.getDoublePointRectangle(), getConverter());
    }

    /**
     * For a query operation, returns the query rectangle (of XXL library KPE type)
     * @return the query rectangle
     */
    public KPE getQueryRectangle() {
        assert isQuery();
        return new KPE(Maths.ZERO, spatialExtent.getDoublePointRectangle(), getConverter());
    }

    public SpatialExtent getSpatialExtent() {
        return spatialExtent;
    }

    /**
     * Tells if this operation is one of the delete operations ("delete" or "finaldelete")
     * @return delete type of operation flag
     */
    public boolean isKindOfDelete() {
        return (operationType == OperationType.DELETE) || (operationType == OperationType.FINAL_DELETE);
    }

    /**
     * Tells if this operation is an empty buffer operation
     * @return empty buffer operation flag
     */
    public boolean isEmptyBuffer() {
        return operationType == OperationType.EMPTY;
    }

    /**
     * Tells if this operation is an insert operation
     * @return insert operation flag
     */
    public boolean isInsert() {
        return operationType == OperationType.INSERT;
    }

    /**
     * Tells if this operation is a query operation
     * @return query operation flag
     */
    public boolean isQuery() {
        return operationType == OperationType.QUERY;
    }

    public boolean isDeletingInsert() {
        return operationType == OperationType.DELETING_INSERT;
    }

    public boolean isUpdate() {
        return isKindOfDelete() || isInsert() || isDeletingInsert();
    }

    /**
     * Prints the operation string in a workload format
     * @param output a <code>PrintWriter</code> to print the string on
     */
    public void write(final PrintWriter output) {
        switch (operationType) {
            case INSERT: output.print("insert\t"); break;
            case QUERY:  output.print("query \t"); break;
            case DELETE: output.print("delete\t"); break;
            case EMPTY:  output.print("empty"); break;
            case FINAL_DELETE: output.print("finaldelete\t"); break;
            case DELETING_INSERT: output.print("deletinginsert\t"); break;
        }
        if (isUpdate()) {
            output.print(objectId);
            output.print(" \t");
        }
        if (!isEmptyBuffer()) {
            spatialExtent.print(output);
        }
    }
}
