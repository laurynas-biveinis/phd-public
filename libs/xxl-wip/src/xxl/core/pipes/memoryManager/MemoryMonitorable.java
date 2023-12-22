/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.pipes.memoryManager;

/**
 * Models an Object (e.g. an operator or a collection) whose memory usage can
 * be monitored. 
 */
public interface MemoryMonitorable {

	/**
	 * A constant signing that the memory usage for an object is unknown.
	 */
	public static final int SIZE_UNKNOWN = -1;
	
	/**
	 * A constant that should be used as key to store the objectsize in the 
	 * composite meta data if the object provides metadata.
	 * @see xxl.core.util.metaData.MetaDataProvider MetaDataProvider-interface
	 */
	public static final String OBJECT_SIZE = "OBJECT_SIZE";

	/**
	 * Returns the amount of memory, which is currently used by this object.
	 * This method can be called from the strategy of the memory manager to
	 * obtain useful information for distributing main memory among the
	 * memory using objects.
	 * 
	 * @return Returns the amount of memory currently used by this object
	 *         (in bytes).
 	 */
	public abstract int getCurrentMemUsage();
	
}
