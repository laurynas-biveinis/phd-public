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
 * This class provides a logical AND predicate. The AND predicate represents
 * the conjunction of two predicates to a new predicate that returns
 * <code>true</code> if and only if both underlying predicates return
 * <code>true</code>.
 *
 * @param <P> the type of the predicate's parameters.
 */
public class And<P> extends BinaryPredicate<P> {

	/**
	 * Creates a new AND predicate that represents the conjunction of the
	 * specified predicates.
	 *
	 * @param predicate0 the first predicate of the conjunction.
	 * @param predicate1 the second predicate of the conjunction.
	 */
	public And(Predicate<? super P> predicate0, Predicate<? super P> predicate1) {
		super(predicate0, predicate1);
	}

	/**
	 * Returns <code>true</code> if and only if both underlying predicates
	 * return <code>true</code>, otherwise <code>false</code> is returned.
	 *
	 * @param arguments the arguments to the underlying predicates.
	 * @return <code>true</code> if and only if both underlying predicates
	 *         return <code>true</code>, otherwise <code>false</code>.
	 */
	@Override
	public boolean invoke(List<? extends P> arguments) {
		return predicate0.invoke(arguments) && predicate1.invoke(arguments);
	}

	/**
	 * Returns <code>true</code> if and only if both underlying predicates
	 * return <code>true</code>, otherwise <code>false</code> is returned.
	 *
	 * @return <code>true</code> if and only if both underlying predicates
	 *         return <code>true</code>, otherwise <code>false</code>.
	 */
	@Override
	public boolean invoke() {
		return predicate0.invoke() && predicate1.invoke();
	}

	/**
	 * Returns <code>true</code> if and only if both underlying predicates
	 * return <code>true</code>, otherwise <code>false</code> is returned.
	 *
	 * @param argument the argument to the underlying predicates.
	 * @return <code>true</code> if and only if both underlying predicates
	 *         return <code>true</code>, otherwise <code>false</code>.
	 */
	@Override
	public boolean invoke(P argument) {
		return predicate0.invoke(argument) && predicate1.invoke(argument);
	}

	/**
	 * Returns <code>true</code> if and only if both underlying predicates
	 * return <code>true</code>, otherwise <code>false</code> is returned.
	 *
	 * @param argument0 the first arguments to the underlying
	 *        predicates.
	 * @param argument1 the second arguments to the underlying
	 *        predicates.
	 * @return <code>true</code> if and only if both underlying predicates
	 *         return <code>true</code>, otherwise <code>false</code>.
	 */
	@Override
	public boolean invoke(P argument0, P argument1) {
		return predicate0.invoke(argument0, argument1) && predicate1.invoke(argument0, argument1);
	}
}
