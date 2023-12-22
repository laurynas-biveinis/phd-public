/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.concurrency;

/**
 * This class is useful if threads are accessing a common variable 
 * (or for simulating reference parameters in methods).
 */
public class MutableInt {
	/** internal value */
	protected int value;

	/** 
	 * Creates a MutableInt.
	 * @param value sets the initial value 
	 */
	public MutableInt(int value) {
		this.value = value;
	}

	/** 
	 * Sets the internal value.
	 * @param value new value
	 */
	public synchronized void set(int value) {
		this.value = value;
	}

	/** 
	 * Gets the internal value.
	 * @return returns the internal value
	 */
	public synchronized int get() {
		return value;
	}
}
