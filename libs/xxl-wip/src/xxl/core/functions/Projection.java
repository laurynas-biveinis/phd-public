/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.functions;

import java.lang.reflect.Array;

/**
 * An object of this class projects an input array-argument to a "sub"-array of
 * arguments. The fields of the array to be kept have to be specified. The
 * projections done by objects of this class are performed by truncating the
 * given array of arguments to project. A different approach is provided by the
 * {@link xxl.core.functions.FastMapProjection FastMap-Projection} using a
 * distance function given upon the object space. Check this class for further
 * details.
 *
 * @param <T> the component type of the arrays to be projected.
 * @see xxl.core.functions.Function
 * @see xxl.core.functions.FastMapProjection
 */
public class Projection<T> extends AbstractFunction<T[], T[]> {

	/**
	 * The fields to keep in the projection.
	 */
	protected int[] indices;

	/**
	 * Constructs a new Object of this type.
	 * 
	 * @param indices fields to keep in the projection.
	 */
	public Projection(int... indices) {
		this.indices = indices;
	}

	/**
	 * Performs the projection keeping the given fields.
	 * 
	 * @param objects array of arguments to project.
	 * @return the "sub"-array containing the objects corresponding to the
	 *         given indices.
	 */
	@Override
	@SuppressWarnings("unchecked") // array of correct component type is created by using reflection
	public T[] invoke(T[] objects) {
		T[] projection = (T[])Array.newInstance(objects.getClass().getComponentType(), indices.length);
		for (int i = 0; i < indices.length; i++)
			projection[i] = objects[indices[i]];
		return projection;
	}

	/**
	 * Returns the used indices for projection.
	 * 
	 * @return the used indices for projection
	 */
	public int[] getIndices() {
		return indices;
	}
}
