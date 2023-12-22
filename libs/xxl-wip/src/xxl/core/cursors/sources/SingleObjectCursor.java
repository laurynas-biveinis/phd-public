/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.cursors.sources;

/**
 * This class provides a cursor that returns a given object exactly one time.
 * It simply depends on a {@link Repeater repeater} that's number of repeating
 * times is set to 1.
 * 
 * @param <E> the type of the elements returned by this iteration.
 */
public class SingleObjectCursor<E> extends Repeater<E> {

	/**
	 * Creates a new single object-cursor that returns the given object exactly
	 * one time.
	 * 
	 * @param object the object that should be returned by the single
	 *        object-cursor exactly one time.
	 */
	public SingleObjectCursor(E object) {
		super(object, 1);
	}

}
