/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.functions;

import java.util.List;

/**
 * A constant is a function that returns a constant value.
 * 
 * @param <T> the type of the constant to be returned.
 */
public class Constant<T> extends AbstractFunction<Object, T> {

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
	@Override
	public T invoke(List<? extends Object> objects) {
		return object;
	}
}
