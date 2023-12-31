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
 * This class models the <tt>Triweight kernel function</tt>. Thus, it extends
 * {@link xxl.core.math.statistics.nonparametric.kernels.KernelFunction KernelFunction}.
 * Since the primitive is known, this class also 
 * implements {@link xxl.core.math.functions.Integrable Integrable}.
 * 
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunction
 * @see xxl.core.math.statistics.nonparametric.kernels.Kernels
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunctionND
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths
 */

public class TriweightKernel extends KernelFunction implements Integrable, Differentiable {

	/**
	 * Constructs a new TriweightKernel and initializes the parameters.
	 *
	 */
	public TriweightKernel() {
		AVG = 0.0;
		VAR = 0.11111111111111111111111111111111; // 1/9
		R = 0.81585081585081585081585081585082; // 355/429
	}

	/**
	 * Evaluates the Triweight kernel at x.
	 * 
	 * @param x point to evaluate
	 * @return value of the Triweight kernel at x
	 */
	public double eval(double x) {
		return Statistics.triweight(x);
	}

	/** Returns the primitive of the Triweight kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return primitive of the Triweight kernel function
	 */
	public RealFunction primitive() {
		return new RealFunction() {
			public double eval(double x) {
				return Statistics.triweightPrimitive(x);
			}
		};
	}

	/** Returns the first derivative of the Triweight kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return first derivative of the Triweight kernel function
	 */
	public RealFunction derivative() {
		return new RealFunction() {
			public double eval(double x) {
				return Statistics.triweightDerivative(x);
			}
		};
	}
}
