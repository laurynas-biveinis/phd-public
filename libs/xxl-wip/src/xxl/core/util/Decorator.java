/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util;

/**
 * A marker interface for decorators additionally providing a method that
 * returns the decorated objects (<it>decoree</it>).
 * 
 * @param <T> the type of the decorated object.
 */
public interface Decorator<T> {
	
	/**
	 * Returns the decorated object (<it>decoree</it>).
	 * 
	 * @return the decorated object (<it>decoree</it>).
	 */
	public abstract T getDecoree();

}
