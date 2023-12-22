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
package xxl.core.pipes.sources;

import xxl.core.functions.Function;
import xxl.core.math.statistics.parametric.aggregates.Average;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.PeriodicEvaluationMetaDataHandler;
import xxl.core.pipes.processors.SourceProcessor;

/**
 * Superclass for sources that transfer instances of <CODE>TemporalObject</CODE>, i.e., the transferred elements consist of a value and 
 * a time interval.
 * 
 * @param <O> type of the value
 */
public abstract class AbstractTemporalSource<O> extends AbstractTimeStampSource<O, TemporalObject<O>> {

	protected volatile boolean measureL = false;

	
	/**
	 * Inner class for metadata management. 
	 */
	public class AbstractTemporalSourceMetaDataManagement extends AbstractTimeStampSourceMetaDataManagement {
		
		// measured parameters
		public static final String L = "L"; // average length of time intervals
		
		public static final String L_ESTIMATION = "L_ESTIMATION";
		
		protected Double avgL;
		protected Double avgLSet;
		protected Average avgLFunction;
		
		/**
		 * 
		 */
		public AbstractTemporalSourceMetaDataManagement() {
			super();
			this.avgLSet = Double.NaN;
		}
		
		/**
		 * Used if parameter l is known in advance.
		 * 
		 * @param l
		 */
		public synchronized void setL(double l) {
			this.avgLSet = l;
			refresh(L_ESTIMATION);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.AbstractTimeStampSource.AbstractTimeStampSourceMetaDataManagement#addMetaData(java.lang.Object)
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
							System.out.println("L");
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
			if (metaDataIdentifier.equals(L_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						
					@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								return avgLSet;
							}
						}
					})
				);
				return true;
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.AbstractTimeStampSource.AbstractTimeStampSourceMetaDataManagement#removeMetaData(java.lang.Object)
		 */
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (super.removeMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(L)) {
				measureL = false;
				avgL = Double.NaN;
				avgLFunction = null;
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(L_ESTIMATION)) {
				metaData.remove(metaDataIdentifier);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Helps to create a new source that transfers instances of <CODE>TemporalObject</CODE>.
	 */
	public AbstractTemporalSource() {
		super();
	}

	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * Creates a new instance of the class <CODE>SourceProcessor</CODE>
	 * using the given period.
	 * 
	 * @param period The constant period of time between two transferred successive elements.
	 */
	public AbstractTemporalSource(long period) {
		super(period);
	}

	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * 
	 * @param processor The thread simulating this source's activity.
	 */
	public AbstractTemporalSource(SourceProcessor processor) {
		super(processor);
	}
		
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.AbstractTimeStampSource#transfer(xxl.core.pipes.elements.TimeStampedObject)
	 */
	@Override
	public void transfer(TemporalObject<O> o) {
		super.transfer(o);		
		if (measureL) {
			synchronized(metaDataManagement) {
				if (measureL) {
					AbstractTemporalSourceMetaDataManagement mdm = (AbstractTemporalSourceMetaDataManagement)metaDataManagement;
					mdm.avgL = (Double)mdm.avgLFunction.invoke(mdm.avgL, new Double(o.getEnd() - o.getStart()));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.AbstractTimeStampSource#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractTemporalSourceMetaDataManagement();
	}
	
}
