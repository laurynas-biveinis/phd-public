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
 * This interface provides the support for the estimation of range queries respectively
 * the association of a numerical value with a <tt>range query<tt>.
 * For instance, for a given <tt>range query</tt> the variance of the 
 * values could be regarded. 
 * <br> 
 * A <tt>range query</tt> is performed on categorical data over an ordinal scale,
 * i.e., a total ordering provided by a {@link java.util.Comparator comparator}
 * is necessary. A <tt>range query</tt> contains all objects within the range
 * <tt>[ l, r)</tt> whereby the left-most object <tt>l</tt> is included and the
 * right-most object <tt>r</tt> is excluded. As an estimation of
 * the <tt>range query<\tt>, a double value is returned.
 *
 * @see xxl.core.math.queries.WindowQuery
 * @see xxl.core.math.queries.PointQuery
 */

public interface RangeQuery {

	/** Performs an estimation of a given <tt>range query</tt>.
	 *
	 * @param a left object of the range to query (inclusively)
	 * @param b right object of the range to query (exclusively)	 
	 * @return a numerical value to be associated with the query
	 */
	public abstract double rangeQuery(Object a, Object b);
}
