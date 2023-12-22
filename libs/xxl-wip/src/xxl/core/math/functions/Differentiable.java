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
 * differentiable, i.e., the first derivative or an approximation of it 
 * can be computed by calling the method <code>derivative</code>. The derivative
 * is returned as a {@link xxl.core.math.functions.RealFunction real-valued function}.  
 *
 * @see xxl.core.math.functions.RealFunction
 * @see xxl.core.math.functions.RealFunctionArea
 */

public interface Differentiable {

	/** Returns the first derivative of the implementing class as
	 * a {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return first derivative as {@link xxl.core.math.functions.RealFunction}
	 */
	public abstract RealFunction derivative();
}
