/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

import xxl.core.functions.Function;

/**
 * This class provides a wrapper for objects of type
 * {@link xxl.core.functions.Function Function} working with
 * {@link java.lang.Number numerical data} for using them as a
 * {@link xxl.core.math.functions.RealFunction real-valued function}. The
 * wrapped {@link xxl.core.functions.Function Function} has to consume objects
 * of type <code>Double</code> and to return objects of type
 * {@link java.lang.Number Number}.
 *
 * @see xxl.core.math.functions.RealFunction
 * @see xxl.core.functions.Function
 */

public class FunctionRealFunction implements RealFunction {

	/**
	 * The {@link xxl.core.functions.Function Function} to wrap.
	 */
	protected Function<? super Double, ? extends Number> function;

	/**
	 * Constructs a new object of this class.
	 * 
	 * @param function object of type
	 *        {@link xxl.core.functions.Function Function} to wrap.
	 */
	public FunctionRealFunction(Function<? super Double, ? extends Number> function) {
		this.function = function;
	}

	/**
	 * Evaluates the function f at the double value x.
	 * 
	 * @param x function argument.
	 * @return f(x).
	 */
	public double eval(double x) {
		return function.invoke(x).doubleValue();
	}

	/**
	 * Evaluates the function at the float value x.
	 * 
	 * @param x function argument.
	 * @return f(x).
	 */
	public double eval(float x) {
		return function.invoke((double)x).doubleValue();
	}

	/**
	 * Evaluates the function at the int value x.
	 * 
	 * @param x function argument.
	 * @return f(x).
	 */
	public double eval(int x) {
		return function.invoke((double)x).doubleValue();
	}

	/**
	 * Evaluates the function at the long value x.
	 * 
	 * @param x function argument.
	 * @return f(x).
	 */
	public double eval(long x) {
		return function.invoke((double)x).doubleValue();
	}

	/**
	 * Evaluates the function at the byte value x.
	 * 
	 * @param x function argument.
	 * @return f(x).
	 */
	public double eval(byte x) {
		return function.invoke((double)x).doubleValue();
	}
}
