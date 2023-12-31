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

package xxl.core.spatial.cursors;

import java.io.File;
import java.util.Iterator;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.spatial.points.FloatPoint;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.rectangles.FloatPointRectangle;
import xxl.core.spatial.rectangles.Rectangle;


/**
 *	The UniverseFloat computes the universe (Minimal Bounding Rectangle, MBR or MBB) for an Iterator of FloatPoints.
 *	Note, that this universe is an aggregate (of type Rectangle).
 *
 */
public class UniverseFloat extends Aggregator{

	/** The aggregate function that computes the actual MBR.
	*/
	public static class UniverseFunction extends AggregationFunction{

		/** The dimensionality of the data
		 */
		protected int dim;

		/** Creates a new UniverseFunction.
		 *
		 * @param dim the dimensionality of the data
		 */
		public UniverseFunction(int dim){
			this.dim = dim;
		}

		/** prints debug information
		 * @param mbr a rectangle
		 * @param point a point
	 	 */
		protected void debugOut(Rectangle mbr, Point point){
			System.out.println("MBR-----------------------------------------");
			System.out.println(mbr);
			System.out.println("POINT---------------------------------------");
			System.out.println(point);
			System.out.println("--------------------------------------------\n\n");
		}

		/** invoke-method required by the aggregator
		 * @param aggregate is the current value for the aggregation
		 * @param next is the next object that is used for computing the next value of the aggregation
		 * @return the next aggregate value 
		 */
		public Object invoke(Object aggregate, Object next){

			Rectangle mbr = (Rectangle) aggregate;
			Point point = (Point) next;

			//debugOut(mbr,point);							//show current aggregate and next Point

			if(mbr==null)								//if aggregate == null
				mbr = new FloatPointRectangle((FloatPoint) point,(FloatPoint) point );	//initialize aggregate-Object

			float[] ll = (float[]) mbr.getCorner(false).getPoint();		//extract ll-point
			float[] ur = (float[]) mbr.getCorner(true).getPoint();		//extract ur-point
			float[] pointRep = (float[]) point.getPoint(); 

			for(int i=0; i<dim; i++){
				ll[i] = Math.min(ll[i], pointRep[i]);				//compute min for this dimension
				ur[i] = Math.max(ur[i], pointRep[i]);				//compute max for this dimension
			}
			return mbr;								//return new aggregate
		}
	}

	/** Creates a new UniverseFloat.
	 *
	 * @param iterator input iterator containing FloatPoints
	 * @param dim dimensionality of the data
	 */
	public UniverseFloat(Iterator iterator, int dim){
		super( iterator, new UniverseFunction(dim) );
	}

	/**
	 * use-case: compute universe for a file containing FloatPoints
	 * @param args: args[0] file-name, args[1] dimensionality of the data
	 **/
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("usage: java xxl.core.spatial.cursors.UniverseFloat <file-name> <dim>");
			return;	
		}

		final int dim = Integer.parseInt(args[1]);
		
		Aggregator ag =
			new UniverseFloat(
				new PointInputCursor(new File(args[0]), PointInputCursor.FLOAT_POINT, dim, 1024*1024),
				dim
		);
		

		System.out.println("# The universe of "+args[0]+" dim "+args[1]+" is");
		System.out.println(ag.last());
		ag.close();
	}
}
