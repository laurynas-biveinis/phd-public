/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.indexStructures;

import xxl.core.spatial.rectangles.Rectangle;

/**
 * This class contains <tt>static</tt> methods for descriptors.
 * 
 * @see Descriptor
 */
public abstract class Descriptors {

	/** Returns the union of the given descriptors (as a new Object).
	 * 
	 * @param d1 first descriptor
	 * @param d2 second descriptor
	 * @return the union of <tt>d1</tt> and <tt>d2</tt> 
	 */
	public static Descriptor union (Descriptor d1, Descriptor d2) {
		Descriptor d = (Descriptor)d1.clone();
		d.union(d2);
		return d;
	}

	/** Returns the union of the given rectangles (as a new Object).
	 * 
	 * @param r1 first rectangle
	 * @param r2 second rectangle
	 * @return the union of <tt>r1</tt> and <tt>r2</tt>
	 * 
	 * @see xxl.core.spatial.rectangles.Rectangle 
	 */
	public static Rectangle union (Rectangle r1, Rectangle r2) {
		Rectangle r = (Rectangle)r1.clone();
		r.union(r2);
		return r;
	}

}
