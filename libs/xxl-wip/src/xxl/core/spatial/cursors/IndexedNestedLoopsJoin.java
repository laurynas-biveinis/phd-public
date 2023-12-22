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

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.indexStructures.RTree;
import xxl.core.spatial.KPE;
import xxl.core.spatial.rectangles.Rectangle;

/** A simple IndexedNestedLoopsJoin-operator: 
 *  The given {@link RTree}-index processes a window query for each {@link KPE}-object of the input iterator.  
 *  The join returns tuples of the current KPE query object and the leaf-entries of the tree return by the 
 *  window-queries.
 *  
 */
public class IndexedNestedLoopsJoin extends AbstractCursor<KPE[]> {

	/** The input iterator */
	protected Iterator<KPE> input;
	
	/** The index to query */
	protected RTree index;
	
	/** The next element of the iterator */
	protected KPE[] nextResult;
	
	/** The current query-object */
	protected KPE queryObject = null;
	
	/** Delivers the results of the current window-query */
	protected Cursor query;					

	/** The top-level constructor of this class. Creates a new Instance of IndexedNestedLoopsJoin.
	 * 
	 * @param input the first input iterator
	 * @param index the index on the second input
	 */
	public IndexedNestedLoopsJoin( Iterator<KPE> input, RTree index ){ 			
		this.input = input;
		this.index = index;
	}	
	
	@Override
	protected boolean hasNextObject() {
		nextResult = null;								
		while(nextResult==null){
			if( query == null || !query.hasNext()){
				// get next element of input1						
				if(input.hasNext()) queryObject = input.next();
					else break;
				query = index.query((Rectangle) queryObject.getData());
			}						
			nextResult = query.hasNext() ? new KPE[] { queryObject, ((KPE)query.next()) } : null; 
		}
		return nextResult!=null;
	}

	@Override
	protected KPE[] nextObject() {						
		return nextResult;
	}

	/** This method is not supported */
	public void remove() {					
		throw new UnsupportedOperationException();
	}		
}
