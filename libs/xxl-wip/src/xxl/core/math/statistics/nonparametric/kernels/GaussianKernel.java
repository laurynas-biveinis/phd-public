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
import xxl.core.math.functions.RealFunction;

/**
 * This class models the <tt>Gaussian kernel function</tt>. Thus, it extends
 * {@link xxl.core.math.statistics.nonparametric.kernels.KernelFunction KernelFunction}.
 * Since the primitive is known, this class also 
 * implements {@link xxl.core.math.functions.Integrable Integrable}.
 * 
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunction
 * @see xxl.core.math.statistics.nonparametric.kernels.Kernels
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunctionND
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths
 */

public class GaussianKernel extends KernelFunction implements Differentiable {

	/**
	 * Constructs a new GaussianKernel and initializes the parameters.
	 *
	 */
	public GaussianKernel() {
		AVG = 0.0;
		VAR = 1.0;
		R = 0.5 * Math.sqrt(Math.PI);
	}

	/**
	 * Evaluates the Gaussian kernel at x.
	 * 
	 * @param x point to evaluate
	 * @return value of the Gaussian kernel at x
	 */
	public double eval(double x) {
		return Statistics.gaussian(x);
	}

	/** Returns the first derivative of the Gaussian kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}.
	 * For further derivatives see {@link Kernels#normalDerivatives( int, double)}.
	 *
	 * @return first derivative of the Gaussian kernel function
	 */
	public RealFunction derivative() {
		return new RealFunction() {
			public double eval(double x) {
				return Kernels.normalDerivatives(1, x);
			}
		};
	}
}
