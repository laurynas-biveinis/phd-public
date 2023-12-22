/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries.predicates;

import xxl.core.predicates.AbstractPredicate;
import xxl.core.spatial.geometries.Geometry2D;

/**
 * A predicate that returns true if the spatial relationship specified by 
 * the <code>patternMatrix</code> holds.
 */
public class Relate extends AbstractPredicate<Geometry2D> {

	/** The matrix specifying the spatial relationship */
	protected String patternMatrix;
	
	/** Creates a new Relate-instance.
	 * @param patternMatrix the matrix specifying the spatial relationship
	 */
	public Relate(String patternMatrix){
		this.patternMatrix = patternMatrix;
	}
	
	/** Returns true if geometry0 relates to geometry1 in the specified way.
	 *
	 * @param g0 first geometry
	 * @param g1 second geometry
	 * @return returns true if <tt>geometry0</tt> overlaps object <tt>geometry1</tt>
	 * 
	 */
	public boolean invoke(Geometry2D g0, Geometry2D g1){
		return g0.relate(g1, patternMatrix);
	}
}
