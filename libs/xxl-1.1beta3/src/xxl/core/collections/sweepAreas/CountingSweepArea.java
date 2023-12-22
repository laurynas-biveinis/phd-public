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

import java.util.Iterator;

import xxl.core.pipes.memoryManager.MemoryMonitorable;

public class CountingSweepArea<E> extends DecoratorSweepArea<E> {

	protected long insertionCalls;
	protected long queryCalls;
	protected long expirationCalls;
	protected long reorganizationCalls;
	
	public CountingSweepArea(SweepArea<E> sweepArea, int objectSize) {
		super(sweepArea, objectSize);
		resetCounters();
	}

	public CountingSweepArea(SweepArea<E> sweepArea) {
		this(sweepArea, MemoryMonitorable.SIZE_UNKNOWN);
	}	
	
	public void insert(E o) throws IllegalArgumentException {
		insertionCalls++;
		super.insert(o); // super call needed to determine objectSize
	}
	
	public void clear() {
		sweepArea.clear();
	}
	
	public void close() {
		sweepArea.close();
	}
	
	public int size() {
		return sweepArea.size();
	}
	
	public Iterator<E> iterator() {
		return sweepArea.iterator();
	}
	
	public Iterator<E> query(E o, int ID) throws IllegalArgumentException {
		queryCalls++;
		return sweepArea.query(o, ID);
	}
	
	public Iterator<E> query(E [] os, int [] IDs, int valid) throws IllegalArgumentException {
		queryCalls++;
		return sweepArea.query(os, IDs, valid);
	}

	public Iterator<E> query(E [] os, int [] IDs) throws IllegalArgumentException {
		queryCalls++;
		return sweepArea.query(os, IDs);
	}

	public Iterator<E> expire (E currentStatus, int ID) {
		expirationCalls++;
		return sweepArea.expire(currentStatus, ID);
	}
	
	public void reorganize(E currentStatus, int ID) throws IllegalStateException {
		reorganizationCalls++;
		sweepArea.reorganize(currentStatus, ID);
	}
	
	public long getNoOfInsertionCalls() {
		return insertionCalls;
	}
	
	public long getNoOfQueryCalls() {
		return queryCalls;
	}
	
	public long getNoOfExpirationCalls() {
		return expirationCalls;
	}
	
	public long getNoOfReorganizationCalls() {
		return reorganizationCalls;
	}
	
	/**
	 * Resets all counters.
	 */
	public void resetCounters() {
		this.insertionCalls = 0;
		this.queryCalls = 0;
		this.expirationCalls = 0;
		this.reorganizationCalls = 0;
	}
	
}
