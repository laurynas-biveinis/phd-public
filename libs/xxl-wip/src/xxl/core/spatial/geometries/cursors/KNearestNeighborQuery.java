/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries.cursors;

import xxl.core.cursors.filters.Taker;

/** Performs a K-Nearest-Neighbor-Query by returning the results of a Nearest-Neighbor-Operator
 *  until the number of demanded elements is reached, or no further elements can be delivered.
 */
public class KNearestNeighborQuery extends Taker<DistanceWeightedKPE>{

	/** Initialized the KNearestNeighborQuery. 
	 * 
	 * @param nnQuery the NearestNeighborOperator whose results will be returned
	 * @param k the number of results to return from the NN-operator
	 */
	public KNearestNeighborQuery( NearestNeighborQuery nnQuery, int k){
		super( 	nnQuery, k );
	}
}
