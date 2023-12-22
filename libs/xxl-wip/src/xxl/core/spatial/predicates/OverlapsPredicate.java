/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.spatial.predicates;

import xxl.core.indexStructures.Descriptor;
import xxl.core.predicates.AbstractPredicate;

/**
 *	A predicate that returns true if object0 overlaps object1.
 *
 *  @see xxl.core.indexStructures.Descriptor
 *  @see xxl.core.predicates.Predicate
 *  @see xxl.core.spatial.predicates.OverlapsPredicate
 *
 */
public class OverlapsPredicate extends AbstractPredicate{

	/** Default instance of this Object.
	 */
	public static final OverlapsPredicate DEFAULT_INSTANCE = new OverlapsPredicate();

	/** Creates a new OverlapsPredicate-instance.
	 */
	public OverlapsPredicate(){
	}

	/** Returns true if object1 overlaps object0.
	 *
	 * The method is implemented as follows:
	 * <pre><code>
	 *
	 * public boolean invoke(Object left, Object right){
	 *	return ((Descriptor)left).overlaps( (Descriptor) right);
	 * } 
	 * </pre></code>
	 * 
	 * @param left first object
	 * @param right second object
	 * @return returns true if object <tt>left</tt> overlaps object <tt>right</tt>
	 * 
	 */
	public boolean invoke(Object left, Object right){
		return ((Descriptor)left).overlaps( (Descriptor) right);
	}
}
