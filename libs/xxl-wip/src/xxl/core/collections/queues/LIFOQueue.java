/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.collections.queues;

import xxl.core.functions.Function;

/**
 * The interface LIFO queue represents a LIFO (<i>last in, first out</i>)
 * iteration over a collection of elements (also known as a
 * <i>sequence</i>) with a <tt>peek</tt> method. This interface predefines
 * a <i>LIFO strategy</i> for addition and removal of elements.
 * 
 * @param <E> the type of the elements of this queue.
 * @see xxl.core.collections.queues.Queue
 */
public interface LIFOQueue<E> extends Queue<E> {

	/**
	 * A factory method to create a default LIFO queue (see contract for
	 * {@link Queue#FACTORY_METHOD FACTORY_METHOD} in interface Queue).
	 * This field is set to
	 * <code>{@link StackQueue#FACTORY_METHOD StackQueue.FACTORY_METHOD}</code>.
	 */
	public static final Function FACTORY_METHOD = StackQueue.FACTORY_METHOD;
	
}
