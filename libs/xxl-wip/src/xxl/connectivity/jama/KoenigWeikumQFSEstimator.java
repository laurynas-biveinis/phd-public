/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.jama;

import java.util.ArrayList;

import xxl.core.math.queries.QueryFeedbackSelectivityEstimator;
import xxl.core.math.queries.RangeQuery;
import Jama.Matrix;

/**
 * This class provides methods to estimate the selectivity
 * of a simple range query which is represented by an array 
 * of doubles(left side closed, right side open).The domain
 * of the values is divided into a specified number of intervals("buckets")
 * and within each bucket, a linear spline is used to approximate
 * the density of the values.The queries and their true selectivities
 * are recorded and used to adjust the variables of each bucket to improve
 * the estimation for frequently queried intervals.
 * <br>
 * <br>
 * A detailed discussion of the general idea is given in<br>
 * [KW99]: Combining Histograms and Parametric Curve Fitting for Feedback-Driven
 * Query Result-size Estimation. VLDB. 1999.
 *
 * @see xxl.core.math.queries.QueryFeedbackSelectivityEstimator
 * @see xxl.core.math.queries.RangeQuery
 */

public class KoenigWeikumQFSEstimator extends QueryFeedbackSelectivityEstimator implements RangeQuery {

	/** This class provides a skeleton implementation for the partitioning strategies
	 * used in [KW99] to partition (the phase of preprocessing of the algorithm)
	 * the data the histogram is based upon.
	 */
	protected abstract class PartitioningStrategy {

		/** filled with 1's */
		int[] d;

		/** stores the products of frequency and values */
		double[] fv;

		/** stores the values */
		double[] v;

		/** stores the frequencies */
		double[] f;

		/** stores the squared values */
		double[] sqv;

		/** stores the squared frequencies */
		double[] sqf;

		/** stores the values */
		double[] low;

		/** stores the "right" neighbour of the current value */
		double[] high;

		/** Returns a bucket represented by a <tt>double []</tt> of length 8.
		 * Each statistical and descriptive value of each bucket will be internally stored 
		 * in a <tt>double []</tt> for reasons of reducing needed memory, so a mapping back
		 * to each bucket is needed.
		 * 
		 * @param pos number of bucket to return
		 * @return a bucket represented by a <tt>double []</tt> of length 8
		 */
		protected double[] getBucket(int pos) {
			double[] bucket = new double[8];
			bucket[0] = d[pos];
			bucket[1] = fv[pos];
			bucket[2] = v[pos];
			bucket[3] = f[pos];
			bucket[4] = sqv[pos];
			bucket[5] = sqf[pos];
			bucket[6] = low[pos];
			bucket[7] = high[pos];
			return bucket;
		}

		/**
		 * This method merges the first delivered bucket with the second one
		 * by adding the values.
		 * Its presupposed that "left" is the left neighbour of "right".
		 * 
		 * @param left left bucket
		 * @param right right bucket
		 */
		protected void merge(double[] left, double[] right) {
			for (int i = 0; i < 6; i++) {
				left[i] += right[i];
			}
			left[7] = right[7];
		}

		/** This method fills the buckets with the boundaries.
		 * 
		 * @param boundaries boundaries to fill in
		 */
		protected abstract void fill(int[] boundaries);

		//statistical methods--------------------------------- 
		/**
		 *Returns the covariance of the bucket.
		 *
		 * @param b bucket
		 * @return covariance of the bucket
		 */
		protected double getCov(double[] b) {
			return (1 / b[0]) * b[1] - (b[2] / b[0]) * (b[3] / b[0]);
		} //end of getCov

		/**
		* Returns the standard deviation of the values 
		* in the specified bucket.
		* 
		* @param b bucket
		* @return covariance of the bucket
		*/
		protected double getValSD(double[] b) {
			return Math.sqrt((b[4] / b[0]) - (b[2] / b[0]) * (b[2] / b[0]));
		} //end of getValSD

		/**
		* Returns the standard deviation of the frequencies 
		* in the specified bucket.
		* 
		* @param b bucket
		* @return standard deviation of the frequencies in the bucket
		*/
		protected double getFreqSD(double[] b) {
			return Math.sqrt((b[5] / b[0]) - (b[3] / b[0]) * (b[3] / b[0]));
		} //end of getFreqSD  

		/**
		* Returns the correlation between values and frequencies
		* in the specified bucket.
		* 
		* @param b bucket
		* @return correlation between values and frequencies
		* in the specified bucket
		*/
		protected double getCor(double[] b) {
			return (b[0] <= 1) || (getFreqSD(b) == 0) ?
				0 :
				getCov(b) / (getValSD(b) * getFreqSD(b));
		} //end of getCor  

		/**
		* Returns the least square error of the bucket 
		* when the average of frequencies is used as approximation.
		* 
		* @param b bucket
		* @return least square error of the bucket 
		* when the average of frequencies is used as approximation
		*/
		protected double getErr(double[] b) {
			return b[5] - 2 * (b[3] / b[0]) * b[3] + b[0] * (b[3] / b[0]) * (b[3] / b[0]);
		} //end of getErr

		/**
		* Returns the least square error of the bucket 
		* when a linear spline is used as approximation.
		* 
		* @param b bucket
		* @return least square error of the bucket 
		* when a linear spline is used as approximation
		*/
		protected double getError(double[] b) {
			double r = getCor(b);
			double err = getErr(b);
			return (1 - r * r) * err;
		} //end of getError            

		/**
		* Returns the slope of the linear spline 
		* which is used as approximation.
		* 
		* @param b bucket
		* @return slope of the linear spline 
		*/
		protected double getSlope(double[] b) {
			return getValSD(b) == 0 ?
				0 :
				(getCor(b) * getFreqSD(b)) / getValSD(b);
		} //end of getSlope

		/**
		* Returns the point of intersection with
		* the y-axis of the linear spline 
		* which is used as approximation.
		* 
		* @param b bucket
		* @return point of intersection with
		* the y-axis of the linear spline
		*/
		protected double getIntersection(double[] b) {
			return (b[3] / b[0]) - getSlope(b) * (b[2] / b[0]);
		} //end of getIntersection

	} //end of PartitioningStrategy----------------------------------------------------------------------------------------------

	//OptimalMerge--------------------------------------------------------------------------------------------------------------         

	/**
	 * This class represents the optimal partitioning of the
	 * delivered values and initializes the variables of the
	 * surrounding KoenigWeikumSelectivityEstimator-Object .
	 *
	 */
	class OptimalMerge extends PartitioningStrategy {

		/**
		 * Constructs the optimal partitioning of the delivered 
		 * values and initializes the variables of the surrounding
		 * KoenigWeikumSelectivityEstimator-Object .
		 *
		 * @param val values
		 * @param freq frequencies of the values
		 * @param numberOfBuckets number of buckets
		 */
		OptimalMerge(double[] val, int[] freq, int numberOfBuckets) {

			if (numberOfBuckets > val.length)
				throw new RuntimeException("Number of buckets too high");

			d = new int[val.length];
			fv = new double[val.length];
			v = new double[val.length];
			f = new double[val.length];
			sqv = new double[val.length];
			sqf = new double[val.length];
			low = new double[val.length];
			high = new double[val.length];
			//initialization of the arrays
			for (int i = 0; i < val.length - 1; i++) {
				d[i] = 1;
				fv[i] = freq[i] * val[i];
				v[i] = val[i];
				f[i] = freq[i];
				sqv[i] = val[i] * val[i];
				sqf[i] = freq[i] * freq[i];
				low[i] = val[i];
				high[i] = val[i + 1];
			} //end of for

			int n = val.length - 1;
			d[n] = 1;
			fv[n] = freq[n] * val[n];
			v[n] = val[n];
			f[n] = freq[n];
			sqv[n] = val[n] * val[n];
			sqf[n] = freq[n] * freq[n];
			low[n] = val[n];
			high[n] = val[n] + 1;

			/*
			* The value of errors[i][j] stores the sum of the squared
			* deviations of the optimal decomposition of the values
			* v(i+1) to v(n)  in (j+1) intervals.  
			*/
			double[][] errors = new double[val.length][];

			/*
			 * The value positions[i][j] contains the index of the last
			 * value that lies for an optimal decomposition of the values
			 * v(i+1) to v(n) in (j+1) intervals in the same interval
			 * as v(i+1).
			 */
			int[][] positions = new int[val.length][];

			/*
			 * The value boundaries[i] contains the index of the last value
			 * of interval[i].
			 */
			int[] boundaries = new int[numberOfBuckets];

			for (int i = 0; i < val.length; i++) {
				errors[i] = new double[val.length - i];
				positions[i] = new int[val.length - i];
			} //end of for

			double[] temp = getBucket(val.length - 1);
			double[] temp2;
			errors[val.length - 1][0] = getError(temp);
			positions[0][0] = val.length - 1;

			for (int i = 1; i < val.length; i++) {
				positions[i][0] = val.length - 1;
				temp2 = getBucket((val.length - 1) - i);
				merge(temp2, temp);
				errors[(val.length - 1) - i][0] = getError(temp2);
				temp = temp2;
			} //end of for

			for (int j = 1; j < numberOfBuckets; j++) {
				for (int i = 0; i < val.length - j; i++) {
					temp = getBucket(i);
					double error = getError(temp) + errors[i + 1][j - 1];
					int position = i;
					for (int k = i + 1; k < val.length - j; k++) {
						merge(temp, getBucket(k));
						if (getError(temp) + errors[k + 1][j - 1] < error) {
							error = getError(temp) + errors[k + 1][j - 1];
							position = k;
						} //end of if
					} //end of for
					errors[i][j] = error;
					positions[i][j] = position;
				} //end of for
			} //end of for

			int k = 0;
			for (int i = 0; i < numberOfBuckets; i++) {
				boundaries[i] = positions[k][(numberOfBuckets - 1) - i];
				k = (positions[k][(numberOfBuckets - 1) - i]) + 1;
			} //end of for
			fill(boundaries);
		} //end of constructor        

		/**
		 * Initializes the variables of the surrounding KoenigWeikumSelectivityEstimator
		 * Object with the values of the optimal partition.
		 * 
		 * @param boundaries array storing the indices
		 */
		@Override
		protected void fill(int[] boundaries) {
			int first = 0;
			double[] temp;
			for (int i = 0; i < boundaries.length; i++) {
				temp = getBucket(first);
				for (int k = first + 1; k <= boundaries[i]; k++) {
					merge(temp, getBucket(k));
				} //end of for
				a[i] = getIntersection(temp);
				b[i] = getSlope(temp);
				vl[i] = temp[6];
				vh[i] = temp[7];
				q[i] = temp[0];
				first = boundaries[i] + 1;
			} //end of for
		} //end of fill            

	} //end of OptimalMerge-----------------------------------------------------------------------------------

	// GreedyMerge----------------------------------------------------------------------------------------------------
	/**
	 * This class represents a GreedyMerge partitioning of the
	 * delivered  values and initializes the variables of the
	 * surrounding KoenigWeikumSelectivityEstmator-Object.
	 */
	class GreedyMerge extends PartitioningStrategy {
		
		/**
		 * The number of buckets.
		 */
		int nob;

		/**
		 * Constructs a GreedyMerge partitioning of the delivered 
		 * values and initializes the variables of the surrounding
		 * KoenigWeikumSelectivityEstmator-Object.
		 * 
		 * @param val values
		 * @param freq frequencies of the values
		 * @param numberOfBuckets number of buckets
		 */
		GreedyMerge(double[] val, int[] freq, int numberOfBuckets) {

			if (2 * numberOfBuckets > val.length)
				throw new RuntimeException("Number of buckets too high");
			nob = numberOfBuckets;
			int init = val.length / 2 + val.length % 2;

			/*
			 * errors(i) contains the error reduction that results from 
			 * merging bucket(i) with  bucket(succ[i]).
			 */
			double[] errors = new double[init - 1];

			/*
			* succ[i] contains the index of the successor of bucket(i)
			*/
			int[] succ = new int[init - 1];

			/*
			 * pre[i] contains the index of the predeccessor of bucket(i)
			 */
			int[] pre = new int[init];

			d = new int[init];
			fv = new double[init];
			v = new double[init];
			f = new double[init];
			sqv = new double[init];
			sqf = new double[init];
			low = new double[init];
			high = new double[init];
			//initialization of the arrays
			for (int i = 0; i < init - 1; i++) {
				d[i] = 2;
				fv[i] = freq[2 * i] * val[2 * i] + freq[2 * i + 1] * val[2 * i + 1];
				v[i] = val[2 * i] + val[2 * i + 1];
				f[i] = freq[2 * i] + freq[2 * i + 1];
				sqv[i] = val[2 * i] * val[2 * i] + val[2 * i + 1] * val[2 * i + 1];
				sqf[i] = freq[2 * i] * freq[2 * i] + freq[2 * i + 1] * freq[2 * i + 1];
				low[i] = val[2 * i];
				high[i] = val[2 * i + 2];
			} //end of for  

			if (val.length % 2 == 0) {
				int n = init - 1;
				d[n] = 2;
				fv[n] = freq[2 * n] * val[2 * n] + freq[2 * n + 1] * val[2 * n + 1];
				v[n] = val[2 * n] + val[2 * n + 1];
				f[n] = freq[2 * n] + freq[2 * n + 1];
				sqv[n] = val[2 * n] * val[2 * n] + val[2 * n + 1] * val[2 * n + 1];
				sqf[n] = freq[2 * n] * freq[2 * n] + freq[2 * n + 1] * freq[2 * n + 1];
				low[n] = val[2 * n];
				high[n] = val[2 * n + 1] + 1;

			} //end of if
			else {
				int n = init - 1;
				d[n] = 1;
				fv[n] = freq[2 * n] * val[2 * n];
				v[n] = val[2 * n];
				f[n] = freq[2 * n];
				sqv[n] = val[2 * n] * val[2 * n];
				sqf[n] = freq[2 * n] * freq[2 * n];
				low[n] = val[2 * n];
				high[n] = val[2 * n] + 1;
			} //end of else
			//Initialisierung errors,succ,pre
			for (int i = 0; i < init - 1; i++) {
				double[] temp1 = getBucket(i);
				double err1 = getError(temp1);
				double[] temp2 = getBucket(i + 1);
				double err2 = getError(temp2);
				merge(temp1, temp2);
				errors[i] = (err1 + err2) - getError(temp1);
				succ[i] = i + 1;
				pre[i] = i - 1;
			} //end of for
			pre[init - 1] = init - 2;

			int count = init;
			int end = init - 1;
			while (count > numberOfBuckets) {
				int pos = 0;
				int mergePos = 0;

				//computing the "merge position"
				double error = errors[pos];
				while (succ[pos] != end) {

					if (errors[succ[pos]] > error) {
						mergePos = succ[pos];
						error = errors[succ[pos]];
					} //end of if                       

					pos = succ[pos];
				} //end of while

				//merge
				mergeBuckets(mergePos, succ[mergePos]);

				//adjusting succ and pre
				if (succ[mergePos] == end) {
					end = mergePos;
				}
				else {
					succ[mergePos] = succ[succ[mergePos]];
					pre[succ[mergePos]] = mergePos;
				} //end of if

				//adjusting of the errors     
				if (mergePos != end) {
					double[] temp1 = getBucket(mergePos);
					double err1 = getError(temp1);
					double[] temp2 = getBucket(succ[mergePos]);
					double err2 = getError(temp2);
					merge(temp1, temp2);
					errors[mergePos] = (err1 + err2) - getError(temp1);
				} //end of if

				if (pre[mergePos] != -1) {
					double[] temp1 = getBucket(pre[mergePos]);
					double err1 = getError(temp1);
					double[] temp2 = getBucket(mergePos);
					double err2 = getError(temp2);
					merge(temp1, temp2);
					errors[pre[mergePos]] = (err1 + err2) - getError(temp1);
				} //end of if 
				count--;
			} //end of while

			fill(succ);

		} //end of constructor

		/**
		* Adds the values of right to the values of left.
		* 
		* @param left left index
		* @param right right index
		*/
		protected void mergeBuckets(int left, int right) {
			d[left] += d[right];
			fv[left] += fv[right];
			v[left] += v[right];
			f[left] += f[right];
			sqv[left] += sqv[right];
			sqf[left] += sqf[right];
			high[left] = high[right];
		} //end of mergeBucket

		/**
		* Initializes the surrounding KoenigWeikumSelectivityEstimator
		* object with the values of the greedyMerge partition.
		* 
		* @param s array with values
		*/
		@Override
		protected void fill(int[] s) {
			int pos = 0;

			for (int i = 0; i < nob; i++) {
				double[] temp = getBucket(pos);
				a[i] = getIntersection(temp);
				b[i] = getSlope(temp);
				vl[i] = temp[6];
				vh[i] = temp[7];
				q[i] = temp[0];
				try {
					pos = s[pos];
				}
				catch (java.lang.ArrayIndexOutOfBoundsException e) {}
				//}
			} //end of for
		} //end of fill
	} //end of GreedyMerge---------------------------------------------------------------------------------------------------

	//GreedySplit-----------------------------------------------------------------------------------------------------------
	/**
	 * This class represents a GreedySplit partitioning of the
	 * delivered values and initializes the variables of the
	 * surrounding KoenigWeikumSelectivityEstmator-Object.
	 */
	class GreedySplit extends PartitioningStrategy {
		
		/**
		 * The number of buckets.
		 */
		int nob;

		/**
		 * Constructs a GreedySplit partitioning of the delivered 
		 * values and initializes the variables of the surrounding
		 * KoenigWeikumSelectivityEstmator-Object.
		 * 
		 * @param val values
		 * @param freq frequencies of the values
		 * @param numberOfBuckets number of buckets
		 */
		GreedySplit(double[] val, int[] freq, int numberOfBuckets) {

			if (numberOfBuckets > val.length)
				throw new RuntimeException("Number of buckets too high");
			nob = numberOfBuckets;
			int init = val.length;

			/*
			* errors[i][j] contains the sum of the squared deviations of the interval[val[j],val[i]]
			*/
			double[][] errors = new double[init][];

			/*
			* if val[i] is the lower border of the interval,
			* boundaries[i] contains the index of the last value
			* that lies in the interval (not the upper bound of the interval)
			*/
			int[] boundaries = new int[init];
			boundaries[0] = init - 1;

			for (int i = 0; i < errors.length; i++) {
				errors[i] = new double[i + 1];
			} //end of for

			//initialization of the arrays           
			d = new int[init];
			fv = new double[init];
			v = new double[init];
			f = new double[init];
			sqv = new double[init];
			sqf = new double[init];
			low = new double[init];
			high = new double[init];

			d[0] = 1;
			fv[0] = freq[0] * val[0];
			v[0] = val[0];
			f[0] = freq[0];
			sqv[0] = val[0] * val[0];
			sqf[0] = freq[0] * freq[0];
			low[0] = val[0];
			if (val.length == 1) {
				high[0] = val[0] + 1;
			}
			else {
				high[0] = val[1];
			}

			for (int i = 1; i < init - 1; i++) {
				d[i] = d[i - 1] + 1;
				fv[i] = freq[i] * val[i] + fv[i - 1];
				v[i] = val[i] + v[i - 1];
				f[i] = freq[i] + f[i - 1];
				sqv[i] = val[i] * val[i] + sqv[i - 1];
				sqf[i] = freq[i] * freq[i] + sqf[i - 1];
				low[i] = val[0];
				high[i] = val[i + 1];
			} //end of for  
			int n = init - 1;
			d[n] = d[n - 1] + 1;
			fv[n] = freq[n] * val[n] + fv[n - 1];
			v[n] = val[n] + v[n - 1];
			f[n] = freq[n] + f[n - 1];
			sqv[n] = val[n] * val[n] + sqv[n - 1];
			sqf[n] = freq[n] * freq[n] + sqf[n - 1];
			low[n] = val[n];
			high[n] = val[n] + 1;

			//computing the errors     
			for (int i = 0; i < errors.length; i++) {
				errors[i][0] = getError(getBucket(i));
			} //end of for                                                   
			for (int i = 1; i < errors.length; i++) {
				for (int j = i; j < errors.length; j++) {
					errors[j][i] = getError(removeBucket(getBucket(j), getBucket(i - 1)));
				} //end of for
			} //end of for

			//Split    
			for (int k = 2; k <= numberOfBuckets; k++) {
				int splitPos = 0;
				int start = 0;
				int first = 0;
				double error = getError(getBucket(init - 1));
				do {
					if (boundaries[start] != start) {
						error = errors[start][start] + errors[boundaries[start]][start + 1];
						splitPos = start;
						first = start;
						break;
					} //end of if
					if (boundaries[start] != init - 1) {
						start = boundaries[start] + 1;
					} //end of if
				} //end of do
				while (boundaries[start] != init - 1);

				do {
					if (boundaries[start] != start) {
						for (int i = start; i < boundaries[start]; i++) {
							if (errors[i][start] + errors[boundaries[start]][i + 1] < error) {
								error = errors[i][start] + errors[boundaries[start]][i + 1];
								splitPos = i;
								first = start;
							} //end of if
						} //end of for
					} //end of if
					if (boundaries[start] != init - 1) {
						start = boundaries[start] + 1;
					} //end of if
				} //end of do
				while (boundaries[start] != init - 1);

				boundaries[splitPos + 1] = boundaries[first];
				boundaries[first] = splitPos;
			} //end of for
			fill(boundaries);
		} //end of constructor

		/**
		 * Initializes the variables of the surrounding
		 * KoenigWeikumSelectivityEstmator-Object with the 
		 * values of the greedySplit partition.
		 * 
		 * @param boundaries array with indices
		 */
		@Override
		protected void fill(int[] boundaries) {
			int pos = 0;
			double[] temp = getBucket(boundaries[pos]);
			a[0] = getIntersection(temp);
			b[0] = getSlope(temp);
			vl[0] = temp[6];
			vh[0] = temp[7];
			q[0] = temp[0];
			pos = boundaries[pos] + 1;
			for (int i = 1; i < nob; i++) {
				temp = removeBucket(getBucket(boundaries[pos]), getBucket(pos - 1));
				a[i] = getIntersection(temp);
				b[i] = getSlope(temp);
				vl[i] = temp[6];
				vh[i] = temp[7];
				q[i] = temp[0];
				pos = boundaries[pos] + 1;
			} //end of for           
		} //end of fill

		/**
		* Removes bucket b from bucket a by subtraction.
		* 
		* @param a left array
		* @param b right array
		* @return resulting array 
		*/
		protected double[] removeBucket(double[] a, double[] b) {
			for (int i = 0; i < 6; i++) {
				a[i] -= b[i];
			} //end of for
			a[6] = b[7];
			return a;
		} //end of removeBucket

	} //end of GreedySplit------------------------------------------------------------------------------------------

	/**
	* Contains the number of queries to be processed 
	* before adjust() is executed.
	*/
	int numberOfQueries;

	/**
	* Contains point of intersection with the y-axis of the linear spline.
	*/
	public double[] a;

	/**
	 * Contains the slope of the linear spline.
	 */
	public double[] b;

	/**
	 * Contains lower boundaries of the buckets.
	 */
	public double[] vl;

	/**
	* Contains upper boundaries of the buckets.
	*/
	public double[] vh;

	/**
	* Contains the number of basepoints in each bucket.
	*/
	public double[] q;

	/**
	 * Constructs a new object of this class.
	 * The used
	 * PartitioningStrategy is specified by "flag":
	 * 0 = OptimalMerge; 1 = GreedyMerge;2 = GreedySplit.
	 * 
	 * @param val double[] contains the values in ascending order
	 * @param freq double[] freq[i] contains the frequency of value val[i]
	 * @param numberOfBuckets int specifies the number of buckets, the domain is divided in
	 * @param flag int specifies the PartitioningStrategy
	 * @param numberOfQueries int specifies the number of queries to be processed before adjust is executed
	 */
	public KoenigWeikumQFSEstimator(double[] val, int[] freq, int numberOfBuckets, int flag, int numberOfQueries) {
		alreadyProcessedQueries = new ArrayList<Object[]>();
		this.numberOfQueries = numberOfQueries;
		a = new double[numberOfBuckets];
		b = new double[numberOfBuckets];
		vl = new double[numberOfBuckets];
		vh = new double[numberOfBuckets];
		q = new double[numberOfBuckets];

		switch (flag) {
			case 0 :
				new OptimalMerge(val, freq, numberOfBuckets);
				break;

			case 1 :
				new GreedyMerge(val, freq, numberOfBuckets);
				break;

			case 2 :
				new GreedySplit(val, freq, numberOfBuckets);
				break;

			default :
				throw new RuntimeException("Wrong flag!");

		} //end of switch
	} //end of constructor 

	/**
	 * Adds a query and its selectivity.
	 * 
	 * @param query query to store
	 * @param sel selectivity of the query
	 */
	@Override
	protected void addQuery(Object query, double sel) {
		alreadyProcessedQueries.add(new Object[] { query, new Double(sel)});
	}

	/** Performs an estimation of a given <tt>range query</tt>.
	 *
	 * @param a left object of the range to query (inclusively)
	 * @param b right object of the range to query (exclusively)	 
	 * @return a numerical value to be associated with the query
	 */
	public double rangeQuery(Object a, Object b) {
		return getSelectivity(new double[] {((Number) a).doubleValue(), ((Number) b).doubleValue()});
	}

	/**
	 * Returns the estimated selectivity of the specified query.
	 * 
	 * @param query specified query
	 * @return estimated selectivity
	 */
	@Override
	protected double getSelectivity(Object query) {
		// got enough queries to adjust?
		if (numberOfEstimates == numberOfQueries) {
			double[] left = new double[alreadyProcessedQueries.size()];
			double[] right = new double[alreadyProcessedQueries.size()];
			double[] s = new double[alreadyProcessedQueries.size()];
			Object[] temp;
			double[] b;
			double sel;
			for (int i = 0; i < alreadyProcessedQueries.size(); i++) {
				temp = alreadyProcessedQueries.get(i);
				b = (double[]) temp[0];
				sel = ((Double) temp[1]).doubleValue();
				left[i] = b[0];
				right[i] = b[1];
				s[i] = sel;
			} //end of for
			adjust(left, right, s); // adjusting
			numberOfEstimates = 0;
			alreadyProcessedQueries.clear();
		} //end of if
		// if possible estimator has been adjusted			
		// estimate given query

		// determine query data			
		// b[0]: lower border, b[1]: upper border of the range query (not interval query!?!)
		double[] b = (double[]) query;
		int low = 0;
		int high = 0;
		double sel;
		// get upper and lower interval such that the query intersects
		try {
			for (int i = 0; i <= vl.length; i++) {
				if (vl[i] <= b[0] && vh[i] > b[0]) {
					low = i;
					break;
				}
			}
			for (int i = low; i <= vl.length; i++) {
				if (vh[i] >= b[1] && vl[i] < b[1]) {
					high = i;
					break;
				}
			}
		}
		catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.out.println("Wrong query boundaries!");
		}
		// end ?? --> to prove 
		if (low == high) {
			return selectivity(low, b[0], b[1]);
		}
		sel = selectivity(low, b[0], vh[low]) + selectivity(high, vl[high], b[1]);
		for (int i = low + 1; i < high; i++) {
			sel += selectivity(i);
		}
		return sel;
	}

	/**
	* Returns the estimated selectivity of the bucket, specified by its index i.
	* 
	* @param i index of the bucket
	* @return estimated selectivity of the bucket
	*/
	private double selectivity(int i) {
		double sel;
		sel = q[i] * (a[i] + b[i] * vl[i] + b[i] * (vh[i] - vl[i])) * 0.5 - b[i] * (vh[i] - vl[i]) * 0.5;
		return sel;
	}

	/**
	* Returns the estimated selectivity of the interval [l,h).Its presupposed that[l,h)<bucket(i).
	* Its presuposed that[l,h) is subset of bucket(i).
	* 
	* @param i index of the bucket 
	* @param l left border of the interval 
	* @param h right border of the interval
	* @return estimated selectivity of the interval [l,h)
	*/
	private double selectivity(int i, double l, double h) {
		double p = (h - l) / (vh[i] - vl[i]);
		double sel;
		sel =
			q[i] * (p * a[i] + p * b[i] * l + b[i] * p * p * (vh[i] - vl[i]) * 0.5) - b[i] * p * (vh[i] - vl[i]) * 0.5;
		return sel;
	} //end selectivity  

	/** Adjusts the given intervals.
	 * 
	 * @param leftBorders array with left borders
	 * @param rightBorders array with right borders
	 * @param s array with values
	*/
	private void adjust(double[] leftBorders, double[] rightBorders, double[] s) {

		double p = 0.0;
		double l = 0.0;
		double h = 0.0;
		double vlow = 0.0;
		double vhigh = 0.0;
		double a = 0.0;
		double b = 0.0;

		// m rows, n columns
		Matrix gamma = new Matrix(leftBorders.length, this.a.length);
		Matrix delta = new Matrix(leftBorders.length, 1);
		for (int i = 0; i < gamma.getRowDimension(); i++) {
			double[] temp = new double[] { leftBorders[i], rightBorders[i] };
			double d = 0.0;
			for (int j = 0; j < gamma.getColumnDimension(); j++) {
				l = 0;
				h = 0;
				if (temp[0] >= vl[j] && temp[0] < vh[j]) {
					l = temp[0];
				}
				if (temp[0] < vl[j] && temp[1] > vl[j]) {
					l = vl[j];
				}
				if (temp[1] > vl[j] && temp[1] <= vh[j]) {
					h = temp[1];
				}
				if (temp[1] > vh[j] && temp[0] < vh[j]) {
					h = vh[j];
				}

				vhigh = vh[j];
				vlow = vl[j];
				a = this.a[j];
				b = this.b[j];
				p = (h - l) / (vhigh - vlow);
				gamma.set(i, j, p * a + p * b * l + b * ((vhigh - vlow) / 2.0) * p * p);
				d += b * ((vhigh - vlow) / 2.0) * p;
			} //end of for
			delta.set(i, 0, s[i] - d);
		} //end of for

		Matrix pseudoInverse = gamma.inverse();
		Matrix Q = pseudoInverse.times(delta); // fertig !!!!!!!
		// writing back the changes of the q's
		for (int i = 0; i < q.length; i++) {
			q[i] = Q.get(i, 0);
		}
	}
}
