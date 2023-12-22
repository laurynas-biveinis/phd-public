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
public class Projection<T> extends Function<T[], T[]> {

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