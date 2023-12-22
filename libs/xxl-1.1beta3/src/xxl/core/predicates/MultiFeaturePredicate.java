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

package xxl.core.predicates;

import java.util.ArrayList;
import java.util.List;

import xxl.core.functions.Function;

/**
 * This class provides a decorator predicate that applies a fixed number number
 * of mapping functions to the arguments of the underlying predicate. Everytime
 * arguments are passed to an <code>invoke</code> method of this class, the
 * arguments are seperately mapped using a given mapping functions and
 * afterwards passed to the <code>invoke</code> method of the underlying
 * predicate.
 * 
 * @param <P> the type of the predicate's parameters.
 * @param <R> the type of the underlying predicate's parameters (the return
 *        type of the mapping functions).
 */
public class MultiFeaturePredicate<P, R> extends Predicate<P> {

	/**
	 * A reference to the predicate to be decorated. This reference is used to
	 * perform method calls on the underlying predicate.
	 */
	protected Predicate<? super R> predicate;
	/**
	 * 
	 * An array of mapping functions that are applied to the arguments of this
	 * predicate's <code>invoke</code> methods before the <code>invoke</code>
	 * method of the underlying predicate is called.
	 */
	protected Function<? super P, ? extends R>[] mappings;

	/**
	 * Creates a new feature predicate that seperately applies the specified
	 * mapping functions to the arguments of it's <code>invoke</code> methods
	 * before the <code>invoke</code> method of the given predicate is called.
	 *
	 * @param predicate the predicate which input arguments should be mapped.
	 * @param mappings the mapping functions that are applied to the arguments
	 *        of the wrapped predicate.
	 */
	public MultiFeaturePredicate(Predicate<? super R> predicate, Function<? super P, ? extends R>... mappings) {
		this.predicate = predicate;
		this.mappings = mappings;
	}

	/**
	 * Returns the result of the predicate as a primitive boolean value. The
	 * mapped arguments of this method are passed to the underlying predicate's
	 * <code>invoke</code> method.
	 *
	 * @param arguments the arguments to the predicate.
	 * @return the result of the predicate as a primitive boolean value.
	 */
	public boolean invoke(List<? extends P> arguments) {
		List<R> mappedArguments = new ArrayList<R>(arguments.size());
		for (Function<? super P, ? extends R> mapping : mappings)
			mappedArguments.add(mapping.invoke(arguments));
		return predicate.invoke(mappedArguments);
	}

}