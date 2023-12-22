/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.statistics.nonparametric.histograms;


/**
 * This class realizes a histogram based on a logarithm scale.
 * As input data 2-dimensional data is considered. The first
 * dimension is used to map the vector to a certain histogram bucket.
 * The second value represents an aggregate (e.g. sum or average).
 */
public class LogScaleHistogram {

	/** element counter for the buckets */
	private int[] buckets;

	/** sum inside each bucket */
	private double[] sum;

	/** borders of the bucket. border[0]<border[1]< ... < border[bucketCount] */
	private double[] borders;

	/** left - right */
	private double size;

	/** number of buckets */
	private int bucketCount;

	/**
	 * Constructs a log scale histogram with a predefined number of buckets. The interval is [left,right].
	 * logFactor means that interval 0 ( [border[0],border[1]) ) is logFactor smaller than
	 * interval 1 ( [border[1],border[2]) ).
	 * 
	 * @param left left border
	 * @param right right border
	 * @param bucketCount number of buckets
	 * @param logFactor size factor between two consecutive intervals
	 */
	public LogScaleHistogram(double left, double right, int bucketCount, double logFactor) {
		this.bucketCount = bucketCount;
		size = right - left;

		borders = new double[bucketCount + 1];
		borders[0] = left;
		borders[bucketCount] = right;

		double partitions = Math.pow(logFactor, bucketCount - 1);
		double momPart = 1;

		for (int i = 1; i < bucketCount; i++) {
			borders[i] = left + (momPart / partitions) * size;
			momPart *= logFactor;
		}

		buckets = new int[bucketCount];
		sum = new double[bucketCount];
	}

	/**
	 * Inserts a tuple into the histogram, i.e., the according bucket is computed and
	 * the frequency is updated.
	 * 
	 * @param d x-value (decides into which bucket the
	 *	value falls)
	 * @param value y-value
	 */
	public void process(double d, double value) {
		for (int i = 1; i < bucketCount; i++) {
			if (d < borders[i]) {
				buckets[i - 1]++;
				sum[i - 1] += value;
				break;
			}
		}
		if (d > borders[bucketCount - 1]) {
			buckets[bucketCount - 1]++;
			sum[bucketCount - 1] += value;
		}
	}

	/**
	 * Returns the gathered statistic.
	 * 
	 * @return String with the data of the histogram
	 */
	public String toString() {
		StringBuffer b = new StringBuffer("Nr\tleft\tright\tcount\taverage\n");
		for (int i = 1; i <= bucketCount; i++)
			if (buckets[i - 1] > 0)
				b.append(
					i
						+ "\t"
						+ borders[i
						- 1]
						+ "\t"
						+ borders[i]
						+ "\t"
						+ buckets[i
						- 1]
						+ "\t"
						+ sum[i
						- 1] / buckets[i
						- 1]
						+ "\n");
		return b.toString();
	}
}
