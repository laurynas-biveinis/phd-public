/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.functions;

/** 
 * This class provides some RealFunctions realizing different weighting strategies
 * for an adaptive online aggregation. There, in each step two objects are convex-linear combined 
 * depending on a weighting strategy. Different strategies allow a time- or user-dependent emphasis of
 * objects, e.g., later objects are 'more' weighted.    
 *
 * @see RealFunction
 * @see AdaptiveAggregationFunction
 */

public class AdaptiveWeightFunctions {

	/** This class provides an arithmetic weighting, i.e., equal weights in every
	 * step. This is done by returning the inverse value of the given step 1/j.
	 */
	public static class ArithmeticWeights implements RealFunction {
		
	/** Evaluates the real-valued function.
	 *
	 * @param j function argument
	 * @return function value
	 */
		public double eval(double j) {
			return 1.0 / j;
		}
	}

	/**
	 * This class provides a geometric weighting, i.e., in each step the same weight alpha
	 * respectively (1-alpha) is assigned.
	 *
	 */
	public static class GeometricWeights implements RealFunction {
		/**
		 * Parameter for the weighting: <code>alpha</code>.
		 */
		protected double alpha;
		
		/** Constructs an object of this type.
		 * 
		 * @param alpha weighting parameter
		 */
		public GeometricWeights(double alpha) {
			this.alpha = alpha;
		}
		
	/** Evaluates the real-valued function.
	 *
	 * @param j function argument
	 * @return function value
	 */
		public double eval(double j) {
			return 1.0 / alpha;
		}
	}

	/**
	 * This class provides a logarithm weighting. 
	 *
	 */
	public static class LogarithmicWeights implements RealFunction {
		
	/** Evaluates the real-valued function.
	 *
	 * @param j function argument
	 * @return function value
	 */
		public double eval(double j) {
			return Math.log(1.0 + j);
		}
	}

	/**
	 * This class provides a progressive respectively degressive weighting with a parameter alpha.
	 * For alpha in (0,1) the weighting is progressive and for alpha > 1 degressive.
	 *
	 */
	public static class ProgressiveDegressiveWeights implements RealFunction {
		/**
		 * Parameter for the weighting: <code>alpha</code>.
		 */
		protected double alpha;
		
		/** Constructs an object of thia type.
		 * 
		 * @param alpha weighting parameter
		 */
		public ProgressiveDegressiveWeights(double alpha) {
			this.alpha = alpha;
		}
		
	/** Evaluates the real-valued function.
	 *
	 * @param j function argument
	 * @return function value
	 */
		public double eval(double j) {
			return 1.0 / Math.pow(j, alpha);
		}
	}

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private AdaptiveWeightFunctions() {}
}
