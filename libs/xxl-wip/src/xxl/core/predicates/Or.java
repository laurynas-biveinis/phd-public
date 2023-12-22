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
 * This class provides a logical OR predicate. The OR predicate represents the
 * disjunction of two predicates to a new predicate that returns
 * <code>false</code> if and only if both underlying predicates return
 * <code>false</code>.
 *
 * @param <P> the type of the predicate's parameters.
 */
public class Or<P> extends BinaryPredicate<P> {

	/**
	 * Creates a new OR predicate that represents the disjunction of the
	 * specified predicates.
	 *
	 * @param predicate0 the first predicate of the disjunction.
	 * @param predicate1 the second predicate of the disjunction.
	 */
 	public Or (Predicate<? super P> predicate0, Predicate<? super P> predicate1) {
		super(predicate0, predicate1);
	}

	/**
	 * Returns <code>false</code> if and only if both underlying predicates
	 * return <code>false</code>, otherwise <code>true</code> is returned.
	 *
	 * @param arguments the arguments to the underlying predicates.
	 * @return <code>false</code> if and only if both underlying predicates
	 *         return <code>false</code>, otherwise <code>true</code>.
	 */
	@Override
	public boolean invoke(List<? extends P> arguments) {
		return predicate0.invoke(arguments) || predicate1.invoke(arguments);
	}

	/**
	 * Returns <code>false</code> if and only if both underlying predicates
	 * return <code>false</code>, otherwise <code>true</code> is returned.
	 *
	 * @return <code>false</code> if and only if both underlying predicates
	 *         return <code>false</code>, otherwise <code>true</code>.
	 */
	@Override
	public boolean invoke() {
		return predicate0.invoke() || predicate1.invoke();
	}

	/**
	 * Returns <code>false</code> if and only if both underlying predicates
	 * return <code>false</code>, otherwise <code>true</code> is returned.
	 *
	 * @param argument the argument to the underlying predicates.
	 * @return <code>false</code> if and only if both underlying predicates
	 *         return <code>false</code>, otherwise <code>true</code>.
	 */
	@Override
	public boolean invoke(P argument) {
		return predicate0.invoke(argument) || predicate1.invoke(argument);
	}

	/**
	 * Returns <code>false</code> if and only if both underlying predicates
	 * return <code>false</code>, otherwise <code>true</code> is returned.
	 *
	 * @param argument0 the first arguments to the underlying predicates.
	 * @param argument1 the second arguments to the underlying predicates.
	 * @return <code>false</code> if and only if both underlying predicates
	 *         return <code>false</code>, otherwise <code>true</code>.
	 */
	@Override
	public boolean invoke(P argument0, P argument1) {
		return predicate0.invoke(argument0, argument1) || predicate1.invoke(argument0, argument1);
	}
}
