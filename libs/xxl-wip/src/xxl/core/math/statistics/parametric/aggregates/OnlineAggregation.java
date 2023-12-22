/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.statistics.parametric.aggregates;

/** This class provides an interface for online aggregation functions.
 * Online aggregation functions are dedicated to support an online control of the
 * processed aggregation. Thus, online aggregation functions must at least support
 * monitoring, i.e. the aggregation process can be 'watched' during runtime.
 * Aggregation functions implementing this interface could be watched by {@link #getState()}.
 * Furthermore many aggregation functions depend on a (current) state in their computations. The 
 * state has to changeable during runtime. This could be done by using the {@link #setState(Object)} method.
 * <b>Warning: Using the {@link #setState(Object)} method could harm the correctness of the computed
 * aggregation value.</b>
 *
 * @see xxl.core.cursors.mappers.Aggregator
 */

public interface OnlineAggregation {

	/** Returns the current state of the on-line aggregation function
	 * implementing this interface.
	 * 
	 * @return current state of this function
	 */
	public Object getState();

	/** Sets a new state of the on-line aggregation function
	 * implementing this interface (optional).
	 * 
	 * @param state status to set
	 * @throws UnsupportedOperationException if this method is not supported by this class
	 */
	public void setState(Object state) throws UnsupportedOperationException;
}
