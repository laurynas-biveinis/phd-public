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
package xxl.core.collections.sweepAreas;

import xxl.core.pipes.memoryManager.MemoryManageable;

/**
 * This abstract class decorates a SweepArea to make it memory manageable.
 * Implementors of subclasses have to specify how to handle an overflow.
 */
public abstract class MemoryManageableSA<E> extends DecoratorSweepArea<E> implements MemoryManageable {

	/**
	 * The preferred amount of memory (in bytes).
	 */
	protected int preferredMemSize;

	/**
	 * The assigned amount of memory (in bytes).
	 */
	protected int assignedMemSize;

	/**
	 * Creates a memory manageable SweepArea by decorating the underlying
	 * SweepArea.
	 * 
	 * @param sa The underlying SweepArea
	 * @param objectSize The size of the objects in this SweepArea (in bytes).
	 * @param memSize The preferred amount of memory (in bytes).
	 */
	public MemoryManageableSA(SweepArea<E> sa, int objectSize, int memSize) {
		super(sa);
		this.objectSize = objectSize;
		this.preferredMemSize = memSize;
	}

	/**
	 * Returns the preferred amount of memory for this SweepArea.
	 * 
	 * @return Returns the preferred amount of memory (in bytes).
	 */
	public int getPreferredMemSize() {
		return preferredMemSize;
	}
	
	/**
	 * Returns the amount of memory which is assigned to this SweepArea.
	 * 
	 * @return Returns the assigned amount of memory (in bytes).
	 */
	public int getAssignedMemSize() {
		return assignedMemSize;
	}
	
	/**
	 * Assigns a special amount of memory to this SweepArea.
	 * If this assignment causes a memory overflow,
	 * the <code>handleOverflow</code>-method is called.
	 * 
	 * @param newMemSize The amount of memory to be assigned to this SweepArea
	 *                   (in bytes).
 	 */
	public void assignMemSize(int newMemSize) {
		if (newMemSize<0)
			throw new IllegalArgumentException("newMemSize < 0");
		assignedMemSize = newMemSize;
		if (getCurrentMemUsage()>assignedMemSize)
			handleOverflow();
	}

	/**
	 * Inserts an object into the underlying SweepArea and
	 * checks, wheather a memory overflow has occured or not.
	 * If an overflow has occured,
	 * the <code>handleOverflow</code>-method is called.
	 * 
	 * @param object The object to insert into the SweepArea.
 	 */
	public void insert (E object) {
		super.insert(object);
		if (getCurrentMemUsage()>assignedMemSize)
			handleOverflow();
	}		

	/**
	 * This method is called, if an overflow has occured.
	 * Implementors of subclasses have to specify
	 * how to handle the overflow.
	 */
	public abstract void handleOverflow();

}
