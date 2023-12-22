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
import xxl.core.util.Decorator;

/**
 * 
 */
public class DecoratorSweepArea<I,E> extends AbstractSweepArea<I,E> implements Decorator<SweepArea<I,E>> {

	/**
	 * The underlying sweeparea. 
	 */
	protected final SweepArea<I,E> sweepArea;	

	public DecoratorSweepArea(SweepArea<I,E> sweepArea, int objectSize) {
		super(objectSize);
		this.sweepArea = sweepArea;
	}

	public DecoratorSweepArea(SweepArea<I,E> sweepArea) {
		this(sweepArea, MemoryMonitorable.SIZE_UNKNOWN);
	}	
	
	public void insert(I o) throws IllegalArgumentException {
		super.insert(o);
		sweepArea.insert(o);
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
		return sweepArea.query(o, ID);
	}
	
	public Iterator<E> query(I [] os, int [] IDs, int valid) throws IllegalArgumentException {
		return sweepArea.query(os, IDs, valid);
	}

	public Iterator<E> query(I [] os, int [] IDs) throws IllegalArgumentException {
		return sweepArea.query(os, IDs);
	}

	public Iterator<E> expire (I currentStatus, int ID) {
		return sweepArea.expire(currentStatus, ID);
	}
	
	public void reorganize(I currentStatus, int ID) throws IllegalStateException {
		sweepArea.reorganize(currentStatus, ID);
	}
	
	@Override
	public SweepArea<I,E> getDecoree() {
		return sweepArea;
	}

}
