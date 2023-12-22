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
package xxl.core.pipes.operators.joins;

import xxl.core.collections.queues.DynamicHeap;
import xxl.core.collections.queues.Heap;
import xxl.core.collections.sweepAreas.*;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.functions.Function;
import xxl.core.functions.NTuplify;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.pipes.metaData.TriggeredEvaluationMetaDataHandler;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.queryGraph.Node;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.And;
import xxl.core.predicates.Or;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.RightBind;
import xxl.core.util.XXLSystem;
import xxl.core.util.metaData.ExternalTriggeredPeriodicMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataHandler;

import java.util.*;

import static xxl.core.pipes.elements.TemporalObject.END_TIMESTAMP_COMPARATOR;
import static xxl.core.pipes.elements.TemporalObject.START_TIMESTAMP_COMPARATOR;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.*;

/**
 * The binary temporal join operator.
 * @param <I> 
 * @param <O> 
 * 
 * @since 1.1
 */
public class TemporalJoin<I,O> extends AbstractTemporalPipe<I,O> implements MemoryMonitorable{

	public static Function<List<Double>,Double> D_ESTIMATION_FUNCTION = new Function() {
		@Override
		public Object invoke(Object p) {
            List<Double> params = (List<Double>)p;
			return (params.get(0)* params.get(2)) / ((params.get(1) + params.get(3)) * params.get(4));
		}
	};
	
	public static Function<List<Double>,Double> L_ESTIMATION_FUNCTION = new Function() {
		@Override
		public Object invoke(Object p) {
            List<Double> params = (List<Double>)p;
			return (params.get(0) * params.get(1)) / (params.get(0) + params.get(1));
		}
	};
	
	public static Function<List<Double>,Double> MEM_ESTIMATION_FUNCTION = new Function() {
		@Override
		public Object invoke(Object p) {
            List<Double> params = (List<Double>)p;
			return (((params.get(1) / params.get(0)) + (params.get(3) / (2*params.get(0)))) * params.get(2))
			     + (((params.get(4) / params.get(3)) + (params.get(0) / (2*params.get(3)))) * params.get(5));
		}
	};	
	
	public static interface TemporalJoinSA<I> extends SweepArea<TemporalObject<I>> {
						
		public abstract long getMinTimeStamp();
		
	}
		
	// elements linked in a list by start timestamps
    public static class TemporalJoinListSA<I> extends ListSA<TemporalObject<I>> implements TemporalJoinSA<I> {
    	
    	public class TemporalJoinListSAMetaDataManagement extends ImplementorBasedSAMetaDataManagement {
    		
    		public TemporalJoinListSAMetaDataManagement() {
    			super();
    		}
    		
    		@Override
    		protected boolean addMetaData(Object metaDataIdentifier) {
    			if (super.addMetaData(metaDataIdentifier))
    				return true;
    			return false;
    		}
    		
    		@Override
    		protected boolean removeMetaData(Object metaDataIdentifier) {
    			if (super.removeMetaData(metaDataIdentifier))
    				return true;
     			return false;
    		}
    		
    		public void createMetaDataManagement() {
    			if (metaDataManagement != null)
    				throw new IllegalStateException("An instance of MetaDataManagement already exists.");
    			metaDataManagement = new TemporalJoinListSAMetaDataManagement();
    		}

    	}
    	

    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, ReorganizeModes reorganizeMode, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>>[] removePredicates, List<TemporalObject<I>> list, int objectSize) {
    		super(impl, ID, false, reorganizeMode, queryPredicates, removePredicates, list, objectSize);
    		for (int i = 0; i < queryPredicates.length; i++) {
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    			this.removeRightBinds[i] = new RightBind<TemporalObject<I>>(this.removePredicates[i], null);
    		}
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    	}
    	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>>[] removePredicates, List<TemporalObject<I>> list) {
    		this(impl, ID, ReorganizeModes.LAZY_REORGANIZE, queryPredicates, removePredicates, list, SIZE_UNKNOWN);
    	}
   	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>> removePredicate, List<TemporalObject<I>> list, int objectSize) {
    		super(impl, ID, false, queryPredicates, new Or<TemporalObject<I>>(TemporalObject.INTERVAL_OVERLAP_REORGANIZE, removePredicate), list, objectSize);
    		for (int i = 0; i < queryPredicates.length; i++) 
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    	}    	

    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>> removePredicate, List<TemporalObject<I>> list) {
    		this(impl, ID, queryPredicates, removePredicate, list, SIZE_UNKNOWN);
    	}

    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, ReorganizeModes reorganizeMode, Predicate<? super TemporalObject<I>> queryPredicate, Predicate<? super TemporalObject<I>> removePredicate, int dim, List<TemporalObject<I>> list, int objectSize) {
    		super(impl, ID, false, reorganizeMode, queryPredicate, removePredicate, dim, list, objectSize);
    		for (int i = 0; i < queryPredicates.length; i++) {
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    			this.removeRightBinds[i] = new RightBind<TemporalObject<I>>(this.removePredicates[i], null);
    		}
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    	}
    	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, Predicate<? super TemporalObject<I>> removePredicate, int dim, List<TemporalObject<I>> list) {
    		this(impl, ID, ReorganizeModes.LAZY_REORGANIZE, queryPredicate, removePredicate, dim, list, SIZE_UNKNOWN);
    	}
    	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, int dim, List<TemporalObject<I>> list) {
    		super(impl, ID, false, TemporalObject.INTERVAL_OVERLAP_PREDICATE, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, dim, list);
    		for (int i = 0; i < queryPredicates.length; i++) 
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    	}
    	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, int dim, List<TemporalObject<I>> list) {
    		super(impl, ID, false, queryPredicate, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, dim, list);	
    		for (int i = 0; i < queryPredicates.length; i++)
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    	}

    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>>[] removePredicates) {
    		this(impl, ID, queryPredicates, removePredicates, new ArrayList<TemporalObject<I>>());
    	}
    	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>> removePredicate) {
    		this(impl, ID, queryPredicates, removePredicate, new ArrayList<TemporalObject<I>>());
    	}
    	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, Predicate<? super TemporalObject<I>> removePredicate, int dim) {
    		this(impl, ID, queryPredicate, removePredicate, dim, new ArrayList<TemporalObject<I>>());
    	}
    	
    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, int dim) {
    		this(impl, ID, queryPredicate, dim, new ArrayList<TemporalObject<I>>());
    	}

    	public TemporalJoinListSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, int dim) {
    		super(impl, ID, false, TemporalObject.INTERVAL_OVERLAP_PREDICATE, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, dim, new ArrayList<TemporalObject<I>>());
    	}
        	
    	public long getMinTimeStamp() {
    		return list.get(0).getStart();
    	}
    }	
    
    // elements arranged in a heap by end timestamps
    // TODO : Remove check for temporal overlap in query predicate
    public static class TemporalJoinHeapSA<I> extends HeapSA<TemporalObject<I>> implements TemporalJoinSA<I> {

    	protected List<TemporalObject<I>> list; // list ordered by start timestamps

    	protected volatile boolean countHeapOperations = false;

    	public class TemporalJoinHeapSAMetaDataManagement extends ImplementorBasedSAMetaDataManagement {
    		
    		public TemporalJoinHeapSAMetaDataManagement() {
    			super();
    		}
    		
    		public static final String HEAP_OPERATION_COUNT = "HEAP_OPERATION_COUNT";
    		protected long heapOperationCounter;
    		protected long heapOperationCount;
    		
    		public static final String SINGLE_HEAP_OPERATION_COSTS = "SINGLE_HEAP_OPERATION_COSTS";
    		protected double singleHeapOperationCosts;
    		public static final String HEAP_COSTS = "HEAP_COSTS";
    		protected volatile boolean measureHeapCosts = false;
    		protected double heapCosts = 0.0;
    		
    		public boolean needsPeriodicUpdate(Object metaDataIdentifier) {
    			if (metaDataIdentifier.equals(HEAP_OPERATION_COUNT) ||
    				metaDataIdentifier.equals(HEAP_COSTS)) {
    					return true;
    			}
    			return super.needsPeriodicUpdate(metaDataIdentifier);
    		}
    		
    		public void updatePeriodicMetaData(long period) {
    			super.updatePeriodicMetaData(period);
    			if (countHeapOperations) {
    				heapOperationCount = heapOperationCounter;
    				heapOperationCounter = 0;
    			}
    			if (measureHeapCosts) {
    				heapCosts = (heapOperationCount * singleHeapOperationCosts) / period;
    			}
    			if (measureCosts) {    				
    				costs += heapCosts;
    			}
    		}
    		    		
    		public void setSingleHeapOperationCosts(double costs) {
    			singleHeapOperationCosts = costs;
    		}
    		
    		public void setCostFactors(double [] queryPredicateCosts, double[] removePredicateCosts, double singleHeapOperationCosts) {
    			super.setCostFactors(queryPredicateCosts, removePredicateCosts);
    			setSingleHeapOperationCosts(singleHeapOperationCosts);
    		}
    		
    		@Override
    		protected boolean addMetaData(Object metaDataIdentifier) {
    			if (metaDataIdentifier.equals(SINGLE_HEAP_OPERATION_COSTS)) {
    				metaData.add(metaDataIdentifier, new Function<Object,Double>() {					
    					@Override
    					public Double invoke() {
    						return singleHeapOperationCosts;
    					}
    				});			
    				return true;
    			}
    			if (metaDataIdentifier.equals(HEAP_OPERATION_COUNT)) {
    				countHeapOperations = true;
    				heapOperationCounter = 0;
    				heapOperationCount = 0;
    				metaData.add(metaDataIdentifier, new Function<Object,Long>() {					
    					@Override
    					public Long invoke() {
    						return heapOperationCount;
    					}
    				});
    				return true;
    			}
    			if (metaDataIdentifier.equals(SINGLE_QUERY_PREDICATE_COSTS)) {
    				metaData.add(metaDataIdentifier, new Function<Object,double[]>() {					
    					@Override
    					public double[] invoke() {
    						return singleQueryPredicateCosts;
    					}
    				});		
    				return true;
    			}
    			if (metaDataIdentifier.equals(HEAP_COSTS)) {
    				include(HEAP_OPERATION_COUNT);
    				heapCosts = 0.0;
    				measureHeapCosts = true;
    				metaData.add(metaDataIdentifier, new Function<Object,Double>() {					
    					@Override
    					public Double invoke() {
    						return heapCosts;
    					}
    				});
    				return true;
    			}
    			if (metaDataIdentifier.equals(COST_MEASUREMENT)) {
    				include(HEAP_COSTS);
    				return super.addMetaData(metaDataIdentifier);
    			}
    			return super.addMetaData(metaDataIdentifier);
    		}
    		
    		@Override
    		protected boolean removeMetaData(Object metaDataIdentifier) {
    			if (metaDataIdentifier.equals(HEAP_OPERATION_COUNT)) {
    				countHeapOperations = false;
    				heapOperationCount = 0;
    				metaData.remove(metaDataIdentifier);
    				return true;
    			}
    			if (metaDataIdentifier.equals(HEAP_COSTS)) {
    				exclude(HEAP_OPERATION_COUNT);
    				heapCosts = 0.0;
    				measureHeapCosts = false;
    				metaData.remove(metaDataIdentifier);
    				return true;
    			}
    			if (metaDataIdentifier.equals(SINGLE_HEAP_OPERATION_COSTS)) {
    				metaData.remove(metaDataIdentifier);
    				return true;
    			}
    			if (metaDataIdentifier.equals(COST_MEASUREMENT)) {
    				exclude(HEAP_COSTS);
    				return super.removeMetaData(metaDataIdentifier);
    			}
    			return super.removeMetaData(metaDataIdentifier);
    		}
    		
    	}

    	
    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>>[] removePredicates, Heap<TemporalObject<I>> heap, int objectSize) {
    		super(impl, ID, false, queryPredicates, removePredicates, heap, objectSize);
    		for (int i = 0; i < queryPredicates.length; i++) {
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    			this.removeRightBinds[i] = new RightBind<TemporalObject<I>>(this.removePredicates[i], null);
    		}
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    		list = new ArrayList<TemporalObject<I>>();
    	}    

    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, Predicate<? super TemporalObject<I>> removePredicate, int dim, Heap<TemporalObject<I>> heap, int objectSize) {
    		super(impl, ID, false, queryPredicate, removePredicate, dim, heap, objectSize);
    		for (int i = 0; i < queryPredicates.length; i++) {
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    			this.removeRightBinds[i] = new RightBind<TemporalObject<I>>(this.removePredicates[i], null);
    		}
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    		list = new ArrayList<TemporalObject<I>>();
    	}
    	
    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, Predicate<? super TemporalObject<I>> removePredicate, int dim, Heap<TemporalObject<I>> heap) {
    		this(impl, ID, queryPredicate, removePredicate, dim, heap, SIZE_UNKNOWN);
    	}
    	
    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, int dim, Heap<TemporalObject<I>> heap) {
    		super(impl, ID, false, TemporalObject.INTERVAL_OVERLAP_PREDICATE, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, dim, heap);
    		for (int i = 0; i < queryPredicates.length; i++) 
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    		list = new ArrayList<TemporalObject<I>>();
    	}
    	
    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, int dim, Heap<TemporalObject<I>> heap) {
    		super(impl, ID, false, queryPredicate, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, dim, heap);	
    		for (int i = 0; i < queryPredicates.length; i++)
    			this.queryPredicates[i] = new And<TemporalObject<I>>(this.queryPredicates[i], TemporalObject.INTERVAL_OVERLAP_PREDICATE);
    		this.impl.setQueryPredicates(ID, this.queryPredicates);
    		list = new ArrayList<TemporalObject<I>>();
    	}

    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>>[] queryPredicates, Predicate<? super TemporalObject<I>>[] removePredicates) {
    		this(impl, ID, queryPredicates, removePredicates, new DynamicHeap<TemporalObject<I>>(END_TIMESTAMP_COMPARATOR), SIZE_UNKNOWN);
    	}
    	
    	
    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, Predicate<? super TemporalObject<I>> removePredicate, int dim) {
    		this(impl, ID, queryPredicate, removePredicate, dim, new DynamicHeap<TemporalObject<I>>(END_TIMESTAMP_COMPARATOR));
    	}
    	
    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, Predicate<? super TemporalObject<I>> queryPredicate, int dim) {
    		this(impl, ID, queryPredicate, dim, new DynamicHeap<TemporalObject<I>>(END_TIMESTAMP_COMPARATOR));
    	}

    	public TemporalJoinHeapSA(SweepAreaImplementor<TemporalObject<I>> impl, int ID, int dim) {
    		super(impl, ID, false, TemporalObject.INTERVAL_OVERLAP_PREDICATE, TemporalObject.INTERVAL_OVERLAP_REORGANIZE, dim, new DynamicHeap<TemporalObject<I>>(END_TIMESTAMP_COMPARATOR));
    		list = new ArrayList<TemporalObject<I>>();
    	}
    	
    	
    	@Override
		public void insert(TemporalObject<I> o) throws IllegalArgumentException {
    		super.insert(o);
    		list.add(o);
			if (countHeapOperations) {
				((TemporalJoinHeapSAMetaDataManagement)metaDataManagement).heapOperationCounter++;
			}
    	}
    
    	@Override
		public void clear() {
    		super.clear();
    		list.clear();
    	}
    	
    	@Override
		public void close() {
    		super.close();
    		list.clear();
    	}
    	
    	@Override
		public Iterator<TemporalObject<I>> expire (final TemporalObject<I> currentStatus, final int ID) {
    		if (selfReorganize || this.ID != ID) { 
    			return new AbstractCursor<TemporalObject<I>>() {
    				@Override
					public boolean hasNextObject() {
    					if (heap.isEmpty())
    						return false;
    					return removePredicates[ID].invoke(heap.peek(), currentStatus);
    				}
    				
    				@Override
					public TemporalObject<I> nextObject() {
    					next = heap.dequeue();
    					list.remove(Collections.binarySearch(list, next, START_TIMESTAMP_COMPARATOR));
    					impl.remove(next);
    					if (countHeapOperations) {
    						((TemporalJoinHeapSAMetaDataManagement)metaDataManagement).heapOperationCounter++;
    					}
    					return next;
    				}
    			};
    		}
    		return new EmptyCursor<TemporalObject<I>>();
    	}

    	@Override
		public void reorganize(TemporalObject<I> currentStatus, int ID) throws IllegalStateException {
    		if (selfReorganize || this.ID != ID) {
    			while(heap.size() > 0 && removePredicates[ID].invoke(heap.peek(), currentStatus)) {
    				TemporalObject<I> next = heap.dequeue();
    				list.remove(Collections.binarySearch(list, next, START_TIMESTAMP_COMPARATOR));
    				impl.remove(next);
    			}
    		}
    	}
    	
    	public long getMinTimeStamp() {
    		return list.get(0).getStart();
    	}
    	
		public void createMetaDataManagement() {
			if (metaDataManagement != null)
				throw new IllegalStateException("An instance of MetaDataManagement already exists.");
			metaDataManagement = new TemporalJoinHeapSAMetaDataManagement();
		}

    }   

	protected volatile boolean countPotentialProbes = false;
	protected volatile boolean countResults = false;
	protected volatile boolean countHeapOperations = false;
   
    /**
	 * Inner class for metadata management. 
	 */
	public class TemporalJoinMetaDataManagement extends AbstractTemporalPipeMetaDataManagement {
		
		public static final String JOIN_SELECTIVITY_MEASUREMENT = "JOIN_SELECTIVITY_MEASUREMENT";
		public static final String JOIN_SELECTIVITY = "JOIN_SELECTIVITY";
		
		public static final String HEAP_SIZE = "HEAP_SIZE";
		public static final String HEAP_OBJECT_SIZE = "HEAP_OBJECT_SIZE";
		public static final String HEAP_MEM_SIZE = "HEAP_MEM_SIZE";
		public static final String POTENTIAL_PROBES_COUNT = "POTENTIAL_PROBES_COUNT";
		public static final String OBJECT_SIZE = "OBJECT_SIZE";  // usage: "OBJECT_SIZE_i" -> objectSize of input i
		
		public static final String RESULT_COUNT = "RESULT_COUNT";
		public static final String RESULT_FUNCTION_COSTS = "RESULT_FUNCTION_COSTS";
		public static final String SINGLE_RESULT_FUNCTION_COSTS = "SINGLE_RESULT_FUNCTION_COSTS";
		protected double singleResultFunctionCosts;
		
		public static final String OUTHEAP_OPERATION_COUNT = "OUTHEAP_OPERATION_COUNT";
		public static final String OUTHEAP_OPERATION_COSTS = "OUTHEAP_OPERATION_COSTS";
		public static final String SINGLE_OUTHEAP_OPERATION_COSTS = "SINGLE_OUTHEAP_OPERATION_COSTS";
		protected double singleOutHeapOperationCosts;
		
		public static final String SWEEPAREA_PERIODIC_TRIGGER = "SWEEPAREA_PERIODIC_TRIGGER";
		public static final String SWEEPAREA_COSTS = "SWEEPAREA_COSTS";
						
		protected double sel;
		
		protected long potentialProbesCount;
		protected long resultCounter;
		protected long heapOperationCounter;
				
		public TemporalJoinMetaDataManagement() {
			super();
			this.sel = Double.NaN;
		}
		
		protected Set<Object> triggeredSweepAreaMetaData;
		
		public void setSingleResultFunctionCosts(double costs) {
			singleResultFunctionCosts = costs;
		}
		
		public void setSingleOutHeapOperationCosts(double costs) {
			singleOutHeapOperationCosts = costs;
		}
		
		public void setCostFactors(double singleResultFunctionCosts, double singleOutHeapOperationCosts) {
			setSingleResultFunctionCosts(singleResultFunctionCosts);
			setSingleOutHeapOperationCosts(singleOutHeapOperationCosts);
		}
		
		protected boolean addMetaDataFromSweepAreas(final Object metaDataIdentifier) {
			boolean res = false;
			for (int i=0; i<sweepAreas.length; i++) {	
				boolean needsTrigger = false;
				try {
					needsTrigger = ((ExternalTriggeredPeriodicMetaData)sweepAreas[i].getMetaDataManagement()).needsPeriodicUpdate(metaDataIdentifier);					
				}
				catch (ClassCastException e) {}
				if (sweepAreas[i].getMetaDataManagement().include(metaDataIdentifier)) {
					final int _i = i;
					final Function<Object,Object> metaDataFunction = new Function<Object,Object>() {			
						@Override
						public Object invoke() {
							synchronized(metaDataManagement) {
								return ((Function<?,Object>)sweepAreas[_i].getMetaData().get(metaDataIdentifier)).invoke();
							}
						}
					}; 
					if (needsTrigger) {
						if (triggeredSweepAreaMetaData==null)
							triggeredSweepAreaMetaData = new HashSet<Object>();
						triggeredSweepAreaMetaData.add(metaDataIdentifier+"_"+i);
						metaData.add(metaDataIdentifier+"_"+i, new TriggeredEvaluationMetaDataHandler<Object>(this, metaDataIdentifier, metaDataFunction));
					}
					else {
						metaData.add(metaDataIdentifier+"_"+i, new DefaultMetaDataHandler<Object>(this, metaDataIdentifier, metaDataFunction));
					}
					res = true;			
				}
			}
			return res;
		}
		
		protected boolean removeMetaDataFromSweepAreas(final Object metaDataIdentifier) {
			boolean res = false;
			for (int i=0; i<sweepAreas.length; i++) {				
				if (sweepAreas[i].getMetaDataManagement().exclude(metaDataIdentifier)) {
					metaData.remove(metaDataIdentifier+"_"+i);
					if (triggeredSweepAreaMetaData!=null) {
						triggeredSweepAreaMetaData.remove(metaDataIdentifier+"_"+i);
						if (triggeredSweepAreaMetaData.size()==0)
							triggeredSweepAreaMetaData = null;
					}
					res = true;			
				}				
			}
			return res;
		}
		
		@Override
		public synchronized Object[] getLocalDependencies(Object metaDataIdentifier) {
			if (triggeredSweepAreaMetaData!=null && triggeredSweepAreaMetaData.contains(metaDataIdentifier)) {
				return new Object[] { SWEEPAREA_PERIODIC_TRIGGER };
			}
			return MetaDataDependencies.getLocalMetaDataDependencies(enclosingClass, metaDataIdentifier);
		}
		
		// inverse function of getLocalDependencies
		@Override
		public synchronized Object[] affectsLocalMetaData(Object[] changedMDIdentifiers) {
			Object [] staticDependencies = MetaDataDependencies.affectsLocalMetaData((Class<? extends Node>)enclosingClass, changedMDIdentifiers); 
			if (triggeredSweepAreaMetaData!=null && triggeredSweepAreaMetaData.contains(SWEEPAREA_PERIODIC_TRIGGER)) {
				if (staticDependencies.length==0)
					return triggeredSweepAreaMetaData.toArray();
				HashSet<Object> resultset = new HashSet<Object>();
				resultset.addAll(triggeredSweepAreaMetaData);
				for (Object changedMDIdentifier : changedMDIdentifiers)
					resultset.add(changedMDIdentifier);
				return resultset.toArray();
			}
			return staticDependencies;
		}		
		
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			
			if (super.addMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(RESULT_FUNCTION_COSTS)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;
								long resultCount = (Long)((MetaDataHandler)metaData.get(RESULT_COUNT)).getMetaData();
								return (resultCount * singleResultFunctionCosts) / updatePeriod; 
							}
						}
					})
				);
				return true;
			}			
			if (metaDataIdentifier.equals(OUTHEAP_OPERATION_COSTS)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;
								long operationCount = (Long)((MetaDataHandler)metaData.get(OUTHEAP_OPERATION_COUNT)).getMetaData();
								return (operationCount * singleOutHeapOperationCosts) / updatePeriod; 
							}
						}
					})
				);
				return true;
			}					
			if (metaDataIdentifier.equals(SWEEPAREA_COSTS)) {
				boolean res = true;
				res &= sweepAreas[0].getMetaDataManagement().include(COST_MEASUREMENT);
				res &= sweepAreas[1].getMetaDataManagement().include(COST_MEASUREMENT);				
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;
								double result = 0;
								result += ((Function<?,Double>)sweepAreas[0].getMetaDataManagement().getMetaData().get(COST_MEASUREMENT)).invoke();
								result += ((Function<?,Double>)sweepAreas[1].getMetaDataManagement().getMetaData().get(COST_MEASUREMENT)).invoke();
								return result;
							}
						}
					})
				);
				return res;
			}		
			if (metaDataIdentifier.equals(COST_MEASUREMENT)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;
								double result = 0;
								result += (Double)((MetaDataHandler)metaData.get(RESULT_FUNCTION_COSTS)).getMetaData();
								result += (Double)((MetaDataHandler)metaData.get(OUTHEAP_OPERATION_COSTS)).getMetaData();
								result += (Double)((MetaDataHandler)metaData.get(SWEEPAREA_COSTS)).getMetaData();
								return result;
							}
						}
					})
				);
				return true;
			}					
			if (metaDataIdentifier.equals(JOIN_SELECTIVITY_MEASUREMENT)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;
								long resultCount = (Long)((MetaDataHandler)metaData.get(RESULT_COUNT)).getMetaData();
								long potentiellProbesCount = (Long)((MetaDataHandler)metaData.get(POTENTIAL_PROBES_COUNT)).getMetaData();
								double selMeasure = (potentiellProbesCount < 1 ||  resultCount==-1) ? Double.NaN : ((double)resultCount) / potentiellProbesCount;
								return selMeasure;
							}
						}
					}, 2)
				);
				return true;
			}
			if (metaDataIdentifier.equals(RESULT_COUNT)) {
				countResults = true;
				resultCounter = 0;
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Long>(this, metaDataIdentifier,
					new Function<Object,Long>() {			
						@Override
						public Long invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return -1l;
								Long res = resultCounter;
								resultCounter = 0;
								return res;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(POTENTIAL_PROBES_COUNT)) {
				countPotentialProbes = true;
				potentialProbesCount = 0;
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Long>(this, metaDataIdentifier,
					new Function<Object,Long>() {			
						@Override
						public Long invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return -1l;
								Long res = potentialProbesCount;
								potentialProbesCount = 0;
								return res;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(OUTHEAP_OPERATION_COUNT)) {
				countHeapOperations = true;
				heapOperationCounter = 0;
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Long>(this, metaDataIdentifier,
					new Function<Object,Long>() {			
						@Override
						public Long invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return -1l;
								Long res = heapOperationCounter;
								heapOperationCounter = 0;
								return res;
							}
						}
					})
				);
				return true;
			}			
			if (metaDataIdentifier.equals(JOIN_SELECTIVITY)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								return sel;
							}
						}
					})
				);
				return true;
			}						
			if (metaDataIdentifier.equals(D_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							if (isClosed || isDone)
								return Double.NaN;
							TemporalJoin.this.graph.RLock.lock();
							try {
								List<Double> params = new ArrayList<Double>();
								for (int i = 0; i < sources.length; i++) {
									params.add((Double)getMetaDataFragmentFromSource(i, D_ESTIMATION, Double.NaN));
									params.add((Double)getMetaDataFragmentFromSource(i, L_ESTIMATION, Double.NaN));
								}
								params.add(((Function<?,Double>)((MetaDataHandler)metaData.get(JOIN_SELECTIVITY)).getMetaData()).invoke()); 
								return D_ESTIMATION_FUNCTION.invoke(params);
							}
							finally {
								TemporalJoin.this.graph.RLock.unlock();
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(L_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							if (isClosed || isDone)
								return Double.NaN;
							TemporalJoin.this.graph.RLock.lock();
							try {
								List<Double> params = new ArrayList<Double>();
								for (int i = 0; i < sources.length; i++) 
									params.add((Double)getMetaDataFragmentFromSource(i, L_ESTIMATION, Double.NaN));
								return L_ESTIMATION_FUNCTION.invoke(params);
							}
							finally {
								TemporalJoin.this.graph.RLock.unlock();
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(G_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							if (isClosed || isDone)
								return Double.NaN;
							double gEst0 = (Double)getMetaDataFragmentFromSource(0, G_ESTIMATION, Double.NaN);
							double gEst1 = (Double)getMetaDataFragmentFromSource(1, G_ESTIMATION, Double.NaN);
							if (gEst0 != gEst1)
								throw new MetaDataException("Input granularities have to be equal.");
							return gEst0;
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(HEAP_SIZE)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Integer>(this, metaDataIdentifier,
					new Function<Object,Integer>() {			
						@Override
						public Integer invoke() {
							if (isClosed || isDone)
								return 0;
							synchronized(metaDataManagement) {
								try {
									return heap.size();
								}
								catch (NullPointerException npe) {
									return 0;
								}
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(HEAP_OBJECT_SIZE)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Integer>(this, metaDataIdentifier,
					new Function<Object,Integer>() {			
						@Override
						public Integer invoke() {
							synchronized(metaDataManagement) {
								return heapObjectSize;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(HEAP_MEM_SIZE)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Integer>(this, metaDataIdentifier,
					new Function<Object,Integer>() {			
						@Override
						public Integer invoke() {
							if (isClosed || isDone)
								return 0;							
							synchronized(metaDataManagement) {
								try {
									return heap.size() * heapObjectSize;
								}
								catch (NullPointerException npe) {
									return 0;
								}
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier instanceof String && ((String)metaDataIdentifier).startsWith(OBJECT_SIZE+"_")) {
				int [] borders = whichAreas(((String)metaDataIdentifier).substring(OBJECT_SIZE.length()+1));
				if (borders==null) 
					return false;
				boolean ret = true;
				for (int index=borders[0]; index<=borders[1]; index++) {
					final int _index = index;
					ret &= sweepAreas[index].getMetaDataManagement().include(OBJECT_SIZE);
					metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Integer>(this, OBJECT_SIZE+"_"+new Integer(index),
						new Function<Object,Integer>() {			
							@Override
							public Integer invoke() {
								if (isClosed || isDone)
									return 0;							
								synchronized(metaDataManagement) {
									return ((Function<?,Integer>)sweepAreas[_index].getMetaData().get(OBJECT_SIZE)).invoke();
								}
							}
						})
					);
				}
				return ret;
			}		
			if (metaDataIdentifier.equals(MEM_USAGE_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier, 
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							if (isClosed || isDone)
								return Double.NaN;							
							TemporalJoin.this.graph.RLock.lock();
							try {
								List<Double> params = new ArrayList<Double>();
								for (int i = 0; i < sources.length; i++) {
									params.add((Double)getMetaDataFragmentFromSource(i, D_ESTIMATION, Double.NaN));
									params.add((Double)getMetaDataFragmentFromSource(i, L_ESTIMATION, Double.NaN));
									MetaDataHandler mdh = (MetaDataHandler)metaData.get(OBJECT_SIZE+"_"+i);
									params.add(((Integer)mdh.getMetaData()).doubleValue());
								}
								return MEM_ESTIMATION_FUNCTION.invoke(params);
							}
							finally {
								TemporalJoin.this.graph.RLock.unlock();
							}	
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(SWEEPAREA_PERIODIC_TRIGGER)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Object>(this, metaDataIdentifier,
					new Function<Object,Object>() {			
						@Override
						public Object invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return null;
								for (int i=0; i<sweepAreas.length; i++) {
									try {
										((ExternalTriggeredPeriodicMetaData)sweepAreas[i].getMetaDataManagement()).updatePeriodicMetaData(updatePeriod);
									}
									catch (ClassCastException e) {}
								}
								return null;
							}
						}
					})
				);
				return true;
			}						
			if (addMetaDataFromSweepAreas(metaDataIdentifier))
				return true;
			return false;
		}
		
		protected int[] whichAreas(String indexString) {
			if (indexString.equals("*")) 
				return new int[] {0, sweepAreas.length-1};
			else {
				try {
					int index = new Integer(indexString);
					if (index < 0 || index >= sweepAreas.length) return null;
					return new int [] { index, index };
				}
				catch (NumberFormatException e) {
					return null;
				}				
			}
		}
		
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (super.removeMetaData(metaDataIdentifier))
				return true;
			if (metaDataIdentifier.equals(RESULT_COUNT)) {
				countResults = false;
				resultCounter = 0;
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(POTENTIAL_PROBES_COUNT)) {
				countPotentialProbes = false;
				potentialProbesCount = 0;
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(OUTHEAP_OPERATION_COUNT)) {
				countHeapOperations = false;
				heapOperationCounter = 0;
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(SWEEPAREA_COSTS)) {
				return true;
			}
			if (metaDataIdentifier.equals(RESULT_FUNCTION_COSTS) ||
				metaDataIdentifier.equals(OUTHEAP_OPERATION_COSTS) ||
				metaDataIdentifier.equals(COST_MEASUREMENT) ||
				metaDataIdentifier.equals(JOIN_SELECTIVITY_MEASUREMENT) ||
				metaDataIdentifier.equals(JOIN_SELECTIVITY) ||
				metaDataIdentifier.equals(D_ESTIMATION) ||
				metaDataIdentifier.equals(L_ESTIMATION) ||
				metaDataIdentifier.equals(G_ESTIMATION) ||
				metaDataIdentifier.equals(HEAP_SIZE) ||
				metaDataIdentifier.equals(HEAP_OBJECT_SIZE) ||
				metaDataIdentifier.equals(HEAP_MEM_SIZE) ||
				metaDataIdentifier.equals(MEM_USAGE_ESTIMATION) ||
				metaDataIdentifier.equals(SWEEPAREA_PERIODIC_TRIGGER)) {
					metaData.remove(metaDataIdentifier);
					return true;
			}
			if (((String)metaDataIdentifier).startsWith(OBJECT_SIZE)) {
				final int index = new Integer(((String)metaDataIdentifier).substring(12));
				if (index >= sweepAreas.length) return false;
				metaData.remove(metaDataIdentifier);
				return sweepAreas[index].getMetaDataManagement().exclude(OBJECT_SIZE);
			}
			if (removeMetaDataFromSweepAreas(metaDataIdentifier))
				return true;
			return false;
		}
		
		/**
		 * Used if the selectivity factor of the join predicate is known in advance.
		 * 
		 * @param sel
		 */
		public void setJoinSelectivity(double sel) {
			synchronized(metaDataManagement) {
				this.sel = sel;
			}
			refresh(JOIN_SELECTIVITY);
		}
		
	}
    
    
    protected TemporalJoinSA<I>[] sweepAreas = new TemporalJoinSA[2];
    protected Function<? super TemporalObject<I>,? extends TemporalObject<O>> newResult = null;
 
    protected Predicate<TemporalObject<O>> readyToTransfer = new Predicate<TemporalObject<O>>() {
		@Override
		public boolean invoke(TemporalObject<O> peek) {
			return peek.getStart() <= minTimeStamp;					
		}
	};
    protected Heap<TemporalObject<O>> heap;
	protected boolean checkedHeapObjectSize;
	protected int heapObjectSize;
	
    public static <P,R> Function<? super TemporalObject<P>, ? extends TemporalObject<R>> wrapResultFunction(final Function<? super P, ? extends R> newResult) {
			return new Function<TemporalObject<P>,TemporalObject<R>>() {
				@Override
				public TemporalObject<R> invoke(TemporalObject<P> o1, TemporalObject<P> o2) {
					return new TemporalObject<R>(
						newResult.invoke(o1.getObject(), o2.getObject()), 
						o1.getTimeInterval().intersect(o2.getTimeInterval())
					);
				}
				@Override
				public TemporalObject<R> invoke(List<? extends TemporalObject<P>> o) {
					ArrayList<P> v = new ArrayList<P>(o.size());
					TimeInterval[] t = new TimeInterval[o.size()];
					for (int i=0, l = o.size(); i < l; i++) {
						v.add(o.get(i).getObject());
						t[i] = o.get(i).getTimeInterval();
					}
					return new TemporalObject<R>(newResult.invoke(v),TimeInterval.intersect(t));					
				}
			};    	
    }
     
    public TemporalJoin(final TemporalJoinSA<I> sweepArea0, final TemporalJoinSA<I> sweepArea1, final Function<? super I, ? extends O> newResult, Heap<TemporalObject<O>> heap) {
		this.sweepAreas[0] = sweepArea0;
		this.sweepAreas[1] = sweepArea1;
		this.newResult = wrapResultFunction(newResult); 
		this.heap = heap;
		this.checkedHeapObjectSize = false;
		this.heapObjectSize = 0;
	}
    
	/** 
	 * Creates a new TemporalJoin as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately.
	 * @see MemoryMonitorable
	 *
	 * @param source0 This pipe gets subscribed to the specified source.
	 * @param source1 This pipe gets subscribed to the specified source.
	 * @param ID_0 This pipe uses the given ID for subscription to the first source.
	 * @param ID_1 This pipe uses the given ID for subscription to the second source.
	 * @param sweepArea0 TemporalJoinSweepArea managing the elements delivered by the first source.
	 * @param sweepArea1 TemporalJoinSweepArea managing the elements delivered by the second source.
	 * @param newResult Function to produce join results. If the function is an instance of 
	 * MetadataFunction and objectSize is unknown, this implementation tries to determine the value
	 * via the key OBJECT_SIZE.
	 * @param heap
	 */
    public TemporalJoin(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int sourceID_0, int sourceID_1, final TemporalJoinSA<I> sweepArea0, final TemporalJoinSA<I> sweepArea1, final Function<? super I, ? extends O> newResult, Heap<TemporalObject<O>> heap) {
		this(sweepArea0, sweepArea1, newResult, heap);
		if (!(Pipes.connect(source0, this, sourceID_0) && Pipes.connect(source1, this, sourceID_1)))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
	}

   	/**
   	 * Creates a new TemporalJoin as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately. <BR>
   	 * The object size is set to unknown.
   	 * 
	 * @param source0 This pipe gets subscribed to the specified source.
	 * @param source1 This pipe gets subscribed to the specified source.
	 * @param ID_0 This pipe uses the given ID for subscription to the first source.
	 * @param ID_1 This pipe uses the given ID for subscription to the second source.
	 * @param sweepArea0 TemporalJoinSweepArea managing the elements delivered by the first source.
	 * @param sweepArea1 TemporalJoinSweepArea managing the elements delivered by the second source.
	 * @param newResult Function to produce join results. If the function is an instance of 
	 * MetadataFunction and objectSize is unknown, this implementation tries to determine the value
	 * via the key OBJECT_SIZE.  
   	 */
	public TemporalJoin(Source<? extends TemporalObject<I>> source0, Source<? extends TemporalObject<I>> source1, int sourceID_0, int sourceID_1, TemporalJoinSA<I> sweepArea0, TemporalJoinSA<I> sweepArea1, Function<? super I, ? extends O> newResult) {
		this(source0, source1, sourceID_0, sourceID_1, sweepArea0, sweepArea1, newResult, new DynamicHeap<TemporalObject<O>>(TemporalObject.START_TIMESTAMP_COMPARATOR));
	}
	
	static {
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalJoin.class, MetaDataDependencies.ALL_SOURCES, D_ESTIMATION, D_ESTIMATION, L_ESTIMATION);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, D_ESTIMATION, TemporalJoin.TemporalJoinMetaDataManagement.JOIN_SELECTIVITY);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalJoin.class, MetaDataDependencies.ALL_SOURCES, L_ESTIMATION, L_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalJoin.class, MetaDataDependencies.ALL_SOURCES, G_ESTIMATION, G_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalJoin.class, MetaDataDependencies.ALL_SOURCES, MEM_USAGE_ESTIMATION, D_ESTIMATION, L_ESTIMATION);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, MEM_USAGE_ESTIMATION, OBJECT_SIZE+"_0", OBJECT_SIZE+"_1");
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.JOIN_SELECTIVITY_MEASUREMENT, TemporalJoin.TemporalJoinMetaDataManagement.RESULT_COUNT, TemporalJoin.TemporalJoinMetaDataManagement.POTENTIAL_PROBES_COUNT);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.RESULT_COUNT, AbstractPipe.AbstractPipeMetaDataManagement.SYNC_TRIGGER+"_ABSTRACTTIMESTAMPPIPE");
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.POTENTIAL_PROBES_COUNT, AbstractPipe.AbstractPipeMetaDataManagement.SYNC_TRIGGER+"_ABSTRACTTIMESTAMPPIPE");
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.OUTHEAP_OPERATION_COUNT, AbstractPipe.AbstractPipeMetaDataManagement.SYNC_TRIGGER+"_ABSTRACTTIMESTAMPPIPE");
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.RESULT_FUNCTION_COSTS, TemporalJoin.TemporalJoinMetaDataManagement.RESULT_COUNT);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.OUTHEAP_OPERATION_COSTS, TemporalJoin.TemporalJoinMetaDataManagement.OUTHEAP_OPERATION_COUNT);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.SWEEPAREA_PERIODIC_TRIGGER, AbstractPipe.AbstractPipeMetaDataManagement.SYNC_TRIGGER+"_ABSTRACTTIMESTAMPPIPE");
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, TemporalJoin.TemporalJoinMetaDataManagement.SWEEPAREA_COSTS, TemporalJoin.TemporalJoinMetaDataManagement.SWEEPAREA_PERIODIC_TRIGGER);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalJoin.class, COST_MEASUREMENT, TemporalJoin.TemporalJoinMetaDataManagement.RESULT_FUNCTION_COSTS, TemporalJoin.TemporalJoinMetaDataManagement.OUTHEAP_OPERATION_COSTS, TemporalJoin.TemporalJoinMetaDataManagement.SWEEPAREA_COSTS);
	}
	
	/** 
	 * Checks if the given ID is valid. Calls <CODE>super.process(o, ID)</CODE>, 
	 * which ensures a correct measurement of the input rate. 
	 * If the given element comes from the first source, the second sweep area
	 * is searched for corresponding elements. The resulting iterator is used to
	 * build the tuples, that are transferred as join-results to all subscribed sinks of this pipe.
	 * Thereafter the first sweep area is reorganized. <BR>
	 * If the specified element is delivered from the second source, the
	 * processing algorithm behaves symmetrically.
	 *
	 * @param o The element streaming in.
	 * @param ID One of the IDs this pipe specified during its subscription by an underlying source. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void processObject(TemporalObject<I> o, int sourceID) throws IllegalArgumentException {
		if (isDone) return; // see optimization
		
		int j = sourceID == sourceIDs[0] ? 0 : 1;
		int k = 1 - j;	
		sweepAreas[k].reorganize(o, j);
		
		// optimization
		if (sweepAreas[k].size() == 0 && receivedDone(sourceIDs[k])) {
			done(sourceID);
			return;
		}
		
		sweepAreas[j].insert(o);
		Iterator<? extends TemporalObject<I>> results = sweepAreas[k].query(o, j);
		int resCounter = 0;
		while(results.hasNext()) {
			TemporalObject<O> next = j == 0 ? 
				newResult.invoke(o, results.next()) 
				: newResult.invoke(results.next(), o);
			heap.enqueue(next);
			resCounter++;
		}
		
		if (countResults || countPotentialProbes || countHeapOperations) {
			synchronized(metaDataManagement) {
				TemporalJoinMetaDataManagement tjmdm = (TemporalJoinMetaDataManagement)metaDataManagement;
				if (countResults)
					tjmdm.resultCounter += resCounter;
				if (countPotentialProbes) 
					tjmdm.potentialProbesCount += sweepAreas[k].size();
				if (countHeapOperations) 
					tjmdm.heapOperationCounter += resCounter;
			}
		}
		transferHeap(countHeapOperations);
	}

	/**
	 * Calls <CODE>super.close()</CODE> and closes
	 * both sweep areas.
	 */
	@Override
	public void close() {
		super.close();
		if (isClosed()) {
			sweepAreas[0].close();
			sweepAreas[1].close();
			heap.close();
		}
	}
	
	@Override
	public void done(int sourceID) {
		if (isClosed || isDone) return;
		
		processingWLock.lock();
		updateDoneStatus(sourceID);
	    try {
			if (isDone()) {
				flushHeap();
				signalDone();
			}
			else { // optimization
				int j = Pipes.getSourceIndex(sourceIDs, sourceID);
				if (sweepAreas[j].size() == 0) 
					done(sourceIDs[1-j]);
			}
		}
	    finally {
	    	processingWLock.unlock();
	    }
	}

	public int getCurrentMemUsage() {
		if (!checkedHeapObjectSize && !heap.isEmpty()) {
			try {	
				heapObjectSize = XXLSystem.getObjectSize(heap.peek());
			} catch (IllegalAccessException e) {
				heapObjectSize = MemoryMonitorable.SIZE_UNKNOWN;
			}
			checkedHeapObjectSize = true;
		}
		int sa0Mem = sweepAreas[0].getCurrentMemUsage();
		int sa1Mem = sweepAreas[1].getCurrentMemUsage();
		if (sa0Mem == SIZE_UNKNOWN || sa1Mem == SIZE_UNKNOWN || heapObjectSize == SIZE_UNKNOWN)
			return SIZE_UNKNOWN;
		return sweepAreas[0].getCurrentMemUsage() + sweepAreas[1].getCurrentMemUsage() + heap.size()*heapObjectSize;		
	}

	protected void flushHeap() {
		while(!isClosed() && !heap.isEmpty())
			super.transfer(heap.dequeue());
	}
	
	protected boolean transferHeap(boolean countHeapOperations) {
		int transfercount = 0;
		while (!heap.isEmpty() && readyToTransfer.invoke(heap.peek())) {
			transfercount++;
			transfer(heap.dequeue()); 
		}
		if (countHeapOperations) {
			synchronized(metaDataManagement) {
				((TemporalJoinMetaDataManagement)metaDataManagement).heapOperationCounter += transfercount;
			}			
		}
		return transfercount>0;
	}
	
	@Override
	protected long processHeartbeat(long minTS, int sourceID) {
		if (sourceID != Heartbeat.MEMORY_MANAGER) {
			int j = sourceID == sourceIDs[0] ? 0 : 1;
			sweepAreas[1-j].reorganize(new TemporalObject<I>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), sourceID);
		}
		else {
			sweepAreas[0].reorganize(new TemporalObject<I>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), 1);
			sweepAreas[1].reorganize(new TemporalObject<I>(null, new TimeInterval(minTS, TimeInterval.INFINITY)), 0);
		}
		if (transferHeap(countHeapOperations))
			return NO_HEARTBEAT;
		if (heap.size()>0)
			return Math.min(heap.peek().getStart(), minTS);
		if (sweepAreas[0].size() >0 && sweepAreas[1].size() >0)
			return Math.min(Math.min(sweepAreas[0].getMinTimeStamp(), sweepAreas[1].getMinTimeStamp()), minTS);
		if (sweepAreas[0].size() >0 || sweepAreas[1].size() >0)
			return sweepAreas[0].size() >0  ? Math.min(sweepAreas[0].getMinTimeStamp(),minTS) : Math.min(sweepAreas[1].getMinTimeStamp(),minTS);
		return minTS;		
	}
	
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new TemporalJoinMetaDataManagement();
	}
	
    public static void main(String[] args) {
        
        /*********************************************************************/
		/*                    Example 1: Temporal Join                       */
		/*********************************************************************/
		
		final int noOfElements1 = 1500;
		final int noOfElements2 = 1000;
		final int buckets = 10;
		final int startInc = 10;
		final int intervalSize = 100;
		final long seed = 42;
		final HashMap<Integer,List<Long>> in1 = new HashMap<Integer,List<Long>>();
		final HashMap<Integer,List<Long>> in2 = new HashMap<Integer,List<Long>>();
		final Integer[] hashCodes = new Integer[buckets];
		for (int i = 0; i < buckets; i++)
			hashCodes[i] = new Integer(i);

		final Function<TemporalObject<Integer>,Integer> hash = new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> o) {
				return o.getObject()%buckets;
			}
		};
					
		RandomNumber<Integer> r1 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements1, 0
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, 0
		);		
		
		Source<TemporalObject<Integer>> s1 = Pipes.decorateWithRandomTimeIntervals(r1, in1, hashCodes, startInc, intervalSize, seed);
		Source<TemporalObject<Integer>> s2 = Pipes.decorateWithRandomTimeIntervals(r2, in2, hashCodes, startInc, intervalSize, seed);
						
		// Symmetric Hash-Join
		TemporalJoin<Integer,Object[]> join = new TemporalJoin<Integer,Object[]>(s1, s2, 0, 1,
			new TemporalJoinListSA<Integer>(
				new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
				0, 2
			),
			new TemporalJoinListSA<Integer>(
				new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
				1, 2
			),
			NTuplify.DEFAULT_INSTANCE
		);
		
		Pipes.verifyStartTimeStampOrdering(join);	
		
		AbstractSink sink = new AbstractSink<TemporalObject<Object[]>>(join) {
			LinkedList<TemporalObject<Object[]>> list = new LinkedList<TemporalObject<Object[]>>();
			
			@Override
			public void processObject(TemporalObject<Object[]> o, int sourceID) {				
				list.add(o);
			}
			
			@Override
			public void done(int sourceID) {
				super.done(sourceID);
				if (isDone()) {
					System.out.println("Verifying results ...");
					Iterator<TemporalObject<Object[]>> listIt = list.iterator();
					HashMap<Integer,List<Long>> result = new HashMap<Integer,List<Long>>();
					while (listIt.hasNext()) {
						TemporalObject<Object[]> t = listIt.next();
						int key = ((Integer)(t.getObject())[0]).intValue();
						List<Long> l = result.get(hashCodes[key]);
						if (l == null) l = new LinkedList<Long>();
						l.addAll(TemporalObject.getAllSnapshots(t));					
						result.put(hashCodes[key], l);
					}
					
					// check for multiplicity 
					for (int i = 0; i < buckets; i++) {
						List<Long> listIn1 = in1.get(hashCodes[i]);
						List<Long> listIn2 = in2.get(hashCodes[i]);
						List<Long> out = result.get(hashCodes[i]);
						if ((listIn1 != null || listIn2 != null) && out != null) {
							Iterator<Long> it = listIn1.iterator();					
							while (it.hasNext()) {
								Long next = it.next();
								if (listIn2.contains(next)) {
									if (!out.contains(next)) 
										System.err.println("ERROR: element lost: value "+i+" at time "+next);						
								}
							}
							System.out.println("CHECK 1 finished for bucket "+i+".");
							it = out.iterator();
							while (it.hasNext()) {
								Long next = it.next();
								if (!listIn1.contains(next) || !listIn2.contains(next)) 
									System.err.println("ERROR: more elements than expected: value "+i+" at time "+next);
								it.remove();	
							}
							if (out.size() > 0)
								System.err.println("ERROR: more elements than expected");
							System.out.println("CHECK 2 finished for bucket "+i+".");
						}
						else {
							if ((listIn1 == null || listIn2 == null) && out != null)
								System.err.println("ERROR: Output, but no valid input. (i = "+i+")");
						}
					}
				}
			}
		};
		
		Printer printer = new Printer<TemporalObject<Object[]>>(join);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(sink);
		exec.registerQuery(printer);
		exec.startAllQueries();
    }
}
