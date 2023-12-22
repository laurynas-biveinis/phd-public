/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/

package xxl.core.math.statistics.nonparametric.kernels;

import xxl.core.math.Statistics;
import xxl.core.math.functions.Differentiable;
import xxl.core.math.functions.Integrable;
import xxl.core.math.functions.RealFunction;

/**
 * This class models the <tt>CosineArch kernel function</tt>. Thus, it extends
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

public class CosineArchKernel extends KernelFunction implements Integrable, Differentiable {

	/**
	 * Constructs a new CosineArchKernel and initializes the parameters.
	 *
	 */
	public CosineArchKernel() {
		AVG = 0.0;
		VAR = 1.0 - (8.0 / (Math.PI * Math.PI)); // 1 - ( 8 / pi^2)
		R = Math.PI * Math.PI / 16.0; // pi^2 / 16
	}

	/**
	 * Evaluates the CosineArch kernel at x.
	 * 
	 * @param x point to evaluate
	 * @return value of the CosineArch kernel at x
	 */
	public double eval(double x) {
		return Statistics.cosineArch(x);
	}

	/** Returns the primitive of the <tt>CosineArch</tt> kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}
	 *
	 * @return primitive of the <tt>CosineArch</tt> kernel function
	 */
	public RealFunction primitive() {
		return new RealFunction() {
			public double eval(double x) {
				return Statistics.cosineArchPrimitive(x);
			}
		};
	}

	/** Returns the first derivative of the <tt>CosineArch</tt> kernel function
	 * as {@link xxl.core.math.functions.RealFunction real-valued function}
	 *
	 * @return first derivative of the <tt>CosineArch</tt> kernel function
	 */
	public RealFunction derivative() {
		return new RealFunction() {
			public double eval(double x) {
				return Statistics.cosineArchDerivative(x);
			}
		};
	}

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		KernelFunction k = new CosineArchKernel();
		xxl.core.math.functions.RealFunction intk = ((xxl.core.math.functions.Integrable) k).primitive();
		xxl.core.math.functions.RealFunction devk = ((xxl.core.math.functions.Differentiable) k).derivative();

		int steps = 100;
		double min = -1.0;
		double max = 1.0;
		double x = 0.0;
		System.out.println("# x \t cos(x) \t cos'(x) \t int(cos(x))dx");
		for (int i = 0; i <= steps; i++) {
			x = min + (max - min) * i / steps;
			System.out.println(x + "\t" + k.eval(x) + "\t" + devk.eval(x) + "\t" + intk.eval(x));
		}
	}
}