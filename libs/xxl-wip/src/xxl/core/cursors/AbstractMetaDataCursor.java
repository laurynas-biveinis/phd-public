/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.cursors;

import java.util.Iterator;

/**
 * This class decorates an object implementing the interface
 * {@link xxl.core.cursors.Cursor} such that it is able to provide metadata
 * information. For this reason this class extends the class
 * {@link xxl.core.cursors.SecureDecoratorCursor} by additionally implementing
 * the interface {@link xxl.core.util.metaData.MetaDataProvider}. For getting a concrete
 * instance of this abstract class the method
 * {@link #getMetaData() getMetaData} has to be overwritten such that it
 * returns the metadata information for the decorated cursor.
 * 
 * <p><b>Note:</b> When the given input iteration only implements the interface
 * {@link java.util.Iterator Iterator} it is wrapped to a cursor by a call to
 * the static method {@link xxl.core.cursors.Cursors#wrap(Iterator) wrap}.</p>
 * 
 * @param <E> the type of the elements returned by this cursor.
 * @param <M> the type of the given meta data object.
 * @see java.util.Iterator
 * @see xxl.core.cursors.Cursor
 * @see xxl.core.cursors.SecureDecoratorCursor
 */
public abstract class AbstractMetaDataCursor<E, M> extends SecureDecoratorCursor<E> implements MetaDataCursor<E, M> {

	/**
	 * Creates a new abstract metadata-cursor. The given iteration is wrapped
	 * to a cursor by calling the method
	 * {@link xxl.core.cursors.Cursors#wrap(java.util.Iterator)} and decorated
	 * such that solely the abstract method {@link #getMetaData() getMetaData}
	 * has to be overwritten for getting a cursor that is able to provide its
	 * meta data information.
	 *
	 * @param iterator the iterator to be decorated.
	 */
	public AbstractMetaDataCursor(Iterator<E> iterator) {
		super(iterator);
	}

	/**
	 * Returns the metadata information for this cursor. This method has to be
	 * overwritten such that it returns the concrete meta data information for
	 * the decorated cursor.
	 *
	 * @return the metadata information for this cursor.
	 */
	public abstract M getMetaData();

}
