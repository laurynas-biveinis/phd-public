/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

/** This class wraps a {@link xxl.core.math.functions.RealFunction RealFunction} 
 * implementing {@link xxl.core.math.functions.Integrable Integrable} in order to compute
 * the area 'under' a given interval of the function.
 * Real-valued functions not implementing {@link xxl.core.math.functions.Integrable}
 * could be integrated by applying an algorithm for numerical
 * integration like the {@link xxl.core.math.numerics.integration.TrapezoidalRuleRealFunctionArea Trapezoidal rule}
 * or the {@link xxl.core.math.numerics.integration.SimpsonsRuleRealFunctionArea Simpson's rule}.
 *
 * @see xxl.core.math.functions.RealFunction
 * @see xxl.core.math.numerics.integration.TrapezoidalRuleRealFunctionArea
 * @see xxl.core.math.numerics.integration.SimpsonsRuleRealFunctionArea
 */

public class RealFunctionArea {

	/** real-valued function to integrate */
	protected RealFunction realFunction;

	/** Constructs a new Object of this type.
	 *
	 * @param realFunction {@link xxl.core.math.functions.RealFunction RealFunction} to wrap
	 */
	public RealFunctionArea(RealFunction realFunction) {
		this.realFunction = realFunction;
	}

	/** Evaluates the area "under" a given {@link xxl.core.math.functions.RealFunction RealFunction}
	 * of a given interval and returns the evaluation of \int_a^b f(x) dx.
	 * 
	 * @param a left interval border of the area to compute
	 * @param b right interval border of the area to compute	 
	 * @return area "under" the given real-valued function \int_a^b f(x) dx
	 * @throws IllegalArgumentException invalid parameters
	 */
	public double eval(double a, double b) throws IllegalArgumentException {
		return ((Integrable) realFunction).primitive().eval(b) - ((Integrable) realFunction).primitive().eval(a);
	}
}
