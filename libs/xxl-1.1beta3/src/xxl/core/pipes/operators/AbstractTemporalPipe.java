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
package xxl.core.pipes.operators;

import xxl.core.functions.Function;
import xxl.core.math.statistics.parametric.aggregates.Average;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.metaData.PeriodicEvaluationMetaDataHandler;
import xxl.core.pipes.sources.Source;

/**
 * Superclass for pipes that process <CODE>TemporalObject</CODE>, i.e., the process elements consist of a value and 
 * a time interval. 
 * @param <I> type of the value of the incoming elements
 * @param <O> type of the value of the outgoing elements
 */
public abstract class AbstractTemporalPipe<I,O> extends AbstractTimeStampPipe<I, O, TemporalObject<I>, TemporalObject<O>> {

	/**
	 * Inner class for metadata management. 
	 */
	public class AbstractTemporalPipeMetaDataManagement extends AbstractTimeStampPipeMetaDataManagement {
		
		// measured parameters
		public static final String L = "L"; // average length of time intervals
				
		protected volatile boolean measureL;
		protected volatile Double avgL;
		protected Average avgLFunction;
		
		/**
		 * 
		 */
		public AbstractTemporalPipeMetaDataManagement() {
			super();
			this.measureL = false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractTimeStampPipe.AbstractTimeStampPipeMetaDataManagement#addMetaData(java.lang.Object)
		 */
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {	
			if (super.addMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(L)) {
				measureL = true;
				avgL = Double.NaN;
				avgLFunction = new Average();
				metaData.add(metaDataIdentifier, new PeriodicEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
												
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed) 
									return Double.NaN;
								double l = avgL == null ? Double.NaN : avgL;
								avgL = null; // reset average function
								return l;
							}
						}
					}, updatePeriod)
				);
				return true;
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractTimeStampPipe.AbstractTimeStampPipeMetaDataManagement#removeMetaData(java.lang.Object)
		 */
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (super.removeMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(L)) {
				measureL = false;
				avgL = Double.NaN;
				avgLFunction = null;
				((PeriodicEvaluationMetaDataHandler)metaData.get(metaDataIdentifier)).close();
				metaData.remove(metaDataIdentifier);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * @param sources
	 * @param IDs
	 */
	public AbstractTemporalPipe(Source<? extends TemporalObject<I>>[] sources, int[] sourceIDs) {
		super(sources, sourceIDs);
	}

	/**
	 * Helps to create a new pipe in a query graph with the given sources.
	 * @param sources sources to connect the pipe with 
	 */
	public AbstractTemporalPipe(Source<? extends TemporalObject<I>>[] sources) {
		super(sources);
	}

	/**
	 * Helps to create a new pipe in a query graph with the given source.
	 * @param source source to connect the pipe with 
	 * @param sourceID ID of the source 
	 */
	public AbstractTemporalPipe(Source<? extends TemporalObject<I>> source, int sourceID) {
		super(source, sourceID);
	}

	/**
	 * Helps to create a new pipe in a query graph with the given source.
	 * @param source source to connect the pipe with 
	 */
	public AbstractTemporalPipe(Source<? extends TemporalObject<I>> source) {
		super(source);
	}

	/**
	 * Helps to create a new pipe in a query graph.
	 */
	public AbstractTemporalPipe() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#transfer(xxl.core.pipes.elements.TimeStampedObject)
	 */
	@Override
	public void transfer(TemporalObject<O> o) {
		super.transfer(o);
		AbstractTemporalPipeMetaDataManagement mdm = (AbstractTemporalPipeMetaDataManagement)metaDataManagement;
		if (mdm.measureL)
			synchronized(metaDataManagement) {
				if (mdm.measureL)
					mdm.avgL = (Double)mdm.avgLFunction.invoke(mdm.avgL, new Double(o.getEnd() - o.getStart()));
			}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractTimeStampPipe#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractTemporalPipeMetaDataManagement();
	}
	
}
