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

package xxl.core.math.statistics.nonparametric;

import java.util.Iterator;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Aggregator;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.functions.Function;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.functions.RealFunction;
import xxl.core.math.statistics.parametric.aggregates.LastN;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/** In the context of online aggregation, running aggregates are built. Given an 
 * iterator of data, an {@link xxl.core.cursors.mappers.Aggregator Aggregator}
 * computes iteratively aggregates. For instance, the current maximum
 * of the already processed data is determined. An internal aggregation function processes
 * the computation of the new element by consuming the old aggregate and the new element
 * from the input cursor.
 * 
 * Generally, each aggregation function must support a function call of the following type:<br>
 * <tt>agg_n = f (agg_n-1, next)</tt>. <br>
 * There, <tt>agg_n</tt> denotes the computed aggregation value after <tt>n</tt> steps,
 * <tt>f</tt> represents the aggregation function,
 * <tt>agg_n-1</tt> the computed aggregation value after <tt>n-1</tt> steps
 * and <tt>next</tt> the next object to use for computation.
 * <br>
 * This class implements an aggregation function that computes empirical estimators. There,
 * the data is processed in blocks of a predefined size. Given such a block of data, an empirical cdf 
 * is established. 
 * <br>
 * Consider the following example that displays a concrete application of an empirical cdf 
 * aggregation function combined with an aggregator:
 * <code><pre>
 	Aggregator aggregator =
			new Aggregator(
				inputCursor(cursor, blockSize), 
				new BlockEmpiricalCDFAggregationFunction()
			);
 * </pre></code>
 * 
 * @see xxl.core.cursors.mappers.Aggregator
 * @see xxl.core.math.functions.AdaptiveAggregationFunction
 * @see xxl.core.math.statistics.nonparametric.EmpiricalCDF
 *
 */
public class BlockEmpiricalCDFAggregationFunction extends AggregationFunction<Object[],EmpiricalCDF> {

	/** factory for empirical cdf's */
	Function factory=EmpiricalCDF.FACTORY;

	/** internal counter to determine how many objects are processed */
	protected int c;

	/** index of the last built cdf */
	protected int last;

	/** indicates whether this instance is initialized */
	protected boolean init;

	/** Delivers the elements of an input iterator blockwise.  
	 * 
	 * @param input input iterator
	 * @param blockSize size of the blocks
	 * @return cursor that delivers the elements blockwise
	 */
	public static Cursor inputCursor(Iterator input, int blockSize) {
		return new Aggregator(
			input,
			new LastN(blockSize));
	}

	/** Two-figured function call for supporting aggregation by this function.
	 * Each aggregation function must support a function call like <tt>agg_n = f (agg_n-1, next)</tt>,
	 * where <tt>agg_n</tt> denotes the computed aggregation value after <tt>n</tt> steps, <tt>f</tt>
	 * the aggregation function, <tt>agg_n-1</tt> the computed aggregation value after <tt>n-1</tt> steps
	 * and <tt>next</tt> the next object to use for computation.
	 * This method delivers only <tt>null</tt> as aggregation result as long as the aggregation
	 * has not yet initialized.
	 * As result of the aggregation a kernel based block estimator, that relies on the current block, is returned.
	 * 
	 * @param old result of the aggregation function in the previous computation step
	 * @param next next object used for computation
	 * @return new kernel based block estimator
	 */
	public EmpiricalCDF invoke(EmpiricalCDF old, Object[] next) { // next = sample
		c++;
		if (next == null)
			return null;		
		Object[] sample = next;
		boolean build = false;
		// indicates whether a new function must be build or not
		// all needed aggregates fully initialized?
		if (sample == null)
			// if the block did not init, this functions also did not init
			return null;
		if (!init) { // building up first function (block != null, but no functions returned so far)
			last = c; // storing time
			build = true; // building up
			init = true;
		} else {
			int blockSize = sample.length;
			if (c >= last + blockSize) { // new block
				last = c; // storing time
				build = true; // building up
			}
		}
		if (build) {
			return (EmpiricalCDF)factory.invoke(sample);
		} else
			return old;
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
		/*                            Example                                */
		/*********************************************************************/
		int blockSize = 100;
		int N = 20000;
		xxl.core.cursors.Cursor data = new ContinuousRandomNumber(new JavaContinuousRandomWrapper(), N);
		data.open();
		Aggregator aggregator =
			new Aggregator<Double[],EmpiricalCDF>(
				inputCursor(data, blockSize), 
				new BlockEmpiricalCDFAggregationFunction()
			);
		RealFunction ecdf=(RealFunction)aggregator.last();
		data.close();
		
		double[] grid = xxl.core.util.DoubleArrays.equiGrid(0.0, 1.0, 100);
		double[] values=xxl.core.math.Statistics.evalRealFunction(grid, ecdf);
		System.out.print(
			"Evaluating empirical cdf based on the last sample block"
			+"\n"+"\n"+"x:"+"\t"+"f(x):"+"\n"
			);
		for(int i=0;i<grid.length;i++) {
			System.out.println(grid[i]+"\t"+values[i]);
		}
	}
}
