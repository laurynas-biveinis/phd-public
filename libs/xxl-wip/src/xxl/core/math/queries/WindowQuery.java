/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.math.queries;

/**
 * Classes implementing this interface are able to consume a <tt>window query</tt>
 * returning the selectivity or any other associated numerical value to the
 * given window. For instance, for a given <tt>window query</tt> the variance of the 
 * values could be regarded.  
 * A <tt>window query</tt> presumes numerical data at least over an interval scale,
 * i.e., data only supporting categorical scales like an ordinal scale or a nominal
 * scale could not be queried with a window query.
 * <br>A {@link xxl.core.math.queries.RangeQuery range query} differs from a  
 * {@link xxl.core.math.queries.WindowQuery window query} by the supported statistical scale.
 * <br>{@link xxl.core.math.queries.RangeQuery Range queries} presume an
 * ordered data space over a categorical scale, i.e., the difference between
 * the range borders is not defined. Moreover, in contrast to a <tt>window query</tt>
 * the right range border not belongs to the query range, i.e., a <tt>range query</tt>
 * could be expressed with <tt>[a,b)</tt> in contrast to a <tt>window query</tt> like <tt>[a,b]</tt>.
 *
 * @see xxl.core.math.queries.RangeQuery
 * @see xxl.core.math.queries.PointQuery
 */

public interface WindowQuery {

	/** Returns the selectivity (true selectivity or an estimation) or any other
	 * associated numerical value to the given window.
	 *
	 * @param a left border(s) of the queried window
	 * @param b right border(s) of the queried window	 
	 * @return a numerical value respectively the selectivity associated with the given query
	 */
	public abstract double windowQuery(Object a, Object b);
}
