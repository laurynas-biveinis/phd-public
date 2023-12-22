/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.statistics;

import java.util.Iterator;
import java.util.Random;

import xxl.connectivity.colt.ColtContinuousRandomWrapper;
import xxl.connectivity.colt.RandomNumbers;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.identities.TeeCursor;
import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.mappers.ReservoirSampler;
import xxl.core.cursors.sources.ArrayCursor;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.math.functions.AdaptiveWeightFunctions;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.functions.RealFunctions;
import xxl.core.math.statistics.nonparametric.EmpiricalCDF;
import xxl.core.math.statistics.nonparametric.aggregates.Aggregators;
import xxl.core.math.statistics.nonparametric.kernels.EpanechnikowKernel;
import xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths;
import xxl.core.math.statistics.nonparametric.kernels.NativeKernelCDF;
import xxl.core.math.statistics.nonparametric.kernels.ReflectionKernelCDF;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.random.InversionDistributionBasedPRNG;
import xxl.core.util.random.RejectionDistributionBasedPRNG;

/** This class demonstrates the usage of complex statistical estimators in an online aggregation setting.
 * Therefore, we consider <tt>random iid samples</tt> of a random variable (in this case Gaussian, CP1, or CP2).
 * We iterate over samples of increasing size to show the runtime behavior and the convergence of the estimators.
 * <br>
 * <br>
 * Let us consider a sample of size n. We generate 1000 range queries over this sample, whereas it is ensured that
 * the full range is in between the minimum and maximum of the sample values. The ranges of the queries have in turn
 * a fixed size of 2% of the sample range, i.e., 0.02*(maximum - minimum). Then we count the number of elements of the 
 * sample that fulfill the range predicate, i.e. the <tt>selectivity of the range query</tt>. 
 * <br>
 * Now we want to estimate this selectivity with different statistical estimators. Therefore, we assume the selectivity
 * to be the probability that an unknown random variable, represented by the random sample, lies in the query
 * interval. Thus, we estimate P(X in [a,b]) with [a,b] the range of the query.
 * This can be done by integrating a <tt>kernel density estimator</tt> or by evaluating the <tt>empirical cumulative distribution (cdf)</tt>
 * function. Both estimators base on the current sample. Concerning kernel based estimators, there are also the bandwidth
 * strategy and the kernel function to choose. 
 * <br>
 * <br>
 * Picking up the idea of <tt>online aggregation</tt> as proposed in [HHW07]: J. Hellerstein, P.Haas, H. Wang. Online Aggregation.
 * 1997, our framework allows to build the estimators above on-the-fly while consuming the sample successively.
 * This bases on the concept of the {xxl.core.cursors.mappers.Aggregator aggregator}. 
 * An aggregator incrementally computes one or even more aggregates for an input cursor and the current aggregate
 * bases on the last aggregate and the new element.
 * <br>
 * In order to transfer this idea to more complex estimators, we provide two generic techniques: a <tt>memory-based</tt>
 * one and an <tt>adaptive compressed/<tt> one.<br>
 * The memory based technique provides in each step an <tt>iid sample</tt> of the previous seen data with a <tt>reservoir
 * sampling</tt> algorithm. With this sample the current estimator is established.<br>
 * The adaptive compressed technique consumes the data blockwise. For each block, a new estimator is build.
 * Then the new and the old estimator are convex-linear combined. Since this consumes unbounded memory,
 * in each step the new build estimator is compressed with its <tt>cubic Bezier-Spline interpolate</tt>. 
 * With these techniques kernel based estimators and the empirical cdf are build online over the data stream as 
 * a kind of running aggregate.
 * In order to make the resulting estimators comparable, the memory based technique and the adaptive compressed technique
 * consume the same amount of memory, for reasons of simplicity in our case the number of necessary coefficients.
 * 
 * @see xxl.core.math.functions.AdaptiveAggregationFunction
 * @see xxl.core.math.functions.SplineCompressedFunctionAggregateFunction
 * @see xxl.core.math.functions.AdaptiveWeightFunctions
 * @see xxl.core.math.functions.LinearCombination
 * @see xxl.core.math.numerics.splines.CubicBezierSpline
 * @see xxl.core.math.statistics.nonparametric.aggregates.Aggregators 
 * @see xxl.core.math.statistics.nonparametric.EmpiricalCDF
 * @see xxl.core.math.statistics.nonparametric.kernels.KernelBandwidths
 * @see xxl.core.math.statistics.nonparametric.kernels.AbstractKernelCDF
 * @see xxl.core.math.statistics.nonparametric.kernels.AbstractKernelDensityEstimator
 * @see xxl.core.math.statistics.parametric.aggregates.ReservoirSample
 * @see xxl.core.cursors.mappers.Aggregator
 * @see xxl.core.cursors.mappers.ReservoirSampler
 */
public class RangeQuerySelectivityEstimation {

	/** number of Queries */
	static int numberOfQueries = 1000;

	/** right and left borders of the range queries */
	static double[][] queryBorders = new double[numberOfQueries][2];

	/** stores the results of the range queries */
	static double[] rangeQueryResults = new double[numberOfQueries];

	/** size of the range query relative to the range of the random sample */
	static double windowSize = 0.02;

	/** size of the random sample */
	static int sampleSize = 0;

	/** size of the reservoir for the memory based approaches */
	static int reservoirSize = 2000;

	/** block size for the compression */
	static int blockSize = (int) Math.floor(reservoirSize * 0.25);

	/** number of grid points for the compression */
	static int gridSize = (int) Math.floor(reservoirSize * 0.25);

	/** stores the estimations for the range queries for the memory based reflection kernel cdf */
	static double[] rangeQueryResultsMemoryBasedRKCDF = new double[numberOfQueries];

	/** stores the estimations for the range queries for the memory based empirical cdf */
	static double[] rangeQueryResultsMemoryBasedEmpiricalCDF = new double[numberOfQueries];

	/** stores the estimations for the range queries for the memory based native kernel cdf */
	static double[] rangeQueryResultsMemoryBasedNKCDF = new double[numberOfQueries];

	/** stores the estimations for the range queries for the adaptive compressed reflection kernel cdf */
	static double[] rangeQueryResultsAdaptiveCompressedRKCDF = new double[numberOfQueries];

	/** stores the estimations for the range queries for the adaptive compressed native kernel cdf */
	static double[] rangeQueryResultsAdaptiveCompressedNKCDF = new double[numberOfQueries];

	/** stores the estimations for the range queries for the adaptive compressed empirical cdf */
	static double[] rangeQueryResultsAdaptiveCompressedEmpiricalCDF = new double[numberOfQueries];

	/** Delivers a random sample of CP2 data. 
	 * 
	 * @param dim number of elements
	 * @return cursor containing the sample 
	 */
	public static Cursor makeCP2Data(int dim) {
		RealFunction f = RealFunctions.pdfCP02();
		RealFunction g = RealFunctions.pdfCont02();
		RealFunction G_inv = RealFunctions.invDistCont02();
		double c = 7.0 / 3.0;
		int seed1 = 1213;
		ContinuousRandomWrapper u_rnd1 = new ColtContinuousRandomWrapper(seed1);
		int seed2 = 65113;
		ContinuousRandomWrapper u_rnd2 = new ColtContinuousRandomWrapper(seed2);
		ContinuousRandomWrapper g_rnd = new InversionDistributionBasedPRNG(u_rnd1, G_inv);
		RejectionDistributionBasedPRNG rb = new RejectionDistributionBasedPRNG(u_rnd2, f, g, c, g_rnd);
		return new ContinuousRandomNumber(rb, dim);
	}

	/** Delivers a random sample of Gaussian data. 
	 * 
	 * @param dim number of elements in the sample
	 * @return cursor containing the sample 
	 */
	public static Cursor makeGaussData(int dim) {
		Object[] gaussSample = new Object[dim];
		Random random = new Random();
		for (int i = 0; i < dim; i++) {
			gaussSample[i] = new Double(random.nextGaussian());
		}
		return new ArrayCursor(gaussSample);
	}

	/** Initializes the range queries whose center are uniformly distributed over the data range.
	 * 
	 * @param inputMin input minimum 
	 * @param inputMax input maximum
	 */
	public static void makeRangeQueries(double inputMin, double inputMax) {
		double abswindowSize = windowSize * (inputMax - inputMin);
		double center = 0;
		Iterator RandomIterator = RandomNumbers.cuniform(inputMin, inputMax);

		for (int i = 0; i < numberOfQueries; i++) {
			center = ((Double) RandomIterator.next()).doubleValue();
			if ((center + abswindowSize * 0.5) > inputMax) {
				i--;
				continue;
			}
			if ((center - abswindowSize * 0.5) < inputMin) {
				i--;
				continue;
			}
			queryBorders[i][0] = center - abswindowSize * 0.5;
			queryBorders[i][1] = center + abswindowSize * 0.5;
		}
	}

	/** Computes the number of results for a range query over [a,b] and the input,
	 * i.e., the number of elements from the input that are element of [a,b].
	 *  
	 * @param a left border of the range query
	 * @param b right border of the range query
	 * @param input input cursor
	 * @return number of elements fulfilling the range predicate
	 */
	public static double rangeQuery(double a, double b, Cursor input) {
		double result = 0;
		double value = 0;
		input.open();

		while (input.hasNext()) {
			value = ((Double) input.next()).doubleValue();
			if ((value >= a) && (value <= b)) {
				result++;
			}
		}
		input.close();
		return result;
	}

	/** Computes the mean relative error between the real result and the estimated result.
	 * If the relative error is bigger than 1, the absolute error is taken, since otherwise
	 * the total error may be highly influenced by some outliers.
	 * 
	 * @param realResult real results of the range queries
	 * @param estimatedResult estimated results of the range queries
	 * @return mean relative error
	 */
	public static double meanRelativeError(double[] realResult, double[] estimatedResult) {
		double error = 0;
		double val=0;
		for (int i = 0; i < realResult.length; i++) {
			if (realResult[i] != 0) {
				val=realResult[i] - estimatedResult[i];
				if ((Math.abs(val / realResult[i])) < 1) {
					error += Math.abs(val / realResult[i]);
				} else {
					error += Math.abs(val);
				}

			} else
				error += Math.abs(estimatedResult[i]);
		}
		return error / realResult.length;
	}

	/** Starts the use case.
	 * 
	 * @param args list of parameters
	 */
	public static void main(String[] args) {

		// settings of the output
		String results = "";

		for (int k = 2000; k <= 10000; k += 1000) {
			sampleSize = k;

			// initialization of the random sample
			Cursor data = makeCP2Data(sampleSize);
			System.out.println(
				"\n==================================================="
					+ "\n CP2 sample with "
					+ sampleSize
					+ " elements");

			// cloning the cursor since it will be multiply consumed
			TeeCursor teeCursor = new TeeCursor(data);
			while (teeCursor.hasNext()) {
				teeCursor.next();
			}
			Cursor input = teeCursor.cursor();
			input.open();

			// determining minimum and maximum of the random sample
			double inputMax = 0;
			double inputMin = 0;
			double value = 0;
			inputMin = ((Double) (input.peek())).doubleValue();
			while (input.hasNext()) {
				value = ((Double) (input.next())).doubleValue();
				if (value < inputMin)
					inputMin = value;
				if (value > inputMax)
					inputMax = value;
			}
			System.out.println('\t' + "Min " + inputMin + " Max " + inputMax);
			input.close();

			// initialization of the range queries
			System.out.println("\t initializing the range queries ...");
			makeRangeQueries(inputMin, inputMax);
			
			/**********************************************************/
			/*       computation of the estimators                    */
			/**********************************************************/

			System.out.println("\t computing the estimators ...");

			// initialization of the memory based reflection kernel cdf
			input = teeCursor.cursor();
			input.open();
			Aggregator memoryBasedRKCDFAggregator =
				Aggregators.getRKCDFAggregator(
					input,
					new EpanechnikowKernel(),
					reservoirSize,
					ReservoirSampler.XTYPE,
					KernelBandwidths.THUMB_RULE_1D);
			ReflectionKernelCDF memoryBasedRKCDF = (ReflectionKernelCDF) memoryBasedRKCDFAggregator.last();
			input.close();

			// initialization of the memory based native kernel cdf
			input = teeCursor.cursor();
			input.open();
			Aggregator memoryBasedNKCDFAggregator =
				Aggregators.getNKCDFAggregator(
					input,
					new EpanechnikowKernel(),
					reservoirSize,
					ReservoirSampler.XTYPE,
					KernelBandwidths.THUMB_RULE_1D);
			NativeKernelCDF memoryBasedNKCDF = (NativeKernelCDF) memoryBasedNKCDFAggregator.last();
			input.close();

			// initialization of the memory based empirical cdf
			input = teeCursor.cursor();
			input.open();
			Aggregator memoryBasedEmpiricalCDFAggregator =
				Aggregators.getEmpiricalCDFAggregator(input, reservoirSize, ReservoirSampler.XTYPE);
			EmpiricalCDF memoryBasedEmpiricalCDF = (EmpiricalCDF) memoryBasedEmpiricalCDFAggregator.last();
			input.close();

			// initialization of the adaptive compressed reflection kernel cdf
			input = teeCursor.cursor();
			input.open();
			Aggregator adaptiveCompressedRKCDFAggregator =
				Aggregators.getSplineCompressedAdaptiveKernelBasedAggregator(
					ReflectionKernelCDF.FACTORY,
					input,
					new EpanechnikowKernel(),
					KernelBandwidths.THUMB_RULE_1D,
					blockSize,
					new AdaptiveWeightFunctions.ArithmeticWeights(),
					gridSize,
					true,
					true);
			RealFunction adaptiveCompressedRKCDF = (RealFunction) adaptiveCompressedRKCDFAggregator.last();
			input.close();

			// initialization of the adaptive compressed native kernel cdf
			input = teeCursor.cursor();
			input.open();
			Aggregator adaptiveCompressedNKCDFAggregator =
				Aggregators.getSplineCompressedAdaptiveKernelBasedAggregator(
					NativeKernelCDF.FACTORY,
					input,
					new EpanechnikowKernel(),
					KernelBandwidths.THUMB_RULE_1D,
					blockSize,
					new AdaptiveWeightFunctions.ArithmeticWeights(),
					gridSize,
					true,
					true);
			RealFunction adaptiveCompressedNKCDF = (RealFunction) adaptiveCompressedNKCDFAggregator.last();
			input.close();

			// initialization of the adaptive compressed empirical cdf 
			input = teeCursor.cursor();
			input.open();
			Aggregator adaptiveCompressedEmpiricalCDFAggregator =
				Aggregators.getSplineCompressedAdaptiveEmpiricalCDFAggregator(
					input,
					blockSize,
					new AdaptiveWeightFunctions.ArithmeticWeights(),
					gridSize);
			RealFunction adaptiveCompressedEmpiricalCDF =
				(RealFunction) adaptiveCompressedEmpiricalCDFAggregator.last();
			input.close();

			// estimation of the range queries with the separate estimators
			System.out.println("\t estimating the range queries ...");
			for (int i = 0; i < numberOfQueries; i++) {
				rangeQueryResults[i] =
					rangeQuery(queryBorders[i][0], queryBorders[i][1], teeCursor.cursor()) / sampleSize;
				// real results

				rangeQueryResultsMemoryBasedRKCDF[i] =
					memoryBasedRKCDF.eval(queryBorders[i][1]) - memoryBasedRKCDF.eval(queryBorders[i][0]);
				// memory based reflection kernel cdf

				rangeQueryResultsMemoryBasedNKCDF[i] =
					memoryBasedNKCDF.eval(queryBorders[i][1]) - memoryBasedNKCDF.eval(queryBorders[i][0]);
				// memory based native kernel cdf

				rangeQueryResultsMemoryBasedEmpiricalCDF[i] =
					memoryBasedEmpiricalCDF.windowQuery(new Double(queryBorders[i][0]), new Double(queryBorders[i][1]));
				// memory based empirical cdf

				rangeQueryResultsAdaptiveCompressedRKCDF[i] =
					adaptiveCompressedRKCDF.eval(queryBorders[i][1]) - adaptiveCompressedRKCDF.eval(queryBorders[i][0]);
				// adaptive compressed reflection kernel cdf

				rangeQueryResultsAdaptiveCompressedNKCDF[i] =
					adaptiveCompressedNKCDF.eval(queryBorders[i][1]) - adaptiveCompressedNKCDF.eval(queryBorders[i][0]);
				// adaptive compressed native kernel cdf

				rangeQueryResultsAdaptiveCompressedEmpiricalCDF[i] =
					adaptiveCompressedEmpiricalCDF.eval(queryBorders[i][1])
						- adaptiveCompressedEmpiricalCDF.eval(queryBorders[i][0]);
				// adaptive compressed empirical cdf
			}

			// computing the mean relative errors
			System.out.println("\t computing the mean relative errors ...");
			results += sampleSize
				+ "\t"
				+ meanRelativeError(rangeQueryResults, rangeQueryResultsMemoryBasedRKCDF)
				+ "\t"
				+ meanRelativeError(rangeQueryResults, rangeQueryResultsMemoryBasedNKCDF)
				+ "\t"
				+ meanRelativeError(rangeQueryResults, rangeQueryResultsMemoryBasedEmpiricalCDF)
				+ "\t"
				+ meanRelativeError(rangeQueryResults, rangeQueryResultsAdaptiveCompressedRKCDF)
				+ "\t"
				+ meanRelativeError(rangeQueryResults, rangeQueryResultsAdaptiveCompressedNKCDF)
				+ "\t"
				+ meanRelativeError(rangeQueryResults, rangeQueryResultsAdaptiveCompressedEmpiricalCDF)
				+ "\n";

		}

		System.out.println(
			"\n________________________________________________________\n"
				+ "\t Mean relative errors for the different sample sizes\n"
				+"\n Sample size"
				+ "\t MemoryBasedRKCDF"
				+ "\t MemoryBasedNKCDF"
				+ "\t MemoryBasedEmpiricalCDF"
				+ "\t AdaptiveCompressedRKCDF"
				+ "\t AdaptiveCompressedNKCDF"
				+ "\t AdaptiveCompressedEmpiricalCDF\n"
				+ results);
	}
}
