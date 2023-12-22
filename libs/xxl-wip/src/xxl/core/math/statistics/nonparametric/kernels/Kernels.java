/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.statistics.nonparametric.kernels;

/**
 * This class provides some static methods for computing respectively processing 
 * kernel estimators and the corresponding bandwidths, e.g., estimating 
 * the roughness of function.
 *
 * @see xxl.core.math.statistics.nonparametric.kernels.AbstractKernelDensityEstimator
 * @see xxl.core.math.statistics.nonparametric.kernels.AbstractKernelCDF
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelFunction
 *
 */

public class Kernels {

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Kernels() {}

	/** Returns an estimation of the roughness of a function f using a kernel estimator.
	 * The roughness R_r(f) of degree r is defined by R_r(f) = \int f^r(x) dx.
	 *
	 * @param g used bandwidth
	 * @param sample used sample to compute the estimation. Given as Objects of type <TT>Number</TT>.
	 * @param r degree of the derivative
	 * @return an estimation of the roughness of a function f
	 */
	public static double roughnessEstimator(double g, Object[] sample, int r) {
		double xi = 0.0;
		double xj = 0.0;
		int size = sample.length;
		double re = 0.0;
		for (int i = 0; i < size; i++) {
			xi = ((Number) sample[i]).doubleValue();
			for (int j = 0; j < size; j++) {
				xj = ((Number) sample[j]).doubleValue();
				re += normalDerivatives(r, (xi - xj) / g);
			}
		}
		return re / g / (size * size);
	}

	/** Returns the r-th derivative of the Gaussian kernel evaluated at 0.
	 *
	 * @param r degree of the derivative
	 * @return value of the r-th derivative of the Gaussian kernel at 0
	 */
	public static double normalDerivativeAt0(int r) {
		double re = Math.pow(-1.0, (r / 2.0));
		re = re / Math.sqrt(2.0 * Math.PI);
		re = re * xxl.core.math.Maths.oddFactorial(r);
		return xxl.core.math.Maths.isEven(r) ? re : 0.0;
	}

	/** Computes the r-th derivative of the Gaussian function at x.
	 * 
	 * @param r degree of the derivative
	 * @param x function argument
	 * @return value of the r-th derivative of the pdf of the N(0,1) distribution at x
	 */
	public static double normalDerivatives(int r, double x) {
		return Math.pow(-1.0, r)
			* xxl.core.math.Maths.hermitePolynomial(r, x)
			* xxl.core.math.Statistics.gaussian(x);
	}

	/** Computes an estimation of the r-th derivative of f at x using the r-th derivative or the Gaussian
	 * kernel, a given bandwidth g and a random sample.
	 *
	 * @param x where to evaluate the estimation
	 * @param sample used sample to compute the estimation. Given as Objects of type <TT>Number</TT>.
	 * @param degree degree of the derivative
	 * @param g used bandwidth
	 *
	 * @return an estimation f^(r)_g (x) of f^(r)(x)
	 */
	public static double kernelDerivativeEstimator(double x, Object[] sample, int degree, double g) {
		double xi = 0.0;
		int size = sample.length;
		double r = 0.0;
		for (int i = 0; i < size; i++) {
			xi = ((Number) sample[i]).doubleValue();
			r = r + normalDerivatives(degree, (x - xi) / g);
		}
		r = r / size;
		r = r / Math.pow(g, degree);
		return r;
	}
}
