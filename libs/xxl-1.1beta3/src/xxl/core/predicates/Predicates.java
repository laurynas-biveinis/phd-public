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

import java.util.Comparator;
import java.util.List;

/**
 * This class provides static methods for the straightforward creation of
 * predicates.
 * 
 * <p>The implemented <code>predicate</code> methods create predicates that can
 * be used for the comparision of objects. The desired predicate can be
 * specified by a binary operator and a comparator. As binary operators the
 * <code>String</code> objects <code>==</code> (or <code>=</code>),
 * <code>!=</code>, <code>&lt;</code>, <code>&lt;=</code>, <code>&gt;</code>
 * and <code>&gt;=</code> can be used. When a comparator is specified, it will
 * be used for comparing the given objects.</p>
 *
 * @see Equal
 * @see Greater
 * @see GreaterEqual
 * @see Less
 * @see LessEqual
 * @see NotEqual
 */
public class Predicates {

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Predicates() {}

	/**
	 * Predicate that always returns true.
	 */
	public static Predicate<Object> TRUE = new Predicate<Object>() {

		public boolean invoke() {
			return true;
		}

		public boolean invoke(Object o ) {
			return true;
		}

		public boolean invoke(Object o1, Object o2) {
			return true;
		}

		public boolean invoke(List<? extends Object> o) {
			return true;
		}
	};

	/**
	 * Predicate that always returns false.
	 */
	public static Predicate<Object> FALSE = new Predicate<Object>() {

		public boolean invoke() {
			return false;
		}

		public boolean invoke(Object o ) {
			return false;
		}

		public boolean invoke(Object o1, Object o2) {
			return false;
		}

		public boolean invoke(List<? extends Object> o) {
			return false;
		}
	};
	
	/**
	 * Predicate, which decides if an integer is even.
	 */
	public static Predicate<Integer> EVEN = new Predicate<Integer>() {
		public boolean invoke(Integer i) {
			return i%2 == 0;
		}			
	};

	/**
	 * Returns a binary predicate that realizes the given binary operator. The
	 * factory method accepts the following binary operators and returns the
	 * following predicates:
	 *
	 * <center>
	 *     <table border rules=groups cellpadding=6>
	 *         <thead>
	 *             <tr>
	 *                 <th>operator</th>
	 *                 <th>predicate</th>
	 *             </tr>
	 *         </thead>
	 *         <tbody>
	 *             <tr>
	 *                 <td><code>==</code> or <code>=</code></td>
	 *                 <td>{@link Equal}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>!=</code></td>
	 *                 <td>{@link NotEqual}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&lt;</code></td>
	 *                 <td>{@link Less}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&lt;=</code></td>
	 *                 <td>{@link LessEqual}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&gt;</code></td>
	 *                 <td>{@link Greater}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&gt;=</code></td>
	 *                 <td>{@link GreaterEqual}</td>
	 *             </tr>
	 *         </tbody>
	 *     </table>
	 * </center>
	 *
	 * @param op the operator that should be implemented by the predicate
	 *        (<code>==</code> or <code>=</code>, <code>!=</code>,
	 *        <code>&lt;</code>, <code>&lt;=</code>, <code>&gt;</code>,
	 *        <code>&gt;=</code>).
	 * @return a binary predicate that implements the specified operator.
	 * @throws IllegalArgumentException if an invalid operator is specified.
	 */
	public static Predicate<Object> predicate(String op) {
		if (op.equals("==") || op.equals("="))
			return Equal.DEFAULT_INSTANCE;
		if (op.equals("!="))
			return NotEqual.DEFAULT_INSTANCE;
		if (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">="))
			throw new IllegalArgumentException("cannot create a predicate for operator [" + op + "] without a given comparator");

		throw new IllegalArgumentException("invalid operator [" + op + "]");
	}

	/**
	 * Returns a binary predicate that realizes the given binary operator. The
	 * specified comparator is used for object comparision. The factory method
	 * accepts the following binary operators and returns the following
	 * predicates:
	 *
	 * <center>
	 *     <table border rules=groups cellpadding=6>
	 *         <thead>
	 *             <tr>
	 *                 <th>operator</th>
	 *                 <th>predicate</th>
	 *             </tr>
	 *         </thead>
	 *         <tbody>
	 *             <tr>
	 *                 <td><code>==</code> or <code>=</code></td>
	 *                 <td>{@link Equal}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>!=</code></td>
	 *                 <td>{@link NotEqual}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&lt;</code></td>
	 *                 <td>{@link Less}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&lt;=</code></td>
	 *                 <td>{@link LessEqual}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&gt;</code></td>
	 *                 <td>{@link Greater}</td>
	 *             </tr>
	 *             <tr>
	 *                 <td><code>&gt;=</code></td>
	 *                 <td>{@link GreaterEqual}</td>
	 *             </tr>
	 *         </tbody>
	 *     </table>
	 * </center>
	 *
	 * @param <P> the type of the predicate's parameters.
	 * @param op the operator that should be implemented by the predicate
	 *        (<code>==</code> or <code>=</code>, <code>!=</code>,
	 *        <code>&lt;</code>, <code>&lt;=</code>, <code>&gt;</code>,
	 *        <code>&gt;=</code>).
	 * @param comparator the comparator that should be used for object
	 *        comparision.
	 * @return a binary predicate that implements the specified operator.
	 * @throws IllegalArgumentException if an invalid operator is specified.
	 */
	public static <P> Predicate<P> predicate(String op, Comparator<? super P> comparator) {
		if (op.equals("==") || op.equals("="))
			return new ComparatorBasedEqual<P>(comparator);
		if (op.equals("!="))
			return new Not<P>(new ComparatorBasedEqual<P>(comparator));
		if (op.equals("<"))
			return new Less<P>(comparator);
		if (op.equals("<="))
			return new LessEqual<P>(comparator);
		if (op.equals(">"))
			return new Greater<P>(comparator);
		if (op.equals(">="))
			return new GreaterEqual<P>(comparator);

		throw new IllegalArgumentException("invalid operator [" + op + "]");
	}

	/**
	 * Returns a predicate able to handle null values. The given flag controls
	 * whether two passed null values should be equal or not. Notice the
	 * difference in semantics when evaluating join predicates using the two
	 * different kinds of Equal-predicates returned by this method.
	 *
	 * @param flag controls whether two passed null values should be equal or
	 *        not.<br />
	 *        if true - null == null<br />
	 *        if false - null != null
	 * @return the predicate able to handle null values.
	 */
	public static Predicate<Object> newNullSensitiveEqual(boolean flag) {
		return flag ?
			new Predicate<Object>() {
				public boolean invoke(Object o1, Object o2) {
					return o1 == null ?
						o2 == null :
						o1.equals(o2);
				}
			} :
			new Predicate<Object>() {
				public boolean invoke(Object o1, Object o2) {
					return o1 == null ?
						false :
						o1.equals(o2);
				}
			};
	}

	/**
	 * Makes a given predicate able to handle null values. The defaultValue is
	 * the return value of the predicate if one of the parameters is a null
	 * value. If no null value is handed over the given predicate is called.
	 *
	 * @param <P> the type of the predicate's parameters.
	 * @param predicate the given predicate.
	 * @param defaultValue the default value.
	 * @return the predicate able to handle null values.
	 */
	public static <P> Predicate<P> newNullSensitivePredicate(final Predicate<? super P> predicate, final boolean defaultValue) {
		return new Predicate<P>() {
			public boolean invoke(List<? extends P> arguments){
				for (P argument : arguments)
					if (argument == null)
						return defaultValue;
				return predicate.invoke(arguments);
			}
		};
	}
	
	/**
	 * Returns a new predicate, which has the same invoke function with the
	 * given predicate, but the order of the arguments is swapped.
	 * 
	 * @param <P> the type of the predicate's parameters.
	 * @param predicate the given predicate.
	 * @return the new predicate.
	 */
	public static <P> Predicate<P> swapArguments(final Predicate<? super P> predicate) {
		return new Predicate<P>() {
			public boolean invoke(P o1, P o2) {
				return predicate.invoke(o2, o1);
			}
		};
	}
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		// example to newNullSensitveEqual(boolean)
		Predicate<Object> p1 = newNullSensitiveEqual(true);
		Object[] oa1 = new Object[] {"a1", "a2", "A3", null};
		Object[] oa2 = new Object[] {"a3", "A2", "a1", null};

		for (Object o1 : oa1)
			for (Object o2 : oa2)
				System.out.println("equal(" + o1 +", " + o2 + ")=" + p1.invoke(o1, o2));
	}
}
