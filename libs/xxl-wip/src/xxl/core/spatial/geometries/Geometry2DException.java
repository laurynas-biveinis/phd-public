/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries;

/** If there's any error dealing with geometries an instance of
 *  this exception should be thrown.
 */
public class Geometry2DException extends RuntimeException{
		
	/** Create an instance of this object.
	 * @param message an error message 
	 */
	public Geometry2DException(String message){
	    super(message);
	}
}
