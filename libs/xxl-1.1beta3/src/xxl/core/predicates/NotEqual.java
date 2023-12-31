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

/**
 * This class provides a binary predicate that determines whether two given
 * arguments are equal. The <code>invoke</code> method of the predicate is
 * based on the {@link Object#equals(Object) equals} method of the class
 * {@link Object}. In other words, two arguments <code>a</code> and
 * <code>b</code> are not equal when
 * (<code>a!=null && !a.equals(b)==true</code>) is true.
 * 
 * @param <P> the type of the predicate's parameters.
 */
public class NotEqual<P> extends Predicate<P> {

	/**
	 * This instance can be used for getting a default instance of NotEqual. It
	 * is similar to the <i>Singleton Design Pattern</i> (for further details
	 * see Creational Patterns, Prototype in <i>Design Patterns: Elements of
	 * Reusable Object-Oriented Software</i> by Erich Gamma, Richard Helm,
	 * Ralph Johnson, and John Vlissides) except that there are no mechanisms
	 * to avoid the creation of other instances of NotEqual.
	 */
	public static final NotEqual<Object> DEFAULT_INSTANCE = new NotEqual<Object>();
	
	/**
	 * Creates a new NotEqual instance.
	 */
	public NotEqual() {}

	/**
	 * Returns whether the given arguments are equal or not. In other words,
	 * returns <code>true</code> if <code>!argument0.equals(argument1)</code>
	 * returns <code>true</code>. The exact implementation is
	 * <code><pre>
	 *   return !argument0.equals(argument1);
	 * </pre></code>
	 *
	 * @param argument0 the first argument to the predicate.
	 * @param argument1 the second argument to the predicate.
	 * @return <code>true</code> if the given arguments are equal, otherwise
	 *         <code>false</code>.
	 */
	public boolean invoke(P argument0, P argument1) {
		return !argument0.equals( argument1);
	}
}