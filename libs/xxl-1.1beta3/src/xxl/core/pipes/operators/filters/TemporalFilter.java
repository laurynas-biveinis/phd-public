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
/**
 * 
 */
package xxl.core.pipes.operators.filters;

import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.COST_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.COST_MEASUREMENT;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.D_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.G_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.L_ESTIMATION;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.pipes.metaData.TriggeredEvaluationMetaDataHandler;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.CountingPredicate;
import xxl.core.predicates.Predicate;
import xxl.core.util.metaData.MetaDataHandler;


/**
 * Operator component in a query graph that selects 
 * the temporal elements that stream in by using an unary predicate.
 * 
 * @param <I> 
 *
 */
public class TemporalFilter<I> extends AbstractTemporalPipe<I,I> {

	/**
	 * Unary selection predicate.
	 */
	protected Predicate<? super TemporalObject<I>> predicate;
	
	public static final Function<Double,Double> D_ESTIMATION_FUNCTION = new Function<Double,Double>() {
		@Override
		public Double invoke(Double d, Double sel) {
			return 1/sel * d;
		}
	};	
	
	/**
	 * Inner class for metadata management. 
	 */
	public class TemporalFilterMetaDataManagement extends AbstractTemporalPipeMetaDataManagement {
			
		public static final String FILTER_SELECTIVITY_MEASUREMENT = "FILTER_SELECTIVITY_MEASUREMENT";
		public static final String FILTER_SELECTIVITY = "FILTER_SELECTIVITY";
		public static final String SINGLE_PREDICATE_COSTS = "SINGLE_PREDICATE_COSTS";
		protected static final String PREDICATE_DECORATION = "PREDICATE_DECORATION";
				
		protected volatile double sel;
		protected volatile double singlePredicateEvaluationCosts;
			
		public TemporalFilterMetaDataManagement() {
			super();
			this.sel = Double.NaN;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractTemporalPipe.AbstractTemporalPipeMetaDataManagement#addMetaData(java.lang.Object)
		 */
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			if (super.addMetaData(metaDataIdentifier))
				return true;
			
			
			if (metaDataIdentifier.equals(PREDICATE_DECORATION)) {
				processingWLock.lock();
				try {
					predicate = new CountingPredicate<TemporalObject<I>>(predicate);
				}
				finally {
					processingWLock.unlock();
				}
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<long[]>(this, metaDataIdentifier,
					new Function<Object,long[]>() {			
						CountingPredicate<TemporalObject<I>> cp = (CountingPredicate<TemporalObject<I>>)predicate;

						@Override
						public long[] invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return null;
								long [] res = new long[2];
								res[0]=cp.getNoOfHits();
								res[1]=cp.getNoOfCalls();
								cp.resetCounters();
								return res;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(FILTER_SELECTIVITY_MEASUREMENT)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;								
								long[] counters = (long[])((MetaDataHandler)metaData.get(PREDICATE_DECORATION)).getMetaData();
								return ((double)counters[0])/counters[1];
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(FILTER_SELECTIVITY)) {
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
			if (metaDataIdentifier.equals(SINGLE_PREDICATE_COSTS)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								return singlePredicateEvaluationCosts;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(COST_MEASUREMENT)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;								
								long[] counters = (long[])((MetaDataHandler)metaData.get(PREDICATE_DECORATION)).getMetaData();
								double singlePredicateCosts = (Double)((MetaDataHandler)metaData.get(SINGLE_PREDICATE_COSTS)).getMetaData();
								return counters[1]*singlePredicateCosts/updatePeriod;
							}
						}
					})
				);
				return true;
			}			
			if (metaDataIdentifier.equals(COST_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return Double.NaN;	
								double dEstSource = (Double)getMetaDataFragmentFromSource(0, D_ESTIMATION, Double.NaN);
								double singlePredicateCosts = (Double)((MetaDataHandler)metaData.get(SINGLE_PREDICATE_COSTS)).getMetaData();
								return singlePredicateCosts/dEstSource;
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
							Object dEstSource = getMetaDataFragmentFromSource(0, D_ESTIMATION, Double.NaN);
							return D_ESTIMATION_FUNCTION.invoke((Double)dEstSource,
								(Double)((MetaDataHandler)metaData.get(FILTER_SELECTIVITY)).getMetaData() 
							);
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(L_ESTIMATION) ||
				metaDataIdentifier.equals(G_ESTIMATION)) {
					metaData.add(metaDataIdentifier, 
						TriggeredEvaluationMetaDataHandler.keepMetaDataFragmentFromSource(this, metaDataIdentifier, 0, Double.NaN)
					);
					return true;
				}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractTemporalPipe.AbstractTemporalPipeMetaDataManagement#removeMetaData(java.lang.Object)
		 */
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (super.removeMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(PREDICATE_DECORATION)) {
				if (!(isClosed || isDone)) {
					CountingPredicate<TemporalObject<I>> cp = (CountingPredicate<TemporalObject<I>>)predicate;
					processingWLock.lock();
					try {
						predicate = cp.getPredicate();
					}
					finally {
						processingWLock.unlock();
					}					
				}
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(FILTER_SELECTIVITY) ||
				metaDataIdentifier.equals(D_ESTIMATION) || 
				metaDataIdentifier.equals(L_ESTIMATION) ||
				metaDataIdentifier.equals(G_ESTIMATION) ||
				metaDataIdentifier.equals(SINGLE_PREDICATE_COSTS) ||
				metaDataIdentifier.equals(COST_MEASUREMENT) ||
				metaDataIdentifier.equals(COST_ESTIMATION) ||
				metaDataIdentifier.equals(FILTER_SELECTIVITY_MEASUREMENT)) {
				metaData.remove(metaDataIdentifier);
				return true;
			}
			return false;
		}
		
		/**
		 * Used if filter selectivity is known in advance.
		 * 
		 * @param sel
		 */
		public void setFilterSelectivity(double sel) {
			synchronized(metaDataManagement) {
				this.sel = sel;
			}
			refresh(FILTER_SELECTIVITY);
		}

		/**
		 * @param costs
		 */
		public void setSinglePredicateEvaluationCosts(double costs) {
			synchronized(metaDataManagement) {
				this.singlePredicateEvaluationCosts = costs;
			}
			refresh(SINGLE_PREDICATE_COSTS);
		}

	}
	
	
	/**
	 * @param predicate
	 */
	public TemporalFilter(Predicate<? super TemporalObject<I>> predicate) {
		this.predicate = predicate;
	}
	
	/** 
	 * Creates a new Filter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param sourceID This pipe uses the given ID for subscription.
	 * @param predicate Unary predicate used for filtering. If it returns <CODE>true</CODE>,
	 * 		the element will be transferred, otherwise it is discarded.
	 */ 
	public TemporalFilter(Source<? extends TemporalObject<I>> source, int sourceID, Predicate<? super TemporalObject<I>> predicate) {
		this(predicate);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");		
	}

	/** 
	 * Creates a new Filter as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately. 
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param predicate Unary predicate used for filtering. If it returns <CODE>true</CODE>,
	 * 		the element will be transferred, otherwise it is discarded.
	 */ 
	public TemporalFilter(Source<? extends TemporalObject<I>> source, Predicate<? super TemporalObject<I>> predicate) {
		this(source, DEFAULT_ID, predicate);
	}

	static {
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalFilter.class, 0, D_ESTIMATION, D_ESTIMATION);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalFilter.class, D_ESTIMATION, TemporalFilter.TemporalFilterMetaDataManagement.FILTER_SELECTIVITY);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalFilter.class, 0, L_ESTIMATION, L_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalFilter.class, 0, G_ESTIMATION, G_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalFilter.class, 0, COST_ESTIMATION, D_ESTIMATION);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalFilter.class, COST_ESTIMATION, TemporalFilter.TemporalFilterMetaDataManagement.SINGLE_PREDICATE_COSTS);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalFilter.class, COST_MEASUREMENT, TemporalFilter.TemporalFilterMetaDataManagement.SINGLE_PREDICATE_COSTS, TemporalFilter.TemporalFilterMetaDataManagement.PREDICATE_DECORATION);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalFilter.class, TemporalFilter.TemporalFilterMetaDataManagement.PREDICATE_DECORATION, AbstractPipe.AbstractPipeMetaDataManagement.SYNC_TRIGGER+"_ABSTRACTTIMESTAMPPIPE");
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalFilter.class, TemporalFilter.TemporalFilterMetaDataManagement.FILTER_SELECTIVITY_MEASUREMENT, TemporalFilter.TemporalFilterMetaDataManagement.PREDICATE_DECORATION);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(TemporalObject<I> o, int sourceID) throws IllegalArgumentException {
		if (predicate.invoke(o))
			super.transfer(o);
		else if (activateHeartbeats && lastHB < minTimeStamp && heartbeatPredicate.invoke()) {			
			transferHeartbeat(minTimeStamp);
			lastHB = minTimeStamp;
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTemporalPipe#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new TemporalFilterMetaDataManagement();
	}
	
}
