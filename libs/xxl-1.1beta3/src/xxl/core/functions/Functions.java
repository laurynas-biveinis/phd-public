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

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.math.functions.AggregationFunction;


/**
 * This class contains some useful static methods for manipulating objects of
 * type {@link xxl.core.functions.Function Function}.
 */
public class Functions {

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Functions() {}

	/**
	 * Returns the first function value (function takes m arguments).
	 * 
	 * @param <P> the type of the given function's parameters.
	 * @param <R> the return type of the given function.
	 * @param iterators iterators holding the arguments to the function. The
	 *        number of iterators should correspond to the number of arguments
	 *        of the function.
	 * @param function the function to invoke.
	 * @throws NoSuchElementException if the given
	 *         {@link java.util.Iterator iterators} do not deliver the
	 *         necessary number of objects.
	 * @return the first function value.
	 */
	public static <P, R> R returnFirst(Function<? super P, ? extends R> function, Iterator<? extends P>... iterators) throws NoSuchElementException {
		return returnNth(0, function, iterators);
	}

	//////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the last function value (function takes m arguments).
	 * 
	 * @param <P> the type of the given function's parameters.
	 * @param <R> the return type of the given function.
	 * @param iterators iterators holding the arguments to the function. The
	 *        number of iterators should correspond to the number of arguments
	 *        of the function.
	 * @param function the function to invoke.
	 * @throws NoSuchElementException if the given
	 *         {@link java.util.Iterator iterators} do not deliver the
	 *         necessary number of objects.
	 * @return the last function value.
	 */
	public static <P, R> R returnLast(Function<? super P, ? extends R> function, Iterator<? extends P>... iterators) throws NoSuchElementException {
		return Cursors.last(new Mapper<P, R>(function, iterators));
	}

	//////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the n-th function value (function takes m arguments).
	 * 
	 * @param <P> the type of the given function's parameters.
	 * @param <R> the return type of the given function.
	 * @param n the number of the returned value.
	 * @param iterators iterators holding the arguments to the function. The
	 *        number of iterators should correspond to the number of arguments
	 *        of the function.
	 * @param function the function to invoke.
	 * @throws NoSuchElementException if the given
	 *         {@link java.util.Iterator iterators} do not deliver the
	 *         necessary number of objects.
	 * @return the n-th function value.
	 */
	public static <P, R> R returnNth(int n, Function<? super P, ? extends R> function, Iterator<? extends P>... iterators) throws NoSuchElementException {
		return Cursors.nth(new Mapper<P, R>(function, iterators), n);
	}

	//////////////////////////////////////////////////////////////////////////////////

	/**
	 * Wraps an aggregation function to a unary function by storing the status
	 * of the aggregation internally.
	 * 
	 * @param <P> the type of the function's parameters.
	 * @param <R> the return type of the function.
	 * @param aggregateFunction aggregation function to provide as an unary function
	 * @return an unary function wrapping an aggregation function
	 */
	public static <P, R> Function<P, R> aggregateUnaryFunction(final AggregationFunction<P, R> aggregateFunction) {
		return new Function<P, R>() {
			R agg = null;
			
			public R invoke(P o) {
				agg = aggregateFunction.invoke(agg, o);
				return agg;
			}
		};
	}

	//////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns an one-dimensional real-valued function providing a negative of
	 * a {@link java.lang.Number number}. The result of the mathematical
	 * operation will be returned by a double number!
	 * 
	 * @return a {@link xxl.core.functions.Function Function} performing a
	 *         negation of numerical data.
	 */
	public static Function<Number, Double> minus() {
		return new Function<Number, Double>() {
			public Double invoke(Number number){
				return -number.doubleValue();
			}
		};
	}

	/**
	 * Returns an one-dimensional real-valued function providing a product of
	 * two {@link java.lang.Number numbers}. The result of the mathematical
	 * operation will be returned by a double number!
	 * 
	 * @return a {@link xxl.core.functions.Function Function} performing a
	 *         multiplication of numerical data.
	 */
	public static Function<Number, Double> mult() {
		return new Function<Number, Double>() {
			public Double invoke(Number multiplicand, Number multiplicator){
				return multiplicand.doubleValue() * multiplicator.doubleValue();
			}
		};
	}

	/**
	 * Returns an one-dimensional real-valued function providing a quotient of
	 * two {@link java.lang.Number numbers}. The result of the mathematical
	 * operation will be returned by a double number!
	 * 
	 * @return a {@link xxl.core.functions.Function Function} performing a
	 *         division of numerical data.
	 */
	public static Function<Number, Double> div() {
		return new Function<Number, Double>() {
			public Double invoke(Number dividend, Number divisor){
				return dividend.doubleValue() / divisor.doubleValue();
			}
		};
	}

	/**
	 * Returns an one-dimensional real-valued function providing a sum of two
	 * {@link java.lang.Number numbers}. The result of the mathematical
	 * operation will be returned by a double number!
	 * 
	 * @return a {@link xxl.core.functions.Function Function} performing a
	 *         summation of numerical data.
	 */
	public static Function<Number, Double> add() {
		return new Function<Number, Double>() {
			public Double invoke(Number augend, Number addend){
				return augend.doubleValue() + addend.doubleValue();
			}
		};
	}

	/**
	 * Returns an one-dimensional real-valued function providing a difference
	 * of two {@link java.lang.Number numbers}. The result of the mathematical
	 * operation will be returned by a double number!
	 * 
	 * @return a {@link xxl.core.functions.Function Function} performing a
	 *         subtraction of numerical data.
	 */
	public static Function<Number, Double> sub() {
		return new Function<Number, Double>() {
			public Double invoke(Number minuend, Number subtrahend){
				return minuend.doubleValue() - subtrahend.doubleValue();
			}
		};
	}

	/**
	 * Returns an one-dimensional real-valued function providing an
	 * exponentiation of two {@link java.lang.Number numbers}. The result of
	 * the mathematical operation will be returned by a double number!
	 * 
	 * @return a {@link xxl.core.functions.Function Function} performing an
	 *         exponentiation of numerical data.
	 */
	public static Function<Number, Double> exp() {
		return new Function<Number, Double>() {
			public Double invoke(Number base, Number exponent){
				return Math.pow(base.doubleValue(), exponent.doubleValue());
			}
		};
	}

	/**
	 * Returns a function providing a concatenation of two
	 * {@link java.lang.Object obejct's} string representations. The result of
	 * the operation will be returned by a string!
	 * 
	 * @return a {@link xxl.core.functions.Function Function} performing a
	 *         concatenation of two object's string representation.
	 */
	public static Function<Object, String> concat() {
		return new Function<Object, String>() {
			public String invoke(Object firstObject, Object secondObject){
				return firstObject.toString() + secondObject.toString();
			}
		};
	}

	/**
	 * Returns the hash-value of the given object wrapped to an Integer
	 * instance. Note, this implementation delivers an unary function. Do not
	 * apply to none, two or more parameters.
	 * 
	 * @return the hash-value of the given object.
	 */
	public static Function<Object, Integer> hash() {
		return new Function<Object, Integer>() {
			public Integer invoke(Object o) {
				return o.hashCode();
			}
		};
	}
	
	/**
	 * This method returns a function which is the identity function with the
	 * side effect of sending the object to a PrintStream.
	 * 
	 * @param <T> the type of the objects the returned function is able to
	 *        process.
	 * @param ps PrintStream to which the object is sent.
	 * @return the desired function.
	 */
	public static <T> Function<T, T> printlnMapFunction(final PrintStream ps) {
		return new Println<T>(ps);
	}

	/**
	 * This method returns a function which is the identity function with the
	 * side effect of sending the object to a PrintStream.
	 * 
	 * @param <P> the type of the given function's parameters.
	 * @param <R> the return type of the given function.
	 * @param f Function to be decorated.
	 * @param ps PrintStream to which the object is sent.
	 * @param showArgs showing the arguments which are sent to the Function?
	 *        (yes/no).
	 * @param beforeArgs String which is printed at first (before writing the
	 *        arguments).
	 * @param argDelimiter String which delimits the arguments from each other.
	 * @param beforeResultDelimiter String which is places between the last
	 *        argument (if this is printed) and the rest.
	 * @param afterResultDelimiter String which is printed after the result (at
	 *        the end).
	 * @return the desired function
	 */
	public static <P, R> Function<P, R> printlnDecoratorFunction(final Function<? super P, ? extends R> f, final PrintStream ps, final boolean showArgs, final String beforeArgs, final String argDelimiter, final String beforeResultDelimiter, final String afterResultDelimiter) {
		return new Function<P, R>() {
			public R invoke(List<? extends P> o) {
				ps.print(beforeArgs);
				if (showArgs) {
					for (int i = 0; i < o.size()-1; i++) {
						ps.print(o.get(i));
						ps.print(argDelimiter);
					}
					ps.print(o.get(o.size()-1));
				}
				ps.print(beforeResultDelimiter);

				R ret = f.invoke(o);
				
				ps.print(ret);
				ps.print(afterResultDelimiter);
				return ret;
			}
		};
	}

	/**
	 * This method returns a function which is the identity function with an
	 * additional test. If the objects of subsequent invoke-calls do not adhere
	 * to the given comparator, a runtime exception is sent.
	 * 
	 * @param <T> the type of the objects the returned function is able to
	 *        process.
	 * @param c the comparator.
	 * @param ascending true iff the order is ascending, else false.
	 * @return the desired function.
	 */
	public static <T> Function<T, T> comparatorTestMapFunction(final Comparator<? super T> c, final boolean ascending) {
		return new Function<T, T>() {
			boolean first = true;
			T lastObject;
			int value = ascending?+1:-1;
			
			public T invoke(T o) {
				if (first)
					first = false;
				else if (c.compare(lastObject,o)*value>0)
					throw new RuntimeException("Ordering is not correct");
				lastObject = o;
				return o;
			}
		};
	}
	
	/**
	 * Makes a given function able to handle null values. The defaultValue is
	 * the return value of the function if one of the parameters is a null
	 * value. If no null value is handed over the given function is called.
	 *
	 * @param <P> the type of the functions's parameters.
	 * @param <R> the return type of the given function.
	 * @param function the given function.
	 * @param defaultValue the default value.
	 * @return the function able to handle null values.
	 */
	public static <P, R> Function<P, R> newNullSensitiveFunction(final Function<? super P, ? extends R> function, final R defaultValue) {
		return new Function<P, R>() {
			public R invoke(List<? extends P> arguments) {
				for (P argument : arguments)
					if (argument == null)
						return defaultValue;
				return function.invoke(arguments);
			}
		};
	}
}
