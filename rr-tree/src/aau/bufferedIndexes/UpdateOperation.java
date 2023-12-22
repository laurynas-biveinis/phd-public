/*
     Copyright (C) 2010 Laurynas Biveinis

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
package aau.bufferedIndexes;

/**
 * An either insertion or deletion
 */
public class UpdateOperation {

    /**
     * The flag for differentiating insertions from deletions.
     */
    private OperationType operationType;

    /**
     * Creates a new update operation
     * @param operationType insertion or deletion
     */
    public UpdateOperation(final OperationType operationType) {
        this.operationType = operationType;
    }

    /**
     * Returns the type of this operation.
     * @return operation type
     */
    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(final OperationType operationType) {
        // TODO: XXL design excludes immutable class here, see UpdateTree.Entry.read and write
        this.operationType = operationType;
    }

    /**
     * Checks if this is a deletion
     * @return <code>true</code> if deletion.
     */
    public boolean isDeletion() {
        return operationType.isDeletion();
    }

    /**
     * Checks if this is a deletion
     * @return <code>true</code> if insertion.
     */
    public boolean isInsertion() {
        return operationType.isInsertion();
    }

    /**
     * Checks for equality with other object
     * @param other an object to compare with
     * @return <code>true</code> if they are equal
     */
    public boolean equals(final Object other) {
        return (other instanceof UpdateOperation) && ((UpdateOperation)other).operationType == operationType;
    }

    /**
     * Returns hash code for this object
     * @return the hash code
     */
    public int hashCode () {
        return operationType.ordinal(); 
    }

    /**
     * Returns string representation of update operation
     * @return the string with update operation summary
     */
    public String toString () {
        return "Update operation " + operationType;
    }
}
