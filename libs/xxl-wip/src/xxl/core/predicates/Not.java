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
 * This class provides a logical NOT predicate. The NOT predicate represents
 * the negation of a predicates to a new predicate that returns
 * <code>true</code> if and only if the underlying predicate returns
 * <code>false</code>.
 * 
 * @param <P> the type of the predicate's parameters.
 */
public class Not<P> extends DecoratorPredicate<P> {

	/**
	 * Creates a new NOT predicate that represents the negation of the
	 * specified predicate.
	 *
	 * @param predicate the predicate to be negated.
	 */
	public Not(Predicate<? super P> predicate) {
		super(predicate);
	}

	/**
	 * Returns <code>true</code> if and only if the underlying predicate
	 * returns <code>false</code>, otherwise <code>false</code> is returned.
	 *
	 * @param arguments the arguments to the underlying predicate.
	 * @return <code>true</code> if and only if the underlying predicate
	 *         returns <code>false</code>, <code>false</code> otherwise.
	 */
	@Override
	public boolean invoke(List<? extends P> arguments){
		return !predicate.invoke(arguments);
	}

	/**
	 * Returns <code>true</code> if and only if the underlying predicate
	 * returns <code>false</code>, otherwise <code>false</code> is returned.
	 *
	 * @return <code>true</code> if and only if the underlying predicate
	 *         returns <code>false</code>, <code>false</code> otherwise.
	 */
	@Override
	public boolean invoke() {
		return !predicate.invoke();
	}

	/**
	 * Returns <code>true</code> if and only if the underlying predicate
	 * returns <code>false</code>, otherwise <code>false</code> is returned.
	 *
	 * @param argument the argument to the underlying predicate.
	 * @return <code>true</code> if and only if the underlying predicate
	 *         returns <code>false</code>, <code>false</code> otherwise.
	 */
	@Override
	public boolean invoke(P argument) {
		return !predicate.invoke(argument);
	}

	/**
	 * Returns <code>true</code> if and only if the underlying predicate
	 * returns <code>false</code>, otherwise <code>false</code> is returned.
	 *
	 * @param argument0 the first argument to the underlying predicate.
	 * @param argument1 the second argument to the underlying
	 *        predicate.
	 * @return <code>true</code> if and only if the underlying predicate
	 *         returns <code>false</code>, <code>false</code> otherwise.
	 */
	@Override
	public boolean invoke(P argument0, P argument1) {
		return !predicate.invoke(argument0, argument1);
	}
}
