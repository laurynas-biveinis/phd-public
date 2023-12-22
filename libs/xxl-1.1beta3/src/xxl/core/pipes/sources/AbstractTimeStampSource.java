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
package xxl.core.pipes.sources;

import xxl.core.functions.Function;
import xxl.core.math.Maths;
import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.pipes.metaData.PeriodicEvaluationMetaDataHandler;
import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.sinks.Sink;

/**
 * Superclass for sources that transfer <CODE>TimeStampedObjects</CODE>, i.e., the transferred elements consist of a value and 
 * a time stamp.
 * 
 * @param <O> type of the value of the elements
 * @param <O2> type of the implementation of TimeStampedObject
 *
 */
public abstract class AbstractTimeStampSource<O, O2 extends TimeStampedObject<O>> extends AbstractSource<O2> implements Heartbeat {
	
	protected boolean[] supportsHeartbeat;
	protected volatile boolean activateHeartbeats;
	protected boolean measureD = false;
	protected boolean measureG = false;
	
	/**
	 * Inner class for metadata management. 
	 */
	public class AbstractTimeStampSourceMetaDataManagement extends AbstractSourceMetaDataManagement {
		
		// measured parameters
		public static final String D = "D"; // average time distance between successive timestamps
		public static final String G = "G"; // time granularity as greatest common divisor of all occurring timestamps
		
		public static final String D_ESTIMATION = "D_ESTIMATION";
		public static final String G_ESTIMATION = "G_ESTIMATION";
				
		protected long costModelOutputCounter;
		protected long firstOutputTimeStamp;
		protected long lastOutputTimeStamp;
		
		protected double dSet;
		protected long g;
		protected long gSet;
		protected volatile short gState;
		
		/**
		 * 
		 */
		public AbstractTimeStampSourceMetaDataManagement() {
			super();
			this.dSet = Double.NaN;
			this.gSet = -1;
		}
		
		/**
		 * Used if parameter d is known in advance.
		 * 
		 * @param d
		 */
		public synchronized void setD(double d) {
			this.dSet = d;
			refresh(D_ESTIMATION);
		}
		
		/**
		 * Used if parameter g is known in advance.
		 * 
		 * @param g
		 */
		public synchronized void setG(long g) {
			this.gSet = g;
			refresh(G_ESTIMATION);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.AbstractSource.AbstractSourceMetaDataManagement#addMetaData(java.lang.Object)
		 */
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			if (super.addMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(D)) {
				measureD = true;
				costModelOutputCounter = 0;
				firstOutputTimeStamp = Long.MIN_VALUE;
				metaData.add(metaDataIdentifier, new PeriodicEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || firstOutputTimeStamp == Long.MIN_VALUE) 
									return Double.NaN;
								double d = costModelOutputCounter == 0 ? Double.NaN : 
									((double)lastOutputTimeStamp - firstOutputTimeStamp)/costModelOutputCounter;
								firstOutputTimeStamp = lastOutputTimeStamp;
								costModelOutputCounter = 0;
								return d;
							}
						}
					}, updatePeriod)
				);
				return true;
			}
			if (metaDataIdentifier.equals(G)) {
				measureG = true;
				g = 1;
				gState = 0;
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Long>(this, metaDataIdentifier,
					new Function<Object,Long>() {			
						@Override
						public Long invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || (gState < 2 && measureD)) 
									return -1l;
								return g;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(D_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								return dSet;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(G_ESTIMATION)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Long>(this, metaDataIdentifier,
					new Function<Object,Long>() {
						@Override
						public Long invoke() {
							synchronized(metaDataManagement) {
								return gSet;
							}
						}
					})
				);
				return true;
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.AbstractSource.AbstractSourceMetaDataManagement#removeMetaData(java.lang.Object)
		 */
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (super.removeMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(D)) {
				measureD = false;
				costModelOutputCounter = 0;
				firstOutputTimeStamp = Long.MIN_VALUE;
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(G)) {
				measureG = false;
				g = 1;
				gState = 0;
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(D_ESTIMATION) ||
			    metaDataIdentifier.equals(G_ESTIMATION)) {
				metaData.remove(metaDataIdentifier);
				return true;
			}
			return false;
		}
	}
	
	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * Transfers the source's elements as fast as possible, i.e., the attribute
	 * <CODE>period</CODE> of a <CODE>SourceProcessor</CODE> is set to 0.
	 * The output rate is not measured.
	 */
	public AbstractTimeStampSource() {
		this(0);
	}

	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * Creates a new instance of the class <CODE>SourceProcessor</CODE>
	 * using the given period.
	 * 
	 * @param period The constant period of time between two transferred successive elements.
	 */
	public AbstractTimeStampSource(long period) {
		this(new SourceProcessor(period));
	}

	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * 
	 * @param processor The thread simulating this source's activity.
	 */
	public AbstractTimeStampSource(SourceProcessor processor) {
		super(processor);
		this.activateHeartbeats = false;
	}
	
	static {
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractTimeStampSource.class, AbstractTimeStampSource.AbstractTimeStampSourceMetaDataManagement.D_ESTIMATION, AbstractTimeStampSource.AbstractTimeStampSourceMetaDataManagement.D);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractTimeStampSource.class, AbstractTimeStampSource.AbstractTimeStampSourceMetaDataManagement.G_ESTIMATION, AbstractTimeStampSource.AbstractTimeStampSourceMetaDataManagement.G);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.AbstractSource#subscribe(xxl.core.pipes.sinks.Sink, int)
	 */
	@Override
	public boolean subscribe(Sink<? super O2> sink, int sinkID) throws SourceIsClosedException {
		graph.WLock.lock();
		try {
			boolean ret = super.subscribe(sink, sinkID);
			if (ret) {
				if (supportsHeartbeat == null) {
					supportsHeartbeat = new boolean[]{sink instanceof Heartbeat};
				}
				else {
					boolean[] tmp = new boolean[supportsHeartbeat.length+1];
					System.arraycopy(supportsHeartbeat, 0, tmp, 0, supportsHeartbeat.length);
					tmp[supportsHeartbeat.length] = sink instanceof Heartbeat;
					supportsHeartbeat = tmp;
				}
			}
			return ret;
		}
		finally {
			graph.WLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.AbstractSource#unsubscribe(xxl.core.pipes.sinks.Sink, int)
	 */
	@Override
	public boolean unsubscribe(Sink<? super O2> sink, int sinkID) throws SourceIsClosedException {
		graph.WLock.lock();
		try {
			int index = -1;
			for (int i = 0; i < sinks.length; i++)
				if (sinks[i] == sink && sinkIDs[i] == sinkID) 
					index = i;
			boolean ret = super.unsubscribe(sink, sinkID);
			if (ret) {
				boolean[] tmp = new boolean[supportsHeartbeat.length-1];
				System.arraycopy(supportsHeartbeat, 0, tmp, 0, index);						
				if (index < supportsHeartbeat.length-1) {
					System.arraycopy(supportsHeartbeat, index+1, tmp, index, supportsHeartbeat.length-1-index);
				}
				supportsHeartbeat = tmp;
			}
			return ret;
		}
		finally {
			graph.WLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeat#heartbeat(long, int)
	 */
	public void heartbeat(long timeStamp, int sourceID) {
		if(!activateHeartbeats) return;
		processingWLock.lock();
		try {
			if(!activateHeartbeats) return;
			transferHeartbeat(timeStamp);
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/**
	 * Calls the <CODE>heartBeat</CODE> method for all subscribed sinks with the given timestamp. 
	 * @param timeStamp timestamp to transfer
	 */
	protected void transferHeartbeat(long timeStamp) {
		graph.RLock.lock();
		try {
			if (!isClosed && sinks != null) {										
				for (int i = 0; i < sinks.length; i++) {
					if (supportsHeartbeat[i])
						((Heartbeat)sinks[i]).heartbeat(timeStamp, sinkIDs[i]);
				}
			}
		}
		finally {
			graph.RLock.unlock();	
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeat#activateHeartbeats(boolean)
	 */
	public void activateHeartbeats(boolean on) {
		processingWLock.lock();
		try {
			this.activateHeartbeats = on;
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.AbstractSource#transfer(java.lang.Object)
	 */
	@Override
	public void transfer(O2 o) {
		super.transfer(o);
		if (measureD || measureG) {
			synchronized(metaDataManagement) {
				AbstractTimeStampSourceMetaDataManagement mdm = (AbstractTimeStampSourceMetaDataManagement)metaDataManagement;
				if (measureG) {
					if (mdm.gState > 1) {
						mdm.g = Maths.gcd(mdm.g, o.getTimeStamp());
					}
					else {
						if (mdm.gState == 1) {
							mdm.g = o.getTimeStamp();
							mdm.gState++;
						}
						else mdm.gState++;
					}
				}
				if (measureD) {
					if (mdm.firstOutputTimeStamp == Long.MIN_VALUE)
						mdm.firstOutputTimeStamp = o.getTimeStamp();
					else 
						mdm.costModelOutputCounter++;
					mdm.lastOutputTimeStamp = o.getTimeStamp();
				}
		    }
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.AbstractSource#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractTimeStampSourceMetaDataManagement();
	}

}
