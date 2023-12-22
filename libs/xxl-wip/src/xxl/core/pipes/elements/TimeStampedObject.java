/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.pipes.elements;

import java.util.Comparator;

/**
 * Interface for objects containing a timestamp. 
 * @param <T> 
 */
public interface TimeStampedObject<T> {
	
	/**
	 * Comparator that compares the start time stamps of two instances.
	 */
	public static final Comparator<TimeStampedObject> START_TIMESTAMP_COMPARATOR = new Comparator<TimeStampedObject>() {
		public int compare(TimeStampedObject o1, TimeStampedObject o2) {
			long t1 = o1.getTimeStamp(); 
			long t2 = o2.getTimeStamp();
			return t1 < t2 ? -1 : t1 > t2 ? +1 : 0;
		}
	};
	
	/**
	 * This method returns the timestamp of an object.
	 * @return the timestamp of an object.
	 */
	public abstract long getTimeStamp();
	
	/**
	 * Returns the object of an instance.
	 * @return object of the instance
	 */
	public abstract T getObject();
	
}
