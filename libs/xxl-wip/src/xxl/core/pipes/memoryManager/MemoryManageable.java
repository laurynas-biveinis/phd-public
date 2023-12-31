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
 * Models an object (e.g. an operator or a collection) that is able
 * to manage limitations of its main memory usage.
 * 
 * A memory manageable object registers itself with a memory manager
 * by calling the <code>register</code>-Method of the memory manager,
 * and the memory manager (respectively its strategy) assigns
 * an amount of memory to this object by calling the
 * <code>assignMemSize</code>-Method of this object.
 * 
 * @since 1.1
 */
public interface MemoryManageable extends MemoryMonitorable{

	public static final int MAXIMUM = Integer.MAX_VALUE;

	/**
	 * Returns the amount of memory which is needed by this object
	 * for an acceptable performance.
	 * This method can be called from the strategy of the memory manager
	 * to obtain useful information for distributing main memory among the
	 * memory using objects.
	 * 
	 * @return Returns the preferred amount of memory (in bytes).
 	 */
	public abstract int getPreferredMemSize();
	
	/**
	 * Returns the amount of memory, which is actually assigned to this
	 * object by the memory manager.
	 * 
	 * @return Returns the assigned amount of memory (in bytes).
 	 */
	public abstract int getAssignedMemSize();
	
	/**
	 * Assigns a special amount of memory to this object.
	 * 
	 * @param newMemSize The amount of memory to be assigned to this object
	 *                   (in bytes).
 	 */
	public abstract void assignMemSize(int newMemSize);
	
	/**
	 * Returns the estimated size of the objects which are stored
	 * in the memory of this memory monitorable object.
	 * This method can be called from the strategy of the memory manager
	 * to obtain useful information for distributing main memory among the
	 * memory using objects.
	 * 
	 * @return Returns the size of a single object (in bytes).
	 */
	public abstract int getObjectSize();
	
}
