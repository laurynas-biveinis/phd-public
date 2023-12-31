/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.spatial.cursors;

import java.util.Iterator;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.spatial.points.DoublePoint;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;


/**
 *	The UniverseDouble computes the universe (Minimal Bounding Rectangle, MBR or MBB) for an Iterator of DoublePoints.
 *	Note, that this universe is an aggregate (of type DefaultRectangle).
 *
 */
public class UniverseDouble extends Aggregator{

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

		/** 
		 * invoke-method required by the aggregator
		 * @param aggregate is the current value for the aggregation
		 * @param next is the next object that is used for computing the next value of the aggregation
		 * @return the next aggregate value 
		 */
		public Object invoke(Object aggregate, Object next){

			Rectangle mbr = (Rectangle) aggregate;
			Point point = (Point) next;

			if(mbr==null)								//if aggregate == null
				mbr = new DoublePointRectangle((DoublePoint) point, (DoublePoint)point );	//initialize aggregate-Object

			double[] ll = (double[]) mbr.getCorner(false).getPoint();		//extract ll-point
			double[] ur = (double[]) mbr.getCorner(true).getPoint();		//extract ur-point
			double[] pointRep = (double[]) point.getPoint(); 

			for(int i=0; i<dim; i++){
				ll[i] = Math.min(ll[i], pointRep[i]);				//compute min for this dimension
				ur[i] = Math.max(ur[i], pointRep[i]);				//compute max for this dimension
			}
			return mbr;								//return new aggregate
		}
	}

	/** Creates a new UniverseDouble.
	 *
	 * @param iterator input iterator containing DoublePoints
	 * @param dim dimensionality of the data
	 */
	public UniverseDouble(Iterator iterator, int dim){
		super( iterator, new UniverseFunction(dim) );
	}
}
