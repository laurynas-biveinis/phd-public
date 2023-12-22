/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.collections.sweepAreas;

import java.util.Iterator;

import xxl.core.pipes.memoryManager.MemoryMonitorable;

public class CountingSweepArea<I,E> extends DecoratorSweepArea<I,E> {

	protected long insertionCalls;
	protected long queryCalls;
	protected long expirationCalls;
	protected long reorganizationCalls;
	
	public CountingSweepArea(SweepArea<I,E> sweepArea, int objectSize) {
		super(sweepArea, objectSize);
		resetCounters();
	}

	public CountingSweepArea(SweepArea<I,E> sweepArea) {
		this(sweepArea, MemoryMonitorable.SIZE_UNKNOWN);
	}	
	
	public void insert(I o) throws IllegalArgumentException {
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
	
	public Iterator<E> query(I o, int ID) throws IllegalArgumentException {
		queryCalls++;
		return sweepArea.query(o, ID);
	}
	
	public Iterator<E> query(I [] os, int [] IDs, int valid) throws IllegalArgumentException {
		queryCalls++;
		return sweepArea.query(os, IDs, valid);
	}

	public Iterator<E> query(I [] os, int [] IDs) throws IllegalArgumentException {
		queryCalls++;
		return sweepArea.query(os, IDs);
	}

	public Iterator<E> expire (I currentStatus, int ID) {
		expirationCalls++;
		return sweepArea.expire(currentStatus, ID);
	}
	
	public void reorganize(I currentStatus, int ID) throws IllegalStateException {
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
