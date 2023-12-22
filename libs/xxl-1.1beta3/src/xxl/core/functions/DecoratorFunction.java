/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.functions;

import java.util.List;

/**
 * A decorator-function decorates (wraps) a function by passing given arguments
 * to the invoke method of the wrapped function. To enhance the functionality
 * of a function just inherit from this class and override the method of your
 * choice.
 * 
 * @param <P> the type of the function's parameters.
 * @param <R> the return type of the function.
 */
public class DecoratorFunction<P, R> extends Function<P, R> {

	/**
	 * The function to be decorated.
	 */
	protected Function<P, R> function;

	/**
	 * Constructs a new decorator-function wrapping the given function.
	 * 
	 * @param function the function to be decorated or wrapped.
	 */
	public DecoratorFunction(Function<P, R> function){
		this.function = function;
	}

	/**
	 * Constructs a new decorator-function without a function to decorate. This
	 * constuctor is only used by classes inherited from this class providing
	 * an enhanced functionality.
	 * 
	 * <p><b>Note</b>: Do not use this constructor by calling it directly.</p>
	 */
	public DecoratorFunction() {
		this(null);
	}

	/**
	 * Passes the arguments to the decorated resp. wrapped function by calling
	 * the wrapped function's invoke method.
	 *
	 * @param arguments arguments passed to the wrapped function.
	 * @return the returned object of the wrapped function.
	 */
	public R invoke(List<? extends P> arguments) {
		return function.invoke(arguments);
	}

	/**
	 * Passes the (empty) argument to the decorated resp. wrapped function by
	 * calling the wrapped function's invoke method.
	 *
	 * @return the returned object of the wrapped function.
	 */
	public R invoke() {
		return function.invoke();
	}

	/**
	 * Passes the argument to the decorated resp. wrapped function by calling
	 * the wrapped function's invoke method.
	 *
	 * @param argument argument passed to the wrapped function.
	 * @return the returned object of the wrapped function.
	 */
	public R invoke(P argument) {
		return function.invoke(argument);
	}

	/**
	 * Passes the arguments to the decorated resp. wrapped function by calling
	 * the wrapped function's invoke method.
	 *
	 * @param argument0 first argument passed to the wrapped function.
	 * @param argument1 second argument passewd to the wrapped function.
	 * @return the returned object of the wrapped function.
	 */
	public R invoke(P argument0, P argument1) {
		return function.invoke(argument0, argument1);
	}

	/**
	 * This method declares a new function <code>h</code> by composing a number
	 * of functions <code>f_1,...,f_n</code> with this function <code>g</code>.
	 * 
	 * <p><b>Note</b> that the method does not execute the code of the new
	 * function <code>h</code>. The invocation of the composed function
	 * <code>h</code> is just triggered by a call of its own
	 * <code>invoke-method</code>. Then, the input parameters are passed to
	 * each given function <code>f_1,...,f_n</code>, and the returned objects
	 * are used as the input parameters of the new function <code>h</code>. The
	 * invoke method of <code>h</code> returns the final result.</p>
	 * 
	 * @param <T> the type of the composed function's parameters.
	 * @param functions the functions to be concatenated with this function.
	 * @return the result of the composition.
	 */
	public <T> Function<T, R> compose(Function<? super T, ? extends P>... functions) {
		return function.compose(functions);
	}
	
	/**
	 * Returns the decorated function.
	 * 
	 * @return the decorated function.
	 */
	public Function<? super P, R> getFunction() {
		return function;
	}

}