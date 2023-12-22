/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.statistics.parametric.aggregates;

import xxl.core.math.functions.AggregationFunction;

/**
 * Computes the variance in a recursive manner
 * using an algorithm based upon [Wes79]:
 * D.H.D. West, Updating mean and StatefulVariance Estimates: An improved Method, 
 * Comm. Assoc. Comput. Mach., 22:532-535, 1979 <BR> and <BR>
 * [CGL83]: Chan, T.F., G.H. Golub, & R.J. LeVeque,
 * Algorithms for Computing the Sample StatefulVariance: Analysis and Recommendations,
 * The American Statistician Vol 37 1983: 242-247. <BR>
 * <br>
 * <p><b>Objects of this type are recommended for the usage with aggregator cursors!</b></p>
 * <br>
 * Each aggregation function must support a function call of the following type:<br>
 * <tt>agg_n = f (agg_n-1, next)</tt>, <br>
 * where <tt>agg_n</tt> denotes the computed aggregation value after <tt>n</tt> steps,
 * <tt>f</tt> the aggregation function,
 * <tt>agg_n-1</tt> the computed aggregation value after <tt>n-1</tt> steps
 * and <tt>next</tt> the next object to use for computation.
 * An aggregation function delivers only <tt>null</tt> as aggregation result as long as the aggregation
 * function has not yet fully initialized.
 * Objects of this type have a two-step phase for initialization.
 * <br>
 * As result of the aggregation, an estimator for the true variance is returned, not the sample variance!
 * Also objects of this class are using internally stored information to obtain the standard deviation, 
 * objects of this type don't support on-line features.
 * See {@link xxl.core.math.statistics.parametric.aggregates.OnlineAggregation OnlineAggregation} for further details about 
 * aggregation function using internally stored information supporting <tt>on-line aggregation</tt>.
 *
 * 
 * @see xxl.core.cursors.mappers.Aggregator
 * @see xxl.core.functions.Function
 * @see xxl.core.math.statistics.parametric.aggregates.StatefulStandardDeviation
 */

public class StatefulVariance extends AggregationFunction<Number,Number> {

	/**
	 * 
	 */
	private double sk;

	/**
	 * variance
	 */
	private double vk;

	/**
	 * number of steps
	 */
	protected long n;

	/** Two-figured function call for supporting aggregation by this function.
	 * Each aggregation function must support a function call like <tt>agg_n = f (agg_n-1, next)</tt>,
	 * where <tt>agg_n</tt> denotes the computed aggregation value after <tt>n</tt> steps, <tt>f</tt>
	 * the aggregation function, <tt>agg_n-1</tt> the computed aggregation value after <tt>n-1</tt> steps
	 * and <tt>next</tt> the next object to use for computation.
	 * This method delivers only <tt>null</tt> as aggregation result as long as the aggregation
	 * has not yet initialized.
	 * Objects of this type have a two-step phase for initialization.
	 * 
	 * @param variance result of the aggregation function in the previous computation step
	 * @param next next number used for computation
	 * @return aggregation value after n steps
	 */
	@Override
	public Number invoke(Number variance, Number next) {
		if (next == null)
			return variance;
		if (variance == null) {
			n = 1;
			sk = next.doubleValue();
			vk = 0;
			return 0d;
		}
		n++;
		vk += (Math.pow((sk - (n - 1) * next.doubleValue()), 2) / n) / (n - 1);
		sk += next.doubleValue();
		return vk / n; // returning the true variance, not the sample variance (an estimator)
	}
}
