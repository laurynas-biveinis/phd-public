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

package xxl.core.util;

import java.util.Stack;

import xxl.core.functions.Function;

/**
 *  An object pool allows the reuse of unneeded objects by storing them in a pool.
 *  The efford lies in avoiding the garbage collection and recreation of commonly used objects.
 */
public class ObjectPool {

	/**
 	 *  A parameterless function returning a new instance of an arbitrary object.
	 */
	protected Function tupleFactory;

	/**
 	 *  The maximum number of objects to be stored in the pool.
	 */
	protected int maxSize;

	/**
 	 *  The object pool internally realized by a {@link java.util.Stack Stack}.
	 */
	protected Stack pool = new Stack();

	/**
 	 *  Constructs a new object pool.
 	 *  @param tupleFactory A parameterless function returning a new instance of an arbitrary object.
 	 *  @param maxSize The maximum number of objects to be stored in the pool.
	 */
	public ObjectPool (Function tupleFactory, int maxSize) {
		this.tupleFactory=tupleFactory;
		this.maxSize=maxSize;
	}

	/**
 	 *  Sets the maximum number of objects to be stored in the pool.
 	 *  @param newMaxSize The maximum number of objects to be stored in the pool.
	 */
	public void setMaxSize(int newMaxSize) {
		maxSize=newMaxSize;
		for (int i=pool.size(); i>maxSize; i--) pool.pop();
	}

	/**
 	 *  Retrieves an object from the pool if available, otherwise the result of
 	 *  <code>tupleFactory.invoke()</code> is returned.
 	 *  @return an object from the pool if available, otherwise
 	 *  <code>tupleFactory.invoke()</code>.
	 */
	public Object getObject () {
		if (pool.empty()) return tupleFactory.invoke();
		return pool.pop();
	}

	/**
	 *  Deliveres an object to the pool. If the maximum size of the pool is not reached,
	 *  the object is stored, otherwise it will be dropped.
 	 *  @param done The object to be delivered to the pool.
	 */
	public void releaseObject (Object done) {
		if (pool.size()<maxSize) pool.push(done);
	}
}