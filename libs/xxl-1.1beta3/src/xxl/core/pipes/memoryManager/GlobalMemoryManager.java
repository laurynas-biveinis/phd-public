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
 * Main class for distributing main memory among the memory-using objects.
 * All memory-using objects have to be memory manageable, and they call
 * the <code>register</code>-method of this GlobalMemoryManager.
 * 
 * @since 1.1
 */
public class GlobalMemoryManager {
		
	/**
	 * The strategy how to distribute memory among the memory-using objects.
	 */
	protected Strategy strategy;
	
	/**
	 * Constructs a GlobalMemoryManager with a specified strategy
	 * for memory distribution.
	 * 
	 * @param strategy The strategy for memory distribution.
	 */
	public GlobalMemoryManager (Strategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * Constructs a GlobalMemoryManager with a uniform memory 
	 * distribution strategy and a specified amount of global
	 * main memory.
	 * 
	 * @param globalMemSize The global amount of main memory (in bytes).
	 */
	public GlobalMemoryManager (int globalMemSize) {
		this(new UniformStrategy(globalMemSize));
	}


	/**
	 * Sets a new global amount of main memory.
	 * 
	 * @param newGlobalMemSize The new global amount of main memory (in bytes).
	 */
	public void setGlobalMemSize (int newGlobalMemSize) {
	    if (newGlobalMemSize < 0)
			throw new IllegalArgumentException();
		strategy.setGlobalMemSize(newGlobalMemSize);
	}

	/**
	 * Returns the global amount of main memory.
	 * 
	 * @return Returns the global amount of main memory (in bytes).
	 */
	public int getGlobalMemSize () {
		return strategy.getGlobalMemSize();
	}

	/**
	 * Returns the total amount of memory which is assigned to the
	 * registered memory-using objects.
	 * 
	 * @return Returns the total amount of assigned memory (in bytes).
	 */
	public int getCurrentMemUsage () {
		return strategy.getCurrentMemUsage();
	}

	/**
	 * Sets a new strategy for memory distribution.
	 * This method also deregisters all memory-using objects at the old
	 * strategy and registers them at the new strategy.
	 * 
	 * @param newStrategy The new strategy for memory distribution.
	 */
	public void setStrategy(Strategy newStrategy) {
		int globalMemSize = this.strategy.getGlobalMemSize();
		newStrategy.setGlobalMemSize(globalMemSize);
		Iterator<MemoryMonitorable> memUsers = this.strategy.getMemUsers();
		while (memUsers.hasNext()) {
			MemoryMonitorable memUser = memUsers.next();
			newStrategy.register(memUser);
			this.strategy.deregister(memUser);
		}
		this.strategy = newStrategy;
	}

	/**
	 * Returns the strategy object which is actually used for
	 * memory distribution.
	 * 
	 * @return Returns the strategy for memory distribution.
	 */
	public Strategy getStrategy() {
		return strategy;
	}


	/**
	 * Registers a memory-using object.
	 * 
	 * @param memUser the memory-using object to register.
	 */
	public void register(MemoryMonitorable memUser) {
		strategy.register(memUser);
	}
	
	/**
	 * Deregisters a memory-using object.
	 * 
	 * @param memUser the memory-using object to deregister.
	 */
	public void deregister(MemoryMonitorable memUser) {
		strategy.deregister(memUser);
	}
	
	/**
	 * Returns an iterator of the registered memory-using objects.
	 * 
	 * @return Returns an iterator of the memory-using objects.
	 */
	public Iterator getMemUsers() {
		return strategy.getMemUsers();
	}
	
	/**
	 * Returns the number of actually registered memory-using objects.
	 * 
	 * @return Returns the number of actually registered memory-using objects.
	 */
	public int getNoOfManagedMemUsers() {
		return strategy.getNoOfManagedMemUsers();
	}

	/**
	 * Receives a request for an amount of memory from a memory-using object.
	 * This method returns immediately, but without a return value.
	 * The memory-using object recognises whether its request is approved,
	 * by observing, if its <code>assignMemSize</code>-method is called
	 * in an appropriate way or not.
	 * 
	 * @param memUser the requesting memory-using object.
	 * @param newMemSize the desired amount of memory (in bytes).
	 */
	public void requestForMemory(MemoryManageable memUser, int newMemSize) {
		strategy.requestForMemory(memUser, newMemSize);
	}
}
