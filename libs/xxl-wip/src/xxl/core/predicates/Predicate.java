/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.predicates;

import java.util.List;

/**
 * This interface provides a predicate, i.e. a kind of function that implements
 * a logical statement and, when it is invoked, determines whether specified
 * objects fulfill the statement. Predicates are highly related to
 * {@link xxl.core.functions.Function functions}. Like functions, predicates
 * provide a set of <code>invoke</code> methods that can be used to evaluate
 * the predicate. For providing predicates with and without parameters, this
 * class contains invoke methods with zero, one and two arguments and with a
 * typed list of arguments.
 *
 * @param <P> the type of the predicate's parameters.
 */
public interface Predicate<P> {

	/**
	 * Returns the result of the predicate as a primitive boolean value.
	 *
	 * @param arguments the arguments to the predicate.
	 * @return the result of the predicate as a primitive boolean value.
	 */
	public abstract boolean invoke(List<? extends P> arguments);

	/**
	 * Returns the result of the predicate as a primitive boolean value.
	 *
	 * @return the result of the predicate as a primitive boolean value.
	 */
	public abstract boolean invoke();

	/**
	 * Returns the result of the predicate as a primitive boolean value.
	 *
	 * @param argument the argument to the predicate.
	 * @return the result of the predicate as a primitive boolean value.
	 */
	public abstract boolean invoke(P argument);

	/**
	 * Returns the result of the predicate as a primitive boolean value.
	 *
	 * @param argument0 the first argument to the predicate.
	 * @param argument1 the second argument to the predicate.
	 * @return the result of the predicate as a primitive boolean value.
	 */
	public abstract boolean invoke(P argument0, P argument1);

}
