/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.metaData;

/**
 * An interface that signals that an implementing class provides meta data
 * information.
 *
 * @param <M> the type of the meta data provided by objects implementing this
 *        interface.
 * @see xxl.core.cursors.MetaDataCursor
 */
public interface MetaDataProvider<M> {

	/**
	 * Returns the meta data information for this class.
	 *
	 * @return the meta data information.
	 */
	public abstract M getMetaData();

}
