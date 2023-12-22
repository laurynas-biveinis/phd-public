/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.functions;

/** 
 * The function provides a simple identity-function.
 *
 * @param <T> the type of the identity-functions parameters (and it's return
 *        type.
 */
public class Identity<T> extends AbstractFunction<T, T> {

	/**
	 * A prototype function simply returns its arguments (identity-function).
	 */
	public static final Function<Object, Object> DEFAULT_INSTANCE = new Identity<Object>();

	/**
	 * Simply returns the given argument.
	 * 
	 * @param argument the argument to be returned.
	 * @return the given argument.
	 */
	@Override
	public T invoke(T argument) {
		return argument;
	}
}
