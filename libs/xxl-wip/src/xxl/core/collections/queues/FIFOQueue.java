/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.collections.queues;

/**
 * The interface FIFO queue represents a FIFO (<i>first in, first out</i>)
 * iteration over a collection of elements (also known as a
 * <i>sequence</i>) with a <tt>peek</tt> method. This interface predefines
 * a <i>FIFO strategy</i> for addition and removal of elements.
 * 
 * @param <E> the type of the elements of this queue.
 * @see xxl.core.collections.queues.Queue
 */
public interface FIFOQueue<E> extends Queue<E> {
	
}
