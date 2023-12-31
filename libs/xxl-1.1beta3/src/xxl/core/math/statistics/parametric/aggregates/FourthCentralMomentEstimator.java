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

package xxl.core.math.statistics.parametric.aggregates;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sorters.ShuffleCursor;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.cursors.sources.Inductors;
import xxl.core.functions.Function;
import xxl.core.functions.Functions;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Estimates iteratively the fourth central moment of given {@link java.lang.Number numerical data} without any error control.
 * In contrast to aggregation functions provided by {@link xxl.core.math.statistics.parametric.aggregates.ConfidenceAggregationFunction}
 * objects of this class don't compute confidence intervals for processed data.
 * The fourth central moment itself is computed iteratively, i.e. the data is successively consumed and 
 * the current fourth central moment
 * is computed with the 'old' average and the new data element.
 * <br>
 * <p><b>Objects of this type are recommended for the usage with aggregator cursors!</b></p>
 * <br>
 * Generally, each aggregation function must support a function call of the following type:<br>
 * <tt>agg_n = f (agg_n-1, next)</tt>, <br>
 * where <tt>agg_n</tt> denotes the computed aggregation value after <tt>n</tt> steps,
 * <tt>f</tt> the aggregation function,
 * <tt>agg_n-1</tt> the computed aggregation value after <tt>n-1</tt> steps
 * and <tt>next</tt> the next object to use for computation.
 * An aggregation function delivers only <tt>null</tt> as aggregation result as long as the aggregation
 * function has not yet fully initialized.
 * Objects of this type have a two-step phase for initialization.
 * <br>
 * Also objects of this class are using internally stored information to obtain the standard deviation, 
 * objects of this type don't support on-line features.
 * See {@link xxl.core.math.statistics.parametric.aggregates.OnlineAggregation OnlineAggregation} for further details about 
 * aggregation function using internally stored information supporting <tt>on-line aggregation</tt>.
 *
 * <br>
 * Consider the following example:
 * <code><pre>
 * Aggregator aggregator = new Aggregator(
		new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(100), 50), // input-Cursor
		new FourthCentralMomentEstimator() // aggregate function
	);
 * <\code><\pre>
 * <br>
 * A more detailed coverage of online aggregation is given in [HHW97]: P. Haas, J. Hellerstein, 
 * H. Wang. Online Aggregation. 1997.
 * 
 
 * @see xxl.core.cursors.mappers.Aggregator
 * @see xxl.core.functions.Function
 * @see xxl.core.math.statistics.parametric.aggregates.StandardDeviation
 */

public class FourthCentralMomentEstimator extends AggregationFunction<Number,Number> {

	/** internal average estimator */
	protected Function<Number, Number> avg;

	/** internal third moment estimator */
	protected Function<Number, Number> mom3;

	/** internal variance estimator */
	protected Function<Number, Number> var;

	/** internal counter */
	protected long n;

	/** internally stored value of the third central moment*/
	private double m3;

	/** internally stored value of the fourth central moment*/
	private double m4;

	/** internally stored value of the variance*/
	private double v;

	/** internally stored value of the average*/
	private double a;

	/** internal variable storing the next value*/
	private double xn;

	/** Constructs a new Object of type ForthCentralMoment. */
	public FourthCentralMomentEstimator() {}

	/** Two-figured function call for supporting aggregation by this function.
	 * Each aggregation function must support a function call like <tt>agg_n = f (agg_n-1, next)</tt>,
	 * where <tt>agg_n</tt> denotes the computed aggregation value after <tt>n</tt> steps, <tt>f</tt>
	 * the aggregation function, <tt>agg_n-1</tt> the computed aggregation value after <tt>n-1</tt> steps
	 * and <tt>next</tt> the next object to use for computation.
	 * This method delivers only <tt>null</tt> as aggregation result as long as the aggregation
	 * has not yet initialized.
	 * Objects of this type have a two-step phase for initialization.
	 * 
	 * @param old result of the aggregation function in the previous computation step
	 * @param next next object used for computation
	 * @return aggregation value after n steps
	 */
	public Number invoke(Number old, Number next) {
		if (next == null)
			return old;
		else {
			if (old == null) {
				avg = Functions.aggregateUnaryFunction(new Average());
				a = avg.invoke(next).doubleValue();
				var = Functions.aggregateUnaryFunction(new VarianceEstimator());
				v = var.invoke(next).doubleValue();
				mom3 = Functions.aggregateUnaryFunction(new ThirdCentralMomentEstimator());
				m3 = mom3.invoke(next).doubleValue();
				n = 1;
				m4 = 0.0;
			} else {
				xn = next.doubleValue();
				n++;
				a = avg.invoke(next).doubleValue();
				m4 = (n - 2) * m4 + (4.0 * m3 * (a - xn)) / n;
				m4 += (6.0 * v * Math.pow((a - xn), 2.0)) / ((double) n * (double) n);
				double temp = (Math.pow(n, 2.0) - 3.0 * n + 3.0) / n;
				temp *= (double) (n - 1) / (double) (n);
				temp *= Math.pow((a - xn), 4.0) / (n);
				m4 += temp;
				m4 = m4 / (n - 1);
				v = var.invoke(next).doubleValue();
				m3 = mom3.invoke(next).doubleValue();
			}
			return new Double(m4);
		}
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
		Aggregator aggregator1 = 
			new Aggregator(
				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper(100), 50), // the input-Cursor
				new FourthCentralMomentEstimator() // aggregate function
			);
		System.out.println(
			"The result of the 4th central moment aggregation of 100 randomly distributed integers is: "
				+ aggregator1.last());
		aggregator1.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		Aggregator aggregator2 = 
			new Aggregator(
				xxl.core.cursors.sources.Inductors.naturalNumbers(1, 100), // the input-Cursor
			    new FourthCentralMomentEstimator() // aggregate function
			 );
		System.out.println(
			"\nThe result of the 4th central moment aggregation of the natural numbers from 1 to 100 is: "
				+ aggregator2.last());
		aggregator2.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		Aggregator aggregator3 =
			new Aggregator(
				new ShuffleCursor(Inductors.naturalNumbers(1, 100), // the input-Cursor
				new DiscreteRandomNumber(new JavaDiscreteRandomWrapper())),
				new FourthCentralMomentEstimator() // aggregate function
			);

		System.out.println(
			"\nThe result of the 4th central moment aggregation of the natural numbers (shuffled) from 1 to 100 is: "
				+ aggregator3.last());
		aggregator3.close();
	}
}