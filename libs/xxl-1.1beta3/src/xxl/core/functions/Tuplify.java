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
 * Maps 2 arguments to a tuple (=Object[2]). If the input-argument is a list of
 * length 2 it is converted to an <code>Object</code> array and returned,
 * otherwise an <code>Exception</code> occurs. The process of tuplifying is
 * performed by the recursive invokation of
 * {@link xxl.core.functions.Function Function} and the <code>toArray</code>
 * method of the parameter list. Calling
 * <code><pre>
 *   Object[] newTuple = Typlify.DEFAULT_INSTANCE.invoke(arg1, arg2);
 * </pre></code>
 * causes a recursive call of the {@link #invoke(List) invoke(List)} method
 * returning the arguments as <code>array</code>.
 *
 */
public class Tuplify extends Function<Object, Object[]> {

	/**
	 * This instance can be used for getting a default instance of Tuplify. It
	 * is similar to the <i>Singleton Design Pattern</i> (for further details
	 * see Creational Patterns, Prototype in <i>Design Patterns: Elements of
	 * Reusable Object-Oriented Software</i> by Erich Gamma, Richard Helm,
	 * Ralph Johnson, and John Vlissides) except that there are no mechanisms
	 * to avoid the creation of other instances of Tuplify.
	 */
	public static final Tuplify DEFAULT_INSTANCE = new Tuplify();

	/**
	 * Tuplifys the given arguments by getting called from the binary
	 * invokation method.
	 * 
	 * <p><b>Note</b>: Do not call this method directly. Use always the binary
	 * function call 
	 * {@link xxl.core.functions.Function#invoke(Object, Object) invoke(arg1, arg2)}
	 * from {@link xxl.core.functions.Function Function} because of the
	 * recursive aspect of Tuplify.
	 * 
	 * @param arguments list with two elements representing the objects to be
	 *        tuplified.
	 * @throws IllegalArgumentException if no argument is given or if too many
	 *         arguments are given. There is no sense in typlifying three or
	 *         more objects.
	 * @return the tuple containing the given arguments.
	 */
	public Object[] invoke(List<? extends Object> arguments) throws IllegalArgumentException{
		if (arguments == null)
			throw new IllegalArgumentException("You can't tuplify null!"); 
		if (arguments.size() != 2)
			throw new IllegalArgumentException("Youn can't typlify " + arguments.size() + " number of objects");
		return arguments.toArray();
	}
}