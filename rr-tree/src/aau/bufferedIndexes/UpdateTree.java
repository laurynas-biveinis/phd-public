/*
     Copyright (C) 2007, 2008, 2010, 2012 Laurynas Biveinis

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

import xxl.core.cursors.Cursor;
import xxl.core.cursors.filters.Filter;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.wrappers.IteratorCursor;
import xxl.core.functions.Function;
import xxl.core.indexStructures.Descriptor;
import xxl.core.indexStructures.RTree;
import xxl.core.indexStructures.Tree;
import xxl.core.io.Convertable;
import xxl.core.predicates.DecoratorPredicate;
import xxl.core.predicates.Equal;
import xxl.core.predicates.Predicate;
import xxl.core.spatial.KPE;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A variant of RTree supporting lazy insertions and deletions.  Instead of data entries, this tree stores lazy
 * insertion and deletion operations.  They are represented by augmenting traditional entries with additional
 * deletion/insertion flag.
 *
 * @see xxl.core.indexStructures.RTree
 */
public class UpdateTree<E extends Convertable> extends RTree {

    public final static int KPE_OPERATION_SIZE = new Entry<KPE>(null, OperationType.INSERTION).logicalSize();

    /**
     * Current size of the buffer in the terms of number of objects.
     */
    private int currSize = 0;

    /**
     * Current number of insertions annihilating deletions
     */
    private int annihilationsID = 0;

    /**
     * Current number of deletions annihilating insertions
     */
    private int annihilationsDI = 0;

    private Function<E, Descriptor> dataDescriptorGetter = null;

    /* A comparator that disregards entry type */
    private static final Predicate<Object> UNWRAPPING_COMPARATOR =
        new DecoratorPredicate<Object>(Equal.DEFAULT_INSTANCE) {
            public boolean invoke(final Object argument0, final Object argument1) {
                final Object unwrapped0 = ((Entry<?>)argument0).getData();
                final Object unwrapped1 = ((Entry<?>)argument1).getData();
                return super.invoke(unwrapped0, unwrapped1);
            }
        };

    /**
     * Entry class is a lazy tree leaf entry node.
     */
    public static final class Entry<E extends Convertable> extends UpdateOperation implements Convertable, HasLogicalSize {

        /**
         * The entry data proper.
         */
        private final E data;

        /**
         * Creates new insertion or deletion entry with the specified data object.
         * @param data the data to be inserted.
         * @param operationType operation type: insertion or deletion
         */
        public Entry(final E data, final OperationType operationType) {
            super(operationType);
            this.data = data;
        }

        // TODO javadoc & unit test
        public Entry<E> makeOpposite() {
            return new Entry<>(getData(), getOperationType().opposite());
        }

        /**
         * Returns the data of this tree node entry.
         * @return the data wrapped by this entry.
         */
        public E getData() {
            return data;
        }

        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Entry))
                return false;
            //noinspection unchecked 
            final Entry<E> other = (Entry<E>)o;
            return super.equals (other) && data.equals(other.data);
        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + super.hashCode();
            result = 37 * result + data.hashCode();
            return result;
        }

        public String toString() {
            return "UpdateTree.Entry " + super.toString() + '\n'
                    + " Data = " + data.toString();
        }

        static public <E extends Convertable> Entry<E> copyEntry(final Entry<E> other) {
            return new Entry<>(other.getData(), other.getOperationType());
        }

        public void read(DataInput input) throws IOException {
            byte opFlag = input.readByte();
            if (opFlag == 0)
                setOperationType(OperationType.INSERTION);
            else if (opFlag == 1)
                setOperationType(OperationType.DELETION);
            else
                throw new IllegalStateException("Unknown operation type in the input!");
            data.read(input);
        }

        public void write(DataOutput output) throws IOException {
            if (getOperationType() == OperationType.INSERTION)
                output.writeByte(0);
            else
                output.writeByte(1);
            data.write(output);
        }

        /**
         * Returns the logical size of this entry.  It is equal to size of data plus one byte.
         *
         * @return the logical size of this entry 
         */
        private int logicalSize() {
            return 39; // TODO: correct only for KPE!
        }
    }

    /**
     * Returns a function that given a lazy tree entry node returns the data proper wrapped in it.
     *
     * @return the unwrapper function
     */
    public static <T extends Convertable> Function<Object, T> getObjectUnwrapper() {
        // A large underground den of sleazy casts
        return new Function<Object, T>() {
            public T invoke(final Object e) {
                if (e instanceof Entry) {
                    //noinspection unchecked
                    return ((Entry<T>)e).getData();
                }
                if (e instanceof Convertable) {
                    //noinspection unchecked,CastConflictsWithInstanceof
                    return (T)e;
                }
                throw new IllegalArgumentException("Unknown type to unwrap!");
            }
        };
    }

    /**
     * This is the basic method to create a new tree.  This method is overrided to support lazy tree
     * node entry type.
     *
     * @param rootEntry          the new {@link xxl.core.indexStructures.Tree#rootEntry}
     * @param rootDescriptor     the new {@link xxl.core.indexStructures.Tree#rootDescriptor}
     * @param getDescriptor      the new {@link xxl.core.indexStructures.Tree#getDescriptor}
     * @param getContainer       the new {@link xxl.core.indexStructures.Tree#getContainer}
     * @param determineContainer the new {@link xxl.core.indexStructures.Tree#determineContainer}
     * @param underflows         the new {@link xxl.core.indexStructures.Tree#underflows}
     * @param overflows          the new {@link xxl.core.indexStructures.Tree#overflows}
     * @param getSplitMinRatio   the new {@link xxl.core.indexStructures.Tree#getSplitMinRatio}
     * @param getSplitMaxRatio   the new {@link xxl.core.indexStructures.Tree#getSplitMaxRatio}
     * @return the initialized tree (i.e. return this)
     */
    @SuppressWarnings({"RawUseOfParameterizedType"})
    public Tree initialize(final Tree.IndexEntry rootEntry, final Descriptor rootDescriptor, final Function getDescriptor,
                           final Function getContainer, final Function determineContainer, final Predicate underflows,
                           final Predicate overflows, final Function getSplitMinRatio, final Function getSplitMaxRatio) {
        if (getDescriptor == null)
            throw new IllegalArgumentException("Parameter getDescriptor cannot be null");
        //noinspection unchecked
        dataDescriptorGetter = getDescriptor;
        // TODO: generics on the next line are beyond me
        //noinspection unchecked
        return super.initialize(rootEntry, rootDescriptor,
                dataDescriptorGetter.composeUnary((Function)getObjectUnwrapper()), getContainer, determineContainer,
                underflows, overflows, getSplitMinRatio, getSplitMaxRatio);
    }

    /**
     * Returns the current size of the tree.
     * @return current number of the entries in the tree.
     */
    public int getCurrentSize() {
        return currSize;
    }

    public int getNumOfIDAnnihilations() {
        return annihilationsID;
    }

    public int getNumOfDIAnnihilations() {
        return annihilationsDI;
    }

    /**
     * Adds a new data entry to the tree.  It works by making and inserting a new insertion entry into the tree
     * with the data.
     *
     * @param data the object that is to be inserted
     * @see xxl.core.indexStructures.Tree#insert(Object,xxl.core.indexStructures.Descriptor,int)
     */
    public void insertWithAnnihilation(final E data) {
        final Entry<E> earlierEntry = removeAnyEntry(data);
        if (earlierEntry == null) {
            // If we didn't find and delete matching deletion, insert the insertion into the tree
            insertEntry(new Entry<>(data, OperationType.INSERTION));
        }
        else {
            assert earlierEntry.isDeletion();
            annihilationsID++;
        }
    }

    // TODO: javadoc, test
    public void insertEntry(final Entry<E> entry) {
        super.insert(entry);
        currSize++;
    }

    // TODO: maybe remove?
    public Cursor<E> query(final Descriptor queryDescriptor) {
        return new Mapper<>(
                new Function<Entry<E>, E>() {
                    public E invoke(final Entry<E> argument) {
                        return argument.getData();
                    }
                }, queryInsertions(queryDescriptor));
    }

    // TODO: javadoc, test
    Cursor<Entry<E>> queryInsertions(final Descriptor queryDescriptor) {
        //noinspection unchecked
        final Cursor<Entry<E>> descriptorResults = (queryDescriptor != null) ?
                super.query(queryDescriptor) : new EmptyCursor<Entry<E>>();

        return new Filter<>(descriptorResults,
            new Predicate<Entry<E>>() {
                public boolean invoke (final Entry<E> o) {
                    return o.isInsertion();
                }
            });
    }

    // TODO: javadoc, test
    public Cursor<Entry<E>> copyQueryInsertions(final Descriptor queryDescriptor) {
        final Cursor<Entry<E>> resultCursor = queryInsertions(queryDescriptor);
        return copyQueryResults(resultCursor);
    }

    // TODO: javadoc, test
    private Cursor<Entry<E>> copyQueryResults(final Cursor<Entry<E>> resultCursor) {
        final Collection<Entry<E>> results = new ArrayList<>();
        while (resultCursor.hasNext()) {
            results.add(resultCursor.next());
        }
        resultCursor.close();
        return new IteratorCursor<>(results.iterator());
    }

    // TODO: javadoc, test
    public Cursor<Entry<E>> copyQueryAllOps(final Descriptor queryDescriptor) {
        final Cursor<Entry<E>> resultCursor = queryEntryOfAnyType(queryDescriptor);
        return copyQueryResults(resultCursor);
    }
    
    /**
     * This method is an implementation of an efficient querying algorithm.
     * The result is a lazy cursor pointing to all leaf entries whose descriptors
     * overlap with the given <tt>queryDescriptor</tt>.
     *
     * @param queryDescriptor describes the query in terms of a descriptor
     * @return a lazy <tt>Cursor</tt> pointing to all response objects
     * @see xxl.core.indexStructures.Tree#query(xxl.core.indexStructures.Descriptor, int)
     */
    public Cursor<Entry<E>> queryEntryOfAnyType(final Descriptor queryDescriptor) {
        //noinspection unchecked
        return super.query(queryDescriptor);
    }

    public Cursor<Entry<E>> queryEntry(final Entry<E> entry) {
        //noinspection unchecked
        final Cursor<Entry<E>> descriptorResults = (entry != null)
                ? super.query(dataDescriptorGetter.invoke(entry.getData())) : new EmptyCursor<Entry<E>>();
        return new Filter<>(descriptorResults,
                new Predicate<Entry<E>>() {
                    public boolean invoke(final Entry<E> o) {
                        return entry != null && entry.equals(o);
                    }
                });
    }

    /**
     * Removes an insertion entry from the buffer. If there is no insertion entry, inserts a deletion entry.
     *
     * @param data   object to remove
     * @return the removed object or <tt>null</tt> if no object was removed
     */
    public Object removeWithAnnihilation(final E data) {
        final Entry<E> earlierEntry = removeAnyEntry(data);
        if (earlierEntry == null) {
            // If we didn't find and delete matching insertion, insert the deletion into the tree
            insertEntry(new Entry<>(data, OperationType.DELETION));
            return null;
        }
        else {
            annihilationsDI++;
            assert earlierEntry.isInsertion();
            return earlierEntry.getData();
        }
    }

    private Entry<E> removeAnyEntry(final E data) {
        final Object wrappedEntry = new Entry<>(data, OperationType.INSERTION);
        // Remove any entry with this data, disregarding the entry type.
        //noinspection unchecked
        final Entry<E> result = (Entry<E>)super.remove(wrappedEntry, UNWRAPPING_COMPARATOR);
        if (result != null)
            currSize--;
        return result;
    }

    public Entry<E> removeExactEntry(final Entry<E> object) {
        //noinspection unchecked
        final Entry<E> result = (Entry<E>)super.remove(object, Equal.DEFAULT_INSTANCE);
        if (result != null)
            currSize--;
        return result;
    }

    /**
     * Removes all data in the tree.
     */
    public void clear() {
        if (currSize == 0)
            return;
        super.clear();
        currSize = 0;
    }

    /**
     * Adds an entry if it does not already exist
     * @param op the operation
     *
     * @return <code>true</code> if the operation was not already present in the buffer, <code>false</code> otherwise
     *
     */
    public boolean addEntryIfNotExists(final Entry<E> op) {
        // TODO: should be possible to fuse these two into a single operation
        // TODO: test return code
        if (!queryEntry(op).hasNext()) {
            insertEntry(op);
            return true;
        }
        return false;
    }

    /**
     * Removes all deletions from the tree, leaving insertions.
     */
    public void removeAllDeletions() {
        // TODO: is it better to remove deletions directly?
        //noinspection unchecked
        final Cursor<Entry<E>> allEntries = query();
        final Collection<Entry<E>> insertions = new ArrayList<>();
        while (allEntries.hasNext()) {
            final Entry<E> op = allEntries.next();
            if (op.isInsertion())
                insertions.add(op);
        }
        clear();
        for (final Entry<E> insertion : insertions) {
            insertEntry(insertion);
        }
    }
}
