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
package xxl.core.pipes.processors;

import xxl.core.cursors.AbstractCursor;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.numerics.splines.RB1CubicBezierSpline;

/**
 * Generates a delay manager for a Processor based on a hazard rate function.
 * A hazard rate function determines the event probability, i.e., events per second.
 * 
 * @since 1.1
 */
public class HazardDelayManager extends AbstractCursor<Long> {

	/** 
	 * Creates a Function, which repeats the origin function on the 
	 * interval [0;period).
	 * 
	 * @param origin The origin Function with one period.
	 * @param periodLength The period length
	 * @return function which repeat the origin function on [0;period) 
	 */
	public static RealFunction getPeriodFunction(
		final RealFunction origin,
		final double minValue,
		final double periodLength) {
		return new RealFunction() {
			public double eval(double val) {
				val -= minValue;
				while (val >= periodLength)
					val -= periodLength;
				return origin.eval(val + minValue);
			}
		};
	}

	/**
	 * Get a spline interpolated hazard function with delays for the given grid points.
	 * @param gridPoints timepoints for given delays in milliseconds
	 * @param delayValues delay times in milliseconds
	 * @return Interpolated hazard function
	 */
	public static RealFunction getPeriodSplineFunction(
		double[] gridPoints,
		double[] delayValues) {
		if (gridPoints.length != delayValues.length)
			throw new IllegalArgumentException("Arrays must have same length!!!");
		int len = gridPoints.length;
		double min = gridPoints[0];
		double max = gridPoints[len - 1];
		double[] rateValues = new double[len * 3 - 2];
		double[] grid2 = new double[len * 3 - 2];
		double offset = min - (max - min);
		for (int off = 0; off < 3; off++) {
			for (int i = 0; i < len; i++) {
				grid2[(off * (len - 1)) + i] = offset + (gridPoints[i] - min);
				rateValues[(off * (len - 1)) + i] = delayValues[i];
			}
			offset += (max - min);
		}
		grid2[len * 3 - 3] = offset + max;
		rateValues[len * 3 - 3] = delayValues[len - 1];
		rateValues[len * 2 - 2] = delayValues[len - 1];
		final RB1CubicBezierSpline spline =
			new RB1CubicBezierSpline(grid2, rateValues);
		spline.eval(gridPoints[0]);
		RealFunction fun = new RealFunction() {
			public double eval(double val) {
				return 1.0d / spline.eval(val);
			}
		};
		return getPeriodFunction(
			fun,
			gridPoints[0],
			gridPoints[gridPoints.length - 1] - gridPoints[0]);
	}

	/**
	 * Linear interpolation of the given grid points.
	 * @param gridPoints grid points
	 * @param values values of the grid points
	 * @return interpolating function
	 */
	protected static RealFunction getLinearInterpolationFunction(
		final double[] gridPoints,
		final double[] values) {
		if (gridPoints.length != values.length)
			throw new IllegalArgumentException("Arrays must have same length!!!");
		final int len = gridPoints.length;
		return new RealFunction() {
			public double eval(double val) {
				int k = 0;
				while ((k < len) && (val > gridPoints[k]))
					k++;
				if (k == 0)
					return values[0];
				if (k == len)
					return values[len - 1];
				if (val == gridPoints[k])
					return values[k];
				return values[k
					- 1]
					+ (((values[k] - values[k - 1])
						/ (gridPoints[k] - gridPoints[k - 1]))
						* (val - gridPoints[k - 1]));
			}
		};
	}

	/**
	 * Get a hazard function with delays for the given grid points.
	 * @param gridPoints timepoints for given delays in milliseconds
	 * @param delayValues delay times in milliseconds
	 * @return Interpolating hazard function
	 */
	public static RealFunction getDelayLinearInterpolateFunction(
		final double[] gridPoints,
		final double[] delayValues) {
		final RealFunction fun =
			getLinearInterpolationFunction(gridPoints, delayValues);
		RealFunction fun2 = new RealFunction() {
			public double eval(double val) {
				return 1.0d / fun.eval(val);
			}
		};
		return getPeriodFunction(
			fun2,
			gridPoints[0],
			gridPoints[gridPoints.length - 1] - gridPoints[0]);
	}

	/**
	 * Get a hazard function with rates for the given grid points.
	 * @param gridPoints timepoints for given rates in milliseconds
	 * @param rateValues Rates in Events per millisecond
	 * @return Interpolating hazard function
	 */
	public static RealFunction getRateLinearInterpolateFunction(
		final double[] gridPoints,
		final double[] rateValues) {
		final RealFunction fun =
			getLinearInterpolationFunction(gridPoints, rateValues);
		return getPeriodFunction(
			fun,
			gridPoints[0],
			gridPoints[gridPoints.length - 1] - gridPoints[0]);
	}

	/** The Hazard Function*/
	protected RealFunction function;
	/** The discrete interval*/
	protected double interval;
	/** The offset of the function*/
	protected double offset;

	/** 
	 * Create a DelayManager with this hazard function. The algorithm will
	 * discretize the function in steps of the length 'interval' and will 
	 * start at the point 'offset'.
	 * 
	 * @param function hazard function
	 * @param interval discrete interval in ms
	 * @param offset offset of the function
	 */
	public HazardDelayManager(
		RealFunction function,
		double interval,
		double offset) {
		super();
		this.function = function;
		this.interval = interval;
		this.offset = offset;
	}

	/** 
	 * Computes the next delay.
	 */
	@Override
	public boolean hasNextObject() {
		return true;
	}

	/* (non-Javadoc)
	 * @see xxl.core.cursors.AbstractCursor#nextObject()
	 */
	@Override
	public Long nextObject() {
		double value = offset;
		double rnd = Math.random();
		double sum = 0d;
		while (Math.exp(-sum) > rnd) {
			sum += function.eval(value + interval * 0.5) * interval;
			value += interval;
		}
		sum = value - offset;
		long val = (long) (sum - interval * 0.5 + 0.5);

		offset += val;
		return new Long(val);
	}

	/**
	 * Example of the HazardDelayManager with the 
	 * constant hazard function.
	 * @param args unused
	 */
	public static void main(String[] args) {

		final double rate = 5; //5 events per seceond

		RealFunction func = new RealFunction() {
			public double eval(double val) {
				return rate / 1000d; //1/mean
			}
		};
		HazardDelayManager hdm = new HazardDelayManager(func, 1, 0);
		double sum = 0d;
		int loops = 10000;
		for (int i = 0; i < loops; i++) {
			double val = (hdm.next()).doubleValue();
			sum += val;
			System.out.println(val);
		}
		System.out.println("Count: " + loops + ", Mean: " + (sum / loops));
	}

}
