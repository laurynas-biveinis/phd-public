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

import java.util.ArrayList;
import java.util.Iterator;

import xxl.core.cursors.unions.Sequentializer;

/**
 * This abstract class is the superclass for each kind of strategy for
 * memory distribution.
 * This class manages registration and deregistration of memory-using objects
 * and it ensures that the total memory usage never exceeds the total amount
 * of available memory.
 * Implementors have only to specify how to handle an overflow.
 * 
 * @since 1.1
 */
public abstract class AbstractStrategy implements Strategy {
	
	/**
	 * The total amount of available main memory.
	 */
	protected int globalMemSize;

	/**
	 * The total amount of memory which is actually assigned to the
	 * memory-using objects.
	 */
	protected int currentMemUsage;

	/**
	 * A list of the registered memory-using objects that are manageable.
	 */
	protected ArrayList<MemoryManageable> manageableMemUsers = new ArrayList<MemoryManageable>();
	
	/**
	 * A list of the registered memory-using objects that are monitorable.
	 */
	protected ArrayList<MemoryMonitorable> monitorableMemUsers = new ArrayList<MemoryMonitorable>();

	
	/**
	 * Constructs a strategy with a specified amount of global main memory
	 * and registers all of the memory-using objects of the given array.
	 * 
	 * @param globalMemSize The global amount of main memory (in bytes).
	 * @param memUsers the memory-using objects to register.
	 */
	public AbstractStrategy(int globalMemSize, MemoryMonitorable[] memUsers) {
		this.globalMemSize = globalMemSize;
		for (int i = 0; i < memUsers.length; i++)
			register(memUsers[i]);
	}
	
	/**
	 * Constructs a strategy with one registered memory-using object
	 * and a specified amount of global main memory.
	 * 
	 * @param globalMemSize The global amount of main memory (in bytes).
	 * @param memUser the memory-using object to register.
	 */
	public AbstractStrategy(int globalMemSize, MemoryMonitorable memUser) {
		this.globalMemSize = globalMemSize;
		register(memUser);
	}
	
	/**
	 * Constructs a strategy with no registered memory using objects
	 * and a specified amount of global main memory.
	 * 
	 * @param globalMemSize The global amount of main memory (in bytes).
	 */
	public AbstractStrategy(int globalMemSize) {
		this.globalMemSize = globalMemSize;
	}

	/**
	 * Registers a memory-using object.
	 * 
	 * @param memUser the memory-using object to register.
	 */
	public void register(MemoryMonitorable memUser) {
		if (memUser == null)
			throw new IllegalArgumentException("No valid memUser specified.");
		if (memUser instanceof MemoryManageable){
			this.manageableMemUsers.add((MemoryManageable)memUser);			
		}			
		else 
			this.monitorableMemUsers.add(memUser);
	}
	
	/**
	 * Deregisters a memory-using object.
	 * 
	 * @param memUser the memory using object to deregister.
	 */
	public void deregister(MemoryMonitorable memUser) {
		if (memUser == null)
			throw new IllegalArgumentException("No valid memUser specified.");
		if (memUser instanceof MemoryManageable){
			this.manageableMemUsers.remove(memUser);
			currentMemUsage -= ((MemoryManageable)memUser).getAssignedMemSize();
		}			
		else 
			this.monitorableMemUsers.remove(memUser);		
	}
	
	
	/**
	 * Returns an iterator of the registered, monitorable memory-using objects.
	 * They implement the memorymonitorable or memorymanageable interface.
	 * 
	 * @return Returns an iterator of the memory-using objects.
	 */
	public Iterator<MemoryMonitorable> getMemUsers() {
		return new Sequentializer(monitorableMemUsers.iterator(), manageableMemUsers.iterator());
	}

	/**
	 * Returns an iterator of the registered, manageable memory-using objects.
	 * They implement the memorymanageable interface.
	 *  
	 * @return Returns an iterator of the memory-using objects.
	 */
	public Iterator<MemoryManageable> getManageableMemUsers() {
		return manageableMemUsers.iterator();
	}
	
	/**
	 * Returns the number of actually registered memory-using objects.
	 * They implement the memorymanageable interface.
	 * 
	 * @return Returns the number of actually registered memory-using objects.
	 */
	public int getNoOfManagedMemUsers() {
		return manageableMemUsers.size();
	}
	
	/**
	 * Returns the number of actually registered monitorable memory-using objects.
	 * They implement the memorymonitorable or memorymanageable interface.
	 * 
	 * @return Returns the number of actually registered memory-using objects.
	 */
	public int getNoOfMonitorableMemUsers(){
		return manageableMemUsers.size() + monitorableMemUsers.size();
	}
	
	/**
	 * Sets a new global amount of main memory.
	 * If this causes a memory overflow,
	 * the <code>handleOverflow</code>-method is called.
	 * 
	 * @param newGlobalMemSize The new global amount of main memory (in bytes).
	 */
	public void setGlobalMemSize(int newGlobalMemSize) {
		this.globalMemSize = newGlobalMemSize;
		if (detectOverflow())
			handleOverflow();
	}

	/**
	 * Returns the global amount of main memory.
	 * 
	 * @return Returns the global amount of main memory (in bytes).
	 */
	public int getGlobalMemSize() {
		return globalMemSize;
	}

	/**
	 * Returns the total amount of memory which is actually assigned to the
	 * registered memory-using objects.
	 * 
	 * @return Returns the total amount of assigned memory (in bytes).
	 */
	public int getCurrentMemUsage() {
		return currentMemUsage;
	}

	/**
	 * Receives a request for an amount of memory from a memory-using object.
	 * This method directly assigns the desired amount of memory to the
	 * requesting object and checks whether a memory overflow has
	 * occured. If so, it calls the <code>handleOverflow</code>-method.
	 * 
	 * @param memUser the requesting memory-using object.
	 * @param newMemSize the desired amount of memory (in bytes).
	 */
	public void requestForMemory (MemoryMonitorable memUser, int newMemSize) {
		if (memUser == null)
			throw new IllegalArgumentException("No valid memUser specified.");
		if (memUser instanceof MemoryManageable)
			assignMemSize((MemoryManageable)memUser, newMemSize);
		if (detectOverflow())
			handleOverflow();
	}

	/**
	 * Assigns a special amount of memory to the specified memory-using object.
	 * If this assignment causes a memory overflow,
	 * the <code>handleOverflow</code>-method is <B>not</B> called,
	 * because the implementor of the <code>handleOverflow</code>-method
	 * shall be able to solve a memory overflow caused by a call of the
	 * <code>setGlobalMemSize</code>-method gradually (object by object).
	 * This technique would possibly result in an endless recursion,
	 * if this method would call the <code>handleOverflow</code>-method
	 * each time an overflow occurs. That's why it does not.
	 * So, in this case the caller of this method is responsible therefor
	 * that his assignments will finally lead to an overflow-free state.
	 * 
	 * @param memUser the memory-using object as the recipient of the assignment.
	 * @param newMemSize The amount of memory to be assigned (in bytes).
 	 */
	public void assignMemSize(MemoryManageable memUser, int newMemSize) {
		currentMemUsage -= memUser.getAssignedMemSize();
		memUser.assignMemSize(newMemSize);
		currentMemUsage += newMemSize;
	}

	/**
	 * Detects wheather a memory overflow has occured or not.
	 * 
	 * @return Returns <code>true</code> if an overflow has occured.
 	 */
	public boolean detectOverflow() {
		return currentMemUsage > globalMemSize;
	}

	/**
	 * This method handles a memory overflow (i.e., it has to solve it).
	 * The implementor specifies in this method the way of distribution of
	 * memory among the memory-using objects.
	 */
	public abstract void handleOverflow();

}
