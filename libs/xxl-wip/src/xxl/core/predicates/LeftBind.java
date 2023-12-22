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
 * This class provides a binary predicate which left (first) argument is bound
 * to a constant object.
 * 
 * <p>For example, consider the predicate {@link Less} that returns true if the
 * first argument is less than the second. By creating a <code>LeftBind</code>
 * instance
 * <code><pre>
 *   Predicate&lt;Integer&gt; p = new LeftBind&lt;Integer&gt;(new Less&lt;Integer&gt;(), 42);
 * </pre></code>
 * <code>p</code> can be evaluated by calling
 * <code><pre>
 *   p.invoke(2);                   //predicate: 42 < 2
 * </pre></code>
 * which corresponds to the call
 * <code><pre>
 *   p.invoke(42, 2);               //predicate: 42 < 2
 * </pre></code>
 *
 * @param <P> the type of the predicate's parameters.
 * @see RightBind
 */
public class LeftBind<P> extends PredicateMetaDataPredicate<P, P> {

	/**
	 * This object is used as constant left (first) object of this predicate's
	 * <code>invoke</code> methods.
	 */
	protected P constArgument;

	/**
	 * Creates a new predicate which binds the left (first) argument of the
	 * specified predicate to the given constant object.
	 *
	 * @param predicate the predicate which left (first) argument should be
	 *        bound.
	 * @param constArgument the constant argument to be used for the left
	 *        (first) argument of the predicate.
	 */
	public LeftBind(Predicate<? super P> predicate, P constArgument) {
		super(predicate);
		this.constArgument = constArgument;
	}

	/**
	 * Creates a new predicate which binds the left (first) argument of the
	 * specified predicate to <code>null</code>.
	 *
	 * @param predicate the predicate which left (first) argument should be
	 *        bound to <code>null</code>.
	 */
	public LeftBind(Predicate<? super P> predicate) {
		this(predicate, null);
	}

	/**
	 * Sets the constant value to which the left (first) argument of the
	 * wrapped predicate should be bound and returns the changed predicate.
	 *
	 * @param constArgument the constant value to which the left (first)
	 *        argument of the wrapped predicate should be bound.
	 * @return the predicate which left (first) argument is bound to the
	 *         specified object.
	 */
	public LeftBind<P> setLeft(P constArgument) {
		this.constArgument = constArgument;
		return this;
	}

	/**
	 * Returns the result of the underlying predicate's <code>invoke</code>
	 * method that is called with the bound left (first) argument. The
	 * implementation of this method is
	 * <code><pre>
	 *   return predicate.invoke(constArgument, arguments.get(1));
	 * </pre></code>
	 *
	 * @param arguments the arguments to the underlying predicate.
	 * @return the result of the underlying predicate's <code>invoke</code>
	 *         method that is called with the bound left (first) argument.
	 */
	@Override
	public boolean invoke(List<? extends P> arguments) {
		return predicate.invoke(constArgument, arguments.get(1));
	}

	/**
	 * Returns the result of the underlying predicate's <code>invoke</code>
	 * method that is called with the bound left (first) argument. The
	 * implementation of this method is
	 * <code><pre>
	 *   return predicate.invoke(constArgument, argument);
	 * </pre></code>
	 *
	 * @param argument the right (second) argument to the underlying predicate.
	 * @return the result of the underlying predicate's <code>invoke</code>
	 *         method that is called with the bound left (first) argument.
	 */
	@Override
	public boolean invoke(P argument) {
		return predicate.invoke(constArgument, argument);
	}

	/**
	 * Returns the result of the underlying predicate's <code>invoke</code>
	 * method that is called with the bound left (first) argument. The
	 * implementation of this method is
	 * <code><pre>
	 *   return predicate.invoke(constArgument, argument1);
	 * </pre></code>
	 *
	 * @param argument0 the left argument to this predicate that is replaced by
	 *        the bound argument.
	 * @param argument1 the right (second) argument to the underlying
	 *        predicate.
	 * @return the result of the underlying predicate's <code>invoke</code>
	 *         method that is called with the bound left (first) argument.
	 */
	@Override
	public boolean invoke(P argument0, P argument1) {
		return predicate.invoke(constArgument, argument1);
	}

	/**
	 * Returns the meta data for this meta data predicate which is set to the
	 * constant left (first) argument of this predicate.
	 *
	 * @return the constant left (first) argument of this predicate.
	 */
	@Override
	public P getMetaData() {
		return constArgument;
	}
}
