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
package xxl.core.pipes.operators.mappers;

import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.COST_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.COST_MEASUREMENT;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.D_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.G_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.L_ESTIMATION;
import xxl.core.functions.CountingFunction;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.pipes.metaData.TriggeredEvaluationMetaDataHandler;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.sources.Source;
import xxl.core.util.metaData.MetaDataHandler;

/**
 * Operator component in a query graph that applies an user-defined
 * unary function to each incoming temporal element.
 * 
 * @param <I> 
 * @param <O> 
 *
 */
public class TemporalMapper<I,O> extends AbstractTemporalPipe<I, O> {

	/**
	 * Unary function applied to each incoming element:
	 * f: Object --> Object
	 */
	protected Function<? super TemporalObject<I>, ? extends TemporalObject<O>> mapping;

	
	/**
	 * Inner class for metadata management. 
	 */
	public class TemporalMapperMetaDataManagement extends AbstractTemporalPipeMetaDataManagement {
								
		public static final String SINGLE_FUNCTION_COSTS = "SINGLE_FUNCTION_COSTS";
		protected static final String FUNCTION_DECORATION = "FUNCTION_DECORATION";
		
		protected volatile double singleFunctionEvaluationCosts;
		
		public TemporalMapperMetaDataManagement() {
			super();			
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractTemporalPipe.AbstractTemporalPipeMetaDataManagement#addMetaData(java.lang.Object)
		 */
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			if (super.addMetaData(metaDataIdentifier))
				return true;
					
			if (metaDataIdentifier.equals(FUNCTION_DECORATION)) {
				processingWLock.lock();
				try {
					mapping = CountingFunction.createFunction(mapping);
				}
				finally {
					processingWLock.unlock();
				}
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Long>(this, metaDataIdentifier,
					new Function<Object,Long>() {			
						CountingFunction<? super TemporalObject<I>, ? extends TemporalObject<O>> cf = (CountingFunction<? super TemporalObject<I>, ? extends TemporalObject<O>>)mapping;

						@Override
						public Long invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone) 
									return null;
								long res = cf.getNoOfCalls();
								cf.resetCounter();
								return res;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(SINGLE_FUNCTION_COSTS)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								return singleFunctionEvaluationCosts;
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
								long counter = (Long)((MetaDataHandler)metaData.get(FUNCTION_DECORATION)).getMetaData();
								double singleFunctionCosts = (Double)((MetaDataHandler)metaData.get(SINGLE_FUNCTION_COSTS)).getMetaData();
								return counter*singleFunctionCosts/updatePeriod;
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
								double singleFunctionCosts = (Double)((MetaDataHandler)metaData.get(SINGLE_FUNCTION_COSTS)).getMetaData();
								return singleFunctionCosts/dEstSource;
							}
						}
					})
				);
				return true;
			}			

			
			if (metaDataIdentifier.equals(D_ESTIMATION) ||
				metaDataIdentifier.equals(L_ESTIMATION) ||
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
			if (metaDataIdentifier.equals(FUNCTION_DECORATION)) {
				if (!(isClosed || isDone)) {
					CountingFunction<? super TemporalObject<I>, ? extends TemporalObject<O>> cf = (CountingFunction<? super TemporalObject<I>, ? extends TemporalObject<O>>)mapping;
					processingWLock.lock();
					try {
						mapping = cf.getFunction();
					}
					finally {
						processingWLock.unlock();
					}					
				}
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(D_ESTIMATION) ||
				metaDataIdentifier.equals(L_ESTIMATION) ||
			    metaDataIdentifier.equals(G_ESTIMATION) ||
				metaDataIdentifier.equals(COST_MEASUREMENT) ||
				metaDataIdentifier.equals(COST_ESTIMATION) ||
			    metaDataIdentifier.equals(SINGLE_FUNCTION_COSTS) ) {
				return true;
			}
			return false;
		}
				
		public void setSingleFunctionEvaluationCosts(double costs) {
			synchronized(metaDataManagement) {
				this.singleFunctionEvaluationCosts = costs;
			}
			refresh(SINGLE_FUNCTION_COSTS);
		}

	}
	
	/**
	 * @param source
	 * @param ID
	 * @param mapping
	 */
	public TemporalMapper(Source<? extends TemporalObject<I>> source, int ID, Function<? super TemporalObject<I>, ? extends TemporalObject<O>> mapping) {
		super(source, ID);
		this.mapping = mapping;
	}

	/**
	 * @param source
	 * @param mapping
	 */
	public TemporalMapper(Source<? extends TemporalObject<I>> source, Function<? super TemporalObject<I>, ? extends TemporalObject<O>> mapping) {
		this(source, DEFAULT_ID, mapping);
	}

	/**
	 * @param mapping
	 */
	public TemporalMapper(Function<? super TemporalObject<I>, ? extends TemporalObject<O>> mapping) {
		this.mapping = mapping;
	}
	
	public TemporalMapper(Source<? extends TemporalObject<I>> source) {
		super(source, DEFAULT_ID);
	}

	static {
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalMapper.class, 0, D_ESTIMATION, D_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalMapper.class, 0, L_ESTIMATION, L_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalMapper.class, 0, G_ESTIMATION, G_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalMapper.class, 0, COST_ESTIMATION, D_ESTIMATION);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalMapper.class, COST_ESTIMATION, TemporalMapper.TemporalMapperMetaDataManagement.SINGLE_FUNCTION_COSTS);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalMapper.class, COST_MEASUREMENT, TemporalMapper.TemporalMapperMetaDataManagement.SINGLE_FUNCTION_COSTS, TemporalMapper.TemporalMapperMetaDataManagement.FUNCTION_DECORATION);
		MetaDataDependencies.addLocalMetaDataDependencies(TemporalMapper.class, TemporalMapper.TemporalMapperMetaDataManagement.FUNCTION_DECORATION, AbstractPipe.AbstractPipeMetaDataManagement.SYNC_TRIGGER+"_ABSTRACTTIMESTAMPPIPE");
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#processObject(java.lang.Object, int)
	 */
	@Override
	public void processObject(TemporalObject<I> o, int ID) throws IllegalArgumentException {
		super.transfer(mapping.invoke(o));		
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTemporalPipe#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new TemporalMapperMetaDataManagement();
	}

}
