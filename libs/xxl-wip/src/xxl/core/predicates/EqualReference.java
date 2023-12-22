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
 * This class provides a binary predicate that determines whether two given
 * arguments have the same reference. The <code>invoke</code> method of the
 * predicate simply uses the binary equality operator <code>==</code>.
 *
 * @param <P> the type of the predicate's parameters.
 */
public class EqualReference<P> extends AbstractPredicate<P> {

	/**
	 * This instance can be used for getting a default instance of
	 * EqualReference. It is similar to the <i>Singleton Design Pattern</i>
	 * (for further details see Creational Patterns, Prototype in <i>Design
	 * Patterns: Elements of Reusable Object-Oriented Software</i> by Erich
	 * Gamma, Richard Helm, Ralph Johnson, and John Vlissides) except that
	 * there are no mechanisms to avoid the creation of other instances of
	 * EqualReference.
	 */
	public static final EqualReference<Object> DEFAULT_INSTANCE = new EqualReference<Object>();
	
	/**
	 * Returns whether the given arguments have the same reference or not. In
	 * other words, returns <code>true</code> if both arguments are equal using
	 * the binary equality operator <code>==</code>. The exact implementation
	 * is
	 * <code><pre>
	 *     return argument0==argument1;
	 * </pre></code>
	 *
	 * @param argument0 the first argument to the predicate.
	 * @param argument1 the second argument to the predicate.
	 * @return <code>true</code> if the given arguments have the same
	 *         reference, otherwise <code>false</code>.
	 */
	@Override
	public boolean invoke(P argument0, P argument1) {
		return argument0 == argument1;
	}
}
