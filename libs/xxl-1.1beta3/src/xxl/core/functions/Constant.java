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

import java.util.List;

/**
 * A constant is a function that returns a constant value.
 * 
 * @param <T> the type of the constant to be returned.
 */
public class Constant<T> extends Function<Object, T> {

	/**
	 * Constant function returning a Boolean object representing
	 * <code>true</code>.
	 */
	public static final Constant<Boolean> TRUE = new Constant<Boolean>(true);

	/**
	 * Constant function returning a Boolean object representing
	 * <code>false</code>.
	 */
	public static final Constant<Boolean> FALSE = new Constant<Boolean>(false);

	/**
	 * Constant returned by this function.
	 */
	protected final T object;

	/**
	 * Constructs a new constant function returning the given object.
	 * 
	 * @param object constant object to return by calling
	 *        {@link xxl.core.functions.Function#invoke() invoke}.
	 */
	public Constant(T object) {
		this.object = object;
	}

	/**
	 * Returns the stored constant value.
	 * 
	 * @param objects arguments of the function.
	 * @return the stored constant value.
	 */
	public T invoke(List<? extends Object> objects) {
		return object;
	}
}