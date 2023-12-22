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
 * Implementation of a uniform memory distribution.
 * This implementation always solves every memory overflow in a very simple way.
 * 
 * @since 1.1
 */
public class UniformStrategy extends AbstractStrategy {

	/**
	 * Constructs a UniformStrategy with a specified amount of global main memory
	 * and registeres all of the memory using objects of the given array.
	 * 
	 * @param globalMemSize The global amount of main memory (in bytes).
	 * @param memUsers the memory using objects to register.
	 */
	public UniformStrategy(int globalMemSize, MemoryManageable[] memUsers) {
		super(globalMemSize, memUsers);
	}
	
	/**
	 * Constructs a UniformStrategy with one registered memory using object
	 * and a specified amount of global main memory.
	 * 
	 * @param globalMemSize The global amount of main memory (in bytes).
	 * @param memUser the memory using object to register.
	 */
	public UniformStrategy(int globalMemSize, MemoryManageable memUser) {
		super(globalMemSize, memUser);
	}

	/**
	 * Constructs a UniformStrategy with no registered memory using objects
	 * and a specified amount of global main memory.
	 * 
	 * @param globalMemSize The global amount of main memory (in bytes).
	 */
	public UniformStrategy(int globalMemSize) {
		super(globalMemSize);
	}

	/**
	 * Handles a memory overflow. 
	 * This method considers 80% of the difference of the total 
	 * available main memory minus the memory used by monitorable objects as
	 * distributable and distributes it uniformly among all of the registered
	 * memory using objects. <br>
	 * If the monitorable objects use the complete memory, the manageable will get nothing.<br>
	 * If the monitorable objects exceed complete memory, an runtime exception is thrown.
	 */
	@Override
	public void handleOverflow() {		
		if (!detectOverflow())
			return; 
		int memToDistribute = getGlobalMemSize();
		for (Iterator<MemoryMonitorable> it = monitorableMemUsers.iterator(); it.hasNext();)
			memToDistribute -= it.next().getCurrentMemUsage();
		if (memToDistribute < 0) {
			throw new RuntimeException("The memorymonitorable Objects are using more memory than the maximum value");			
		}
		if (memToDistribute == 0) {
			System.out.println("The memorymonitorable Objects are using all memory, for the memorymonitorable objects is nothing left");
			return;
		}
		Iterator<MemoryManageable> memUsers = getManageableMemUsers();		
		int newMem = (memToDistribute * 4/5) / getNoOfManagedMemUsers();
		while (memUsers.hasNext()) {
			MemoryManageable memUser = memUsers.next();
			assignMemSize(memUser, newMem);
		}
	}

}
