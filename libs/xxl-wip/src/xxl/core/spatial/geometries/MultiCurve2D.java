/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries;

/** A <code>MultiCurve2D</code> is a one-dimensional GeometryCollection whose elements are Curves.
 *  In the SFS a MultiCurve is a non-instantiable class which defines a set of methods for its subclasses and
 *  is included for reasons of extensibility.
 *  <br><br>
 *  Specifications of MultiCurve2D according to the SFS:
 *  <ul>
 *   <li> A MultiCurve is simple if and only if all of its elements 
 *        are simple, the only intersections between any two elements
 *        occur at points that are on the boundaries of both elements. 
 *       </li>
 *   <li> The boundary of a MultiCurve is obtained by applying the ‘mod 2’ 
 *        union rule: A point is in the boundary of a MultiCurve if it is 
 *        in the boundaries of an odd number of elements of the MultiCurve. 
 *       </li>
 *   <li> A MultiCurve is closed if all of its elements are closed. The  
 *    	  boundary of a closed MultiCurve is always empty. 
 *       </li>
 *   <li> A MultiCurve is defined as topologically closed. 
 *       </li>
 *  </ul>
 *  <br><br>See <a href="./doc-files/ogc_sfs.pdf">Simple Feature Specification (pdf)</a>.
 * @param <T> The type of this collection's elements
 */
public interface MultiCurve2D<T extends Curve2D> extends GeometryCollection2D<T>{

	/** Returns <tt>true</tt> if this MultiCurve is closed 
	 * @return <tt>true</tt> if this MultiCurve is closed 
	 */
	public boolean isClosed();
		
	/** Returns the Length of this MultiCurve which is equal to the sum of the lengths of the element
	 *  Curves.
	 * @return the Length of this MultiCurve
	 */
	public double getLength();
			
}
