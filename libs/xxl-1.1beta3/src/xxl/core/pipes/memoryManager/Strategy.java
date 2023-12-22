/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
						Head of the Database Research Group
						Department of Mathematics and Computer Science
						University of Marburg
						Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.pipes.memoryManager;

import java.util.Iterator;

/**
 * Interface for strategies for distribution of main memory.
 * A strategy must be able to manage the registration and deregistration
 * of memory-using objects and it must ensure that the total memory usage
 * never exceeds the total amount of available memory.
 * The strategy is also responsible for the distribution of memory
 * among the memory-using objects.
 * 
 * @since 1.1
 */
public interface Strategy {
	
	/**
	 * Registers a memory-using object. The object has to call requestformemory()
	 * to get memory.
	 * 
	 * @param memUser the memory-using object to register.
	 */
	public abstract void register(MemoryMonitorable memUser);
	
	/**
	 * Deregisters a memory-using object.
	 * 
	 * @param memUser the memory-using object to deregister.
	 */
	public abstract void deregister(MemoryMonitorable memUser);
	
	/**
	 * Returns an iterator of the registered memory-using objects.
	 * They implement the memorymonitorable or memorymanageable interface. 
	 * 
	 * @return Returns an iterator of the memory-using objects.
	 */
	public abstract Iterator<MemoryMonitorable> getMemUsers();
	
	/**
	 * Returns an iterator of the registered memory-using objects.
	 * They implement the memorymanageable interface. 
	 * 
	 * @return Returns an iterator of the memory-using objects.
	 */
	public abstract Iterator<MemoryManageable> getManageableMemUsers();

	
	/**
	 * Returns the number of actually registered manageable memory-using objects.
	 * They implement the memorymanageable interface.
	 * 
	 * @return Returns the number of actually registered memory-using objects.
	 */
	public abstract int getNoOfManagedMemUsers();

	/**
	 * Returns the number of actually registered monitorable memory-using objects.
	 * They implement the memorymonitorable or memorymanageable interface.
	 * 
	 * @return Returns the number of actually registered memory-using objects.
	 */
	public abstract int getNoOfMonitorableMemUsers();

	/**
	 * Sets a new global amount of main memory.
	 * 
	 * @param newGlobalMemSize The new global amount of main memory (in bytes).
	 */
	public abstract void setGlobalMemSize(int newGlobalMemSize);

	/**
	 * Returns the global amount of main memory.
	 * 
	 * @return Returns the global amount of main memory (in bytes).
	 */
	public abstract int getGlobalMemSize();

	/**
	 * Returns the total amount of memory which is actually assigned to the
	 * registered memory-using objects.
	 * 
	 * @return Returns the total amount of assigned memory (in bytes).
	 */
	public abstract int getCurrentMemUsage();

	/**
	 * Receives a request for an amount of memory from a memory-using object.
	 * 
	 * @param memUser the requesting memory-using object.
	 * @param newMemSize the desired amount of memory (in bytes).
	 */
	public abstract void requestForMemory(MemoryMonitorable memUser, int newMemSize);
	

	/**
	 * Assigns a special amount of memory to the specified memory-using object.
	 * 
	 * @param memUser the memory-using object as the recipient of the assignment.
	 * @param newMemSize The amount of memory to be assigned (in bytes).
 	 */
	public abstract void assignMemSize(MemoryManageable memUser, int newMemSize);

	/**
	 * Detects whether a memory overflow has occured or not.
	 * 
	 * @return Returns <code>true</code> if an overflow has occured.
 	 */
	public abstract boolean detectOverflow();

	/**
	 * This method handles a memory overflow (i.e. it has to solve it).
	 * The distribution of main memory among the memory-using objects
	 * is mainly coded in this method.
	 */
	public abstract void handleOverflow();

}