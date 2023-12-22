/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

import xxl.core.functions.AbstractFunction;

/** This class provides a wrapper for objects of type {@link xxl.core.math.functions.RealFunction RealFunction}
 * for using them as {@link xxl.core.functions.Function Function}.
 *
 * @see xxl.core.math.functions.RealFunction
 * @see xxl.core.functions.Function
 */

public class RealFunctionFunction extends AbstractFunction {

	/** real-valued function to wrap */
	protected RealFunction realFunction;

	/** Constructs a new wrapper for {@link xxl.core.math.functions.RealFunction real-valued functions} 
	 * for using them as {@link xxl.core.functions.Function Function}.
	 *
	 * @param realFunction {@link xxl.core.math.functions.RealFunction RealFunction} to wrap
	 */
	public RealFunctionFunction(RealFunction realFunction) {
		this.realFunction = realFunction;
	}

	/** Evaluates the {@link xxl.core.math.functions.RealFunction real-valued function} at a given point x.
	 * 
	 * @param o function argument of type {@link java.lang.Number Number} 
	 * @return f(o) as an object of type <tt>Double</tt> 
	 */
	public Object invoke(Object o) {
		return new Double(realFunction.eval(((Number) o).doubleValue()));
	}
}
