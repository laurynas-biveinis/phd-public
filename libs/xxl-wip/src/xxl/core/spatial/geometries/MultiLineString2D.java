/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries;

/** A <code>MultiLineString</code> is a {@link MultiCurve2D} whose elements are LineStrings.
 *  It does not provide additional functionality to MultiCurve2D but is implemented
 *  for	completeness.
 *  <br><br>See <a href="./doc-files/ogc_sfs.pdf">Simple Feature Specification (pdf)</a>.
 * @param <T> the type of this collection's elements
 */
public interface MultiLineString2D<T extends LineString2D> extends MultiCurve2D<T>{
}
