/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.converters;

/**
 * This is a converter which can convert objects with a known maximal size.
 * That is in many cases very useful (for example
 * {@link xxl.core.collections.containers.io.BlockFileContainer}). For example
 * the class {@link xxl.core.indexStructures.BPlusTree} uses normaly a 
 * {@link xxl.core.collections.containers.io.BlockFileContainer} to store its
 * nodes. I/O operations are executed by composite converters. To determine the
 * node size it is necessary to know the maximal size of the data objects. For
 * this purpose a measured converter is practical.
 * 
 * @param <T> the type to be converted.
 */
public abstract class MeasuredConverter<T> extends Converter<T> {
	
	/**
	 * Determines the maximal size of the objects for which this converter is
	 * used. In the case of an integer converter the result will be 4 bytes.
	 *  
	 * @return the maximal size of the objects for which this converter is
	 *         used.
	 */
	public abstract int getMaxObjectSize();
}
