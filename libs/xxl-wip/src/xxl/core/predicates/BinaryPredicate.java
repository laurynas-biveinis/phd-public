/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.predicates;

/**
 * This class provides a higher-order predicate (in other words, this predicate
 * decorates two input predicates). The binary predicate is the basis for every
 * predicate implementing a logical binary operator or the combination of two
 * predicates.
 *
 * @param <P> the type of the predicate's parameters.
 */
public abstract class BinaryPredicate<P> extends AbstractPredicate<P> {

	/**
	 * A reference to the first decorated predicate. This reference is used to
	 * perform method calls on this predicate.
	 */
	protected Predicate<? super P> predicate0;

	/**
	 * A reference to the second decorated predicate. This reference is used to
	 * perform method calls on this predicate.
	 */
	protected Predicate<? super P> predicate1;


	/**
	 * Creates a new binary predicate that decorates the specified predicates.
	 *
	 * @param predicate0 the first predicate to be decorated.
	 * @param predicate1 the second predicate to be decorated.
	 */
	public BinaryPredicate(Predicate<? super P> predicate0, Predicate<? super P> predicate1) {
		this.predicate0 = predicate0;
		this.predicate1 = predicate1;
	}
}
