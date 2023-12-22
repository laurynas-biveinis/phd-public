/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.io.fat.util;

import java.util.Stack;

/**
 * This class supports an inorder traversal when used with the AllEntriesIterator of class DIR.
 */
public class InorderTraverse implements DataManagement
{	
	/**
	 * Data structure to hold the data. A stack implies an inorder traversal.
	 */
	protected Stack stack;
		
	/**
	 * Create an instance of this object.
	 */
	public InorderTraverse()
	{
		stack = new Stack();
	}	//end constructor
	
	
	/**
	 * Return true if there is an other element.
	 * @return true if there is an other element.
	 */
	public boolean hasNext()
	{
		return !stack.empty();
	}	//end hasNext()
	
	/**
	 * Return the next element.
	 * @return the next element.
	 */
	public Object next()
	{
		return stack.pop();
	}	//end next()
	
	
	/**
	 * Add one element to the underlying structure.
	 * 
	 * @param o the given element 
	 */
	public void add(Object o)
	{
		stack.push(o);
	}	//end add(Object o)
	
	
	/**
	 * Remove and return the actual element. If no such element
	 * exist null is returned.
	 * @return the actual element or null if non such element exist.
	 */
	public Object remove()
	{
		return stack.pop();
	}	//end remove()
}
