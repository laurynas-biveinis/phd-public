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
 * first given argument is less than or equal to the second. In other words,
 * when <code>argument0</code> and <code>argument1</code> are the given
 * arguments, the predicate returns <code>true</code> if
 * (<code>argument0&le;argument1</code>) holds.
 * 
 * @param <P> the type of the predicate's parameters.
 */
public class LessEqual<P> extends Or<P> {

	/**
	 * Creates a new binary predicate that determines whether the first given
	 * argument is less than or equal to the second.
	 *
	 * @param comparator the comparator that should be used for comparing
	 *        objects.
	 */
	public LessEqual(Comparator<? super P> comparator) {
		super(new Less<P>(comparator), new ComparatorBasedEqual<P>(comparator));
	}
}
