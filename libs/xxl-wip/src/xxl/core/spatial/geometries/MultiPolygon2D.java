/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries;

/** This is a {@link MultiSurface2D} which can only hold {@link Polygon2D}- objects
 *  It does not provide additional functionality to MultiSurface but is implemented
 *  for	completeness.
 *  <br><br>See <a href="./doc-files/ogc_sfs.pdf">Simple Feature Specification (pdf)</a>.
 * @param <T> the type of this collection's elements 
 */
public interface MultiPolygon2D<T extends Polygon2D> extends MultiSurface2D<T>{
}
