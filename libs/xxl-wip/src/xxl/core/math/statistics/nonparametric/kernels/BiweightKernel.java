/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.statistics.nonparametric.kernels;

import xxl.core.math.Statistics;
import xxl.core.math.functions.Differentiable;
import xxl.core.math.functions.Integrable;
import xxl.core.math.functions.RealFunction;

/**
 * This class models the <tt>Biweight kernel function</tt>. Thus, it extends
 * {@link xxl.core.math.statistics.nonparametric.kernels.KernelFunction KernelFunction}.
 * Since the primitive as well as the first derivative are known, this class also 
 * implements {@link xxl.core.math.functions.Integrable Integrable} and
 * {@link xxl.core.math.functions.Differentiable Differentiable}.
 *
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunction
 * @see xxl.core.math.statistics.nonparametric.kernels.Kernels
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunctionND
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths
 */

public class BiweightKernel extends KernelFunction implements Integrable, Differentiable {

	/**
	 * Constructs a new BiweightKernel and initializes the parameters.
	 *
	 */
	public BiweightKernel() {
		AVG = 0.0;
		VAR = 0.14285714285714285714285714285714; // 1/7
		R = 0.71428571428571428571428571428571; // 5/7
	}

	/**
	 * Evaluates the Biweight kernel at x.
	 * 
	 * @param x point to evaluate
	 * @return value of the Biweight kernel at x
	 */
	public double eval(double x) {
		return Statistics.biweight(x);
	}

	/** Returns the primitive of the <tt>Biweight</tt> kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return primitive of the <tt>Biweight</tt> kernel function
	 */
	public RealFunction primitive() {
		return new RealFunction() {
			public double eval(double x) {
				return Statistics.biweightPrimitive(x);
			}
		};
	}

	/** Returns the first derivative of the <tt>Biweight</tt> kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return first derivative of the <tt>Biweight</tt> kernel function
	 */
	public RealFunction derivative() {
		return new RealFunction() {
			public double eval(double x) {
				return Statistics.biweightDerivative(x);
			}
		};
	}
}
