/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.core.spatial.geometries.cursors;

import xxl.core.cursors.filters.Filter;
import xxl.core.functions.Function;
import xxl.core.indexStructures.RTree;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Predicate;
import xxl.core.spatial.KPE;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.predicates.Intersects;

/** A region query returns those geometries which lie inside the given query- object
 *  or which intersect its edge. The window query is a special form of the region query
 *  with rectangular query objects.
 *  <br><br>
 *  Geometries are held in the leafs of the index-tree. The query is processed
 *  in two steps:
 *  <ul>
 *  	<li>query the index with the minimum bounding rectangle of the query- object</li>
 *  	<li>expand the real geometries from the resulting leafs and</li>
 *  	<li>filter the exact results using a refinement- predicate</li>
 *  </ul>
 */
public class RegionQuery extends Filter<KPE>{

	/** The main constructor of the class. It delivers a more general form
	 *  of the window query as you can specifiy a refinement- predicate other
	 *  than overlaps. It's important to use a predicate which relates to
	 *  overlaps, like <code>contains</code> or <code>covers</code>. Other predicates 
	 *  like <code>distanceWithin</code> lead to unreasonable results!
	 *   
	 * @param index the {@link RTree} which indexes the spatial data
	 * @param region the query object
	 * @param getGeometry a Function which determines how to extract geometries from the trees' leafs
	 * 		  
	 * @param refinementPredicate the predicate which identifies actual results
	 */
	public RegionQuery( RTree index, final Geometry2D region, final Function<KPE, Geometry2D> getGeometry, final Predicate<Geometry2D> refinementPredicate){
		super(  index.query(region.getMBR()),
			    new AbstractPredicate<KPE>(){
					public boolean invoke(KPE k){
						return refinementPredicate.invoke(region, getGeometry.invoke(k));
					}
				}
			);
	}
	
	/** This is the most original form of the window query which only returns
	 *  those geometries, which are overlapped by the query object.  
	 * 
	 * @param index the {@link RTree} which indexes the spatial data
	 * @param region the query object
	 * @param getGeometry a Function which determines how to extract the geometries from the trees' leafs
	 */
	public RegionQuery( RTree index, final Geometry2D region, Function<KPE, Geometry2D> getGeometry){
		this( index, region, getGeometry, Intersects.DEFAULT_INSTANCE);
	}
}
