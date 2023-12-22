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

import java.util.ArrayList;
import java.util.List;

/**
 * A DecoratorArrayFunction invokes each function given to the constructor on
 * the given parameters when calling the invoke method. Meaning this class
 * defines a function from an one dimensional object space to a n-dimensonal
 * object space
 * 
 * <p>
 * <tt>f:O -->O^n</tt>&ensp;with&ensp;<tt>f=(f1(a),f2(a),...,fn(a))</tt>
 * </p>
 * 
 * <p>for given functions&ensp;<tt>f1,...,fn</tt>&ensp;with&ensp;
 * <tt>fi:O-->O, i=1,...,n</tt>&ensp; by composing them in a vector-like
 * manner.</p>
 * 
 * @param <P> the type of the decorator list-function's parameters.
 * @param <R> the most general return type of the component type of the list
 *        returned by the decorator list-function's invoke method.
 */
public class DecoratorArrayFunction<P, R> extends Function<P, List<R>> {

	/**
	 * The functions are separately invoked on the given parameters when
	 * calling the invoke method.
	 */
	protected Function<? super P, ? extends R>[] functions;

	/**
	 * Constructs a new decorator array-function. The function i defined by
	 * <pre>
	 *   f : P --> R^n
	 * </pre>
	 * with
	 * <pre>
	 *   f = ( f1(a), f2(a), ... , fn(a) ) , fi : P --> R
	 * </pre>
	 * 
	 * @param functions the functions are separately invoked on the given
	 *        parameters when calling the invoke method.
	 */
	public DecoratorArrayFunction(Function<? super P, ? extends R>... functions) {
		this.functions = functions;
	}

	/**
	 * Calls the invoke methods of the wrapped functions on the given
	 * parameters. The results of this invokations are gathered in an array
	 * that is returned as the result of the invoke method.
	 * 
	 * @param arguments arguments passed to the given functions.
	 * @return an <code>List&lt;R&gt;</code> containing the results of the
	 *         invoked functions
	 */
	public List<R> invoke(List<? extends P> arguments) {
		List<R> result = new ArrayList<R>(functions.length);
		for (Function<? super P, ? extends R> function : functions)
			result.add(function.invoke(arguments));
		return result;
	}
}