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
 *	A predicate that returns true if geometry0 touches geometry1.
 */
public class Touches extends AbstractPredicate<Geometry2D>{

	/** Default instance of this Object.
	 */
	public static Touches DEFAULT_INSTANCE = new Touches();
	
	/** Creates a new Touches-instance.
	 */
	public Touches(){
		super();
	}
	
	/** Returns true if geometry0 touches geometry1.
	 *
	 * @param geometry0 first geometry
	 * @param geometry1 second geometry
	 * @return returns true if <tt>geometry0</tt> touches object <tt>geometry1</tt>
	 * 
	 */
	public boolean invoke(Geometry2D geometry0, Geometry2D geometry1){		
		return geometry0.touches(geometry1);
	}	
}
