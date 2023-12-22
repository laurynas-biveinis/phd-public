/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries.cursors;

import xxl.core.cursors.filters.WhileTaker;
import xxl.core.predicates.AbstractPredicate;

/** A DistanceQuery delivers all neighbors of a query-object whose distance to the
 *  query- object is below epsilon.
 *  
 *  @see xxl.core.spatial.geometries.cursors.NearestNeighborQuery
 */
public class DistanceQuery extends WhileTaker<DistanceWeightedKPE>{

	/** Initializes the DistanceQuery.
	 * 
	 * @param nnQuery the query-operator which returns its results sorted by the minimum distance to 
	 * 				  the query object
	 * @param epsilon the maximum distance between the query object and qualifying result- objects 
	 */
	public DistanceQuery( NearestNeighborQuery nnQuery, final double epsilon){
		super( 	nnQuery, 
				new AbstractPredicate<DistanceWeightedKPE>(){
					public boolean invoke(DistanceWeightedKPE k){
						return k.getDistance()<= epsilon;
					}
				}
			);
	}
}
