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
 *	A predicate that returns true if the first given geometry is within the other one.
 *
 */
public class Within extends AbstractPredicate<Geometry2D>{
	
	/** Default instance of this Object. */
	public static Within DEFAULT_INSTANCE = new Within();
	
	/** Creates a new Within-instance. */
	public Within(){
		super();
	}
		
	/** Returns true if the first given geometry isWithin the other one.
	 *
	 * @param left first geometry
	 * @param right second geometry
	 * @return returns true if geometry <tt>left</tt> isWithin geometry <tt>right</tt>
	 * 
	 */
	public boolean invoke(Geometry2D left, Geometry2D right){		
		return left.isWithin(right);
	}	
}
