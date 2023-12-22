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
package xxl.core.pipes.operators;

import xxl.core.functions.Function;
import xxl.core.math.Maths;
import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.memoryManager.heartbeat.HeartbeatPipe;
import xxl.core.pipes.memoryManager.heartbeat.Heartbeats;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.PeriodicEvaluationMetaDataHandler;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sinks.SinkIsDoneException;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.predicates.Predicate;

/**
 * Superclass for pipes that process <CODE>TimeStampedObjects</CODE>, i.e., the process elements consist of a value and 
 * a time stamp.
 * 
 * @param <I> type of the value of the incoming elements
 * @param <O> type of the value of the outgoing elements
 * @param <I2> type of the implementation of TimeStampedObject of the incoming object
 * @param <O2> type of the implementation of TimeStampedObject of the outgoing object
 *
 */
public abstract class AbstractTimeStampPipe<I,O, I2 extends TimeStampedObject<I>, O2 extends TimeStampedObject<O>> extends AbstractPipe<I2, O2> implements HeartbeatPipe {
	
	/**
	 * An array containing the latest timestamp delivered by each source. 
	 */
	protected long[] latestTimeStamps;
	
	/**
	 * The minimum of lastTimeStamps
	 */
	protected long minTimeStamp;
	
	/**
	 * The value of the last heartbeat that has been delivered to all subscribed sinks that support heartbeats.
	 */
	protected long lastHB;
	
	/**
	 * The i-th position is true, if the i sink supports heartbeats.
	 */
	protected boolean[] supportsHeartbeat;
	
	/**
	 * Indicates if heartBeats are activated
	 */
	protected volatile boolean activateHeartbeats;
	
	/**
	 * A heartbeat is transfered to all subscribed sinks that support heartbeats, if it has a higher timestamp and this predicates evaluates
	 * to true. Therefore, this predicate allows to control the overhead due to the use of heartbeats.  
	 */
	@SuppressWarnings("unchecked")
	protected Predicate heartbeatPredicate = Heartbeats.everyNMillis(1000);
	
	protected volatile boolean measureD = false;
	protected volatile boolean measureG = false;

	/**
	 * Inner class for metadata management. 
	 */
	public class AbstractTimeStampPipeMetaDataManagement extends AbstractPipeMetaDataManagement {
		
		// measured parameters
		public static final String D = "D"; // average time distance between successive timestamps
		public static final String G = "G"; // time granularity as greatest common divisor of all occurring timestamps
			
		protected long costModelOutputCounter;
		protected long firstOutputTimeStamp;
		protected long lastOutputTimeStamp;
		
		protected long g;
		protected volatile short gState;
		
		/**
		 * 
		 */
		public AbstractTimeStampPipeMetaDataManagement() {
			super();
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement#addMetaData(java.lang.Object)
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
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement#removeMetaData(java.lang.Object)
		 */
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (super.removeMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(D)) {
				measureD = false;
				costModelOutputCounter = 0;
				firstOutputTimeStamp = Long.MIN_VALUE;
				((PeriodicEvaluationMetaDataHandler)metaData.get(metaDataIdentifier)).close();
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
			return false;
		}
	}
	
	/** 
	 * Helps to create a new pipe in a query graph with the given sources.
	 * @param sources sources to connect the pipe with
	 * @param IDs IDs of the sources
	 */
	public AbstractTimeStampPipe(Source<? extends I2>[] sources, int[] sourceIDs) {
		super(sources, sourceIDs);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/** 
	 * Helps to create a new pipe in a query graph with the given sources.
	 * @param sources sources to connect the pipe with
	 */
	public AbstractTimeStampPipe(Source<? extends I2>[] sources) {
		super(sources);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/** 
	 * Helps to create a new pipe in a query graph with the given source.
	 * @param source source to connect the pipe with
	 * @param ID ID of the source
	 */
	public AbstractTimeStampPipe(Source<? extends I2> source, int sourceID) {
		super(source, sourceID);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/**
	 * Helps to create a new pipe in a query graph with the given source.
	 * @param source source to connect the pipe with
	 */
	public AbstractTimeStampPipe(Source<? extends I2> source) {
		super(source);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/**
	 * Helps to create a new pipe in a query graph.
	 */
	public AbstractTimeStampPipe() {
		super();
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}
	
	/**
	 * Similar to the <CODE>subscribe</CODE> method, this method
	 * establishes a connection between this pipe and a source during runtime. The intention of
	 * both methods is to build up a double-linked query graph, which is completely navigatable. <BR>
	 * The method should be used to store a reference to the given underlying source
	 * together with the ID specified during this pipe's subscription. <BR>
	 * The source and the ID used during subscription are stored in internal arrays.
	 * Their positions in both arrays are equal.
	 *
	 * @param source The source this pipe got subscribed to.
	 * @param sourceID The ID this pipe used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the add operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the pipe has already finished its processing.
	 */
	@Override
	public boolean addSource(Source<? extends I2> source, int sourceID) throws SinkIsDoneException {
		graph.WLock.lock();
		try {
			boolean ret = super.addSource(source, sourceID);	
			if (ret) {
				if (latestTimeStamps == null) {
					latestTimeStamps = new long[]{0};
				}
				else {
					long[] newTS = new long[latestTimeStamps.length+1];
					System.arraycopy(latestTimeStamps, 0, newTS, 0, latestTimeStamps.length);
					newTS[latestTimeStamps.length] = 0;
					latestTimeStamps = newTS;
				}
			}
			return ret;
		}
		finally {
			graph.WLock.unlock();
		}
	}

	/**
	 * In conformity to the <CODE>unsubscribe</CODE> method, this
	 * method removes the connection between this pipe and a source on the pipe's side.
	 * Therefore it removes the given source with ID <CODE>sourceID</CODE> from the
	 * internal arrays used to store this pipe's references to its sources.
	 * 
	 * @param source The source this pipe got subscriped to.
	 * @param sourceID The ID this pipe used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the remove operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	@Override
	public boolean removeSource(Source<? extends I2> source, int sourceID) throws SinkIsDoneException {
		graph.WLock.lock();
		try {
			int index = -1;
			for (int i = 0; i < sources.length; i++)
				if (sources[i] == source && sourceIDs[i] == sourceID)
					index = i;
			boolean ret = super.removeSource(source, sourceID);	
			if (ret) {
				long[] newTS = new long[latestTimeStamps.length-1];
				System.arraycopy(latestTimeStamps, 0, newTS, 0, index);						
				if (index < latestTimeStamps.length-1) {
					System.arraycopy(latestTimeStamps, index+1, newTS, index, latestTimeStamps.length-1-index);
				}
				latestTimeStamps = newTS;
			}
			return ret;
		}
		finally {
			graph.WLock.unlock();
		}
	}

	/**
	 * This method will be called in the <CODE>transfer</CODE> method of each source
	 * this pipe got subscribed to. The element to be transferred will be sent to this pipe's
	 * sink part selecting it as the first paramenter. The second parameter specifies
	 * the ID this sink used during its subscription. Therefore the ID determines from
	 * which source this element is from. <BR>
	 * The element should not be cloned, if it is transferred to multiple sinks, which
	 * have been registered by a source. Due to performance reasons this pipe should rather 
	 * use the same element and pass it to all subscribed sinks based on the assumption
	 * that these sinks will not perform any changes on this element, unless they clone
	 * it themselves. <BR>
	 * The implementation consumes the element and updates metadata information. Furthermore,
	 * it calls the abstract method <CODE>processObject</CODE> in order to process the
	 * incoming object.
	 *
	 * @param o The element streaming in.
	 * @param sourceID One of the IDs this pipe specified during its subscription by the underlying sources. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	@Override
	public void process(I2 o, int sourceID) throws IllegalArgumentException {
		processingWLock.lock();
		graph.RLock.lock();
		try {
			long time = o.getTimeStamp();
			int index = Pipes.getSourceIndex(sourceIDs, sourceID);
			latestTimeStamps[index] = Math.max(time, latestTimeStamps[index]);
			updateMinTimeStamp();			
			super.process(o, sourceID);
			lastHB = Math.max(lastHB, minTimeStamp);
		}
		finally {
			graph.RLock.unlock();
			processingWLock.unlock();
		}		
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#close()
	 */
	@Override
	public void close() {
		super.close();
		processingWLock.lock();
		try {
			latestTimeStamps = null;
			supportsHeartbeat = null;
		}
		finally {
			processingWLock.unlock();
		}
	}
		
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#subscribe(xxl.core.pipes.sinks.Sink, int)
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
	 * @see xxl.core.pipes.operators.AbstractPipe#unsubscribe(xxl.core.pipes.sinks.Sink, int)
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
		
	/**
	 * Returns an array containing the latest timestamp delivered by each source.
	 * 
	 * @return array containing the latest timestamp delivered by each source
	 */
	public long[] getLatestTimeStamps() {
		processingRLock.lock();
		try {
			return latestTimeStamps;
		}
		finally {
			processingRLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeat#heartbeat(long, int)
	 */
	@SuppressWarnings("unchecked")
	public void heartbeat(long timeStamp, int sourceID) {
		if(!activateHeartbeats) return;
		processingWLock.lock();
		try {
			if(!activateHeartbeats) return;
			if (isOpened && ! isClosed && sinks != null) {
				boolean update = false; 
				if (sourceID != MEMORY_MANAGER) { // heartbeat from source
					int index = Pipes.getSourceIndex(sourceIDs, sourceID);
					if (latestTimeStamps[index] < timeStamp) {				
						latestTimeStamps[index] = timeStamp;
						update = true;
					}
				}
				else { // heartbeat from memory manager
					for (int i = 0; i < latestTimeStamps.length; i++)
						if (latestTimeStamps[i] < timeStamp) {
							latestTimeStamps[i] =  timeStamp;
							update = true;
						}
				}
				if (update)
					updateMinTimeStamp();	
				if (lastHB < minTimeStamp && heartbeatPredicate.invoke()) {
					long heartbeat = processHeartbeat(minTimeStamp, sourceID); 
					if (heartbeat > 0) 
						transferHeartbeat(heartbeat);
					lastHB = heartbeat > 0 ? heartbeat : minTimeStamp;
				}
			}
		}
		finally {
			processingWLock.unlock();	
		}
	}
	
	// returns the value of the heartbeat that is sent to all sinks.
	// a value <= 0 means that no heartbeat will be sent. 
	// only called inside heartbeat()
	/**
	 * 
	 * @param minTS
	 * @param sourceID
	 * @return
	 */
	protected long processHeartbeat(long minTS, int sourceID) {
		return minTS;
	}
	
	/**
	 * @param timeStamp
	 */
	protected void transferHeartbeat(long timeStamp) {
		graph.RLock.lock();
		try {
			if (isOpened && ! isClosed && sinks != null)
				for (int i = 0; i < sinks.length; i++)
					if (supportsHeartbeat[i])
						((Heartbeat)sinks[i]).heartbeat(timeStamp, sinkIDs[i]);
			}
		finally {
			graph.RLock.unlock();	
		}
	}

	/**
	 * 
	 */
	protected void updateMinTimeStamp() {
		minTimeStamp = Long.MAX_VALUE;
		for (long l : latestTimeStamps)
			minTimeStamp = Math.min(minTimeStamp, l);
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.AbstractPipe#updateDoneStatus(int)
	 */
	@Override
	public void updateDoneStatus(int sourceID) {
		processingWLock.lock();
		graph.RLock.lock();
		try {
			super.updateDoneStatus(sourceID);
			latestTimeStamps[Pipes.getSourceIndex(sourceIDs, sourceID)] = Long.MAX_VALUE;
		}
		finally {
			graph.RLock.unlock();
			processingWLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.HeartbeatPipe#setHeartbeatPredicate(xxl.core.predicates.Predicate)
	 */
	@SuppressWarnings("unchecked")
	public void setHeartbeatPredicate(Predicate heartbeatPredicate) {
		processingWLock.lock();
		try {
			this.heartbeatPredicate = heartbeatPredicate;
		}
		finally {
			processingWLock.unlock();	
		}
	}
	
	/**
	 * Activates or deactivates heartbeats for this sink. In order to activate heartbeats
	 * in a query graph top-down or bottom-up use static methods provided in Heartbeats.
	 * 
	 * @param on true activates heartbeats. 
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeats
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
	 * @see xxl.core.pipes.operators.AbstractPipe#transfer(java.lang.Object)
	 */
	@Override
	public void transfer(O2 o) {
		super.transfer(o);
		if (measureD || measureG) {
			synchronized(metaDataManagement) {
				AbstractTimeStampPipeMetaDataManagement mdm = (AbstractTimeStampPipeMetaDataManagement)metaDataManagement;
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
	 * @see xxl.core.pipes.operators.AbstractPipe#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractTimeStampPipeMetaDataManagement();
	}

	/**
	 * Returns minTimeStamp.
	 * @return minTimeStamp
	 */
	public long getMinTimeStamp() {
		processingRLock.lock();
		try {
			return minTimeStamp;
	    }
		finally {
			processingRLock.unlock();
		}
	}

	/**
	 * Returns the value of the last heartbeat.
	 * @return value of the last heartbeat
	 */
	public long getLastHB() {
		processingRLock.lock();
		try {
			return lastHB;
	    }
		finally {
			processingRLock.unlock();
		}		
	}
}
