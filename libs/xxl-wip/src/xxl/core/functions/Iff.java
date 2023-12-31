/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.functions;

import java.util.List;

import xxl.core.predicates.Predicate;

/**
 * A functional If-clause (like the java ?:-operator).
 *
 * @param <P> the type of the function's parameters.
 * @param <R> the return type of the function.
 */
public class Iff<P, R> extends AbstractFunction<P, R> {

	/**
	 * The {@link xxl.core.predicates.Predicate predicate} providing the
	 * functionality of an if-clause.
	 */
	protected Predicate<? super P> predicate;

	/**
	 * The {@link xxl.core.functions.Function function} using in the case of
	 * TRUE.
	 */
	protected Function<? super P, ? extends R> f1;

	/**
	 * The {@link xxl.core.functions.Function function} using in the case of
	 * FALSE.
	 */
	protected Function<? super P, ? extends R> f2;

	/**
	 * With this class one is able to compose a higher order function
	 * containing an if-clause. Constructs a new object of type
	 * {@link xxl.core.functions.Function Function} providing the functionality
	 * of an if-clause. If the given
	 * {@link xxl.core.predicates.Predicate predicate} returns
	 * <code>true</code> for the first argument the
	 * {@link xxl.core.functions.Function function} f1 will be invoked with
	 * this argument, otherwise the
	 * {@link xxl.core.functions.Function function} f2 will be invoked. The
	 * function value will be returned.
	 * 
	 * @param predicate {@link xxl.core.predicates.Predicate predicate}
	 *        providing the functionality of an if-clause. If the
	 *        {@link xxl.core.predicates.Predicate predicate} returns true, the
	 *        if-branch will be executed resp. the first given function
	 *        invoked, otherwise (false was been returned) the else-branch will
	 *        be executed.
	 * @param f1 {@link xxl.core.functions.Function function} invoked in the
	 *        if-branch.
	 * @param f2 {@link xxl.core.functions.Function function} invoked in the
	 *        else-branch.
	 */
	public Iff(Predicate<? super P> predicate, Function<? super P, ? extends R> f1, Function<? super P, ? extends R> f2){
		this.predicate = predicate;
		this.f1 = f1;
		this.f2 = f2;
	}

	/**
	 * Returns the result of the function as an object controlled by the given
	 * {@link xxl.core.predicates.Predicate predicate}. If the
	 * {@link xxl.core.predicates.Predicate predicate} returns
	 * <code>true</code> for the given <code>arguments</code> the
	 * {@link xxl.core.functions.Function function} of the
	 * {@link #f1 if-branch} will be invoked otherwise the
	 * {@link xxl.core.functions.Function function} of the
	 * {@link #f2 else-branch} will be invoked.
	 * 
	 * @param arguments function arguments.
	 * @return the result of the function.
	 */
	@Override
	public R invoke(List<? extends P> arguments) {
		return predicate.invoke(arguments) ? 
			f1.invoke(arguments) :
			f2.invoke(arguments);

		//old implementation:
		/*if (predicate.invoke(arguments))
			return f1.invoke(arguments);
		else
			return f2.invoke(arguments);*/
	}
}
