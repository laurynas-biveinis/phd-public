/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries.cursors;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.filters.Filter;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.Function;
import xxl.core.predicates.Predicate;

/** An abstract multi-step Spatial Join: the join takes an input cursor of
 *  possible join- candidates identified by a filterstep and returns those 
 *  candidates which satisfy the given refinement predicate.
 * 
 * @param <F> The return type of the filterstep (usually an array of KPEs).
 * @param <R> The return type of the refinement-step (usually an array of Geometry2D- objects).
 */
public class SpatialJoin<F, R> extends Filter<R>{		
	
	/** The constructor has to be called by subclasses to initialize the refinement- step.
	 * @param filterStep the join- candidates identified by the filter-step
	 * @param mapFilterResultToFinalResult a function to map candidate tuples to result- tuples
	 * @param refinementPredicate the predicate which has to be satisfied by result- candidates
	 */
	public SpatialJoin(Cursor<F> filterStep, Function<F, R> mapFilterResultToFinalResult, Predicate<R> refinementPredicate){
		super( 
			new Mapper<F,R>(
				mapFilterResultToFinalResult, 
				filterStep
				),
				refinementPredicate							 
		);
	}	
}
