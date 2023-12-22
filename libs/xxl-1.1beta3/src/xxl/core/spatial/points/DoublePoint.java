/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.core.spatial.points;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.core.functions.Function;
import xxl.core.io.Convertable;

/**
 * A Wrapper for double[]-points that provides useful methods on points
 * like e.g. a conversion mechanism.
 *
 * @see xxl.core.io.Convertable
 * @see xxl.core.spatial.points.Point
 * @see xxl.core.spatial.points.Points
 * @see xxl.core.spatial.points.AbstractPoint
 * @see xxl.core.spatial.points.FloatPoint
 *
 *
 */
public class DoublePoint extends AbstractPoint implements Convertable, Cloneable {

	/** A factory for DoublePoints.
	 */
	public static final Function FACTORY = new Function(){
		public Object invoke(Object point){
			return new DoublePoint( (double[]) point );
		}

		public Object invoke(Object object, Object dim){
			return new DoublePoint( ((Integer)dim).intValue() );
		}
	};

	/** The primitive double-point to be wrapped.
	 */
	protected double[] point;

	/** Creates a new DoublePoint.
	 *
	 *	@param point the primitive double-point to be wrapped.
	 */
	public DoublePoint(double[] point){
		this.point = point;
	}

	/** Creates a new DoublePoint.
	 *  (with coordinates (0,...,0)).
	 *
	 *	@param dim dimensionality of the point
	 */
	public DoublePoint(int dim){
		this(new double[dim]);
	}

	/** Returns a physical copy of this DoublePoint.
	 * @return returns a physical copy of this DoublePoint
	 */
	public Object clone(){
		return new DoublePoint((double[])point.clone());
	}

	/** Calculates the hashCode of this DoublePoint.
	 * @return returns the hashCode of this DoublePoint
	 */
	public int hashCode() {
		double c = 0;
		for (int i = 0; i < point.length; i++)
			c += point[i];
		return (int)c%1117;
	}

	/** Returns (gets) the primitive double-point wrapped by this DoublePoint.
	 * @return returns the primitive double-point wrapped by this DoublePoint
	 */
	public Object getPoint(){
		return point;
	}

	/** Returns the dimensionality of this DoublePoint.
	 * @return returns the dimensionality of this DoublePoint
	 */
	public int dimensions(){
		return point.length;
	}

	/**
	 * Reads the state (the attributes) for an object of this class from
	 * the specified data input and restores the calling object. The state
	 * of the object before calling <tt>read</tt> will be lost.<br>
	 * The <tt>read</tt> method must read the values in the same sequence
	 * and with the same types as were written by <tt>write</tt>.
	 *
	 * @param dataInput the stream to read data from in order to restore
	 *        the object.
	 * @throws IOException if I/O errors occur.
	 */
	public void read (DataInput dataInput) throws IOException{
		for(int i=0; i< point.length; i++)
			point[i] = dataInput.readDouble();
	}

	/**
	 * Writes the state (the attributes) of the calling object to the
	 * specified data output. This method should serialize the state of
	 * this object without calling another <tt>write</tt> method in order
	 * to prevent recursions.
	 *
	 * @param dataOutput the stream to write the state (the attributes) of
	 *        the object to.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public void write (DataOutput dataOutput) throws IOException{
		for(int i=0; i< point.length; i++)
			dataOutput.writeDouble(point[i]);
	}

	/** Returns the coordinate of this DoublePoint in a given dimension <dim>.
	 * 	
	 * @param dim dimension to get coordinate
	 * @return returns the coordinate in given dimension
	 */

	public double getValue(int dim){
		return point[dim];	
	}

	/** Returns a string representation of this object.
	 * @return a string representation of the object.
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<point.length; i++){
			sb.append(point[i]+"\t");
		}
		return sb.toString();
	}
}
