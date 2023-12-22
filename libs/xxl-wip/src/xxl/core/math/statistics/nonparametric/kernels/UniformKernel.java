/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.statistics.nonparametric.kernels;

import xxl.core.math.functions.Integrable;
import xxl.core.math.functions.RealFunction;

/**
 * This class models the <tt>Uniform kernel function</tt>. Thus, it extends
 * {@link xxl.core.math.statistics.nonparametric.kernels.KernelFunction KernelFunction}.
 * Since the primitive is known, this class also 
 * implements {@link xxl.core.math.functions.Integrable Integrable}.
 * 
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunction
 * @see xxl.core.math.statistics.nonparametric.kernels.Kernels
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunctionND
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths
 */

public class UniformKernel extends KernelFunction implements Integrable {

	/**
	 * Constructs a new UniformKernel and initializes the parameters.
	 *
	 */
	public UniformKernel() {
		AVG = 0.0;
		VAR = 1.0 / 3.0; // 1/3
		R = 0.5; // 1/2
	}

	/**
	 * Evaluates the Uniform kernel at x.
	 * 
	 * @param x point to evaluate
	 * @return value of the Uniform kernel at x
	 */
	public double eval(double x) {
		return 0.5 * xxl.core.math.Maths.characteristicalFunction(x, -1.0, 1.0);
	}

	/** Returns the primitive of the Uniform kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return primitive of the Uniform kernel function
	 */
	public RealFunction primitive() {
		return new RealFunction() {
			public double eval(double x) {
				return (x <= 1.0) ? (0.5 * x + 0.5) * xxl.core.math.Maths.characteristicalFunction(x, -1.0, 1.0) : 1.0;
			}
		};
	}
}
