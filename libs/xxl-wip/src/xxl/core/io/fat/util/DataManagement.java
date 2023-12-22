/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.util;

/**
 * This interface is used to manage some kind of data.
 * The implementation classes of this interface can be
 * a stack or a queue for instance. This will change
 * the order the data objects are used.
 * @see xxl.core.io.fat.util.InorderTraverse
 */
public interface DataManagement
{
	/**
	 * Return true if there is an other element.
	 * 
	 * @return true if there is an other element.
	 */
	public boolean hasNext();
	
	/**
	 * Return the next element.
	 * 
	 * @return the next element.
	 */
	public Object next();
	
	/**
	 * Add one element to the underlying structure.
	 * 
	 * @param o the element to be added.
	 */
	public void add(Object o);
	
	/**
	 * Remove and return the actual element. If no such element
	 * exist null is returned.
	 * 
	 * @return the removed element.
	 */
	public Object remove();
}	//end interface DataManagement
