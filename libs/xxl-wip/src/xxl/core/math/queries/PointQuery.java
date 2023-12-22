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
 * This interface provides the support for the estimation of point queries respectively
 * the association of a numerical value with a <tt>point query<tt>.
 * A <tt>point query</tt> is performed on categorical data over a nominal scale,
 * i.e., additional information of the data is not required. As an estimation of
 * the <tt>point query<\tt>, a double value is returned.
 *
 * @see xxl.core.math.queries.WindowQuery
 * @see xxl.core.math.queries.RangeQuery
 */

public interface PointQuery {

	/** Performs an estimation of the given <tt>point query</tt>.
	 *
	 * @param query point query to process
	 * @return a numerical value delivering an estimation of the point query
	 */
	public abstract double pointQuery(Object query);
}
