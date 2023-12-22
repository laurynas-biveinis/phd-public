/*
     Copyright (C) 2007, 2009, 2010 Laurynas Biveinis

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

import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.Descriptor;
import xxl.core.io.Convertable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Maintains a copy of data in the tree and performs verification.
 */
class TreeVerifier<E extends Convertable> {

    /**
     * A checked exception to signal tree data verification failure.
     */
    static class FailedVerificationException extends Exception {
        /**
         * Constructs a new FailedVerificationException object.
         * @param message message about all data inequalities found.
         */
        FailedVerificationException (final String message) {
            super(message);
        }
    }

    /**
     * The verifier data.
     */
    private final Set<E> objectsInTheTree;

    /**
     * Tree being verified.
     */
    private final TreeDriver<E> tree;

    /**
     * Creates a new tree verificator.
     * @param theTree the tree to verify.
     * @param doVerification <code>true</code>, if the verification should be actually performed, <code>false</<code>,
     * if verification requests should be ignored.
     */
    TreeVerifier(final TreeDriver<E> theTree, final boolean doVerification) {
        if (doVerification) {
            objectsInTheTree = new HashSet<E>();
            tree = theTree;
        }
        else {
            objectsInTheTree = null;
            tree = null;
        }
    }

    /**
     * Inserts an object into the tree verificator.  Should be called at the same time as insertion to the real tree.
     * @param datum an object to insert.
     */
    void insert(final E datum) {
        if (objectsInTheTree == null)
            return;
        final boolean result = objectsInTheTree.add(datum);
        if (!result)
            throw new IllegalStateException("Attempt to insert the same object twice");
    }

    /**
     * Removes an object from the tree verificator.  Should be called at the same time as deletion from the real tree.
     * @param datum an object to remove.
     */
    void remove(final E datum) {
        if (objectsInTheTree == null)
            return;
        objectsInTheTree.remove(datum);
    }

    /**
     * Performs the tree verification.
     * @throws FailedVerificationException an tree verification exception.
     */
    void verify() throws FailedVerificationException {
        if (objectsInTheTree == null)
            return;
        final boolean piggybackingState = tree.getPiggybackingState();
        tree.setPiggybackingState(false);

        final StringBuilder errors = new StringBuilder();
        final Cursor<E> wholeTree = tree.query(tree.getGlobalDescriptor());
        final Set<E> treeData = new HashSet<E>();
        boolean problems = false;

        while (wholeTree.hasNext()) {
            final E inTree = wholeTree.next();
            final boolean result = treeData.add(inTree);
            if (!result) {
                errors.append("Duplicate data returned by the tree cursor:\n");
                errors.append("Datum: ");
                errors.append(inTree.toString());
                errors.append('\n');
                problems = true;
            }
        }

        problems |= setsDiffer(errors, treeData, objectsInTheTree);
        tree.setPiggybackingState(piggybackingState);
        if (problems)
            throw new FailedVerificationException(errors.toString());
    }

    static private <E> boolean setsDiffer(StringBuilder errors, Set<E> treeData, Set<E> verifierData) {
        boolean problems = false;
        if (!verifierData.equals(treeData)) {
            errors.append("Data comparison failed\n");
            errors.append("Verifier size = ");
            errors.append(verifierData.size());
            errors.append(", tree returned objects = ");
            errors.append(treeData.size());
            errors.append('\n');
            final Set<E> treeDataCopy = new HashSet<E>(treeData);
            treeDataCopy.removeAll(verifierData);
            Iterator<E> d = treeDataCopy.iterator();
            if (d.hasNext()) {
                errors.append("Data in treeData but not verifier:\n");
                while (d.hasNext()) {
                    errors.append(d.next().toString());
                    errors.append('\n');
                }
            }
            verifierData.removeAll(treeData);
            d = verifierData.iterator();
            if (d.hasNext()) {
                errors.append("Data in verifier but not treeData:\n");
                while (d.hasNext()) {
                    errors.append(d.next().toString());
                    errors.append('\n');
                }
            }
            problems = true;
        }
        return problems;
    }

    void verifyQuery(final Descriptor queryRectangle, final Set<E> treeResults)
            throws FailedVerificationException {
        if (objectsInTheTree == null)
            return;
        final Set<E> verifierResults = new HashSet<E>();
        for (final E candidate : objectsInTheTree) {
            if (queryRectangle.overlaps(tree.getDescriptor(candidate))) {
                boolean result = verifierResults.add(candidate);
                assert result;
            }
        }
        final StringBuilder errors = new StringBuilder();
        if (setsDiffer(errors, treeResults, verifierResults))
            throw new FailedVerificationException(errors.toString());
    }
}
