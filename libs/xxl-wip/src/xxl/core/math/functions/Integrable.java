/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

/** 
 * Classes implementing this interface are either analytically or numerically
 * integrable, i.e., the primitive or an approximation of it 
 * can be computed by calling the method <code>primitive</code>. The primitive
 * is returned as a {@link xxl.core.math.functions.RealFunction real-valued function}.
 * 
 * @see xxl.core.math.functions.RealFunction
 * @see xxl.core.math.functions.RealFunctionArea
 * @see xxl.core.math.numerics.integration.TrapezoidalRuleRealFunctionArea
 * @see xxl.core.math.numerics.integration.SimpsonsRuleRealFunctionArea
 */

public interface Integrable {

	/** Returns the primitive of the implementing class as
	 * a {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return primitive as {@link xxl.core.math.functions.RealFunction}
	 */
	public abstract RealFunction primitive();
}
