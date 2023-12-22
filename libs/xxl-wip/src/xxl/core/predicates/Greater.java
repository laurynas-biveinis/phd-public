/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.predicates;

import java.util.Comparator;

/**
 * This class provides a binary predicate that returns <code>true</code> if the
 * first given argument is greater than the second. In other words, when
 * <code>argument0</code> and <code>argument1</code> are the given arguments,
 * the predicate returns <code>true</code> if
 * (<code>argument0&gt;argument1</code>) holds.
 * 
 * @param <P> the type of the predicate's parameters.
 */
public class Greater<P> extends ComparatorPredicate<P> {

	/**
	 * Creates a new binary predicate that determines whether the first given
	 * argument is greater than the second.
	 *
	 * @param comparator the comparator that should be used for
	 *        comparing objects.
	 */
	public Greater(Comparator<? super P> comparator) {
		super(comparator);
	}

	/**
	 * Returns <code>true</code> if the <code>argument0</code> is greater than
	 * <code>argument1</code>. In other words, returns
	 * <code>comparator.compare(argument0,argument1)&gt;0</code>.
	 *
	 * @param argument0 the first argument to the predicate.
	 * @param argument1 the second argument to the predicate.
	 * @return <code>true</code> if the <code>argument0</code> is greater than
	 *         <code>argument1</code>, otherwise <code>false</code>.
	 */
	@Override
	public boolean invoke(P argument0, P argument1){
		return comparator.compare(argument0, argument1) > 0;
	}
}
