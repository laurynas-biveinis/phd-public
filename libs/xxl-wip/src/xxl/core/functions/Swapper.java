/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.functions;

import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * A Swapper decorates (wraps) a Function and swaps arguments when calling
 * <code>invoke(argument0, argument1)</code> if demanded by the user or a given
 * predicate.
 * 
 * @param <P> the type of the function's parameters.
 * @param <R> the return type of the function.
 */
public class Swapper<P, R> extends DecoratorFunction<P, R> {

	/**
	 * The predicate determining if a swap is performed.
	 */
	protected Predicate<? super P> swapPredicate;

	/**
	 * Constructs a new Swapper that swap the arguments driven by a given
	 * predicate.
	 *
	 * @param function the function to wrap.
	 * @param swapPredicate the predicate that determines whether to swap or
	 *        not.
	 */
	public Swapper(Function<P, R> function, Predicate<? super P> swapPredicate) {
		super(function);
		this.swapPredicate = swapPredicate;
	}

	/**
	 * Constructs a new Swapper that whether always swaps the arguments or
	 * never.
	 *
	 * @param function the unction to wrap.
	 * @param swap a boolean value indicating whether to swap or not.
	 */
	public Swapper(Function<P, R> function, boolean swap) {
		/* OLD FUNCITONALITY
		this(function);
		this.swap = swap;
		*/
		this(function, swap ? Predicates.TRUE : Predicates.FALSE);
	}

	/**
	 * Constructs a new Swapper that never swaps.
	 *
	 * @param function the function to wrap.
	 */
	public Swapper(Function<P, R> function) {
		/* OLD FUNCTIONALITY
		super(function);
		*/
		this(function, Predicates.FALSE);
	}

	/**
	 * This method provides enhanced functionality to this function by given
	 * the possibility of overruling the predicate controlling the swaps. If
	 * parameter <code>doSwap==true</code> the given arguments will be swapped
	 * otherwise not.
	 *
	 * @param doSwap overrules the internally stored predicate.
	 * @param argument0 the first argument to pass to the wrapped function.
	 * @param argument1 the second argument to pass to the wrapped function.
	 * @return the returned object of the wrapped function.
	 */
	public R invoke(boolean doSwap, P argument0, P argument1) {
		return doSwap ?
			function.invoke(argument1, argument0) :	//return call to decorated function where arguments are swapped
			function.invoke(argument0, argument1);	//return call to decorated function
	}

	/**
	 * This method invokes the wrapped function with the given arguments. The
	 * ordering of the arguments passed to the wrapped function is controlled
	 * by the given predicate. The predicate will be called in the given order
	 * of the arguments:
	 * <code><pre>
	 *   swapPredicate.invoke(argument0, argument1)
	 * </pre></code>
	 *
	 * @param argument0 the first argument to pass to the wrapped function.
	 * @param argument1 the second argument to pass to the wrapped function.
	 * @return the returned object of the wrapped function.
	 */
	@Override
	public R invoke(P argument0, P argument1) {
		return invoke(swapPredicate.invoke(argument0, argument1), argument0, argument1);
	}
}
