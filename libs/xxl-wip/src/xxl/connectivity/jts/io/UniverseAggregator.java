/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.connectivity.jts.io;

import java.util.Iterator;

import xxl.core.cursors.mappers.Aggregator;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.rectangles.Rectangle;

/** This Aggregator incrementally computes the minimum bounding rectangle
 *  for an iterator of {@link Geometry2D Geometry} objects.
 *
 * @see xxl.core.cursors.mappers.Aggregator
 */
public class UniverseAggregator extends Aggregator<Geometry2D, Rectangle>{
	
	/** The aggregate function that computes the actual MBR.
	*/
	public static class UniverseFunction extends AggregationFunction<Geometry2D, Rectangle> {

		public Rectangle invoke(Rectangle aggregate, Geometry2D next){

			if(aggregate == null) aggregate = next.getMBR();
				else aggregate.union(next.getMBR());
			
			return aggregate;
		}
	}

	/** Creates a new UniverseAggregator.
	 *
	 * @param iterator input iterator containing geometries 
	 */
	public UniverseAggregator(Iterator<Geometry2D> iterator){
		super( iterator, new UniverseFunction());
	}
}
