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
import xxl.core.math.functions.Integrable;
import xxl.core.math.functions.RealFunction;

/**
 * This class models the <tt>Epanechnikow kernel function</tt>. Thus, it extends
 * {@link xxl.core.math.statistics.nonparametric.kernels.KernelFunction KernelFunction}.
 * Since the primitive is known, this class also 
 * implements {@link xxl.core.math.functions.Integrable Integrable}.
 * 
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunction
 * @see xxl.core.math.statistics.nonparametric.kernels.Kernels
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunctionND
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths
 */

public class EpanechnikowKernel extends KernelFunction implements Integrable {

	/**
	 * Constructs a new EpanechnikowKernel and initializes the parameters.
	 *
	 */
	public EpanechnikowKernel() {
		AVG = 0.0;
		VAR = 0.2; // 1/5
		R = 0.6; // 3/5
	}

	/**
	 * Evaluates the Epanechnikow kernel at x.
	 * 
	 * @param x point to evaluate
	 * @return value of the Epanechnikow kernel at x
	 */
	public double eval(double x) {
		return Statistics.epanechnikow(x);
	}

	/** Returns the primitive of the Epanechnikow kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}.
	 *
	 * @return primitive of the Epanechnikow kernel function
	 */
	public RealFunction primitive() {
		return new RealFunction() {
			public double eval(double x) {
				return Statistics.epanechnikowPrimitive(x);
			}
		};
	}
}
