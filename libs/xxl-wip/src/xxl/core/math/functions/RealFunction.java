/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

/** This interface models a mathematical real-valued, one-dimensional function.
 * Classes implementing this interface need to implement an abstract method {@link #eval(double x)} that consumes
 * and returns double values.
 */

public interface RealFunction {

	/** Evaluates the real-valued function.
	 *
	 * @param x function argument
	 * @return function value
	 */
	public abstract double eval(double x);
}
